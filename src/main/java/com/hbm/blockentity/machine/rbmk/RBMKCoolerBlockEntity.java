package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.Compat;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKCooler.
 *
 * Die Kuehlsaeule. Verbraucht kaltes Perfluormethyl und zieht dafuer allen Saeulen im 5x5-Feld
 * um sich herum Waerme ab. Das erwaermte Mittel geht oben wieder heraus.
 */
public class RBMKCoolerBlockEntity extends RBMKBaseBlockEntity implements IFluidStandardTransceiverMK2 {

    /** Wie viele Ticks bis zur naechsten Nachbarsuche. */
    protected int timer = 0;
    private final FluidTank[] tanks;
    /** Die 25 Saeulen im Umkreis; wird nur alle drei Sekunden neu gesucht. */
    protected RBMKBaseBlockEntity[] coolingCache = new RBMKBaseBlockEntity[25];

    public RBMKCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_COOLER.get(), pos, state);

        this.tanks = new FluidTank[] {
                new FluidTank(Fluids.PERFLUOROMETHYL_COLD, 4_000),
                new FluidTank(Fluids.PERFLUOROMETHYL, 4_000)
        };
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) {
            super.updateEntity();
            return;
        }

        if(this.timer <= 0) {

            this.timer = 60;

            for(int i = 0; i < 25; i++) {
                BlockPos pos = this.worldPosition.offset(-2 + i / 5, 0, -2 + i % 5);
                BlockEntity tile = Compat.getBlockEntityStandard(this.level, pos);
                this.coolingCache[i] = tile instanceof RBMKBaseBlockEntity base ? base : null;
            }

        } else {
            this.timer--;
        }

        if(this.tanks[0].getFill() >= 50 && this.tanks[1].getMaxFill() - this.tanks[1].getFill() >= 50) {

            this.tanks[0].setFill(this.tanks[0].getFill() - 50);
            this.tanks[1].setFill(this.tanks[1].getFill() + 50);

            for(RBMKBaseBlockEntity neighbor : this.coolingCache) {
                if(neighbor != null) {
                    neighbor.heat -= 200;
                    if(neighbor.heat < 20) neighbor.heat = 20;
                }
            }
        }

        this.trySubscribe(this.tanks[0].getTankType(), this.level, new DirPos(this.worldPosition.below(), Direction.DOWN));

        if(this.tanks[1].getFill() > 0) {
            for(DirPos pos : this.getOutputPos()) this.tryProvide(this.tanks[1], this.level, pos);
        }

        super.updateEntity();
    }

    @Override
    public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1] }; }

    @Override
    public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }

    @Override
    public FluidTank[] getAllTanks() { return this.tanks; }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.COOLER;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.OTHER;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tanks[0].readFromNBT(tag, "cold");
        this.tanks[1].readFromNBT(tag, "hot");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tanks[0].writeToNBT(tag, "cold");
        this.tanks[1].writeToNBT(tag, "hot");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }
}
