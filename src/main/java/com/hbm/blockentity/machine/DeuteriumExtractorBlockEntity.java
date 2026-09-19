package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityDeuteriumExtractor.
 *
 * Macht aus Wasser schweres Wasser, und zwar im Verhaeltnis fuenfzig zu eins. Strom kostet das
 * ein Zwanzigstel des Speichers je Tick, und gearbeitet wird nur, solange der voll genug ist.
 *
 * Kein Inventar und keine Oberflaeche: was drin ist, zeigt der Blick auf den Block. Das
 * Original leitet trotzdem von TileEntityMachineBase ab, mit null Faechern -- im Port waere
 * das ein MenuProvider ohne Menue, deshalb steht hier LoadedBaseBlockEntity.
 */
public class DeuteriumExtractorBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IFluidCopiable {

    /** Wieviel Wasser auf ein Milliliter schweres Wasser geht. Wert aus dem Original. */
    public static final int VERHAELTNIS = 50;

    public long power = 0;
    public final FluidTank[] tanks = new FluidTank[2];

    public DeuteriumExtractorBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.MACHINE_DEUTERIUM_EXTRACTOR.get(), pos, state, 1000, 100);
    }

    protected DeuteriumExtractorBlockEntity(BlockEntityType<? extends DeuteriumExtractorBlockEntity> type, BlockPos pos, BlockState state, int wasser, int schweres) {
        super(type, pos, state);
        this.tanks[0] = new FluidTank(Fluids.WATER, wasser);
        this.tanks[1] = new FluidTank(Fluids.HEAVYWATER, schweres);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.hasPower() && this.hasEnoughWater() && this.tanks[1].getFill() < this.tanks[1].getMaxFill()) {

            // Erst teilen, dann malnehmen -- so bleibt kein Rest im Wassertank haengen.
            int menge = Math.min(this.tanks[1].getMaxFill(), this.tanks[0].getFill()) / VERHAELTNIS;
            menge = Math.min(menge, this.tanks[1].getMaxFill() - this.tanks[1].getFill());

            this.tanks[0].setFill(this.tanks[0].getFill() - menge * VERHAELTNIS);
            this.tanks[1].setFill(this.tanks[1].getFill() + menge);
            this.power -= this.getMaxPower() / 20;
        }

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
        }

        this.networkPackNT(50);
    }

    /** Der Extraktor haengt an allen sechs Seiten. */
    protected DirPos[] getConPos() {

        DirPos[] aus = new DirPos[Direction.values().length];
        int i = 0;
        for(Direction dir : Direction.values()) aus[i++] = new DirPos(this.worldPosition.relative(dir), dir);
        return aus;
    }

    public boolean hasPower() { return this.power >= this.getMaxPower() / 20; }
    public boolean hasEnoughWater() { return this.tanks[0].getFill() >= 100; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.tanks[0].readFromNBT(tag, "water");
        this.tanks[1].readFromNBT(tag, "heavyWater");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        this.tanks[0].writeToNBT(tag, "water");
        this.tanks[1].writeToNBT(tag, "heavyWater");
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return 10_000; }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }

    /** Die Tanks stehen fest -- es gibt nichts einzufuegen. */
    @Override public @Nullable FluidTank getTankToPaste() { return null; }
}
