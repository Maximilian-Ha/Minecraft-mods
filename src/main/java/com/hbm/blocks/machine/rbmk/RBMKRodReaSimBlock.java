package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKRodReaSimBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKRodReaSim.
 * Der ReaSim-Brennkanal, wahlweise mit Graphit um den Kanal.
 */
public class RBMKRodReaSimBlock extends RBMKRodBlock {

    @Override
    public String getTextureBase() {
        return this.moderated ? "rbmk_element_reasim_mod" : "rbmk_element_reasim";
    }

    public static final MapCodec<RBMKRodReaSimBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("moderated").forGetter(block -> block.moderated),
            propertiesCodec()
    ).apply(instance, RBMKRodReaSimBlock::new));

    public RBMKRodReaSimBlock(boolean moderated, Properties properties) {
        super(moderated, properties);
    }

    @Override
    protected MapCodec<? extends RBMKRodReaSimBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKRodReaSimBlockEntity(pos, state);
    }
}
