package com.hbm.blockentity.machine.storage;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineBatteryBlock;
import com.hbm.inventory.menus.MachineBatteryMenu;
import com.hbm.lib.Library;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.storage.TileEntityMachineBattery.
 *
 * Der klassische Energiespeicherblock in fuenf Stufen. Die Kapazitaet steckt im
 * Block (MachineBatteryBlock.maxPower) und wird beim ersten Aufruf gepuffert,
 * genau wie im Original.
 *
 * Vier Betriebsarten (redLow = ohne Redstonesignal, redHigh = mit Signal):
 *  0 Eingang, 1 Puffer, 2 Ausgang, 3 aus.
 * Nur im Puffermodus legt der Block einen eigenen Netzknoten an und wirkt damit
 * wie ein Kabel; in allen anderen Modi haengt er sich nur an die Netze der
 * Nachbarn an. Das entspricht dem Original und ist der Grund, weshalb hier
 * updateEntity() vollstaendig ueberschrieben wird, statt die Fassung von
 * BatteryBaseBlockEntity zu benutzen (die immer einen Knoten anlegt, weil
 * Sockel und FEnSU dauerhaft Leiter sind).
 *
 * Nicht uebernommen: OpenComputers-Anbindung und IInfoProviderEC (im Port nicht
 * vorhanden) sowie die Uebernahme eines Ambossnamens (MachineBaseBlockEntity
 * bietet dafuer keinen Setter).
 */
public class MachineBatteryBlockEntity extends BatteryBaseBlockEntity implements IPersistentNBT, IRORValueProvider, IRORInteractive {

    /** Ringpuffer der letzten 20 Ticks, daraus wird delta berechnet. */
    public long[] log = new long[20];
    /** Leistungsaenderung ueber die letzten 20 Ticks, also HE/s. */
    public long delta = 0;
    public long power = 0;

    /** Slot 0 laedt den Block aus Batterien, Slot 1 laedt Batterien aus dem Block. */
    public static final int SLOT_DISCHARGE = 0;
    public static final int SLOT_CHARGE = 1;

    private long bufferedMax = 0;

    public MachineBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_BATTERY.get(), pos, state, 2);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("container.battery");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;
        if(!(this.getBlockState().getBlock() instanceof MachineBatteryBlock)) return;

        if(this.priority == null || this.priority.ordinal() == 0 || this.priority.ordinal() == 4) {
            this.priority = ConnectionPriority.LOW;
        }

        int mode = this.getRelevantMode(false);

        long prevPower = this.power;

        this.power = Library.chargeItemsFromTE(this.slots, SLOT_CHARGE, this.power, this.getMaxPower());

        // Im Puffermodus wird der Block selbst zum Kabelknoten und versorgt sich
        // ueber das eigene Netz; sonst verhaelt er sich wie eine normale Maschine.
        if(mode == mode_buffer) {
            if(this.node == null || this.node.expired) {

                this.node = (PowerNode) UniNodespace.getNode(this.level, this.getBlockPos(), Nodespace.THE_POWER_PROVIDER);

                if(this.node == null || this.node.expired) {
                    this.node = this.createNode();
                    UniNodespace.createNode(this.level, this.node);
                }
            }

            // Original: tryProvide(world, x, y, z, ForgeDirection.UNKNOWN) plus
            // node.net.addReceiver(this). Da tryProvide auf dem eigenen Knoten
            // nichts anderes tut als addProvider, steht das hier direkt -- eine
            // Richtung UNKNOWN gibt es in 1.21 nicht.
            if(this.node != null && this.node.hasValidNet()) {
                this.node.net.addProvider(this);
                this.node.net.addReceiver(this);
            }
        } else {
            if(this.node != null) {
                UniNodespace.destroyNode(this.level, this.getBlockPos(), Nodespace.THE_POWER_PROVIDER);
                this.node = null;
            }

            for(Direction dir : Direction.values()) {
                BlockPos neighbor = this.getBlockPos().relative(dir);
                PowerNode dirNode = Nodespace.getNode(this.level, neighbor);

                if(mode == mode_output) {
                    this.tryProvide(this.level, neighbor, dir);
                } else {
                    if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
                }

                if(mode == mode_input) {
                    if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
                } else {
                    if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
                }
            }
        }

        byte comp = this.getComparatorPower();
        if(comp != this.lastRedstone) this.setChanged();
        this.lastRedstone = comp;

        this.power = Library.chargeTEFromItems(this.slots, SLOT_DISCHARGE, this.power, this.getMaxPower());

        long avg = (this.power + prevPower) / 2;
        this.delta = avg - this.log[0];

        for(int i = 1; i < this.log.length; i++) {
            this.log[i - 1] = this.log[i];
        }

        this.log[19] = avg;

        this.prevPowerState = this.power;

        this.networkPackNT(20);
    }

    /**
     * Abweichend von BatteryBaseBlockEntity die Originalformel: leer heisst 0,
     * alles darueber mindestens 1 (gegen Rundungsfehler bei der FEnSU).
     */
    @Override
    public byte getComparatorPower() {
        if(this.power == 0) return 0;
        double frac = (double) this.power / (double) this.getMaxPower() * 15D;
        return (byte) Mth.clamp((int) frac + 1, 0, 15);
    }

    public long getPowerRemainingScaled(long i) {
        return (this.power * i) / this.getMaxPower();
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }

    /** Im Original darf dieser Block direkt von Nachbarn beliefert werden. */
    @Override public boolean allowDirectProvision() { return true; }

    @Override
    public long getMaxPower() {
        if(this.bufferedMax == 0 && this.getBlockState().getBlock() instanceof MachineBatteryBlock battery) {
            this.bufferedMax = battery.maxPower;
        }

        return this.bufferedMax;
    }

    @Override
    public long getProviderSpeed() {
        int mode = this.getRelevantMode(true);
        return mode == mode_output || mode == mode_buffer ? this.getMaxPower() / 600 : 0;
    }

    @Override
    public long getReceiverSpeed() {
        int mode = this.getRelevantMode(true);
        return mode == mode_input || mode == mode_buffer ? this.getMaxPower() / 200 : 0;
    }

    @Override
    public BlockPos[] getPortPos() {
        return new BlockPos[] { this.getBlockPos() };
    }

    @Override
    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new DirPos[] {
                new DirPos(x + 1, y, z, Direction.EAST),
                new DirPos(x - 1, y, z, Direction.WEST),
                new DirPos(x, y + 1, z, Direction.UP),
                new DirPos(x, y - 1, z, Direction.DOWN),
                new DirPos(x, y, z + 1, Direction.SOUTH),
                new DirPos(x, y, z - 1, Direction.NORTH)
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if(stack.getItem() instanceof IBatteryItem batteryItem) {
            if(index == SLOT_DISCHARGE && batteryItem.getCharge(stack) == 0) return true;
            if(index == SLOT_CHARGE && batteryItem.getCharge(stack) == batteryItem.getMaxCharge(stack)) return true;
        }

        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if(direction == Direction.DOWN) return new int[] { SLOT_DISCHARGE, SLOT_CHARGE };
        if(direction == Direction.UP) return new int[] { SLOT_DISCHARGE };
        return new int[] { SLOT_CHARGE };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);

        buf.writeLong(this.power);
        buf.writeLong(this.delta);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        this.power = buf.readLong();
        this.delta = buf.readLong();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("Power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("Power", this.power);
    }

    @Override
    public void writeNBT(CompoundTag savedTag) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Power", this.power);
        tag.putLong("PrevPowerState", this.prevPowerState);
        tag.putShort("RedLow", this.redLow);
        tag.putShort("RedHigh", this.redHigh);
        tag.putByte("Priority", (byte) this.priority.ordinal());
        savedTag.put(NBT_PERSISTENT_KEY, tag);
    }

    @Override
    public void readNBT(CompoundTag savedTag) {
        CompoundTag tag = savedTag.getCompound(NBT_PERSISTENT_KEY);
        this.power = tag.getLong("Power");
        this.prevPowerState = tag.getLong("PrevPowerState");
        this.redLow = tag.getShort("RedLow");
        this.redHigh = tag.getShort("RedHigh");
        this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.class, tag.getByte("Priority"));
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineBatteryMenu(id, inventory, this);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_VALUE + "fill",
                PREFIX_VALUE + "fillpercent",
                PREFIX_VALUE + "delta",
                PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode (0-3)",
                PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback (0-3)",
                PREFIX_FUNCTION + "setredmode" + NAME_SEPARATOR + "mode (0-3)",
                PREFIX_FUNCTION + "setredmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback (0-3)",
                PREFIX_FUNCTION + "setpriority" + NAME_SEPARATOR + "priority (0-2)",
        };
    }

    @Override
    public String provideRORValue(String name) {
        if((PREFIX_VALUE + "fill").equals(name))        return "" + this.power;
        if((PREFIX_VALUE + "fillpercent").equals(name)) return "" + this.getPowerRemainingScaled(100);
        if((PREFIX_VALUE + "delta").equals(name))       return "" + this.delta;
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {

        if((PREFIX_FUNCTION + "setmode").equals(name) && params.length > 0) {
            int mode = IRORInteractive.parseInt(params[0], 0, 3);

            if(mode != this.redLow) {
                this.redLow = (short) mode;
                this.setChanged();
                return null;
            } else if(params.length > 1) {
                int altmode = IRORInteractive.parseInt(params[1], 0, 3);
                this.redLow = (short) altmode;
                this.setChanged();
                return null;
            }
            return null;
        }

        if((PREFIX_FUNCTION + "setredmode").equals(name) && params.length > 0) {
            int mode = IRORInteractive.parseInt(params[0], 0, 3);

            if(mode != this.redHigh) {
                this.redHigh = (short) mode;
                this.setChanged();
                return null;
            } else if(params.length > 1) {
                int altmode = IRORInteractive.parseInt(params[1], 0, 3);
                this.redHigh = (short) altmode;
                this.setChanged();
                return null;
            }
            return null;
        }

        if((PREFIX_FUNCTION + "setpriority").equals(name) && params.length > 0) {
            int priority = IRORInteractive.parseInt(params[0], 0, 2) + 1;
            this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.class, priority);
            this.setChanged();
            return null;
        }

        return null;
    }
}
