package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.util.Tuple.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.SolidificationRecipes.
 *
 * Was der Verfestiger aus welcher Fluessigkeit macht. Der erste Wert jedes Eintrags ist die
 * Menge in mB, die ein Erzeugnis kostet.
 *
 * ABWEICHUNGEN:
 *  - das Original benutzt eine HashMap; hier ist es eine LinkedHashMap, damit die
 *    ausgeschriebene Rezeptdatei ihre Reihenfolge behaelt.
 *  - NICHT UEBERNOMMEN: Quecksilber. ingot_mercury gibt es im Port nicht, wie schon in
 *    CrystallizerRecipes vermerkt.
 *  - das Original traegt Balefire zweimal ein: erst fest mit 250 mB, danach ueber die
 *    Automatik, die den festen Wert wieder ueberschreibt. Hier steht nur die Automatik,
 *    der feste Eintrag waere ohne Wirkung.
 */
public class SolidificationRecipes extends SerializableRecipe {

    public static final Map<FluidType, Pair<Integer, ItemStack>> recipes = new LinkedHashMap<>();

    /**
     * Wie viel Waermeenergie ein Brennstoffwuerfel wert ist: 3200 Ticks Brenndauer mal 1,5
     * Bonus mal 300 TU je Tick. Unveraendert aus dem Original.
     */
    private static final long TU_PER_SOLID_FUEL = 1_440_000L;
    /** Und der des Balefire-Wuerfels -- er ist um Groessenordnungen dichter. */
    private static final long TU_PER_BALEFIRE_FUEL = 24_000_000L;

    @Override
    public void registerDefaults() {

        register(Fluids.WATER, 1000, Items.ICE);
        register(Fluids.LAVA, 1000, Items.OBSIDIAN);
        register(Fluids.BIOGAS, 250, new ItemStack(NtmItems.BIOMASS_COMPRESSED.get(), 4));
        /* 4 Nahrwert * 2 Saettigung * 2 (fester Faktor) * 10 Quanten * 8 je Los. */
        register(Fluids.SALIENT, 1280, new ItemStack(NtmItems.BIO_WAFER.get(), 8));
        register(Fluids.ENDERJUICE, 100, Items.ENDER_PEARL);
        register(Fluids.WATZ, 1000, NtmItems.INGOT_MUD.get());
        register(Fluids.REDMUD, 450, Items.IRON_INGOT);
        register(Fluids.SODIUM, 100, NtmItems.POWDER_SODIUM.get());
        register(Fluids.LEAD, 100, NtmItems.INGOT_LEAD.get());
        register(Fluids.SLOP, 250, NtmBlocks.ORE_OIL_SAND.get());

        /* Die Teere. Der Port fuehrt sie als sechs eigene Gegenstaende statt als einen mit
         * Metadaten -- die Zuordnung ist dieselbe wie im Original. */
        register(Fluids.OIL, SF_OIL, NtmItems.OIL_TAR_CRUDE.get());
        register(Fluids.CRACKOIL, SF_CRACK, NtmItems.OIL_TAR_CRACK.get());
        register(Fluids.COALOIL, SF_COALOIL, NtmItems.OIL_TAR_COAL.get());
        register(Fluids.HEAVYOIL, SF_HEAVY, NtmItems.OIL_TAR_CRUDE.get());
        register(Fluids.HEAVYOIL_VACUUM, SF_HEAVY, NtmItems.OIL_TAR_CRUDE.get());
        register(Fluids.BITUMEN, SF_BITUMEN, NtmItems.OIL_TAR_CRUDE.get());
        register(Fluids.COALCREOSOTE, SF_CREOSOTE, NtmItems.OIL_TAR_COAL.get());
        register(Fluids.WOODOIL, SF_WOOD, NtmItems.OIL_TAR_WOOD.get());
        register(Fluids.LUBRICANT, SF_LUBE, NtmItems.OIL_TAR_PARAFFIN.get());

        /* Alles, was brennt, laesst sich zu Brennstoffwuerfeln pressen. Wie viel es dafuer
         * braucht, rechnet der Waermewert des Stoffes aus -- siehe autoSolidFuel. */
        autoSolidFuel(Fluids.SMEAR);
        autoSolidFuel(Fluids.HEATINGOIL);
        autoSolidFuel(Fluids.HEATINGOIL_VACUUM);
        autoSolidFuel(Fluids.RECLAIMED);
        autoSolidFuel(Fluids.PETROIL);
        autoSolidFuel(Fluids.NAPHTHA);
        autoSolidFuel(Fluids.NAPHTHA_CRACK);
        autoSolidFuel(Fluids.DIESEL);
        autoSolidFuel(Fluids.DIESEL_REFORM);
        autoSolidFuel(Fluids.DIESEL_CRACK);
        autoSolidFuel(Fluids.DIESEL_CRACK_REFORM);
        autoSolidFuel(Fluids.LIGHTOIL);
        autoSolidFuel(Fluids.LIGHTOIL_CRACK);
        autoSolidFuel(Fluids.LIGHTOIL_VACUUM);
        autoSolidFuel(Fluids.KEROSENE);
        autoSolidFuel(Fluids.KEROSENE_REFORM);
        autoSolidFuel(Fluids.SOURGAS);
        autoSolidFuel(Fluids.REFORMGAS);
        autoSolidFuel(Fluids.SYNGAS);
        autoSolidFuel(Fluids.PETROLEUM);
        autoSolidFuel(Fluids.LPG);
        autoSolidFuel(Fluids.BIOFUEL);
        autoSolidFuel(Fluids.AROMATICS);
        autoSolidFuel(Fluids.UNSATURATEDS);
        autoSolidFuel(Fluids.REFORMATE);
        autoSolidFuel(Fluids.XYLENE);
        autoSolidFuel(Fluids.BALEFIRE, TU_PER_BALEFIRE_FUEL, NtmItems.SOLID_FUEL_BF.get());
    }

    /* Die Mengen der Teere, unveraendert aus dem Original. */
    public static final int SF_OIL = 200;
    public static final int SF_CRACK = 200;
    public static final int SF_HEAVY = 150;
    public static final int SF_BITUMEN = 100;
    public static final int SF_LUBE = 100;
    public static final int SF_COALOIL = 200;
    public static final int SF_CREOSOTE = 200;
    public static final int SF_WOOD = 1000;

    private static void register(FluidType fluid, int amount, ItemLike item) {
        register(fluid, amount, new ItemStack(item));
    }

    private static void register(FluidType fluid, int amount, ItemStack stack) {
        recipes.put(fluid, new Pair<>(amount, stack));
    }

    /**
     * Wie viel von einem brennbaren Stoff ein Brennstoffwuerfel kostet: so viel, dass seine
     * Waerme dem Wuerfel entspricht, plus fuenfundzwanzig Prozent Aufschlag fuers Pressen.
     * Danach wird die Zahl geglaettet, damit keine krummen Werte in der Liste stehen.
     */
    private static void autoSolidFuel(FluidType fluid) {
        autoSolidFuel(fluid, TU_PER_SOLID_FUEL, NtmItems.SOLID_FUEL.get());
    }

    private static void autoSolidFuel(FluidType fluid, long tuPerUnit, Item fuel) {

        FT_Flammable flammable = fluid.getTrait(FT_Flammable.class);
        if(flammable == null) return;

        long tuPerBucket = flammable.getHeatEnergy();
        if(tuPerBucket <= 0) return;

        double penalty = 1.25D;
        int mB = (int) (tuPerUnit * 1000L * penalty / tuPerBucket);

        if(mB > 10_000) mB -= (mB % 1000);
        else if(mB > 1_000) mB -= (mB % 100);
        else if(mB > 100) mB -= (mB % 10);

        register(fluid, Math.max(mB, 1), fuel);
    }

    public static @Nullable Pair<Integer, ItemStack> getOutput(FluidType fluid) {
        return recipes.get(fluid);
    }

    @Override public String getFileName() { return "hbmSolidifier.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public String getComment() {
        return "The fill of the input fluid stack is how many mB one output costs.";
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        FluidStack input = readFluidStack(obj.get("input").getAsJsonArray());
        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());

        recipes.put(input.type, new Pair<>(input.fill, output));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<FluidType, Pair<Integer, ItemStack>> rec = (Map.Entry<FluidType, Pair<Integer, ItemStack>>) recipe;

        writer.name("input"); writeFluidStack(new FluidStack(rec.getKey(), rec.getValue().getKey()), writer);
        writer.name("output"); writeItemStack(rec.getValue().getValue(), writer);
    }
}
