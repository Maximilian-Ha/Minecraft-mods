package com.hbm.world.gen;

import com.hbm.main.NuclearTechMod;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Map;

/**
 * Das METEORITENVERLIES -- das erste Bauwerk, das der Port in die Welt setzt.
 *
 * Aus NTMWorldGenerator.java:278: minHeight und maxHeight beide 32 (es haengt also immer an
 * derselben Hoehe), sizeLimit 128 Stuecke, canSpawn ueber biome.rootHeight >= 0.
 *
 * DREI DINGE HEISSEN IN 1.21 ANDERS, und keines davon ist eins zu eins:
 *
 * 1. GROESSE. Das Original zaehlt STUECKE und hoert bei 128 auf. 1.21 zaehlt TIEFE -- wie oft
 *    von einem Stueck zum naechsten weitergesprungen wurde -- und laesst hoechstens 20 zu.
 *    Hier stehen 7, wie bei den Doerfern von Vanilla; die kommen damit auf rund hundert
 *    Stuecke, also auf dieselbe Groessenordnung.
 *
 * 2. REICHWEITE. Das Original erlaubt 128 Bloecke vom Mittelpunkt. Der hier benutzte
 *    Erbauer von 1.21 setzt 80 fest; wer 128 will, braucht die lange Fassung des
 *    Erbauers. Das Verlies wird damit etwas gedrungener.
 *
 * 3. BIOME. Das Original fragt eine Zahl ab -- biome.rootHeight >= 0 --, und die gibt es in
 *    1.21 nicht mehr. An ihrer Stelle steht die Liste unten: alle Oberflaechenbiome ausser
 *    Ozeanen, Fluessen und Suempfen, dazu die drei Hoehlenbiome. Genau diese drei Gruppen
 *    liegen in 1.7.10 unter null (Sumpf: -0,2; Fluss: -0,5; Ozean tiefer).
 */
public class NtmStructures {

    public static final ResourceKey<Structure> METEOR_DUNGEON = registerKey("meteor_dungeon");

    public static void bootstrap(BootstrapContext<Structure> context) {

        HolderGetter<Biome> biome = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(METEOR_DUNGEON, new JigsawStructure(
                new Structure.StructureSettings(
                        landbiome(biome),
                        Map.of(),
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.NONE),
                pools.getOrThrow(NtmTemplatePools.START),
                7,
                ConstantHeight.of(VerticalAnchor.absolute(32)),
                false));
    }

    /** Alles, was in 1.7.10 eine rootHeight von null oder mehr hatte, plus die Hoehlenbiome. */
    private static HolderSet<Biome> landbiome(HolderGetter<Biome> biome) {
        return HolderSet.direct(
                biome.getOrThrow(Biomes.PLAINS),
                biome.getOrThrow(Biomes.SUNFLOWER_PLAINS),
                biome.getOrThrow(Biomes.SNOWY_PLAINS),
                biome.getOrThrow(Biomes.ICE_SPIKES),
                biome.getOrThrow(Biomes.DESERT),
                biome.getOrThrow(Biomes.FOREST),
                biome.getOrThrow(Biomes.FLOWER_FOREST),
                biome.getOrThrow(Biomes.BIRCH_FOREST),
                biome.getOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST),
                biome.getOrThrow(Biomes.DARK_FOREST),
                biome.getOrThrow(Biomes.TAIGA),
                biome.getOrThrow(Biomes.SNOWY_TAIGA),
                biome.getOrThrow(Biomes.OLD_GROWTH_PINE_TAIGA),
                biome.getOrThrow(Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                biome.getOrThrow(Biomes.SAVANNA),
                biome.getOrThrow(Biomes.SAVANNA_PLATEAU),
                biome.getOrThrow(Biomes.WINDSWEPT_SAVANNA),
                biome.getOrThrow(Biomes.WINDSWEPT_HILLS),
                biome.getOrThrow(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                biome.getOrThrow(Biomes.WINDSWEPT_FOREST),
                biome.getOrThrow(Biomes.JUNGLE),
                biome.getOrThrow(Biomes.SPARSE_JUNGLE),
                biome.getOrThrow(Biomes.BAMBOO_JUNGLE),
                biome.getOrThrow(Biomes.BADLANDS),
                biome.getOrThrow(Biomes.ERODED_BADLANDS),
                biome.getOrThrow(Biomes.WOODED_BADLANDS),
                biome.getOrThrow(Biomes.MEADOW),
                biome.getOrThrow(Biomes.CHERRY_GROVE),
                biome.getOrThrow(Biomes.GROVE),
                biome.getOrThrow(Biomes.SNOWY_SLOPES),
                biome.getOrThrow(Biomes.FROZEN_PEAKS),
                biome.getOrThrow(Biomes.JAGGED_PEAKS),
                biome.getOrThrow(Biomes.STONY_PEAKS),
                biome.getOrThrow(Biomes.BEACH),
                biome.getOrThrow(Biomes.SNOWY_BEACH),
                biome.getOrThrow(Biomes.STONY_SHORE),
                biome.getOrThrow(Biomes.MUSHROOM_FIELDS),
                biome.getOrThrow(Biomes.DRIPSTONE_CAVES),
                biome.getOrThrow(Biomes.LUSH_CAVES),
                biome.getOrThrow(Biomes.DEEP_DARK));
    }

    private static ResourceKey<Structure> registerKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE, NuclearTechMod.withDefaultNamespace(path));
    }
}
