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
 * Portiert aus 1.7.10: ItemModIron.
 *
 * Eine Auskleidung aus Eisen. Sie haelt keine Strahlung ab, macht den Traeger aber schwerer:
 * jedes so ausgekleidete Teil gibt einen halben Punkt Rueckstossfestigkeit.
 */
public class ItemModKnockback extends ItemArmorMod {

    private final double knockbackResistance;

    public ItemModKnockback(Properties properties, double knockbackResistance) {
        super(properties.stacksTo(1), ArmorModHandler.CLADDING, true, true, true, true);
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.knockback", this.knockbackResistance).withStyle(ChatFormatting.WHITE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.knockback", this.knockbackResistance)).append(Component.literal(")"))
                .withStyle(ChatFormatting.WHITE));
    }

    @Override
    public void addAttributes(ItemStack armor, Map<Holder<Attribute>, Double> out) {
        out.merge(Attributes.KNOCKBACK_RESISTANCE, this.knockbackResistance, Double::sum);
    }
}
