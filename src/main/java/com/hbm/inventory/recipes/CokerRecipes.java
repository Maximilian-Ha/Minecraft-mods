package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.util.Tuple.Triplet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.CokerRecipes.
 *
 * Der Verkoker treibt aus einem Oel den Kohlenstoff aus: es bleibt fester Koks, und was sich
 * dabei leichter verfluechtigt, geht als Gas oder leichteres Oel weiter. Der erste Wert jedes
 * Eintrags ist die Menge in mB, die ein Durchgang kostet.
 *
 * ABWEICHUNG: das Original benutzt eine HashMap. Hier ist es eine LinkedHashMap, damit die
 * ausgeschriebene Rezeptdatei bei jedem Start dieselbe Reihenfolge hat.
 */
public class CokerRecipes extends SerializableRecipe {

    public static final Map<FluidType, Triplet<Integer, ItemStack, FluidStack>> recipes = new LinkedHashMap<>();

    /**
     * Was ein Stueck Petrolkoks an Waerme wert ist: 3200 Ticks Brenndauer mal 1,25 Bonus mal
     * 200 TU je Tick, plus 20.000 TU je Durchgang. Unveraendert aus dem Original.
     */
    private static final long TU_PER_COKE = 820_000L;

    @Override
    public void registerDefaults() {

        auto(Fluids.HEAVYOIL,             Fluids.OIL_COKER);
        auto(Fluids.HEAVYOIL_VACUUM,      Fluids.REFORMATE);
        auto(Fluids.COALCREOSOTE,         Fluids.NAPHTHA_COKER);
        auto(Fluids.SMEAR,                Fluids.OIL_COKER);
        auto(Fluids.HEATINGOIL,           Fluids.OIL_COKER);
        auto(Fluids.HEATINGOIL_VACUUM,    Fluids.OIL_COKER);
        auto(Fluids.RECLAIMED,            Fluids.NAPHTHA_COKER);
        auto(Fluids.NAPHTHA,              Fluids.NAPHTHA_COKER);
        auto(Fluids.NAPHTHA_DS,           Fluids.NAPHTHA_COKER);
        auto(Fluids.NAPHTHA_CRACK,        Fluids.NAPHTHA_COKER);
        auto(Fluids.DIESEL,               Fluids.NAPHTHA_COKER);
        auto(Fluids.DIESEL_REFORM,        Fluids.NAPHTHA_COKER);
        auto(Fluids.DIESEL_CRACK,         Fluids.GAS_COKER);
        auto(Fluids.DIESEL_CRACK_REFORM,  Fluids.GAS_COKER);
        auto(Fluids.LIGHTOIL,             Fluids.GAS_COKER);
        auto(Fluids.LIGHTOIL_DS,          Fluids.GAS_COKER);
        auto(Fluids.LIGHTOIL_CRACK,       Fluids.GAS_COKER);
        auto(Fluids.LIGHTOIL_VACUUM,      Fluids.GAS_COKER);
        auto(Fluids.BIOFUEL,              Fluids.GAS_COKER);
        auto(Fluids.AROMATICS,            Fluids.GAS_COKER);
        auto(Fluids.REFORMATE,            Fluids.GAS_COKER);
        auto(Fluids.XYLENE,               Fluids.GAS_COKER);
        auto(Fluids.FISHOIL,              Fluids.MERCURY);
        auto(Fluids.SUNFLOWEROIL,         Fluids.GAS_COKER);

        /* Holzoel gibt Holzkohle statt Petrolkoks und bringt weniger Waerme mit. */
        auto(Fluids.WOODOIL, 340_000L, new ItemStack(Items.CHARCOAL), Fluids.GAS_COKER);

        register(Fluids.WATZ, 4_000, new ItemStack(NtmItems.INGOT_MUD.get(), 4), null);
        register(Fluids.REDMUD, 450, new ItemStack(Items.IRON_INGOT), new FluidStack(Fluids.MERCURY, 50));
        register(Fluids.BITUMEN, 16_000, new ItemStack(NtmItems.COKE_PETROLEUM.get()), new FluidStack(Fluids.OIL_COKER, 1_600));
        register(Fluids.LUBRICANT, 12_000, new ItemStack(NtmItems.COKE_PETROLEUM.get()), new FluidStack(Fluids.OIL_COKER, 1_200));
        register(Fluids.CALCIUM_SOLUTION, 125, new ItemStack(NtmItems.POWDER_CALCIUM.get()), new FluidStack(Fluids.SPENTSTEAM, 100));
        /* Das einzige verkokbare Gas -- es geht allein um den Schwefel darin. */
        register(Fluids.SOURGAS, 1_000, new ItemStack(NtmItems.SULFUR.get()), new FluidStack(Fluids.GAS_COKER, 150));
        register(Fluids.SLOP, 1_000, new ItemStack(NtmItems.POWDER_LIMESTONE.get()), new FluidStack(Fluids.COLLOID, 250));
        register(Fluids.VITRIOL, 4_000, new ItemStack(NtmItems.POWDER_IRON.get()), new FluidStack(Fluids.SULFURIC_ACID, 500));
    }

    private static void auto(FluidType fluid, FluidType byproduct) {
        auto(fluid, TU_PER_COKE, new ItemStack(NtmItems.COKE_PETROLEUM.get()), byproduct);
    }

    /**
     * Wie viel von einem Oel ein Stueck Koks kostet: so viel, dass seine Waerme dem Koks
     * entspricht. Als Nebenprodukt faellt ein Zehntel davon an, mindestens aber zehn mB.
     */
    private static void auto(FluidType fluid, long tuPerCoke, ItemStack output, @Nullable FluidType byproduct) {

        long flammable = fluid.hasTrait(FT_Flammable.class) ? fluid.getTrait(FT_Flammable.class).getHeatEnergy() : 0L;
        long combustible = fluid.hasTrait(FT_Combustible.class) ? fluid.getTrait(FT_Combustible.class).getCombustionEnergy() : 0L;

        long tuPerBucket = Math.max(flammable, combustible);
        if(tuPerBucket <= 0L) return;

        int mB = (int) (tuPerCoke * 1000L / tuPerBucket);

        if(mB > 10_000) mB -= (mB % 1000);
        else if(mB > 1_000) mB -= (mB % 100);
        else if(mB > 100) mB -= (mB % 10);

        register(fluid, mB, output, byproduct == null ? null : new FluidStack(byproduct, Math.max(10, mB / 10)));
    }

    private static void register(FluidType fluid, int amount, @Nullable ItemStack output, @Nullable FluidStack byproduct) {
        recipes.put(fluid, new Triplet<>(amount, output, byproduct));
    }

    public static @Nullable Triplet<Integer, ItemStack, FluidStack> getOutput(FluidType fluid) {
        return recipes.get(fluid);
    }

    @Override public String getFileName() { return "hbmCoker.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public String getComment() {
        return "The fill of the input fluid stack is how many mB one operation costs.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        FluidStack in = readFluidStack(obj.get("input").getAsJsonArray());
        ItemStack out = obj.has("output") ? readItemStack(obj.get("output").getAsJsonArray()) : null;
        FluidStack byproduct = obj.has("byproduct") ? readFluidStack(obj.get("byproduct").getAsJsonArray()) : null;

        recipes.put(in.type, new Triplet<>(in.fill, out, byproduct));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, Triplet<Integer, ItemStack, FluidStack>> rec = (Map.Entry<FluidType, Triplet<Integer, ItemStack, FluidStack>>) recipe;

        writer.name("input"); writeFluidStack(new FluidStack(rec.getKey(), rec.getValue().getX()), writer);
        if(rec.getValue().getY() != null) { writer.name("output"); writeItemStack(rec.getValue().getY(), writer); }
        if(rec.getValue().getZ() != null) { writer.name("byproduct"); writeFluidStack(rec.getValue().getZ(), writer); }
    }
}
