package com.hbm.blockentity.machine.oil;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker.
 *
 * Der Krackturm spaltet ein schweres Oel unter Dampf in ein leichteres und ein Gas. Er hat
 * keine Oberflaeche und kein Inventar: eingestellt wird er mit einem Fluidkennzeichner in der
 * Hand, abgelesen ueber die Einblendung. Was woraus wird, steht in CrackingRecipes.
 *
 * Fuenf Tanks: schweres Oel und Dampf hinein, leichteres Oel, Gas und Altdampf hinaus.
 */
public class MachineCatalyticCrackerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardTransceiverMK2 {

    /** Was ein Krackdurchgang verbraucht, zweimal je fuenf Ticks. */
    public static final int OIL_PER_BATCH = 100;
    public static final int STEAM_PER_BATCH = 200;
    /**
     * Der Altdampf, der dabei anfaellt: nur zwei mB je Durchgang. Der Kommentar des Originals
     * erklaert warum -- Altdampf hat die Dichte von Wasser, nicht die von Dampf.
     */
    public static final int SPENT_STEAM_PER_BATCH = 2;

    public final FluidTank[] tanks;

    public MachineCatalyticCrackerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_CATALYTIC_CRACKER.get(), pos, state);
        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.BITUMEN, 4_000),
                new FluidTank(Fluids.STEAM, 8_000),
                new FluidTank(Fluids.OIL, 4_000),
                new FluidTank(Fluids.PETROLEUM, 4_000),
                new FluidTank(Fluids.SPENTSTEAM, 800)
        };
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.setupTanks();

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            this.trySubscribe(this.tanks[1].getTankType(), this.level, pos);
        }

        if(this.level.getGameTime() % 5 == 0) this.crack();

        if(this.level.getGameTime() % 10 == 0) {
            for(DirPos pos : this.getConPos()) {
                for(int i = 2; i <= 4; i++) {
                    if(this.tanks[i].getFill() > 0) this.tryProvide(this.tanks[i], this.level, pos);
                }
            }
        }

        this.networkPackNT(25);
    }

    /** Die drei Ausgabetanks richten sich nach dem, was gerade eingestellt ist. */
    private void setupTanks() {

        Pair<FluidStack, FluidStack> recipe = CrackingRecipes.getCracking(this.tanks[0].getTankType());

        if(recipe != null) {
            this.tanks[1].setTankType(Fluids.STEAM);
            this.tanks[2].setTankType(recipe.getKey().type);
            this.tanks[3].setTankType(recipe.getValue().type);
            this.tanks[4].setTankType(Fluids.SPENTSTEAM);
        } else {
            this.tanks[2].setTankType(Fluids.NONE);
            this.tanks[3].setTankType(Fluids.NONE);
            this.tanks[4].setTankType(Fluids.NONE);
        }
    }

    private void crack() {

        Pair<FluidStack, FluidStack> recipe = CrackingRecipes.getCracking(this.tanks[0].getTankType());
        if(recipe == null) return;

        int left = recipe.getKey().fill;
        int right = recipe.getValue().fill;

        /* Zwei Durchgaenge je Aufruf, wie im Original. */
        for(int i = 0; i < 2; i++) {

            if(this.tanks[0].getFill() < OIL_PER_BATCH) return;
            if(this.tanks[1].getFill() < STEAM_PER_BATCH) return;
            if(!this.hasSpace(left, right)) return;

            this.tanks[0].setFill(this.tanks[0].getFill() - OIL_PER_BATCH);
            this.tanks[1].setFill(this.tanks[1].getFill() - STEAM_PER_BATCH);
            this.tanks[2].setFill(this.tanks[2].getFill() + left);
            this.tanks[3].setFill(this.tanks[3].getFill() + right);
            this.tanks[4].setFill(this.tanks[4].getFill() + SPENT_STEAM_PER_BATCH);
        }
    }

    private boolean hasSpace(int left, int right) {
        return this.tanks[2].getFill() + left <= this.tanks[2].getMaxFill()
                && this.tanks[3].getFill() + right <= this.tanks[3].getMaxFill()
                && this.tanks[4].getFill() + SPENT_STEAM_PER_BATCH <= this.tanks[4].getMaxFill();
    }

    /** Acht Anschluesse rings um den Sockel, alle richtungsbezogen. */
    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 4 + rot.getStepX(), y, z + dir.getStepZ() * 4 + rot.getStepZ(), dir),
                new DirPos(x + dir.getStepX() * 4 - rot.getStepX() * 2, y, z + dir.getStepZ() * 4 - rot.getStepZ() * 2, dir),
                new DirPos(x - dir.getStepX() * 4 + rot.getStepX(), y, z - dir.getStepZ() * 4 + rot.getStepZ(), dir.getOpposite()),
                new DirPos(x - dir.getStepX() * 4 - rot.getStepX() * 2, y, z - dir.getStepZ() * 4 - rot.getStepZ() * 2, dir.getOpposite()),
                new DirPos(x + dir.getStepX() * 2 + rot.getStepX() * 3, y, z + dir.getStepZ() * 2 + rot.getStepZ() * 3, rot),
                new DirPos(x + dir.getStepX() * 2 - rot.getStepX() * 4, y, z + dir.getStepZ() * 2 - rot.getStepZ() * 4, rot),
                new DirPos(x - dir.getStepX() * 2 + rot.getStepX() * 3, y, z - dir.getStepZ() * 2 + rot.getStepZ() * 3, rot.getOpposite()),
                new DirPos(x - dir.getStepX() * 2 - rot.getStepX() * 4, y, z - dir.getStepZ() * 2 - rot.getStepZ() * 4, rot.getOpposite())
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(tag, "tank" + i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(tag, "tank" + i);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    @Override public boolean canConnect(FluidType type, Direction dir) { return dir != null; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0], this.tanks[1] }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[2], this.tanks[3], this.tanks[4] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    private AABB renderBox;

    /* Kein @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 16, z + 4);
        }
        return this.renderBox;
    }
}
