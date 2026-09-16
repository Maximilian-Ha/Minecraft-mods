package com.zuxelus.energycontrol.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Die sechs Eingabefaecher der Bausatzmontage, so wie das Rezeptwesen sie sehen will.
 *
 * Auf 1.21.1 bekommt ein Rezept keinen Behaelter mehr, sondern ein {@link RecipeInput} --
 * eine Sicht auf genau die Faecher, die zaehlen. Das Ausgabefach gehoert nicht dazu.
 */
public record KitAssemblerInput(Container container, int size) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return container.getItem(index);
    }

    @Override
    public boolean isEmpty() {
        for(int i = 0; i < size; i++) {
            if(!container.getItem(i).isEmpty()) return false;
        }
        return true;
    }
}
