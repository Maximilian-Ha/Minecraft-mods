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
import com.hbm.blockentity.machine.oil.MachineCatalyticCrackerBlockEntity;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.util.Tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI-Ansicht fuer den Krackturm.
 *
 * Portiert aus 1.7.10: com.hbm.handler.nei.CrackingHandler.
 */
public class CrackingRecipeHandler implements IRecipeCategory<CrackingRecipeHandler.CrackingRecipe> {

    public static final RecipeType<CrackingRecipe> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "cracking", CrackingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CrackingRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_CATALYTIC_CRACKER.asItem());
    }

    @Override public RecipeType<CrackingRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.hbmsntm.machine_catalytic_cracker"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    public static List<CrackingRecipe> getRecipes() {
        List<CrackingRecipe> list = new ArrayList<>();
        for(Map.Entry<FluidType, Pair<FluidStack, FluidStack>> e : CrackingRecipes.cracking.entrySet()) {
            list.add(new CrackingRecipe(e.getKey(), e.getValue().getKey(), e.getValue().getValue()));
        }
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrackingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 14).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.input, MachineCatalyticCrackerBlockEntity.OIL_PER_BATCH));
        builder.addInputSlot(38, 32).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(Fluids.STEAM, MachineCatalyticCrackerBlockEntity.STEAM_PER_BATCH));
        builder.addOutputSlot(110, 5).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.left));
        builder.addOutputSlot(110, 23).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.right));
        builder.addOutputSlot(110, 41).setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(Fluids.SPENTSTEAM, MachineCatalyticCrackerBlockEntity.SPENT_STEAM_PER_BATCH));
    }

    public record CrackingRecipe(FluidType input, FluidStack left, FluidStack right) { }
}
