package com.hbm.handler.jei;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.RTGPelletItem;
import com.hbm.items.machine.RTGPelletItem.RTGPelletType;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.handler.nei.RTGRecipeHandler.
 *
 * Wozu ein RTG-Pellet zerfaellt. Das Original zeigt als Maschine sowohl den RTG als auch den
 * RTG-Doppelofen; JEI fuehrt beide als Katalysator derselben Ansicht.
 */
public class RTGRecipeHandler extends ConversionRecipeHandler {

    public static final RecipeType<Conversion> RECIPE_TYPE = type("rtg");

    public RTGRecipeHandler(IGuiHelper guiHelper) {
        super(guiHelper, NtmBlocks.MACHINE_RTG.asItem(), "container.rtg");
    }

    @Override public RecipeType<Conversion> getRecipeType() { return RECIPE_TYPE; }

    public static List<Conversion> getRecipes() {

        List<Conversion> recipes = new ArrayList<>();

        for(RTGPelletType type : RTGPelletType.values()) {
            ItemStack pellet = MetaHelper.newStack(NtmItems.PELLET_RTG.get(), 1, type.ordinal());
            ItemStack decayed = RTGPelletItem.getDecayItem(pellet);
            if(!decayed.isEmpty()) recipes.add(new Conversion(pellet, decayed));
        }

        return recipes;
    }
}
