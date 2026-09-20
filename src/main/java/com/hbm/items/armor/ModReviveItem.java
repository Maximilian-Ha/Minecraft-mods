package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ItemModRevive.
 *
 * Wild P -- "Explosive Reactive Plot Armor". Ein Aufsatz mit Haltbarkeit, und die Haltbarkeit
 * ist die Zahl der Leben: wer mit ihm stirbt, steht mit voller Gesundheit und fuenf Sekunden
 * Resistenz wieder auf, und der Aufsatz verliert einen Punkt. Beim letzten wird er entfernt.
 *
 * DIE WIRKUNG STEHT NICHT HIER, sondern dort, wo das Sterben abgefangen wird -- im Original
 * ModEventHandler.onEntityDeathFirst, im Port CommonEvents.onLivingDeath. Diese Klasse ist
 * nur das Kennzeichen, an dem der Ereignisbehandler sie erkennt; genau so ist sie auch im
 * Original geschrieben.
 *
 * ER SITZT IM SONDERPLATZ und passt nur auf die BEINSCHIENE. Das ist keine Willkuer: das
 * Original schreibt super(extra, false, false, true, false).
 */
public class ModReviveItem extends ItemArmorMod {

    public ModReviveItem(Properties properties, int leben) {
        super(properties.stacksTo(1).durability(leben), ArmorModHandler.EXTRA, false, false, true, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.literal("Explosive ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Reactive ").withStyle(ChatFormatting.RED))
                .append(Component.literal("Plot ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal("Armor").withStyle(ChatFormatting.RED)));
        components.add(Component.empty());
        components.add(Component.literal(verbleibend(stack) + " revives left").withStyle(ChatFormatting.GOLD));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, tooltipFlag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getDisplayName())
                .append(Component.literal(" (" + verbleibend(stack) + " revives left)")).withStyle(ChatFormatting.GOLD));
    }

    public static int verbleibend(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }
}
