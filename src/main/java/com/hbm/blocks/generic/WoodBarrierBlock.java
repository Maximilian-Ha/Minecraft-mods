package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockBarrier.
 *
 * Eine zwei Pixel dicke Bohle, die an einer der vier Seitenflaechen des Blocks klebt. Im
 * Original steckt die Seite in den Metadaten, hier in FACING -- und FACING benennt die Seite,
 * AN der die Bohle sitzt, nicht die Richtung, in die sie zeigt.
 *
 * Gesetzt wird sie wie im Original: klickt man eine Seitenflaeche an, klebt sie an dieser
 * Flaeche; klickt man von oben oder unten, richtet sie sich nach der Blickrichtung.
 */
public class WoodBarrierBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<WoodBarrierBlock> CODEC = simpleCodec(WoodBarrierBlock::new);

    private static final VoxelShape WEST = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape EAST = Block.box(14, 0, 0, 16, 16, 16);
    private static final VoxelShape NORTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SOUTH = Block.box(0, 0, 14, 16, 16, 16);

    public WoodBarrierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<WoodBarrierBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction side = face.getAxis().isHorizontal() ? face.getOpposite() : context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, side);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(FACING)) {
            case WEST -> WEST;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            default -> NORTH;
        };
    }
}
