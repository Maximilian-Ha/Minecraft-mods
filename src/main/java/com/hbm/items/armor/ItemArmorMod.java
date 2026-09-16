package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.Map;

public class ItemArmorMod extends Item {

    public final int type;
    public final boolean helmet;
    public final boolean chestplate;
    public final boolean leggings;
    public final boolean boots;

    public ItemArmorMod(Properties properties, int type, boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
        super(properties.stacksTo(1));
        this.type = type;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.translatable("armorMod.applicableTo").withStyle(ChatFormatting.DARK_PURPLE));

        if (helmet && chestplate && leggings && boots) {
            components.add(Component.translatable("armorMod.all").withStyle(ChatFormatting.GRAY));
        } else {
            if (helmet) components.add(Component.translatable("armorMod.helmets").withStyle(ChatFormatting.GRAY));
            if (chestplate) components.add(Component.translatable("armorMod.chestplates").withStyle(ChatFormatting.GRAY));
            if (leggings) components.add(Component.translatable("armorMod.leggings").withStyle(ChatFormatting.GRAY));
            if (boots) components.add(Component.translatable("armorMod.boots").withStyle(ChatFormatting.GRAY));
        }
        components.add(Component.translatable("armorMod.slot").withStyle(ChatFormatting.DARK_PURPLE));

        switch (this.type) {
            case ArmorModHandler.HELMET_ONLY -> components.add(Component.translatable("armorMod.type.helmet").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.PLATE_ONLY -> components.add(Component.translatable("armorMod.type.chestplate").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.LEGS_ONLY -> components.add(Component.translatable("armorMod.type.leggings").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.BOOTS_ONLY -> components.add(Component.translatable("armorMod.type.boots").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.SERVOS -> components.add(Component.translatable("armorMod.type.servo").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.CLADDING -> components.add(Component.translatable("armorMod.type.cladding").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.KEVLAR -> components.add(Component.translatable("armorMod.type.insert").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.EXTRA -> components.add(Component.translatable("armorMod.type.special").withStyle(ChatFormatting.GRAY));
            case ArmorModHandler.BATTERY -> components.add(Component.translatable("armorMod.type.battery").withStyle(ChatFormatting.GRAY));
        }
    }

    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(stack.getDisplayName());
    }

    public void modUpdate(LivingEntity entity, ItemStack armor) { }

    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) { }

    /**
     * Was das Modul dem Traeger an Eigenschaften gibt, solange das Teil angelegt ist.
     *
     * Das Original haengt dafuer eine Multimap an das Ruestungsteil und laesst Forge die
     * Werte beim Anlegen uebernehmen. In 1.21 stehen die Eigenschaften eines Gegenstands in
     * einer Datenkomponente fest und lassen sich nicht je nach NBT aendern, deshalb sammelt
     * ArmorModHandler.updateAttributes die Werte hier ein und setzt sie am Traeger selbst.
     *
     * Der Wert ist ein Summand: 0.5 heisst "plus ein halber Punkt".
     */
    public void addAttributes(ItemStack armor, Map<Holder<Attribute>, Double> out) { }
}
