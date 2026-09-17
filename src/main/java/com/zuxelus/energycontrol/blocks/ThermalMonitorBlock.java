package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.ThermalMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.ThermalMonitor.
 *
 * Der Waermemelder. Der Zustand steckt im Blockzustand, damit die Schauseite ihn ohne
 * eigenen Renderer zeigen kann: 0 kein Reaktor gefunden, 1 in Ordnung, 2 Alarm.
 */
public class ThermalMonitorBlock extends BaseEntityBlock {

    public static final IntegerProperty STATUS = IntegerProperty.create("status", 0, 2);

    public static final MapCodec<ThermalMonitorBlock> CODEC = simpleCodec(ThermalMonitorBlock::new);

    public ThermalMonitorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATUS, 0));
    }

    @Override
    public MapCodec<ThermalMonitorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThermalMonitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if(be instanceof ThermalMonitorBlockEntity monitor) monitor.tick();
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
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ThermalMonitorBlockEntity monitor ? monitor.getSignal() : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    /** Ein flacher Kasten auf dem Boden, kein voller Wuerfel -- so sieht ihn das Modell. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BoxShape.slab(Direction.UP, 1, 7);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
