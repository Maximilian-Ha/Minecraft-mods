package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineIntake.
 *
 * Der Einlass ist die Luftquelle des Ports: ein Geblaese, das Strom in Pressluft verwandelt. Er
 * ist damit das Gegenstueck zum Absauger -- der gibt an die Welt ab, dieser nimmt von ihr.
 *
 * ER FUELLT SEINEN TANK GANZ ODER GAR NICHT. Reicht der Strom fuer einen Zug, steht der Tank
 * anschliessend voll; reicht er nicht, geschieht nichts. Das Original rechnet ebenso, und es
 * ergibt auch Sinn: Luft ist nicht knapp, knapp ist die Leistung, sie zu verdichten.
 *
 * SEIN TANK IST KLEIN -- ein Kubikmeter. Er ist kein Speicher, sondern ein Durchlauf; was er
 * fuellt, geht im selben Tick ins Rohrnetz.
 *
 * NICHT UEBERNOMMEN: das Laufgeraeusch des Motors und das sich drehende Geblaeserad. Der Port
 * zeichnet die Maschine als Kasten; ein Rad, das sich nicht dreht, braucht auch keinen Ton.
 */
public class MachineIntakeBlockEntity extends TickingBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardSenderMK2 {

    public static final long MAX_POWER = 2_000;

    public final FluidTank compair;
    public long power;

    public MachineIntakeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_INTAKE.get(), pos, state);
        this.compair = new FluidTank(Fluids.AIR, 1_000);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.power >= MAX_POWER / 20) {
            this.compair.setFill(this.compair.getMaxFill());
            this.power -= MAX_POWER / 20;
        }

        for(DirPos pos : this.getConPos()) {
            this.tryProvide(this.compair, this.level, pos);
            this.trySubscribe(this.level, pos);
        }

        this.networkPackNT(50);
    }

    /**
     * Das Geblaese ist zwei mal zwei Bloecke gross, und angeschlossen wird an allen vier
     * Aussenkanten -- acht Stellen. Die Zahlen sind die des Originals.
     */
    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.relative(dir), dir),
                new DirPos(p.relative(dir).relative(rot), dir),

                new DirPos(p.relative(dir, -2), dir.getOpposite()),
                new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),

                new DirPos(p.relative(rot, 2), rot),
                new DirPos(p.relative(rot, 2).relative(dir, -1), rot),

                new DirPos(p.relative(rot, -1), rot.getOpposite()),
                new DirPos(p.relative(rot, -1).relative(dir, -1), rot.getOpposite())
        };
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.compair }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.compair }; }

    @Override public boolean canConnect(Direction dir) { return dir != Direction.UP && dir != Direction.DOWN; }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return type == Fluids.AIR && dir != Direction.UP && dir != Direction.DOWN;
    }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeLong(this.power);
        this.compair.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.power = buf.readLong();
        this.compair.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.compair.readFromNBT(tag, "compair");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        this.compair.writeToNBT(tag, "compair");
    }
}
