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
import com.hbm.blockentity.machine.oil.MachineHydrotreaterBlockEntity;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.util.Tuple.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Hydrotreater.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.HydrotreatingHandler.
 */
public class HydrotreatingRecipeHandler implements IRecipeCategory<HydrotreatingRecipeHandler.HydrotreatingRecipe> {

    public static final RecipeType<HydrotreatingRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "hydrotreating", HydrotreatingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public HydrotreatingRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_HYDROTREATER.asItem());
    }

    @Override public RecipeType<HydrotreatingRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machine_hydrotreater"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<HydrotreatingRecipe> getRecipes() {
        List<HydrotreatingRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> e : HydrotreatingRecipes.recipes.entrySet()) {
            list.add(new HydrotreatingRecipe(e.getKey(), e.getValue().getX(), e.getValue().getY(), e.getValue().getZ()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HydrotreatingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 14).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, MachineHydrotreaterBlockEntity.INPUT_PER_BATCH));
        builder.addInputSlot(38, 32).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.hydrogen));
        builder.addOutputSlot(110, 14).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.output));
        builder.addOutputSlot(110, 32).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.sourgas));
    }

    public record HydrotreatingRecipe(FluidType input, FluidStack hydrogen, FluidStack output, FluidStack sourgas) { }
}
