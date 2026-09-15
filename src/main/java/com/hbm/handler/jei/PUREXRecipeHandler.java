package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.recipes.PUREXRecipe;
import com.hbm.items.machine.FluidIconItem;
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

import java.util.Arrays;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.PUREXRecipeHandler.
 *
 * Die Wiederaufbereitung. Drei Eingabefaecher, drei Eingabetanks, sechs Ausgabefaecher und ein
 * Ausgabetank -- dieselben Koordinaten wie in der Oberflaeche der Maschine.
 *
 * ABWEICHUNG: das Original zeichnet die Tanks als Tanks. Hier stehen an ihrer Stelle die
 * Fluidsymbole in gewoehnlichen Faechern, wie in jeder anderen JEI-Ansicht dieses Ports auch.
 */
public class PUREXRecipeHandler implements IRecipeCategory<PUREXRecipe> {

    public static final RecipeType<PUREXRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "purex",
            PUREXRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;

    public PUREXRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_purex.png"),
                0, 0, 176, 120
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_PUREX.asItem());
    }

    @Override public RecipeType<PUREXRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machinePUREX"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PUREXRecipe recipe, IFocusGroup focuses) {

        if(recipe.inputItem != null) {
            for(int i = 0; i < Math.min(recipe.inputItem.length, 3); i++) {
                builder.addInputSlot(8 + i * 18, 90).setStandardSlotBackground().addItemStacks(recipe.inputItem[i].extractForJEI());
            }
        }

        if(recipe.outputItem != null) {
            for(int i = 0; i < Math.min(recipe.outputItem.length, 6); i++) {
                builder.addOutputSlot(80 + (i % 2) * 18, 36 + (i / 2) * 18).setOutputSlotBackground()
                        .addItemStacks(Arrays.asList(recipe.outputItem[i].getAllPossibilities()));
            }
        }

        if(recipe.inputFluid != null) {
            for(int i = 0; i < Math.min(recipe.inputFluid.length, 3); i++) {
                addFluidSlot(builder, 8 + i * 18, 18, recipe.inputFluid[i], true);
            }
        }

        if(recipe.outputFluid != null && recipe.outputFluid.length > 0) {
            addFluidSlot(builder, 116, 36, recipe.outputFluid[0], false);
        }
    }

    @Override
    public void draw(PUREXRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidStack stack, boolean input) {
        var slot = input ? builder.addInputSlot(x, y) : builder.addOutputSlot(x, y);
        slot.setStandardSlotBackground().addItemStack(FluidIconItem.make(stack));
    }
}
