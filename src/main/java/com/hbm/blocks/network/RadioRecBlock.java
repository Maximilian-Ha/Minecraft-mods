package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.IScreenProvider;
import com.hbm.blockentity.network.RadioRecBlockEntity;
import com.hbm.blocks.INBTBlockTransformable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.screens.RadioRecScreen;
import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.Rotation;
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

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.RadioRec.
 *
 * Ein Funkempfaenger mit Kanaleingabe. Das Original zeichnet ihn mit einem Darsteller aus
 * vier Techne-Kaesten -- alle achsenparallel, ohne Drehung. Solche Kaesten lassen sich in
 * einem Blockmodell ausdruecken, deshalb braucht er hier keinen eigenen Darsteller.
 *
 * Er blickt in die Richtung des Setzenden, nicht von ihm weg: das Original legt bei Blick
 * nach Sueden die Metadaten auf Sued.
 */
public class RadioRecBlock extends BaseEntityBlock implements IScreenProvider, ITooltipProvider, INBTBlockTransformable {

    public static final MapCodec<RadioRecBlock> CODEC = simpleCodec(RadioRecBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Masse aus setBlockBoundsBasedOnState des Originals. */
    private static final VoxelShape SHAPE_NS = Block.box(1, 0, 4, 15, 10, 12);
    private static final VoxelShape SHAPE_WE = Block.box(4, 0, 1, 12, 10, 15);

    public RadioRecBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<RadioRecBlock> codec() { return CODEC; }

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
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_WE;
    }

    @Override
    public BlockState transformState(BlockState state, Rotation rotation) {
        return INBTBlockTransformable.transformFacingState(state, rotation);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(player.isShiftKeyDown()) return InteractionResult.PASS;
        NuclearTechMod.proxy.openScreen(player, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Object provideScreen(Player player, BlockPos pos) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if(be instanceof RadioRecBlockEntity radio) return new RadioRecScreen(radio);
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioRecBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
