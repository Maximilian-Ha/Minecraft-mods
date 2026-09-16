package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.init.ECBlocks;
import com.zuxelus.energycontrol.init.ECItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

/**
 * Rezepte.
 *
 * NICHT aus dem Original uebernommen: dort bestand fast alles aus IC2-Bauteilen
 * (Maschinengehaeuse, Elektronische Schaltung, Fortgeschrittene Schaltung). Die gibt es
 * auf 1.21.1 nicht, also stellt dieser Port die drei Bauteile selbst her -- aus
 * Vanilla-Stoffen und in derselben Rolle wie im Original.
 */
public class ECRecipeProvider extends RecipeProvider {

    public ECRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        components(output);
        blocks(output);
        upgrades(output);
        kitsAndCards(output);
    }

    private void components(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ECItems.MACHINE_CASING.get())
                .pattern("III")
                .pattern("I I")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ECItems.BASIC_CIRCUIT.get())
                .pattern("CRC")
                .pattern("RIR")
                .pattern("CRC")
                .define('C', Items.COPPER_INGOT)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ECItems.ADVANCED_CIRCUIT.get())
                .pattern("RLR")
                .pattern("GBG")
                .pattern("RLR")
                .define('R', Items.REDSTONE)
                .define('L', Items.LAPIS_LAZULI)
                .define('G', Items.GOLD_INGOT)
                .define('B', ECItems.BASIC_CIRCUIT.get())
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.THERMOMETER.get())
                .requires(Items.GLASS)
                .requires(Items.REDSTONE)
                .requires(Items.IRON_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ECItems.PANEL_TOOLKIT.get())
                .requires(Items.IRON_INGOT)
                .requires(ECItems.BASIC_CIRCUIT.get())
                .requires(Items.GLASS_PANE)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);
    }

    private void blocks(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.INFO_PANEL.get())
                .pattern("GGG")
                .pattern("CMC")
                .pattern("III")
                .define('G', Items.GLASS)
                .define('C', ECItems.BASIC_CIRCUIT.get())
                .define('M', ECItems.MACHINE_CASING.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_machine_casing", has(ECItems.MACHINE_CASING.get()))
                .save(output);

        // Die Erweiterung fuehrt keine Daten und braucht deshalb keine Schaltung.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.INFO_PANEL_EXTENDER.get(), 2)
                .pattern("GGG")
                .pattern("IMI")
                .pattern("III")
                .define('G', Items.GLASS)
                .define('M', ECItems.MACHINE_CASING.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_machine_casing", has(ECItems.MACHINE_CASING.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.THERMAL_MONITOR.get())
                .pattern(" T ")
                .pattern("CMC")
                .pattern(" R ")
                .define('T', ECItems.THERMOMETER.get())
                .define('C', ECItems.BASIC_CIRCUIT.get())
                .define('M', ECItems.MACHINE_CASING.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_thermometer", has(ECItems.THERMOMETER.get()))
                .save(output);

        // Die Fernwaermeanzeige ist ein Waermemelder, der seinen Reaktor ueber eine Karte
        // findet -- sie kostet deshalb den Melder und die bessere Schaltung.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.REMOTE_THERMAL_MONITOR.get())
                .pattern(" T ")
                .pattern("ATA")
                .pattern(" R ")
                .define('T', ECBlocks.THERMAL_MONITOR.get())
                .define('A', ECItems.ADVANCED_CIRCUIT.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_thermal_monitor", has(ECBlocks.THERMAL_MONITOR.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.ENERGY_COUNTER.get())
                .pattern("III")
                .pattern("CMC")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('C', ECItems.ADVANCED_CIRCUIT.get())
                .define('M', ECItems.MACHINE_CASING.get())
                .unlockedBy("has_advanced_circuit", has(ECItems.ADVANCED_CIRCUIT.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.RANGE_TRIGGER.get())
                .pattern(" P ")
                .pattern("CMC")
                .pattern(" R ")
                .define('P', Items.COMPARATOR)
                .define('C', ECItems.BASIC_CIRCUIT.get())
                .define('M', ECItems.MACHINE_CASING.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_machine_casing", has(ECItems.MACHINE_CASING.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.HOWLER_ALARM.get())
                .pattern(" N ")
                .pattern("CMC")
                .pattern(" R ")
                .define('N', Items.NOTE_BLOCK)
                .define('C', ECItems.BASIC_CIRCUIT.get())
                .define('M', ECItems.MACHINE_CASING.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_machine_casing", has(ECItems.MACHINE_CASING.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ECBlocks.INDUSTRIAL_ALARM.get())
                .pattern(" G ")
                .pattern("CMC")
                .pattern(" R ")
                .define('G', Items.GLOWSTONE)
                .define('C', ECItems.BASIC_CIRCUIT.get())
                .define('M', ECItems.MACHINE_CASING.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_machine_casing", has(ECItems.MACHINE_CASING.get()))
                .save(output);
    }

    private void upgrades(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ECItems.UPGRADE_RANGE.get())
                .pattern(" E ")
                .pattern("ICI")
                .pattern(" R ")
                .define('E', Items.ENDER_PEARL)
                .define('I', Items.IRON_INGOT)
                .define('C', ECItems.BASIC_CIRCUIT.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.UPGRADE_COLOR.get())
                .requires(ECItems.BASIC_CIRCUIT.get())
                .requires(Items.RED_DYE)
                .requires(Items.GREEN_DYE)
                .requires(Items.BLUE_DYE)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.UPGRADE_TOUCH.get())
                .requires(ECItems.BASIC_CIRCUIT.get())
                .requires(Items.GLASS_PANE)
                .requires(Items.REDSTONE)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);
    }

    private void kitsAndCards(RecipeOutput output) {
        kit(output, ECItems.KIT_ENERGY.get(), Items.REDSTONE_BLOCK);
        kit(output, ECItems.KIT_LIQUID.get(), Items.BUCKET);
        kit(output, ECItems.KIT_INVENTORY.get(), Items.CHEST);
        kit(output, ECItems.KIT_REDSTONE.get(), Items.REDSTONE_TORCH);
        kit(output, ECItems.KIT_VANILLA.get(), Items.FURNACE);
        kit(output, ECItems.KIT_TOGGLE.get(), Items.LEVER);
        kit(output, ECItems.KIT_COUNTER.get(), Items.CLOCK);

        // Der HBM-Bausatz misst Reaktoren aus und braucht deshalb die bessere Schaltung.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.KIT_HBM.get())
                .requires(ECItems.ADVANCED_CIRCUIT.get())
                .requires(ECItems.THERMOMETER.get())
                .requires(Items.PAPER)
                .unlockedBy("has_advanced_circuit", has(ECItems.ADVANCED_CIRCUIT.get()))
                .save(output);

        // Der Mekanism-Bausatz liest Reaktoren und Matrizen aus -- wie der HBM-Bausatz.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.KIT_MEKANISM.get())
                .requires(ECItems.ADVANCED_CIRCUIT.get())
                .requires(Items.REDSTONE_BLOCK)
                .requires(Items.PAPER)
                .unlockedBy("has_advanced_circuit", has(ECItems.ADVANCED_CIRCUIT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.CARD_TEXT.get())
                .requires(ECItems.BASIC_CIRCUIT.get())
                .requires(Items.PAPER)
                .requires(Items.INK_SAC)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ECItems.CARD_TIME.get())
                .requires(ECItems.BASIC_CIRCUIT.get())
                .requires(Items.PAPER)
                .requires(Items.CLOCK)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);
    }

    private void kit(RecipeOutput output, Item result, ItemLike marker) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                .requires(ECItems.BASIC_CIRCUIT.get())
                .requires(Items.PAPER)
                .requires(marker)
                .unlockedBy("has_basic_circuit", has(ECItems.BASIC_CIRCUIT.get()))
                .save(output);
    }
}
