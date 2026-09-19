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
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DecoBlock, soweit sie steel_wall betrifft.
 *
 * Eine zwei Pixel dicke Stahlwand an einer Seite des Blocks. Anders als bei der Holzbohle
 * benennt FACING hier die Richtung, in die der Spieler beim Setzen schaut -- die Wand steht
 * dann auf der ABGEWANDTEN Seite. Das ist die Zuordnung des Originals (onBlockPlacedBy setzt
 * Metadatum 3 fuer Blick nach Sueden, und Metadatum 3 zeichnet die Wand an der Nordkante).
 *
 * Der Schraubendreher dreht sie weiter, im Original in der Reihenfolge 3 - 4 - 2 - 5; mit
 * gedrueckter Schleichtaste andersherum.
 */
public class SteelWallBlock extends HorizontalDirectionalBlock implements IToolable {

    public static final MapCodec<SteelWallBlock> CODEC = simpleCodec(SteelWallBlock::new);

    /* Die Wand des Originals: Metadatum 2 (Norden) zeichnet sie bei z 14..16. */
    private static final VoxelShape NORTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape WEST = Block.box(14, 0, 0, 16, 16, 16);
    private static final VoxelShape EAST = Block.box(0, 0, 0, 2, 16, 16);

    public SteelWallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<SteelWallBlock> codec() { return CODEC; }

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
        return shapeFor(state.getValue(FACING));
    }

    static VoxelShape shapeFor(Direction facing) {
        return switch(facing) {
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
