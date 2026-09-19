package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.BroadcasterBlockEntity;
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

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.PinkCloudBroadcaster.
 *
 * Dasselbe Gehaeuse wie der Funkempfaenger: das Original benutzt fuer beide dasselbe
 * Techne-Modell und nur eine andere Haut. Auch Umriss und Ausrichtung stimmen ueberein --
 * vier achsenparallele Kaesten, die sich als Blockmodell ausdruecken lassen, also auch hier
 * kein eigener Darsteller.
 *
 * Er blickt in die Richtung des Setzenden, nicht von ihm weg.
 */
public class BroadcasterBlock extends BaseEntityBlock implements ITooltipProvider, INBTBlockTransformable {

    public static final MapCodec<BroadcasterBlock> CODEC = simpleCodec(BroadcasterBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Masse aus setBlockBoundsBasedOnState des Originals -- dieselben wie beim Empfaenger. */
    private static final VoxelShape SHAPE_NS = Block.box(1, 0, 4, 15, 10, 12);
    private static final VoxelShape SHAPE_WE = Block.box(4, 0, 1, 12, 10, 15);

    public BroadcasterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<BroadcasterBlock> codec() { return CODEC; }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

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
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_WE;
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
        return new BroadcasterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
