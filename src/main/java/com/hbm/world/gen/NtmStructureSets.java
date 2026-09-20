package com.hbm.world.gen;

import com.hbm.main.NuclearTechMod;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

/**
 * Wie dicht das Meteoritenverlies in der Welt steht.
 *
 * WIE DAS ORIGINAL VERTEILT: es legt EIN Raster ueber die Welt, zwoelf Bloecke -- pardon,
 * zwoelf CHUNKS -- weit, wuerfelt darin eine Stelle und verlost dort EIN Bauwerk unter allen,
 * die im dortigen Biom stehen duerfen (NBTStructure.GenStructure.getSpawnAtCoords, dazu
 * structureMinChunks 4 und structureMaxChunks 12). Das Verlies zieht mit Gewicht 1 mit.
 *
 * WIE DICHT IST DAS? Nachgerechnet fuer die Ebene, wo dreizehn andere Bauwerke und das
 * Leergewicht mitziehen: 2 + 50 + 6 + 10 + 15 + 30 + 30 + 40 + 20 + 50 + 118 + 30 + 20 + 1
 * = 422. Das Verlies steht also in einer von 422 Rasterzellen zu zwoelf mal zwoelf Chunks.
 *
 * 1.21 VERLOST NICHT. Jedes Bauwerk bekommt sein eigenes Raster; die Dichte steckt allein im
 * Rasterabstand. Gleiche Dichte heisst hier: Abstand mal Abstand gleich 144 mal 422, also
 * 246 Chunks. Der Zwischenraum bleibt im Verhaeltnis des Originals (4 zu 12, also ein
 * Drittel): 82.
 *
 * DAS IST SEHR SELTEN -- rund ein Verlies auf 3900 mal 3900 Bloecke. Genau so selten ist es
 * im Original auch. Wenn die uebrigen achtundsiebzig Bauwerke nachkommen, aendert das an
 * dieser Zahl nichts: in 1.21 addieren sich die Dichten, statt sich zu verdraengen.
 *
 * DER STREUWERT 996996996 ist die Zahl, mit der das Original seinen Zufall setzt
 * (NBTStructure.java:1308). Sie bedeutet nichts, sie steht nur an derselben Stelle.
 */
public class NtmStructureSets {

    public static final ResourceKey<StructureSet> METEOR_DUNGEON = registerKey("meteor_dungeon");

    public static final ResourceKey<StructureSet> VERTIBIRD = registerKey("vertibird");
    public static final ResourceKey<StructureSet> CRASHED_VERTIBIRD = registerKey("crashed_vertibird");
    public static final ResourceKey<StructureSet> DESERT_SHACK_1 = registerKey("desert_shack_1");
    public static final ResourceKey<StructureSet> DESERT_SHACK_2 = registerKey("desert_shack_2");
    public static final ResourceKey<StructureSet> DESERT_SHACK_3 = registerKey("desert_shack_3");
    public static final ResourceKey<StructureSet> DEAD_DISH_SMALL = registerKey("dead_dish_small");

    public static final ResourceKey<StructureSet> RUIN_A = registerKey("ruin_a");
    public static final ResourceKey<StructureSet> RUIN_B = registerKey("ruin_b");
    public static final ResourceKey<StructureSet> RUIN_C = registerKey("ruin_c");
    public static final ResourceKey<StructureSet> RUIN_D = registerKey("ruin_d");
    public static final ResourceKey<StructureSet> RUIN_E = registerKey("ruin_e");
    public static final ResourceKey<StructureSet> RUIN_F = registerKey("ruin_f");
    public static final ResourceKey<StructureSet> RUIN_G = registerKey("ruin_g");
    public static final ResourceKey<StructureSet> RUIN_H = registerKey("ruin_h");
    public static final ResourceKey<StructureSet> RUIN_I = registerKey("ruin_i");
    public static final ResourceKey<StructureSet> RUIN_J = registerKey("ruin_j");

    public static final ResourceKey<StructureSet> SPIRE = registerKey("spire");
    public static final ResourceKey<StructureSet> FOREST_CHEM = registerKey("forest_chem");
    public static final ResourceKey<StructureSet> FOREST_POST = registerKey("forest_post");
    public static final ResourceKey<StructureSet> CRASHED_PLANE_1 = registerKey("crashed_plane_1");
    public static final ResourceKey<StructureSet> CRASHED_PLANE_2 = registerKey("crashed_plane_2");
    public static final ResourceKey<StructureSet> WATER_PUMP = registerKey("water_pump");

    public static void bootstrap(BootstrapContext<StructureSet> context) {

        HolderGetter<Structure> strukturen = context.lookup(Registries.STRUCTURE);

        context.register(METEOR_DUNGEON, new StructureSet(
                strukturen.getOrThrow(NtmStructures.METEOR_DUNGEON),
                new RandomSpreadStructurePlacement(246, 82, RandomSpreadType.LINEAR, 996996996)));

        /*
         * DIE SECHS DER WUESTE, nach derselben Rechnung wie oben: Abstand = 12 * Wurzel aus
         * (Gesamtgewicht / eigenem Gewicht), Zwischenraum ein Drittel davon.
         *
         * DAS GESAMTGEWICHT IST HIER 320, und es ist ausgezaehlt, nicht geschaetzt: im
         * Wuestenbiom von 1.7.10 duerfen Spire (2), Features (50), Bunker (6), Vertibird (6),
         * Wrack (10), Waldchemie (30), Waldposten (30), Fabrik (40), Kran (20), die beiden
         * Flugzeugwracks (je 25), die drei Huetten (18, 20, 22), die tote Schuessel (15) und
         * das Meteoritenverlies (1) ziehen. Nicht dabei sind die Ruinen -- sie verlangen
         * canSpawnLightningBolt(), und in der Wueste regnet es nicht --, alles mit isFlatBiome
         * (die Wueste ist nicht SPARSE), der Turmsockel (er schliesst SANDY aus) und alles,
         * was Ozean, Strand oder Ebene verlangt.
         */
        streuung(context, strukturen, VERTIBIRD, NtmStructures.VERTIBIRD, 88, 29, 996996996 + 1);
        streuung(context, strukturen, CRASHED_VERTIBIRD, NtmStructures.CRASHED_VERTIBIRD, 68, 23, 996996996 + 2);
        streuung(context, strukturen, DESERT_SHACK_1, NtmStructures.DESERT_SHACK_1, 51, 17, 996996996 + 3);
        streuung(context, strukturen, DESERT_SHACK_2, NtmStructures.DESERT_SHACK_2, 48, 16, 996996996 + 4);
        streuung(context, strukturen, DESERT_SHACK_3, NtmStructures.DESERT_SHACK_3, 46, 15, 996996996 + 5);
        streuung(context, strukturen, DEAD_DISH_SMALL, NtmStructures.DEAD_DISH_SMALL, 55, 18, 996996996 + 6);

        /*
         * DIE ZEHN RUINEN. Gewichte 10 fuer die erste, 12 fuer die uebrigen neun.
         *
         * DAS GESAMTGEWICHT IST HIER 422 -- die Zahl, die Runde 251 fuer die Ebene ausgezaehlt
         * hat und mit der das Meteoritenverlies steht. EINE ZWEITE AUSZAEHLUNG IN DIESER RUNDE
         * KAM AUF 491, und der Unterschied liegt an zwei Angaben, die sich in diesem
         * Verzeichnisbaum nicht nachschlagen lassen: ob Forge der Ebene den Typ SPARSE gibt
         * (davon haengen Labor, Funkhaus und Sendeturm ab, zusammen 75) und wie das
         * Leergewicht mitzaehlt. Hier steht die aeltere Zahl, damit die Ruinen und das Verlies
         * auf derselben Skala liegen; wer die beiden Angaben einmal misst, rechnet BEIDE Orte
         * um, nicht nur einen.
         */
        streuung(context, strukturen, RUIN_A, NtmStructures.RUIN_A, 78, 26, 996996996 + 7);
        streuung(context, strukturen, RUIN_B, NtmStructures.RUIN_B, 71, 24, 996996996 + 8);
        streuung(context, strukturen, RUIN_C, NtmStructures.RUIN_C, 71, 24, 996996996 + 9);
        streuung(context, strukturen, RUIN_D, NtmStructures.RUIN_D, 71, 24, 996996996 + 10);
        streuung(context, strukturen, RUIN_E, NtmStructures.RUIN_E, 71, 24, 996996996 + 11);
        streuung(context, strukturen, RUIN_F, NtmStructures.RUIN_F, 71, 24, 996996996 + 12);
        streuung(context, strukturen, RUIN_G, NtmStructures.RUIN_G, 71, 24, 996996996 + 13);
        streuung(context, strukturen, RUIN_H, NtmStructures.RUIN_H, 71, 24, 996996996 + 14);
        streuung(context, strukturen, RUIN_I, NtmStructures.RUIN_I, 71, 24, 996996996 + 15);
        streuung(context, strukturen, RUIN_J, NtmStructures.RUIN_J, 71, 24, 996996996 + 16);

        /*
         * DIE SECHS DES FLACHEN LANDES. Nenner ist wieder das Gesamtgewicht der Ebene (422,
         * mit derselben Einschraenkung wie oben bei den Ruinen); alle sechs duerfen dort
         * stehen, die Ebene ist also fuer jedes von ihnen ein zulaessiger Ort.
         *
         * Der Spire ist mit Gewicht 2 das seltenste Bauwerk des Originals -- Abstand 174
         * Chunks, also rund eines auf 2800 mal 2800 Bloecke.
         */
        streuung(context, strukturen, SPIRE, NtmStructures.SPIRE, 174, 58, 996996996 + 17);
        streuung(context, strukturen, FOREST_CHEM, NtmStructures.FOREST_CHEM, 45, 15, 996996996 + 18);
        streuung(context, strukturen, FOREST_POST, NtmStructures.FOREST_POST, 45, 15, 996996996 + 19);
        streuung(context, strukturen, CRASHED_PLANE_1, NtmStructures.CRASHED_PLANE_1, 49, 16, 996996996 + 20);
        streuung(context, strukturen, CRASHED_PLANE_2, NtmStructures.CRASHED_PLANE_2, 49, 16, 996996996 + 21);
        streuung(context, strukturen, WATER_PUMP, NtmStructures.WATER_PUMP, 64, 21, 996996996 + 22);
    }

    /**
     * Ein Bauwerk in sein eigenes Raster.
     *
     * DER STREUWERT IST JE BAUWERK EIN ANDERER, und das muss er sein: zwei Bauwerke mit
     * demselben Wert und demselben Abstand landeten in jeder Rasterzelle auf demselben Feld
     * und staenden ineinander.
     *
     * Er wird deshalb DURCHGEZAEHLT und nicht aus dem Abstand gebildet. Runde 260 bildete ihn
     * als 996996996 + Abstand. Das ging gut, solange nur die sechs der Wueste hier standen --
     * ihre Abstaende sind alle verschieden. Bei den Ruinen faellt dieselbe Formel in genau die
     * Falle, vor der dieser Absatz warnt: neun der zehn haben den Abstand 71, bekaemen damit
     * denselben Streuwert und staenden ineinander.
     */
    private static void streuung(BootstrapContext<StructureSet> context, HolderGetter<Structure> strukturen,
            ResourceKey<StructureSet> schluessel, ResourceKey<Structure> bauwerk, int abstand, int zwischenraum, int streuwert) {

        context.register(schluessel, new StructureSet(
                strukturen.getOrThrow(bauwerk),
                new RandomSpreadStructurePlacement(abstand, zwischenraum, RandomSpreadType.LINEAR, streuwert)));
    }

    private static ResourceKey<StructureSet> registerKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, NuclearTechMod.withDefaultNamespace(path));
    }
}
