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

    public static void bootstrap(BootstrapContext<StructureSet> context) {

        HolderGetter<Structure> strukturen = context.lookup(Registries.STRUCTURE);

        context.register(METEOR_DUNGEON, new StructureSet(
                strukturen.getOrThrow(NtmStructures.METEOR_DUNGEON),
                new RandomSpreadStructurePlacement(246, 82, RandomSpreadType.LINEAR, 996996996)));
    }

    private static ResourceKey<StructureSet> registerKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, NuclearTechMod.withDefaultNamespace(path));
    }
}
