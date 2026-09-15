package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.PWRFuelItem.EnumPWRFuel;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.PWRRecipeHandler.
 *
 * Die fuenfzehn PWR-Sorten, frisch und heiss. Was aus dem heissen wird, sagt das Brennstoffbecken
 * in seiner eigenen Ansicht.
 */
public class PWRRecipeHandler extends ConversionRecipeHandler {

    public static final RecipeType<Conversion> RECIPE_TYPE = type("pwr");

    public PWRRecipeHandler(IGuiHelper guiHelper) {
        super(guiHelper, NtmBlocks.PWR_CONTROLLER.asItem(), "container.pwrController");
    }

    @Override public RecipeType<Conversion> getRecipeType() { return RECIPE_TYPE; }

    public static List<Conversion> getRecipes() {
        List<Conversion> recipes = new ArrayList<>();
        for(EnumPWRFuel fuel : EnumPWRFuel.values()) {
            recipes.add(new Conversion(
                    MetaHelper.newStack(NtmItems.PWR_FUEL.get(), 1, fuel.ordinal()),
                    MetaHelper.newStack(NtmItems.PWR_FUEL_HOT.get(), 1, fuel.ordinal())));
        }
        return recipes;
    }
}
