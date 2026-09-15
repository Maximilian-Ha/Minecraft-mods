package com.hbm.util;

import com.hbm.inventory.RecipesCommon.AStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class InventoryUtil {

    public static List<ItemStack> getItemsFromBothHands(LivingEntity living) {
        return Arrays.stream(InteractionHand.values()).map(living::getItemInHand).toList();
    }

    public static Stream<ItemStack> getItemSteamFromBothHands(LivingEntity living) {
        return Arrays.stream(InteractionHand.values()).map(living::getItemInHand);
    }

    /**
     * Legt einen Stapel in einen Fachbereich: erst auf gleichartige Stapel draufpacken, dann in
     * das erste leere Fach. Gibt zurueck, was NICHT untergebracht werden konnte -- ein leerer
     * Stapel heisst "alles hat gepasst".
     *
     * Der uebergebene Stapel wird dabei verbraucht; wer ihn behalten will, gibt eine Kopie.
     *
     * Portiert aus 1.7.10: InventoryUtil.tryAddItemToInventory. Dort steht das Ganze in drei
     * Methoden mit null als "nichts uebrig"; auf 1.21 gibt es ItemStack.EMPTY, und die drei
     * Faelle passen in eine.
     *
     * ENDE IST EINSCHLIESSLICH, wie im Original: (slots, 0, 3, ...) meint die Faecher null bis
     * drei, also vier Stueck.
     */
    public static ItemStack tryAddItemToInventory(NonNullList<ItemStack> inv, int start, int end, ItemStack stack) {

        if(stack.isEmpty()) return ItemStack.EMPTY;

        for(int i = start; i <= end && i < inv.size(); i++) {

            ItemStack slot = inv.get(i);
            if(slot.isEmpty()) continue;
            if(!ItemStack.isSameItemSameComponents(slot, stack)) continue;

            int transfer = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
            if(transfer <= 0) continue;

            slot.grow(transfer);
            stack.shrink(transfer);

            if(stack.isEmpty()) return ItemStack.EMPTY;
        }

        for(int i = start; i <= end && i < inv.size(); i++) {

            if(!inv.get(i).isEmpty()) continue;

            int transfer = Math.min(stack.getCount(), stack.getMaxStackSize());
            inv.set(i, stack.copyWithCount(transfer));
            stack.shrink(transfer);

            if(stack.isEmpty()) return ItemStack.EMPTY;
        }

        return stack;
    }

    /**
     * Ob ALLE genannten Stapel zusammen in den Fachbereich passen wuerden, ohne etwas abzulegen.
     *
     * Gerechnet wird auf einer Kopie des Bereichs -- sonst waere die Frage nicht zu beantworten,
     * ohne sie zugleich zu beantworten. Wichtig ist, dass die Stapel NACHEINANDER gepasst haben
     * muessen: zwei Stapel, die einzeln in dasselbe letzte Fach passten, passen nicht zusammen.
     *
     * Portiert aus 1.7.10: InventoryUtil.doesArrayHaveSpace.
     */
    public static boolean doesArrayHaveSpace(NonNullList<ItemStack> inv, int start, int end, ItemStack[] items) {

        NonNullList<ItemStack> copy = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for(int i = 0; i < inv.size(); i++) copy.set(i, inv.get(i).copy());

        for(ItemStack item : items) {
            if(item == null || item.isEmpty()) continue;
            if(!tryAddItemToInventory(copy, start, end, item.copy()).isEmpty()) return false;
        }

        return true;
    }


    /**
     * Checks if a player has matching item stacks in his inventory and removes them if so desired
     * @param player guess
     * @param stacks the AStacks (comparable or ore-dicted)
     * @param shouldRemove whether it should just return true or false or if a successful check should also remove all the items
     * @return whether the player has the required item stacks or not
     */
    public static boolean doesPlayerHaveAStacks(Player player, List<AStack> stacks, boolean shouldRemove) {

        NonNullList<ItemStack> original = player.getInventory().items;
        NonNullList<ItemStack> inventory = NonNullList.withSize(original.size(), ItemStack.EMPTY);
        boolean[] modified = new boolean[original.size()];
        AStack[] input = new AStack[stacks.size()];

        //first we copy the inputs into an array because 1. it's easier to deal with and 2. we can dick around with the stack sized with no repercussions
        for (int i = 0; i < input.length; i++) {
            input[i] = stacks.get(i).copy();
        }

        //then we copy the inventory so we can dick around with it as well without making actual modifications to the player's inventory
        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).isEmpty()) {
                inventory.set(i, original.get(i).copy());
            }
        }

        //now we go through every ingredient...
        for (int i = 0; i < input.length; i++) {

            AStack stack = input[i];

            //...and compare each ingredient to every stack in the inventory
            for (int j = 0; j < inventory.size(); j++) {

                ItemStack inv = inventory.get(j);

                //we check if it matches but ignore stack stacksize for now
                if (stack.matchesRecipe(inv, true)) {
                    //and NOW we care about the stack stacksize
                    int size = Math.min(stack.stacksize, inv.getCount());
                    stack.stacksize -= size;
                    inv.shrink(size);
                    modified[j] = true;

                    //spent stacks are removed from the equation so that we don't cross ourselves later on
                    if (stack.stacksize <= 0) {
                        input[i] = null;
                        break;
                    }

                    if (inv.getCount() <= 0) {
                        inventory.set(j, ItemStack.EMPTY);
                    }
                }
            }
        }

        for (AStack stack : input) {
            if (stack != null) {
                return false;
            }
        }

        if (shouldRemove) {
            for(int i = 0; i < original.size(); i++) {

                if (!inventory.get(i).isEmpty() && inventory.get(i).getCount() <= 0) {
                    original.set(i, ItemStack.EMPTY);
                } else {
                    if (modified[i]) original.set(i, inventory.get(i));
                }
            }
        }

        return true;
    }
}
