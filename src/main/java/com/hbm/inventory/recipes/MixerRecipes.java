package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.FuelAdditiveItem;
import com.hbm.items.NtmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.MixerRecipes.
 * Schluessel ist das Ausgangsfluid, der Wert die Liste der Rezepte, die es erzeugen --
 * im Mixer waehlt der Spieler ueber den Knopf zwischen ihnen aus.
 *
 * Die Erzdictionary-Eintraege des Originals sind auf die konkreten Items des Ports
 * abgebildet, da es im Port (noch) keine Staub-Tags gibt.
 */
public class MixerRecipes extends SerializableRecipe {

    public static final HashMap<FluidType, MixerRecipe[]> recipes = new HashMap<>();

    /** Ersatz fuer ANY_TAR.any() aus dem Original */
    private static final TagStack ANY_TARS = new TagStack(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_tars")));

    @Override
    public void registerDefaults() {
        register(Fluids.COOLANT, new MixerRecipe(2_000, 50).setStack1(new FluidStack(Fluids.WATER, 1_800)).setSolid(new ComparableStack(NtmItems.NITER.get())));
        register(Fluids.CRYOGEL, new MixerRecipe(2_000, 50).setStack1(new FluidStack(Fluids.COOLANT, 1_800)).setSolid(new ComparableStack(NtmItems.POWDER_ICE.get())));
        register(Fluids.NITAN, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.KEROSENE, 600)).setStack2(new FluidStack(Fluids.MERCURY, 200)).setSolid(new ComparableStack(NtmItems.POWDER_NITAN_MIX.get())));
        register(Fluids.FRACKSOL,
                new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.SULFURIC_ACID, 900)).setStack2(new FluidStack(Fluids.PETROLEUM, 100)),
                new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.WATER, 1000)).setStack2(new FluidStack(Fluids.PETROLEUM, 100)).setSolid(new ComparableStack(NtmItems.SULFUR.get())));
        register(Fluids.ENDERJUICE, new MixerRecipe(100, 100).setStack1(new FluidStack(Fluids.XPJUICE, 500)).setSolid(new ComparableStack(NtmItems.POWDER_DIAMOND.get())));
        register(Fluids.SALIENT, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.SEEDSLURRY, 500)).setStack2(new FluidStack(Fluids.BLOOD, 500)));
        register(Fluids.COLLOID, new MixerRecipe(500, 20).setStack1(new FluidStack(Fluids.WATER, 500)).setSolid(new ComparableStack(NtmItems.DUST.get())));
        register(Fluids.PHOSGENE, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.UNSATURATEDS, 500)).setStack2(new FluidStack(Fluids.CHLORINE, 500)));
        register(Fluids.MUSTARDGAS, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.REFORMGAS, 750)).setStack2(new FluidStack(Fluids.CHLORINE, 250)).setSolid(new ComparableStack(NtmItems.SULFUR.get())));
        register(Fluids.IONGEL, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.WATER, 1000)).setStack2(new FluidStack(Fluids.HYDROGEN, 200)).setSolid(new ComparableStack(NtmItems.PELLET_CHARGED.get())));
        register(Fluids.EGG, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.RADIOSOLVENT, 500)).setSolid(new ComparableStack(Items.EGG)));
        // Original: jeder Fisch (Metadaten-Wildcard). Im Port gibt es dafuer keinen Tag im Repo -- nur roher Kabeljau.
        register(Fluids.FISHOIL, new MixerRecipe(100, 50).setSolid(new ComparableStack(Items.COD)));
        register(Fluids.SUNFLOWEROIL, new MixerRecipe(100, 50).setSolid(new ComparableStack(Blocks.SUNFLOWER)));
        // Runde 10: Der Feinruss aus dem Industrieschornstein gibt es jetzt, damit auch dieses
        // Rezept (Original Z. 54).
        register(Fluids.FULLERENE, new MixerRecipe(250, 50).setStack1(new FluidStack(Fluids.RADIOSOLVENT, 500)).setSolid(new ComparableStack(NtmItems.POWDER_ASH_SOOT.get())));

        register(Fluids.SOLVENT,
                new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)),
                new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA_CRACK, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)),
                new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA_DS, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)),
                new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.NAPHTHA_COKER, 500)).setStack2(new FluidStack(Fluids.AROMATICS, 500)));
        register(Fluids.SULFURIC_ACID, new MixerRecipe(500, 50).setStack1(new FluidStack(Fluids.PEROXIDE, 800)).setSolid(new ComparableStack(NtmItems.SULFUR.get())));
        register(Fluids.NITRIC_ACID, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.SULFURIC_ACID, 500)).setSolid(new ComparableStack(NtmItems.NITER.get())));
        register(Fluids.RADIOSOLVENT, new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.REFORMGAS, 750)).setStack2(new FluidStack(Fluids.CHLORINE, 250)));
        register(Fluids.SCHRABIDIC, new MixerRecipe(16_000, 100).setStack1(new FluidStack(Fluids.SAS3, 8_000)).setStack2(new FluidStack(Fluids.PEROXIDE, 6_000)).setSolid(new ComparableStack(NtmItems.PELLET_CHARGED.get())));

        register(Fluids.PETROIL, new MixerRecipe(1_000, 30).setStack1(new FluidStack(Fluids.RECLAIMED, 800)).setStack2(new FluidStack(Fluids.LUBRICANT, 200)));
        register(Fluids.LUBRICANT,
                new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.HEATINGOIL, 500)).setStack2(new FluidStack(Fluids.UNSATURATEDS, 500)),
                new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.FISHOIL, 800)).setStack2(new FluidStack(Fluids.ETHANOL, 200)),
                new MixerRecipe(1_000, 20).setStack1(new FluidStack(Fluids.SUNFLOWEROIL, 800)).setStack2(new FluidStack(Fluids.ETHANOL, 200)));
        register(Fluids.BIOFUEL,
                new MixerRecipe(250, 20).setStack1(new FluidStack(Fluids.FISHOIL, 500)).setStack2(new FluidStack(Fluids.WOODOIL, 500)),
                new MixerRecipe(200, 20).setStack1(new FluidStack(Fluids.SUNFLOWEROIL, 500)).setStack2(new FluidStack(Fluids.WOODOIL, 500)));
        register(Fluids.NITROGLYCERIN,
                new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.PETROLEUM, 1_000)).setStack2(new FluidStack(Fluids.NITRIC_ACID, 1_000)),
                new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.FISHOIL, 500)).setStack2(new FluidStack(Fluids.NITRIC_ACID, 500)));

        register(Fluids.THORIUM_SALT, new MixerRecipe(1_000, 30).setStack1(new FluidStack(Fluids.CHLORINE, 1000)).setSolid(new ComparableStack(NtmItems.POWDER_THORIUM.get())));

        register(Fluids.SYNGAS, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.COALOIL, 500)).setStack2(new FluidStack(Fluids.STEAM, 500)));
        register(Fluids.OXYHYDROGEN,
                new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.HYDROGEN, 500)).setStack2(new FluidStack(Fluids.AIR, 2_000)),
                new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.HYDROGEN, 500)).setStack2(new FluidStack(Fluids.OXYGEN, 500)));

        register(Fluids.PETROIL_LEADED, new MixerRecipe(12_000, 40).setStack1(new FluidStack(Fluids.PETROIL, 10_000)).setSolid(new ComparableStack(NtmItems.FUEL_ADDITIVE.get(), 1, FuelAdditiveItem.Type.ANTIKNOCK)));
        register(Fluids.GASOLINE_LEADED, new MixerRecipe(12_000, 40).setStack1(new FluidStack(Fluids.GASOLINE, 10_000)).setSolid(new ComparableStack(NtmItems.FUEL_ADDITIVE.get(), 1, FuelAdditiveItem.Type.ANTIKNOCK)));
        register(Fluids.COALGAS_LEADED, new MixerRecipe(12_000, 40).setStack1(new FluidStack(Fluids.COALGAS, 10_000)).setSolid(new ComparableStack(NtmItems.FUEL_ADDITIVE.get(), 1, FuelAdditiveItem.Type.ANTIKNOCK)));

        register(Fluids.DIESEL_REFORM, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.DIESEL, 900)).setStack2(new FluidStack(Fluids.REFORMATE, 100)));
        register(Fluids.DIESEL_CRACK_REFORM, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.DIESEL_CRACK, 900)).setStack2(new FluidStack(Fluids.REFORMATE, 100)));
        register(Fluids.KEROSENE_REFORM, new MixerRecipe(1_000, 50).setStack1(new FluidStack(Fluids.KEROSENE, 900)).setStack2(new FluidStack(Fluids.REFORMATE, 100)));

        register(Fluids.CHLOROCALCITE_SOLUTION, new MixerRecipe(500, 50).setStack1(new FluidStack(Fluids.WATER, 250)).setStack2(new FluidStack(Fluids.NITRIC_ACID, 250)).setSolid(new ComparableStack(NtmItems.POWDER_CHLOROCALCITE.get())));
        register(Fluids.CHLOROCALCITE_MIX, new MixerRecipe(1000, 50).setStack1(new FluidStack(Fluids.CHLOROCALCITE_SOLUTION, 500)).setStack2(new FluidStack(Fluids.SULFURIC_ACID, 500)).setSolid(new ComparableStack(NtmItems.POWDER_FLUX.get())));
        // Original: Fluids.PHEROMONE_M mit ModItems.pill_herbal -- das Item fehlt im Port, Rezept ausgelassen.

        register(Fluids.BAUXITE_SOLUTION, new MixerRecipe(300, 80).setStack1(new FluidStack(Fluids.LYE, 50)).setSolid(new ComparableStack(NtmBlocks.RESOURCE_BAUXITE.get())));
        register(Fluids.LYE, new MixerRecipe(100, 100).setStack1(new FluidStack(Fluids.WATER, 100)).setSolid(new ComparableStack(NtmItems.POWDER_ASH_WOOD.get())));
        register(Fluids.ALUMINA, new MixerRecipe(200, 40).setStack1(new FluidStack(Fluids.SODIUM_ALUMINATE, 150)).setSolid(new ComparableStack(NtmItems.FLUORITE.get(), 3)),
                                 new MixerRecipe(300, 40).setStack1(new FluidStack(Fluids.SODIUM_ALUMINATE, 150)).setSolid(new ComparableStack(NtmItems.CHUNK_CRYOLITE.get())));

        register(Fluids.PERFLUOROMETHYL, new MixerRecipe(1000, 20).setStack1(new FluidStack(Fluids.PETROLEUM, 1000)).setStack2(new FluidStack(Fluids.UNSATURATEDS, 500)).setSolid(new ComparableStack(NtmItems.FLUORITE.get())));

        register(Fluids.BITUMEN, new MixerRecipe(50, 20).setSolid(ANY_TARS));
    }

    public static void register(FluidType type, MixerRecipe... rec) {
        recipes.put(type, rec);
    }

    public static MixerRecipe[] getOutput(FluidType type) {
        return recipes.get(type);
    }

    public static MixerRecipe getOutput(FluidType type, int index) {
        MixerRecipe[] recs = recipes.get(type);

        if(recs == null || recs.length == 0) return null;

        return recs[index % recs.length];
    }

    @Override
    public String getFileName() {
        return "hbmMixer.json";
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
        JsonObject obj = (JsonObject) recipe;

        FluidType outputType = Fluids.fromName(obj.get("outputType").getAsString());
        JsonArray recipeArray = obj.get("recipes").getAsJsonArray();
        MixerRecipe[] array = new MixerRecipe[recipeArray.size()];

        for(int i = 0; i < recipeArray.size(); i++) {
            JsonObject sub = recipeArray.get(i).getAsJsonObject();
            MixerRecipe mix = new MixerRecipe(sub.get("outputAmount").getAsInt(), sub.get("duration").getAsInt());

            if(sub.has("input1")) mix.setStack1(readFluidStack(sub.get("input1").getAsJsonArray()));
            if(sub.has("input2")) mix.setStack2(readFluidStack(sub.get("input2").getAsJsonArray()));
            if(sub.has("solidInput")) mix.setSolid(readAStack(sub.get("solidInput").getAsJsonArray()));

            array[i] = mix;
        }

        recipes.put(outputType, array);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Entry<FluidType, MixerRecipe[]> rec = (Entry<FluidType, MixerRecipe[]>) recipe;
        MixerRecipe[] entries = rec.getValue();

        writer.name("outputType").value(rec.getKey().getUnlocalizedName());
        writer.name("recipes").beginArray();

        for(MixerRecipe mix : entries) {
            writer.beginObject();
            writer.name("duration").value(mix.processTime);
            writer.name("outputAmount").value(mix.output);

            if(mix.input1 != null) { writer.name("input1"); writeFluidStack(mix.input1, writer); }
            if(mix.input2 != null) { writer.name("input2"); writeFluidStack(mix.input2, writer); }
            if(mix.solidInput != null) { writer.name("solidInput"); writeAStack(mix.solidInput, writer); }
            writer.endObject();
        }
        writer.endArray();
    }

    public static class MixerRecipe {
        public FluidStack input1;
        public FluidStack input2;
        public AStack solidInput;
        public int processTime;
        public int output;

        public MixerRecipe(int output, int processTime) {
            this.output = output;
            this.processTime = processTime;
        }

        public MixerRecipe setStack1(FluidStack stack) { input1 = stack; return this; }
        public MixerRecipe setStack2(FluidStack stack) { input2 = stack; return this; }
        public MixerRecipe setSolid(AStack stack) { solidInput = stack; return this; }
    }
}
