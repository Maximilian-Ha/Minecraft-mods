package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineHephaestus.
 *
 * Der Hephaestus ist ein Waermetauscher ohne Brennstoff: er holt sich die Waerme aus dem Boden,
 * ueber dem er steht. Lava zaehlt, Vulkangestein zaehlt viel mehr, und ueber einem wirklichen
 * Vulkanschlot verdreifacht sich alles.
 *
 * ER SIEHT ZEHN LAGEN TIEF NACH, aber nur EINE JE TICK. Der Bereich ist fuenfzehn mal fuenfzehn
 * Bloecke gross -- zweihundertfuenfundzwanzig Bloecke je Lage --, und alle zehn Lagen in einem
 * Tick anzusehen hiesse, zweitausendzweihundertfuenfzig Bloecke abzufragen. Stattdessen wird
 * reihum je eine Lage aufgefrischt; die Gesamtwaerme ist die Summe der zehn gemerkten Werte.
 * Das Original rechnet ebenso.
 *
 * ER BRAUCHT KEINE EINSTELLUNG AUSSER DEM STOFF. Was hineinlaeuft, entscheidet, was
 * herauskommt: die Heizeigenschaft des Stoffes nennt den ersten Schritt, und der Ausgangstank
 * stellt sich danach ein. Oel wird zu Heissoel; was keine Heizeigenschaft hat, laesst er gar
 * nicht erst herein.
 *
 * ABWEICHUNG: das Original zaehlt volcanic_lava_block und ore_volcano getrennt. Im Port heisst
 * der erste VOLCANIC_LAVA, und die Stelle des zweiten nehmen VOLCANO_CORE und
 * VOLCANO_RAD_CORE ein -- die beiden Schlotbloecke, die der Port hat.
 *
 * NICHT UEBERNOMMEN: das Dauergeraeusch des Turms, die Dampfwolke ueber ihm und das drehende
 * Rad. Der Ton fehlt dem Port; ohne Modell gibt es auch kein Rad.
 */
public class MachineHephaestusBlockEntity extends TickingBaseBlockEntity implements IFluidStandardTransceiverMK2, IFluidCopiable {

    /** Wie viele Lagen unter der Maschine zaehlen -- eine je Tick, reihum. */
    public static final int LAYERS = 10;

    /** Halbe Kantenlaenge des abgesuchten Feldes. */
    public static final int RANGE = 7;

    public final FluidTank input;
    public final FluidTank output;

    /** Nur zur Anzeige: die zuletzt gemeldete Gesamtwaerme. */
    public int bufferedHeat;

    private final int[] heat = new int[LAYERS];
    private long fissureScanTime = Long.MIN_VALUE;

    public MachineHephaestusBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_HEPHAESTUS.get(), pos, state);

        this.input = new FluidTank(Fluids.OIL, 24_000);
        this.output = new FluidTank(Fluids.HOTOIL, 24_000);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.setupTanks();

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.input.getTankType(), this.level, pos);
            if(this.output.getFill() > 0) this.tryProvide(this.output, this.level, pos);
        }

        this.scanLayer((int) (this.level.getGameTime() % LAYERS));

        this.heatFluid();
        this.bufferedHeat = this.getTotalHeat();

        this.networkPackNT(150);
    }

    /** Eine Lage unter der Maschine neu abzaehlen. */
    private void scanLayer(int layer) {

        int y = this.worldPosition.getY() - 1 - layer;
        this.heat[layer] = 0;

        if(y < this.level.getMinBuildHeight()) return;

        int sum = 0;

        for(int x = -RANGE; x <= RANGE; x++) {
            for(int z = -RANGE; z <= RANGE; z++) {
                sum += this.heatFromBlock(new BlockPos(this.worldPosition.getX() + x, y, this.worldPosition.getZ() + z));
            }
        }

        this.heat[layer] = sum;
    }

    private int heatFromBlock(BlockPos pos) {

        BlockState state = this.level.getBlockState(pos);

        if(state.is(Blocks.LAVA)) return 5;
        if(state.is(NtmBlocks.VOLCANIC_LAVA.get())) return 150;

        if(state.is(NtmBlocks.VOLCANO_CORE.get()) || state.is(NtmBlocks.VOLCANO_RAD_CORE.get())) {
            this.fissureScanTime = this.level.getGameTime();
            return 300;
        }

        return 0;
    }

    /** Ueber einem Schlot zaehlt alles dreifach -- zwanzig Ticks lang, bis er neu gesehen wird. */
    public int getTotalHeat() {

        int total = 0;
        for(int h : this.heat) total += h;

        boolean fissure = this.level.getGameTime() - this.fissureScanTime < 20;

        return fissure ? total * 3 : total;
    }

    /** So viele Schritte, wie Stoff, Platz und Waerme zugleich hergeben. */
    private void heatFluid() {

        FluidType type = this.input.getTankType();
        if(!type.hasTrait(FT_Heatable.class)) return;

        FT_Heatable trait = type.getTrait(FT_Heatable.class);
        if(!trait.hasSteps()) return;

        HeatingStep step = trait.getFirstStep();
        if(step.amountReq <= 0 || step.amountProduced <= 0 || step.heatReq <= 0) return;

        int ops = Math.min(this.input.getFill() / step.amountReq,
                Math.min((this.output.getMaxFill() - this.output.getFill()) / step.amountProduced,
                        this.getTotalHeat() / step.heatReq));

        if(ops <= 0) return;

        this.input.setFill(this.input.getFill() - step.amountReq * ops);
        this.output.setFill(this.output.getFill() + step.amountProduced * ops);
        this.setChanged();
    }

    /** Der Ausgangstank richtet sich nach dem Eingang; ohne Heizeigenschaft bleiben beide leer. */
    private void setupTanks() {

        FluidType type = this.input.getTankType();

        if(type.hasTrait(FT_Heatable.class)) {

            FT_Heatable trait = type.getTrait(FT_Heatable.class);

            if(trait.getEfficiency(HeatingType.HEATEXCHANGER) > 0 && trait.hasSteps()) {
                this.output.setTankType(trait.getFirstStep().typeProduced);
                return;
            }
        }

        this.input.setTankType(Fluids.NONE);
        this.output.setTankType(Fluids.NONE);
    }

    /** Vier Anschluesse unten, vier ganz oben -- der Turm ist zwoelf Bloecke hoch. */
    private DirPos[] getConPos() {

        BlockPos pos = this.worldPosition;

        return new DirPos[] {
                new DirPos(pos.offset(2, 0, 0), Direction.EAST),
                new DirPos(pos.offset(-2, 0, 0), Direction.WEST),
                new DirPos(pos.offset(0, 0, 2), Direction.SOUTH),
                new DirPos(pos.offset(0, 0, -2), Direction.NORTH),
                new DirPos(pos.offset(2, 11, 0), Direction.EAST),
                new DirPos(pos.offset(-2, 11, 0), Direction.WEST),
                new DirPos(pos.offset(0, 11, 2), Direction.SOUTH),
                new DirPos(pos.offset(0, 11, -2), Direction.NORTH)
        };
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.input, this.output }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.output }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.input }; }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return dir != null && dir != Direction.UP && dir != Direction.DOWN;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.input.serialize(buf);
        this.output.serialize(buf);
        buf.writeInt(this.bufferedHeat);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.input.deserialize(buf);
        this.output.deserialize(buf);
        this.bufferedHeat = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.input.readFromNBT(tag, "0");
        this.output.readFromNBT(tag, "1");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.input.writeToNBT(tag, "0");
        this.output.writeToNBT(tag, "1");
    }
}
