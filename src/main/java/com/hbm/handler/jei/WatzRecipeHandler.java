package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.WatzPelletItem.EnumWatzType;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;

import java.util.ArrayList;
import java.util.List;

/** Portiert aus 1.7.10: com.hbm.handler.nei.WatzRecipeHandler. Die zwoelf Watz-Pellets. */
public class WatzRecipeHandler extends ConversionRecipeHandler {

    public static final RecipeType<Conversion> RECIPE_TYPE = type("watz");

    public WatzRecipeHandler(IGuiHelper guiHelper) {
        super(guiHelper, NtmBlocks.WATZ.asItem(), "container.watz");
    }

    @Override public RecipeType<Conversion> getRecipeType() { return RECIPE_TYPE; }

    public static List<Conversion> getRecipes() {
        List<Conversion> recipes = new ArrayList<>();
        for(EnumWatzType type : EnumWatzType.values()) {
            recipes.add(new Conversion(
                    MetaHelper.newStack(NtmItems.WATZ_PELLET.get(), 1, type.ordinal()),
                    MetaHelper.newStack(NtmItems.WATZ_PELLET_DEPLETED.get(), 1, type.ordinal())));
        }
        return recipes;
    }
}
