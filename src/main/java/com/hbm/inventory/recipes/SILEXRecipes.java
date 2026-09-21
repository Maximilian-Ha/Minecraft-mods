package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.FelCrystalItem.Wellenlaenge;
import com.hbm.items.special.NuclearWasteItem;
import com.hbm.items.special.NuclearWasteItem.WasteClass;
import com.hbm.main.NuclearTechMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.SILEXRecipes.
 *
 * Die SILEX trennt Isotope mit Licht. Was sie dabei herausholt, steht hier: 91 Rezeptzeilen,
 * 51 davon in der Fuenferschleife der Brennstoffpellets -- zusammen 295 Eintraege in der Karte.
 *
 * DREI ZAHLEN MACHEN EIN REZEPT. Die erste sagt, wieviel Loesung ein Stueck des Eingangs
 * ergibt; die Maschine nimmt dafuer Peroxid aus ihrem Tank und haelt bis zu 16000 Einheiten
 * Loesung vor. Die zweite sagt, wieviel davon ein einzelner Ausgang kostet. Die dritte ist die
 * Wellenlaenge: der Strahl des FEL muss mindestens so weit oben liegen, und liegt er hoeher,
 * laeuft die Maschine schneller. Deshalb ist ein Digamma-Kristall auch dann etwas wert, wenn
 * man nur Uran trennt.
 *
 * DIE AUSGABEN SIND GEWICHTET, ABER ES WIRD NICHT GEWUERFELT. Das Original zaehlt mit einem
 * festen Primzahlschritt durch die Gewichtsleiter -- wer eine Tonne unangereichertes Uran
 * durchlaesst, bekommt am Ende genau die 86:10:2:2, die im Rezept stehen. Die Verteilung ist
 * damit eine Zusage, keine Hoffnung. Das Durchzaehlen gehoert der Maschine, nicht den Rezepten.
 *
 * ABWEICHUNG: das Original findet einen Teil seiner Eingaenge ueber das Erzwoerterbuch --
 * "dustUranium" fuehrt auf das Rezept des Uranbarrens. Ein Erzwoerterbuch gibt es auf 1.21
 * nicht. An seine Stelle tritt dieselbe Uebersetzungstabelle, die das Original ohnehin schon
 * fuer Gegenstaende fuehrt: Uranpulver zeigt auf den Uranbarren, Plutoniumpulver auf den
 * Plutoniumbarren. Fuer den Spieler bleibt alles, wie es war.
 *
 * ABWEICHUNG: das Original fuellt seine Uebersetzungstabellen in register(), also nur dann,
 * wenn keine Rezeptdatei des Nutzers vorliegt -- mit eigener Rezeptdatei verliert es die
 * Fluessigkeitszuordnung und die kleinen Abfallhaufen. Hier stehen sie in registerPost(), das
 * in beiden Faellen laeuft.
 *
 * ABWEICHUNG: rbmk_pellet_hep239 heisst im Port RBMK_PELLET_HEP, ore_tikite und fallout sind
 * Bloecke statt Gegenstaende, und "undefined" ist NOTHING. Die Fullerenasche gab es bis eben
 * nicht -- sie entsteht nur hier, im letzten Rezept dieser Liste.
 */
public class SILEXRecipes extends SerializableRecipe {

    public static final Map<ComparableStack, SILEXRecipe> recipes = new LinkedHashMap<>();

    /**
     * Was beim Nachschlagen durch etwas anderes ersetzt wird. Zwei Gruende gibt es dafuer:
     * eine Fluessigkeit soll dasselbe Rezept fahren wie der feste Stoff (Uranhexafluorid wie
     * der Uranbarren), oder ein Pulver soll dasselbe fahren wie sein Barren.
     */
    private static final Map<ComparableStack, ComparableStack> itemTranslation = new HashMap<>();

    /**
     * Die kleinen Abfallhaufen haben kein eigenes Rezept. Sie fahren das des grossen, bekommen
     * aber nur ein Neuntel der Loesung -- 900 wird zu 100.
     */
    private static final Map<Item, Item> tinyWasteTranslation = new HashMap<>();

    /** Eine gewichtete Ausgabe. Das Gewicht ist ein Anteil, keine Wahrscheinlichkeit. */
    public record Output(ItemStack stack, int weight) { }

    public static class SILEXRecipe {

        public final int fluidProduced;
        public final int fluidConsumed;
        public final Wellenlaenge laserStrength;
        public final List<Output> outputs = new ArrayList<>();

        public SILEXRecipe(int fluidProduced, int fluidConsumed, Wellenlaenge laserStrength) {
            this.fluidProduced = fluidProduced;
            this.fluidConsumed = fluidConsumed;
            this.laserStrength = laserStrength;
        }

        public SILEXRecipe addOut(ItemStack stack, int weight) {
            this.outputs.add(new Output(stack, weight));
            return this;
        }

        public SILEXRecipe addOut(ItemLike item, int weight) { return addOut(new ItemStack(item), weight); }
        public SILEXRecipe addOut(ItemLike item, int count, int weight) { return addOut(new ItemStack(item, count), weight); }
    }

    @Override
    public void registerDefaults() {

        /*
         * BARREN UND KRISTALLE -- der gerade Weg. Ein Barren gibt 900 Loesung, ein Ausgang
         * kostet 100: neun Nuggets je Barren, und die Verteilung steht in den Gewichten.
         */
        register(NtmItems.INGOT_URANIUM.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_U235.get(), 1)
                .addOut(NtmItems.NUGGET_U238.get(), 11);

        register(NtmItems.INGOT_PU_MIX.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_PU239.get(), 6)
                .addOut(NtmItems.NUGGET_PU240.get(), 3);

        register(NtmItems.INGOT_AM_MIX.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_AM241.get(), 3)
                .addOut(NtmItems.NUGGET_AM242.get(), 6);

        register(NtmItems.INGOT_PLUTONIUM.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_PU238.get(), 3)
                .addOut(NtmItems.NUGGET_PU239.get(), 4)
                .addOut(NtmItems.NUGGET_PU240.get(), 2);

        register(NtmItems.INGOT_SCHRARANIUM.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_SCHRABIDIUM.get(), 4)
                .addOut(NtmItems.NUGGET_URANIUM.get(), 3)
                .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 2);

        register(NtmItems.INGOT_AUSTRALIUM.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_AUSTRALIUM_LESSER.get(), 5)
                .addOut(NtmItems.NUGGET_AUSTRALIUM_GREATER.get(), 1);

        /* Der Kristall bringt mehr heraus als der Barren -- dafuer braucht er ultraviolett. */
        register(NtmItems.CRYSTAL_SCHRARANIUM.get(), 900, 100, Wellenlaenge.UV)
                .addOut(NtmItems.NUGGET_SCHRABIDIUM.get(), 5)
                .addOut(NtmItems.NUGGET_URANIUM.get(), 2)
                .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 2);

        register(NtmBlocks.ORE_TIKITE.get(), 900, 100, Wellenlaenge.UV)
                .addOut(NtmItems.POWDER_PLUTONIUM.get(), 2)
                .addOut(NtmItems.POWDER_COBALT.get(), 3)
                .addOut(NtmItems.POWDER_NIOBIUM.get(), 3)
                .addOut(NtmItems.POWDER_NITAN_MIX.get(), 2);

        register(NtmItems.CRYSTAL_TRIXITE.get(), 1200, 100, Wellenlaenge.UV)
                .addOut(NtmItems.POWDER_PLUTONIUM.get(), 2)
                .addOut(NtmItems.POWDER_COBALT.get(), 3)
                .addOut(NtmItems.POWDER_NIOBIUM.get(), 3)
                .addOut(NtmItems.POWDER_NITAN_MIX.get(), 1)
                .addOut(NtmItems.POWDER_SPARK_MIX.get(), 1);

        /* Lapis ist das billigste Rezept der Maschine: hundert Loesung, infrarot reicht. */
        register(Items.LAPIS_LAZULI, 100, 100, Wellenlaenge.IR)
                .addOut(NtmItems.SULFUR.get(), 4)
                .addOut(NtmItems.POWDER_ALUMINIUM.get(), 3)
                .addOut(NtmItems.POWDER_COBALT.get(), 3);

        /*
         * FLUESSIGKEITEN. Der Tank der Maschine nimmt sie unmittelbar an; 1000 Loesung
         * kosten 1000 zurueck, das ist kein Handel, sondern ein Filter.
         */
        register(fluid(Fluids.DEATH), 1000, 1000, Wellenlaenge.GAMMA)
                .addOut(NtmItems.POWDER_IMPURE_OSMIRIDIUM.get(), 1);

        register(fluid(Fluids.VITRIOL), 1000, 300, Wellenlaenge.IR)
                .addOut(NtmItems.POWDER_BROMINE.get(), 5)
                .addOut(NtmItems.POWDER_IODINE.get(), 5)
                .addOut(NtmItems.POWDER_IRON.get(), 5)
                .addOut(NtmItems.SULFUR.get(), 15);

        /* Rotschlamm: der Abraum der Aluminiumkette, und hier wird er zur Fundgrube. */
        register(fluid(Fluids.REDMUD), 300, 50, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.POWDER_ALUMINIUM.get(), 10)
                .addOut(NtmItems.POWDER_NEODYMIUM_TINY.get(), 3, 5)
                .addOut(NtmItems.POWDER_BORON_TINY.get(), 3, 5)
                .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 5)
                .addOut(NtmItems.POWDER_IRON.get(), 20)
                .addOut(NtmItems.POWDER_TITANIUM.get(), 15)
                .addOut(NtmItems.POWDER_SODIUM.get(), 10);

        /*
         * DIE PELLETS. Jedes abgebrannte Pellet traegt seinen Abbrand als Zahl von 0 bis 4,
         * und ab fuenf ist es zusaetzlich mit Xenon vergiftet. Beide Reihen fahren dasselbe
         * Rezept, nur dass die vergiftete oben ein Xenonpulver abwirft und dafuer ein Nugget
         * weniger gibt.
         *
         * JE WEITER ABGEBRANNT, DESTO WENIGER BRENNSTOFF UND DESTO MEHR ABFALL -- das ist die
         * ganze Rechnung. Die Zahlen sind die des Originals, bis auf die letzte Stelle.
         */
        for(int i = 0; i < 5; i++) {

            /* Unangereichertes Uran brennt am laengsten unter der Schwelle und bruetet
             * deshalb am meisten Plutonium; erst ab dem dritten Abbrand ist es Mischplutonium. */
            pellet(NtmItems.RBMK_PELLET_UEU.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_URANIUM.get(), 86 - i * 11)
                    .addOut(i < 2 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 10 + i * 3)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 2 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 2 + 5 * i);

            pellet(NtmItems.RBMK_PELLET_UEU.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_URANIUM.get(), 86 - i * 11)
                    .addOut(i < 2 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 10 + i * 3)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 2 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 1 + 5 * i);

            pellet(NtmItems.RBMK_PELLET_MEU.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_URANIUM_FUEL.get(), 84 - i * 16)
                    .addOut(i < 1 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 6 + i * 4)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 4 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 6 + 7 * i);

            pellet(NtmItems.RBMK_PELLET_MEU.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_URANIUM_FUEL.get(), 83 - i * 16)
                    .addOut(i < 1 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 6 + i * 4)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 4 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 6 + 7 * i);

            pellet(NtmItems.RBMK_PELLET_HEU233.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_U233.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM233), 4 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM233), 6 + 12 * i);

            pellet(NtmItems.RBMK_PELLET_HEU233.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_U233.get(), 89 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM233), 4 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM233), 6 + 12 * i);

            pellet(NtmItems.RBMK_PELLET_HEU235.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_U235.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 4 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 6 + 12 * i);

            pellet(NtmItems.RBMK_PELLET_HEU235.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_U235.get(), 89 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 4 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 6 + 12 * i);

            /* Uranzirkoniumhydrid: drei Viertel des Pellets sind Zirkonium und kommen
             * unveraendert wieder heraus, egal wie lange es im Reaktor stand. */
            pellet(NtmItems.RBMK_PELLET_UZH.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 75)
                    .addOut(NtmItems.NUGGET_URANIUM_FUEL.get(), 20 - i * 4)
                    .addOut(NtmItems.NUGGET_PU_MIX.get(), 3 + i * 3)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 1 + i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 1 + i);

            pellet(NtmItems.RBMK_PELLET_UZH.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 75)
                    .addOut(NtmItems.NUGGET_URANIUM_FUEL.get(), 19 - i * 4)
                    .addOut(NtmItems.NUGGET_PU_MIX.get(), 3 + i * 3)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 1 + i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 1 + i);

            /* Thorium bruetet Uran-233 -- das ist der ganze Sinn des Thoriumzyklus, und hier
             * steht er als Zahl: aus 84 Brennstoff werden 6 bis 22 Uran-233. */
            pellet(NtmItems.RBMK_PELLET_THMEU.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_THORIUM_FUEL.get(), 84 - i * 20)
                    .addOut(NtmItems.NUGGET_U233.get(), 6 + i * 4)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.THORIUM), 10 + 16 * i);

            pellet(NtmItems.RBMK_PELLET_THMEU.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_THORIUM_FUEL.get(), 83 - i * 20)
                    .addOut(NtmItems.NUGGET_U233.get(), 6 + i * 4)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.THORIUM), 10 + 16 * i);

            pellet(NtmItems.RBMK_PELLET_LEP.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_PLUTONIUM_FUEL.get(), 84 - i * 14)
                    .addOut(i < 1 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 6 + i * 2)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 7 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM240), 3 + 4 * i);

            pellet(NtmItems.RBMK_PELLET_LEP.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_PLUTONIUM_FUEL.get(), 83 - i * 14)
                    .addOut(i < 1 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 6 + i * 2)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 7 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM240), 3 + 4 * i);

            pellet(NtmItems.RBMK_PELLET_MEP.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_PU_MIX.get(), 85 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 10 + 10 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM240), 5 + 5 * i);

            pellet(NtmItems.RBMK_PELLET_MEP.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_PU_MIX.get(), 84 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 10 + 10 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM240), 5 + 5 * i);

            pellet(NtmItems.RBMK_PELLET_HEP.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_PU239.get(), 85 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 15 + 20 * i);

            pellet(NtmItems.RBMK_PELLET_HEP.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_PU239.get(), 84 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 15 + 20 * i);

            pellet(NtmItems.RBMK_PELLET_HEP241.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_PU241.get(), 85 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM241), 15 + 20 * i);

            pellet(NtmItems.RBMK_PELLET_HEP241.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_PU241.get(), 84 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM241), 15 + 20 * i);

            pellet(NtmItems.RBMK_PELLET_MEN.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_NEPTUNIUM_FUEL.get(), 84 - i * 14)
                    .addOut(i < 1 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 6 + i * 2)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 4 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 6 + 7 * i);

            pellet(NtmItems.RBMK_PELLET_MEN.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_NEPTUNIUM_FUEL.get(), 83 - i * 14)
                    .addOut(i < 1 ? NtmItems.NUGGET_PU239.get() : NtmItems.NUGGET_PU_MIX.get(), 6 + i * 2)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 4 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 6 + 7 * i);

            pellet(NtmItems.RBMK_PELLET_HEN.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 4 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 6 + 12 * i);

            pellet(NtmItems.RBMK_PELLET_HEN.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 89 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 4 + 8 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 6 + 12 * i);

            /* Mischoxid ist das einzige Pellet mit drei Abfallsorten: es traegt Uran und
             * Plutonium nebeneinander, und beide hinterlassen ihre eigene Klasse. */
            pellet(NtmItems.RBMK_PELLET_MOX.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_MOX_FUEL.get(), 84 - i * 20)
                    .addOut(NtmItems.NUGGET_PU_MIX.get(), 6 + i * 4)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 2 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 3 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 5 + 8 * i);

            pellet(NtmItems.RBMK_PELLET_MOX.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_MOX_FUEL.get(), 83 - i * 20)
                    .addOut(NtmItems.NUGGET_PU_MIX.get(), 6 + i * 4)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.URANIUM235), 2 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.URANIUM235), 3 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.PLUTONIUM239), 5 + 8 * i);

            /* Australium zerfaellt nicht in Abfall, sondern in Blei -- es ist kein Kernbrennstoff
             * im ueblichen Sinn, und das sieht man seinen Ausgaengen an. */
            pellet(NtmItems.RBMK_PELLET_LEAUS.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_AUSTRALIUM_LESSER.get(), 90 - i * 20)
                    .addOut(NtmItems.NUGGET_LEAD.get(), 6 + 12 * i)
                    .addOut(NtmItems.NUGGET_PB209.get(), 4 + 8 * i);

            pellet(NtmItems.RBMK_PELLET_LEAUS.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_AUSTRALIUM_LESSER.get(), 89 - i * 20)
                    .addOut(NtmItems.NUGGET_LEAD.get(), 6 + 12 * i)
                    .addOut(NtmItems.NUGGET_PB209.get(), 4 + 8 * i);

            pellet(NtmItems.RBMK_PELLET_HEAUS.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_AUSTRALIUM_GREATER.get(), 90 - i * 20)
                    .addOut(NtmItems.NUGGET_AU198.get(), 5 + 10 * i)
                    .addOut(Items.GOLD_NUGGET, 3 + 6 * i)
                    .addOut(NtmItems.NUGGET_PB209.get(), 2 + 4 * i);

            pellet(NtmItems.RBMK_PELLET_HEAUS.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_AUSTRALIUM_GREATER.get(), 89 - i * 20)
                    .addOut(NtmItems.NUGGET_AU198.get(), 5 + 10 * i)
                    .addOut(Items.GOLD_NUGGET, 3 + 6 * i)
                    .addOut(NtmItems.NUGGET_PB209.get(), 2 + 4 * i);

            /* DIE DREI SCHRABIDIUMPELLETS HABEN SECHS AUSGAENGE, und das ist ihre Grenze: das
             * Original schreibt daneben, dass seine Rezeptanzeige nicht mehr als sechs zeigen
             * kann, und laesst deshalb bei der vergifteten Reihe das Xenon weg. Die Zahlen der
             * beiden Reihen sind darum gleich. Der Port koennte sieben zeigen -- er behaelt die
             * Zahlen trotzdem, weil sonst die vergiftete Reihe mehr herausgaebe als die reine. */
            pellet(NtmItems.RBMK_PELLET_LES.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_LES.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 2 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 2 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.SCHRABIDIUM), 1 + 2 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.SCHRABIDIUM), 1 + 2 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 4 + 8 * i);

            pellet(NtmItems.RBMK_PELLET_LES.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_LES.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 2 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 2 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.SCHRABIDIUM), 1 + 2 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.SCHRABIDIUM), 1 + 2 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 4 + 8 * i);

            pellet(NtmItems.RBMK_PELLET_MES.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_SCHRABIDIUM_FUEL.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 1 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 2 + 4 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.SCHRABIDIUM), 1 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.SCHRABIDIUM), 2 + 4 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 4 + 6 * i);

            pellet(NtmItems.RBMK_PELLET_MES.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_SCHRABIDIUM_FUEL.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 1 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 2 + 4 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.SCHRABIDIUM), 1 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.SCHRABIDIUM), 2 + 4 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 4 + 6 * i);

            pellet(NtmItems.RBMK_PELLET_HES.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_HES.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 1 + 2 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 1 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.SCHRABIDIUM), 2 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.SCHRABIDIUM), 4 + 6 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 2 + 4 * i);

            pellet(NtmItems.RBMK_PELLET_HES.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_HES.get(), 90 - i * 20)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.NEPTUNIUM), 1 + 2 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.NEPTUNIUM), 1 + 3 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_LONG_TINY, WasteClass.SCHRABIDIUM), 2 + 5 * i)
                    .addOut(waste(NtmItems.NUCLEAR_WASTE_SHORT_TINY, WasteClass.SCHRABIDIUM), 4 + 6 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 2 + 4 * i);

            /* Die fuenf letzten kennen kein Xenon -- Balefire, die beiden Antiwasserstoffgitter,
             * die Neutronenquellen aus Polonium und Radium. Ihre Pellets haben nur fuenf
             * Zustaende, und deshalb steht hier nur die untere Reihe. Balefire ist ausserdem
             * das einzige Pellet mit einem kleineren Ertrag: 400 statt 600 Loesung. */
            register(new ComparableStack(NtmItems.RBMK_PELLET_BALEFIRE.get(), 1, i), 400, 100, Wellenlaenge.UV)
                    .addOut(NtmItems.POWDER_BALEFIRE.get(), 90 - i * 20)
                    .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 10 + 20 * i);

            pellet(NtmItems.RBMK_PELLET_BALEFIRE_GOLD.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_AU198.get(), 90 - 20 * i)
                    .addOut(NtmItems.POWDER_BALEFIRE.get(), 10 + 20 * i);

            pellet(NtmItems.RBMK_PELLET_FLASHLEAD.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_AU198.get(), 44 - 10 * i)
                    .addOut(NtmItems.NUGGET_PB209.get(), 44 - 10 * i)
                    .addOut(NtmItems.NUGGET_BISMUTH.get(), 1 + 6 * i)
                    .addOut(NtmItems.NUGGET_MERCURY.get(), 1 + 6 * i)
                    .addOut(NtmItems.NUGGET_GH336.get(), 10 + 8 * i);

            pellet(NtmItems.RBMK_PELLET_PO210BE.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_POLONIUM.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_BERYLLIUM.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_LEAD.get(), 5 + 10 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 5 + 10 * i);

            pellet(NtmItems.RBMK_PELLET_PU238BE.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_PU238.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_BERYLLIUM.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_LEAD.get(), 3 + 5 * i)
                    .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 2 + 5 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 5 + 10 * i);

            pellet(NtmItems.RBMK_PELLET_PU238BE.get(), i + 5, Wellenlaenge.IR)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 1)
                    .addOut(NtmItems.NUGGET_PU238.get(), 44 - 10 * i)
                    .addOut(NtmItems.NUGGET_BERYLLIUM.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_LEAD.get(), 3 + 5 * i)
                    .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 2 + 5 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 5 + 10 * i);

            pellet(NtmItems.RBMK_PELLET_RA226BE.get(), i, Wellenlaenge.IR)
                    .addOut(NtmItems.NUGGET_RA226.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_BERYLLIUM.get(), 45 - 10 * i)
                    .addOut(NtmItems.NUGGET_LEAD.get(), 3 + 5 * i)
                    .addOut(NtmItems.NUGGET_POLONIUM.get(), 2 + 5 * i)
                    .addOut(NtmItems.POWDER_COAL_TINY.get(), 5 + 10 * i);

            /* DAS DIGAMMA-PELLET GIBT NICHTS HER -- sechsmal nichts, damit die Anzeige sechs
             * Felder fuellt. Es ist der teuerste Weg zu gar nichts, den der Mod kennt, und er
             * braucht dafuer auch noch Gammalicht. */
            for(int meta : new int[] { i, i + 5 }) {
                SILEXRecipe drx = pellet(NtmItems.RBMK_PELLET_DRX.get(), meta, Wellenlaenge.GAMMA);
                for(int slot = 0; slot < 6; slot++) drx.addOut(NtmItems.NOTHING.get(), 1);
            }

            /* Die Zirkoniumbrueter: 150 Nuggets Zirkonium je Pellet, und darin gebruetet das,
             * was ihren Namen traegt. Bei Wismut und Plutonium-241 zahlt die vergiftete Reihe
             * das Xenon aus dem Zirkonium -- drei Xenonpulver gegen drei Nuggets. Beim
             * Americiumbrueter nicht: dort stehen in beiden Reihen 150, und das ist kein
             * Uebertragungsfehler, sondern steht so im Original. */
            pellet(NtmItems.RBMK_PELLET_ZFB_BISMUTH.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_URANIUM.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_PU241.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_BISMUTH.get(), 50 + i * 20)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 150);

            pellet(NtmItems.RBMK_PELLET_ZFB_BISMUTH.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 3)
                    .addOut(NtmItems.NUGGET_URANIUM.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_PU241.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_BISMUTH.get(), 50 + i * 20)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 147);

            pellet(NtmItems.RBMK_PELLET_ZFB_PU241.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_U235.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_PU240.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_PU241.get(), 50 + i * 20)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 150);

            pellet(NtmItems.RBMK_PELLET_ZFB_PU241.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 3)
                    .addOut(NtmItems.NUGGET_U235.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_PU240.get(), 50 - i * 10)
                    .addOut(NtmItems.NUGGET_PU241.get(), 50 + i * 20)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 147);

            pellet(NtmItems.RBMK_PELLET_ZFB_AM_MIX.get(), i, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.NUGGET_PU241.get(), 100 - i * 20)
                    .addOut(NtmItems.NUGGET_AM_MIX.get(), 50 + i * 20)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 150);

            pellet(NtmItems.RBMK_PELLET_ZFB_AM_MIX.get(), i + 5, Wellenlaenge.VISIBLE)
                    .addOut(NtmItems.POWDER_XE135_TINY.get(), 3)
                    .addOut(NtmItems.NUGGET_PU241.get(), 100 - i * 20)
                    .addOut(NtmItems.NUGGET_AM_MIX.get(), 50 + i * 20)
                    .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 150);
        }

        /*
         * DER ABFALL. Jede Klasse hat zwei Zustaende -- frisch und abgeklungen --, und die
         * SILEX macht aus beiden etwas: aus dem frischen die Spaltprodukte, die noch Wert
         * haben, aus dem abgeklungenen Blei, Wismut und Staub. Das ist das Ende der Kette.
         */
        registerWaste(NtmItems.NUCLEAR_WASTE_LONG, WasteClass.URANIUM235, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 20)
                .addOut(NtmItems.NUGGET_PU239.get(), 45)
                .addOut(NtmItems.NUGGET_PU240.get(), 20)
                .addOut(NtmItems.NUGGET_TECHNETIUM.get(), 15);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED, WasteClass.URANIUM235, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_LEAD.get(), 65)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 20)
                .addOut(NtmItems.DUST_TINY.get(), 15);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.URANIUM235, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_PU238.get(), 12)
                .addOut(NtmItems.POWDER_SR90_TINY.get(), 10)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 10)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 12)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 56);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.URANIUM235, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 10)
                .addOut(NtmItems.DUST_TINY.get(), 32)
                .addOut(NtmItems.NUGGET_LEAD.get(), 22)
                .addOut(NtmItems.NUGGET_U238.get(), 5)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 15)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 16);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG, WasteClass.URANIUM233, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_U235.get(), 15)
                .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 25)
                .addOut(NtmItems.NUGGET_PU239.get(), 45)
                .addOut(NtmItems.NUGGET_TECHNETIUM.get(), 15);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED, WasteClass.URANIUM233, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_LEAD.get(), 60)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 25)
                .addOut(NtmItems.DUST_TINY.get(), 15);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.URANIUM233, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_PU238.get(), 4)
                .addOut(NtmItems.POWDER_SR90_TINY.get(), 12)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 10)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 14)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 60);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.URANIUM233, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 12)
                .addOut(NtmItems.DUST_TINY.get(), 34)
                .addOut(NtmItems.NUGGET_LEAD.get(), 13)
                .addOut(NtmItems.NUGGET_U238.get(), 2)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 10)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 29);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.PLUTONIUM239, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_PU240.get(), 10)
                .addOut(NtmItems.NUGGET_PU241.get(), 25)
                .addOut(NtmItems.POWDER_SR90_TINY.get(), 2)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 5)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 6)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 52);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.PLUTONIUM239, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 2)
                .addOut(NtmItems.DUST_TINY.get(), 16)
                .addOut(NtmItems.NUGGET_LEAD.get(), 40)
                .addOut(NtmItems.NUGGET_U238.get(), 3)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 39);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.PLUTONIUM240, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_PU241.get(), 15)
                .addOut(NtmItems.NUGGET_NEPTUNIUM.get(), 5)
                .addOut(NtmItems.POWDER_SR90_TINY.get(), 2)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 5)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 7)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 66);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.PLUTONIUM240, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 2)
                .addOut(NtmItems.DUST_TINY.get(), 22)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 20)
                .addOut(NtmItems.NUGGET_LEAD.get(), 17)
                .addOut(NtmItems.NUGGET_U238.get(), 3)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 36);

        /* Plutonium-241 ist der einzige Abfall, der Americium hergibt -- und der einzige,
         * der dafuer sichtbares Licht braucht. */
        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.PLUTONIUM241, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_AM241.get(), 25)
                .addOut(NtmItems.NUGGET_AM242.get(), 35)
                .addOut(NtmItems.NUGGET_TECHNETIUM.get(), 5)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 3)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 7)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 25);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.PLUTONIUM241, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 60)
                .addOut(NtmItems.DUST_TINY.get(), 20)
                .addOut(NtmItems.NUGGET_LEAD.get(), 15)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 5);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG, WasteClass.THORIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_U233.get(), 40)
                .addOut(NtmItems.NUGGET_U235.get(), 35)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 25);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED, WasteClass.THORIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_LEAD.get(), 35)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 40)
                .addOut(NtmItems.DUST_TINY.get(), 15)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 10);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG, WasteClass.NEPTUNIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_U238.get(), 15)
                .addOut(NtmItems.NUGGET_PU239.get(), 40)
                .addOut(NtmItems.NUGGET_PU240.get(), 15)
                .addOut(NtmItems.NUGGET_TECHNETIUM.get(), 15)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 15);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED, WasteClass.NEPTUNIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_U238.get(), 16)
                .addOut(NtmItems.NUGGET_LEAD.get(), 55)
                .addOut(NtmItems.DUST_TINY.get(), 20)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 9);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.NEPTUNIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_PU238.get(), 40)
                .addOut(NtmItems.POWDER_SR90_TINY.get(), 7)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 5)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 8)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 40);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.NEPTUNIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_ZIRCONIUM.get(), 7)
                .addOut(NtmItems.DUST_TINY.get(), 29)
                .addOut(NtmItems.NUGGET_U238.get(), 2)
                .addOut(NtmItems.NUGGET_LEAD.get(), 45)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 17);

        /* Schrabidiumabfall ist der einzige, der auch abgeklungen noch fast alles hergibt --
         * Solinium, Euphemium, GH-336. Das Original schreibt daneben, dass ihm die passenden
         * Lanthanoide fehlen und Blei-209 mit Gold-198 schon hart an der Grenze sei. */
        registerWaste(NtmItems.NUCLEAR_WASTE_LONG, WasteClass.SCHRABIDIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_SOLINIUM.get(), 25)
                .addOut(NtmItems.NUGGET_EUPHEMIUM.get(), 18)
                .addOut(NtmItems.NUGGET_GH336.get(), 16)
                .addOut(NtmItems.NUGGET_TANTALIUM.get(), 8)
                .addOut(NtmItems.POWDER_NEODYMIUM_TINY.get(), 8)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 25);

        registerWaste(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED, WasteClass.SCHRABIDIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_SOLINIUM.get(), 20)
                .addOut(NtmItems.NUGGET_EUPHEMIUM.get(), 18)
                .addOut(NtmItems.NUGGET_GH336.get(), 15)
                .addOut(NtmItems.NUGGET_TANTALIUM.get(), 8)
                .addOut(NtmItems.POWDER_NEODYMIUM_TINY.get(), 8)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 31);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT, WasteClass.SCHRABIDIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_PB209.get(), 7)
                .addOut(NtmItems.NUGGET_AU198.get(), 7)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 5)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 5)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 76);

        registerWaste(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED, WasteClass.SCHRABIDIUM, Wellenlaenge.IR)
                .addOut(NtmItems.NUGGET_BISMUTH.get(), 7)
                .addOut(NtmItems.NUGGET_MERCURY.get(), 12)
                .addOut(NtmItems.POWDER_CERIUM_TINY.get(), 14)
                .addOut(NtmItems.POWDER_LANTHANIUM_TINY.get(), 15)
                .addOut(NtmItems.DUST_TINY.get(), 20)
                .addOut(NtmItems.NUCLEAR_WASTE_TINY.get(), 32);

        /*
         * DREI, DIE NIRGENDWO SONST HINGEHOEREN. Fallout ist zu neunzig Hundertsteln Staub
         * und enthaelt genau das, was eine Bombe hinterlaesst; Kies gibt vor allem Feuerstein;
         * und die Fullerenloesung wird zu Fullerenasche, dem einzigen Weg zum CFT-Barren.
         */
        register(NtmBlocks.FALLOUT.get(), 900, 100, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.DUST_TINY.get(), 90)
                .addOut(NtmItems.NUGGET_CO60.get(), 2)
                .addOut(NtmItems.POWDER_SR90_TINY.get(), 3)
                .addOut(NtmItems.POWDER_I131_TINY.get(), 1)
                .addOut(NtmItems.POWDER_CS137_TINY.get(), 3)
                .addOut(NtmItems.NUGGET_AU198.get(), 1);

        register(Blocks.GRAVEL, 1000, 250, Wellenlaenge.VISIBLE)
                .addOut(Items.FLINT, 80)
                .addOut(NtmItems.POWDER_BORON.get(), 5)
                .addOut(NtmItems.POWDER_LITHIUM.get(), 10)
                .addOut(NtmItems.FLUORITE.get(), 5);

        register(fluid(Fluids.FULLERENE), 1_000, 1_000, Wellenlaenge.VISIBLE)
                .addOut(NtmItems.POWDER_ASH_FULLERENE.get(), 1);
    }

    /**
     * Die Uebersetzungstabellen. Sie stehen hier und nicht in registerDefaults(), weil sie
     * auch dann gebraucht werden, wenn der Nutzer eine eigene Rezeptdatei mitbringt.
     */
    @Override
    public void registerPost() {

        itemTranslation.clear();
        tinyWasteTranslation.clear();

        /* Uranhexafluorid und Plutoniumhexafluorid fahren das Rezept ihres Barrens -- so
         * kommt die Gaszentrifuge unmittelbar an die SILEX. */
        itemTranslation.put(fluid(Fluids.UF6), new ComparableStack(NtmItems.INGOT_URANIUM.get(), 1, 0));
        itemTranslation.put(fluid(Fluids.PUF6), new ComparableStack(NtmItems.INGOT_PLUTONIUM.get(), 1, 0));

        /* Der Ersatz fuer die Erzwoerterbuch-Eintraege dustUranium und dustPlutonium. */
        itemTranslation.put(new ComparableStack(NtmItems.POWDER_URANIUM.get(), 1, 0), new ComparableStack(NtmItems.INGOT_URANIUM.get(), 1, 0));
        itemTranslation.put(new ComparableStack(NtmItems.POWDER_PLUTONIUM.get(), 1, 0), new ComparableStack(NtmItems.INGOT_PLUTONIUM.get(), 1, 0));

        itemTranslation.put(new ComparableStack(NtmItems.POWDER_AUSTRALIUM.get(), 1, 0), new ComparableStack(NtmItems.INGOT_AUSTRALIUM.get(), 1, 0));
        itemTranslation.put(new ComparableStack(NtmItems.POWDER_LAPIS.get(), 1, 0), new ComparableStack(Items.LAPIS_LAZULI, 1, 0));

        tinyWasteTranslation.put(NtmItems.NUCLEAR_WASTE_SHORT_TINY.get(), NtmItems.NUCLEAR_WASTE_SHORT.get());
        tinyWasteTranslation.put(NtmItems.NUCLEAR_WASTE_LONG_TINY.get(), NtmItems.NUCLEAR_WASTE_LONG.get());
        tinyWasteTranslation.put(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED_TINY.get(), NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED.get());
        tinyWasteTranslation.put(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED_TINY.get(), NtmItems.NUCLEAR_WASTE_LONG_DEPLETED.get());
    }

    /**
     * Das Rezept fuer diesen Gegenstand, oder null. Der kleine Abfall hat keins -- fuer ihn
     * wird das des grossen auf ein Neuntel der Loesung heruntergerechnet.
     */
    public static @Nullable SILEXRecipe getOutput(ItemStack stack) {

        if(stack.isEmpty()) return null;

        ComparableStack comp = translateItem(stack);
        SILEXRecipe recipe = recipes.get(comp);
        if(recipe != null) return recipe;

        Item big = tinyWasteTranslation.get(comp.item);

        if(big != null) {
            SILEXRecipe full = getOutput(MetaHelper.newStack(big, 1, comp.meta));

            if(full != null) {
                /* Ganzzahlig, wie im Original: was nicht durch 900 teilbar ist, faellt weg. */
                SILEXRecipe tiny = new SILEXRecipe((full.fluidProduced / 900) * 100, full.fluidConsumed, full.laserStrength);
                tiny.outputs.addAll(full.outputs);
                return tiny;
            }
        }

        return null;
    }

    /** Wonach in der Rezeptkarte gesucht wird -- der Gegenstand selbst oder sein Ersatz. */
    public static ComparableStack translateItem(ItemStack stack) {
        ComparableStack original = new ComparableStack(stack.getItem(), 1, MetaHelper.getMeta(stack));
        ComparableStack translation = itemTranslation.get(original);
        return translation != null ? translation : original;
    }

    /** Das Sinnbild einer Fluessigkeit: der Gegenstand fluid_icon mit ihrer Nummer als Meta. */
    private static ComparableStack fluid(FluidType type) {
        return new ComparableStack(NtmItems.FLUID_ICON.get(), 1, type.getID());
    }

    /**
     * Der Metawert eines Abfallhaufens ist NICHT die Ordnungszahl der Klasse, sondern ihr
     * Platz in der Liste DIESES Gegenstands -- kurzlebiger Abfall kennt acht Klassen,
     * langlebiger fuenf, und Thorium steht nur in der zweiten. Wer hier die Ordnungszahl
     * einsetzte, bekaeme stillschweigend den falschen Abfall.
     */
    private static ItemStack waste(DeferredItem<Item> item, WasteClass wasteClass) {

        NuclearWasteItem waste = (NuclearWasteItem) item.get();
        WasteClass[] classes = waste.getClasses();

        for(int i = 0; i < classes.length; i++) {
            if(classes[i] == wasteClass) return MetaHelper.newStack(waste, 1, i);
        }

        throw new IllegalArgumentException("Abfallklasse " + wasteClass + " kennt " + item.getId() + " nicht");
    }

    private static SILEXRecipe register(ItemLike input, int fluidProduced, int fluidConsumed, Wellenlaenge laserStrength) {
        return register(new ComparableStack(input.asItem(), 1, 0), fluidProduced, fluidConsumed, laserStrength);
    }

    private static SILEXRecipe register(ComparableStack input, int fluidProduced, int fluidConsumed, Wellenlaenge laserStrength) {
        SILEXRecipe recipe = new SILEXRecipe(fluidProduced, fluidConsumed, laserStrength);
        recipes.put(input, recipe);
        return recipe;
    }

    /** Jedes Pellet gibt 600 Loesung und kostet 100 je Ausgang; nur Balefire faellt heraus. */
    private static SILEXRecipe pellet(ItemLike pellet, int meta, Wellenlaenge laserStrength) {
        return register(new ComparableStack(pellet.asItem(), 1, meta), 600, 100, laserStrength);
    }

    /** Jeder Abfall gibt 900 Loesung und kostet 100 je Ausgang. */
    private static SILEXRecipe registerWaste(DeferredItem<Item> item, WasteClass wasteClass, Wellenlaenge laserStrength) {
        return register(new ComparableStack(waste(item, wasteClass)), 900, 100, laserStrength);
    }

    @Override public String getFileName() { return "hbmSILEX.json"; }
    @Override public Object getRecipeObject() { return recipes; }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = recipe.getAsJsonObject();

        AStack input = readAStack(obj.get("input").getAsJsonArray());
        int fluidProduced = obj.get("fluidProduced").getAsInt();
        int fluidConsumed = obj.get("fluidConsumed").getAsInt();
        Wellenlaenge laserStrength = Wellenlaenge.valueOf(obj.get("wavelength").getAsString());

        if(!(input instanceof ComparableStack comp)) {
            NuclearTechMod.LOGGER.error("SILEX recipe with unsupported input type: {}", obj);
            return;
        }

        SILEXRecipe result = new SILEXRecipe(fluidProduced, fluidConsumed, laserStrength);

        for(JsonElement element : obj.get("outputs").getAsJsonArray()) {
            JsonObject output = element.getAsJsonObject();
            result.addOut(readItemStack(output.get("stack").getAsJsonArray()), output.get("weight").getAsInt());
        }

        recipes.put(comp, result);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        @SuppressWarnings("unchecked")
        Entry<ComparableStack, SILEXRecipe> entry = (Entry<ComparableStack, SILEXRecipe>) recipe;

        writer.name("input");
        writeAStack(entry.getKey(), writer);
        writer.name("fluidProduced").value(entry.getValue().fluidProduced);
        writer.name("fluidConsumed").value(entry.getValue().fluidConsumed);
        writer.name("wavelength").value(entry.getValue().laserStrength.name());
        writer.name("outputs").beginArray();

        for(Output output : entry.getValue().outputs) {
            writer.beginObject();
            writer.name("stack");
            writeItemStack(output.stack(), writer);
            writer.name("weight").value(output.weight());
            writer.endObject();
        }

        writer.endArray();
    }

    @Override
    public String getComment() {
        return "Outputs are weighted shares, not chances: the machine steps through the ladder instead of rolling. The wavelength is the minimum the FEL has to supply.";
    }
}
