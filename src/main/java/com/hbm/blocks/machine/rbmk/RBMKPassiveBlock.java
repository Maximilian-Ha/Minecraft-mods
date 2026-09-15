package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKPassiveBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: RBMKBlank, RBMKModerator, RBMKAbsorber und RBMKReflector.
 *
 * Die vier Saeulen ohne eigene Mechanik. Sie unterscheiden sich nur darin, was sie mit einem
 * durchlaufenden Neutronenstrom anstellen.
 */
public class RBMKPassiveBlock extends RBMKBaseBlock {

    public static final MapCodec<RBMKPassiveBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(RBMKType::valueOf, RBMKType::name).fieldOf("rbmk_type").forGetter(block -> block.type),
            propertiesCodec()
    ).apply(instance, RBMKPassiveBlock::new));

    public final RBMKType type;

    public RBMKPassiveBlock(RBMKType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    protected MapCodec<RBMKPassiveBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKPassiveBlockEntity(pos, state);
    }
}
