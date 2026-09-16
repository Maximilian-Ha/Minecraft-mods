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
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.util.Tuple.Triplet;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Verkoker.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.CokerHandler.
 */
public class CokerRecipeHandler implements IRecipeCategory<CokerRecipeHandler.CokerRecipe> {

    public static final RecipeType<CokerRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "coker", CokerRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CokerRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_COKER.asItem());
    }

    @Override public RecipeType<CokerRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_coker"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<CokerRecipe> getRecipes() {
        List<CokerRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, Triplet<Integer, ItemStack, FluidStack>> e : CokerRecipes.recipes.entrySet()) {
            list.add(new CokerRecipe(e.getKey(), e.getValue().getX(), e.getValue().getY(), e.getValue().getZ()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CokerRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, recipe.amount));
        if(recipe.output != null) builder.addOutputSlot(110, 14).setStandardSlotBackground().addItemStack(recipe.output.copy());
        if(recipe.byproduct != null) builder.addOutputSlot(110, 32).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.byproduct));
    }

    public record CokerRecipe(FluidType input, int amount, ItemStack output, FluidStack byproduct) { }
}
