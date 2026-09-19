package com.hbm.blocks.generic;

import com.hbm.blockentity.machine.DecoPoleSatelliteReceiverBlockEntity;
import com.hbm.blocks.INBTBlockTransformable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DecoPoleSatelliteReceiver.
 *
 * Der Aufsatz eines Antennenmastes mit einer Richtfunkschuessel. Er tut nichts, er steht nur
 * herum -- aber die Schuessel steht schraeg, und schraege Kaesten kann ein Blockmodell nicht.
 * Deshalb eine Blockentitaet mit eigenem Darsteller.
 *
 * Die Schuessel schaut vom Spieler weg: das Original legt bei Blick nach Sueden die Metadaten
 * auf Nord, bei Blick nach Westen auf Ost und so fort.
 */
public class DecoPoleSatelliteReceiverBlock extends BaseEntityBlock implements INBTBlockTransformable {

    public static final MapCodec<DecoPoleSatelliteReceiverBlock> CODEC = simpleCodec(DecoPoleSatelliteReceiverBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DecoPoleSatelliteReceiverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<DecoPoleSatelliteReceiverBlock> codec() { return CODEC; }

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
    public BlockState transformState(BlockState state, Rotation rotation) {
        return INBTBlockTransformable.transformFacingState(state, rotation);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecoPoleSatelliteReceiverBlockEntity(pos, state);
    }
}
