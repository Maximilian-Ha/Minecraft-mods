package com.hbm.blocks.network;

import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Kleiner Mast: red_pylon (Holz) und red_pylon_steel (Stahl) */
public class PylonRedWireBlock extends PylonBaseBlock {

    public PylonRedWireBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new PylonBlockEntity(pos, state) : null;
    }

    public static final MapCodec<PylonRedWireBlock> CODEC = simpleCodec(PylonRedWireBlock::new);
    @Override protected MapCodec<PylonRedWireBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {4, 0, 0, 0, 0, 0}; }
    @Override public int getOffset() { return 0; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Connection Type: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Single").withStyle(ChatFormatting.YELLOW)));
        components.add(Component.literal("Connection Range: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("25m").withStyle(ChatFormatting.YELLOW)));
    }
}
