package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModSerum.
 *
 * Das Serum. Es tauscht Gift gegen Staerke: hundert Ticks Staerke der Stufe fuenf, jedes
 * Mal, wenn der Traeger vergiftet ist.
 */
public class ItemModSerum extends ItemArmorMod {

    public ItemModSerum(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.serum").withStyle(ChatFormatting.GREEN));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.serum")).append(Component.literal(")"))
                .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {

        if(entity.level().isClientSide) return;

        if(entity.hasEffect(MobEffects.POISON)) {
            entity.removeEffect(MobEffects.POISON);
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 4));
        }
    }
}
