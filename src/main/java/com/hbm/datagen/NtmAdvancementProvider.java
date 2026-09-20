package com.hbm.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Die Erfolge, Runde 247.
 *
 * DAS ORIGINAL HAT 61, DER PORT HATTE EINEN -- und der war ein Platzhalter: root.json mit
 * dem Titel "test" und leerer Beschreibung, handgeschrieben in den Datenordner gelegt. Ein
 * Erfolgsbaum, der aus einer leeren Wurzel besteht, ist derselbe Fehler wie eine Klasse,
 * die niemand anmeldet: er steht da und tut nichts.
 *
 * WAS DIESE RUNDE BRINGT: die Fortschrittskette. Das Original teilt seine Erfolge in zwei
 * Gruppen, und die Trennlinie laesst sich messen -- 32 Namen stehen in einem
 * triggerAchievement-Aufruf irgendwo im Quelltext, die uebrigen nicht. Wer nicht getriggert
 * wird, kommt ueber Forges Bau- und Aufnahmeerkennung: sein Symbolgegenstand im Inventar
 * loest ihn aus. Genau diese Gruppe steht hier, mit InventoryChangeTrigger, der auf 1.21
 * dasselbe tut.
 *
 * DIE 32 GETRIGGERTEN FEHLEN NOCH, und das ist kein Versehen: jeder von ihnen braucht eine
 * Stelle im Port, die ihn feuert -- den Tod durch Strahlung, das Betreten des roten
 * Zimmers, den Start einer Sojus. Sie kommen mit ihren Ausloesern, nicht vorher.
 *
 * GITTERPLAETZE GIBT ES IN 1.21 NICHT MEHR. Das Original setzt jeden Erfolg auf eine
 * Koordinate (x, y); 1.21 legt den Baum selbst aus der Vorgaengerkette. Die Kette ist
 * uebernommen, die Koordinaten entfallen ersatzlos.
 *
 * NICHT DABEI: achSILEX -- machine_silex gibt es im Port nicht. Und achChicagoPile, dessen
 * Symbol pile_rod_plutonium im Port die Metadatensorte PU239 von PILE_ROD ist; ein
 * Erfolg auf eine Metadatensorte braucht ein DataComponentPredicate und kommt mit der
 * naechsten Gruppe.
 */
public class NtmAdvancementProvider extends AdvancementProvider {

    public NtmAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        super(output, registries, helper, List.of(new NtmAdvancements()));
    }

    /* Der innere Typ von NeoForges AdvancementProvider, voll ausgeschrieben: so sieht
     * auch das Import-Tor, woher er kommt. */
    private static class NtmAdvancements implements AdvancementProvider.AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> speichern, ExistingFileHelper helper) {

            AdvancementHolder wurzel = Advancement.Builder.advancement()
                    .display(NtmBlocks.MACHINE_PRESS.get(),
                            Component.translatable("advancements.hbmsntm.root.title"),
                            Component.translatable("advancements.hbmsntm.root.description"),
                            NuclearTechMod.withDefaultNamespace("textures/block/ore_oil.png"),
                            AdvancementType.TASK, false, false, false)
                    .addCriterion("tick", InventoryChangeTrigger.TriggerInstance.hasItems(NtmBlocks.MACHINE_PRESS.get()))
                    .save(speichern, NuclearTechMod.withDefaultNamespace("root").toString(), helper);

            /* Die Kette des Originals, Vorgaenger fuer Vorgaenger. Die Namen sind die von
             * MainRegistry; die Sprachschluessel tragen sie weiter. */
            AdvancementHolder burnerPress = erfolg(speichern, helper, wurzel, "burner_press", NtmBlocks.MACHINE_PRESS.get(), false);
            AdvancementHolder blastFurnace = erfolg(speichern, helper, burnerPress, "blast_furnace", NtmBlocks.MACHINE_BLAST_FURNACE.get(), false);
            AdvancementHolder assembly = erfolg(speichern, helper, burnerPress, "assembly", NtmBlocks.MACHINE_ASSEMBLY_MACHINE.get(), false);
            erfolg(speichern, helper, burnerPress, "selenium", NtmItems.INGOT_STARMETAL.get(), true);

            AdvancementHolder chemplant = erfolg(speichern, helper, assembly, "chemplant", NtmBlocks.MACHINE_CHEMICAL_PLANT.get(), false);
            AdvancementHolder concrete = erfolg(speichern, helper, chemplant, "concrete", NtmBlocks.CONCRETE.get(), false);
            AdvancementHolder polymer = erfolg(speichern, helper, chemplant, "polymer", NtmItems.INGOT_POLYMER.get(), false);
            AdvancementHolder desh = erfolg(speichern, helper, chemplant, "desh", NtmItems.INGOT_DESH.get(), false);
            erfolg(speichern, helper, chemplant, "tantalum", NtmItems.GEM_TANTALIUM.get(), true);

            erfolg(speichern, helper, desh, "gas_cent", NtmItems.INGOT_URANIUM_FUEL.get(), false);
            AdvancementHolder schrab = erfolg(speichern, helper, desh, "schrab", NtmItems.INGOT_SCHRABIDIUM.get(), false);
            erfolg(speichern, helper, desh, "acidizer", NtmBlocks.MACHINE_CRYSTALLIZER.get(), false);

            AdvancementHolder centrifuge = erfolg(speichern, helper, polymer, "centrifuge", NtmBlocks.MACHINE_CENTRIFUGE.get(), false);
            erfolg(speichern, helper, centrifuge, "technetium", NtmItems.INGOT_TCALLOY.get(), false);

            erfolg(speichern, helper, schrab, "watz", NtmItems.WATZ_PELLET.get(), false);

            AdvancementHolder rbmk = erfolg(speichern, helper, concrete, "rbmk", NtmItems.RBMK_FUEL_UEU.get(), false);
            AdvancementHolder bismuth = erfolg(speichern, helper, rbmk, "bismuth", NtmItems.INGOT_BISMUTH.get(), false);
            erfolg(speichern, helper, rbmk, "breeding", NtmItems.INGOT_AM_MIX.get(), true);
            erfolg(speichern, helper, bismuth, "fusion", NtmBlocks.FUSION_TORUS.get(), true);

            erfolg(speichern, helper, polymer, "red_balloons", NtmItems.MISSILE_NUCLEAR.get(), true);
        }

        /**
         * Ein Erfolg der Fortschrittskette: Symbolgegenstand im Inventar, fertig.
         *
         * @param besonders die .setSpecial()-Erfolge des Originals. In 1.21 heisst das
         *                  AdvancementType.CHALLENGE -- derselbe gezackte Rahmen.
         */
        private static AdvancementHolder erfolg(Consumer<AdvancementHolder> speichern, ExistingFileHelper helper,
                AdvancementHolder vorgaenger, String name, ItemLike symbol, boolean besonders) {

            return Advancement.Builder.advancement()
                    .parent(vorgaenger)
                    .display(symbol,
                            Component.translatable("advancements.hbmsntm." + name + ".title"),
                            Component.translatable("advancements.hbmsntm." + name + ".description"),
                            null,
                            besonders ? AdvancementType.CHALLENGE : AdvancementType.TASK,
                            true, true, false)
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(symbol))
                    .save(speichern, NuclearTechMod.withDefaultNamespace(name).toString(), helper);
        }
    }
}
