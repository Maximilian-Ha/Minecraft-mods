package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKDebrisDigamma.
 *
 * Der Schutt einer Anlage, in der ein DRX-Stab geschmolzen ist. Er kuehlt nicht ab und klingt
 * nicht ab -- er frisst sich weiter: jeder Nachbar, der gewoehnlicher Schutt oder Corium ist,
 * wird selbst zu Digamma-Schutt. Zwei Ticks je Schritt, also fressend, aber langsam.
 */
public class RBMKDebrisDigammaBlock extends RBMKDebrisBlock {

    /** Wie viele Ticks zwischen zwei Ausbreitungsschritten liegen. */
    private static final int TICK_RATE = 2;

    public RBMKDebrisDigammaBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKDebrisDigammaBlock> CODEC = simpleCodec(RBMKDebrisDigammaBlock::new);

    @Override
    protected MapCodec<? extends RBMKDebrisDigammaBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if(!level.isClientSide) level.scheduleTick(pos, this, TICK_RATE);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        for(Direction dir : Direction.values()) {

            BlockPos neighborPos = pos.relative(dir);
            BlockState neighbor = level.getBlockState(neighborPos);

            boolean debris = neighbor.getBlock() instanceof RBMKDebrisBlock && !neighbor.is(this);
            boolean corium = neighbor.is(NtmBlocks.CORIUM.get()) || neighbor.is(NtmBlocks.BLOCK_CORIUM.get());

            if(debris || corium) level.setBlock(neighborPos, this.defaultBlockState(), 3);
        }
    }
}
