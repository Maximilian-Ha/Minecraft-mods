package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.oil.SpacerBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FractionSpacer.
 *
 * Ein Block hoch, drei mal drei breit. Er sitzt zwischen zwei Fraktioniertuermen und traegt
 * nur das Zwischenstueck -- der untere Turm reicht durch ihn hindurch zum naechsten.
 */
public class FractionSpacerBlock extends DummyableBlock {

    public FractionSpacerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == com.hbm.blocks.DummyBlockType.CORE ? new SpacerBlockEntity(pos, state) : null;
    }

    public static final MapCodec<FractionSpacerBlock> CODEC = simpleCodec(FractionSpacerBlock::new);
    @Override public MapCodec<FractionSpacerBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {0, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }
}
