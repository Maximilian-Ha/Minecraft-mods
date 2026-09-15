package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.inventory.recipes.CyclotronRecipes.Pair;
import com.hbm.inventory.recipes.CyclotronRecipes.Result;
import com.hbm.main.NuclearTechMod;
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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * JEI-Kategorie fuer das Zyklotron. Im Original uebernahm das der NEI-Handler
 * CyclotronRecipeHandler.
 *
 * ZWEI EINGAENGE, EIN AUSGANG, und die Reihenfolge ist die der Maschine: links das Geschoss,
 * daneben das Ziel, rechts das Ergebnis. Darunter steht, wieviel Antimaterie dabei anfaellt --
 * ohne diese Zahl saehen die vier Wege gleich teuer aus, und das sind sie nicht.
 */
public class CyclotronRecipeHandler implements IRecipeCategory<CyclotronRecipeHandler.CyclotronJeiRecipe> {

    public static final RecipeType<CyclotronJeiRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "cyclotron",
            CyclotronJeiRecipe.class
    );

    public static List<CyclotronJeiRecipe> getRecipes() {

        List<CyclotronJeiRecipe> list = new ArrayList<>();

        for(Entry<Pair, Result> entry : CyclotronRecipes.recipes.entrySet()) {

            ItemStack particle = entry.getKey().particle().toStack();
            particle.setCount(1);

            for(ItemStack ingredient : entry.getKey().ingredient().extractForJEI()) {
                list.add(new CyclotronJeiRecipe(particle.copy(), ingredient.copy(),
                        entry.getValue().output().copy(), entry.getValue().antimatter()));
            }
        }

        return list;
    }

    private final IDrawable background;
    private final IDrawable icon;

    public CyclotronRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_CYCLOTRON.asItem());
    }

    @Override public RecipeType<CyclotronJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.category.hbmsntm.cyclotron"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CyclotronJeiRecipe recipe, IFocusGroup focuses) {

        builder.addInputSlot(20, 23).setStandardSlotBackground().addItemStack(recipe.particle);
        builder.addInputSlot(56, 23).setStandardSlotBackground().addItemStack(recipe.ingredient);
        builder.addOutputSlot(110, 23).setStandardSlotBackground().addItemStack(recipe.output);
    }

    @Override
    public void draw(CyclotronJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

        var font = Minecraft.getInstance().font;

        Component amat = Component.translatable("jei.hbmsntm.cyclotron.antimatter", String.format(Locale.US, "%,d", recipe.antimatter));
        guiGraphics.drawString(font, amat, 74 - font.width(amat) / 2, 47, 0x404040, false);
    }

    public static class CyclotronJeiRecipe {

        public final ItemStack particle;
        public final ItemStack ingredient;
        public final ItemStack output;
        public final int antimatter;

        public CyclotronJeiRecipe(ItemStack particle, ItemStack ingredient, ItemStack output, int antimatter) {
            this.particle = particle;
            this.ingredient = ingredient;
            this.output = output;
            this.antimatter = antimatter;
        }
    }
}
