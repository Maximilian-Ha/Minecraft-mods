package com.hbm.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.food.DrinkItem;
import com.hbm.items.machine.BatteryPackItem.BatteryPackType;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Die Erfolge, Runde 247.
 *
 * STAND NACH RUNDE 258: 46 der 61 Namen des Originals, in 47 Knoten -- der eigene Wurzelknoten
 * kommt hinzu, den 1.21 verlangt und den das Original nicht hat. (Runde 252 schrieb "40 von
 * 61" und zaehlte diese Wurzel mit; gemessen waren es 39.)
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
 * den Start einer Sojus.
 *
 * STAND NACH RUNDE 286: 61 von 61. Die Liste ist geschlossen. Der letzte -- SILEX -- hing an
 * der Maschine selbst, und die steht seit dieser Runde; wie im Original faellt er beim BAU
 * (AchievementHandler.craftingAchievements), nicht beim Betrieb.
 *
 * DIE VIER BOSSE KAMEN IN DEN RUNDEN 280 BIS 283: der Maskenmann, das Strahlenbiest, der
 * Wurm und das UFO.
 *
 * (Diese Aufzaehlung stand zwischen Runde 272 und 275 halb zerbrochen da -- ein Glied wurde
 * entfernt, ohne den Rest zu lesen. Seit Runde 275 steht hier die gemessene Liste.)
 *
 * BERICHTIGT IN RUNDE 284: hier stand, die SILEX pruefe hasLaser. Das Feld gibt es, aber
 * NIEMAND SETZT ES -- gemessen mit git grep ueber das ganze Original: die Zeile
 * TileEntitySILEX.java:39 ist die einzige Fundstelle. Was sie wirklich prueft, ist ihr Feld
 * mode, und das setzt ihr der FEL von aussen (TileEntityFEL.java:84 ff.).
 *
 * DIE SOJUS STAND HIER ZU UNRECHT (berichtigt in Runde 277) -- das dritte Mal, dass die
 * Messung aus Runde 270 danebenlag. Sie ist als Soyuz samt Startrampe, Satellitenregister und
 * Abgasfahne portiert; gefehlt haben nur zwei Ausloeserzeilen und die Schadensart der
 * Abgasfahne. Der zweite Erfolg haengt am Plueschpony als Nutzlast.
 *
 * DER SPEER KAM IN RUNDE 276 und brachte digammaKauaiMoho und digammaUpOnTop.
 *
 * DIE SCHWEFELSAEURE KAM IN RUNDE 273: sie ist der einzige Fluidblock des Originals mit
 * Schadensquelle, und ihr Erfolg faellt, wenn sich darin ein Schleimball aufloest.
 *
 * DAS MESSER KAM IN RUNDE 272 (someWounds). Es ist ein Ruestungsmodul, und das Modulsystem
 * stand schon -- gefehlt hat nur die Klasse.
 *
 * DER RADIUMKAFFEE STAND HIER ZU UNRECHT (berichtigt in Runde 271). Er ist im Port laengst da
 * -- aber als DrinkType.COFFEE_RADIUM, ein Meta-Gegenstand ohne eigenen Registriereintrag. Die
 * Messung in Runde 270 suchte nur nach Registriernamen in NtmItems und hat ihn deshalb
 * uebersehen. Sein Erfolg haengt jetzt an einer Marke in LAMBDA_COFFEE_RADIUM.
 *
 * DREI DER EINUNDSECHZIG SIND IM ORIGINAL SELBST UNERREICHBAR: tasteofblood, c20_5 und
 * digammaUpOnTop stehen in keinem triggerAchievement-Aufruf und in keiner Zeile des
 * AchievementHandlers. Alle drei stehen hier -- digammaUpOnTop seit Runde 276, als sein
 * Vorgaenger digammaKauaiMoho mit dem Speer dazukam.
 *
 * GITTERPLAETZE GIBT ES IN 1.21 NICHT MEHR. Das Original setzt jeden Erfolg auf eine
 * Koordinate (x, y); 1.21 legt den Baum selbst aus der Vorgaengerkette. Die Kette ist
 * uebernommen, die Koordinaten entfallen ersatzlos.
 *
 * NICHT MEHR OFFEN: achSILEX stand hier lange als "machine_silex gibt es im Port nicht". Seit
 * Runde 286 gibt es sie. achPotato kam in Runde 274
 * dazu: die Kartoffelbatterien sind dort in BatteryPackType nachgetragen. achChicagoPile steht seit Runde 258 dabei: sein Symbol war der
 * Grund fuer die Zurueckstellung, nicht sein Ausloeser, und ein Symbol laesst sich
 * ersetzen.
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
            AdvancementHolder acidizer = erfolg(speichern, helper, desh, "acidizer", NtmBlocks.MACHINE_CRYSTALLIZER.get(), false);

            // DIE SILEX, RUNDE 286. Der letzte Erfolg der Liste. Im Original haengt er an
            // achAcidizer und wird beim BAU vergeben (AchievementHandler.craftingAchievements),
            // nicht beim Betrieb -- das Gegenstueck dazu ist hier wie ueberall das Stueck im
            // Inventar.
            erfolg(speichern, helper, acidizer, "silex", NtmBlocks.MACHINE_SILEX.get(), false);

            // DIE SOJUS, RUNDE 277. Beide haengen im Original an achDesh und tragen beide
            // setSpecial(). Die Symbole sind die des Originals: die Ofenkartoffel fuer den,
            // der in der Abgasfahne steht, die Rakete fuer den, der ein Plueschpony in den
            // Orbit schiesst.
            erfolg(speichern, helper, desh, "soyuz", Items.BAKED_POTATO, true, "soyuz");
            erfolg(speichern, helper, desh, "space", NtmItems.MISSILE_SOYUZ.get(), true, "space");

            AdvancementHolder centrifuge = erfolg(speichern, helper, polymer, "centrifuge", NtmBlocks.MACHINE_CENTRIFUGE.get(), false);
            erfolg(speichern, helper, centrifuge, "technetium", NtmItems.INGOT_TCALLOY.get(), false);
            /* Der Radiumkaffee ist ein Meta-Gegenstand, kein eigener Registriereintrag --
             * darum ein Stapel statt eines ItemLike. Genau das hat ihn in Runde 270 als
             * "fehlt im Port" erscheinen lassen: die Suche sah nur NtmItems. */
            erfolg(speichern, helper, centrifuge, "radium",
                    MetaHelper.newStack(NtmItems.DRINK, DrinkItem.DrinkType.COFFEE_RADIUM), true, "radium");

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
            erfolg(speichern, helper, wurzel, "boss_maskman", NtmItems.COIN_MASKMAN.get(), true, "boss_maskman");
            erfolg(speichern, helper, wurzel, "boss_meltdown", NtmItems.COIN_RADIATION.get(), true, "boss_meltdown");
            erfolg(speichern, helper, wurzel, "boss_worm", NtmItems.COIN_WORM.get(), true, "boss_worm");
            erfolg(speichern, helper, wurzel, "boss_ufo", NtmItems.COIN_UFO.get(), true, "boss_ufo");
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
            AdvancementHolder slimeball = erfolg(speichern, helper, wurzel, "slimeball", Items.SLIME_BALL, false, "slimeball");

            /* Die Saeure haengt im Original am Schleimball -- und das ist woertlich gemeint:
             * der Erfolg faellt, wenn sich ein Schleimball darin aufloest. Als Sinnbild nimmt
             * das Original achievement_icon mit dem Merkmal BALLS; den Meta-Gegenstand hat der
             * Port nicht, also steht dort der Schleimball selbst. Ein Sinnbild laesst sich
             * ersetzen, ein Ausloeser nicht. */
            erfolg(speichern, helper, slimeball, "sulfuric", Items.SLIME_BALL, true, "sulfuric");

            /* Das Messer haengt im Original an keinem Vorgaenger (initIndependentStat ohne
             * Elternteil); in 1.21 braucht jeder Erfolg einen, also die Wurzel. Kein
             * Herausforderungs-Rahmen -- setSpecial fehlt dort. */
            erfolg(speichern, helper, wurzel, "some_wounds", NtmItems.INJECTOR_KNIFE.get(), false, "some_wounds");

            /* Die grosse Kartoffelbatterie. Im Original ein Bau-Erfolg ohne Vorgaenger, mit
             * Herausforderungs-Rahmen; die Marke setzt BatteryPackItem, sobald sie im
             * Inventar liegt -- das ist dasselbe, was Forges Bau-Erkennung dort abfaengt. */
            /* Die beiden Fiend-Erfolge, Runde 275. Im Original ohne Vorgaenger und mit
             * Herausforderungs-Rahmen; die Marke setzt SpecialSwordItem, wenn Jacke und
             * passende Waffe zusammenkommen. */
            erfolg(speichern, helper, wurzel, "fiend", NtmItems.SHIMMER_SLEDGE.get(), true, "fiend");
            erfolg(speichern, helper, wurzel, "fiend2", NtmItems.SHIMMER_AXE.get(), true, "fiend2");

            erfolg(speichern, helper, wurzel, "potato",
                    MetaHelper.newStack(NtmItems.BATTERY_PACK.get(), 1, BatteryPackType.BATTERY_POTATOS), true, "potato");
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
            /*
             * FUENF WEITERE, Runde 258.
             *
             * DIE DIGAMMA-KETTE -- sehen, fuehlen, wissen. Das Original haengt sie an drei
             * Schwellen desselben Wertes: ueber null, ab zwei, ab zehn Drx. Die Schwellen
             * stehen jetzt in EntityEffectHandler.handleDigamma, dort, wo der Port den Wert
             * ohnehin jeden Tick liest.
             *
             * Ihre Symbole sind im Original drei Metadatensorten von achievement_icon, die
             * der Port nicht hat; alle drei zeigen deshalb das Digamma-Teilchen selbst.
             *
             * INFERNO haengt am Tank voll Brennbarem, den ein Zeta-Bomblet zerlegt.
             *
             * CHICAGO PILE kommt ohne Marke aus: das Original verleiht ihn beim Bauen von
             * billet_pu_mix, und genau den Gegenstand gibt es im Port. Sein Symbol ist im
             * Original pile_rod_plutonium -- im Port eine Metadatensorte von PILE_ROD, die
             * ein DataComponentPredicate braeuchte. Der Erfolg zeigt stattdessen den
             * Gegenstand, der ihn verleiht.
             */
            AdvancementHolder digammaSee = erfolg(speichern, helper, wurzel, "digamma_see", NtmItems.PARTICLE_DIGAMMA.get(), false, "digamma_see");
            AdvancementHolder digammaFeel = erfolg(speichern, helper, digammaSee, "digamma_feel", NtmItems.PARTICLE_DIGAMMA.get(), false, "digamma_feel");
            AdvancementHolder digammaKnow = erfolg(speichern, helper, digammaFeel, "digamma_know", NtmItems.PARTICLE_DIGAMMA.get(), true, "digamma_know");

            /* Der Speer, Runde 276. Die Marke setzt DigammaSpear, waehrend er sinkt.
             *
             * UP_ON_TOP IST DER DRITTE DER IM ORIGINAL UNERREICHBAREN -- er steht in keinem
             * triggerAchievement-Aufruf und in keiner Zeile des AchievementHandlers. Er stand
             * bis hierher nicht hier, weil ihm sein Vorgaenger fehlte; jetzt hat er ihn.
             *
             * Beide nehmen im Original achievement_icon mit einem eigenen Merkmal als Sinnbild.
             * Den Meta-Gegenstand hat der Port nicht, also steht dort das Digamma-Teilchen --
             * dasselbe Sinnbild wie bei den drei Erfolgen davor. */
            AdvancementHolder kauaiMoho = erfolg(speichern, helper, digammaKnow, "digamma_kauai_moho", NtmItems.PARTICLE_DIGAMMA.get(), true, "digamma_kauai_moho");
            unerreichbar(speichern, helper, kauaiMoho, "digamma_up_on_top", NtmItems.PARTICLE_DIGAMMA.get(), true);

            erfolg(speichern, helper, wurzel, "inferno", NtmItems.CANISTER_NAPALM.get(), true, "inferno");
            erfolg(speichern, helper, centrifuge, "chicago_pile", NtmItems.BILLET_PU_MIX.get(), false);

            /*
             * DIE ZWEI UNERREICHBAREN, ebenfalls Runde 258. Nachgemessen ueber den ganzen
             * Quelltext des Originals: "tasteofblood" und "c20_5" stehen zwar auf seiner
             * Erfolgsseite, aber in keinem triggerAchievement-Aufruf UND in keiner Zeile von
             * AchievementHandler. Sie sind dort genauso unerreichbar wie "impossible" -- das
             * ist kein Versehen des Ports, sondern der Zustand des Originals.
             *
             * Sie bekommen deshalb dasselbe Kriterium wie "impossible": den Gegenstand
             * NOTHING, den niemand bekommt. Eine Marke mit einer Kennung, die nirgends
             * gefeuert wird, waere dasselbe in umstaendlich -- und sie saehe aus wie eine
             * vergessene Verdrahtung.
             *
             * Die Symbole sind im Original Metadatensorten, die der Port nicht hat:
             *   fluid_icon ASCHRAB           -> das Antischrabidium-Teilchen, derselbe Stoff
             *   achievement_icon QUESTIONMARK -> das Schwarze Buch, passend zum "Kapitel"
             */
            unerreichbar(speichern, helper, wurzel, "taste_of_blood", NtmItems.PARTICLE_ASCHRAB.get(), true);
            unerreichbar(speichern, helper, wurzel, "c20_5", NtmItems.BOOK_OF_.get(), true);

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
         * Ein Erfolg, den niemand bekommen kann: sein Kriterium ist NOTHING, der Gegenstand
         * ohne Rezept und ohne Fundort. So steht er im Original auch da -- ohne Ausloeser und
         * ohne Eintrag im AchievementHandler. Das Symbol bleibt frei waehlbar, damit der
         * Erfolg im Baum das zeigt, wovon er handelt.
         */
        private static AdvancementHolder unerreichbar(Consumer<AdvancementHolder> speichern, ExistingFileHelper helper,
                AdvancementHolder vorgaenger, String name, ItemLike symbol, boolean besonders) {

            return bauen(speichern, helper, vorgaenger, name, symbol, besonders,
                    "has_item", InventoryChangeTrigger.TriggerInstance.hasItems(NtmItems.NOTHING.get()));
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

        /** Wie oben, aber mit einem fertigen Stapel als Sinnbild -- fuer Meta-Gegenstaende. */
        private static AdvancementHolder erfolg(Consumer<AdvancementHolder> speichern, ExistingFileHelper helper,
                AdvancementHolder vorgaenger, String name, ItemStack symbol, boolean besonders, String kennung) {

            return Advancement.Builder.advancement()
                    .parent(vorgaenger)
                    .display(symbol,
                            Component.translatable("advancements.hbmsntm." + name + ".title"),
                            Component.translatable("advancements.hbmsntm." + name + ".description"),
                            null,
                            besonders ? AdvancementType.CHALLENGE : AdvancementType.TASK,
                            true, true, false)
                    .addCriterion("marke", NtmCriteria.MARKE.get().createCriterion(
                            new NtmCriteria.MarkeTrigger.Bedingung(Optional.empty(), kennung)))
                    .save(speichern, NuclearTechMod.withDefaultNamespace(name), helper);
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
