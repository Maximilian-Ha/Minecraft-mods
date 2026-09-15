package com.hbm.blocks.machine.pile;

import com.hbm.blockentity.machine.pile.PileLoaderBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Der Nachlader: schiebt Brennstoffstaebe seitlich in einen Brennstoffkanal. */
public class PileLoaderBlock extends PileDeviceBlock {

    public PileLoaderBlock(Properties properties) {
        super(properties, false, true);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileLoaderBlockEntity(pos, state);
    }

    public static final MapCodec<PileLoaderBlock> CODEC = simpleCodec(PileLoaderBlock::new);
    @Override protected MapCodec<PileLoaderBlock> codec() { return CODEC; }
}
