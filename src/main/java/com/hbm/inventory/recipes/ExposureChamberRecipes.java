package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.ExposureChamberRecipes.
 *
 * Vier Rezepte, und sie sind das Ende der ganzen Kette: Higgs auf Uran ergibt Schraranium, Higgs
 * auf Uran-238 ergibt Schrabidium, Dunkle Materie auf Plutonium ergibt Euphemium, Sparkticle auf
 * Schrabidat ergibt Dineutronium.
 *
 * ES GIBT KEINEN ANDEREN WEG ZU DIESEN VIER STOFFEN. Der Beschleuniger liefert die Teilchen, die
 * Kammer macht daraus die Barren -- alles davor ist Vorbereitung.
 *
 * ABWEICHUNG: das Original nennt die vier Ausgangsbarren ueber das Erzwoerterbuch, das es auf
 * 1.21 nicht gibt; hier stehen die Barren des Mods selbst.
 *
 * ABWEICHUNG: im teuren Modus setzt das Original fuer Dineutronium ENTARTETE MATERIE statt
 * Schrabidat ein. Die gehoert zur Gegenstandsfamilie "item_expensive" des Weltraumbaus, die der
 * Port nicht hat -- hier steht der Schrabidatbarren in beiden Faellen.
 */
public class ExposureChamberRecipes extends SerializableRecipe {

    public static final List<ExposureChamberRecipe> recipes = new ArrayList<>();

    @Override
    public void registerDefaults() {

        recipes.add(new ExposureChamberRecipe(
                new ComparableStack(NtmItems.PARTICLE_HIGGS.get()),
                new ComparableStack(NtmItems.INGOT_URANIUM.get()),
                new ItemStack(NtmItems.INGOT_SCHRARANIUM.get())));

        recipes.add(new ExposureChamberRecipe(
                new ComparableStack(NtmItems.PARTICLE_HIGGS.get()),
                new ComparableStack(NtmItems.INGOT_U238.get()),
                new ItemStack(NtmItems.INGOT_SCHRABIDIUM.get())));

        recipes.add(new ExposureChamberRecipe(
                new ComparableStack(NtmItems.PARTICLE_DARK.get()),
                new ComparableStack(NtmItems.INGOT_PLUTONIUM.get()),
                new ItemStack(NtmItems.INGOT_EUPHEMIUM.get())));

        recipes.add(new ExposureChamberRecipe(
                new ComparableStack(NtmItems.PARTICLE_SPARKTICLE.get()),
                new ComparableStack(NtmItems.INGOT_SCHRABIDATE.get()),
                new ItemStack(NtmItems.INGOT_DINEUTRONIUM.get())));
    }

    public static ExposureChamberRecipe getRecipe(ItemStack particle, ItemStack input) {
        for(ExposureChamberRecipe recipe : recipes) {
            if(recipe.particle.matchesRecipe(particle, true) && recipe.ingredient.matchesRecipe(input, true)) return recipe;
        }
        return null;
    }

    public static class ExposureChamberRecipe {

        public AStack particle;
        public AStack ingredient;
        public ItemStack output;

        public ExposureChamberRecipe(AStack particle, AStack ingredient, ItemStack output) {
            this.particle = particle;
            this.ingredient = ingredient;
            this.output = output;
        }
    }

    @Override
    public String getFileName() {
        return "hbmExposureChamber.json";
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

        AStack particle = readAStack(obj.get("particle").getAsJsonArray());
        AStack ingredient = readAStack(obj.get("ingredient").getAsJsonArray());
        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());

        recipes.add(new ExposureChamberRecipe(particle, ingredient, output));
    }

    @Override
    public void writeRecipe(Object o, JsonWriter writer) throws IOException {

        ExposureChamberRecipe recipe = (ExposureChamberRecipe) o;

        writer.name("particle");
        writeAStack(recipe.particle, writer);
        writer.name("ingredient");
        writeAStack(recipe.ingredient, writer);
        writer.name("output");
        writeItemStack(recipe.output, writer);
    }
}
