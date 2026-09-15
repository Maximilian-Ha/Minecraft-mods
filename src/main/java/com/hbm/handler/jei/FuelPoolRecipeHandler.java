package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.FuelPoolHandler.
 *
 * Das Abklingbecken. Was heiss hineingeht, kommt kalt wieder heraus.
 *
 * ABWEICHUNG: das Original blendet die Ansicht fuer heisse RBMK-Staebe aus, damit sie nicht in
 * beiden Richtungen erscheint. JEI hat dafuer keinen Haken, der ohne eigenen Zutatentyp
 * auskaeme; die Ansicht zeigt deshalb schlicht alle Paare.
 */
public class FuelPoolRecipeHandler extends ConversionRecipeHandler {

    public static final RecipeType<Conversion> RECIPE_TYPE = type("fuel_pool");

    public FuelPoolRecipeHandler(IGuiHelper guiHelper) {
        super(guiHelper, NtmBlocks.MACHINE_WASTE_DRUM.asItem(), "container.wasteDrum");
    }

    @Override public RecipeType<Conversion> getRecipeType() { return RECIPE_TYPE; }

    public static List<Conversion> getRecipes() {
        List<Conversion> recipes = new ArrayList<>();
        for(Map.Entry<ComparableStack, ItemStack> entry : FuelPoolRecipes.recipes.entrySet()) {
            recipes.add(new Conversion(entry.getKey().toStack(), entry.getValue()));
        }
        return recipes;
    }
}
