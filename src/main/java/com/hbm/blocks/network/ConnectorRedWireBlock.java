package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.ConnectorBlockEntity;
import com.hbm.blockentity.network.PylonBaseBlockEntity;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.ConnectorRedWire.
 *
 * Anders als die Masten ist der Anschlusskasten KEIN Multiblock: im Original erbt er von
 * PylonBase (einem schlichten BlockContainer), waehrend PylonRedWire & Co. von
 * BlockDummyable erben. Die gemeinsame Aufgabe von PylonBase -- Draehte beim Abbau loesen
 * und Faerben per Farbstoff -- steht deshalb hier direkt.
 *
 * FACING ist die angeklickte Blockseite (im Original die Metadaten-Seite aus
 * onBlockPlaced); der Kasten haengt also an der Gegenseite fest.
 */
public class ConnectorRedWireBlock extends BaseEntityBlock implements ITooltipProvider {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<ConnectorRedWireBlock> CODEC = simpleCodec(ConnectorRedWireBlock::new);

    public ConnectorRedWireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<? extends ConnectorRedWireBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    /** Im Original getRenderType() == -1: der Block wird ausschliesslich vom OBJ-Renderer gezeichnet. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PylonBaseBlockEntity pylon) {
            pylon.disconnectAll();
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    /** Farbstoff in der Hand faerbt den Draht, wie bei den Masten. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.getBlockEntity(pos) instanceof PylonBaseBlockEntity pylon && pylon.setColor(stack)) {
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        double pixel = 0.0625D;
        double min = pixel * 5D;
        double max = pixel * 11D;

        Direction dir = state.getValue(FACING).getOpposite();

        double minX = dir == Direction.WEST ? 0D : min;
        double maxX = dir == Direction.EAST ? 1D : max;
        double minY = dir == Direction.DOWN ? 0D : min;
        double maxY = dir == Direction.UP ? 1D : max;
        double minZ = dir == Direction.NORTH ? 0D : min;
        double maxZ = dir == Direction.SOUTH ? 1D : max;

        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
