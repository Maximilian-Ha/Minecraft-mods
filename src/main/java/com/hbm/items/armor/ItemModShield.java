package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Portiert aus 1.7.10: ItemModShield.
 *
 * Ein Aufsatz fuer den Kevlar-Steckplatz der Brustplatte, der die Obergrenze des Schilds
 * anhebt. Er tut selbst nichts -- HbmPlayerAttachments.getEffectiveMaxShield fragt ihn ab.
 *
 * DER BLINKENDE HINWEISTEXT IST DER DES ORIGINALS: es waehlt die Farbe nach
 * System.currentTimeMillis() % 1000, also halbsekuendlich zwischen Gelb und Dunkelgelb.
 * Der Hinweistext wird bei jedem Bild neu gebaut, das blinkt also wirklich.
 */
public class ItemModShield extends ItemArmorMod {

    /** Um wie viel dieser Aufsatz die Obergrenze anhebt. */
    public final float schild;

    public ItemModShield(Properties properties, float schild) {
        super(properties, ArmorModHandler.KEVLAR, false, true, false, false);
        this.schild = schild;
    }

    /** Gelb oder Dunkelgelb, halbsekuendlich wechselnd. Aus dem Original uebernommen. */
    private static ChatFormatting takt() {
        return System.currentTimeMillis() % 1000 < 500 ? ChatFormatting.YELLOW : ChatFormatting.GOLD;
    }

    /** Eine Nachkommastelle, gerundet -- Math.round(schild * 10) * 0.1 im Original. */
    private String betrag() {
        return String.valueOf(Math.round(this.schild * 10) * 0.1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("+" + this.betrag() + " shield").withStyle(takt()));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (+" + this.betrag() + " health)"))
                .withStyle(takt()));
    }
}
