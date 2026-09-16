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
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Verfluessiger.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.LiquefactionHandler.
 */
public class LiquefactionRecipeHandler implements IRecipeCategory<LiquefactionRecipeHandler.LiquefactionRecipe> {

    public static final RecipeType<LiquefactionRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "liquefaction", LiquefactionRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public LiquefactionRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_LIQUEFACTOR.asItem());
    }

    @Override public RecipeType<LiquefactionRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_liquefactor"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<LiquefactionRecipe> getRecipes() {
        List<LiquefactionRecipe> list = new ArrayList<>();
        for(Map.Entry<AStack, FluidStack> e : LiquefactionRecipes.recipes.entrySet()) {
            List<ItemStack> in = e.getKey().extractForJEI();
            if(in.isEmpty()) continue;
            list.add(new LiquefactionRecipe(in, e.getValue()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiquefactionRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23).setStandardSlotBackground().addItemStacks(recipe.input);
        builder.addOutputSlot(110, 23).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.output));
    }

    public record LiquefactionRecipe(List<ItemStack> input, FluidStack output) { }
}
