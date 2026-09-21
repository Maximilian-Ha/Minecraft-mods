package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.TagsUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ItemModKnife.
 *
 * Das Messer sitzt im Sonderplatz einer Brustplatte und schneidet seinen Traeger langsam
 * auseinander: alle 50 Ticks nimmt es ihm zwei Punkte hoechster Lebensenergie, bis nur noch
 * zwei uebrig sind. Dann hoert es auf -- und genau in dem Augenblick faellt der Erfolg.
 *
 * WIE DER ABZUG GESPEICHERT WIRD. Das Original haengt eine Attributaenderung mit fester
 * Kennung an den Traeger und ersetzt sie bei jedem Schnitt durch eine groessere; die Summe
 * steht also im Traeger. In 1.21 rechnet ArmorModHandler die Summe jeden Tick neu aus den
 * Modulen zusammen, darum steht der Stand hier am Ruestungsteil -- an derselben Stelle, an
 * der auch die Module selbst liegen. Das Ergebnis ist dasselbe, und es ueberlebt das
 * Abnehmen und Wiederanlegen, wie im Original.
 *
 * DER ERFOLG FAELLT EINEN SCHNITT FRUEHER, ALS ER AUSSIEHT. Das Original liest die Gesundheit
 * NACH dem Setzen der Aenderung; hier laeuft addAttributes erst nach modUpdate, die neue
 * Grenze steht also noch nicht. Darum wird sie vorgerechnet: wer nach diesem Schnitt bei zwei
 * oder darunter landet, hat ihn. Das ist derselbe Schnitt wie dort.
 *
 * OHNE DEN BLUTPARTIKEL. Das Original schickt dazu einen "bloodvomit"-Partikel und einen
 * Bildschirmruckler ("properJolt"); beides gibt es im Port nicht. Der Klang bleibt.
 */
public class ItemModKnife extends ItemArmorMod {

    /** Wie viel hoechste Lebensenergie das Messer dem Traeger schon genommen hat. */
    public static final String SCHNITTE_KEY = "ntm_knife_cut";

    /** Zwei Punkte je Schnitt, und Schluss bei zwei -- beide Zahlen aus dem Original. */
    private static final double JE_SCHNITT = 2D;
    private static final float UNTERGRENZE = 2F;

    public ItemModKnife(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, false, true, false, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Pain.").withStyle(ChatFormatting.RED));
        components.add(Component.empty());
        components.add(Component.literal("Hurts, doesn't it?").withStyle(ChatFormatting.RED));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName()).withStyle(ChatFormatting.RED));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {

        if(entity.level().isClientSide) return;
        if(entity.tickCount % 50 != 0) return;
        if(entity.getMaxHealth() <= UNTERGRENZE) return;

        entity.level().playSound(null, entity.blockPosition(), NtmSoundEvents.SLICER.get(),
                SoundSource.NEUTRAL, 1.0F, 1.0F);

        CompoundTag tafel = TagsUtil.getCustomData(armor);
        tafel.putDouble(SCHNITTE_KEY, tafel.getDouble(SCHNITTE_KEY) + JE_SCHNITT);
        TagsUtil.putCustomData(armor, tafel);

        if(entity.getMaxHealth() - JE_SCHNITT <= UNTERGRENZE && entity instanceof ServerPlayer spieler) {
            NtmCriteria.marke(spieler, "some_wounds");
        }
    }

    @Override
    public void addAttributes(ItemStack armor, Map<Holder<Attribute>, Double> out) {
        double schnitte = TagsUtil.getCustomData(armor).getDouble(SCHNITTE_KEY);
        if(schnitte > 0D) out.merge(Attributes.MAX_HEALTH, -schnitte, Double::sum);
    }
}
