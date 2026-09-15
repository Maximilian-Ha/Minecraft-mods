package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKOutgasserBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKOutgasser. Die Bestrahlungssaeule. */
public class RBMKOutgasserBlock extends RBMKBaseBlock {

    public RBMKOutgasserBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKOutgasserBlock> CODEC = simpleCodec(RBMKOutgasserBlock::new);

    @Override
    protected MapCodec<RBMKOutgasserBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKOutgasserBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
