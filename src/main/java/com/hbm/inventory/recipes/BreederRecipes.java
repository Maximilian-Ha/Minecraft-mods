package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.BreedingRodItem.BreedingRodType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.BreederRecipes.
 *
 * Was der Brutreaktor aus einem Brutstab macht. Zehn Umwandlungen, jede fuer alle drei
 * Stabgroessen: der einfache Stab braucht den Grundwert an Fluss, der doppelte das Doppelte, der
 * vierfache das Dreifache -- so im Original, und ja, drei und nicht vier.
 *
 * NICHT UEBERNOMMEN: das geaetzte Meteoritenschwert, das zum gezuechteten wird. Beide
 * Gegenstaende gibt es im Port nicht.
 */
public class BreederRecipes extends SerializableRecipe {

    public static final Map<ComparableStack, BreederRecipe> recipes = new HashMap<>();

    @Override
    public void registerDefaults() {
        setRecipe(BreedingRodType.LITHIUM, BreedingRodType.TRITIUM, 200);
        setRecipe(BreedingRodType.CO, BreedingRodType.CO60, 100);
        setRecipe(BreedingRodType.RA226, BreedingRodType.AC227, 300);
        setRecipe(BreedingRodType.TH232, BreedingRodType.THF, 500);
        setRecipe(BreedingRodType.U235, BreedingRodType.NP237, 300);
        setRecipe(BreedingRodType.NP237, BreedingRodType.PU238, 200);
        setRecipe(BreedingRodType.PU238, BreedingRodType.PU239, 1000);
        setRecipe(BreedingRodType.U238, BreedingRodType.RGP, 300);
        setRecipe(BreedingRodType.URANIUM, BreedingRodType.RGP, 200);
        setRecipe(BreedingRodType.RGP, BreedingRodType.WASTE, 200);
    }

    /** Legt die Umwandlung fuer den einfachen, den doppelten und den vierfachen Stab an. */
    public static void setRecipe(BreedingRodType input, BreedingRodType output, int flux) {
        recipes.put(new ComparableStack(NtmItems.ROD.get(), 1, input.ordinal()),
                new BreederRecipe(MetaHelper.newStack(NtmItems.ROD.get(), 1, output.ordinal()), flux));
        recipes.put(new ComparableStack(NtmItems.ROD_DUAL.get(), 1, input.ordinal()),
                new BreederRecipe(MetaHelper.newStack(NtmItems.ROD_DUAL.get(), 1, output.ordinal()), flux * 2));
        recipes.put(new ComparableStack(NtmItems.ROD_QUAD.get(), 1, input.ordinal()),
                new BreederRecipe(MetaHelper.newStack(NtmItems.ROD_QUAD.get(), 1, output.ordinal()), flux * 3));
    }

    public static @Nullable BreederRecipe getOutput(ItemStack stack) {
        if(stack.isEmpty()) return null;
        return recipes.get(new ComparableStack(stack).makeSingular());
    }

    public static class BreederRecipe {

        public final ItemStack output;
        /** So viel Fluss je Tick muss anliegen, damit ueberhaupt etwas geschieht. */
        public final int flux;

        public BreederRecipe(ItemStack output, int flux) {
            this.output = output;
            this.flux = flux;
        }
    }

    @Override public String getFileName() { return "hbmBreeder.json"; }
    @Override public Object getRecipeObject() { return recipes; }
    @Override public void deleteRecipes() { recipes.clear(); }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;
        AStack in = this.readAStack(obj.get("input").getAsJsonArray());
        int flux = obj.get("flux").getAsInt();
        ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());
        if(in instanceof ComparableStack comp) recipes.put(comp, new BreederRecipe(out, flux));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        Map.Entry<ComparableStack, BreederRecipe> rec = (Map.Entry<ComparableStack, BreederRecipe>) recipe;

        writer.name("input"); this.writeAStack(rec.getKey(), writer);
        writer.name("flux").value(rec.getValue().flux);
        writer.name("output"); this.writeItemStack(rec.getValue().output, writer);
    }
}
