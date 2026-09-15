package com.hbm.blocks.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FoundryChannelBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ScrapsItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundryChannel.
 *
 * Die Rinne, in der die Schmelze waagerecht laeuft. Sie waechst dorthin, wo sie andocken kann:
 * an andere Rinnen und an Giessformen.
 *
 * ABWEICHUNG: das Original liest die Nachbarschaft bei jedem Zeichnen neu. Auf 1.21 stehen die
 * vier Verbindungen im Blockzustand, wie bei einem Zaun -- so kennt sie auch das Modell.
 */
public class FoundryChannelBlock extends BaseEntityBlock implements ICrucibleAcceptor {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public FoundryChannelBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, Boolean.FALSE)
                .setValue(SOUTH, Boolean.FALSE)
                .setValue(EAST, Boolean.FALSE)
                .setValue(WEST, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST);
    }

    public static final MapCodec<FoundryChannelBlock> CODEC = simpleCodec(FoundryChannelBlock::new);
    @Override public MapCodec<FoundryChannelBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryChannelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /** Angedockt wird an andere Rinnen und an Giessformen -- beides nimmt seitlichen Zulauf an. */
    public boolean canConnectTo(LevelReader level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block == NtmBlocks.FOUNDRY_CHANNEL.get() || block == NtmBlocks.FOUNDRY_MOLD.get();
    }

    private BlockState withConnections(BlockState state, LevelReader level, BlockPos pos) {
        return state
                .setValue(NORTH, this.canConnectTo(level, pos.north()))
                .setValue(SOUTH, this.canConnectTo(level, pos.south()))
                .setValue(EAST, this.canConnectTo(level, pos.east()))
                .setValue(WEST, this.canConnectTo(level, pos.west()));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.withConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return this.withConnections(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {

        double min = 0.3125D;
        double max = 0.6875D;

        return Shapes.box(
                state.getValue(WEST) ? 0D : min, 0D, state.getValue(NORTH) ? 0D : min,
                state.getValue(EAST) ? 1D : max, 0.5D, state.getValue(SOUTH) ? 1D : max);
    }

    private static @Nullable ICrucibleAcceptor acceptor(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acc ? acc : null;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc != null && acc.canAcceptPartialPour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc == null ? stack : acc.pour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc != null && acc.canAcceptPartialFlow(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc == null ? stack : acc.flow(level, pos, side, stack);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(!stack.is(ItemTags.SHOVELS)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof FoundryChannelBlockEntity channel)) return ItemInteractionResult.FAIL;

        if(channel.amount > 0 && channel.type != null) {

            ItemStack scrap = ScrapsItem.create(new MaterialStack(channel.type, channel.amount));

            if(player.getInventory().add(scrap)) {
                player.inventoryMenu.broadcastChanges();
            } else {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, scrap);
            }

            channel.amount = 0;
            channel.type = null;
            channel.setChanged();
        }

        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FoundryChannelBlockEntity channel) {

            if(channel.amount > 0 && channel.type != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ScrapsItem.create(new MaterialStack(channel.type, channel.amount)));
                channel.amount = 0;
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
