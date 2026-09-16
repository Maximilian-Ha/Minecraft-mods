package com.zuxelus.energycontrol.menus;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Ein Fach, das nur annimmt, was die Block-Entitaet dafuer zulaesst. */
public class SlotFiltered extends Slot {

    public SlotFiltered(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getContainerSlot(), stack);
    }
}
