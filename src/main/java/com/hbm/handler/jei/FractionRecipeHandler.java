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
import com.hbm.blockentity.machine.oil.MachineFractionTowerBlockEntity;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.util.Tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Fraktionierturm.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.FractionHandler.
 */
public class FractionRecipeHandler implements IRecipeCategory<FractionRecipeHandler.FractionRecipe> {

    public static final RecipeType<FractionRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "fraction", FractionRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FractionRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_FRACTION_TOWER.asItem());
    }

    @Override public RecipeType<FractionRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_fraction_tower"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<FractionRecipe> getRecipes() {
        List<FractionRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, Pair<FluidStack, FluidStack>> e : FractionRecipes.fractions.entrySet()) {
            list.add(new FractionRecipe(e.getKey(), e.getValue().getKey(), e.getValue().getValue()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FractionRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, MachineFractionTowerBlockEntity.BATCH));
        builder.addOutputSlot(110, 14).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.left));
        builder.addOutputSlot(110, 32).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.right));
    }

    public record FractionRecipe(FluidType input, FluidStack left, FluidStack right) { }
}
