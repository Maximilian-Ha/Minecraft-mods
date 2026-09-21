package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.LiquefactionRecipes.
 *
 * Was der Verfluessiger aus welchem Gegenstand macht -- das Gegenstueck zu
 * SolidificationRecipes. Was in keiner Liste steht, aber essbar ist, wird zu Nahrfluessigkeit;
 * wie viel, sagt der Saettigungswert.
 *
 * ABWEICHUNGEN:
 *  - das Original benutzt eine HashMap mit gemischten Schluesseln (Gegenstand oder
 *    OreDictionary-Name); hier ist es eine LinkedHashMap ueber AStack, damit die Datei ihre
 *    Reihenfolge behaelt und Gegenstand wie Tag gleich behandelt werden.
 *  - NICHT UEBERNOMMEN: die leere Glyphidendruese. Die gibt es im Port noch nicht.
 *  - das Original kennt zwei Blumen (plant_flower mit Metadaten 3 und 4) mit 100 und 50 mB.
 *    Der Port hat nur die eine Blume; sie steht mit den 100 mB der ersten.
 *  - Fischoel kam im Original aus einem Eintrag mit Platzhalter-Metadaten. Im Port sind die
 *    vier Fische eigene Gegenstaende und stehen einzeln.
 */
public class LiquefactionRecipes extends SerializableRecipe {

    public static final Map<AStack, FluidStack> recipes = new LinkedHashMap<>();

    @Override
    public void registerDefaults() {

        /* Erdoelverarbeitung */
        register(Items.COAL, Fluids.COALOIL, 250);
        register(NtmItems.POWDER_COAL.get(), Fluids.COALOIL, 250);
        register(NtmItems.LIGNITE.get(), Fluids.COALOIL, 150);
        register(NtmItems.POWDER_LIGNITE.get(), Fluids.COALOIL, 150);
        register(NtmItems.OIL_TAR_CRUDE.get(), Fluids.BITUMEN, 75);
        register(NtmItems.OIL_TAR_CRACK.get(), Fluids.BITUMEN, 100);
        register(NtmItems.OIL_TAR_COAL.get(), Fluids.BITUMEN, 50);
        recipes.put(new TagStack(ItemTags.LOGS), new FluidStack(Fluids.MUG, 100));
        register(NtmItems.POWDER_SODIUM.get(), Fluids.SODIUM, 100);
        register(NtmItems.INGOT_LEAD.get(), Fluids.LEAD, 100);
        register(NtmItems.POWDER_LEAD.get(), Fluids.LEAD, 100);
        register(NtmBlocks.BLOCK_LEAD.get(), Fluids.LEAD, 900);

        /* Allerlei Nuetzliches, weil es sich anbietet */
        register(Blocks.NETHERRACK, Fluids.LAVA, 250);
        register(Blocks.COBBLESTONE, Fluids.LAVA, 250);
        register(Blocks.STONE, Fluids.LAVA, 250);
        register(Blocks.OBSIDIAN, Fluids.LAVA, 500);
        register(Items.SNOWBALL, Fluids.WATER, 125);
        register(Blocks.SNOW_BLOCK, Fluids.WATER, 500);
        register(Blocks.ICE, Fluids.WATER, 1000);
        register(Blocks.PACKED_ICE, Fluids.WATER, 1000);
        register(Items.ENDER_PEARL, Fluids.ENDERJUICE, 100);
        register(NtmBlocks.ORE_OIL_SAND.get(), Fluids.BITUMEN, 100);

        /* Biokraftstoff */
        register(Items.SUGAR, Fluids.ETHANOL, 100);
        register(Items.MELON_SLICE, Fluids.ETHANOL, 100);
        register(NtmBlocks.PLANT_FLOWER.get(), Fluids.ETHANOL, 100);
        register(NtmItems.BIOMASS.get(), Fluids.BIOGAS, 125);
        /* Die leere Glyphidendruese, Runde 301 -- zweitausend Millibar Biogas, mit
         * Abstand die ergiebigste Zeile dieser Liste. Steht so im Original. */
        register(NtmItems.GLYPHID_GLAND_EMPTY.get(), Fluids.BIOGAS, 2000);
        register(Items.COD, Fluids.FISHOIL, 100);
        register(Items.SALMON, Fluids.FISHOIL, 100);
        register(Items.TROPICAL_FISH, Fluids.FISHOIL, 100);
        register(Items.PUFFERFISH, Fluids.FISHOIL, 100);
        register(Blocks.SUNFLOWER, Fluids.SUNFLOWEROIL, 100);

        /* Pflanzenbrei */
        register(Items.WHEAT_SEEDS, Fluids.SEEDSLURRY, 50);
        register(Blocks.SHORT_GRASS, Fluids.SEEDSLURRY, 100);
        register(Blocks.FERN, Fluids.SEEDSLURRY, 100);
        register(Blocks.VINE, Fluids.SEEDSLURRY, 100);
    }

    private static void register(ItemLike item, com.hbm.inventory.fluid.FluidType fluid, int amount) {
        recipes.put(new ComparableStack(item.asItem()), new FluidStack(fluid, amount));
    }

    /**
     * Sucht den Eintrag zum Gegenstand. Gibt es keinen, aber der Gegenstand ist essbar, wird
     * er zu Nahrfluessigkeit: Saettigung mal zehn Quanten. Der Saettigungswert enthaelt in
     * 1.21 bereits Naehrwert mal Faktor mal zwei, deshalb fehlt hier die Rechnung, die das
     * Original noch selbst anstellt.
     */
    public static @Nullable FluidStack getOutput(ItemStack stack) {

        if(stack.isEmpty()) return null;

        for(Map.Entry<AStack, FluidStack> entry : recipes.entrySet()) {
            if(entry.getKey().matchesRecipe(stack, true)) return entry.getValue();
        }

        FoodProperties food = stack.get(DataComponents.FOOD);

        if(food != null) {
            int amount = (int) (food.saturation() * 10F);
            if(amount > 0) return new FluidStack(Fluids.SALIENT, amount);
        }

        return null;
    }

    @Override public String getFileName() { return "hbmLiquefactor.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public String getComment() {
        return "As with most handlers, stacksizes for the inputs are ignored and default to 1.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        AStack in = readAStack(obj.get("input").getAsJsonArray());
        FluidStack out = readFluidStack(obj.get("output").getAsJsonArray());

        if(in instanceof ComparableStack comp) in = comp.makeSingular();

        recipes.put(in, out);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<AStack, FluidStack> rec = (Map.Entry<AStack, FluidStack>) recipe;

        writer.name("input"); writeAStack(rec.getKey(), writer);
        writer.name("output"); writeFluidStack(rec.getValue(), writer);
    }
}
