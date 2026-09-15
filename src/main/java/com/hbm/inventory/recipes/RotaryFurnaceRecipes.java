package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.RotaryFurnaceRecipes.
 *
 * Der Drehrohrofen nimmt bis zu drei Gegenstaende und wahlweise ein Fluid und macht daraus
 * fluessiges Material. Anders als beim Lichtbogenofen steht die Zutatenliste fest; die
 * Reihenfolge, in der die Zutaten in den Faechern liegen, spielt keine Rolle.
 *
 * NICHT UEBERNOMMEN: die drei Rezepte auf Eisenbruchstuecke (IRON.fragment()). Die
 * Grundgesteinserze und ihre Bruchstuecke gibt es im Port noch nicht.
 *
 * ABWEICHUNG: das Original nimmt fuer Kohle und Koks OreDictionary-Namen. Im Port steht fuer
 * Kohle der Gegenstand selbst (die Form GEM hat im Port keinen Kohlegegenstand) und fuer Koks
 * ein eigener Tag, in dem die drei Kokssorten des Mods haengen.
 */
public class RotaryFurnaceRecipes extends SerializableRecipe {

    public static final List<RotaryFurnaceRecipe> recipes = new ArrayList<>();

    /** Sammeltag fuer alle Kokssorten -- das Gegenstueck zu ANY_COKE im Original. */
    public static final TagKey<Item> COKE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/coke"));

    @Override
    public void registerDefaults() {

        int ingot = MaterialShapes.INGOT.q(1);

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_STEEL, ingot), 100, 100,
                new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_IRON)), new ComparableStack(Items.COAL)));
        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_STEEL, ingot), 100, 100,
                new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_IRON)), new TagStack(COKE)));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_DESH, ingot), 100, 200,
                new FluidStack(Fluids.LIGHTOIL, 100), new ComparableStack(NtmItems.POWDER_DESH_READY.get())));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_GUNMETAL, MaterialShapes.INGOT.q(4)), 200, 100,
                new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_COPPER), 3), new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_ALUMINIUM), 1)));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_WEAPONSTEEL, ingot), 200, 400,
                new FluidStack(Fluids.GAS_COKER, 100),
                new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_STEEL), 1), new ComparableStack(NtmItems.POWDER_FLUX.get(), 2)));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_SATURN, MaterialShapes.INGOT.q(2)), 200, 400,
                new FluidStack(Fluids.REFORMGAS, 250),
                new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_DURA), 4), new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_COPPER))));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_SATURN, MaterialShapes.INGOT.q(4)), 200, 300,
                new FluidStack(Fluids.REFORMGAS, 250),
                new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_DURA), 4), new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_COPPER)),
                new ComparableStack(NtmItems.POWDER_BORAX.get())));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2)), 100, 400,
                new FluidStack(Fluids.SODIUM_ALUMINATE, 150)));

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(3)), 40, 200,
                new FluidStack(Fluids.SODIUM_ALUMINATE, 150), new ComparableStack(NtmItems.POWDER_FLUX.get(), 2)));
    }

    /**
     * Sucht das Rezept, das genau zu den eingelegten Gegenstaenden passt: jeder belegte Platz
     * muss eine Zutat treffen, und keine Zutat darf uebrig bleiben.
     */
    public static @Nullable RotaryFurnaceRecipe getRecipe(ItemStack... inputs) {

        outer:
        for(RotaryFurnaceRecipe recipe : recipes) {

            List<AStack> remaining = new ArrayList<>(List.of(recipe.ingredients));

            for(ItemStack input : inputs) {

                if(input.isEmpty()) continue;

                boolean hasMatch = false;
                Iterator<AStack> iterator = remaining.iterator();

                while(iterator.hasNext()) {
                    AStack ingredient = iterator.next();

                    if(ingredient.matchesRecipe(input, true) && input.getCount() >= ingredient.stacksize) {
                        hasMatch = true;
                        iterator.remove();
                        break;
                    }
                }

                if(!hasMatch) continue outer;
            }

            if(remaining.isEmpty()) return recipe;
        }

        return null;
    }

    @Override public String getFileName() { return "hbmRotaryFurnace.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        AStack[] inputs = readAStackArray(obj.get("inputs").getAsJsonArray());
        FluidStack fluid = obj.has("fluid") ? readFluidStack(obj.get("fluid").getAsJsonArray()) : null;

        JsonArray array = obj.get("output").getAsJsonArray();
        NTMMaterial mat = Mats.matByName.get(array.get(0).getAsString());
        if(mat == null) return;

        recipes.add(new RotaryFurnaceRecipe(new MaterialStack(mat, array.get(1).getAsInt()),
                obj.get("duration").getAsInt(), obj.get("steam").getAsInt(), fluid, inputs));
    }

    @Override
    public void writeRecipe(Object obj, JsonWriter writer) throws IOException {

        RotaryFurnaceRecipe recipe = (RotaryFurnaceRecipe) obj;

        writer.name("inputs").beginArray();
        for(AStack aStack : recipe.ingredients) writeAStack(aStack, writer);
        writer.endArray();

        if(recipe.fluid != null) {
            writer.name("fluid");
            writeFluidStack(recipe.fluid, writer);
        }

        writer.name("output").beginArray();
        writer.setIndent("");
        writer.value(recipe.output.material.names[0]).value(recipe.output.amount);
        writer.endArray();
        writer.setIndent("  ");

        writer.name("duration").value(recipe.duration);
        writer.name("steam").value(recipe.steam);
    }

    @Override
    public String getComment() {
        return "Recipes for the rotary kiln. Up to three items plus an optional fluid become molten material. "
                + "Duration is in ticks at base speed, steam is the amount of steam consumed per tick. "
                + "Output amounts are in quanta (1 quantum is 1/72 of an ingot).";
    }

    public static class RotaryFurnaceRecipe {

        public final AStack[] ingredients;
        public final FluidStack fluid;
        public final MaterialStack output;
        public final int duration;
        public final int steam;

        public RotaryFurnaceRecipe(MaterialStack output, int duration, int steam, @Nullable FluidStack fluid, AStack... ingredients) {
            this.ingredients = ingredients;
            this.fluid = fluid;
            this.output = output;
            this.duration = duration;
            this.steam = steam;
        }

        public RotaryFurnaceRecipe(MaterialStack output, int duration, int steam, AStack... ingredients) {
            this(output, duration, steam, null, ingredients);
        }
    }
}
