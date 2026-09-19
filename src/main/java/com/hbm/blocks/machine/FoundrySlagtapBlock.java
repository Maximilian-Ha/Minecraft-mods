package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.FoundrySlagtapBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundrySlagtap.
 *
 * Der Schlackenabstich. Im Original ist er der Ausguss mit anderen Texturen und einer anderen
 * Blockentitaet, und genau das ist er hier auch: Form, Filter, Riegel und Werkzeugverhalten
 * kommen unveraendert von FoundryOutletBlock, nur das Modell und die Blockentitaet sind eigen.
 */
public class FoundrySlagtapBlock extends FoundryOutletBlock {

    public static final MapCodec<FoundrySlagtapBlock> CODEC = simpleCodec(FoundrySlagtapBlock::new);

    public FoundrySlagtapBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<FoundrySlagtapBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundrySlagtapBlockEntity(pos, state);
    }
}
