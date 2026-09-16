package com.zuxelus.energycontrol.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Ein Fach, aus dem sich nichts nehmen laesst -- der Gegenstand, der die Oberflaeche traegt. */
public class SlotLocked extends Slot {

    public SlotLocked(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
