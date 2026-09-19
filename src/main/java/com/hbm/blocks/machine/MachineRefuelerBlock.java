package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.RefuelerBlockEntity;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockRefueler.
 *
 * Die Saeule steht an der Wand hinter sich -- vier Pixel tief, ueber die ganze Breite und Hoehe.
 * Mit einem Fluidkennzeichner in der Hand laesst sich einstellen, was sie zapft.
 */
public class MachineRefuelerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<MachineRefuelerBlock> CODEC = simpleCodec(MachineRefuelerBlock::new);

    /** Vier Pixel an der Wand gegenueber der Vorderseite. Masse aus dem Original. */
    private static final Map<Direction, VoxelShape> SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.NORTH, Block.box(0, 0, 12, 16, 16, 16));
        map.put(Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 4));
        map.put(Direction.WEST, Block.box(12, 0, 0, 16, 16, 16));
        map.put(Direction.EAST, Block.box(0, 0, 0, 4, 16, 16));
    });

    public MachineRefuelerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<MachineRefuelerBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefuelerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /** Gezeichnet wird sie vom Darsteller, nicht von einem Modell. */
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(!(stack.getItem() instanceof IItemFluidIdentifier kennzeichner)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof RefuelerBlockEntity saeule)) return ItemInteractionResult.FAIL;

        FluidType typ = kennzeichner.getType(level, pos, stack);
        saeule.tank.setTankType(typ);
        saeule.setChanged();

        player.displayClientMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                .append(typ.getName())
                .append(Component.literal("!")), false);

        return ItemInteractionResult.CONSUME;
    }
}
