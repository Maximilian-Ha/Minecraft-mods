package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Einstieg der Datengeneratoren.
 *
 * Wie beim HBM-Port gilt: {@code ./gradlew build} uebersetzt nur. Blockmodelle,
 * Sprachdateien, Rezepte und Loot-Tabellen entstehen erst durch {@code ./gradlew runData}
 * und muessen einmal vor dem Packen erzeugt werden.
 */
@EventBusSubscriber(modid = EnergyControl.MODID)
public class ECDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ECBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new ECItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new ECLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new ECLanguageProvider(output, "de_de"));

        LootTableProvider.SubProviderEntry blockLoot =
                new LootTableProvider.SubProviderEntry(ECBlockLootProvider::new, LootContextParamSets.BLOCK);
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) lootOutput ->
                new LootTableProvider(lootOutput, Collections.emptySet(), List.of(blockLoot), lookup));

        generator.addProvider(event.includeServer(), new ECBlockTagProvider(output, lookup, helper));
        generator.addProvider(event.includeServer(), new ECRecipeProvider(output, lookup));
    }
}
