package com.hbm.inventory.recipes;

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
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.OutgasserRecipes.
 *
 * Was die Bestrahlungssaeule des RBMK aus einem Gegenstand macht. Anders als bei den meisten
 * Maschinen gibt es keine Dauer im Rezept: wie schnell es geht, haengt allein am Neutronenfluss.
 *
 * NICHT UEBERNOMMEN: die Rezepte auf Kohle- und Wachsteer sowie auf Pilze und Pilzsuppe. Die
 * Gegenstaende oil_tar, mush und glowing_stew gibt es im Port noch nicht.
 *
 * ABWEICHUNG: das Original nimmt OreDictionary-Namen. Im Port stehen dafuer die Form-Tags des
 * Materialsystems; PVC hat dort keine Barrenform, also steht der Gegenstand selbst.
 */
public class OutgasserRecipes extends SerializableRecipe {

    public static final List<OutgasserRecipe> recipes = new ArrayList<>();

    @Override
    public void registerDefaults() {

        /* Lithium wird zu Tritium */
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.BLOCK.getTag(Mats.MAT_LITHIUM)), null, new FluidStack(Fluids.TRITIUM, 10_000)));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_LITHIUM)), null, new FluidStack(Fluids.TRITIUM, 1_000)));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_LITHIUM)), null, new FluidStack(Fluids.TRITIUM, 1_000)));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.DUSTTINY.getTag(Mats.MAT_LITHIUM)), null, new FluidStack(Fluids.TRITIUM, 100)));

        /* Gold wird zu Gold-198 */
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_GOLD)), new ItemStack(NtmItems.INGOT_AU198.get()), null));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.NUGGET.getTag(Mats.MAT_GOLD)), new ItemStack(NtmItems.NUGGET_AU198.get()), null));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.DUST.getTag(Mats.MAT_GOLD)), new ItemStack(NtmItems.POWDER_AU198.get()), null));

        /* Thorium wird zu Thorium-Brennstoff */
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.INGOT.getTag(Mats.MAT_THORIUM)), new ItemStack(NtmItems.INGOT_THORIUM_FUEL.get()), null));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.NUGGET.getTag(Mats.MAT_THORIUM)), new ItemStack(NtmItems.NUGGET_THORIUM_FUEL.get()), null));
        recipes.add(new OutgasserRecipe(new TagStack(MaterialShapes.BILLET.getTag(Mats.MAT_THORIUM)), new ItemStack(NtmItems.BILLET_THORIUM_FUEL.get()), null));

        /* PVC wird zu C4 und Kolloid */
        recipes.add(new OutgasserRecipe(new ComparableStack(NtmItems.INGOT_PVC.get()), new ItemStack(NtmItems.INGOT_C4.get()), new FluidStack(Fluids.COLLOID, 250)));
    }

    public static @Nullable OutgasserRecipe getOutput(ItemStack input) {

        if(input.isEmpty()) return null;

        for(OutgasserRecipe recipe : recipes) {
            if(recipe.input.matchesRecipe(input, true)) return recipe;
        }

        return null;
    }

    @Override public String getFileName() { return "hbmIrradiation.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        AStack input = this.readAStack(obj.get("input").getAsJsonArray());
        ItemStack solidOutput = obj.has("solidOutput") ? this.readItemStack(obj.get("solidOutput").getAsJsonArray()) : null;
        FluidStack fluidOutput = obj.has("fluidOutput") ? this.readFluidStack(obj.get("fluidOutput").getAsJsonArray()) : null;

        recipes.add(new OutgasserRecipe(input, solidOutput, fluidOutput));
    }

    @Override
    public void writeRecipe(Object obj, JsonWriter writer) throws IOException {

        OutgasserRecipe recipe = (OutgasserRecipe) obj;

        writer.name("input");
        this.writeAStack(recipe.input, writer);

        if(recipe.solidOutput != null) {
            writer.name("solidOutput");
            this.writeItemStack(recipe.solidOutput, writer);
        }

        if(recipe.fluidOutput != null) {
            writer.name("fluidOutput");
            this.writeFluidStack(recipe.fluidOutput, writer);
        }
    }

    @Override
    public String getComment() {
        return "Recipes for the RBMK irradiation channel. There is no duration: how fast an item is processed "
                + "depends entirely on the neutron flux passing through the column.";
    }

    public static class OutgasserRecipe {

        public final AStack input;
        public final ItemStack solidOutput;
        public final FluidStack fluidOutput;

        public OutgasserRecipe(AStack input, ItemStack solidOutput, FluidStack fluidOutput) {
            this.input = input;
            this.solidOutput = solidOutput;
            this.fluidOutput = fluidOutput;
        }
    }
}
