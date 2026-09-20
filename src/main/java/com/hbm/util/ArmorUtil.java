package com.hbm.util;

import api.hbm.item.IGasMask;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.HazmatRegistry;
import com.hbm.items.NtmItems;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ArmorUtil {

    /*
     * The less horrifying part
     */

    public static void register() {

        /* Die Filter tragen den eigentlichen Schutz. Die Maske entscheidet nur, was sie
         * davon durchlaesst -- siehe GasMaskItem.getBlacklist. */
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK_FILTER.get(), HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA);
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK_FILTER_MONO.get(), HazardClass.PARTICLE_COARSE, HazardClass.GAS_MONOXIDE);
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK_FILTER_COMBO.get(), HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE);
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK_FILTER_RAG.get(), HazardClass.PARTICLE_COARSE);
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK_FILTER_PISS.get(), HazardClass.PARTICLE_COARSE, HazardClass.GAS_LUNG);

        /* Was die Maske von sich aus kann, ganz ohne Filter. */
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK.get(), HazardClass.SAND, HazardClass.LIGHT);
        ArmorRegistry.registerHazard(NtmItems.GAS_MASK_M65.get(), HazardClass.SAND);
        ArmorRegistry.registerHazard(NtmItems.GOGGLES.get(), HazardClass.LIGHT, HazardClass.SAND);
        ArmorRegistry.registerHazard(NtmItems.MASK_RAG.get(), HazardClass.PARTICLE_COARSE);
        ArmorRegistry.registerHazard(NtmItems.MASK_PISS.get(), HazardClass.PARTICLE_COARSE, HazardClass.GAS_LUNG);

        ArmorRegistry.registerHazard(NtmItems.HAZMAT_HELMET.get(), HazardClass.SAND);
        ArmorRegistry.registerHazard(NtmItems.HAZMAT_HELMET_RED.get(), HazardClass.SAND);
        ArmorRegistry.registerHazard(NtmItems.HAZMAT_HELMET_GREY.get(), HazardClass.SAND);
        ArmorRegistry.registerHazard(NtmItems.HAZMAT_PAA_HELMET.get(), HazardClass.LIGHT, HazardClass.SAND);
        ArmorRegistry.registerHazard(NtmItems.LIQUIDATOR_HELMET.get(), HazardClass.LIGHT, HazardClass.SAND);

        /* Der HEV-Anzug haelt alles ab, was es gibt. Das Original schreibt dafuer
         * ArmorUtil.FULL_PACKAGE an jedes der vier Teile. */
        for(Item teil : new Item[] { NtmItems.HEV_HELMET.get(), NtmItems.HEV_PLATE.get(), NtmItems.HEV_LEGS.get(), NtmItems.HEV_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Die T-51 haelt alles ab ausser Blendung -- sie hat ein Sichtfenster. */
        for(Item teil : new Item[] { NtmItems.T51_HELMET.get(), NtmItems.T51_PLATE.get(), NtmItems.T51_LEGS.get(), NtmItems.T51_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_NO_LIGHT);
        }

        /* Der AJR-Anzug und seine orangene Spielart halten alles ab -- im Original steht an
         * beiden FULL_PACKAGE, anders als bei der T-51. */
        for(Item teil : new Item[] {
                NtmItems.AJR_HELMET.get(), NtmItems.AJR_PLATE.get(), NtmItems.AJR_LEGS.get(), NtmItems.AJR_BOOTS.get(),
                NtmItems.AJRO_HELMET.get(), NtmItems.AJRO_PLATE.get(), NtmItems.AJRO_LEGS.get(), NtmItems.AJRO_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Die Taurun-Ruestung ebenso -- auch sie traegt im Original FULL_PACKAGE. */
        for(Item teil : new Item[] { NtmItems.TAURUN_HELMET.get(), NtmItems.TAURUN_PLATE.get(), NtmItems.TAURUN_LEGS.get(), NtmItems.TAURUN_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Der Fau-Anzug ebenso. */
        for(Item teil : new Item[] { NtmItems.FAU_HELMET.get(), NtmItems.FAU_PLATE.get(), NtmItems.FAU_LEGS.get(), NtmItems.FAU_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Der Grabenmeister ebenso. */
        for(Item teil : new Item[] { NtmItems.TRENCHMASTER_HELMET.get(), NtmItems.TRENCHMASTER_PLATE.get(), NtmItems.TRENCHMASTER_LEGS.get(), NtmItems.TRENCHMASTER_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Der Umgebungsanzug ebenso -- er ist ja gerade dafuer gebaut. */
        for(Item teil : new Item[] { NtmItems.ENVSUIT_HELMET.get(), NtmItems.ENVSUIT_PLATE.get(), NtmItems.ENVSUIT_LEGS.get(), NtmItems.ENVSUIT_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Der DNT-Nanoanzug ebenso. */
        for(Item teil : new Item[] { NtmItems.DNS_HELMET.get(), NtmItems.DNS_PLATE.get(), NtmItems.DNS_LEGS.get(), NtmItems.DNS_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }

        /* Der Dampfanzug ebenso -- sein Helm ist ausdruecklich ein Atemschutz. */
        for(Item teil : new Item[] { NtmItems.STEAMSUIT_HELMET.get(), NtmItems.STEAMSUIT_PLATE.get(), NtmItems.STEAMSUIT_LEGS.get(), NtmItems.STEAMSUIT_BOOTS.get() }) {
            ArmorRegistry.registerHazard(teil, FULL_PACKAGE);
        }
    }

    /**
     * Dasselbe ohne LIGHT. Panzerruestungen mit Sichtfenster halten alles ab, was ein
     * geschlossener Anzug abhaelt -- nur vor Blendung schuetzen sie nicht. Im Original heisst
     * diese Liste ebenso.
     */
    public static final HazardClass[] FULL_NO_LIGHT = {
            HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.SAND };

    /**
     * Alles, wogegen ein geschlossener Anzug schuetzt. Uebernommen aus ArmorUtil des
     * Originals, wo dieselbe Liste unter demselben Namen steht.
     */
    public static final HazardClass[] FULL_PACKAGE = {
            HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND };

    public static boolean checkArmor(LivingEntity entity, Item... armor) {
        EquipmentSlot[] slots = {
                EquipmentSlot.FEET,   // 0
                EquipmentSlot.LEGS,   // 1
                EquipmentSlot.CHEST,  // 2
                EquipmentSlot.HEAD    // 3
        };

        for (int i = 0; i < slots.length; i++) {
            if (!checkArmorPiece(entity, armor[i], slots[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkArmorPiece(LivingEntity entity, Item armor, EquipmentSlot slot) {
        return !checkArmorEmpty(entity, slot) && entity.getItemBySlot(slot).getItem() == armor;
    }

    public static boolean checkArmorEmpty(LivingEntity player, EquipmentSlot slot) {
        return player.getItemBySlot(slot).isEmpty();
    }

    /**
     * Portiert aus 1.7.10: ArmorUtil.metals.
     *
     * Der faradaysche Kaefig entsteht im Original nicht ueber ein Merkmal am Gegenstand,
     * sondern ueber den Namen: enthaelt der Name eines der folgenden Woerter, leitet das
     * Teil. Gummi und Hazmat sind mit derselben Begruendung dabei wie im Original --
     * sie isolieren, statt zu leiten, und schuetzen deshalb ebenso.
     *
     * ABWEICHUNG: das Original prueft den unlokalisierten Namen ("item.armor_steel_helmet"),
     * der Port den Pfad im Gegenstandsverzeichnis ("armor_steel_helmet"). Dasselbe Ergebnis,
     * aber unabhaengig von der Sprachdatei -- bis auf einen Fall: die Kettenruestung heisst
     * in 1.7.10 "item.helmetChain", und "chain" allein trifft den Listeneintrag "chainmail"
     * nicht. Im Original schuetzt Kettenruestung also trotz Eintrag nicht. In 1.21 heisst
     * sie "chainmail_helmet" und schuetzt. Das ist gewollt: die Liste sagt, was gemeint war.
     */
    private static final String[] LEITENDE_NAMEN = {
            "chainmail", "iron", "silver", "gold", "platinum", "tin", "lead", "liquidator",
            "schrabidium", "euphemium", "steel", "cmb", "titanium", "alloy", "copper",
            "bronze", "electrum", "t45", "t51", "bj", "starmetal",
            "hazmat", // zaehlt mit, weil Gummi isoliert
            "rubber", "hev", "ajr", "rpa", "spacesuit"
    };

    /** Ein einzelnes Ruestungsteil, das den Strom ableitet oder abhaelt. */
    public static boolean isFaradayArmor(Level level, ItemStack stack) {

        if(stack.isEmpty()) return false;

        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.US);
        for(String metall : LEITENDE_NAMEN) if(name.contains(metall)) return true;

        return HazmatRegistry.getCladding(level, stack) > 0;
    }

    /** Erst wenn alle vier Teile leiten, steht der Kaefig -- eine Luecke genuegt. */
    public static boolean checkForFaraday(Player player) {

        for(EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            if(!isFaradayArmor(player.level(), player.getItemBySlot(slot))) return false;
        }

        return true;
    }

    public static void damageSuit(LivingEntity entity, EquipmentSlot slot, int amount) {
        entity.getItemBySlot(slot).hurtAndBreak(amount, entity, slot);
    }

    /*
     * Default implementations for IGasMask items
     */
    public static final String FILTER_KEY = "hfr_Filter";

    public static void installGasMaskFilter(Level level, ItemStack mask, ItemStack filter) {

        if (mask.isEmpty() || filter.isEmpty()) return;

        CompoundTag tag = TagsUtil.getCustomData(mask);

        CompoundTag attach = new CompoundTag();
        filter.save(level.registryAccess(), attach);

        tag.put(FILTER_KEY, attach);

        TagsUtil.putCustomData(mask, tag);
    }

    public static void removeFilter(ItemStack mask) {
        if (mask.isEmpty()) return;

        CompoundTag maskTag = TagsUtil.getCustomData(mask);

        if (maskTag.contains(FILTER_KEY)) {
            maskTag.remove(FILTER_KEY);

            TagsUtil.putCustomData(mask, maskTag);
        }
    }

    /**
     * Grabs the installed filter or the filter of the attachment, used for attachment rendering
     */
    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {

        ItemStack filter = getGasMaskFilter(entity.level(), mask);

        if (filter.isEmpty() && ArmorModHandler.hasMods(mask)) {

            ItemStack[] mods = ArmorModHandler.pryMods(entity.level(), mask);

            if (!mods[ArmorModHandler.HELMET_ONLY].isEmpty() && mods[ArmorModHandler.HELMET_ONLY].getItem() instanceof IGasMask)
                filter = ((IGasMask)mods[ArmorModHandler.HELMET_ONLY].getItem()).getFilter(mods[ArmorModHandler.HELMET_ONLY], entity);
        }

        return filter;
    }

    public static ItemStack getGasMaskFilter(Level level, ItemStack mask) {
        if (mask.isEmpty()) return ItemStack.EMPTY;

        CompoundTag maskTag = TagsUtil.getCustomData(mask);

        if (!maskTag.contains(FILTER_KEY)) return ItemStack.EMPTY;

        CompoundTag attach = maskTag.getCompound(FILTER_KEY);

        Optional<ItemStack> optionalStack = ItemStack.parse(level.registryAccess(), attach);

        return optionalStack.orElse(ItemStack.EMPTY);
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {

        ItemStack mask = entity.getItemBySlot(EquipmentSlot.HEAD);

        if (mask.isEmpty()) return;

        if (!(mask.getItem() instanceof IGasMask)) {

            if (ArmorModHandler.hasMods(mask)) {

                ItemStack[] mods = ArmorModHandler.pryMods(entity.level(), mask);

                if (!mods[ArmorModHandler.HELMET_ONLY].isEmpty() && mods[ArmorModHandler.HELMET_ONLY].getItem() instanceof IGasMask)
                    mask = mods[ArmorModHandler.HELMET_ONLY];
            }
        }
        damageGasMaskFilter(entity.level(), mask, damage);
    }

    public static void damageGasMaskFilter(Level level, ItemStack mask, int damage) {
        ItemStack filter = getGasMaskFilter(level, mask);

        if (filter.isEmpty()) {
            if (ArmorModHandler.hasMods(mask)) {
                ItemStack[] mods = ArmorModHandler.pryMods(level, mask);

                if (!mods[ArmorModHandler.HELMET_ONLY].isEmpty() && mods[ArmorModHandler.HELMET_ONLY].getItem() instanceof IGasMask)
                    filter = getGasMaskFilter(level, mods[ArmorModHandler.HELMET_ONLY]);
            }
        }

        if (filter.isEmpty() || filter.getMaxDamage() == 0)
            return;

        filter.setDamageValue(filter.getDamageValue() + damage);

        if (filter.getDamageValue() > filter.getMaxDamage()) {
            removeFilter(mask);
        } else {
            installGasMaskFilter(level, mask, filter);
        }
    }


    /**
     * Portiert aus 1.7.10: ArmorUtil.addGasMaskTooltip.
     *
     * Zeigt am Kopfteil, welcher Filter steckt und wie viel von ihm noch uebrig ist.
     */
    public static void addGasMaskTooltip(Level level, ItemStack mask, List<Component> components) {

        if(level == null) return;
        if(!(mask.getItem() instanceof IGasMask)) return;

        ItemStack filter = getGasMaskFilter(level, mask);

        if(filter.isEmpty()) {
            components.add(Component.translatable("armor.noFilter").withStyle(ChatFormatting.RED));
            return;
        }

        components.add(Component.translatable("armor.filter").withStyle(ChatFormatting.GOLD));

        MutableComponent line = Component.literal("  ").append(filter.getHoverName());

        int max = filter.getMaxDamage();
        if(max > 0) line.append(Component.literal(" (" + ((max - filter.getDamageValue()) * 100 / max) + "%)"));

        components.add(line.withStyle(ChatFormatting.YELLOW));
    }

    public static boolean isWearingEmptyMask(Player player) {

        ItemStack mask = player.getItemBySlot(EquipmentSlot.HEAD);

        if (mask.isEmpty()) return false;

        if (mask.getItem() instanceof IGasMask) {
            return getGasMaskFilter(player.level(), mask).isEmpty();
        }

        ItemStack mod = ArmorModHandler.pryMods(player.level(), mask)[ArmorModHandler.HELMET_ONLY];

        if (mod != null && mod.getItem() instanceof IGasMask) {
            return getGasMaskFilter(player.level(), mod).isEmpty();
        }

        return false;
    }
}