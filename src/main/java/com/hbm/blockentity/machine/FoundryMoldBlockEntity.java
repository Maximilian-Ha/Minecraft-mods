package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.material.NTMMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryMold.
 *
 * Die kleine Giessform. Sie nimmt die kleinen Formen und laesst -- anders als das Becken --
 * auch seitlich hineinfliessendes Metall zu.
 */
public class FoundryMoldBlockEntity extends FoundryCastingBaseBlockEntity implements IRenderFoundry {

    public FoundryMoldBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FOUNDRY_MOLD.get(), pos, state);
    }

    @Override public int getMoldSize() { return 0; }

    @Override public boolean shouldRender() { return this.type != null && this.amount > 0; }
    @Override public double getFillLevel() { return this.getCapacity() <= 0 ? 0.125D : 0.125D + this.amount * 0.25D / this.getCapacity(); }
    @Override public NTMMaterial getMat() { return this.type; }

    @Override public double minX() { return 0.125D; }
    @Override public double maxX() { return 0.875D; }
    @Override public double minZ() { return 0.125D; }
    @Override public double maxZ() { return 0.875D; }
    @Override public double moldHeight() { return 0.13D; }
    @Override public double outHeight() { return 0.25D; }
}
