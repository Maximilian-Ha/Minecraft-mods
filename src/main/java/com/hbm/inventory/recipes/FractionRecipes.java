package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.util.Tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.FractionRecipes.
 *
 * Welches Oel im Fraktionierturm in welche zwei leichteren Fraktionen zerfaellt. Die Eingabe
 * ist immer 100 mB, die Ausgabemengen stehen im Rezept.
 *
 * ABWEICHUNG: das Original benutzt eine HashMap. Hier ist es eine LinkedHashMap, damit die
 * ausgeschriebene Rezeptdatei bei jedem Start dieselbe Reihenfolge hat.
 */
public class FractionRecipes extends SerializableRecipe {

    public static final Map<FluidType, Pair<FluidStack, FluidStack>> fractions = new LinkedHashMap<>();

    @Override
    public void registerDefaults() {
        fractions.put(Fluids.HEAVYOIL,           pair(Fluids.BITUMEN, 30,                Fluids.SMEAR, 70));
        fractions.put(Fluids.HEAVYOIL_VACUUM,    pair(Fluids.SMEAR, 40,                  Fluids.HEATINGOIL_VACUUM, 60));
        fractions.put(Fluids.SMEAR,              pair(Fluids.HEATINGOIL, 60,             Fluids.LUBRICANT, 40));
        fractions.put(Fluids.NAPHTHA,            pair(Fluids.HEATINGOIL, 40,             Fluids.DIESEL, 60));
        fractions.put(Fluids.NAPHTHA_DS,         pair(Fluids.XYLENE, 60,                 Fluids.DIESEL_REFORM, 40));
        fractions.put(Fluids.NAPHTHA_CRACK,      pair(Fluids.HEATINGOIL, 30,             Fluids.DIESEL_CRACK, 70));
        fractions.put(Fluids.LIGHTOIL,           pair(Fluids.DIESEL, 40,                 Fluids.KEROSENE, 60));
        fractions.put(Fluids.LIGHTOIL_DS,        pair(Fluids.DIESEL_REFORM, 60,          Fluids.KEROSENE_REFORM, 40));
        fractions.put(Fluids.LIGHTOIL_CRACK,     pair(Fluids.KEROSENE, 70,               Fluids.PETROLEUM, 30));
        fractions.put(Fluids.COALOIL,            pair(Fluids.COALGAS, 30,                Fluids.OIL, 70));
        fractions.put(Fluids.COALCREOSOTE,       pair(Fluids.COALOIL, 10,                Fluids.BITUMEN, 90));
        fractions.put(Fluids.REFORMATE,          pair(Fluids.AROMATICS, 40,              Fluids.XYLENE, 60));
        fractions.put(Fluids.LIGHTOIL_VACUUM,    pair(Fluids.KEROSENE, 70,               Fluids.REFORMGAS, 30));
        fractions.put(Fluids.EGG,                pair(Fluids.CHOLESTEROL, 50,            Fluids.RADIOSOLVENT, 50));
        fractions.put(Fluids.OIL_COKER,          pair(Fluids.CRACKOIL, 30,               Fluids.HEATINGOIL, 70));
        fractions.put(Fluids.NAPHTHA_COKER,      pair(Fluids.NAPHTHA_CRACK, 75,          Fluids.LIGHTOIL_CRACK, 25));
        fractions.put(Fluids.GAS_COKER,          pair(Fluids.AROMATICS, 25,              Fluids.CARBONDIOXIDE, 75));
        fractions.put(Fluids.CHLOROCALCITE_MIX,  pair(Fluids.CHLOROCALCITE_CLEANED, 50,  Fluids.COLLOID, 50));
        fractions.put(Fluids.BAUXITE_SOLUTION,   pair(Fluids.REDMUD, 50,                 Fluids.SODIUM_ALUMINATE, 50));
    }

    private static Pair<FluidStack, FluidStack> pair(FluidType left, int leftAmount, FluidType right, int rightAmount) {
        return new Pair<>(new FluidStack(left, leftAmount), new FluidStack(right, rightAmount));
    }

    public static @Nullable Pair<FluidStack, FluidStack> getFractions(FluidType oil) {
        return fractions.get(oil);
    }

    @Override public String getFileName() { return "hbmFractions.json"; }
    @Override public Object getRecipeObject() { return fractions; }
    @Override public void deleteRecipes() { fractions.clear(); }

    @Override
    public String getComment() {
        return "Inputs are always 100mB, set output quantities accordingly.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        FluidType input = Fluids.fromName(obj.get("input").getAsString());
        FluidStack output1 = readFluidStack(obj.get("output1").getAsJsonArray());
        FluidStack output2 = readFluidStack(obj.get("output2").getAsJsonArray());

        fractions.put(input, new Pair<>(output1, output2));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, Pair<FluidStack, FluidStack>> rec = (Map.Entry<FluidType, Pair<FluidStack, FluidStack>>) recipe;

        writer.name("input").value(rec.getKey().getUnlocalizedName());
        writer.name("output1"); writeFluidStack(rec.getValue().getKey(), writer);
        writer.name("output2"); writeFluidStack(rec.getValue().getValue(), writer);
    }
}
