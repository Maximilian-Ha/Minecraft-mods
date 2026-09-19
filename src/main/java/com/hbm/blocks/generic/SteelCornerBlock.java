package com.hbm.blocks.generic;

import api.hbm.block.IToolable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DecoBlock samt RenderSteelCorner,
 * soweit sie steel_corner betrifft.
 *
 * Die Aussenecke zweier Stahlwaende. Sie besteht aus drei Quadern: der langen Wand, einem
 * dickeren Eckstueck und dem kurzen Schenkel. Die Zahlen sind unveraendert aus dem Darsteller
 * des Originals uebernommen, der sie fuer Metadatum 2 (Norden) so setzt:
 *
 *   (4|0|14)-(16|16|16), (0|0|12)-(4|16|16), (0|0|0)-(2|16|12)
 *
 * Die drei uebrigen Richtungen sind im Original genau diese Form gedreht -- nachgerechnet und
 * deckungsgleich --, deshalb steht hier ein Modell mit vier Drehungen.
 *
 * Der Block selbst hat im Original volle Wuerfelmasse; die Form hier ist die tatsaechliche
 * Geometrie, damit man nicht auf Luft steht.
 */
public class SteelCornerBlock extends HorizontalDirectionalBlock implements IToolable {

    public static final MapCodec<SteelCornerBlock> CODEC = simpleCodec(SteelCornerBlock::new);

    private static final VoxelShape NORTH = Shapes.or(
            Block.box(4, 0, 14, 16, 16, 16),
            Block.box(0, 0, 12, 4, 16, 16),
            Block.box(0, 0, 0, 2, 16, 12));
    private static final VoxelShape SOUTH = Shapes.or(
            Block.box(0, 0, 0, 12, 16, 2),
            Block.box(12, 0, 0, 16, 16, 4),
            Block.box(14, 0, 4, 16, 16, 16));
    private static final VoxelShape WEST = Shapes.or(
            Block.box(14, 0, 0, 16, 16, 12),
            Block.box(12, 0, 12, 16, 16, 16),
            Block.box(0, 0, 14, 12, 16, 16));
    private static final VoxelShape EAST = Shapes.or(
            Block.box(0, 0, 4, 2, 16, 16),
            Block.box(0, 0, 0, 4, 16, 4),
            Block.box(4, 0, 0, 16, 16, 2));

    public SteelCornerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<SteelCornerBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {
        if(tool != ToolType.SCREWDRIVER) return false;
        if(level.isClientSide) return true;

        BlockState state = level.getBlockState(pos);
        Direction facing = state.getValue(FACING);
        level.setBlock(pos, state.setValue(FACING, player.isShiftKeyDown() ? facing.getCounterClockWise() : facing.getClockWise()), 3);
        return true;
    }
}
