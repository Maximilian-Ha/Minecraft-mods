package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.DepletedFuelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.FuelPoolRecipes.
 *
 * Was im Brennstoffbecken aus heissem Abfall wird, sobald er lange genug im Wasser lag. Jeder
 * Eintrag bildet denselben Gegenstand im heissen Zustand auf seinen abgekuehlten ab.
 *
 * NICHT UEBERNOMMEN:
 * - Die PWR-Eintraege (pwr_fuel_hot zu pwr_fuel_depleted). Der Druckwasserreaktor ist nicht
 *   portiert; die Eintraege kommen mit ihm.
 * - Die RBMK-Brennstaebe. Das Original fuehrt sie hier zwar auf, erreicht die Stelle aber nie:
 *   die Trommel prueft zuerst auf einen Brennstab und kuehlt ihn ueber seine Waermewerte ab.
 *   Die Eintraege sind dort nur fuer die Rezeptansicht da, und die ist noch nicht portiert.
 */
public class FuelPoolRecipes extends SerializableRecipe {

    public static final HashMap<ComparableStack, ItemStack> recipes = new HashMap<>();

    /** Was aus diesem Stapel im Becken wird, oder null. */
    public static ItemStack getOutput(ItemStack stack) {
        ItemStack out = recipes.get(new ComparableStack(stack));
        return out == null ? null : out.copy();
    }

    public static boolean isValidInput(ItemStack stack) {
        return recipes.containsKey(new ComparableStack(stack));
    }

    @Override
    public void registerDefaults() {
        for(DeferredItem<Item> waste : new DeferredItem[] {
                NtmItems.WASTE_NATURAL_URANIUM, NtmItems.WASTE_URANIUM, NtmItems.WASTE_THORIUM,
                NtmItems.WASTE_MOX, NtmItems.WASTE_PLUTONIUM, NtmItems.WASTE_U233,
                NtmItems.WASTE_U235, NtmItems.WASTE_SCHRABIDIUM, NtmItems.WASTE_ZFB_MOX,
                NtmItems.WASTE_PLATE_U233, NtmItems.WASTE_PLATE_U235, NtmItems.WASTE_PLATE_MOX,
                NtmItems.WASTE_PLATE_PU239, NtmItems.WASTE_PLATE_SA326,
                NtmItems.WASTE_PLATE_RA226BE, NtmItems.WASTE_PLATE_PU238BE }) {

            recipes.put(new ComparableStack(waste.get(), 1, DepletedFuelItem.HOT),
                    MetaHelper.newStack(waste.get(), 1, DepletedFuelItem.COOL));
        }
    }

    @Override
    public String getFileName() {
        return "hbmFuelpool.json";
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
        JsonObject obj = recipe.getAsJsonObject();
        ItemStack in = this.readItemStack(obj.get("input").getAsJsonArray());
        ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());
        recipes.put(new ComparableStack(in), out);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        @SuppressWarnings("unchecked")
        Map.Entry<ComparableStack, ItemStack> entry = (Map.Entry<ComparableStack, ItemStack>) recipe;

        writer.name("input");
        this.writeItemStack(entry.getKey().toStack(), writer);
        writer.name("output");
        this.writeItemStack(entry.getValue(), writer);
    }
}
