package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModObsidian.
 *
 * Eine Auskleidung aus Obsidian. Sie tut nichts, solange das Teil getragen wird -- aber ein
 * so ausgekleidetes Ruestungsteil, das am Boden liegt, verbrennt und zerfaellt nicht mehr.
 * Ausgewertet wird das beim Fallenlassen, siehe CommonEvents.onItemDropped.
 */
public class ItemModIndestructible extends ItemArmorMod {

    public ItemModIndestructible(Properties properties) {
        super(properties.stacksTo(1), ArmorModHandler.CLADDING, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.indestructible").withStyle(ChatFormatting.DARK_PURPLE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.indestructible")).append(Component.literal(")"))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
