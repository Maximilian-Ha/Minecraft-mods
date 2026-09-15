package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Rezeptansicht des Giessens: welches Material in welcher Form zu welchem Gegenstand wird.
 * Ein Eintrag zeigt den fluessigen Schrott, die Form und den Block, in den sie gehoert.
 */
public class CrucibleCastingRecipeHandler implements IRecipeCategory<CrucibleCastingRecipeHandler.Display> {

    public static final RecipeType<Display> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "crucible_casting", Display.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CrucibleCastingRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.FOUNDRY_MOLD.asItem());
    }

    @Override public RecipeType<Display> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.crucibleCasting"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        builder.addInputSlot(20, 23).setStandardSlotBackground().addItemStack(recipe.material);
        builder.addInputSlot(47, 23).setStandardSlotBackground().addItemStack(recipe.mold);
        builder.addInputSlot(74, 23).setStandardSlotBackground().addItemStack(recipe.vessel);
        builder.addOutputSlot(128, 23).setStandardSlotBackground().addItemStack(recipe.output);
    }

    public static List<Display> getRecipes() {

        List<Display> list = new ArrayList<>();

        for(ItemStack[] entry : CrucibleRecipes.getMoldRecipes()) {
            if(entry.length < 4) continue;
            list.add(new Display(entry[0], entry[1], entry[2], entry[3]));
        }

        return list;
    }

    public record Display(ItemStack material, ItemStack mold, ItemStack vessel, ItemStack output) { }
}
