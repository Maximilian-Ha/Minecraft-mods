package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.HowlerAlarmBlockEntity;
import com.zuxelus.energycontrol.init.ECSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.HowlerAlarm.
 *
 * Der Heuler. Ton und Hoerweite werden am Block eingestellt -- Rechtsklick schaltet den
 * Ton weiter, Rechtsklick im Schleichen die Hoerweite.
 */
public class HowlerAlarmBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final MapCodec<HowlerAlarmBlock> CODEC = simpleCodec(HowlerAlarmBlock::new);

    public HowlerAlarmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public MapCodec<HowlerAlarmBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HowlerAlarmBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        boolean powered = state.getValue(POWERED);
        return (lvl, pos, st, be) -> {
            if(be instanceof HowlerAlarmBlockEntity alarm) alarm.tick(powered);
        };
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if(level.isClientSide) return;
        boolean powered = level.hasNeighborSignal(pos);
        if(powered != state.getValue(POWERED)) level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof HowlerAlarmBlockEntity alarm)) return InteractionResult.PASS;

        if(player.isShiftKeyDown()) {
            alarm.cycleRange();
            player.displayClientMessage(Component.translatable("msg.ec.HowlerAlarmSoundRange", alarm.getRange()), true);
        } else {
            alarm.cycleSound();
            player.displayClientMessage(Component.translatable("msg.ec.HowlerAlarmSound")
                    .append(": ").append(Component.translatable(ECSounds.alarmName(alarm.getSoundIndex()))), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
