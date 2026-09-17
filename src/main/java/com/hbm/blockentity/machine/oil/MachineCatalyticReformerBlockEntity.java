package com.hbm.blockentity.machine.oil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineCatalyticReformerMenu;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.util.Tuple.Triplet;
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
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer.
 *
 * Der Reformer baut lange Ketten zu ringfoermigen Verbindungen um: aus 100 mB Eingabe werden
 * ein Reformat, ein Nebengas und Wasserstoff, nach ReformingRecipes. Er braucht Strom und
 * einen Katalysator, der dabei nicht verbraucht wird.
 *
 * Elf Plaetze, wie im Original: Batterie, je ein Paar zum Ein- und Ausfuellen fuer die vier
 * Tanks, der Fluidkennzeichner und ganz oben der Katalysator.
 */
public class MachineCatalyticReformerBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

    private AABB renderBox;

    public static final long MAX_POWER = 1_000_000L;
    /** Was ein Durchgang kostet und verbraucht -- unveraendert aus dem Original. */
    public static final long POWER_PER_BATCH = 20_000L;
    public static final int INPUT_PER_BATCH = 100;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FLUID_ID = 9;
    public static final int SLOT_CATALYST = 10;

    public long power;
    public final FluidTank[] tanks;

    public MachineCatalyticReformerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_CATALYTIC_REFORMER.get(), pos, state, 11);

        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.NAPHTHA, 64_000),
                new FluidTank(Fluids.REFORMATE, 24_000),
                new FluidTank(Fluids.PETROLEUM, 24_000),
                new FluidTank(Fluids.HYDROGEN, 24_000)
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_catalytic_reformer");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % 20 == 0) {
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            }
        }

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);
        this.tanks[0].setType(SLOT_FLUID_ID, this.slots);
        this.tanks[0].loadTank(this.level, 1, 2, this.slots);

        this.reform();

        this.tanks[1].unloadTank(this.level, 3, 4, this.slots);
        this.tanks[2].unloadTank(this.level, 5, 6, this.slots);
        this.tanks[3].unloadTank(this.level, 7, 8, this.slots);

        for(DirPos pos : this.getConPos()) {
            for(int i = 1; i < this.tanks.length; i++) {
                if(this.tanks[i].getFill() > 0) this.tryProvide(this.tanks[i], this.level, pos);
            }
        }

        this.networkPackNT(150);
    }

    private void reform() {

        Triplet<FluidStack, FluidStack, FluidStack> out = ReformingRecipes.getOutput(this.tanks[0].getTankType());

        if(out == null) {
            /* Ohne Rezept steht nichts in den Ausgabetanks -- sonst haengt dort ein Stoff,
             * der aus dem eingefuellten gar nicht entstehen kann. */
            for(int i = 1; i < this.tanks.length; i++) this.tanks[i].setTankType(Fluids.NONE);
            return;
        }

        this.tanks[1].setTankType(out.getX().type);
        this.tanks[2].setTankType(out.getY().type);
        this.tanks[3].setTankType(out.getZ().type);

        if(this.power < POWER_PER_BATCH) return;
        if(this.tanks[0].getFill() < INPUT_PER_BATCH) return;
        if(this.getItem(SLOT_CATALYST).getItem() != NtmItems.CATALYTIC_CONVERTER.get()) return;

        if(this.tanks[1].getFill() + out.getX().fill > this.tanks[1].getMaxFill()) return;
        if(this.tanks[2].getFill() + out.getY().fill > this.tanks[2].getMaxFill()) return;
        if(this.tanks[3].getFill() + out.getZ().fill > this.tanks[3].getMaxFill()) return;

        this.tanks[0].setFill(this.tanks[0].getFill() - INPUT_PER_BATCH);
        this.tanks[1].setFill(this.tanks[1].getFill() + out.getX().fill);
        this.tanks[2].setFill(this.tanks[2].getFill() + out.getY().fill);
        this.tanks[3].setFill(this.tanks[3].getFill() + out.getZ().fill);

        this.power -= POWER_PER_BATCH;
        this.setChanged();
    }

    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Direction.NORTH)
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_FLUID_ID) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot == SLOT_CATALYST) return stack.getItem() == NtmItems.CATALYTIC_CONVERTER.get();
        if(slot == 1) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[0].getTankType()).isEmpty();
        if(slot == 3) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[1].getTankType()).isEmpty();
        if(slot == 5) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[2].getTankType()).isEmpty();
        if(slot == 7) return !FluidContainerRegistry.getFullContainer(stack, this.tanks[3].getTankType()).isEmpty();
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 2 || index == 4 || index == 6 || index == 8;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(tag, "tank" + i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(tag, "tank" + i);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
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
    @Override public boolean canConnect(com.hbm.inventory.fluid.FluidType type, Direction dir) { return dir != null && dir.getAxis().isHorizontal(); }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1], this.tanks[2], this.tanks[3] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCatalyticReformerMenu(id, inventory, this);
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            /*
             * Masse aus dem Original uebernommen. catalytic_reformer.obj misst X +-1,5, Z +-2,5, Y 0 bis 7.
             */
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 7, z + 3);
        }
        return this.renderBox;
    }
}
