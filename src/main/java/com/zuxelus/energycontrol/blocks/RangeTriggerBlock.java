package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.RangeTrigger.
 *
 * Der Bereichsmelder. Wie beim Waermemelder steckt der Zustand im Blockzustand:
 * 0 keine oder ungueltige Karte, 1 innerhalb des Bereichs, 2 ausserhalb.
 */
public class RangeTriggerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty STATUS = IntegerProperty.create("status", 0, 2);

    public static final MapCodec<RangeTriggerBlock> CODEC = simpleCodec(RangeTriggerBlock::new);

    public RangeTriggerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(STATUS, 0));
    }

    @Override
    public MapCodec<RangeTriggerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATUS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RangeTriggerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if(be instanceof RangeTriggerBlockEntity trigger) trigger.tick();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof MenuProvider menu) player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof RangeTriggerBlockEntity trigger) Containers.dropContents(level, pos, trigger);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RangeTriggerBlockEntity trigger ? trigger.getSignal() : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
