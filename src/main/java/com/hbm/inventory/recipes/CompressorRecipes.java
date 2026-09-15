package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.util.Tuple.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CompressorRecipes.
 * Schluessel ist das Paar aus Eingangsfluid und dessen Druckstufe.
 */
public class CompressorRecipes extends SerializableRecipe {

    public static final HashMap<Pair<FluidType, Integer>, CompressorRecipe> recipes = new HashMap<>();

    public static class CompressorRecipe {

        public final FluidStack output;
        public final int inputAmount;
        public final int duration;

        public CompressorRecipe(int input, FluidStack output, int duration) {
            this.output = output;
            this.inputAmount = input;
            this.duration = duration;
        }

        public CompressorRecipe(int input, FluidStack output) {
            this(input, output, 100);
        }
    }

    /** Rezept fuer ein Fluid bei gegebener Eingangsdruckstufe, oder null */
    public static CompressorRecipe getRecipe(FluidType type, int pressure) {
        return recipes.get(new Pair<>(type, pressure));
    }

    @Override
    public void registerDefaults() {
        recipes.put(new Pair<>(Fluids.PETROLEUM, 0), new CompressorRecipe(2_000, new FluidStack(Fluids.PETROLEUM, 2_000, 1), 20));
        recipes.put(new Pair<>(Fluids.PETROLEUM, 1), new CompressorRecipe(2_000, new FluidStack(Fluids.LPG, 1_000, 0), 20));

        recipes.put(new Pair<>(Fluids.BLOOD, 3), new CompressorRecipe(1_000, new FluidStack(Fluids.HEAVYOIL, 250, 0), 200));

        recipes.put(new Pair<>(Fluids.PERFLUOROMETHYL, 0), new CompressorRecipe(1_000, new FluidStack(Fluids.PERFLUOROMETHYL, 1_000, 1), 50));
        recipes.put(new Pair<>(Fluids.PERFLUOROMETHYL, 1), new CompressorRecipe(1_000, new FluidStack(Fluids.PERFLUOROMETHYL_COLD, 1_000, 0), 50));
    }

    @Override
    public String getFileName() {
        return "hbmCompressor.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = recipe.getAsJsonObject();

        FluidStack input = readFluidStack(obj.get("input").getAsJsonArray());
        FluidStack output = readFluidStack(obj.get("output").getAsJsonArray());

        recipes.put(new Pair<>(input.type, input.pressure), new CompressorRecipe(input.fill, output));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        @SuppressWarnings("unchecked")
        Entry<Pair<FluidType, Integer>, CompressorRecipe> entry = (Entry<Pair<FluidType, Integer>, CompressorRecipe>) recipe;

        writer.name("input");
        writeFluidStack(new FluidStack(entry.getKey().getKey(), entry.getValue().inputAmount, entry.getKey().getValue()), writer);
        writer.name("output");
        writeFluidStack(entry.getValue().output, writer);
    }
}
