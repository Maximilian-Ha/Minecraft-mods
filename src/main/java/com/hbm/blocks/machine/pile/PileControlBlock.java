package com.hbm.blocks.machine.pile;

import com.hbm.blockentity.machine.pile.PileControlBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Der Steuerstabantrieb: steht oben auf einem Steuerkanal und faehrt den Stab ein und aus. */
public class PileControlBlock extends PileDeviceBlock {

    public PileControlBlock(Properties properties) {
        super(properties, true, false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileControlBlockEntity(pos, state);
    }

    public static final MapCodec<PileControlBlock> CODEC = simpleCodec(PileControlBlock::new);
    @Override protected MapCodec<PileControlBlock> codec() { return CODEC; }
}
