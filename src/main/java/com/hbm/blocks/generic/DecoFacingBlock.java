package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

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

    /** Nur gesetzt, wo das Original eigene Masse angibt; sonst ein voller Wuerfel. */
    @Nullable private final Map<Direction, VoxelShape> shapes;

    public DecoFacingBlock(Properties properties) {
        this(properties, null);
    }

    public DecoFacingBlock(Properties properties, @Nullable VoxelShape shape) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));

        if(shape == null) {
            this.shapes = null;
        } else {
            /* Einmal vorgedreht statt bei jedem Kollisionstest: die Masse des Originals
             * gelten fuer Norden, und das Modell wird im Uhrzeigersinn mitgedreht. */
            this.shapes = new EnumMap<>(Direction.class);
            this.shapes.put(Direction.NORTH, shape);
            this.shapes.put(Direction.EAST, drehe(shape, 1));
            this.shapes.put(Direction.SOUTH, drehe(shape, 2));
            this.shapes.put(Direction.WEST, drehe(shape, 3));
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(this.shapes == null) return super.getShape(state, level, pos, context);
        return this.shapes.get(state.getValue(FACING));
    }

    /** Dreht eine Form um Vielfache von neunzig Grad im Uhrzeigersinn: (x|z) wird zu (1-z|x). */
    private static VoxelShape drehe(VoxelShape shape, int viertel) {

        VoxelShape gedreht = shape;

        for(int i = 0; i < viertel; i++) {
            VoxelShape[] sammler = { Shapes.empty() };
            gedreht.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    sammler[0] = Shapes.or(sammler[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
            gedreht = sammler[0];
        }

        return gedreht;
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
