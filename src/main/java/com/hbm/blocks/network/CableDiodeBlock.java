package com.hbm.blocks.network;

import api.hbm.energymk2.IEnergyConnectorBlock;
import com.hbm.blockentity.IScreenProvider;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.network.DiodeBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.screens.DiodeScreen;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CableDiode.
 *
 * Kein Multiblock -- im Original ein schlichter BlockContainer. Die Metadaten aus
 * BlockPistonBase.determineOrientation sind hier die Blockstate-Eigenschaft FACING;
 * die Diode gibt Strom in die Gegenrichtung von FACING ab.
 */
public class CableDiodeBlock extends Block implements EntityBlock, IEnergyConnectorBlock, IScreenProvider, ILookOverlay, ITooltipProvider {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<CableDiodeBlock> CODEC = simpleCodec(CableDiodeBlock::new);

    public CableDiodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<CableDiodeBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, NetworkPlacement.determineOrientation(context));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiodeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(!player.isShiftKeyDown()) {
            NuclearTechMod.proxy.openScreen(player, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object provideScreen(Player player, BlockPos pos) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if(be instanceof DiodeBlockEntity diode) return new DiodeScreen(diode);
        return null;
    }

    @Override
    public boolean canConnect(BlockGetter level, BlockPos pos, Direction dir) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Limits throughput and restricts flow direction").withStyle(ChatFormatting.GOLD));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof DiodeBlockEntity diode)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal("Max.: " + BobMathUtil.getShortNumber(diode.getMaxPower()) + "HE/t"));
        text.add(Component.literal("Priority: " + diode.priority.name()));

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
