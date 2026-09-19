package com.hbm.blocks.fluids;

import com.hbm.blocks.NtmBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.fluid.ToxicBlock.
 *
 * Die Giftbruehe aus dem gelben Fass. Sie haelt fest wie ein Spinnennetz, verstrahlt, wer
 * darin steht, und erstarrt zu geloeschtem Sellafit, sobald sie eine andere Fluessigkeit
 * beruehrt.
 *
 * ABWEICHUNG: das Original ist ein BlockFluidClassic mit vier Stufen und eigener
 * Verdraengungslogik; auf 1.21 uebernimmt das Fluidsystem das Fliessen, die vier Stufen
 * entsprechen dem Stufenabfall von zwei je Block. So steht es auch beim Rotschlamm.
 */
public class ToxicLiquidBlock extends LiquidBlock {

    public ToxicLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        /* Zaeh wie ein Spinnennetz -- das Original ruft hier setInWeb. */
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));

        if(level.isClientSide) return;

        if(entity instanceof LivingEntity living) {
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 1.0F);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if(level.isClientSide) return;

        /* Beruehrt die Bruehe eine fremde Fluessigkeit, erstarrt sie an Ort und Stelle. */
        for(Direction dir : Direction.values()) {

            BlockState neighbor = level.getBlockState(pos.relative(dir));

            if(neighbor.getBlock() instanceof ToxicLiquidBlock) continue;
            if(neighbor.getFluidState().isEmpty()) continue;

            level.setBlock(pos, NtmBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            return;
        }
    }
}
