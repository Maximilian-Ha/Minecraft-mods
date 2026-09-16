package com.hbm.blockentity.machine.oil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineVacuumDistillMenu;
import com.hbm.inventory.recipes.VacuumRefineryRecipes;
import com.hbm.inventory.recipes.VacuumRefineryRecipes.VacuumRefineryRecipe;
import com.hbm.items.machine.IItemFluidIdentifier;
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

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill.
 *
 * Die Vakuumdestille zieht aus einem Oel vier Fraktionen statt der zwei des
 * Fraktionierturms -- dafuer muss das Oel unter Druck stehen und es braucht Strom.
 *
 * Zwoelf Plaetze, wie im Original. Die beiden fuer die Eingabe sind gesperrt: das Oel muss
 * unter Druck ankommen und laesst sich nicht aus einem Kanister einfuellen. Im Original
 * heisst dieser Platz SlotDeprecated; den Typ gibt es im Port nicht, hier weist canPlaceItem
 * ihn ab.
 */
public class MachineVacuumDistillBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

    public static final long MAX_POWER = 1_000_000L;
    public static final long POWER_PER_BATCH = 10_000L;
    public static final int INPUT_PER_BATCH = 100;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FLUID_ID = 11;

    public long power;
    public boolean isOn;
    public final FluidTank[] tanks;

    public MachineVacuumDistillBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_VACUUM_DISTILL.get(), pos, state, 12);

        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.OIL, 64_000).withPressure(2),
                new FluidTank(Fluids.HEAVYOIL_VACUUM, 24_000),
                new FluidTank(Fluids.REFORMATE, 24_000),
                new FluidTank(Fluids.LIGHTOIL_VACUUM, 24_000),
                new FluidTank(Fluids.SOURGAS, 24_000)
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_vacuum_distill");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.isOn = false;

        if(this.level.getGameTime() % 20 == 0) {
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            }
        }

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);
        this.tanks[0].setType(SLOT_FLUID_ID, this.slots);

        this.distill();

        this.tanks[1].unloadTank(this.level, 3, 4, this.slots);
        this.tanks[2].unloadTank(this.level, 5, 6, this.slots);
        this.tanks[3].unloadTank(this.level, 7, 8, this.slots);
        this.tanks[4].unloadTank(this.level, 9, 10, this.slots);

        for(DirPos pos : this.getConPos()) {
            for(int i = 1; i < this.tanks.length; i++) {
                if(this.tanks[i].getFill() > 0) this.tryProvide(this.tanks[i], this.level, pos);
            }
        }

        this.networkPackNT(150);
    }

    private void distill() {

        VacuumRefineryRecipe recipe = VacuumRefineryRecipes.getVacuum(this.tanks[0].getTankType());

        if(recipe == null) {
            for(int i = 1; i < this.tanks.length; i++) this.tanks[i].setTankType(Fluids.NONE);
            return;
        }

        FluidStack[] outputs = recipe.outputs;
        for(int i = 0; i < outputs.length; i++) this.tanks[i + 1].setTankType(outputs[i].type);

        if(this.power < POWER_PER_BATCH) return;
        if(this.tanks[0].getFill() < INPUT_PER_BATCH) return;

        for(int i = 0; i < outputs.length; i++) {
            if(this.tanks[i + 1].getFill() + outputs[i].fill > this.tanks[i + 1].getMaxFill()) return;
        }

        this.isOn = true;
        this.power -= POWER_PER_BATCH;
        this.tanks[0].setFill(this.tanks[0].getFill() - INPUT_PER_BATCH);

        for(int i = 0; i < outputs.length; i++) {
            this.tanks[i + 1].setFill(this.tanks[i + 1].getFill() + outputs[i].fill);
        }

        this.setChanged();
    }

    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Direction.EAST),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Direction.EAST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Direction.WEST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Direction.WEST),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Direction.NORTH),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Direction.NORTH)
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_FLUID_ID) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot == 3) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[1].getTankType()).isEmpty();
        if(slot == 5) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[2].getTankType()).isEmpty();
        if(slot == 7) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[3].getTankType()).isEmpty();
        if(slot == 9) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[4].getTankType()).isEmpty();
        /* Die Plaetze 1 und 2 bleiben leer: das Oel muss unter Druck ankommen. */
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 4 || index == 6 || index == 8 || index == 10;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.isOn = tag.getBoolean("isOn");
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(tag, "tank" + i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putBoolean("isOn", this.isOn);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(tag, "tank" + i);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    @Override public long getPower() { return Math.max(Math.min(this.power, MAX_POWER), 0); }
    @Override public void setPower(long i) { this.power = i; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public long transferPower(long power) {
        if(power + this.getPower() <= this.getMaxPower()) {
            this.setPower(power + this.getPower());
            return 0;
        }

        long overshoot = power - (this.getMaxPower() - this.getPower());
        this.setPower(this.getMaxPower());
        return overshoot;
    }

    @Override public boolean canConnect(Direction dir) { return dir != null && dir.getAxis().isHorizontal(); }
    @Override public boolean canConnect(FluidType type, Direction dir) { return dir != null && dir.getAxis().isHorizontal(); }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1], this.tanks[2], this.tanks[3], this.tanks[4] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineVacuumDistillMenu(id, inventory, this);
    }
}
