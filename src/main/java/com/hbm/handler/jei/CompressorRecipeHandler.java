package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.CompressorRecipes;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JEI-Kategorie fuer den Verdichter. Im Original uebernahm das der NEI-Handler
 * CompressorRecipeHandler.
 *
 * Die Rezeptliste ist auf Paare (Fluid, Eingangsdruckstufe) geschluesselt, weil dasselbe
 * Fluid je nach anliegendem Druck ein anderes Ergebnis liefert -- Erdoel wird bei Stufe 0
 * zu verdichtetem Erdoel und bei Stufe 1 zu Fluessiggas. Die Stufe gehoert deshalb mit in
 * die Anzeige, sonst stuenden im Rezeptbrowser scheinbar widerspruechliche Eintraege.
 */
public class CompressorRecipeHandler implements IRecipeCategory<CompressorRecipeHandler.CompressorJeiRecipe> {

    public static final RecipeType<CompressorJeiRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "compressor",
            CompressorJeiRecipe.class
    );

    public static List<CompressorJeiRecipe> getRecipes() {
        List<CompressorJeiRecipe> list = new ArrayList<>();

        for(Map.Entry<Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe> entry : CompressorRecipes.recipes.entrySet()) {
            FluidType inputType = entry.getKey().getKey();
            int inputPressure = entry.getKey().getValue();
            CompressorRecipes.CompressorRecipe recipe = entry.getValue();

            if(inputType == null || recipe == null || recipe.output == null) continue;

            list.add(new CompressorJeiRecipe(inputType, inputPressure, recipe.inputAmount, recipe.output, recipe.duration));
        }

        return list;
    }

    private final IDrawable background;
    private final IDrawable icon;

    public CompressorRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_COMPRESSOR.asItem());
    }

    @Override
    public RecipeType<CompressorJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.hbmsntm.compressor");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompressorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(38, 23)
                .setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(new FluidStack(recipe.inputType, recipe.inputAmount, recipe.inputPressure)));

        builder.addOutputSlot(110, 23)
                .setStandardSlotBackground()
                .addItemStack(FluidIconItem.make(recipe.output));
    }

    @Override
    public void draw(CompressorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        String stage = String.format(Locale.US, "%d bar -> %d bar", recipe.inputPressure, recipe.output.pressure);
        String time = String.format(Locale.US, "%,d ticks", recipe.duration);
        guiGraphics.drawString(font, stage, 74 - font.width(stage) / 2, 43, 0x404040, false);
        guiGraphics.drawString(font, time, 74 - font.width(time) / 2, 55, 0x404040, false);
    }

    public static class CompressorJeiRecipe {
        public final FluidType inputType;
        public final int inputPressure;
        public final int inputAmount;
        public final FluidStack output;
        public final int duration;

        public CompressorJeiRecipe(FluidType inputType, int inputPressure, int inputAmount, FluidStack output, int duration) {
            this.inputType = inputType;
            this.inputPressure = inputPressure;
            this.inputAmount = inputAmount;
            this.output = output;
            this.duration = duration;
        }
    }
}
