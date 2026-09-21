package com.hbm.world.gen;

import com.hbm.main.NuclearTechMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.VerticalAnchor;

import java.util.List;

public class NtmPlacedFeatures {

    public static final ResourceKey<PlacedFeature> OIL_BUBBLE_PLACED = registerKey("oil_bubble_placed");

    public static final ResourceKey<PlacedFeature> BEDROCK_OIL_PLACED = registerKey("bedrock_oil_placed");
    public static final ResourceKey<PlacedFeature> BEDROCK_ORE_PLACED = registerKey("bedrock_ore_placed");

    public static final ResourceKey<PlacedFeature> LANDMINE_PLACED = registerKey("landmine_placed");
    public static final ResourceKey<PlacedFeature> GLYPHID_HIVE_PLACED = registerKey("glyphid_hive_placed");

    public static final ResourceKey<PlacedFeature> CRASHED_BOMB_PLACED = registerKey("crashed_bomb_placed");

    public static final ResourceKey<PlacedFeature> SOYUZ_CAPSULE_PLACED = registerKey("soyuz_capsule_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, OIL_BUBBLE_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.OIL_BUBBLE), List.of(InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(24))));
        register(context, BEDROCK_OIL_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.BEDROCK_OIL), List.of(RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)));
        /* Das Grundgesteinserz liegt in der untersten Lage, nicht an der Oberflaeche -- es
         * braucht daher keine Hoehenkarte, die Platzierung rechnet selbst ab getMinBuildHeight.
         * Einmal je acht Bloecke im Mittel: das Original streut sie dichter als das Oelvorkommen,
         * weil ein Bagger eine ganze Weile an einem Fleck arbeitet. */
        register(context, BEDROCK_ORE_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.BEDROCK_ORE), List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread()));
        register(context, LANDMINE_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.LANDMINE), List.of(RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)));
        /* Ein Bau je 256 Chunks im Mittel -- das ist MobConfig.hiveSpawn des Originals.
         * Auf 1.7.10 stand diese Zahl im Weltgenerator selbst, auf 1.21 steht sie hier. */
        register(context, GLYPHID_HIVE_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.GLYPHID_HIVE), List.of(RarityFilter.onAverageOnceEvery(256), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)));
        register(context, CRASHED_BOMB_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.CRASHED_BOMB), List.of(RarityFilter.onAverageOnceEvery(500), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)));
        /* Jeder hundertste Chunk -- WorldConfig.capsuleStructure des Originals. */
        register(context, SOYUZ_CAPSULE_PLACED, configuredFeatures.getOrThrow(NtmConfiguredFeatures.SOYUZ_CAPSULE), List.of(RarityFilter.onAverageOnceEvery(100), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, NuclearTechMod.withDefaultNamespace(name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
