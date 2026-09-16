package com.hbm.blockentity.machine.oil;

import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntitySpacer.
 *
 * Der Abstandshalter zwischen zwei Fraktioniertuermen. Er rechnet nichts und tickt nicht --
 * er ist nur der Traeger, an dem das Modell haengt, und gibt dessen Ausmasse an.
 */
public class SpacerBlockEntity extends BlockEntity {

    public SpacerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FRACTION_SPACER.get(), pos, state);
    }

    /* Ohne @Override: getRenderBoundingBox() stammt aus der NeoForge-Erweiterung der
     * Block-Entitaet und gilt dem Compiler nicht als ueberschriebene Methode. */
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
    }
}
