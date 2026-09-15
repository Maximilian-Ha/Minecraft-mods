package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKOutlet.
 *
 * Der Dampfauslass. Das Gegenstueck zum Einlass: er saugt den Dampf aus den angrenzenden
 * Saeulen ab und gibt ihn ans Rohrnetz weiter. Ebenfalls nur mit dem Dial dialReasimBoilers.
 */
public class RBMKOutletBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardSenderMK2 {

    public FluidTank steam;

    public RBMKOutletBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_OUTLET.get(), pos, state);
        this.steam = new FluidTank(Fluids.SUPERHOTSTEAM, 32_000);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(RBMKDials.getReasimBoilers(this.level)) {

            for(Direction dir : Direction.Plane.HORIZONTAL) {

                RBMKBaseBlockEntity rbmk = RBMKBaseBlock.findColumn(this.level, this.worldPosition.relative(dir));
                if(rbmk == null) continue;

                int prov = Math.min(this.steam.getMaxFill() - this.steam.getFill(), rbmk.reasimSteam);
                rbmk.reasimSteam -= prov;
                this.steam.setFill(this.steam.getFill() + prov);
            }
        }

        if(this.steam.getFill() > 0) {
            for(Direction dir : Direction.Plane.HORIZONTAL) {
                this.tryProvide(this.steam, this.level, new DirPos(this.worldPosition.relative(dir), dir));
            }
            this.tryProvide(this.steam, this.level, new DirPos(this.worldPosition.below(), Direction.DOWN));
            this.tryProvide(this.steam, this.level, new DirPos(this.worldPosition.above(), Direction.UP));
        }

        this.networkPackNT(25);
    }

    @Override
    public FluidTank[] getSendingTanks() { return new FluidTank[] { this.steam }; }

    @Override
    public FluidTank[] getAllTanks() { return new FluidTank[] { this.steam }; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.steam.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.steam.writeToNBT(tag, "tank");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.steam.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.steam.deserialize(buf);
    }
}
