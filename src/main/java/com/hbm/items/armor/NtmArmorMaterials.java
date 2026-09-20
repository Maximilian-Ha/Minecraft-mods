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

    /*
     * DREI MASKEN MIT EIGENEM KOPFMODELL. Werte wie MASK -- sie unterscheiden sich allein in
     * der Schichttextur, und die ist hier nicht unsichtbar, sondern die Textur des Modells:
     * ein Kastenmodell aus getGenericArmorModel wird von der Ruestungsschicht mit genau dieser
     * Datei gezeichnet. Darum braucht jede Maske einen eigenen Werkstoff, obwohl alle drei
     * gleich schuetzen.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MASK_GAS = register("mask_gas", "gas_mask", 9, 2, 6, 5, 2, () -> Ingredient.of(Items.IRON_INGOT));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MASK_M65 = register("mask_m65", "gas_mask_m65", 9, 2, 6, 5, 2, () -> Ingredient.of(Items.IRON_INGOT));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MASK_MONO = register("mask_mono", "gas_mask_mono", 9, 2, 6, 5, 2, () -> Ingredient.of(Items.IRON_INGOT));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES = register("goggles", "goggles", 9, 2, 6, 5, 2, () -> Ingredient.of(Items.IRON_INGOT));

    /*
     * DIE ACHT SCHLICHTEN GARNITUREN. Im Original allesamt ArmorFSB ohne einen einzigen
     * Zusatz -- kein Satzbonus, keine Trankwirkung, kein Schirmbild. Sie unterscheiden sich
     * nur in Haltbarkeitsfaktor, Verzauberbarkeit und Textur; die Schutzwerte {3, 8, 6, 3}
     * sind bei allen gleich. Werte aus MainRegistry des Originals.
     *
     * HBM_ALLOY ist dort als @Deprecated gekennzeichnet -- der Gegenstand bleibt trotzdem
     * angemeldet, und darum steht er auch hier.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> STEEL = register("steel", "steel", 5, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.INGOT_STEEL.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TITANIUM = register("titanium", "titanium", 9, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.INGOT_TITANIUM.get()));
    /* HBM_ALLOY hat im Original KEIN customCraftingMaterial -- die Garnitur laesst sich dort
     * nicht am Amboss ausbessern, passend dazu, dass der Werkstoff als @Deprecated
     * gekennzeichnet ist und MAT_ALLOY in Mats auskommentiert. Das bleibt so. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALLOY = register("alloy", "alloy", 12, 3, 8, 6, 3, () -> Ingredient.EMPTY);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COBALT = register("cobalt", "cobalt", 60, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.INGOT_COBALT.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> STARMETAL = register("starmetal", "starmetal", 100, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.INGOT_STARMETAL.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SECURITY = register("security", "security", 15, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_KEVLAR.get()));
    /* DIE ROBE IST KEIN EIGENER WERKSTOFF: das Original reicht ArmorMaterial.CHAIN durch,
     * also die Kettenruestung des Grundspiels -- {2, 5, 4, 1}, Verzauberbarkeit 12,
     * Haltbarkeitsfaktor 15, Reparatur mit Eisen. Nur die Schicht ist eine eigene. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ROBES = register("robes", "robes", 12, 2, 5, 4, 1, () -> Ingredient.of(Items.IRON_INGOT));
    /* HBM_DNT_LOLOLOL: der Name ist der des Originals, die Werte auch -- {1,1,1,1}, kaum
     * Haltbarkeit, keine Verzauberbarkeit. Die Garnitur ist ein Scherz und soll einer sein. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DNT = register("dnt", "dnt", 0, 1, 1, 1, 1, () -> Ingredient.of(NtmItems.INGOT_DINEUTRONIUM.get()));
    /* Zirkonium gibt es nur als Hose. Haltbarkeitsfaktor 1000, Verzauberbarkeit 1000. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ZIRCONIUM = register("zirconium", "zirconium", 1000, 2, 5, 3, 1, () -> Ingredient.of(NtmItems.INGOT_ZIRCONIUM.get()));

    /*
     * VIER GARNITUREN MIT EINEM ZUSATZ. Auch sie sind ArmorFSB, aber jede bringt etwas mit:
     * Asbest ein Schirmbild, CMB und Schrabidium Trankwirkungen fuer den ganzen Satz, die
     * PAA-Ruestung eine Trankwirkung und die Eigenheit, dass sie OHNE HELM zaehlt.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ASBESTOS = register("asbestos", "asbestos", 5, 1, 4, 3, 1, () -> Ingredient.of(NtmItems.ASBESTOS_CLOTH.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CMB = register("cmb", "cmb", 50, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.INGOT_COMBINE_STEEL.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SCHRABIDIUM = register("schrabidium", "schrabidium", 50, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.INGOT_SCHRABIDIUM.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PAA = register("paa", "paa", 25, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_PAA.get()));

    /* Die T-51: Haltbarkeitsfaktor 150, {3, 8, 6, 3}, keine Verzauberbarkeit, Reparatur mit
     * der Titan-Panzerplatte. Sie wird als Wellenfrontmodell gezeichnet, die Schicht bleibt
     * darum unsichtbar. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> T51 = register("t51", LAYER_INVISIBLE, 0, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_ARMOR_TITANIUM.get()));

    public static final int DURABILITY_T51 = 150;
    public static final int DURABILITY_ASBESTOS = 20;
    public static final int DURABILITY_CMB = 60;
    public static final int DURABILITY_SCHRABIDIUM = 100;
    public static final int DURABILITY_PAA_ARMOR = 75;

    public static final int DURABILITY_STEEL = 30;
    public static final int DURABILITY_TITANIUM = 25;
    public static final int DURABILITY_ALLOY = 40;
    public static final int DURABILITY_COBALT = 70;
    public static final int DURABILITY_STARMETAL = 150;
    public static final int DURABILITY_SECURITY = 100;
    public static final int DURABILITY_ROBES = 15;
    public static final int DURABILITY_DNT = 3;
    public static final int DURABILITY_ZIRCONIUM = 1000;

    /*
     * DER BLEIANZUG DER LIQUIDATOREN. Im Original ein Werkstoff: HBM_LIQUIDATOR,
     * Haltbarkeitsfaktor 750, {3, 8, 6, 3}, Verzauberbarkeit 10, Reparatur mit der Bleiplatte.
     *
     * Im Port sind es zwei, und zwar nur wegen der Textur: die Haube traegt am Koerper das
     * M65-Kopfmodell, und die Ruestungsschicht bindet dafuer liquidator_helmet.png, waehrend
     * Weste, Hose und Stiefel die gewoehnlichen Schichten liquidator_1 und liquidator_2
     * brauchen. Die Schutzwerte sind in beiden gleich; der Grund steht in Runde 206.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIQUIDATOR = register("liquidator", "liquidator", 10, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_LEAD.get()));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIQUIDATOR_HOOD = register("liquidator_hood", "liquidator_helmet", 10, 3, 8, 6, 3, () -> Ingredient.of(NtmItems.PLATE_LEAD.get()));

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
    public static final int DURABILITY_LIQUIDATOR = 750;

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
