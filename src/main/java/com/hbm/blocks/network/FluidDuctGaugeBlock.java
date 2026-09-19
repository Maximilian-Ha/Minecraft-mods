package com.hbm.blocks.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.network.PipeGaugeBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.INBTBlockTransformable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.FluidDuctGauge.
 *
 * Voller Block aus Stahldeko statt Rohrform -- wie im Original. Auf der beim Setzen
 * gewaehlten Seite sitzt das Zeigerinstrument, die uebrigen fuenf tragen das
 * einfaerbbare Overlay und nehmen so die Farbe des eingestellten Fluids an.
 * Im Original waren das zwei Renderdurchgaenge; hier ist es eine zweite
 * Modellschicht mit tintIndex 1, die derselbe Farbgeber einfaerbt wie das Rohr.
 */
public class FluidDuctGaugeBlock extends FluidDuctBaseBlock implements ILookOverlay, ITooltipProvider, INBTBlockTransformable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<FluidDuctGaugeBlock> CODEC = simpleCodec(FluidDuctGaugeBlock::new);

    public FluidDuctGaugeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public MapCodec<FluidDuctGaugeBlock> codec() { return CODEC; }

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
        return new PipeGaugeBlockEntity(pos, state);
    }

    @Override
    public BlockState transformState(BlockState state, Rotation rotation) {
        return INBTBlockTransformable.transformFacingState(state, rotation);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof PipeGaugeBlockEntity gauge)) return;

        List<Component> text = new ArrayList<>();
        FluidType type = gauge.getFluidType();
        text.add(Component.translatable(type.getUnlocalizedName()).withColor(type.getColor()));
        text.add(Component.literal(String.format(Locale.US, "%,d", gauge.deltaTick) + " mB/t"));
        text.add(Component.literal(String.format(Locale.US, "%,d", gauge.deltaLastSecond) + " mB/s"));

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
