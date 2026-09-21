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
 * Portiert aus 1.7.10: com.hbm.items.armor.ItemModCloud.
 *
 * Die Wolkenflasche -- eines der Beutestuecke des Maskenmanns und ein Ruestungsmodul fuer die
 * Brustplatte. Sie macht den Traeger um ein Achtel schneller.
 *
 * DER SPRUNG NACH VORN FEHLT NOCH. Im Original ist die Flasche ausserdem ein IArmorModDash
 * und gibt drei waagerechte Spruenge; der Port hat die Taste dafuer (HbmKeybinds DASH), aber
 * niemanden, der sie auswertet -- das ganze Dash-System des Originals steht in
 * EntityEffectHandler:716 und ist nicht uebernommen. Was hier steht, ist der Temposchub; der
 * Sprung wartet auf das System, nicht auf dieses Modul.
 */
public class ItemModCloud extends ItemArmorMod {

    /** Das Original: AttributeModifier(0.125, Operation 2) -- ein Achtel mehr, multiplikativ. */
    private static final double TEMPO = 0.125D;

    public ItemModCloud(Properties properties) {
        super(properties.stacksTo(1), ArmorModHandler.PLATE_ONLY, false, true, false, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.cloud").withStyle(ChatFormatting.WHITE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.cloud")).append(Component.literal(")"))
                .withStyle(ChatFormatting.RED));
    }

    @Override
    public void addAttributes(ItemStack armor, Map<Holder<Attribute>, Double> out) {
        out.merge(Attributes.MOVEMENT_SPEED, TEMPO, Double::sum);
    }
}
