package com.hbm.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.SlotPattern.
 *
 * Ein Musterfach haelt keinen Gegenstand, sondern ein Abbild davon. Man legt hinein, indem man
 * mit einem Gegenstand in der Hand hineinklickt -- der Gegenstand bleibt dabei in der Hand,
 * im Fach landet nur eine Kopie mit der Anzahl eins. Herausnehmen laesst sich daraus nichts.
 *
 * Das Einlegen selbst macht nicht dieses Fach, sondern das Menue: FilterMenuBase faengt den
 * Klick ab, bevor Minecraft daraus eine Gegenstandsbewegung macht. Hier steht nur, was fuer
 * das Fach in jedem Fall gilt.
 */
public class SlotPattern extends Slot {

    /** Ein Fach, das ein ganzes Buendel zeigen darf -- die Rezeptvorschau des Selbstbauers. */
    protected boolean allowStackSize = false;

    public SlotPattern(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    /**
     * Zeigt dieses Fach die wirkliche Anzahl statt immer eins? Gebraucht wird das dort, wo im
     * Fach kein Muster liegt, sondern ein Ergebnis: ein Rezept, das vier Bretter macht, soll
     * auch vier zeigen. Herausnehmen laesst sich auch daraus nichts.
     */
    public SlotPattern allowStackSize() {
        this.allowStackSize = true;
        return this;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    /**
     * Auch nicht per Umschalttaste: sonst schoebe ein Schnellverschieben aus dem Rucksack
     * Gegenstaende in die Musterfaecher, und das Abbild wuerde zum Gut.
     */
    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return this.allowStackSize ? super.getMaxStackSize() : 1;
    }

    @Override
    public void set(ItemStack stack) {
        if(this.allowStackSize) {
            super.set(stack);
        } else {
            super.set(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
    }
}
