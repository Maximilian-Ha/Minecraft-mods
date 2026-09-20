package com.hbm.items.armor;

import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModQuartz.
 *
 * Der Plutoniumquarz. Jeder Treffer nimmt dem Traeger zehn RAD ab -- der Schaden selbst
 * bleibt.
 */
public class ItemModQuartz extends ItemArmorMod {

    /** Wie viel Strahlung ein Treffer abtraegt. */
    public static final float ABTRAG = 10F;

    public ItemModQuartz(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.quartz").withStyle(ChatFormatting.DARK_GRAY));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.quartz")).append(Component.literal(")"))
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {

        LivingEntity traeger = event.getEntity();
        if(traeger.level().isClientSide) return;

        float strahlung = HbmLivingAttachments.getRadiation(traeger);
        HbmLivingAttachments.setRadiation(traeger, Math.max(strahlung - ABTRAG, 0));
    }
}
