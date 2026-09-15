package com.hbm.blocks.machine.rbmk;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase.
 *
 * Der gemeinsame Unterbau aller kleinen Anzeigetafeln: ein Dreiviertelfeld, das an der Wand
 * klebt. Das leere Viertel liegt immer auf der Seite, von der man die Tafel abliest -- so steht
 * man beim Ablesen nicht in ihr drin.
 *
 * FACING zeigt auf den Spieler, der die Tafel gesetzt hat, also auf die Ablesefseite.
 *
 * ABWEICHUNG: das Original zeichnet den Block ueber einen eigenen Renderer (ISBRHUniversal) und
 * setzt die Grenzen jedes Mal neu aus den Metadaten. In 1.21 steckt beides im Blockzustand: die
 * vier Formen stehen fest, und das Modell kommt aus der Zustandsdatei.
 */
public class RBMKMiniPanelBlock extends Block {

    /* Vier Bloecke weit gekuerzt, jeweils zur Ablesefseite hin -- Zahlen aus dem Original. */
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 4, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 12);
    private static final VoxelShape SHAPE_WEST = Block.box(4, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 12, 16, 16);

    public RBMKMiniPanelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    public static final MapCodec<RBMKMiniPanelBlock> CODEC = simpleCodec(RBMKMiniPanelBlock::new);
    @Override protected MapCodec<? extends RBMKMiniPanelBlock> codec() { return CODEC; }
}
