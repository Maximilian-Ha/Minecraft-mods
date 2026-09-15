package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.util.Tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.FluidBreederRecipes.
 *
 * Was der Brutreaktor aus einem Fluid macht, wenn der Neutronenfluss darauf trifft. Drei
 * Eintraege: Erdgas wird zu Synthesegas, Leichtoel -- gekrackt oder nicht -- zu Reformgas.
 */
public class FluidBreederRecipes extends SerializableRecipe {

    /** Eingangsfluid -> (noetige Menge, Ausgang). */
    public static final Map<FluidType, Pair<Integer, FluidStack>> recipes = new HashMap<>();

    @Override
    public void registerDefaults() {
        register(new FluidStack(Fluids.GAS, 1_000), new FluidStack(Fluids.SYNGAS, 1_000));
        register(new FluidStack(Fluids.LIGHTOIL, 1_000), new FluidStack(Fluids.REFORMGAS, 1_000));
        register(new FluidStack(Fluids.LIGHTOIL_CRACK, 1_000), new FluidStack(Fluids.REFORMGAS, 1_000));
    }

    public static void register(FluidStack input, FluidStack output) {
        recipes.put(input.type, new Pair<>(input.fill, output));
    }

    public static @Nullable Pair<Integer, FluidStack> getOutput(FluidType type) {
        return recipes.get(type);
    }

    @Override public String getFileName() { return "hbmFluidBreeder.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;
        FluidStack input = this.readFluidStack(obj.get("input").getAsJsonArray());
        FluidStack output = this.readFluidStack(obj.get("output").getAsJsonArray());
        recipes.put(input.type, new Pair<>(input.fill, output));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, Pair<Integer, FluidStack>> rec = (Map.Entry<FluidType, Pair<Integer, FluidStack>>) recipe;

        writer.name("input"); writeFluidStack(new FluidStack(rec.getKey(), rec.getValue().getKey()), writer);
        writer.name("output"); writeFluidStack(rec.getValue().getValue(), writer);
    }
}
