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
import net.minecraft.world.level.levelgen.Heightmap;
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

    /* Die sechs Einzelbauwerke der Wueste, Runde 260. Im Original haengen sie alle an
     * derselben Bedingung: BiomeDictionary.isBiomeOfType(biome, Type.SANDY). */
    public static final ResourceKey<Structure> VERTIBIRD = registerKey("vertibird");
    public static final ResourceKey<Structure> CRASHED_VERTIBIRD = registerKey("crashed_vertibird");
    public static final ResourceKey<Structure> DESERT_SHACK_1 = registerKey("desert_shack_1");
    public static final ResourceKey<Structure> DESERT_SHACK_2 = registerKey("desert_shack_2");
    public static final ResourceKey<Structure> DESERT_SHACK_3 = registerKey("desert_shack_3");
    public static final ResourceKey<Structure> DEAD_DISH_SMALL = registerKey("dead_dish_small");

    /* Die zehn Ruinen, Runde 261. */
    public static final ResourceKey<Structure> RUIN_A = registerKey("ruin_a");
    public static final ResourceKey<Structure> RUIN_B = registerKey("ruin_b");
    public static final ResourceKey<Structure> RUIN_C = registerKey("ruin_c");
    public static final ResourceKey<Structure> RUIN_D = registerKey("ruin_d");
    public static final ResourceKey<Structure> RUIN_E = registerKey("ruin_e");
    public static final ResourceKey<Structure> RUIN_F = registerKey("ruin_f");
    public static final ResourceKey<Structure> RUIN_G = registerKey("ruin_g");
    public static final ResourceKey<Structure> RUIN_H = registerKey("ruin_h");
    public static final ResourceKey<Structure> RUIN_I = registerKey("ruin_i");
    public static final ResourceKey<Structure> RUIN_J = registerKey("ruin_j");

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

        /*
         * DIE SECHS DER WUESTE. Ein Stueck, keine Anschlussstellen, also Tiefe 1.
         *
         * DIE HOEHE IST EIN VERSATZ, KEINE HOEHE: das Original gibt dem JigsawPiece einen
         * heightOffset (drittes Argument des Erbauers) und setzt das Stueck so viele Bloecke
         * unter die Gelaendeoberkante -- der Vertibird drei, sein Wrack zehn. In 1.21 heisst
         * dasselbe: projectStartToHeightmap setzen und die Starthoehe als Versatz lesen. Ohne
         * das Heightmap-Argument waere die Zahl eine absolute Hoehe, und die Huetten staenden
         * auf Y=-7 im Grundgestein.
         */
        einzeln(context, biome, pools, VERTIBIRD, NtmTemplatePools.VERTIBIRD, -3);
        einzeln(context, biome, pools, CRASHED_VERTIBIRD, NtmTemplatePools.CRASHED_VERTIBIRD, -10);
        einzeln(context, biome, pools, DESERT_SHACK_1, NtmTemplatePools.DESERT_SHACK_1, -7);
        einzeln(context, biome, pools, DESERT_SHACK_2, NtmTemplatePools.DESERT_SHACK_2, -7);
        einzeln(context, biome, pools, DESERT_SHACK_3, NtmTemplatePools.DESERT_SHACK_3, -5);
        einzeln(context, biome, pools, DEAD_DISH_SMALL, NtmTemplatePools.DEAD_DISH_SMALL, -5);

        /*
         * DIE ZEHN RUINEN. Versatz null, obwohl das Original heightOffset = -1 nennt: der
         * Versatz gehoert dort zum conformToTerrain und steckt hier im Schwerkraftprozessor
         * des Pools. Zweimal gerechnet stuende jede Ruine einen Block zu tief.
         */
        ruine(context, biome, pools, RUIN_A, NtmTemplatePools.RUIN_A);
        ruine(context, biome, pools, RUIN_B, NtmTemplatePools.RUIN_B);
        ruine(context, biome, pools, RUIN_C, NtmTemplatePools.RUIN_C);
        ruine(context, biome, pools, RUIN_D, NtmTemplatePools.RUIN_D);
        ruine(context, biome, pools, RUIN_E, NtmTemplatePools.RUIN_E);
        ruine(context, biome, pools, RUIN_F, NtmTemplatePools.RUIN_F);
        ruine(context, biome, pools, RUIN_G, NtmTemplatePools.RUIN_G);
        ruine(context, biome, pools, RUIN_H, NtmTemplatePools.RUIN_H);
        ruine(context, biome, pools, RUIN_I, NtmTemplatePools.RUIN_I);
        ruine(context, biome, pools, RUIN_J, NtmTemplatePools.RUIN_J);
    }

    /** Ein Einzelbauwerk der Wueste: ein Stueck, an der Gelaendeoberkante, um Versatz tiefer. */
    private static void einzeln(BootstrapContext<Structure> context, HolderGetter<Biome> biome,
            HolderGetter<StructureTemplatePool> pools, ResourceKey<Structure> schluessel,
            ResourceKey<StructureTemplatePool> pool, int versatz) {

        context.register(schluessel, new JigsawStructure(
                new Structure.StructureSettings(
                        sandbiome(biome),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE),
                pools.getOrThrow(pool),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(versatz)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG));
    }

    /** Eine Ruine: ein Stueck, Gelaendefolgen im Prozessor, darum ohne eigenen Versatz. */
    private static void ruine(BootstrapContext<Structure> context, HolderGetter<Biome> biome,
            HolderGetter<StructureTemplatePool> pools, ResourceKey<Structure> schluessel,
            ResourceKey<StructureTemplatePool> pool) {

        context.register(schluessel, new JigsawStructure(
                new Structure.StructureSettings(
                        regenbiome(biome),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE),
                pools.getOrThrow(pool),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG));
    }

    /**
     * Wo die Ruinen stehen duerfen: !isWaterBiome(biome) && biome.canSpawnLightningBolt().
     *
     * canSpawnLightningBolt() ist eine Zeile aus Vanilla 1.7.10 -- "enableSnow ? false :
     * enableRain". Es regnet dort also, und es schneit nicht. Damit fallen die Wuesten und die
     * Mesa-Familie weg (kein Regen) und alles Verschneite; isWaterBiome nimmt Ozeane und
     * Fluesse heraus.
     *
     * Die Liste unten ist die Anwendung dieser Regel auf die Biome von 1.21: Laub, Gras und
     * Dschungel, dazu die Suempfe und die feuchten Bergbiome. Sie ist NICHT an Vanillas
     * Tabelle nachgemessen -- die liegt so wenig in diesem Baum wie Forges Typentabelle.
     */
    private static HolderSet<Biome> regenbiome(HolderGetter<Biome> biome) {
        return HolderSet.direct(
                biome.getOrThrow(Biomes.PLAINS),
                biome.getOrThrow(Biomes.SUNFLOWER_PLAINS),
                biome.getOrThrow(Biomes.FOREST),
                biome.getOrThrow(Biomes.FLOWER_FOREST),
                biome.getOrThrow(Biomes.BIRCH_FOREST),
                biome.getOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST),
                biome.getOrThrow(Biomes.DARK_FOREST),
                biome.getOrThrow(Biomes.TAIGA),
                biome.getOrThrow(Biomes.OLD_GROWTH_PINE_TAIGA),
                biome.getOrThrow(Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                biome.getOrThrow(Biomes.JUNGLE),
                biome.getOrThrow(Biomes.SPARSE_JUNGLE),
                biome.getOrThrow(Biomes.BAMBOO_JUNGLE),
                biome.getOrThrow(Biomes.SWAMP),
                biome.getOrThrow(Biomes.MANGROVE_SWAMP),
                biome.getOrThrow(Biomes.WINDSWEPT_HILLS),
                biome.getOrThrow(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                biome.getOrThrow(Biomes.WINDSWEPT_FOREST),
                biome.getOrThrow(Biomes.MEADOW),
                biome.getOrThrow(Biomes.CHERRY_GROVE),
                biome.getOrThrow(Biomes.STONY_PEAKS),
                biome.getOrThrow(Biomes.MUSHROOM_FIELDS));
    }

    /**
     * Was in 1.7.10 BiomeDictionary.Type.SANDY war.
     *
     * DIESE LISTE IST NICHT NACHGEMESSEN, UND DAS LAESST SICH HIER AUCH NICHT AENDERN: welche
     * Biome Forge mit SANDY versieht, steht in Forges eigener registerVanillaBiomes -- nicht
     * im Quelltext des Originals und nicht in diesem Verzeichnisbaum. Nachlesbar ist nur, was
     * SANDY bedeutet: Boden aus Sand oder Sandstein.
     *
     * Die Liste hier ist deshalb bewusst eng gehalten -- die Wueste und die Mesa-Familie, bei
     * denen der Sandboden ausser Frage steht. Straende und Savannen stehen NICHT drin: fuer
     * den Strand gibt es im Original einen eigenen Typ (BEACH, den der Leuchtturm getrennt
     * abfragt), und die Savanne hat Grasboden. Wer die Zuordnung spaeter an Forges Tabelle
     * misst, erweitert hier -- eine zu enge Liste laesst ein Bauwerk seltener stehen, eine zu
     * weite setzt es an Orte, an denen es im Original nie stand.
     */
    private static HolderSet<Biome> sandbiome(HolderGetter<Biome> biome) {
        return HolderSet.direct(
                biome.getOrThrow(Biomes.DESERT),
                biome.getOrThrow(Biomes.BADLANDS),
                biome.getOrThrow(Biomes.ERODED_BADLANDS),
                biome.getOrThrow(Biomes.WOODED_BADLANDS));
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
