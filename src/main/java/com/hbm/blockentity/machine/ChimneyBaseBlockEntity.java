package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityChimneyBase.
 *
 * Ein Schornstein nimmt Rauch aus Abgasleitungen entgegen und setzt ihn stark gemindert als
 * Umweltverschmutzung frei -- der Ziegelschornstein laesst ein Viertel durch, der
 * Industrieschornstein ein Zehntel. Was er dabei zurueckhaelt, faellt als Asche an und geht
 * in die Aschegrube direkt darunter.
 *
 * Der Schornstein hat selbst keinen Tank: was ankommt, wird im selben Tick verrechnet.
 * getDemand meldet deshalb konstant eine Million.
 */
public abstract class ChimneyBaseBlockEntity extends LoadedBaseBlockEntity implements IFluidReceiverMK2, ITickable {

    private static final FluidType[] SMOKE_TYPES = new FluidType[] { Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON };

    public long ashTick = 0;
    public long sootTick = 0;
    public int onTicks;

    protected ChimneyBaseBlockEntity(BlockEntityType<? extends ChimneyBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            if(this.level.getGameTime() % 20 == 0) {
                BlockPos pos = this.getBlockPos();
                for(FluidType type : SMOKE_TYPES) {
                    this.trySubscribe(type, this.level, new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Direction.EAST));
                    this.trySubscribe(type, this.level, new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Direction.WEST));
                    this.trySubscribe(type, this.level, new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Direction.SOUTH));
                    this.trySubscribe(type, this.level, new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Direction.NORTH));
                }
            }

            if(this.ashTick > 0 || this.sootTick > 0) {
                BlockEntity below = this.level.getBlockEntity(this.getBlockPos().below());

                if(below instanceof AshpitBlockEntity ashpit) {
                    ashpit.ashLevelFly += this.ashTick;
                    ashpit.ashLevelSoot += this.sootTick;
                    ashpit.setChanged();
                }

                this.ashTick = 0;
                this.sootTick = 0;
            }

            this.networkPackNT(150);

            if(this.onTicks > 0) this.onTicks--;

        } else {
            if(this.onTicks > 0) this.spawnParticles();
        }
    }

    /** Original: cpaturesAsh/cpaturesSoot, Tippfehler im Original inbegriffen. */
    public boolean capturesAsh() { return true; }
    public boolean capturesSoot() { return false; }

    public void spawnParticles() { }

    public abstract double getPollutionMod();

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return dir != null && dir.getAxis().isHorizontal()
                && (type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if(this.level == null) return amount;
        if(type != Fluids.SMOKE && type != Fluids.SMOKE_LEADED && type != Fluids.SMOKE_POISON) return amount;

        this.onTicks = 20;

        if(this.capturesAsh()) this.ashTick += amount;
        if(this.capturesSoot()) this.sootTick += amount;

        // Original: "fluid *= getPollutionMod()" auf einem long, also mit Abschneiden der
        // Nachkommastellen -- erst danach wird durch 100F geteilt.
        long polluting = (long) (amount * this.getPollutionMod());

        if(type == Fluids.SMOKE) PollutionHandler.incrementPollution(this.level, this.getBlockPos(), PollutionType.SOOT, polluting / 100F);
        if(type == Fluids.SMOKE_LEADED) PollutionHandler.incrementPollution(this.level, this.getBlockPos(), PollutionType.HEAVYMETAL, polluting / 100F);
        if(type == Fluids.SMOKE_POISON) PollutionHandler.incrementPollution(this.level, this.getBlockPos(), PollutionType.POISON, polluting / 100F);

        return 0;
    }

    @Override public long getDemand(FluidType type, int pressure) { return 1_000_000; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[0]; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.onTicks);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.onTicks = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ashTick = tag.getLong("ashTick");
        this.sootTick = tag.getLong("sootTick");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("ashTick", this.ashTick);
        tag.putLong("sootTick", this.sootTick);
    }
}
