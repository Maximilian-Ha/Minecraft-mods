package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachinePumpElectric.
 *
 * Die elektrische Wasserpumpe. Tausend HE je Zug, zehntausend Millibar Wasser dafuer -- zehnmal
 * so schnell wie die Dampfpumpe und der Regelfall, sobald ein Stromnetz steht.
 */
public class MachinePumpElectricBlockEntity extends MachinePumpBaseBlockEntity implements IEnergyReceiverMK2 {

    public static final long MAX_POWER = 10_000;
    private static final long PER_CYCLE = 1_000;

    public long power;

    public MachinePumpElectricBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PUMP_ELECTRIC.get(), pos, state);
        this.water = new FluidTank(Fluids.WATER, ELECTRIC_SPEED * 100);
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide && this.level.getGameTime() % 20 == 0) {
            for(DirPos pos : this.getConPos()) this.trySubscribe(this.level, pos);
        }

        super.updateEntity();
    }

    @Override
    protected boolean canOperate() {
        return this.power >= PER_CYCLE && this.water.getFill() < this.water.getMaxFill();
    }

    @Override
    protected void operate() {
        this.power -= PER_CYCLE;
        this.water.setFill(Math.min(this.water.getFill() + ELECTRIC_SPEED, this.water.getMaxFill()));
    }

    @Override public boolean canConnect(Direction dir) { return true; }

    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }
    @Override public void setPower(long power) { this.power = power; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
    }
}
