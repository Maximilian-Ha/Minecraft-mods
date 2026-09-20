package com.hbm.world.gen;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class NtmBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_OIL_BUBBLE = registerKey("add_oil_bubble");

    public static final ResourceKey<BiomeModifier> ADD_BEDROCK_OIL = registerKey("add_bedrock_oil");
    public static final ResourceKey<BiomeModifier> ADD_BEDROCK_ORE = registerKey("add_bedrock_ore");

    public static final ResourceKey<BiomeModifier> ADD_LANDMINE = registerKey("add_landmine");

    public static final ResourceKey<BiomeModifier> ADD_CRASHED_BOMB = registerKey("add_crashed_bomb");

    /**
     * Die drei Creeper, die von selbst erscheinen, Runde 240. Das Original meldet sie in
     * EntityMappings an: Phosgen mit Gewicht 5, der fluechtige mit 10, der goldene mit 1 --
     * und alle drei mit einer Gruppengroesse von genau einem Stueck, in allen Biomen.
     *
     * "ALLE BIOME" HEISST HIER DIE OBERWELT. Im Original ist das
     * BiomeGenBase.getBiomeGenArray(), also jedes angemeldete Biom; der Creeper prueft dann
     * in getCanSpawnHere noch einmal auf dimension == 0. Der Port dreht das um und haengt
     * das Erscheinen gleich an BiomeTags.IS_OVERWORLD -- dieselbe Wirkung, eine Pruefung
     * weniger.
     */
    public static final ResourceKey<BiomeModifier> ADD_CREEPERS = registerKey("add_creepers");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(ADD_OIL_BUBBLE, new AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placedFeatures.getOrThrow(NtmPlacedFeatures.OIL_BUBBLE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_BEDROCK_OIL, new AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placedFeatures.getOrThrow(NtmPlacedFeatures.BEDROCK_OIL_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_BEDROCK_ORE, new AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placedFeatures.getOrThrow(NtmPlacedFeatures.BEDROCK_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_LANDMINE, new AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placedFeatures.getOrThrow(NtmPlacedFeatures.LANDMINE_PLACED)), GenerationStep.Decoration.UNDERGROUND_DECORATION));

        context.register(ADD_CRASHED_BOMB, new AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placedFeatures.getOrThrow(NtmPlacedFeatures.CRASHED_BOMB_PLACED)), GenerationStep.Decoration.UNDERGROUND_DECORATION));

        context.register(ADD_CREEPERS, new AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                List.of(
                        new MobSpawnSettings.SpawnerData(NtmEntityTypes.CREEPER_PHOSGENE.get(), 5, 1, 1),
                        new MobSpawnSettings.SpawnerData(NtmEntityTypes.CREEPER_VOLATILE.get(), 10, 1, 1),
                        new MobSpawnSettings.SpawnerData(NtmEntityTypes.CREEPER_GOLD.get(), 1, 1, 1))));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, NuclearTechMod.withDefaultNamespace(name));
    }
}
