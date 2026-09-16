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
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.ReformingRecipes.
 *
 * Der Reformer baut lange Ketten zu ringfoermigen Verbindungen um. Jede Umsetzung hat drei
 * Erzeugnisse: das eigentliche Reformat, ein Nebengas und den freiwerdenden Wasserstoff.
 * Die Eingabe ist immer 100 mB.
 *
 * ABWEICHUNG: das Original benutzt eine HashMap. Hier ist es eine LinkedHashMap, damit die
 * ausgeschriebene Rezeptdatei bei jedem Start dieselbe Reihenfolge hat.
 */
public class ReformingRecipes extends SerializableRecipe {

    public static final Map<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> recipes = new LinkedHashMap<>();

    @Override
    public void registerDefaults() {
        recipes.put(Fluids.HEATINGOIL,      triple(Fluids.NAPHTHA, 50,        Fluids.PETROLEUM, 15,  Fluids.HYDROGEN, 10));
        recipes.put(Fluids.NAPHTHA,         triple(Fluids.REFORMATE, 50,      Fluids.PETROLEUM, 15,  Fluids.HYDROGEN, 10));
        recipes.put(Fluids.NAPHTHA_CRACK,   triple(Fluids.REFORMATE, 50,      Fluids.AROMATICS, 10,  Fluids.HYDROGEN, 5));
        recipes.put(Fluids.NAPHTHA_COKER,   triple(Fluids.REFORMATE, 50,      Fluids.REFORMGAS, 10,  Fluids.HYDROGEN, 5));
        recipes.put(Fluids.LIGHTOIL,        triple(Fluids.AROMATICS, 50,      Fluids.REFORMGAS, 10,  Fluids.HYDROGEN, 15));
        recipes.put(Fluids.LIGHTOIL_CRACK,  triple(Fluids.AROMATICS, 50,      Fluids.REFORMGAS, 5,   Fluids.HYDROGEN, 20));
        recipes.put(Fluids.PETROLEUM,       triple(Fluids.UNSATURATEDS, 85,   Fluids.REFORMGAS, 10,  Fluids.HYDROGEN, 5));
        recipes.put(Fluids.SOURGAS,         triple(Fluids.SULFURIC_ACID, 75,  Fluids.PETROLEUM, 10,  Fluids.HYDROGEN, 15));
        recipes.put(Fluids.CHOLESTEROL,     triple(Fluids.ESTRADIOL, 50,      Fluids.REFORMGAS, 35,  Fluids.HYDROGEN, 15));
    }

    private static Triplet<FluidStack, FluidStack, FluidStack> triple(FluidType a, int fillA, FluidType b, int fillB, FluidType c, int fillC) {
        return new Triplet<>(new FluidStack(a, fillA), new FluidStack(b, fillB), new FluidStack(c, fillC));
    }

    public static @Nullable Triplet<FluidStack, FluidStack, FluidStack> getOutput(FluidType type) {
        return recipes.get(type);
    }

    @Override public String getFileName() { return "hbmReforming.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

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
        FluidStack output3 = readFluidStack(obj.get("output3").getAsJsonArray());

        recipes.put(input, new Triplet<>(output1, output2, output3));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> rec = (Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>>) recipe;

        writer.name("input").value(rec.getKey().getUnlocalizedName());
        writer.name("output1"); writeFluidStack(rec.getValue().getX(), writer);
        writer.name("output2"); writeFluidStack(rec.getValue().getY(), writer);
        writer.name("output3"); writeFluidStack(rec.getValue().getZ(), writer);
    }
}
