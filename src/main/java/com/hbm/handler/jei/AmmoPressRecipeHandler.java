package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.inventory.recipes.AmmoPressRecipes.AmmoPressRecipe;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Die Rezeptansicht der Munitionspresse. Der Ausschnitt ist der des Gitters samt Ausgabefach --
 * die Rezeptliste der Oberflaeche bleibt draussen, die waere hier doppelt.
 */
public class AmmoPressRecipeHandler implements IRecipeCategory<AmmoPressRecipe> {

    public static final RecipeType<AmmoPressRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "ammo_press",
            AmmoPressRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;

    public AmmoPressRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_ammo_press.png"),
                107, 10, 69, 86
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_AMMO_PRESS.asItem());
    }

    @Override public RecipeType<AmmoPressRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machineAmmoPress"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AmmoPressRecipe recipe, IFocusGroup focuses) {

        /* Die Ausschnittsecke liegt bei 107/10, die Faecher des Gitters bei 116/18. */
        for(int i = 0; i < 9; i++) {
            if(recipe.input[i] == null) continue;
            builder.addInputSlot(9 + 18 * (i % 3), 8 + 18 * (i / 3)).setStandardSlotBackground().addItemStacks(recipe.input[i].extractForJEI());
        }

        builder.addOutputSlot(27, 62).setOutputSlotBackground().addItemStack(recipe.output.copy());
    }

    @Override
    public void draw(AmmoPressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }

    public static java.util.List<AmmoPressRecipe> getRecipes() {
        return AmmoPressRecipes.recipes;
    }
}
