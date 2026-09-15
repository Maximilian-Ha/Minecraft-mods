package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKInlet.
 *
 * Der Wassereinlass. Steht neben dem Reaktor und schiebt Speisewasser in die angrenzenden
 * Saeulen -- allerdings nur, wenn der Dial dialReasimBoilers gesetzt ist, denn erst dann
 * verhalten sich alle Saeulen wie Dampferzeuger.
 */
public class RBMKInletBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardReceiverMK2 {

    public FluidTank water;

    public RBMKInletBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_INLET.get(), pos, state);
        this.water = new FluidTank(Fluids.WATER, 32_000);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        for(Direction dir : Direction.Plane.HORIZONTAL) {
            this.trySubscribe(this.water.getTankType(), this.level, new DirPos(this.worldPosition.relative(dir), dir));
        }
        this.trySubscribe(this.water.getTankType(), this.level, new DirPos(this.worldPosition.below(), Direction.DOWN));
        this.trySubscribe(this.water.getTankType(), this.level, new DirPos(this.worldPosition.above(), Direction.UP));

        if(RBMKDials.getReasimBoilers(this.level)) {

            for(Direction dir : Direction.Plane.HORIZONTAL) {

                RBMKBaseBlockEntity rbmk = RBMKBaseBlock.findColumn(this.level, this.worldPosition.relative(dir));
                if(rbmk == null) continue;

                int prov = Math.min(RBMKBaseBlockEntity.MAX_WATER - rbmk.reasimWater, this.water.getFill());
                rbmk.reasimWater += prov;
                this.water.setFill(this.water.getFill() - prov);
            }
        }

        this.networkPackNT(25);
    }

    @Override
    public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.water }; }

    @Override
    public FluidTank[] getAllTanks() { return new FluidTank[] { this.water }; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.water.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.water.writeToNBT(tag, "tank");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.water.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.water.deserialize(buf);
    }
}
