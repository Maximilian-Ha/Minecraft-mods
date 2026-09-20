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
        streuung(context, strukturen, VERTIBIRD, NtmStructures.VERTIBIRD, 88, 29);
        streuung(context, strukturen, CRASHED_VERTIBIRD, NtmStructures.CRASHED_VERTIBIRD, 68, 23);
        streuung(context, strukturen, DESERT_SHACK_1, NtmStructures.DESERT_SHACK_1, 51, 17);
        streuung(context, strukturen, DESERT_SHACK_2, NtmStructures.DESERT_SHACK_2, 48, 16);
        streuung(context, strukturen, DESERT_SHACK_3, NtmStructures.DESERT_SHACK_3, 46, 15);
        streuung(context, strukturen, DEAD_DISH_SMALL, NtmStructures.DEAD_DISH_SMALL, 55, 18);
    }

    /**
     * Ein Bauwerk in sein eigenes Raster.
     *
     * DER STREUWERT IST JE BAUWERK EIN ANDERER, und das muss er sein: zwei Bauwerke mit
     * demselben Wert und demselben Abstand landeten in jeder Rasterzelle auf demselben Feld
     * und staenden ineinander. Er ist hier aus der Zahl des Originals und dem Abstand
     * gebildet, damit er nachvollziehbar bleibt und nicht ausgedacht wirkt.
     */
    private static void streuung(BootstrapContext<StructureSet> context, HolderGetter<Structure> strukturen,
            ResourceKey<StructureSet> schluessel, ResourceKey<Structure> bauwerk, int abstand, int zwischenraum) {

        context.register(schluessel, new StructureSet(
                strukturen.getOrThrow(bauwerk),
                new RandomSpreadStructurePlacement(abstand, zwischenraum, RandomSpreadType.LINEAR, 996996996 + abstand)));
    }

    private static ResourceKey<StructureSet> registerKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, NuclearTechMod.withDefaultNamespace(path));
    }
}
