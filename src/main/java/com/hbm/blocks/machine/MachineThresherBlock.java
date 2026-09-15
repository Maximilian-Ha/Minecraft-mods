package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineThresherBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
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
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineThresher.
 *
 * Einzelner Block ohne Mehrblockstruktur -- das Original erbt von BlockContainer,
 * nicht von BlockDummyable, deshalb steht hier BaseEntityBlock und nicht
 * DummyableBlock. Die Blickrichtung aus onBlockPlacedBy wird zur FACING-Eigenschaft.
 */
public class MachineThresherBlock extends BaseEntityBlock implements ILookOverlay, ITooltipProvider, IToolable {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<MachineThresherBlock> CODEC = simpleCodec(MachineThresherBlock::new);

    public MachineThresherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<MachineThresherBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Das Original leitet die Metadaten aus dem Gierwinkel ab und erhaelt so genau
     * die Richtung, in die der Spieler schaut.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineThresherBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /** Das Original zeichnet den Block ausschliesslich ueber den TESR (getRenderType() == -1). */
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!(stack.getItem() instanceof IItemFluidIdentifier identifier)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity te = level.getBlockEntity(pos);
        if(!(te instanceof MachineThresherBlockEntity thresher)) return ItemInteractionResult.FAIL;

        FluidType type = identifier.getType(level, pos, stack);

        if(MachineThresherBlockEntity.acceptedFuels.contains(type)) {
            thresher.tank.setTankType(type);
            thresher.setChanged();
            player.displayClientMessage(
                    Component.literal("Changed type to ").append(type.getName()).append(Component.literal("!")).withStyle(ChatFormatting.YELLOW),
                    false);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {
        if(tool != ToolType.SCREWDRIVER) return false;

        BlockEntity te = level.getBlockEntity(pos);
        if(!(te instanceof MachineThresherBlockEntity thresher)) return false;

        thresher.isSuspended = !thresher.isSuspended;
        thresher.setChanged();

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockEntity te = level.getBlockEntity(pos);
        if(!(te instanceof MachineThresherBlockEntity thresher)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.empty().append(thresher.tank.getTankType().getName())
                .append(Component.literal(": " + thresher.tank.getFill() + "/" + thresher.tank.getMaxFill() + "mB")));

        if(thresher.isSuspended) {
            text.add(Component.literal("! ").append(Component.translatable(this.getDescriptionId() + ".suspended")).append(Component.literal(" !")).withStyle(ChatFormatting.RED));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
