package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SILEXRecipes.Output;
import com.hbm.inventory.recipes.SILEXRecipes.SILEXRecipe;
import com.hbm.items.machine.FelCrystalItem.Wellenlaenge;
import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * JEI-Kategorie fuer die SILEX. Im Original uebernahm das der NEI-Handler SILEXRecipeHandler.
 *
 * EIN EINGANG, BIS ZU SIEBEN AUSGAENGE, und unter jedem steht sein Anteil in Prozent -- die
 * Maschine wuerfelt nicht, die Zahl ist also keine Chance, sondern das, was auf lange Sicht
 * wirklich herauskommt. Obendrueber steht, welche Wellenlaenge der FEL mindestens liefern muss.
 *
 * ABWEICHUNG: das Original zeigt hoechstens sechs Ausgaenge und laesst deshalb bei den drei
 * Schrabidiumpellets das Xenon weg. Die Rezepte bleiben unveraendert; diese Ansicht kann
 * sieben zeigen und zeigt sie auch.
 */
public class SILEXRecipeHandler implements IRecipeCategory<SILEXRecipeHandler.SILEXJeiRecipe> {

    public static final RecipeType<SILEXJeiRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "silex",
            SILEXJeiRecipe.class
    );

    /** Wieviele Ausgaenge nebeneinander passen. */
    private static final int COLUMNS = 7;

    public static List<SILEXJeiRecipe> getRecipes() {

        List<SILEXJeiRecipe> list = new ArrayList<>();

        for(Entry<ComparableStack, SILEXRecipe> entry : SILEXRecipes.recipes.entrySet()) {

            ItemStack input = entry.getKey().toStack();
            input.setCount(1);

            list.add(new SILEXJeiRecipe(input, entry.getValue()));
        }

        return list;
    }

    private final IDrawable background;
    private final IDrawable icon;

    public SILEXRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_SILEX.asItem());
    }

    @Override public RecipeType<SILEXJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.category.hbmsntm.silex"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SILEXJeiRecipe recipe, IFocusGroup focuses) {

        builder.addInputSlot(8, 28).setStandardSlotBackground().addItemStack(recipe.input);

        for(int i = 0; i < recipe.outputs.size() && i < COLUMNS; i++) {
            builder.addOutputSlot(36 + i * 18, 28).setStandardSlotBackground()
                    .addItemStack(recipe.outputs.get(i).stack().copy());
        }
    }

    @Override
    public void draw(SILEXJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

        Font font = Minecraft.getInstance().font;

        Component wave = Component.translatable("jei.hbmsntm.silex.wavelength",
                Component.translatable(recipe.wavelength.name)).withStyle(recipe.wavelength.textfarbe);
        guiGraphics.drawString(font, wave, 8, 8, 0x404040, false);

        Component solution = Component.translatable("jei.hbmsntm.silex.solution",
                String.format(Locale.US, "%,d", recipe.fluidProduced), String.format(Locale.US, "%,d", recipe.fluidConsumed));
        guiGraphics.drawString(font, solution, 8, 18, 0x404040, false);

        /* Die Anteile stehen unter den Faechern, in Prozent des Gesamtgewichts. */
        for(int i = 0; i < recipe.outputs.size() && i < COLUMNS; i++) {
            String share = Math.round(recipe.outputs.get(i).weight() * 100F / recipe.totalWeight) + "%";
            guiGraphics.drawString(font, share, 45 + i * 18 - font.width(share) / 2, 48, 0x404040, false);
        }
    }

    public static class SILEXJeiRecipe {

        public final ItemStack input;
        public final List<Output> outputs;
        public final int totalWeight;
        public final int fluidProduced;
        public final int fluidConsumed;
        public final Wellenlaenge wavelength;

        public SILEXJeiRecipe(ItemStack input, SILEXRecipe recipe) {

            this.input = input;
            this.outputs = recipe.outputs;
            this.fluidProduced = recipe.fluidProduced;
            this.fluidConsumed = recipe.fluidConsumed;
            this.wavelength = recipe.laserStrength;

            int weight = 0;
            for(Output output : recipe.outputs) weight += output.weight();
            this.totalWeight = Math.max(weight, 1);
        }
    }
}
