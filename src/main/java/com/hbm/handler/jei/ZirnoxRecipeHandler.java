package com.hbm.handler.jei;

import com.hbm.blockentity.machine.ReactorZirnoxBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Portiert aus 1.7.10: com.hbm.handler.nei.ZirnoxRecipeHandler. Die elf ZIRNOX-Brennstaebe. */
public class ZirnoxRecipeHandler extends ConversionRecipeHandler {

    public static final RecipeType<Conversion> RECIPE_TYPE = type("zirnox");

    public ZirnoxRecipeHandler(IGuiHelper guiHelper) {
        super(guiHelper, NtmBlocks.REACTOR_ZIRNOX.asItem(), "container.zirnox");
    }

    @Override public RecipeType<Conversion> getRecipeType() { return RECIPE_TYPE; }

    public static List<Conversion> getRecipes() {
        ReactorZirnoxBlockEntity.initFuelMap();

        List<Conversion> recipes = new ArrayList<>();
        for(Map.Entry<ComparableStack, ItemStack> entry : ReactorZirnoxBlockEntity.fuelMap.entrySet()) {
            recipes.add(new Conversion(entry.getKey().toStack(), entry.getValue()));
        }
        return recipes;
    }
}
