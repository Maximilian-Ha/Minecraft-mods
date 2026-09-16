package com.zuxelus.energycontrol.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.12.2: com.zuxelus.zlib.containers.ContainerBase.
 *
 * Gemeinsames aller Oberflaechen des Mods: das Spielerinventar an der ueblichen Stelle
 * und das Verschieben mit Umschalt-Klick.
 */
public abstract class ECMenuBase<T extends Container> extends AbstractContainerMenu {

    public final T be;

    protected ECMenuBase(MenuType<?> type, int id, T be) {
        super(type, id);
        this.be = be;
    }

    @Override
    public boolean stillValid(Player player) {
        return be.stillValid(player);
    }

    /** Spielerinventar mit den Abstaenden, die Minecraft selbst benutzt. */
    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for(int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, x + col * 18, y + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();
        int machineSlots = be.getContainerSize();

        if(machineSlots == 0) {
            // Ein Block ohne Faecher -- dann bleibt nur das Uebliche zwischen Rucksack
            // und Schnellleiste, statt gar nichts zu tun.
            int hotbarStart = this.slots.size() - 9;
            if(index < hotbarStart) {
                if(!moveItemStackTo(stack, hotbarStart, this.slots.size(), false)) return ItemStack.EMPTY;
            } else {
                if(!moveItemStackTo(stack, 0, hotbarStart, false)) return ItemStack.EMPTY;
            }
        } else if(index < machineSlots) {
            if(!moveItemStackTo(stack, machineSlots, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if(!moveItemStackTo(stack, 0, machineSlots, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return result;
    }
}
