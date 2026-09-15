package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.RadiolysisRecipes;
import com.hbm.items.machine.FluidIconItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.Tuple.Pair;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.RadiolysisRecipeHandler.
 *
 * Ein Fluid geht hinein, zwei kommen heraus. Die Liste ist dieselbe, aus der auch die Kammer
 * selbst rechnet -- Wasser plus die zwoelf Krackrezepte.
 *
 * ABWEICHUNG: das Original zeichnet die drei Tanks als Tanks. Hier stehen an ihrer Stelle die
 * Fluidsymbole in gewoehnlichen Faechern, wie in jeder anderen JEI-Ansicht dieses Ports auch.
 */
public class RadiolysisRecipeHandler implements IRecipeCategory<RadiolysisRecipeHandler.RadiolysisRecipe> {

    /** Ein Eintrag der Radiolyseliste als eigener Typ -- JEI braucht einen Rezepttyp je Ansicht. */
    public record RadiolysisRecipe(FluidStack input, FluidStack output1, FluidStack output2) { }

    public static final RecipeType<RadiolysisRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "radiolysis",
            RadiolysisRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;

    public RadiolysisRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/gui_radiolysis.png"),
                52, 10, 60, 60
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_RADIOLYSIS.asItem());
    }

    @Override public RecipeType<RadiolysisRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.radiolysis"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RadiolysisRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(6, 21).setStandardSlotBackground().addItemStack(FluidIconItem.make(recipe.input()));
        builder.addOutputSlot(38, 3).setOutputSlotBackground().addItemStack(FluidIconItem.make(recipe.output1()));
        builder.addOutputSlot(38, 39).setOutputSlotBackground().addItemStack(FluidIconItem.make(recipe.output2()));
    }

    @Override
    public void draw(RadiolysisRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }

    /** Die Liste der Kammer, eins zu eins in Rezepte uebersetzt. Ein Kubikmeter geht hinein. */
    public static List<RadiolysisRecipe> getRecipes() {

        List<RadiolysisRecipe> recipes = new ArrayList<>();

        for(Map.Entry<FluidType, Pair<FluidStack, FluidStack>> entry : RadiolysisRecipes.getRecipes().entrySet()) {
            recipes.add(new RadiolysisRecipe(
                    new FluidStack(entry.getKey(), 1_000),
                    entry.getValue().getKey(),
                    entry.getValue().getValue()));
        }

        return recipes;
    }
}
