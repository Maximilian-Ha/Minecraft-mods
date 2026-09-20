package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: ItemModHealth.
 *
 * Herzcontainer und schwarzer Diamant. Beide geben dem Traeger mehr Lebensenergie, solange
 * die Brustplatte sitzt -- zwanzig Punkte der eine, vierzig der andere.
 *
 * NUR IN DIE BRUSTPLATTE: das Original erlaubt sie nur dort (false, true, false, false).
 * Vier davon nebeneinander gaebe es also nicht.
 *
 * NICHT UEBERNOMMEN: die blinkende Farbe im Hinweistext. Das Original faerbt die Zeile je
 * nach Systemzeit abwechselnd rot und violett; das ist ein Gag, der in 1.21 an einer
 * anderen Stelle stuende (der Hinweistext wird nicht mehr jeden Frame neu gebaut). Die
 * Zeile bleibt darum durchgehend rot.
 */
public class ItemModHealth extends ItemArmorMod {

    private final double leben;

    public ItemModHealth(Properties properties, double leben) {
        super(properties, ArmorModHandler.EXTRA, false, true, false, false);
        this.leben = leben;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.health", this.leben).withStyle(ChatFormatting.RED));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.health", this.leben)).append(Component.literal(")"))
                .withStyle(ChatFormatting.RED));
    }

    @Override
    public void addAttributes(ItemStack armor, Map<Holder<Attribute>, Double> out) {
        out.merge(Attributes.MAX_HEALTH, this.leben, Double::sum);
    }
}
