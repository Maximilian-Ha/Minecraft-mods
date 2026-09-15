package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKCoolerBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKCooler. Die Kuehlsaeule, ohne Oberflaeche. */
public class RBMKCoolerBlock extends RBMKBaseBlock {

    public RBMKCoolerBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKCoolerBlock> CODEC = simpleCodec(RBMKCoolerBlock::new);

    @Override
    protected MapCodec<RBMKCoolerBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKCoolerBlockEntity(pos, state);
    }
}
