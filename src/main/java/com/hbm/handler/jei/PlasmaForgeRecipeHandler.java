package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Die Rezeptansicht der Plasmaschmiede. Aufbau wie bei der Montagemaschine -- zwoelf Eingaben,
 * ein Tank, ein Erzeugnis -- nur steht rechts unten zusaetzlich die Zuendtemperatur, weil ohne
 * sie das Rezept nicht anlaeuft.
 */
public class PlasmaForgeRecipeHandler implements IRecipeCategory<PlasmaForgeRecipe> {

    public static final RecipeType<PlasmaForgeRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "plasma_forge",
            PlasmaForgeRecipe.class
    );

    private static final String TEXTURE = "textures/gui/jei/gui_nei.png";

    private static final int[][] INPUT_COORDS = new int[][] {
            {12, 6}, {30, 6}, {48, 6}, {66, 6},
            {12, 24}, {30, 24}, {48, 24}, {66, 24},
            {12, 42}, {30, 42}, {48, 42}, {66, 42}
    };

    private final IDrawable background;
    private final IDrawable icon;

    public PlasmaForgeRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace(TEXTURE),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.FUSION_PLASMA_FORGE.asItem());
    }

    @Override public RecipeType<PlasmaForgeRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machinePlasmaForge"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PlasmaForgeRecipe recipe, IFocusGroup focuses) {

        if(recipe.inputItem != null) {
            int limit = Math.min(recipe.inputItem.length, INPUT_COORDS.length);
            for(int i = 0; i < limit; i++) {
                addInputSlot(builder, INPUT_COORDS[i][0], INPUT_COORDS[i][1], recipe.inputItem[i].extractForJEI());
            }
        }

        if(recipe.inputFluid != null && recipe.inputFluid.length > 0) {
            addFluidSlot(builder, 134, 6, recipe.inputFluid[0]);
        }

        if(recipe.outputItem != null && recipe.outputItem.length > 0) {
            builder.addOutputSlot(134, 24)
                    .setStandardSlotBackground()
                    .addItemStacks(Arrays.asList(recipe.outputItem[0].getAllPossibilities()));
        }
    }

    @Override
    public void draw(PlasmaForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

        var font = Minecraft.getInstance().font;
        String temp = BobMathUtil.getShortNumber(recipe.ignitionTemp) + "TU/t";

        guiGraphics.drawString(font, temp, 164 - font.width(temp), 57, 0xA000A0, false);
    }

    private static void addInputSlot(IRecipeLayoutBuilder builder, int x, int y, List<ItemStack> stacks) {
        var slot = builder.addInputSlot(x, y).setStandardSlotBackground();
        if(!stacks.isEmpty()) slot.addItemStacks(stacks);
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidStack stack) {
        builder.addInputSlot(x, y).setStandardSlotBackground().addItemStack(FluidIconItem.make(stack));
    }
}
