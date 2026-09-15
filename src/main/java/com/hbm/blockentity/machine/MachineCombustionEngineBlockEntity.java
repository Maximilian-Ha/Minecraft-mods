package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.MachinePollutingBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.menus.MachineCombustionEngineMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.PistonsItem;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineCombustionEngine.
 *
 * Grosser Verbrennungsmotor als Multiblock. Verbrennt jeden Treibstoff mit
 * FT_Combustible, der Wirkungsgrad ergibt sich aus dem eingelegten Kolbensatz
 * (PistonsItem) und der Treibstoffguete. Die Drosselklappe ("setting") laeuft
 * von 0 bis 30 und bestimmt den Durchsatz: setting * 2 Zehntel-mB pro Tick.
 *
 * Alle Kennwerte sind unveraendert aus dem Original uebernommen.
 * Nicht uebernommen: die OpenComputers- und Redstone-over-Radio-Anbindung,
 * beides gibt es im Port nicht.
 */
public class MachineCombustionEngineBlockEntity extends MachinePollutingBlockEntity
        implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IControlReceiver, IFluidCopiable {

    public static final long MAX_POWER = 2_500_000L;
    public static final int FUEL_CAPACITY = 24_000;
    /** Puffergroesse der drei Rauchtanks, im Original der zweite Parameter von super(5, 50). */
    private static final int SMOKE_BUFFER = 50;
    /** Groesste einstellbare Drosselklappenstellung. */
    public static final int MAX_SETTING = 30;

    public static final int SLOT_FLUID_IN = 0;
    public static final int SLOT_FLUID_OUT = 1;
    public static final int SLOT_PISTONS = 2;
    public static final int SLOT_BATTERY = 3;
    public static final int SLOT_IDENTIFIER = 4;

    private static final int[] ACCESSIBLE_SLOTS = new int[] { SLOT_FLUID_IN, SLOT_FLUID_OUT, SLOT_BATTERY };

    public boolean isOn = false;
    public long power;
    private int playersUsing = 0;
    public int setting = 0;
    /** true, solange tatsaechlich verbrannt wird -- steuert Sound und Anzeige. */
    public boolean wasOn = false;

    public float doorAngle = 0F;
    public float prevDoorAngle = 0F;

    private AudioWrapper audio;

    public FluidTank tank;
    /** Zehntel-mB, die noch nicht zu einem vollen mB zusammengelaufen sind. */
    public int tenth = 0;

    private AABB renderBox = null;

    public MachineCombustionEngineBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_COMBUSTION_ENGINE.get(), pos, state, 5, SMOKE_BUFFER);
        this.tank = new FluidTank(Fluids.DIESEL, FUEL_CAPACITY);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.combustion_engine");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.tank.loadTank(this.level, SLOT_FLUID_IN, SLOT_FLUID_OUT, this.slots);
            if(this.tank.setType(SLOT_IDENTIFIER, this.slots)) {
                this.tenth = 0;
            }

            this.wasOn = false;

            int fill = this.tank.getFill() * 10 + this.tenth;
            ItemStack pistons = this.slots.get(SLOT_PISTONS);

            if(this.isOn && this.setting > 0 && pistons.getItem() instanceof PistonsItem && fill > 0 && this.tank.getTankType().hasTrait(FT_Combustible.class)) {

                FT_Combustible trait = this.tank.getTankType().getTrait(FT_Combustible.class);
                double eff = PistonsItem.getEfficiency(pistons, trait.getGrade());

                if(eff > 0) {
                    int speed = this.setting * 2;

                    int toBurn = Math.min(fill, speed);
                    this.power += (long) (toBurn * (trait.getCombustionEnergy() / 10_000D) * eff);
                    fill -= toBurn;

                    if(this.level.getGameTime() % 5 == 0 && toBurn > 0) {
                        this.pollute(this.tank.getTankType(), FluidReleaseType.BURN, toBurn * 0.5F);
                    }

                    if(toBurn > 0) {
                        this.wasOn = true;
                    }

                    this.tank.setFill(fill / 10);
                    this.tenth = fill % 10;
                }
            }

            this.power = Library.chargeItemsFromTE(this.slots, SLOT_BATTERY, this.power, this.power);

            // Ersatz fuer autoPort() aus 1.7.10: Strom anbieten, Treibstoff abonnieren, Rauch abgeben
            DirPos[] connections = this.getConPos();
            for(DirPos pos : connections) {
                if(this.power > 0) this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
                this.trySubscribe(this.tank.getTankType(), this.level, pos);
            }
            this.sendSmoke(connections);

            if(this.power > MAX_POWER) this.power = MAX_POWER;

            this.networkPackNT(50);

        } else {
            this.prevDoorAngle = this.doorAngle;
            float swingSpeed = (this.doorAngle / 10F) + 3F;

            if(this.playersUsing > 0) {
                this.doorAngle += swingSpeed;
            } else {
                this.doorAngle -= swingSpeed;
            }

            this.doorAngle = Mth.clamp(this.doorAngle, 0F, 135F);

            this.updateAudio();
        }
    }

    private void updateAudio() {
        if(this.wasOn) {

            if(this.audio == null) {
                this.audio = this.createAudioLoop();
                if(this.audio != null) this.audio.startSound();
            } else if(!this.audio.isPlaying()) {
                this.audio = this.rebootAudio(this.audio);
            }

            if(this.audio != null) {
                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(1F));
            }

        } else if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.IGENERATOR_OPERATE.get(), SoundSource.BLOCKS, this, 1.0F, 10F, 1.0F, 20);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    /**
     * Die vier Anschluesse liegen jeweils einen Block hinter den vier
     * Huellbloecken (EXTRA) an den Laengsseiten des Motors.
     */
    public DirPos[] getConPos() {
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos pos = this.getBlockPos();

        return new DirPos[] {
                new DirPos(pos.getX() + dir.getStepX() + rot.getStepX(), pos.getY(), pos.getZ() + dir.getStepZ() + rot.getStepZ(), dir),
                new DirPos(pos.getX() + dir.getStepX() - rot.getStepX(), pos.getY(), pos.getZ() + dir.getStepZ() - rot.getStepZ(), dir),
                new DirPos(pos.getX() - dir.getStepX() * 2 + rot.getStepX(), pos.getY(), pos.getZ() - dir.getStepZ() * 2 + rot.getStepZ(), dir.getOpposite()),
                new DirPos(pos.getX() - dir.getStepX() * 2 - rot.getStepX(), pos.getY(), pos.getZ() - dir.getStepZ() * 2 - rot.getStepZ(), dir.getOpposite())
        };
    }

    @Override public boolean canConnect(Direction dir) { return dir != null && dir != Direction.DOWN; }
    @Override public boolean canConnect(FluidType type, Direction dir) { return dir != null && dir != Direction.DOWN; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_FLUID_IN) return true;
        if(slot == SLOT_PISTONS) return stack.getItem() instanceof PistonsItem;
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_IDENTIFIER) return stack.getItem() instanceof IItemFluidIdentifier;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if(index == SLOT_FLUID_OUT) {
            return stack.is(NtmItems.CANISTER_EMPTY.get()) || stack.is(NtmItems.TANK_STEEL.get());
        }
        if(index == SLOT_BATTERY && stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) == battery.getMaxCharge(stack);
        }
        return false;
    }

    @Override
    public void startOpen(Player player) {
        if(this.level == null) return;
        if(!this.level.isClientSide) this.playersUsing++;
    }

    @Override
    public void stopOpen(Player player) {
        if(this.level == null) return;
        if(!this.level.isClientSide) this.playersUsing--;
    }

    @Override
    public boolean hasPermission(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 625D;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("turnOn")) this.isOn = !this.isOn;
        if(tag.contains("setting")) this.setting = Mth.clamp(tag.getInt("setting"), 0, MAX_SETTING);

        this.setChanged();
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = IFluidCopiable.super.getSettings(level, pos);
        tag.putBoolean("isOn", this.isOn);
        tag.putInt("burnRate", this.setting);
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        IFluidCopiable.super.pasteSettings(tag, index, level, player, pos);
        if(tag.contains("isOn")) this.isOn = tag.getBoolean("isOn");
        if(tag.contains("burnRate")) this.setting = Mth.clamp(tag.getInt("burnRate"), 0, MAX_SETTING);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.setting = tag.getInt("setting");
        this.power = tag.getLong("power");
        this.isOn = tag.getBoolean("isOn");
        this.tank.readFromNBT(tag, "tank");
        this.tenth = tag.getInt("tenth");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("setting", this.setting);
        tag.putLong("power", this.power);
        tag.putBoolean("isOn", this.isOn);
        this.tank.writeToNBT(tag, "tank");
        tag.putInt("tenth", this.tenth);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.playersUsing);
        buf.writeInt(this.setting);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.wasOn);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.playersUsing = buf.readInt();
        this.setting = buf.readInt();
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        this.wasOn = buf.readBoolean();
        this.tank.deserialize(buf);
    }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { this.tank, this.smoke, this.smokeLeaded, this.smokePoison };
    }

    /** Ersetzt getRenderBoundingBox() aus 1.7.10 -- deckt den gesamten Multiblock ab. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos pos = this.getBlockPos();
            this.renderBox = new AABB(
                    pos.getX() - 3,
                    pos.getY(),
                    pos.getZ() - 3,
                    pos.getX() + 4,
                    pos.getY() + 2,
                    pos.getZ() + 4
            );
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCombustionEngineMenu(id, inventory, this);
    }
}
