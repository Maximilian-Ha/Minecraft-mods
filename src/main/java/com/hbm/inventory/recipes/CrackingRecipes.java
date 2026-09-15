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
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CrackingRecipes.
 *
 * Welches Oel unter Dampf in welche zwei Fraktionen zerfaellt. Die Eingabe ist immer 100 mB,
 * die Ausgabemengen stehen im Rezept.
 *
 * Der Krackturm selbst ist noch nicht portiert; diese Liste haengt hier trotzdem nicht in der
 * Luft: die Radiolyse uebernimmt sie vollstaendig, weil harte Strahlung dieselben Bindungen
 * aufbricht wie Hitze. So bleiben beide Wege dauerhaft deckungsgleich -- genau die Begruendung,
 * die das Original in RadiolysisRecipes gibt.
 *
 * ABWEICHUNG: das Original benutzt eine HashMap. Hier ist es eine LinkedHashMap, damit die
 * ausgeschriebene Rezeptdatei bei jedem Start dieselbe Reihenfolge hat.
 */
public class CrackingRecipes extends SerializableRecipe {

    /* Ausbeute in Prozent, unveraendert aus dem Original. */
    public static final int oil_crack_oil = 80;
    public static final int oil_crack_petro = 20;
    public static final int bitumen_crack_oil = 80;
    public static final int bitumen_crack_aroma = 20;
    public static final int smear_crack_napht = 60;
    public static final int smear_crack_petro = 40;
    public static final int gas_crack_petro = 30;
    public static final int gas_crack_unsat = 20;
    public static final int diesel_crack_kero = 40;
    public static final int diesel_crack_petro = 30;
    public static final int kero_crack_petro = 60;
    public static final int wood_crack_aroma = 10;
    public static final int wood_crack_heat = 40;
    public static final int xyl_crack_aroma = 80;
    public static final int xyl_crack_petro = 20;

    public static final Map<FluidType, Pair<FluidStack, FluidStack>> cracking = new LinkedHashMap<>();

    @Override
    public void registerDefaults() {
        cracking.put(Fluids.OIL,               pair(Fluids.CRACKOIL,     oil_crack_oil,       Fluids.PETROLEUM,    oil_crack_petro));
        cracking.put(Fluids.BITUMEN,           pair(Fluids.OIL,          bitumen_crack_oil,   Fluids.AROMATICS,    bitumen_crack_aroma));
        cracking.put(Fluids.SMEAR,             pair(Fluids.NAPHTHA,      smear_crack_napht,   Fluids.PETROLEUM,    smear_crack_petro));
        cracking.put(Fluids.GAS,               pair(Fluids.PETROLEUM,    gas_crack_petro,     Fluids.UNSATURATEDS, gas_crack_unsat));
        cracking.put(Fluids.DIESEL,            pair(Fluids.KEROSENE,     diesel_crack_kero,   Fluids.PETROLEUM,    diesel_crack_petro));
        cracking.put(Fluids.DIESEL_CRACK,      pair(Fluids.KEROSENE,     diesel_crack_kero,   Fluids.PETROLEUM,    diesel_crack_petro));
        cracking.put(Fluids.KEROSENE,          pair(Fluids.PETROLEUM,    kero_crack_petro,    Fluids.NONE,         0));
        cracking.put(Fluids.WOODOIL,           pair(Fluids.HEATINGOIL,   wood_crack_heat,     Fluids.AROMATICS,    wood_crack_aroma));
        cracking.put(Fluids.XYLENE,            pair(Fluids.AROMATICS,    xyl_crack_aroma,     Fluids.PETROLEUM,    xyl_crack_petro));
        cracking.put(Fluids.HEATINGOIL_VACUUM, pair(Fluids.HEATINGOIL,   80,                  Fluids.REFORMGAS,    20));
        cracking.put(Fluids.REFORMATE,         pair(Fluids.UNSATURATEDS, 40,                  Fluids.REFORMGAS,    60));
        cracking.put(Fluids.BIOGAS,            pair(Fluids.PETROLEUM,    20,                  Fluids.AROMATICS,    20));
    }

    private static Pair<FluidStack, FluidStack> pair(FluidType left, int leftAmount, FluidType right, int rightAmount) {
        return new Pair<>(new FluidStack(left, leftAmount), new FluidStack(right, rightAmount));
    }

    public static @Nullable Pair<FluidStack, FluidStack> getCracking(FluidType oil) {
        return cracking.get(oil);
    }

    /** Die Radiolyse baut ihre eigene Liste darauf auf. */
    public static Map<FluidType, Pair<FluidStack, FluidStack>> getCrackingRecipes() {
        return cracking;
    }

    /**
     * ABWEICHUNG: das Original ruft registerRadiolysis() weit hinten in MainRegistry auf und
     * prueft dort selbst, ob die Krackliste schon steht. Hier haengt der Aufruf direkt an der
     * Liste, von der er abhaengt -- dann kann die Reihenfolge gar nicht erst brechen.
     */
    @Override
    public void registerPost() {
        RadiolysisRecipes.registerRadiolysis();
    }

    @Override public String getFileName() { return "hbmCracking.json"; }
    @Override public Object getRecipeObject() { return cracking; }
    @Override public void deleteRecipes() { cracking.clear(); }

    @Override
    public String getComment() {
        return "Inputs are always 100mB, set output quantities accordingly. The steam in/outputs are fixed, using 200mB of steam per 100mB of input.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        FluidType input = Fluids.fromName(obj.get("input").getAsString());
        FluidStack output1 = readFluidStack(obj.get("output1").getAsJsonArray());
        FluidStack output2 = readFluidStack(obj.get("output2").getAsJsonArray());

        cracking.put(input, new Pair<>(output1, output2));
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
