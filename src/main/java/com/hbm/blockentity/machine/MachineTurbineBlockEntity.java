package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.inventory.menus.MachineTurbineMenu;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineTurbine.
 *
 * Kleine Dampfturbine: wandelt Dampf ueber FT_Coolable in Strom und gibt den
 * abgekuehlten Dampf wieder aus. Einzelblock, alle sechs Seiten sind Anschluss.
 *
 * Nicht uebernommen: IConfigurableMachine (JSON-Konfiguration fehlt im Port),
 * die OpenComputers-Anbindung und IInfoProviderEC (beides gibt es im Port nicht).
 */
public class MachineTurbineBlockEntity extends MachineBaseBlockEntity implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IFluidCopiable {

    public static final int SLOT_ID_IN = 0;
    public static final int SLOT_ID_OUT = 1;
    public static final int SLOT_INPUT_LOAD = 2;
    public static final int SLOT_INPUT_UNLOAD = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_OUTPUT_LOAD = 5;
    public static final int SLOT_OUTPUT_UNLOAD = 6;

    // todo config: im Original ueber IConfigurableMachine (steamturbine.json) einstellbar
    public static long maxPower = 1_000_000L;
    public static int inputTankSize = 64_000;
    public static int outputTankSize = 128_000;
    public static int maxSteamPerTick = 6_000;
    public static double efficiency = 0.85D;

    private static final int[] SLOTS_TOP = new int[] { SLOT_BATTERY };
    private static final int[] SLOTS_BOTTOM = new int[] { SLOT_OUTPUT_UNLOAD };
    private static final int[] SLOTS_SIDE = new int[] { SLOT_BATTERY };

    public long power;
    public int age = 0;
    public FluidTank[] tanks;

    public MachineTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TURBINE.get(), pos, state, 7);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.STEAM, inputTankSize);
        this.tanks[1] = new FluidTank(Fluids.SPENTSTEAM, outputTankSize);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineTurbine");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.age++;
        if(this.age >= 2) this.age = 0;

        DirPos[] connections = this.getConPos();

        for(DirPos pos : connections) {
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
        }

        this.tanks[0].setType(SLOT_ID_IN, SLOT_ID_OUT, this.slots);
        this.tanks[0].loadTank(this.level, SLOT_INPUT_LOAD, SLOT_INPUT_UNLOAD, this.slots);
        this.power = Library.chargeItemsFromTE(this.slots, SLOT_BATTERY, this.power, maxPower);

        // Pufferverlust von 5% pro Tick, wie im Original
        this.power = (long) (this.power * 0.95D);

        FluidType in = this.tanks[0].getTankType();
        boolean valid = false;

        if(in.hasTrait(FT_Coolable.class)) {
            FT_Coolable trait = in.getTrait(FT_Coolable.class);
            double eff = trait.getEfficiency(CoolingType.TURBINE) * efficiency; // kleine Turbine schafft nur 85%

            if(eff > 0) {
                this.tanks[1].setTankType(trait.coolsTo);
                int inputOps = this.tanks[0].getFill() / trait.amountReq;
                int outputOps = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / trait.amountProduced;
                int cap = maxSteamPerTick / trait.amountReq;
                int ops = Math.min(inputOps, Math.min(outputOps, cap));
                this.tanks[0].setFill(this.tanks[0].getFill() - ops * trait.amountReq);
                this.tanks[1].setFill(this.tanks[1].getFill() + ops * trait.amountProduced);
                this.power += (long) (ops * trait.heatEnergy * eff);
                valid = true;
            }
        }

        if(!valid) this.tanks[1].setTankType(Fluids.NONE);
        if(this.power > maxPower) this.power = maxPower;

        for(DirPos pos : connections) {
            this.tryProvide(this.tanks[1], this.level, pos);
        }

        this.tanks[1].unloadTank(this.level, SLOT_OUTPUT_LOAD, SLOT_OUTPUT_UNLOAD, this.slots);

        this.networkPackNT(25);
    }

    /** Ersetzt subscribeToAllAround/sendFluidToAll aus 1.7.10: alle sechs Seiten */
    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Direction.NORTH),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Direction.UP),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Direction.DOWN)
        };
    }

    public long getPowerScaled(int i) {
        return (this.power * i) / maxPower;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        // Im Original nimmt nur der Batterieslot etwas an (isItemValidForSlot);
        // die uebrigen Slots der Oberflaeche sind einfache Vanilla-Slots.
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_ID_IN || slot == SLOT_INPUT_LOAD || slot == SLOT_OUTPUT_LOAD) return true;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if(direction == Direction.DOWN) return SLOTS_BOTTOM;
        if(direction == Direction.UP) return SLOTS_TOP;
        return SLOTS_SIDE;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        // canInsertItem im Original: nur was isItemValidForSlot erlaubt, also die Batterie
        return index == SLOT_BATTERY && stack.getItem() instanceof IBatteryItem;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.tanks[0].readFromNBT(tag, "water");
        this.tanks[1].readFromNBT(tag, "steam");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        this.tanks[0].writeToNBT(tag, "water");
        this.tanks[1].writeToNBT(tag, "steam");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }
    @Override public void setPower(long power) { this.power = power; }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineTurbineMenu(id, inventory, this);
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "L:maxPower", maxPower);
        inputTankSize = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSize);
        outputTankSize = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSize);
        maxSteamPerTick = IConfigurableMachine.grab(obj, "I:maxSteamPerTick", maxSteamPerTick);
        efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:maxPower").value(maxPower);
        writer.name("I:inputTankSize").value(inputTankSize);
        writer.name("I:outputTankSize").value(outputTankSize);
        writer.name("I:maxSteamPerTick").value(maxSteamPerTick);
        writer.name("D:efficiency").value(efficiency);
    }

}
