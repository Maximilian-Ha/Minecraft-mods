package com.hbm.handler.jei;

import com.hbm.main.NuclearTechMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.NEIUniversalHandler.
 *
 * Die schlichteste Ansicht des Mods: ein Gegenstand geht hinein, ein anderer kommt heraus, und
 * die Maschine daneben sagt, worin. Im Original bedient eine einzige Klasse alle diese Faelle;
 * JEI braucht je Ansicht einen eigenen Rezepttyp, deshalb steht hier die Mechanik und in den
 * Ableitungen nur noch Name, Symbol und Liste.
 *
 * ABWEICHUNG: das Original kann bis zu acht Ein- und Ausgaben in einem Kranz anordnen. Kein
 * einziger Nutzer des Reaktorzweigs braucht mehr als einen von beiden, also bleibt es hier bei
 * einem Paar -- dieselbe Anordnung wie in den uebrigen JEI-Ansichten des Ports.
 */
public abstract class ConversionRecipeHandler implements IRecipeCategory<ConversionRecipeHandler.Conversion> {

    /** Ein Eintrag: was hineingeht und was herauskommt. */
    public record Conversion(ItemStack input, ItemStack output) { }

    /** Ein Rezepttyp fuer diese Ansichten. Die Kennung unterscheidet sie, nicht die Klasse. */
    public static RecipeType<Conversion> type(String uid) {
        return RecipeType.create(NuclearTechMod.MODID, uid, Conversion.class);
    }

    private final IDrawable background;
    private final IDrawable icon;
    private final String titleKey;

    protected ConversionRecipeHandler(IGuiHelper guiHelper, ItemLike catalyst, String titleKey) {
        this.background = guiHelper.drawableBuilder(
                NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"),
                5, 11, 166, 65
        ).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.titleKey = titleKey;
    }

    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }
    @Override public Component getTitle() { return Component.translatable(this.titleKey); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Conversion recipe, IFocusGroup focuses) {
        builder.addInputSlot(47, 23).setStandardSlotBackground().addItemStack(recipe.input().copy());
        builder.addOutputSlot(101, 23).setOutputSlotBackground().addItemStack(recipe.output().copy());
    }

    @Override
    public void draw(Conversion recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }
}
