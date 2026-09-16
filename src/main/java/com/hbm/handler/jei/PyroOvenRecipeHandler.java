package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import com.hbm.inventory.recipes.PyroOvenRecipes;
import net.minecraft.world.item.ItemStack;
import java.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Pyroofen.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.PyroHandler.
 */
public class PyroOvenRecipeHandler implements IRecipeCategory<PyroOvenRecipeHandler.PyroRecipe> {

    public static final RecipeType<PyroRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "pyrolysis", PyroRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PyroOvenRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_PYRO_OVEN.asItem());
    }

    @Override public RecipeType<PyroRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_pyro_oven"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<PyroRecipe> getRecipes() {
        List<PyroRecipe> list = new ArrayList<>();
        for(PyroOvenRecipes.PyroOvenRecipe r : PyroOvenRecipes.recipes) {
            List<ItemStack> in = r.inputItem == null ? Collections.emptyList() : r.inputItem.extractForJEI();
            if(r.inputItem != null && in.isEmpty()) continue;
            list.add(new PyroRecipe(in, r.inputFluid, r.outputItem, r.outputFluid));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PyroRecipe recipe, IFocusGroup focuses) {
        if(!recipe.inputItems.isEmpty()) builder.addInputSlot(38, 14).setStandardSlotBackground().addItemStacks(recipe.inputItems);
        if(recipe.inputFluid != null) builder.addInputSlot(38, 32).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.inputFluid));
        if(recipe.outputItem != null) builder.addOutputSlot(110, 14).setStandardSlotBackground().addItemStack(recipe.outputItem.copy());
        if(recipe.outputFluid != null) builder.addOutputSlot(110, 32).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.outputFluid));
    }

    public record PyroRecipe(List<ItemStack> inputItems, FluidStack inputFluid, ItemStack outputItem, FluidStack outputFluid) { }
}
