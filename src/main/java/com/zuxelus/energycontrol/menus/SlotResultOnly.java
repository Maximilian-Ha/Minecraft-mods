package com.zuxelus.energycontrol.menus;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Ein Ausgabefach: herausnehmen ja, hineinlegen nein. */
public class SlotResultOnly extends Slot {

    public SlotResultOnly(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
