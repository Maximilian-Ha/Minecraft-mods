package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.IndustrialAlarm.
 *
 * Die Warnleuchte. Sie leuchtet, solange sie ein Redstone-Signal bekommt, und braucht
 * dafuer keine Block-Entitaet -- das Licht haengt am Blockzustand.
 */
public class IndustrialAlarmBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final MapCodec<IndustrialAlarmBlock> CODEC = simpleCodec(IndustrialAlarmBlock::new);

    public IndustrialAlarmBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(POWERED) ? 15 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public MapCodec<IndustrialAlarmBlock> codec() {
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if(level.isClientSide) return;
        boolean powered = level.hasNeighborSignal(pos);
        if(powered != state.getValue(POWERED)) level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
    }

    /** Ein flacher Kasten, kein voller Wuerfel -- so sieht ihn auch das Modell. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BoxShape.slab(state.getValue(FACING), 2, 7);
    }

}
