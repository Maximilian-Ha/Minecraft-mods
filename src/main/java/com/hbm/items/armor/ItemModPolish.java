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
 * Portiert aus 1.7.10: ItemModPolish.
 *
 * Die Politur. Jeder zwanzigste Treffer prallt ab.
 */
public class ItemModPolish extends ItemArmorMod {

    public ItemModPolish(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.polish").withStyle(ChatFormatting.BLUE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.polish")).append(Component.literal(")"))
                .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {

        LivingEntity traeger = event.getEntity();
        if(traeger.level().isClientSide) return;

        /* Einer von zwanzig -- das Original schreibt nextInt(20) == 0, nicht fuenf Prozent. */
        if(traeger.getRandom().nextInt(20) == 0) event.setNewDamage(0F);
    }
}
