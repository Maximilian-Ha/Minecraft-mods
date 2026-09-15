package com.hbm.blocks.network;

import com.hbm.blockentity.network.PylonMediumBlockEntity;
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

/**
 * Mittlerer Mast, vier Bloecke nutzen diese Klasse:
 * red_pylon_medium_wood, red_pylon_medium_wood_transformer,
 * red_pylon_medium_steel, red_pylon_medium_steel_transformer
 */
public class PylonMediumBlock extends PylonBaseBlock {

    public PylonMediumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new PylonMediumBlockEntity(pos, state) : null;
    }

    public static final MapCodec<PylonMediumBlock> CODEC = simpleCodec(PylonMediumBlock::new);
    @Override protected MapCodec<PylonMediumBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {6, 0, 0, 0, 0, 0}; }
    @Override public int getOffset() { return 0; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Connection Type: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Triple").withStyle(ChatFormatting.YELLOW)));
        components.add(Component.literal("Connection Range: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("45m").withStyle(ChatFormatting.YELLOW)));
    }
}
