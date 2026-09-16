package com.hbm.items.armor;

import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.handler.ArmorModHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: ItemModInsert.
 *
 * Eine Einlage im Kevlarplatz der Brustplatte. Sie nimmt einen Teil des Schadens auf und
 * geht dabei selbst kaputt; schwere Einlagen bremsen ihren Traeger.
 *
 * Die vier Faktoren sind Multiplikatoren auf den ankommenden Schaden: 1.0 heisst
 * unveraendert, 0.9 heisst zehn Prozent weniger. Sie werden nacheinander angewandt, eine
 * Einlage gegen Geschosse wirkt also zusaetzlich zur allgemeinen Minderung.
 */
public class ItemModInsert extends ItemArmorMod {

    private final float damageMod;
    private final float projectileMod;
    private final float explosionMod;
    private final float speed;
    /**
     * Strahlung je Tick, die die Einlage selbst abgibt -- nur die Poloniumplatte hat welche.
     * Der Hinweis im Original sagt "RAD/s", rechnet aber je Tick; der Wert ist unveraendert
     * uebernommen, der Text ebenfalls.
     */
    private final float radPerTick;
    /** Reaktivpanzerung: geht bei jedem Treffer mit einem kleinen Knall los. */
    private final boolean reactive;

    public ItemModInsert(Properties properties, int durability, float damageMod, float projectileMod, float explosionMod, float speed, float radPerTick, boolean reactive) {
        super(properties.stacksTo(1).durability(durability), ArmorModHandler.KEVLAR, false, true, false, false);
        this.damageMod = damageMod;
        this.projectileMod = projectileMod;
        this.explosionMod = explosionMod;
        this.speed = speed;
        this.radPerTick = radPerTick;
        this.reactive = reactive;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if(this.damageMod != 1F) components.add(percent("armorMod.insert.damage", this.damageMod).withStyle(ChatFormatting.RED));
        if(this.projectileMod != 1F) components.add(percent("armorMod.insert.projectile", this.projectileMod).withStyle(ChatFormatting.YELLOW));
        if(this.explosionMod != 1F) components.add(percent("armorMod.insert.explosion", this.explosionMod).withStyle(ChatFormatting.YELLOW));
        if(this.speed != 1F) components.add(percent("armorMod.insert.speed", this.speed).withStyle(ChatFormatting.BLUE));
        if(this.radPerTick > 0F) components.add(Component.translatable("armorMod.insert.rad", (int) this.radPerTick).withStyle(ChatFormatting.DARK_RED));

        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {

        List<String> parts = new ArrayList<>();

        if(this.damageMod != 1F) parts.add(signedPercent(this.damageMod) + " dmg");
        if(this.projectileMod != 1F) parts.add(signedPercent(this.projectileMod) + " proj");
        if(this.explosionMod != 1F) parts.add(signedPercent(this.explosionMod) + " exp");
        if(this.speed != 1F) parts.add(signedPercent(this.speed) + " speed");

        parts.add((stack.getMaxDamage() - stack.getDamageValue()) + "HP");

        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (" + String.join(" / ", parts) + ")"))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {

        float amount = event.getContainer().getNewDamage();

        amount *= this.damageMod;
        if(event.getSource().is(DamageTypeTags.IS_PROJECTILE)) amount *= this.projectileMod;
        if(event.getSource().is(DamageTypeTags.IS_EXPLOSION)) amount *= this.explosionMod;

        event.getContainer().setNewDamage(amount);

        /* Die Einlage nutzt sich ab und faellt heraus, wenn sie durch ist. */
        ItemStack insert = ArmorModHandler.pryMod(event.getEntity().level(), armor, ArmorModHandler.KEVLAR);

        if(insert.isEmpty()) return;

        insert.setDamageValue(insert.getDamageValue() + 1);

        if(this.reactive && !event.getEntity().level().isClientSide) {
            LivingEntity wearer = event.getEntity();
            wearer.level().explode(wearer, wearer.getX(), wearer.getY() + wearer.getBbHeight() * 0.5D, wearer.getZ(), 0.05F, Level.ExplosionInteraction.NONE);
        }

        if(insert.getDamageValue() >= insert.getMaxDamage()) {
            ArmorModHandler.removeMod(armor, ArmorModHandler.KEVLAR);
        } else {
            ArmorModHandler.applyMod(event.getEntity().level(), armor, insert);
        }
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {

        if(this.radPerTick <= 0F) return;
        if(entity.level().isClientSide) return;

        HbmLivingAttachments.incrementRadiation(entity, this.radPerTick);
    }

    @Override
    public void addAttributes(ItemStack armor, Map<Holder<Attribute>, Double> out) {

        if(this.speed == 1F) return;

        out.merge(Attributes.MOVEMENT_SPEED, (double) (this.speed - 1F), Double::sum);
    }

    private static MutableComponent percent(String key, float factor) {
        return Component.translatable(key, signedPercent(factor));
    }

    /** "-10%" bzw. "+10%", gerechnet aus dem Faktor. */
    private static String signedPercent(float factor) {
        int value = Math.round((factor - 1F) * 100F);
        return (value > 0 ? "+" : "") + value + "%";
    }
}
