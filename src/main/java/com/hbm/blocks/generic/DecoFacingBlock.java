package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Ein Ausstattungsstueck der Bauwerke -- Bildschirm, Toaster, Tonbandgeraet. Es tut nichts,
 * steht nur herum und richtet sich beim Setzen nach der Blickrichtung, wie die Deko-Bloecke
 * des Originals (BlockDecoCRT, BlockDecoToaster, DecoTapeRecorder).
 *
 * Alle drei haben im Original mehrere Ausfuehrungen in den Metadaten; im Port ist jede ein
 * eigener Block, und das Aussehen steckt im Modell, nicht in der Klasse. Darum kommt eine
 * Klasse fuer alle aus.
 */
public class DecoFacingBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<DecoFacingBlock> CODEC = simpleCodec(DecoFacingBlock::new);

    public DecoFacingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<DecoFacingBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
}
