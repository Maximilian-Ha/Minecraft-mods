package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKHeaterBlockEntity;
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

/** Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKHeater. Die Waermetauschersaeule. */
public class RBMKHeaterBlock extends RBMKBaseBlock {

    /* Im Original eine RBMKPipedBase: ohne Deckel sitzen oben vier Rohrstutzen, mit Deckel
     * stattdessen die Platte. Beides schliesst sich aus. */
    @Override public boolean hasPipes() { return true; }

    public RBMKHeaterBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKHeaterBlock> CODEC = simpleCodec(RBMKHeaterBlock::new);

    @Override
    protected MapCodec<RBMKHeaterBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKHeaterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
