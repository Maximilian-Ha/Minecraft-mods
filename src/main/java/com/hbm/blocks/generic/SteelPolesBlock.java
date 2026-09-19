package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DecoSteelPoles.
 *
 * Zwei Masten mit Querstrebe, wie sie an den Bauwerken stehen. Das Original gibt ihnen keine
 * eigene Kollisionsform -- sie haben volle Wuerfelmasse, obwohl das Modell schlank ist; das
 * bleibt hier so.
 *
 * Die Aufstellrichtung folgt der Blickrichtung, wie in onBlockPlacedBy des Originals.
 */
public class SteelPolesBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<SteelPolesBlock> CODEC = simpleCodec(SteelPolesBlock::new);

    public SteelPolesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<SteelPolesBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
}
