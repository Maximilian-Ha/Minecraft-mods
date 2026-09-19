package com.hbm.blocks.network;

import com.hbm.blockentity.network.PipeCounterValveBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.FluidCounterValve.
 *
 * Derselbe Hahn, aber mit Zaehlwerk: beim Hinsehen steht neben dem Fluid auch, wie viel
 * seit dem letzten Zuruecksetzen durchgeflossen ist.
 */
public class FluidCounterValveBlock extends FluidValveBlock {

    public static final MapCodec<FluidCounterValveBlock> CODEC = simpleCodec(FluidCounterValveBlock::new);

    public FluidCounterValveBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<FluidCounterValveBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeCounterValveBlockEntity(pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof PipeCounterValveBlockEntity ventil)) return;

        List<Component> text = new ArrayList<>();
        FluidType type = ventil.getFluidType();
        text.add(Component.translatable(type.getUnlocalizedName()).withColor(type.getColor()));
        text.add(Component.literal(String.format(Locale.US, "%,d", ventil.getCounter()) + " mB"));

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
