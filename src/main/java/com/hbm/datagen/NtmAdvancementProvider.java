package com.hbm.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;

import java.util.Optional;
import com.hbm.registry.NtmCriteria;
import net.minecraft.advancements.Criterion;
import net.minecraft.world.item.Items;
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
 * DIE GETRIGGERTEN KOMMEN MIT IHREN AUSLOESERN, NICHT VORHER: jeder von ihnen braucht eine
 * Stelle im Port, die ihn feuert -- den Tod durch Strahlung, das Betreten des roten Zimmers,
 * den Start einer Sojus. Nach Runde 252 stehen 17 der 32; die uebrigen 15 warten auf
 * Entitaeten und Gegenstaende, die der Port noch nicht hat (die Sojus, die vier uebrigen
 * Bosse, die Digamma-Kette, die Schimmerwaffen, das Messer, der Radiumkaffee, die Saeure).
 *
 * GITTERPLAETZE GIBT ES IN 1.21 NICHT MEHR. Das Original setzt jeden Erfolg auf eine
 * Koordinate (x, y); 1.21 legt den Baum selbst aus der Vorgaengerkette. Die Kette ist
 * uebernommen, die Koordinaten entfallen ersatzlos.
 *
 * NICHT DABEI: achSILEX -- machine_silex gibt es im Port nicht. Und achChicagoPile, dessen
 * Symbol pile_rod_plutonium im Port die Metadatensorte PU239 von PILE_ROD ist; ein
 * Erfolg auf eine Metadatensorte braucht ein DataComponentPredicate und kommt mit der
 * naechsten Gruppe.
 *
 * SAVE NIMMT EINE ResourceLocation, KEINEN STRING. Das hat Runde 247 einen
 * CI-Durchgang gekostet: NeoForges Erweiterung von Advancement.Builder.save nimmt
 * (Consumer, ResourceLocation, ExistingFileHelper); Vanillas eigene nimmt einen
 * String. Kein Tor kann das sehen -- die Tore laufen ohne Minecraft auf der Platte.
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
                    .save(speichern, NuclearTechMod.withDefaultNamespace("root"), helper);

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

            /*
             * DIE ERSTEN GETRIGGERTEN, Runde 248. Fuenf von zweiunddreissig -- genau die,
             * deren Ausloeser im Port schon steht. Sie haengen alle an der Wurzel, weil das
             * Original sie ohne Vorgaenger anmeldet; nur der Strahlentod folgt der
             * Strahlenkrankheit.
             */
            erfolg(speichern, helper, wurzel, "red_room", NtmItems.KEY_RED.get(), true, "red_room");
            AdvancementHolder radPoison = erfolg(speichern, helper, wurzel, "rad_poison", NtmItems.GEIGER_COUNTER.get(), false, "rad_poison");
            erfolg(speichern, helper, radPoison, "rad_death", Items.SKELETON_SKULL, true, "rad_death");
            erfolg(speichern, helper, wurzel, "no9", NtmItems.NO9.get(), false, "no9");

            /* Das Original nimmt hier sein achievement_icon mit der Metadatensorte GOFISH --
             * ein Symbolgegenstand, den es nur fuer Erfolge gibt und den der Port nicht hat.
             * Statt ihn nachzubauen zeigt der Erfolg die Waffe, die ihn verleiht. */
            erfolg(speichern, helper, wurzel, "go_fish", NtmItems.BOLTGUN.get(), true, "go_fish");

            /*
             * VIER WEITERE, Runde 249 -- die Katastrophen. Drei von ihnen haengen im
             * Original an einem Symbolgegenstand, den der Port nicht hat; sie bekommen
             * einen, der dasselbe meint:
             *
             *   bucket_mud   -> das Watz-Pellet (der Eimer Schlamm fehlt im Port)
             *   coin_creeper -> Vanillas Creeperkopf (die Bossmuenzen fehlen alle)
             *   nuke_boy     -> nuke_little_boy, derselbe Block unter dem vollen Namen
             */
            erfolg(speichern, helper, rbmk, "rbmk_boom", NtmItems.DEBRIS_FUEL.get(), true, "rbmk_boom");
            erfolg(speichern, helper, wurzel, "watz_boom", NtmItems.WATZ_PELLET.get(), true, "watz_boom");
            erfolg(speichern, helper, wurzel, "boss_creeper", Items.CREEPER_HEAD, false, "boss_creeper");
            erfolg(speichern, helper, polymer, "manhattan", NtmBlocks.NUKE_LITTLE_BOY.get(), true, "manhattan");

            /*
             * ELF WEITERE, Runde 252 -- acht getriggerte und drei aus dem Inventar. Die acht
             * sind genau die, deren Ausloeser diese Runde verdrahtet hat:
             *
             *   horizons_start  Satellit in der Umlaufbahn (SatelliteHorizons.onOrbit)
             *   horizons_end    Tom faellt vom Himmel (SatelliteHorizons.theHorizons)
             *   zirnox_boom     Zirnox zerlegt sich (ReactorZirnoxBlockEntity.meltdown)
             *   omega12         Digamma-Teilchen im Detektor (MachinePADetectorBlockEntity)
             *   hidden          verseuchter Creeper, erschlagen von einem Gueterwagen
             *   stratum         der erste Gneis
             *   slimeball       ein aufgehobener Schleimball
             *
             * Symbole, die der Port nicht hat, bekommen eines, das dasselbe meint:
             *   achievement_icon ACID          -> der Schleimball selbst
             *   achievement_icon QUESTIONMARK  -> der Gueterwagen-Sprengkopf
             */
            erfolg(speichern, helper, wurzel, "slimeball", Items.SLIME_BALL, false, "slimeball");
            erfolg(speichern, helper, wurzel, "stratum", NtmBlocks.STONE_GNEISS.get(), true, "stratum");
            erfolg(speichern, helper, wurzel, "hidden", NtmItems.MP_WARHEAD_15_BOXCAR.get(), false, "hidden");
            erfolg(speichern, helper, wurzel, "omega12", NtmItems.PARTICLE_DIGAMMA.get(), true, "omega12");
            erfolg(speichern, helper, centrifuge, "zirnox_boom", NtmItems.DEBRIS_ELEMENT.get(), true, "zirnox_boom");

            AdvancementHolder horizonsStart = erfolg(speichern, helper, wurzel, "horizons_start", NtmItems.SAT_GERALD.get(), false, "horizons_start");
            AdvancementHolder horizonsEnd = erfolg(speichern, helper, horizonsStart, "horizons_end", NtmItems.SAT_GERALD.get(), false, "horizons_end");

            /*
             * DREI OHNE AUSLOESER, die trotzdem gehen: das Original meldet sie ebenfalls
             * ohne triggerAchievement an, ihr Symbolgegenstand im Inventar genuegt.
             *
             * DER UNMOEGLICHE ERFOLG ist im Original auf "nothing" gesetzt -- einen
             * Gegenstand, den niemand bekommt. Das ist der Witz, und er bleibt.
             */
            erfolg(speichern, helper, horizonsEnd, "horizons_bonus", NtmItems.SAT_GERALD.get(), true);
            erfolg(speichern, helper, wurzel, "sacrifice", NtmItems.BURNT_BARK.get(), true);
            erfolg(speichern, helper, wurzel, "impossible", NtmItems.NOTHING.get(), true);
        }

        /**
         * Ein Erfolg der Fortschrittskette: Symbolgegenstand im Inventar, fertig.
         *
         * @param besonders die .setSpecial()-Erfolge des Originals. In 1.21 heisst das
         *                  AdvancementType.CHALLENGE -- derselbe gezackte Rahmen.
         */
        private static AdvancementHolder erfolg(Consumer<AdvancementHolder> speichern, ExistingFileHelper helper,
                AdvancementHolder vorgaenger, String name, ItemLike symbol, boolean besonders) {

            return bauen(speichern, helper, vorgaenger, name, symbol, besonders,
                    "has_item", InventoryChangeTrigger.TriggerInstance.hasItems(symbol));
        }

        /**
         * Ein Erfolg, der an einer Marke haengt statt am Inventar -- das Gegenstueck zu
         * triggerAchievement. Die Kennung muss mit der im Aufruf uebereinstimmen.
         */
        private static AdvancementHolder erfolg(Consumer<AdvancementHolder> speichern, ExistingFileHelper helper,
                AdvancementHolder vorgaenger, String name, ItemLike symbol, boolean besonders, String kennung) {

            return bauen(speichern, helper, vorgaenger, name, symbol, besonders, "marke",
                    NtmCriteria.MARKE.get().createCriterion(
                            new NtmCriteria.MarkeTrigger.Bedingung(Optional.empty(), kennung)));
        }

        private static AdvancementHolder bauen(Consumer<AdvancementHolder> speichern, ExistingFileHelper helper,
                AdvancementHolder vorgaenger, String name, ItemLike symbol, boolean besonders,
                String kriteriumsname, Criterion<?> kriterium) {

            return Advancement.Builder.advancement()
                    .parent(vorgaenger)
                    .display(symbol,
                            Component.translatable("advancements.hbmsntm." + name + ".title"),
                            Component.translatable("advancements.hbmsntm." + name + ".description"),
                            null,
                            besonders ? AdvancementType.CHALLENGE : AdvancementType.TASK,
                            true, true, false)
                    .addCriterion(kriteriumsname, kriterium)
                    .save(speichern, NuclearTechMod.withDefaultNamespace(name), helper);
        }
    }
}
