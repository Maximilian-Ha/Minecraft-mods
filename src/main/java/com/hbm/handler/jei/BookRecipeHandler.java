package com.hbm.handler.jei;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.MagicRecipes;
import com.hbm.inventory.recipes.MagicRecipes.MagicRecipe;
import com.hbm.items.NtmItems;
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
 * JEI-Kategorie fuer die Rezepte des Buchs. Im Original uebernahm das der NEI-Handler
 * BookRecipeHandler ("Black Book").
 *
 * Als Hintergrund dient wie im Original die Oberflaeche des Buchs selbst. Der Ausschnitt
 * beginnt bei (5|11), und genau darauf beziehen sich auch die Platzkoordinaten des Originals:
 * die vier Eingaenge bei (25|6) im Abstand 36, das Ergebnis bei (119|24).
 */
public class BookRecipeHandler implements IRecipeCategory<BookRecipeHandler.BookJeiRecipe> {

    public static final RecipeType<BookJeiRecipe> RECIPE_TYPE = RecipeType.create(
            NuclearTechMod.MODID,
            "book",
            BookJeiRecipe.class
    );

    public static List<BookJeiRecipe> getRecipes() {

        List<BookJeiRecipe> list = new ArrayList<>();

        for(MagicRecipe recipe : MagicRecipes.getRecipes()) {

            List<List<ItemStack>> inputs = new ArrayList<>();
            for(AStack stack : recipe.in) inputs.add(stack.extractForJEI());

            list.add(new BookJeiRecipe(inputs, recipe.out.copy()));
        }

        return list;
    }

    private final IDrawable background;
    private final IDrawable icon;

    public BookRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_book.png"),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmItems.BOOK_OF_.get());
    }

    @Override public RecipeType<BookJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.category.hbmsntm.book"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BookJeiRecipe recipe, IFocusGroup focuses) {

        for(int i = 0; i < recipe.inputs.size() && i < 4; i++) {
            builder.addInputSlot(25 + (i % 2) * 36, 6 + (i / 2) * 36).addItemStacks(recipe.inputs.get(i));
        }

        builder.addOutputSlot(119, 24).addItemStack(recipe.output);
    }

    public static class BookJeiRecipe {

        public final List<List<ItemStack>> inputs;
        public final ItemStack output;

        public BookJeiRecipe(List<List<ItemStack>> inputs, ItemStack output) {
            this.inputs = inputs;
            this.output = output;
        }
    }
}
