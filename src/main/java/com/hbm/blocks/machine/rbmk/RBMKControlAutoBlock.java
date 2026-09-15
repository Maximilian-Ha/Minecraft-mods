package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKControlAutoBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKControlAuto.
 * Der selbsttaetige Steuerstab; Graphit gibt es bei dieser Bauform nicht.
 */
public class RBMKControlAutoBlock extends RBMKControlBlock {

    public RBMKControlAutoBlock(Properties properties) {
        this(false, properties);
    }

    public RBMKControlAutoBlock(boolean powered, Properties properties) {
        super(false, powered, properties);
    }

    public static final MapCodec<RBMKControlAutoBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("powered", false).forGetter(block -> block.powered),
            propertiesCodec()
    ).apply(instance, RBMKControlAutoBlock::new));

    @Override
    protected MapCodec<? extends RBMKControlAutoBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKControlAutoBlockEntity(pos, state);
    }
}
