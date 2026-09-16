package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.special.BedrockOreItem;
import com.hbm.items.special.BedrockOreItem.BedrockOreGrade;
import com.hbm.items.special.BedrockOreItem.BedrockOreType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PyroOvenRecipes.
 *
 * Der Pyroofen zerlegt unter Hitze und Sauerstoffabschluss. Jedes Rezept nimmt wahlweise ein
 * Fluid, einen Gegenstand oder beides und gibt wahlweise beides zurueck.
 *
 * ABWEICHUNGEN:
 *  - das Original nimmt fuer Kohle, Koks, Teer und Wolframstaub OreDictionary-Namen. Im Port
 *    steht fuer Kohle der Gegenstand selbst, fuer Kohlenstaub der eigene Gegenstand, fuer Koks
 *    und Teer je ein Sammeltag und fuer Wolframstaub der Materialtag.
 *  - NICHT UEBERNOMMEN: nichts. Alle Rezepte des Originals sind hier.
 */
public class PyroOvenRecipes extends SerializableRecipe {

    public static final List<PyroOvenRecipe> recipes = new ArrayList<>();

    /** Sammeltag fuer alle Teersorten -- das Gegenstueck zu ANY_TAR.any() im Original. */
    private static final TagKey<Item> ANY_TARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_tars"));

    /**
     * Wie viel Waermeenergie ein Brennstoffwuerfel wert ist: 3200 Ticks Brenndauer mal 1,5
     * Bonus mal 300 TU je Tick. Unveraendert aus dem Original.
     */
    private static final long TU_PER_SOLID_FUEL = 1_440_000L;
    /** Und der des Balefire-Wuerfels -- er ist um Groessenordnungen dichter. */
    private static final long TU_PER_BALEFIRE_FUEL = 24_000_000L;

    @Override
    public void registerDefaults() {

        /* Brennstoffwuerfel. Der Pyroofen ist dabei doppelt so sparsam wie der Verfestiger --
         * siehe den Bonus in autoSolidFuel. */
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

        /* Grundgesteinserz roesten: jede Sorte in jeder der fuenf Vorstufen, jedes Mal faellt
         * Vitriol an. */
        for(BedrockOreType type : BedrockOreType.values()) {
            roast(type, BedrockOreGrade.BASE, BedrockOreGrade.BASE_ROASTED);
            roast(type, BedrockOreGrade.PRIMARY, BedrockOreGrade.PRIMARY_ROASTED);
            roast(type, BedrockOreGrade.SULFURIC_BYPRODUCT, BedrockOreGrade.SULFURIC_ROASTED);
            roast(type, BedrockOreGrade.SOLVENT_BYPRODUCT, BedrockOreGrade.SOLVENT_ROASTED);
            roast(type, BedrockOreGrade.RAD_BYPRODUCT, BedrockOreGrade.RAD_ROASTED);
        }

        /* Dampf zu Synthesegas steht 1:2, Synthesegas zu Altdampf hier 2:1 -- damit laesst
         * sich der Kreis tatsaechlich schliessen. */
        recipes.add(new PyroOvenRecipe(300)
                .in(new FluidStack(Fluids.SYNGAS, 2_000)).in(new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_TUNGSTEN)))
                .out(new FluidStack(Fluids.SPENTSTEAM, 1_000)).out(new ItemStack(NtmItems.INGOT_TUNGSTEN_CARBIDE.get())));

        /* Synthesegas aus Kohle */
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.STEAM, 500)).in(new ComparableStack(Items.COAL))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.STEAM, 500)).in(new ComparableStack(NtmItems.POWDER_COAL.get()))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.STEAM, 250)).in(new TagStack(RotaryFurnaceRecipes.COKE))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)));

        /* Synthesegas aus Biomasse */
        recipes.add(new PyroOvenRecipe(100)
                .in(new ComparableStack(NtmItems.BIOMASS.get(), 4))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)).out(new ItemStack(Items.CHARCOAL)));

        /* Russ aus Teer */
        recipes.add(new PyroOvenRecipe(40)
                .in(new FluidStack(Fluids.HYDROGEN, 250)).in(new TagStack(ANY_TARS, 4))
                .out(new FluidStack(Fluids.CARBONDIOXIDE, 1_000)).out(new ItemStack(NtmItems.POWDER_ASH_SOOT.get())));

        /* Schweroel aus Kohle */
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.HYDROGEN, 500)).in(new ComparableStack(Items.COAL))
                .out(new FluidStack(Fluids.HEAVYOIL, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.HYDROGEN, 500)).in(new ComparableStack(NtmItems.POWDER_COAL.get()))
                .out(new FluidStack(Fluids.HEAVYOIL, 1_000)));

        /* Kohlegas aus Kohle */
        recipes.add(new PyroOvenRecipe(50)
                .in(new FluidStack(Fluids.HEAVYOIL, 500)).in(new ComparableStack(Items.COAL))
                .out(new FluidStack(Fluids.COALGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(50)
                .in(new FluidStack(Fluids.HEAVYOIL, 500)).in(new ComparableStack(NtmItems.POWDER_COAL.get()))
                .out(new FluidStack(Fluids.COALGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(50)
                .in(new FluidStack(Fluids.HEAVYOIL, 500)).in(new TagStack(RotaryFurnaceRecipes.COKE))
                .out(new FluidStack(Fluids.COALGAS, 1_000)));

        /* Reformgas aus Kokergas */
        recipes.add(new PyroOvenRecipe(60)
                .in(new FluidStack(Fluids.GAS_COKER, 4_000))
                .out(new FluidStack(Fluids.REFORMGAS, 100)));

        /* Wasserstoff und Kohlenstoff aus Erdgas */
        recipes.add(new PyroOvenRecipe(60)
                .in(new FluidStack(Fluids.GAS, 12_000))
                .out(new FluidStack(Fluids.HYDROGEN, 8_000)).out(new ItemStack(NtmItems.INGOT_GRAPHITE.get())));
    }

    private static void roast(BedrockOreType type, BedrockOreGrade from, BedrockOreGrade to) {
        recipes.add(new PyroOvenRecipe(10)
                .in(new ComparableStack(BedrockOreItem.make(from, type)))
                .out(new FluidStack(Fluids.VITRIOL, 50))
                .out(BedrockOreItem.make(to, type)));
    }

    private static void autoSolidFuel(FluidType fluid) {
        autoSolidFuel(fluid, TU_PER_SOLID_FUEL, NtmItems.SOLID_FUEL.get());
    }

    /**
     * Dieselbe Rechnung wie in SolidificationRecipes, nur mit einem Bonus statt einem
     * Aufschlag: der Pyroofen holt aus derselben Menge doppelt so viele Wuerfel heraus.
     */
    private static void autoSolidFuel(FluidType fluid, long tuPerUnit, Item fuel) {

        FT_Flammable flammable = fluid.getTrait(FT_Flammable.class);
        if(flammable == null) return;

        long tuPerBucket = flammable.getHeatEnergy();
        if(tuPerBucket <= 0) return;

        double bonus = 0.5D;
        int mB = (int) (tuPerUnit * 1000L * bonus / tuPerBucket);

        if(mB > 10_000) mB -= (mB % 1000);
        else if(mB > 1_000) mB -= (mB % 100);
        else if(mB > 100) mB -= (mB % 10);

        recipes.add(new PyroOvenRecipe(60).in(new FluidStack(fluid, Math.max(mB, 1))).out(new ItemStack(fuel)));
    }

    @Override public String getFileName() { return "hbmPyrolysis.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        AStack inputItem = obj.has("inputItem") ? readAStack(obj.get("inputItem").getAsJsonArray()) : null;
        FluidStack inputFluid = obj.has("inputFluid") ? readFluidStack(obj.get("inputFluid").getAsJsonArray()) : null;
        ItemStack outputItem = obj.has("outputItem") ? readItemStack(obj.get("outputItem").getAsJsonArray()) : null;
        FluidStack outputFluid = obj.has("outputFluid") ? readFluidStack(obj.get("outputFluid").getAsJsonArray()) : null;
        int duration = obj.get("duration").getAsInt();

        recipes.add(new PyroOvenRecipe(duration).in(inputFluid).in(inputItem).out(outputFluid).out(outputItem));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        PyroOvenRecipe rec = (PyroOvenRecipe) recipe;

        if(rec.inputFluid != null) { writer.name("inputFluid"); writeFluidStack(rec.inputFluid, writer); }
        if(rec.inputItem != null) { writer.name("inputItem"); writeAStack(rec.inputItem, writer); }
        if(rec.outputFluid != null) { writer.name("outputFluid"); writeFluidStack(rec.outputFluid, writer); }
        if(rec.outputItem != null) { writer.name("outputItem"); writeItemStack(rec.outputItem, writer); }
        writer.name("duration").value(rec.duration);
    }

    public static class PyroOvenRecipe {

        public @Nullable FluidStack inputFluid;
        public @Nullable AStack inputItem;
        public @Nullable FluidStack outputFluid;
        public @Nullable ItemStack outputItem;
        public final int duration;

        public PyroOvenRecipe(int duration) {
            this.duration = duration;
        }

        public PyroOvenRecipe in(@Nullable FluidStack stack) { this.inputFluid = stack; return this; }
        public PyroOvenRecipe in(@Nullable AStack stack) { this.inputItem = stack; return this; }
        public PyroOvenRecipe out(@Nullable FluidStack stack) { this.outputFluid = stack; return this; }
        public PyroOvenRecipe out(@Nullable ItemStack stack) { this.outputItem = stack; return this; }
    }
}
