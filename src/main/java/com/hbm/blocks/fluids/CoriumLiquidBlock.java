package com.hbm.blocks.fluids;

import com.hbm.blocks.NtmBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.fluid.CoriumBlock.
 *
 * Corium -- die geschmolzene Masse aus Brennstoff, Huellrohr und allem, was der Kern sonst noch
 * mitgerissen hat. Sie fliesst zaeh, setzt in Brand, verstrahlt und erstarrt binnen Sekunden.
 *
 * Was erstarrt, haengt davon ab, ob es eine Quelle war: Quellen werden zum dichten Coriumblock,
 * ausgelaufenes Corium zum poroesen Schutt, der Radon ausgast.
 *
 * ABWEICHUNGEN:
 * - Das Original ist ein "endliches" Fluid mit fuenf Stufen und eigener Verdraengungslogik, die
 *   sich an der Sprengfestigkeit des Nachbarblocks entscheidet. In 1.21 uebernimmt das
 *   Fluidsystem das Fliessen; die Verdraengungslogik entfaellt, weil es sie in dieser Form nicht
 *   mehr gibt.
 * - Das Original schlaegt zusaetzlich zwei Punkte Strahlenschaden zu. Der Port kennt keine
 *   eigene Strahlenschadensquelle -- die Verstrahlung selbst richtet den Schaden an, so wie bei
 *   der Sellafield-Lava auch.
 */
public class CoriumLiquidBlock extends LiquidBlock {

    public CoriumLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        /* Zaeh wie ein Spinnennetz -- wer hineinlaeuft, kommt kaum wieder heraus. */
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));

        if(level.isClientSide) return;

        entity.igniteForSeconds(3F);

        if(entity instanceof LivingEntity living) {
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 1F);
        }
    }

    /** Erstarrt mit einem Zehntel Wahrscheinlichkeit je Tick. */
    public static void baseTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if(!(state.getBlock() instanceof CoriumLiquidBlock)) return;
        if(random.nextInt(10) != 0) return;

        boolean source = state.getValue(LEVEL) == 0;

        level.setBlock(pos, source
                ? NtmBlocks.BLOCK_CORIUM.get().defaultBlockState()
                : NtmBlocks.BLOCK_CORIUM_COBBLE.get().defaultBlockState(), 3);
    }
}
