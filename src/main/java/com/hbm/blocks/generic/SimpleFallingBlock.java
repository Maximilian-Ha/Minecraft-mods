package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FallingBlock;

/**
 * Ein Block, der faellt, sobald unter ihm nichts mehr ist -- mehr nicht. In 1.7.10 war das
 * BlockFalling und liess sich unmittelbar anlegen; in 1.21 ist FallingBlock abstrakt und
 * verlangt einen Codec. Diese Klasse reicht ihn nach.
 */
public class SimpleFallingBlock extends FallingBlock {

    public static final MapCodec<SimpleFallingBlock> CODEC = simpleCodec(SimpleFallingBlock::new);

    public SimpleFallingBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends FallingBlock> codec() { return CODEC; }
}
