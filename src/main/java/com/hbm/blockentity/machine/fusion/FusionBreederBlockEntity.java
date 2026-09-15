package com.hbm.blockentity.machine.fusion;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.FusionBreederMenu;
import com.hbm.inventory.recipes.FluidBreederRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes.OutgasserRecipe;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.Tuple.Pair;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionBreeder.
 *
 * Der Brutreaktor ist der einzige Abnehmer, der nicht die Plasmawaerme nimmt, sondern den
 * NEUTRONENFLUSS -- und der wird nicht geteilt: jeder Brutreaktor bekommt ihn ganz. Dafuer haengt
 * er an der Brennstoffwahl des Torus, denn nicht jedes Rezept gibt Fluss ab; Helium-3 etwa
 * fusioniert aneutronisch und laesst ihn leer ausgehen.
 *
 * Er kann zweierlei: Gegenstaende ausgasen -- dieselbe Liste wie der Ausgaser -- und Fluide
 * umwandeln. Was zuerst geht, wird gemacht; der Fortschritt zaehlt in beiden Faellen den
 * aufgelaufenen Fluss, und bei zehntausend ist eine Umwandlung fertig.
 *
 * ABWEICHUNG: das Original kennt einen Sonderfall -- ein bestrahltes Meteoritenschwert wird zum
 * verschmolzenen. Beide Gegenstaende gibt es im Port nicht, der Zweig entfaellt.
 */
public class FusionBreederBlockEntity extends MachineBaseBlockEntity implements IFluidStandardTransceiverMK2, IFusionPowerReceiver {

    public static final int SLOT_FLUID_ID = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;

    /** Wie viel Fluss eine Umwandlung braucht. */
    public static final double CAPACITY = 10_000D;

    protected GenNode<?> plasmaNode;

    public final FluidTank[] tanks = new FluidTank[2];

    /** Was in diesem Tick ankam; wird am Tickende geleert. */
    public double neutronEnergy;
    /** Dasselbe, einen Tick versetzt -- nur fuer die Anzeige. */
    public double neutronEnergySync;
    public double progress;

    private AABB renderBox;

    public FusionBreederBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FUSION_BREEDER.get(), pos, state, 3);

        this.tanks[0] = new FluidTank(Fluids.NONE, 16_000);
        this.tanks[1] = new FluidTank(Fluids.NONE, 16_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.fusionBreeder");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.tanks[0].setType(SLOT_FLUID_ID, this.slots);

        if(!this.canProcessSolid() && !this.canProcessLiquid()) this.progress = 0;

        /*
         * Die Blockentitaeten ticken in beliebiger Reihenfolge, und die Pakete gehen gebuendelt
         * hinaus: der Wert muss deshalb einen Tick laenger stehen bleiben als er gilt.
         */
        this.neutronEnergySync = this.neutronEnergy;

        for(DirPos pos : this.getConPos()) {
            if(this.tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
        }

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getOpposite();
        BlockPos nodePos = this.worldPosition.offset(dir.getStepX() * 2, 2, dir.getStepZ() * 2);

        this.plasmaNode = FusionNodes.ensure(this.plasmaNode, this.level, nodePos,
                new DirPos(this.worldPosition.offset(dir.getStepX() * 3, 2, dir.getStepZ() * 3), dir),
                PlasmaNetworkProvider.THE_PROVIDER);

        FusionNodes.subscribe(this.plasmaNode, this);

        this.networkPackNT(25);

        this.neutronEnergy = 0;
    }

    public boolean canProcessSolid() {

        ItemStack input = this.slots.get(SLOT_INPUT);
        if(input.isEmpty()) return false;

        OutgasserRecipe output = OutgasserRecipes.getOutput(input);
        if(output == null) return false;

        FluidStack fluid = output.fluidOutput;

        if(fluid != null) {
            if(this.tanks[1].getTankType() != fluid.type && this.tanks[1].getFill() > 0) return false;
            this.tanks[1].setTankType(fluid.type);
            if(this.tanks[1].getFill() + fluid.fill > this.tanks[1].getMaxFill()) return false;
        }

        ItemStack out = output.solidOutput;
        ItemStack slot = this.slots.get(SLOT_OUTPUT);

        if(slot.isEmpty() || out == null) return true;

        return ItemStack.isSameItemSameComponents(slot, out)
                && slot.getCount() + out.getCount() <= slot.getMaxStackSize();
    }

    public boolean canProcessLiquid() {

        Pair<Integer, FluidStack> output = FluidBreederRecipes.getOutput(this.tanks[0].getTankType());
        if(output == null) return false;
        if(this.tanks[0].getFill() < output.getKey()) return false;

        FluidStack fluid = output.getValue();

        if(this.tanks[1].getTankType() != fluid.type && this.tanks[1].getFill() > 0) return false;
        this.tanks[1].setTankType(fluid.type);

        return this.tanks[1].getFill() + fluid.fill <= this.tanks[1].getMaxFill();
    }

    private void processSolid() {

        OutgasserRecipe output = OutgasserRecipes.getOutput(this.slots.get(SLOT_INPUT));
        this.removeItem(SLOT_INPUT, 1);
        this.progress = 0;

        if(output.fluidOutput != null) {
            this.tanks[1].setFill(this.tanks[1].getFill() + output.fluidOutput.fill);
        }

        ItemStack out = output.solidOutput;
        if(out == null) return;

        if(this.slots.get(SLOT_OUTPUT).isEmpty()) {
            this.slots.set(SLOT_OUTPUT, out.copy());
        } else {
            this.slots.get(SLOT_OUTPUT).grow(out.getCount());
        }
    }

    private void processLiquid() {
        Pair<Integer, FluidStack> output = FluidBreederRecipes.getOutput(this.tanks[0].getTankType());
        this.tanks[0].setFill(this.tanks[0].getFill() - output.getKey());
        this.tanks[1].setFill(this.tanks[1].getFill() + output.getValue().fill);
    }

    public void doProgress() {

        if(this.canProcessSolid()) {

            this.progress += this.neutronEnergy;

            if(this.progress > CAPACITY) {
                this.processSolid();
                this.progress = 0;
                this.setChanged();
            }

        } else if(this.canProcessLiquid()) {

            this.progress += this.neutronEnergy;

            if(this.progress > CAPACITY) {
                this.processLiquid();
                this.progress = 0;
                this.setChanged();
            }

        } else {
            this.progress = 0;
        }
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 3, y + 2, z + dir.getStepZ() * 3, dir),
                new DirPos(x + rot.getStepX() * 2, y, z + rot.getStepZ() * 2, rot),
                new DirPos(x - rot.getStepX() * 2, y, z - rot.getStepZ() * 2, rot.getOpposite()),
                new DirPos(x + dir.getStepX() + rot.getStepX() * 2, y, z + dir.getStepZ() + rot.getStepZ() * 2, rot),
                new DirPos(x + dir.getStepX() - rot.getStepX() * 2, y, z + dir.getStepZ() - rot.getStepZ() * 2, rot.getOpposite())
        };
    }

    /** Der Fluss wird NICHT geteilt -- deshalb falsch. */
    @Override public boolean receivesFusionPower() { return false; }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        this.neutronEnergy = neutronPower;
        this.doProgress();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        FusionNodes.destroy(this.level, this.plasmaNode);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.neutronEnergySync);
        buf.writeDouble(this.progress);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.neutronEnergy = buf.readDouble();
        this.progress = buf.readDouble();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getDouble("progress");
        this.tanks[0].readFromNBT(tag, "t0");
        this.tanks[1].readFromNBT(tag, "t1");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("progress", this.progress);
        this.tanks[0].writeToNBT(tag, "t0");
        this.tanks[1].writeToNBT(tag, "t1");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_FLUID_ID) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot == SLOT_INPUT) return OutgasserRecipes.getOutput(stack) != null;
        return false;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { SLOT_INPUT, SLOT_OUTPUT }; }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FusionBreederMenu(id, inventory, this);
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 4, z + 4);
        }
        return this.renderBox;
    }
}
