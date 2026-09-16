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
import com.hbm.blockentity.machine.oil.MachineCatalyticReformerBlockEntity;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.util.Tuple.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den katalytischen Reformer.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.ReformingHandler.
 */
public class ReformingRecipeHandler implements IRecipeCategory<ReformingRecipeHandler.ReformingRecipe> {

    public static final RecipeType<ReformingRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "reforming", ReformingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ReformingRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_CATALYTIC_REFORMER.asItem());
    }

    @Override public RecipeType<ReformingRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_catalytic_reformer"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<ReformingRecipe> getRecipes() {
        List<ReformingRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> e : ReformingRecipes.recipes.entrySet()) {
            list.add(new ReformingRecipe(e.getKey(), e.getValue().getX(), e.getValue().getY(), e.getValue().getZ()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ReformingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, MachineCatalyticReformerBlockEntity.INPUT_PER_BATCH));
        builder.addOutputSlot(110, 5).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.out1));
        builder.addOutputSlot(110, 23).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.out2));
        builder.addOutputSlot(110, 41).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.out3));
    }

    public record ReformingRecipe(FluidType input, FluidStack out1, FluidStack out2, FluidStack out3) { }
}
