package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.items.machine.ScrapsItem;
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
 * Die Rezeptansicht des grossen Lichtbogenofens.
 *
 * Gezeigt werden die ausdruecklich eingetragenen Rezepte sowie, fuer die fluessige Betriebsart,
 * jedes schmelzbare Material als Schrott. Was der Ofen darueber hinaus aus Form-und-Material-Tags
 * und aus Schmelzofenrezepten ableitet, steht nicht in der Liste -- es waere so umfangreich wie
 * der Gegenstandsbestand selbst.
 */
public class ArcFurnaceRecipeHandler implements IRecipeCategory<ArcFurnaceRecipeHandler.Display> {

    public static final RecipeType<Display> RECIPE_TYPE = RecipeType.create(NuclearTechMod.MODID, "arc_furnace", Display.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ArcFurnaceRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(NuclearTechMod.withDefaultNamespace("textures/gui/jei/gui_nei.png"), 5, 11, 166, 65).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableItemLike(NtmBlocks.MACHINE_ARC_FURNACE.asItem());
    }

    @Override public RecipeType<Display> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("container.machineArcFurnaceLarge"); }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {

        var input = builder.addInputSlot(38, 23).setStandardSlotBackground();
        for(ItemStack stack : recipe.inputs) input.addItemStack(stack);

        builder.addOutputSlot(128, 23).setStandardSlotBackground().addItemStack(recipe.output);
    }

    public static List<Display> getRecipes() {

        List<Display> list = new ArrayList<>();

        for(var entry : ArcFurnaceRecipes.recipeList) {

            List<ItemStack> inputs = entry.getKey().extractForJEI();
            if(inputs.isEmpty()) continue;

            var recipe = entry.getValue();

            if(recipe.solidOutput != null) {
                list.add(new Display(inputs, recipe.solidOutput.copy()));
            }

            if(recipe.fluidOutput != null) {
                for(MaterialStack mat : recipe.fluidOutput) {
                    list.add(new Display(inputs, ScrapsItem.create(mat, true)));
                }
            }
        }

        // die fluessige Betriebsart: Schrott bleibt Schrott, nur geschmolzen
        for(var mat : Mats.orderedList) {
            if(mat.smeltable != SmeltingBehavior.SMELTABLE) continue;

            MaterialStack ingot = new MaterialStack(mat, MaterialShapes.INGOT.q(1));
            list.add(new Display(List.of(ScrapsItem.create(ingot)), ScrapsItem.create(ingot, true)));
        }

        return list;
    }

    /** Ein Eintrag der Ansicht: was hineingeht (mehrere Moeglichkeiten) und was herauskommt. */
    public record Display(List<ItemStack> inputs, ItemStack output) { }
}
