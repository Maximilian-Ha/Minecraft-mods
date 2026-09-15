package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.CrucibleRecipe;
import com.hbm.items.machine.ScrapsItem;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

/**
 * Die Rezeptansicht des Tiegels: was er woraus legiert.
 *
 * Ein- und Ausgang sind Material, kein Gegenstand -- gezeigt werden sie deshalb als fluessiger
 * Schrott, genau wie im Original.
 */
public class CrucibleAlloyingRecipeHandler implements IRecipeCategory<CrucibleRecipe> {

    public static final RecipeType<CrucibleRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "crucible_alloying", CrucibleRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CrucibleAlloyingRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_CRUCIBLE.asItem());
    }

    @Override public RecipeType<CrucibleRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machineCrucible"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipe recipe, IFocusGroup focuses) {

        for(int i = 0; i < recipe.input.length && i < 4; i++) {
            MaterialStack mat = recipe.input[i];
            builder.addInputSlot(11 + i * 18, 23).setStandardSlotBackground().addItemStack(ScrapsItem.create(mat, true));
        }

        for(int i = 0; i < recipe.output.length && i < 2; i++) {
            MaterialStack mat = recipe.output[i];
            builder.addOutputSlot(119 + i * 18, 23).setStandardSlotBackground().addItemStack(ScrapsItem.create(mat, true));
        }
    }
}
