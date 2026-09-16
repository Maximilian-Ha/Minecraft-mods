package com.hbm.blockentity.machine.oil;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.FractionRecipes;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower.
 *
 * Der Fraktionierturm. Er spaltet ein Oel in zwei leichtere Fraktionen -- 100 mB Eingabe alle
 * zehn Ticks, Ausbeute nach FractionRecipes. Ohne Strom, ohne Oberflaeche.
 *
 * Tuerme lassen sich stapeln: steht drei Bloecke darueber ein zweiter Turm, schiebt dieser
 * hier sein Oel nach oben und zieht die Fraktionen von dort wieder herunter. Die Anschluesse
 * sitzen deshalb nur am untersten Turm -- oben wird nichts abgenommen.
 */
public class MachineFractionTowerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardTransceiverMK2 {

    /** Wie weit der naechste Turm ueber diesem steht. */
    public static final int STACK_HEIGHT = 3;
    /** Wie viel Oel eine Spaltung verbraucht -- die Ausbeute im Rezept bezieht sich darauf. */
    public static final int BATCH = 100;

    public final FluidTank[] tanks;

    public MachineFractionTowerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_FRACTION_TOWER.get(), pos, state);

        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.HEAVYOIL, 4_000),
                new FluidTank(Fluids.BITUMEN, 4_000),
                new FluidTank(Fluids.SMEAR, 4_000)
        };
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.exchangeWithTowerAbove();
        this.setupTanks();

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
        }

        if(this.level.getGameTime() % 10 == 0) this.fractionate();

        for(DirPos pos : this.getConPos()) {
            for(int i = 1; i < this.tanks.length; i++) {
                if(this.tanks[i].getFill() > 0) this.tryProvide(this.tanks[i], this.level, pos);
            }
        }

        this.networkPackNT(50);
    }

    /**
     * Der Austausch mit dem Turm darueber: das Oel steigt auf, die Fraktionen sinken ab.
     * Beide Tuerme spalten dasselbe Oel, deshalb werden erst die Sorten gleichgezogen.
     */
    private void exchangeWithTowerAbove() {

        if(!(this.level.getBlockEntity(this.getBlockPos().above(STACK_HEIGHT)) instanceof MachineFractionTowerBlockEntity above)) return;

        for(int i = 0; i < this.tanks.length; i++) {
            above.tanks[i].setTankType(this.tanks[i].getTankType());
        }

        int up = Math.min(this.tanks[0].getFill(), above.tanks[0].getMaxFill() - above.tanks[0].getFill());
        int left = Math.min(above.tanks[1].getFill(), this.tanks[1].getMaxFill() - this.tanks[1].getFill());
        int right = Math.min(above.tanks[2].getFill(), this.tanks[2].getMaxFill() - this.tanks[2].getFill());

        this.tanks[0].setFill(this.tanks[0].getFill() - up);
        this.tanks[1].setFill(this.tanks[1].getFill() + left);
        this.tanks[2].setFill(this.tanks[2].getFill() + right);

        above.tanks[0].setFill(above.tanks[0].getFill() + up);
        above.tanks[1].setFill(above.tanks[1].getFill() - left);
        above.tanks[2].setFill(above.tanks[2].getFill() - right);
    }

    /** Die beiden Ausgabetanks richten sich nach dem, was gerade im Eingabetank steht. */
    private void setupTanks() {

        Pair<FluidStack, FluidStack> fractions = FractionRecipes.getFractions(this.tanks[0].getTankType());

        if(fractions != null) {
            this.tanks[1].setTankType(fractions.getKey().type);
            this.tanks[2].setTankType(fractions.getValue().type);
        } else {
            /* Ein Oel ohne Rezept wird gar nicht erst angenommen. */
            for(FluidTank tank : this.tanks) tank.setTankType(Fluids.NONE);
        }
    }

    private void fractionate() {

        Pair<FluidStack, FluidStack> fractions = FractionRecipes.getFractions(this.tanks[0].getTankType());

        if(fractions == null) return;
        if(this.tanks[0].getFill() < BATCH) return;

        int left = fractions.getKey().fill;
        int right = fractions.getValue().fill;

        if(this.tanks[1].getFill() + left > this.tanks[1].getMaxFill()) return;
        if(this.tanks[2].getFill() + right > this.tanks[2].getMaxFill()) return;

        this.tanks[0].setFill(this.tanks[0].getFill() - BATCH);
        this.tanks[1].setFill(this.tanks[1].getFill() + left);
        this.tanks[2].setFill(this.tanks[2].getFill() + right);
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

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1], this.tanks[2] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    /* Ohne @Override: getRenderBoundingBox() stammt aus der NeoForge-Erweiterung der
     * Block-Entitaet und gilt dem Compiler nicht als ueberschriebene Methode. */
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
    }
}
