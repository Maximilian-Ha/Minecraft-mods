package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.machine.RBMKPelletItem;
import com.hbm.items.machine.RBMKRodItem;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.RBMKRodDisassemblyHandler.
 *
 * Ein abgekuehlter, angebrannter RBMK-Brennstab zerfaellt im Werkbankraster in acht Pellets --
 * welche Sorte, entscheidet sein Abbrand. Das Rezept selbst ist ein CustomRecipe und taucht
 * darum nirgends von allein auf.
 *
 * ABWEICHUNG: das Original blendet heisse Staebe aus der Ansicht aus. Dafuer braeuchte JEI einen
 * eigenen Zutatentyp; die Ansicht zeigt deshalb schlicht alle Paare. Dass ein heisser Stab sich
 * nicht zerlegen laesst, sagt schon das Rezept selbst, wenn man es versucht.
 */
public class RBMKDisassemblyRecipeHandler extends ConversionRecipeHandler {

    public static final RecipeType<Conversion> RECIPE_TYPE = type("rbmk_disassembly");

    public RBMKDisassemblyRecipeHandler(IGuiHelper guiHelper) {
        super(guiHelper, NtmBlocks.RBMK_ROD.asItem(), "container.rbmkDisassembly");
    }

    @Override public RecipeType<Conversion> getRecipeType() { return RECIPE_TYPE; }

    public static List<Conversion> getRecipes() {

        List<Conversion> recipes = new ArrayList<>();

        for(Item item : BuiltInRegistries.ITEM) {

            if(!(item instanceof RBMKRodItem rod) || rod.pellet == null) continue;

            Item pelletItem = rod.pellet.get();
            boolean xenon = pelletItem instanceof RBMKPelletItem pellet && pellet.isXenonEnabled();

            for(int depletion = 0; depletion <= 4; depletion++) {
                recipes.add(new Conversion(new ItemStack(rod), MetaHelper.newStack(pelletItem, 8, depletion)));
                if(xenon) recipes.add(new Conversion(new ItemStack(rod), MetaHelper.newStack(pelletItem, 8, depletion + 5)));
            }
        }

        return recipes;
    }
}
