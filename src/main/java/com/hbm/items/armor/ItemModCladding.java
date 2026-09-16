package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModCladding.
 *
 * Eine Auskleidung im Verkleidungsplatz. Ihr Wert wird von HazmatRegistry.getCladding
 * gelesen und schlaegt dort auf die Strahlungsminderung des Teils auf.
 */
public class ItemModCladding extends ItemArmorMod {

    public final double rad;

    public ItemModCladding(Properties properties, double rad) {
        super(properties.stacksTo(1), ArmorModHandler.CLADDING, true, true, true, true);
        this.rad = rad;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("armorMod.radRes", this.rad).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.empty());
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.radRes", this.rad)).append(Component.literal(")"))
                .withStyle(ChatFormatting.YELLOW));
    }
}
