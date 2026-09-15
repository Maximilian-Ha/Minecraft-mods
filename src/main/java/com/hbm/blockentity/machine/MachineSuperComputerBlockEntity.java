package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineSuperComputerMenu;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachineSuperComputer;
import com.hbm.util.BobMathUtil;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineSuperComputer.
 *
 * Der Grossrechner: acht Faecher, zwei Tanks, ein Rezept. Was er tut, steht in
 * SuperComputerRecipes -- rechnen, auswerten, kopieren, und einmal Klaus.
 *
 * SEIN STROMBEDARF RICHTET SICH NACH DEM REZEPT. Er haelt das Hundertfache dessen vor, was der
 * eingestellte Lauf je Tick zieht, mindestens aber hunderttausend. Der Klaus-Lauf zieht fuenf
 * Millionen -- der Speicher waechst dann auf fuenfhundert.
 *
 * FUENF ANSCHLUSSPUNKTE, alle am hinteren Ende des Bauwerks: einer in der Mitte, vier seitlich.
 * Strom und Kuehlmittel gehen ueber dieselben Punkte hinein, das verbrauchte Kuehlmittel wieder
 * hinaus.
 *
 * FEHLER IM ORIGINAL, hier berichtigt: der fuenfte Anschlusspunkt steht dort als
 * "zCoord + dir.offsetZ *  - rot.offsetZ * 2" -- die Fuenf fehlt, und damit landet der Punkt
 * nicht seitlich hinten, sondern irgendwo neben dem Kern. Die vier anderen stehen vollstaendig
 * da; dass ausgerechnet dieser eine anders gemeint war, ist an ihnen abzulesen.
 */
public class MachineSuperComputerBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver {

    public final FluidTank inputTank;
    public final FluidTank outputTank;

    public long power;
    public long maxPower = 100_000;
    public boolean didProcess = false;

    public final ModuleMachineSuperComputer computerModule;

    private AABB renderBox;

    public MachineSuperComputerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SUPER_COMPUTER.get(), pos, state, 8);

        this.inputTank = new FluidTank(Fluids.NONE, 4_000);
        this.outputTank = new FluidTank(Fluids.NONE, 4_000);

        this.computerModule = new ModuleMachineSuperComputer(0, this, this.slots)
                .itemInput(2).itemOutput(5)
                .fluidInput(this.inputTank).fluidOutput(this.outputTank);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineSuperComputer");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.maxPower <= 0) this.maxPower = 1_000_000;

        if(!this.level.isClientSide) {

            GenericRecipe recipe = SuperComputerRecipes.INSTANCE.recipeNameMap.get(this.computerModule.recipe);
            if(recipe != null) this.maxPower = recipe.power * 100;

            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);
            this.power = Library.chargeTEFromItems(this.slots, 0, this.power, this.maxPower);

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);

                if(this.inputTank.getTankType() != Fluids.NONE) this.trySubscribe(this.inputTank.getTankType(), this.level, pos);
                if(this.outputTank.getFill() > 0) this.tryProvide(this.outputTank, this.level, pos);
            }

            /* Keine Aufwertungen: der Grossrechner nimmt keine an, und im Original steht hier
             * ebenfalls glatt eins zu eins. */
            this.computerModule.update(1D, 1D, true, this.slots.get(1));
            this.didProcess = this.computerModule.didProcess;
            if(this.computerModule.markDirty) this.setChanged();

            this.networkPackNT(100);
        }
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Direction.Axis.Y);

        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.relative(dir, 9), dir),
                new DirPos(p.relative(dir, 7).relative(rot, 2), rot),
                new DirPos(p.relative(dir, 7).relative(rot, -2), rot.getOpposite()),
                new DirPos(p.relative(dir, 5).relative(rot, 2), rot),
                new DirPos(p.relative(dir, 5).relative(rot, -2), rot.getOpposite())
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.inputTank.serialize(buf);
        this.outputTank.serialize(buf);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        buf.writeBoolean(this.didProcess);
        this.computerModule.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.inputTank.deserialize(buf);
        this.outputTank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.computerModule.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inputTank.readFromNBT(tag, "i");
        this.outputTank.readFromNBT(tag, "o");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        this.computerModule.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.inputTank.writeToNBT(tag, "i");
        this.outputTank.writeToNBT(tag, "o");
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        this.computerModule.writeToNBT(tag);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == 0) return true;
        if(slot == 1 && stack.getItem() == NtmItems.BLUEPRINTS.get()) return true;
        return this.computerModule.isItemValid(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= 5 || this.computerModule.isSlotClogged(index);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {2, 3, 4, 5, 6, 7};
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.inputTank }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.outputTank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.inputTank, this.outputTank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineSuperComputerMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("index") && tag.contains("selection")) {
            if(tag.getInt("index") == 0) {
                this.computerModule.recipe = tag.getString("selection");
                this.setChanged();
            }
        }
    }

    /* Die Masse sind die des Originals, unveraendert uebernommen. Sie greifen nach unten
     * eigentlich zu kurz -- das Bauwerk reicht sieben Bloecke tiefer --, aber ohne Renderer ist
     * daran nichts nachzupruefen, und eine Zahl zu erfinden waere schlechter als eine zu erben. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 8, p.getY(), p.getZ() - 8, p.getX() + 9, p.getY() + 9, p.getZ() + 9);
        }
        return this.renderBox;
    }
}
