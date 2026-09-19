package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Liquid;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Viscous;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineDrain.
 *
 * Der Absauger ist das Ende einer Rohrleitung: was hineinlaeuft, laeuft in die Welt hinaus. Er
 * ist damit die Entsorgung fuer alles, wofuer es keinen Abnehmer gibt -- Abwasser, Abgase,
 * Rueckstaende.
 *
 * ER ENTSORGT NICHT FOLGENLOS. Was ausgelassen wird, geht durch dieselben Eigenschaften, durch
 * die es auch aus einem geborstenen Tank ginge: Rauch verschmutzt, Radioaktives strahlt, Gift
 * vergiftet. Das ist der ganze Punkt -- ein Abfluss, aus dem beliebig viel verschwindet, waere
 * eine Einladung, die Abgasreinigung wegzulassen.
 *
 * ANTIMATERIE MACHT EINE AUSNAHME: sie beruehrt die Welt gar nicht erst, sondern reisst den
 * Absauger und alles im Umkreis mit sich. Auch das steht so im Original.
 *
 * ER GIBT JE TICK DIE HAELFTE dessen ab, was im Tank steht, mindestens aber ein Millibar. So
 * leert er sich schnell, wenn viel kommt, und tropft, wenn wenig kommt.
 *
 * DIE OELLACHE. Zaehe, brennbare Fluessigkeiten hinterlassen mit einer Wahrscheinlichkeit von
 * eins zu zwanzig einen Oelfleck in der Naehe -- aber nur, wenn mindestens hundert Millibar auf
 * einmal auslaufen. Gesucht wird die Stelle mit einem Strahl, der drei Bloecke HINTER dem
 * Auslass ansetzt und schraeg nach unten laeuft; er muss auf eine Oberseite treffen, und ueber
 * ihr muss Platz sein.
 *
 * NICHT UEBERNOMMEN: die Spritzer- und Dampfwolken, die das Original ueber dem Auslass zeichnet.
 */
public class MachineDrainBlockEntity extends TickingBaseBlockEntity implements IFluidStandardReceiverMK2, IFluidCopiable {

    public final FluidTank tank;

    public MachineDrainBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DRAIN.get(), pos, state);
        this.tank = new FluidTank(Fluids.NONE, 2_000);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % 20 == 0) {
            for(DirPos pos : this.getConPos()) this.trySubscribe(this.tank.getTankType(), this.level, pos);
        }

        this.networkPackNT(50);

        if(this.tank.getFill() <= 0) return;

        if(this.tank.getTankType().hasTrait(FT_Amat.class)) {
            this.level.explode(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5, 10F, Level.ExplosionInteraction.TNT);
            return;
        }

        int toSpill = Math.max(this.tank.getFill() / 2, 1);
        this.tank.setFill(this.tank.getFill() - toSpill);

        FluidTrait.onRelease(this.level, this.worldPosition, this.tank.getTankType(), this.tank, FluidReleaseType.SPILL, toSpill);

        FluidType sorte = this.tank.getTankType();
        if(toSpill >= 100 && this.level.random.nextInt(20) == 0
                && sorte.hasTrait(FT_Liquid.class) && sorte.hasTrait(FT_Viscous.class) && sorte.hasTrait(FT_Flammable.class)) {
            this.hinterlasseLache();
        }
    }

    /**
     * Sucht eine Stelle fuer den Oelfleck und setzt ihn dorthin.
     *
     * DER STRAHL SETZT HINTER DEM AUSLASS AN, drei Bloecke in Gegenrichtung, und faellt
     * fuenfundzwanzig Bloecke tief bei einer seitlichen Streuung von fuenf. Er zaehlt nur,
     * wenn er auf eine OBERSEITE trifft -- an einer Wand laeuft nichts zusammen.
     */
    private void hinterlasseLache() {

        Direction front = this.getBlockState().getValue(DummyableBlock.FACING);

        Vec3 start = new Vec3(
                this.worldPosition.getX() + 0.5 - front.getStepX() * 3,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5 - front.getStepZ() * 3);

        Vec3 ende = start.add(this.level.random.nextGaussian() * 5, -25, this.level.random.nextGaussian() * 5);

        BlockHitResult treffer = this.level.clip(new ClipContext(start, ende,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));

        if(treffer.getType() != HitResult.Type.BLOCK) return;
        if(treffer.getDirection() != Direction.UP) return;

        BlockPos stelle = treffer.getBlockPos().above();
        BlockState daueber = this.level.getBlockState(stelle);

        if(!daueber.canBeReplaced()) return;
        if(!daueber.getFluidState().isEmpty()) return;

        BlockState lache = NtmBlocks.OIL_SPILL.get().defaultBlockState();
        if(!lache.canSurvive(this.level, stelle)) return;

        this.level.setBlockAndUpdate(stelle, lache);
    }

    /**
     * Angeschlossen wird nach vorn und zu beiden Seiten -- nach hinten zeigt der Auslass, und
     * dort steht nichts.
     */
    public DirPos[] getConPos() {

        Direction front = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction left = front.getClockWise();
        Direction right = front.getCounterClockWise();

        return new DirPos[] {
                new DirPos(this.worldPosition.relative(front), front),
                new DirPos(this.worldPosition.relative(left), left),
                new DirPos(this.worldPosition.relative(right), right)
        };
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank getTankToPaste() { return this.tank; }

    /** Von oben und unten nimmt er nichts an -- die Rohre laufen waagerecht heran. */
    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return dir != Direction.UP && dir != Direction.DOWN;
    }

    @Override public void serialize(RegistryFriendlyByteBuf buf) { this.tank.serialize(buf); }
    @Override public void deserialize(RegistryFriendlyByteBuf buf) { this.tank.deserialize(buf); }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(tag, "t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(tag, "t");
    }
}
