package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryBasin.
 *
 * Das grosse Becken. Es nimmt nur die grossen Formen und laesst sich nur von oben befuellen --
 * seitlich hineinfliessen kann hier nichts.
 */
public class FoundryBasinBlockEntity extends FoundryCastingBaseBlockEntity implements IRenderFoundry {

    public FoundryBasinBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FOUNDRY_BASIN.get(), pos, state);
    }

    @Override public int getMoldSize() { return 1; }

    @Override public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return stack; }

    @Override public boolean shouldRender() { return this.type != null && this.amount > 0; }
    @Override public double getFillLevel() { return this.getCapacity() <= 0 ? 0.125D : 0.125D + this.amount * 0.75D / this.getCapacity(); }
    @Override public NTMMaterial getMat() { return this.type; }

    @Override public double minX() { return 0.125D; }
    @Override public double maxX() { return 0.875D; }
    @Override public double minZ() { return 0.125D; }
    @Override public double maxZ() { return 0.875D; }
    @Override public double moldHeight() { return 0.13D; }
    @Override public double outHeight() { return 0.875D; }
}
