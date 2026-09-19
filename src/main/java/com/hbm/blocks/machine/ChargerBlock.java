package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.ChargerBlockEntity;
import com.hbm.blocks.INBTBlockTransformable;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.Charger.
 *
 * Eine Ladestation an der Wand: wer davortritt, dem laedt sie die Batterien in Ruestung und
 * Hand auf. Der Arm faehrt dabei aus und schwenkt auf -- das zeichnet der Darsteller.
 *
 * Die vier Umrisse stammen aus setBlockBoundsBasedOnState des Originals: das Geraet haengt
 * flach an der Wand, in halber Hoehe, sechs Pixel breit.
 */
public class ChargerBlock extends BaseEntityBlock implements ITooltipProvider, INBTBlockTransformable {

    public static final MapCodec<ChargerBlock> CODEC = simpleCodec(ChargerBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.NORTH, Block.box(5, 4, 12, 11, 12, 16));
        SHAPES.put(Direction.SOUTH, Block.box(5, 4, 0, 11, 12, 4));
        SHAPES.put(Direction.WEST, Block.box(12, 4, 5, 16, 12, 11));
        SHAPES.put(Direction.EAST, Block.box(0, 4, 5, 4, 12, 11));
    }

    public ChargerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<ChargerBlock> codec() { return CODEC; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState transformState(BlockState state, Rotation rotation) {
        return INBTBlockTransformable.transformFacingState(state, rotation);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
