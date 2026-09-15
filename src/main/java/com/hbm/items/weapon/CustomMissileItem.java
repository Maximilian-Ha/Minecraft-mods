package com.hbm.items.weapon;

import com.hbm.handler.MissileStruct;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.CustomMissilePartItem.FuelType;
import com.hbm.items.weapon.CustomMissilePartItem.WarheadType;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.ItemCustomMissile.
 *
 * Die fertig montierte Rakete als Gegenstand. Sie traegt ihre fuenf Bauteile im Datenanhang --
 * Chip, Sprengkopf, Rumpf, Leitwerk und Triebwerk -- und rechnet daraus vor, was sie kann:
 * Sprengkraft, Reichweite, Streuung und Haltbarkeit.
 *
 * DAS LEITWERK DARF FEHLEN. Ohne eines betraegt die Streuung hundert Prozent; das ist im Original
 * genauso und macht ein Leitwerk zur ersten Aufwertung, die sich lohnt.
 *
 * ABWEICHUNG: das Original legt die Zahlenkennungen der Gegenstaende ab. Auf 1.21 sind die nicht
 * mehr stabil; hier stehen die Registriernamen in den Zusatzdaten.
 */
public class CustomMissileItem extends Item {

    public static final String KEY_CHIP = "chip";
    public static final String KEY_WARHEAD = "warhead";
    public static final String KEY_FUSELAGE = "fuselage";
    public static final String KEY_STABILITY = "stability";
    public static final String KEY_THRUSTER = "thruster";

    public CustomMissileItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack buildMissile(ItemStack chip, ItemStack warhead, ItemStack fuselage, ItemStack stability, ItemStack thruster) {

        ItemStack missile = new ItemStack(NtmItems.MISSILE_CUSTOM.get());
        CompoundTag tag = new CompoundTag();

        put(tag, KEY_CHIP, chip);
        put(tag, KEY_WARHEAD, warhead);
        put(tag, KEY_FUSELAGE, fuselage);
        put(tag, KEY_THRUSTER, thruster);
        put(tag, KEY_STABILITY, stability);

        TagsUtil.putCustomData(missile, tag);

        return missile;
    }

    private static void put(CompoundTag tag, String key, ItemStack stack) {
        if(stack == null || stack.isEmpty()) return;
        tag.putString(key, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    /** Das Bauteil hinter einem Schluessel -- null, wenn keines abgelegt ist. */
    public static CustomMissilePartItem getPart(ItemStack stack, String key) {

        if(!TagsUtil.hasCustomData(stack)) return null;

        String name = TagsUtil.getCustomData(stack).getString(key);
        if(name.isEmpty()) return null;

        ResourceLocation id = ResourceLocation.tryParse(name);
        if(id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;

        return BuiltInRegistries.ITEM.get(id) instanceof CustomMissilePartItem part ? part : null;
    }

    /** Der Bauplan zum Abschuss. */
    public static MissileStruct getStruct(ItemStack stack) {

        if(stack == null || !(stack.getItem() instanceof CustomMissileItem)) return null;

        return new MissileStruct(
                getPart(stack, KEY_WARHEAD),
                getPart(stack, KEY_FUSELAGE),
                getPart(stack, KEY_STABILITY),
                getPart(stack, KEY_THRUSTER));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        CustomMissilePartItem chip = getPart(stack, KEY_CHIP);
        CustomMissilePartItem warhead = getPart(stack, KEY_WARHEAD);
        CustomMissilePartItem fuselage = getPart(stack, KEY_FUSELAGE);
        CustomMissilePartItem stability = getPart(stack, KEY_STABILITY);
        CustomMissilePartItem thruster = getPart(stack, KEY_THRUSTER);

        if(warhead == null || fuselage == null || thruster == null || chip == null) {
            components.add(Component.translatable("error.generic").withStyle(ChatFormatting.RED));
            return;
        }

        components.add(label("item.missile.desc.warhead").append(CustomMissilePartItem.getWarhead((WarheadType) warhead.attributes[0])));
        line(components, "item.missile.desc.strength", String.valueOf((Float) warhead.attributes[1]));

        components.add(label("item.missile.desc.fuelType").append(CustomMissilePartItem.getFuel((FuelType) fuselage.attributes[0])));
        line(components, "item.missile.desc.fuelAmount", fuselage.attributes[1] + "l");

        line(components, "item.missile.desc.chipInaccuracy", ((Float) chip.attributes[0]) * 100F + "%");
        line(components, "item.missile.desc.finInaccuracy", stability != null ? ((Float) stability.attributes[0]) * 100F + "%" : "100%");

        components.add(label("item.missile.desc.size")
                .append(CustomMissilePartItem.getSize(fuselage.top))
                .append(Component.literal("/").withStyle(ChatFormatting.GRAY))
                .append(CustomMissilePartItem.getSize(fuselage.bottom)));

        float health = warhead.health + fuselage.health + thruster.health + (stability != null ? stability.health : 0F);
        line(components, "item.missile.desc.health", health + "HP");
    }

    /* MutableComponent, nicht Component: append() gibt es nur auf der veraenderlichen Fassung,
     * und jede Zeile haengt hier noch etwas an. */
    private static MutableComponent label(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.BOLD).append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
    }

    private static void line(List<Component> components, String key, String value) {
        components.add(label(key).append(Component.literal(value).withStyle(ChatFormatting.GRAY)));
    }

    /** Dieselbe Zeile, wenn der Wert schon ein fertiger Textbaustein ist (Groesse, Treibstoff). */
    private static void line(List<Component> components, String key, Component value) {
        components.add(label(key).append(value));
    }
}
