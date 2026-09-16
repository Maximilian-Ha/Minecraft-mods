package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.util.Tuple.Triplet;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.HydrotreatingRecipes.
 *
 * Der Hydrotreater waescht den Schwefel aus einem Oel: unter Wasserstoffdruck entstehen das
 * entschwefelte Oel und Sauergas. Die Eingabe ist immer 100 mB; die erste Angabe jedes
 * Rezepts ist der Wasserstoff, den es dafuer braucht, samt seinem Druck.
 *
 * ABWEICHUNG: das Original benutzt eine HashMap. Hier ist es eine LinkedHashMap, damit die
 * ausgeschriebene Rezeptdatei bei jedem Start dieselbe Reihenfolge hat.
 */
public class HydrotreatingRecipes extends SerializableRecipe {

    public static final Map<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> recipes = new LinkedHashMap<>();

    @Override
    public void registerDefaults() {
        recipes.put(Fluids.OIL,                  triple(5,  Fluids.OIL_DS, 90,          Fluids.SOURGAS, 15));
        recipes.put(Fluids.CRACKOIL,             triple(5,  Fluids.CRACKOIL_DS, 90,     Fluids.SOURGAS, 15));
        recipes.put(Fluids.GAS,                  triple(5,  Fluids.PETROLEUM, 80,       Fluids.SOURGAS, 15));
        recipes.put(Fluids.DIESEL_CRACK,         triple(10, Fluids.DIESEL, 80,          Fluids.SOURGAS, 30));
        recipes.put(Fluids.DIESEL_CRACK_REFORM,  triple(10, Fluids.DIESEL_REFORM, 80,   Fluids.SOURGAS, 30));
        recipes.put(Fluids.COALOIL,              triple(10, Fluids.COALGAS, 80,         Fluids.SOURGAS, 15));
    }

    /** Der Wasserstoff steht im Original immer unter Druckstufe 1. */
    private static Triplet<FluidStack, FluidStack, FluidStack> triple(int hydrogen, FluidType out, int outFill, FluidType gas, int gasFill) {
        return new Triplet<>(new FluidStack(Fluids.HYDROGEN, hydrogen, 1), new FluidStack(out, outFill), new FluidStack(gas, gasFill));
    }

    public static @Nullable Triplet<FluidStack, FluidStack, FluidStack> getOutput(FluidType type) {
        return recipes.get(type);
    }

    @Override public String getFileName() { return "hbmHydrotreating.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public String getComment() {
        return "Inputs are always 100mB, set output quantities accordingly. The first entry is the hydrogen required, including its pressure.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        FluidType input = Fluids.fromName(obj.get("input").getAsString());
        FluidStack hydrogen = readFluidStack(obj.get("hydrogen").getAsJsonArray());
        FluidStack output1 = readFluidStack(obj.get("output1").getAsJsonArray());
        FluidStack output2 = readFluidStack(obj.get("output2").getAsJsonArray());

        recipes.put(input, new Triplet<>(hydrogen, output1, output2));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> rec = (Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>>) recipe;

        writer.name("input").value(rec.getKey().getUnlocalizedName());
        writer.name("hydrogen"); writeFluidStack(rec.getValue().getX(), writer);
        writer.name("output1"); writeFluidStack(rec.getValue().getY(), writer);
        writer.name("output2"); writeFluidStack(rec.getValue().getZ(), writer);
    }
}
