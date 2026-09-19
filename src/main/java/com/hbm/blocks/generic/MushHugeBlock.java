package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockMushHuge.
 *
 * Hut und Stiel des Riesenpilzes. Beide leuchten und zerfallen leicht; abgebaut geben sie
 * mit geringer Wahrscheinlichkeit kleine Pilze her -- das steht in der Beutetabelle.
 */
public class MushHugeBlock extends Block {

    public static final MapCodec<MushHugeBlock> CODEC = simpleCodec(MushHugeBlock::new);

    public MushHugeBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MushHugeBlock> codec() { return CODEC; }
}
