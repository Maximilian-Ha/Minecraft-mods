package com.hbm.items.armor;

import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Die Ruestungsmaterialien des Mods. Im Original sind das Eintraege aus
 * EnumHelper.addArmorMaterial(name, haltbarkeitsfaktor, ruestungswerte, verzauberbarkeit);
 * in 1.21 ist ArmorMaterial ein eigenes Register, also wandert jeder Eintrag hierher.
 *
 * Die Zahlen sind unveraendert aus MainRegistry bzw. ModItemsArmor des Originals
 * uebernommen. Die Reihenfolge der Ruestungswerte dort ist {Helm, Brust, Beine, Schuhe}.
 */
public class NtmArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, NuclearTechMod.MODID);

    /**
     * Die Schicht fuer alles, was am Koerper unsichtbar bleiben soll. Im Original
     * tragen diese Teile ein eigenes Modell (ModelGasMask, ModelM65) statt einer
     * Ruestungsschicht; solange die Modelle nicht portiert sind, wird nichts gezeichnet.
     * Das ist besser als die lilaschwarze Ersatztextur einer fehlenden Datei.
     */
    private static final String LAYER_INVISIBLE = "invisible";

    /* Die drei Schutzanzuege teilen sich alle Werte und unterscheiden sich nur in
     * Textur und Reparaturmaterial. Der Haltbarkeitsfaktor ist 60. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = register("hazmat", "hazmat", 5, 2, 5, 4, 1, () -> Ingredient.of(NtmItems.HAZMAT_CLOTH.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT_RED = register("hazmat_red", "hazmat_red", 5, 2, 5, 4, 1, () -> Ingredient.of(NtmItems.HAZMAT_CLOTH_RED.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT_GREY = register("hazmat_grey", "hazmat_grey", 5, 2, 5, 4, 1, () -> Ingredient.of(NtmItems.HAZMAT_CLOTH_GREY.get()));
    /* Der PAA-Anzug ist der schwere Bruder: volle Ruestungswerte, Haltbarkeitsfaktor 75. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT_PAA = register("hazmat_paa", "hazmat_paa", 25, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_PAA.get()));
    /* Lappen vor dem Mund: HBM_RAGS, Haltbarkeitsfaktor 150, ein Punkt Schutz. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> RAGS = register("rags", LAYER_INVISIBLE, 0, 1, 1, 1, 1, () -> Ingredient.of(NtmItems.RAG.get()));
    /* Die Gasmasken nehmen im Original schlicht ArmorMaterial.IRON. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MASK = register("mask", LAYER_INVISIBLE, 9, 2, 6, 5, 2, () -> Ingredient.of(Items.IRON_INGOT));

    /* Der HEV-Anzug. Er wird nicht als Ruestungsschicht gezeichnet, sondern als eigenes
     * Wellenfrontmodell (ModelArmorHEV) -- die Schicht bleibt darum unsichtbar.
     * Werte aus ModItemsArmor: HBM_HEV, Haltbarkeitsfaktor 150, {3, 8, 6, 3}, ohne
     * Verzauberbarkeit. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEV = register("hev", LAYER_INVISIBLE, 0, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_ARMOR_HEV.get()));

    /* Die beiden Panzerruestungen teilen sich im Original den Werkstoff HBM_T45AJR:
     * Haltbarkeitsfaktor 150, {3, 8, 6, 3}, ohne Verzauberbarkeit, Reparatur mit der
     * AJR-Platte. Gezeichnet werden sie als eigenes Wellenfrontmodell, die Schicht bleibt
     * darum unsichtbar. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> AJR = register("ajr", LAYER_INVISIBLE, 0, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_ARMOR_AJR.get()));

    /** Haltbarkeitsfaktoren aus dem Original, gebraucht fuer Item.Properties.durability. */
    public static final int DURABILITY_HAZMAT = 60;
    public static final int DURABILITY_PAA = 75;
    public static final int DURABILITY_RAGS = 150;
    public static final int DURABILITY_MASK = 15;
    public static final int DURABILITY_HEV = 150;
    public static final int DURABILITY_AJR = 150;

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(String name, String layer, int enchantability, int helmet, int chest, int legs, int boots, Supplier<Ingredient> repair) {
        return ARMOR_MATERIALS.register(name, () -> {
            Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
            defense.put(ArmorItem.Type.HELMET, helmet);
            defense.put(ArmorItem.Type.CHESTPLATE, chest);
            defense.put(ArmorItem.Type.LEGGINGS, legs);
            defense.put(ArmorItem.Type.BOOTS, boots);
            /* BODY gilt fuer Tierpanzerung. Die gibt es hier nicht, der Eintrag muss
             * aber gesetzt sein, weil getDefense sonst ins Leere greift. */
            defense.put(ArmorItem.Type.BODY, chest);

            return new ArmorMaterial(
                    defense,
                    enchantability,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    repair,
                    List.of(new ArmorMaterial.Layer(NuclearTechMod.withDefaultNamespace(layer))),
                    0.0F,
                    0.0F);
        });
    }

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
