package com.hbm.blocks.machine.pile;

import com.hbm.blockentity.machine.pile.PileVentBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Der Luefter: pumpt Pressluft seitlich in einen Lueftungskanal. */
public class PileVentBlock extends PileDeviceBlock {

    public PileVentBlock(Properties properties) {
        super(properties, false, false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileVentBlockEntity(pos, state);
    }

    public static final MapCodec<PileVentBlock> CODEC = simpleCodec(PileVentBlock::new);
    @Override protected MapCodec<PileVentBlock> codec() { return CODEC; }
}
