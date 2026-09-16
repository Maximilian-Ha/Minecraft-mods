package com.hbm.items.special;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.NtmItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemRag.
 *
 * Ein Lappen. Liegt er im Wasser, saugt er sich voll; klickt man ihn an, pinkelt
 * der Spieler darauf. Beides zusammen ist die Armeleuteversion einer Gasmaske.
 */
public class RagItem extends Item {

    public RagItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {

        if(entity.level().isClientSide) return false;
        if(!entity.level().getFluidState(entity.blockPosition()).is(FluidTags.WATER)) return false;

        entity.setItem(new ItemStack(NtmItems.RAG_DAMP.get(), stack.getCount()));
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            ItemStack pissed = new ItemStack(NtmItems.RAG_PISS.get());
            stack.shrink(1);
            if(!player.getInventory().add(pissed)) player.drop(pissed, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        String[] lines = ITooltipProvider.getDescriptionOrNull(stack);
        if(lines != null) for(String line : lines) components.add(Component.translatable(line).withStyle(ChatFormatting.GRAY));
    }
}
