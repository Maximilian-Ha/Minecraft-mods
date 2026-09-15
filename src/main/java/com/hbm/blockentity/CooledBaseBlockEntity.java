package com.hbm.blockentity;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityCooledBase.
 *
 * Alles, was mit supraleitenden Magneten arbeitet, muss kalt gehalten werden. Die Basis fuehrt
 * dafuer zwei Tanks -- kaltes Perfluormethyl hinein, warmes hinaus -- und eine Temperatur, die
 * von selbst wieder auf Raumtemperatur steigt.
 *
 * Ein halbes Grad je Millibucket Kuehlmittel, hoechstens siebeneinhalb Grad je Tick, und die
 * Anlage arbeitet erst, wenn sie auf 123 Kelvin herunter ist.
 *
 * Die Feldnamen des Originals sind uebernommen: "coolantTanks" statt "tanks", damit Ableitungen
 * ihre eigenen Tanks ohne Verwechslung fuehren koennen.
 */
public abstract class CooledBaseBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

    public final FluidTank[] coolantTanks = new FluidTank[2];

    public long power;

    public static final float KELVIN = 273F;
    public float temperature = KELVIN + 20;
    public static final float temperature_target = KELVIN - 150F;
    public static final float temp_change_per_mb = 0.5F;
    public static final float temp_passive_heating = 2.5F;
    public static final float temp_change_max = 5F + temp_passive_heating;

    public CooledBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
        super((BlockEntityType<? extends MachineBaseBlockEntity>) type, pos, state, slotCount);

        this.coolantTanks[0] = new FluidTank(Fluids.PERFLUOROMETHYL_COLD, 4_000);
        this.coolantTanks[1] = new FluidTank(Fluids.PERFLUOROMETHYL, 4_000);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);
            this.trySubscribe(this.coolantTanks[0].getTankType(), this.level, pos);
            this.tryProvide(this.coolantTanks[1], this.level, pos);
        }

        this.cool();

        this.networkPackNT(50);
    }

    /** Ein Tick Temperaturausgleich: Aufheizen von selbst, Abkuehlen gegen Kuehlmittel. */
    protected void cool() {

        this.temperature += temp_passive_heating;
        if(this.temperature > KELVIN + 20) this.temperature = KELVIN + 20;

        if(this.temperature > temperature_target) {

            int cyclesTemp = (int) Math.ceil(Math.min(this.temperature - temperature_target, temp_change_max) / temp_change_per_mb);
            int cyclesCool = this.coolantTanks[0].getFill();
            int cyclesHot = this.coolantTanks[1].getMaxFill() - this.coolantTanks[1].getFill();
            int cycles = BobMathUtil.min(cyclesTemp, cyclesCool, cyclesHot);

            this.coolantTanks[0].setFill(this.coolantTanks[0].getFill() - cycles);
            this.coolantTanks[1].setFill(this.coolantTanks[1].getFill() + cycles);
            this.temperature -= temp_change_per_mb * cycles;
        }
    }

    public boolean isCool() {
        return this.temperature <= temperature_target;
    }

    public abstract DirPos[] getConPos();

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.coolantTanks[0].serialize(buf);
        this.coolantTanks[1].serialize(buf);
        buf.writeFloat(this.temperature);
        buf.writeLong(this.power);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.coolantTanks[0].deserialize(buf);
        this.coolantTanks[1].deserialize(buf);
        this.temperature = buf.readFloat();
        this.power = buf.readLong();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.coolantTanks[0].readFromNBT(tag, "t0");
        this.coolantTanks[1].readFromNBT(tag, "t1");
        this.temperature = tag.getFloat("temperature");
        this.power = tag.getLong("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.coolantTanks[0].writeToNBT(tag, "t0");
        this.coolantTanks[1].writeToNBT(tag, "t1");
        tag.putFloat("temperature", this.temperature);
        tag.putLong("power", this.power);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.coolantTanks[1] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.coolantTanks[0] }; }
    @Override public FluidTank[] getAllTanks() { return this.coolantTanks; }
}
