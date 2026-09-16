package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.VacuumRefineryRecipes.
 *
 * Die Vakuumdestille zieht aus einem Oel vier Fraktionen statt der zwei des
 * Fraktionierturms -- dafuer muss das Oel unter Druck stehen. Die Eingabe ist immer 100 mB
 * und laesst sich nicht aendern.
 *
 * ABWEICHUNG: das Original benutzt eine HashMap. Hier ist es eine LinkedHashMap, damit die
 * ausgeschriebene Rezeptdatei bei jedem Start dieselbe Reihenfolge hat.
 */
public class VacuumRefineryRecipes extends SerializableRecipe {

    /* Ausbeute je Fraktion, unveraendert aus dem Original. */
    public static final int VAC_FRAC_HEAVY = 40;
    public static final int VAC_FRAC_REFORM = 25;
    public static final int VAC_FRAC_LIGHT = 20;
    public static final int VAC_FRAC_SOUR = 15;

    public static final Map<FluidType, VacuumRefineryRecipe> recipes = new LinkedHashMap<>();

    @Override
    public void registerDefaults() {
        recipes.put(Fluids.OIL, new VacuumRefineryRecipe(
                new FluidStack(Fluids.HEAVYOIL_VACUUM, VAC_FRAC_HEAVY),
                new FluidStack(Fluids.REFORMATE, VAC_FRAC_REFORM),
                new FluidStack(Fluids.LIGHTOIL_VACUUM, VAC_FRAC_LIGHT),
                new FluidStack(Fluids.SOURGAS, VAC_FRAC_SOUR)));

        /* Entschwefeltes Oel gibt statt Sauergas Reformgas -- der Schwefel ist schon weg. */
        recipes.put(Fluids.OIL_DS, new VacuumRefineryRecipe(
                new FluidStack(Fluids.HEAVYOIL_VACUUM, VAC_FRAC_HEAVY),
                new FluidStack(Fluids.REFORMATE, VAC_FRAC_REFORM),
                new FluidStack(Fluids.LIGHTOIL_VACUUM, VAC_FRAC_LIGHT),
                new FluidStack(Fluids.REFORMGAS, VAC_FRAC_SOUR)));
    }

    public static @Nullable VacuumRefineryRecipe getVacuum(FluidType oil) {
        return recipes.get(oil);
    }

    @Override public String getFileName() { return "hbmVacRefinery.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public String getComment() {
        return "Inputs always assume 100mB, input ammount cannot be changed.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = recipe.getAsJsonObject();

        FluidType type = Fluids.fromName(obj.get("input").getAsString());
        FluidStack o0 = readFluidStack(obj.get("output0").getAsJsonArray());
        FluidStack o1 = readFluidStack(obj.get("output1").getAsJsonArray());
        FluidStack o2 = readFluidStack(obj.get("output2").getAsJsonArray());
        FluidStack o3 = readFluidStack(obj.get("output3").getAsJsonArray());

        recipes.put(type, new VacuumRefineryRecipe(o0, o1, o2, o3));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, VacuumRefineryRecipe> rec = (Map.Entry<FluidType, VacuumRefineryRecipe>) recipe;

        writer.name("input").value(rec.getKey().getUnlocalizedName());

        for(int i = 0; i < rec.getValue().outputs.length; i++) {
            writer.name("output" + i);
            writeFluidStack(rec.getValue().outputs[i], writer);
        }
    }

    public static class VacuumRefineryRecipe {

        public final FluidStack[] outputs;

        public VacuumRefineryRecipe(FluidStack f0, FluidStack f1, FluidStack f2, FluidStack f3) {
            this.outputs = new FluidStack[] { f0, f1, f2, f3 };
        }
    }
}
