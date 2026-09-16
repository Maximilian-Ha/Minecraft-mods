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
import com.hbm.blockentity.machine.oil.MachineVacuumDistillBlockEntity;
import com.hbm.inventory.recipes.VacuumRefineryRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer die Vakuumdestille.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.VacuumRefineryHandler.
 */
public class VacuumRefineryRecipeHandler implements IRecipeCategory<VacuumRefineryRecipeHandler.VacuumRefineryRecipe> {

    public static final RecipeType<VacuumRefineryRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "vacuum_refinery", VacuumRefineryRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public VacuumRefineryRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_VACUUM_DISTILL.asItem());
    }

    @Override public RecipeType<VacuumRefineryRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_vacuum_distill"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<VacuumRefineryRecipe> getRecipes() {
        List<VacuumRefineryRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, VacuumRefineryRecipes.VacuumRefineryRecipe> e : VacuumRefineryRecipes.recipes.entrySet()) {
            list.add(new VacuumRefineryRecipe(e.getKey(), e.getValue().outputs));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VacuumRefineryRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, MachineVacuumDistillBlockEntity.INPUT_PER_BATCH));
        int[] y = { 5, 23, 41, 5 };
        int[] x = { 110, 110, 110, 128 };
        for(int i = 0; i < recipe.outputs.length; i++) {
            builder.addOutputSlot(x[i], y[i]).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.outputs[i]));
        }
    }

    public record VacuumRefineryRecipe(FluidType input, FluidStack[] outputs) { }
}
