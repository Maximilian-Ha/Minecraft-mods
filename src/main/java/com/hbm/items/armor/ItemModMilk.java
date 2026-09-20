package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModMilk.
 *
 * Die Spinnenmilch. Sie nimmt dem Traeger jeden schaedlichen Trankeffekt wieder ab, Tick
 * fuer Tick.
 *
 * WAS "SCHAEDLICH" HEISST: das Original fragt HbmPotion.getIsBadEffect, das seinerseits
 * Potion.isBadEffect abfragt. In 1.21 steht dieselbe Auskunft in MobEffectCategory.HARMFUL
 * -- auch die Effekte dieser Mod sind so angelegt (siehe ModEffect).
 */
public class ItemModMilk extends ItemArmorMod {

    public ItemModMilk(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.milk").withStyle(ChatFormatting.WHITE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.milk")).append(Component.literal(")"))
                .withStyle(ChatFormatting.WHITE));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {

        if(entity.level().isClientSide) return;

        /* Erst sammeln, dann entfernen: wer waehrend des Durchlaufens entfernt, bekommt
         * eine ConcurrentModificationException. Das Original macht es genauso. */
        List<Holder<MobEffect>> schaedlich = new ArrayList<>();

        for(MobEffectInstance wirkung : entity.getActiveEffects()) {
            if(wirkung.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                schaedlich.add(wirkung.getEffect());
            }
        }

        for(Holder<MobEffect> wirkung : schaedlich) entity.removeEffect(wirkung);
    }
}
