package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModBandaid.
 *
 * Das Pflaster. Drei von hundert Treffern heilen den Traeger statt ihn zu verletzen --
 * und zwar ganz.
 */
public class ItemModBandaid extends ItemArmorMod {

    public ItemModBandaid(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.bandaid").withStyle(ChatFormatting.RED));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.bandaid")).append(Component.literal(")"))
                .withStyle(ChatFormatting.RED));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {

        LivingEntity traeger = event.getEntity();
        if(traeger.level().isClientSide) return;

        if(traeger.getRandom().nextInt(100) < 3) {
            event.setNewDamage(0F);
            traeger.heal(traeger.getMaxHealth());
        }
    }
}
