package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModTwoKick -- der ballistische Handschuh.
 *
 * ER TUT NICHTS, und das ist kein Versehen des Ports. Die Klasse des Originals hat keinen
 * einzigen Haken: sie setzt den Steckplatz auf servos, schreibt zwei Zeilen in den
 * Hinweistext ("Punches fire 12 gauge shells") und das war es. Der Name ballistic_gauntlet
 * kommt im ganzen Original an genau zwei Stellen vor -- in seiner eigenen Anmeldung und in
 * dieser Klasse. Niemand fragt ihn ab; der Schrotschuss beim Faustschlag ist im Original
 * nie gebaut worden.
 *
 * Der Port uebernimmt das unveraendert, samt Hinweistext. Wer den Schuss will, muss ihn
 * erst erfinden -- und das waere kein Portieren mehr.
 */
public class ItemModTwoKick extends ItemArmorMod {

    public ItemModTwoKick(Properties properties) {
        super(properties, ArmorModHandler.SERVOS, false, true, false, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.twokick.quote").withStyle(ChatFormatting.ITALIC));
        components.add(Component.translatable("armorMod.twokick").withStyle(ChatFormatting.YELLOW));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.twokick.short")).append(Component.literal(")"))
                .withStyle(ChatFormatting.YELLOW));
    }
}
