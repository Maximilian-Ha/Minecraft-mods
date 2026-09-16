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
import com.hbm.inventory.recipes.SolidificationRecipes;
import com.hbm.util.Tuple.Pair;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Verfestiger.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.SolidificationHandler.
 */
public class SolidificationRecipeHandler implements IRecipeCategory<SolidificationRecipeHandler.SolidificationRecipe> {

    public static final RecipeType<SolidificationRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "solidification", SolidificationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public SolidificationRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_SOLIDIFIER.asItem());
    }

    @Override public RecipeType<SolidificationRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_solidifier"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<SolidificationRecipe> getRecipes() {
        List<SolidificationRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, Pair<Integer, ItemStack>> e : SolidificationRecipes.recipes.entrySet()) {
            list.add(new SolidificationRecipe(e.getKey(), e.getValue().getKey(), e.getValue().getValue()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SolidificationRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, recipe.amount));
        builder.addOutputSlot(110, 23).setStandardSlotBackground().addItemStack(recipe.output.copy());
    }

    public record SolidificationRecipe(FluidType input, int amount, ItemStack output) { }
}
