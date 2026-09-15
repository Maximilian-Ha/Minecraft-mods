package com.hbm.blocks.machine.rbmk;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKDebris.
 *
 * Der Schutt, der von einer eingestuerzten RBMK-Saeule uebrig bleibt.
 */
public class RBMKDebrisBlock extends Block {

    public RBMKDebrisBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKDebrisBlock> CODEC = simpleCodec(RBMKDebrisBlock::new);

    @Override
    protected MapCodec<? extends RBMKDebrisBlock> codec() {
        return CODEC;
    }
}
