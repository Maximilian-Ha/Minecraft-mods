package com.hbm.items.armor;

import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.items.IHelmetOverlayItem;
import com.hbm.render.util.RenderScreenOverlay;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.ContaminationUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorFSB ("armor with full set bonus").
 *
 * Die Wurzel aller Anzuege, die erst als GANZER SATZ etwas koennen. Getragen werden sie
 * einzeln, aber Trankwirkungen, Geigerton und alles weitere springen erst an, wenn alle vier
 * Teile aus demselben Material am Koerper sind -- geprueft wird das an der Brustplatte, denn
 * nur sie weiss, ob ihr Satz ueberhaupt einen Helm vorsieht.
 *
 * ABWEICHUNG, die Anmeldung: das Original haengt Gefahrenklassen und Strahlenschutz mit
 * Baukastenmethoden an den Gegenstand (setHazardClass, setRadResist) und sammelt sie in
 * Listen, die spaeter abgearbeitet werden. Der Port hat dafuer schon zwei zentrale Stellen --
 * ArmorUtil.register und HazmatRegistry.initDefault --, und dort steht der HEV-Anzug jetzt
 * auch. Zwei Wege fuer dieselbe Sache waeren einer zu viel.
 *
 * NOCH NICHT PORTIERT und darum hier auch nicht als Schalter vorhanden: VATS, Waermesicht,
 * Sprung ("dash"), Schritt- und Sprunggeraeusche sowie die Helmscheibe. Alle vier haengen an
 * Teilsystemen, die der Port nicht hat; ein Schalter, den niemand liest, waere toter Zustand.
 */
public class ArmorFSBItem extends ArmorItem implements IHelmetOverlayItem {

    /** Was der ganze Satz dem Traeger dauerhaft gibt. */
    public final List<MobEffectInstance> effects = new ArrayList<>();

    /** Saetze ohne eigenen Helm -- dann zaehlen nur Brust, Beine und Schuhe. */
    public boolean noHelmet = false;

    /** Ob die Brustplatte den Traeger die Strahlung hoeren laesst. */
    public boolean geigerSound = false;

    /** Ob der Helm eine eigene Strahlenanzeige einblendet. */
    public boolean customGeiger = false;

    /**
     * Das Bild, das der Helm ueber den Schirm legt. Im Original heisst die Baukastenmethode
     * setOverlay; nur die Asbestruestung benutzt sie. Leer heisst kein Bild -- und dann
     * zeichnet renderHelmetOverlay auch nichts.
     */
    private @Nullable ResourceLocation overlay;

    public ArmorFSBItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public ArmorFSBItem setOverlay(ResourceLocation overlay) {
        this.overlay = overlay;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHelmetOverlay(GuiGraphics guiGraphics, ItemStack stack) {
        if(this.overlay != null) RenderScreenOverlay.renderHelmetOverlay(guiGraphics, this.overlay);
    }

    public ArmorFSBItem addEffect(MobEffectInstance effect) {
        this.effects.add(effect);
        return this;
    }

    public ArmorFSBItem setNoHelmet(boolean noHelmet) {
        this.noHelmet = noHelmet;
        return this;
    }

    public ArmorFSBItem setHasGeigerSound(boolean geigerSound) {
        this.geigerSound = geigerSound;
        return this;
    }

    public ArmorFSBItem setHasCustomGeiger(boolean customGeiger) {
        this.customGeiger = customGeiger;
        return this;
    }

    /** Ob dieses einzelne Teil mitspielt. Bei den bestromten haengt das an der Ladung. */
    public boolean isArmorEnabled(ItemStack stack) {
        return true;
    }

    /**
     * Traegt der Spieler einen vollstaendigen Satz, und hat jedes Teil auch das, was es zum
     * Arbeiten braucht? Die Brustplatte gibt den Ton an: ihr Material muessen alle teilen, und
     * sie allein weiss, ob der Satz einen Helm vorsieht.
     */
    public static boolean hasFSBArmor(Player player) {
        return hatSatz(player, true);
    }

    /** Wie oben, aber ohne die Ladungspruefung -- das Ladegeraet fragt so. */
    public static boolean hasFSBArmorIgnoreCharge(Player player) {
        return hatSatz(player, false);
    }

    private static boolean hatSatz(Player player, boolean mitLadung) {

        ItemStack plate = player.getItemBySlot(EquipmentSlot.CHEST);

        if(!(plate.getItem() instanceof ArmorFSBItem chestplate)) return false;

        EquipmentSlot[] slots = chestplate.noHelmet
                ? new EquipmentSlot[] { EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }
                : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

        for(EquipmentSlot slot : slots) {

            ItemStack piece = player.getItemBySlot(slot);

            if(!(piece.getItem() instanceof ArmorFSBItem armor)) return false;
            if(armor.getMaterial() != chestplate.getMaterial()) return false;
            if(mitLadung && !armor.isArmorEnabled(piece)) return false;
        }

        return true;
    }

    /**
     * Der Geigerton. Er kommt aus der BRUSTPLATTE, nicht aus dem Helm, und nur, wenn der
     * Traeger nicht ohnehin ein Zaehlrohr in der Tasche hat -- sonst klapperte es doppelt.
     *
     * Die Dosis wird mit dem Schutz des Anzugs verrechnet: was der Anzug abhaelt, hoert der
     * Traeger auch nicht. Die Staffelung der sieben Toene steht genauso im Original.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide) return;
        if(!(entity instanceof Player player)) return;
        if(player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;
        if(!hasFSBArmor(player)) return;

        if(!this.effects.isEmpty()) {
            for(MobEffectInstance effect : this.effects) {
                player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), true, false));
            }
        }

        if(!this.geigerSound) return;
        if(player.getInventory().contains(new ItemStack(NtmItems.GEIGER_COUNTER.get()))) return;
        if(player.getInventory().contains(new ItemStack(NtmItems.DOSIMETER.get()))) return;

        if(level.getGameTime() % 5 != 0) return;

        float dosis = HbmLivingAttachments.getRadBuf(player) * ContaminationUtil.calculateRadiationMod(player);

        if(dosis <= 1E-5) return;

        List<Integer> stufen = new ArrayList<>();

        if(dosis < 1) stufen.add(0);
        if(dosis < 5) stufen.add(0);
        if(dosis < 10) stufen.add(1);
        if(dosis > 5 && dosis < 15) stufen.add(2);
        if(dosis > 10 && dosis < 20) stufen.add(3);
        if(dosis > 15 && dosis < 25) stufen.add(4);
        if(dosis > 20 && dosis < 30) stufen.add(5);
        if(dosis > 25) stufen.add(6);

        SoundEvent ton = switch(stufen.get(level.random.nextInt(stufen.size()))) {
            case 1 -> NtmSoundEvents.GEIGER1.get();
            case 2 -> NtmSoundEvents.GEIGER2.get();
            case 3 -> NtmSoundEvents.GEIGER3.get();
            case 4 -> NtmSoundEvents.GEIGER4.get();
            case 5 -> NtmSoundEvents.GEIGER5.get();
            case 6 -> NtmSoundEvents.GEIGER6.get();
            default -> null;
        };

        if(ton != null) level.playSound(null, player.getX(), player.getY(), player.getZ(), ton, SoundSource.AMBIENT, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        List<Component> bonus = new ArrayList<>();

        if(!this.effects.isEmpty()) {
            List<String> namen = new ArrayList<>();
            for(MobEffectInstance effect : this.effects) namen.add(Component.translatable(effect.getDescriptionId()).getString());
            bonus.add(Component.literal(String.join(", ", namen)).withStyle(ChatFormatting.AQUA));
        }

        if(this.geigerSound) bonus.add(Component.translatable("armor.geigerSound").withStyle(ChatFormatting.GOLD));
        if(this.customGeiger) bonus.add(Component.translatable("armor.geigerHUD").withStyle(ChatFormatting.GOLD));

        if(!bonus.isEmpty()) {
            components.add(Component.translatable("armor.fullSetBonus").withStyle(ChatFormatting.GOLD));
            components.addAll(bonus);
        }
    }
}
