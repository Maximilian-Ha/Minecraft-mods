package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ItemModMorningGlory.
 *
 * Der Aufsatz der Morgenroete. Zwei Wirkungen, beide klein und beide staendig da: jeder
 * zwanzigste Treffer gibt fuenf Sekunden Resistenz V, und Verfall haelt sich auf dem
 * Traeger keine Sekunde.
 *
 * ER PASST AUF ALLE VIER TEILE und sitzt im Sonderplatz. So steht es im Original.
 */
public class ModMorningGloryItem extends ItemArmorMod {

    public ModMorningGloryItem(Properties properties) {
        super(properties.stacksTo(1), ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.literal("5% chance to apply resistance when hit, wither immunity").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, tooltipFlag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getDisplayName())
                .append(Component.literal(" (5% for resistance, wither immunity)")).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {
        LivingEntity traeger = event.getEntity();
        if(traeger.level().isClientSide) return;
        /* Einer von zwanzig -- nicht fuenf Prozent gerundet, sondern genau nextInt(20) == 0. */
        if(traeger.getRandom().nextInt(20) != 0) return;
        traeger.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if(entity.level().isClientSide) return;
        if(entity.hasEffect(MobEffects.WITHER)) entity.removeEffect(MobEffects.WITHER);
    }
}
