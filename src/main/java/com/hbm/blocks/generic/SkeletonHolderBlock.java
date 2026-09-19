package com.hbm.blocks.generic;

import com.hbm.blockentity.SkeletonHolderBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockSkeletonHolder.
 *
 * Ein Sockel, der genau einen Gegenstand haelt. Rechtsklick mit vollem Beutel legt ab,
 * Rechtsklick mit leerer Hand nimmt wieder mit; geduckt geschieht nichts. Beim Abbauen
 * faellt der Gegenstand heraus -- er gehoert nicht zur Beutetabelle, sondern zum Inhalt.
 *
 * Die Drehung ist die des Originals, samt ihrer Eigenart: der Sockel zeigt nicht dorthin,
 * wo man hinsieht, sondern eine Vierteldrehung gegen den Uhrzeigersinn davon. Im Original
 * steht das als Tabelle von Blickviertel auf Metadatenwert da, mit dem Kommentar des
 * Urhebers, dass er es nicht mehr aufraeumen wollte. Die Tabelle bleibt, weil die Bauwerke
 * des Originals sich darauf verlassen.
 */
public class SkeletonHolderBlock extends BaseEntityBlock {

    public static final MapCodec<SkeletonHolderBlock> CODEC = simpleCodec(SkeletonHolderBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SkeletonHolderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override public MapCodec<SkeletonHolderBlock> codec() { return CODEC; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getCounterClockWise());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    /** Mit etwas in der Hand: ablegen, aber nur auf einen leeren Sockel. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {

        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(level.getBlockEntity(pos) instanceof SkeletonHolderBlockEntity sockel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if(!sockel.item.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        // Wie im Original wandert der ganze Stapel auf den Sockel, nicht nur ein Stueck.
        sockel.item = stack.copy();
        player.setItemInHand(hand, ItemStack.EMPTY);
        sockel.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);

        return ItemInteractionResult.CONSUME;
    }

    /** Mit leerer Hand: mitnehmen, wenn etwas daliegt. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit) {

        if(player.isShiftKeyDown()) return InteractionResult.PASS;
        if(!(level.getBlockEntity(pos) instanceof SkeletonHolderBlockEntity sockel)) return InteractionResult.PASS;
        if(sockel.item.isEmpty()) return InteractionResult.PASS;

        if(level.isClientSide) return InteractionResult.SUCCESS;

        player.setItemInHand(InteractionHand.MAIN_HAND, sockel.item.copy());
        sockel.item = ItemStack.EMPTY;
        sockel.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof SkeletonHolderBlockEntity sockel
                && !sockel.item.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, sockel.item);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkeletonHolderBlockEntity(pos, state);
    }
}
