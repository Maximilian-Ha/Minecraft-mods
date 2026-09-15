package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineCrucibleBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCrucible.
 * Alle Fachkoordinaten sind unveraendert uebernommen.
 */
public class MachineCrucibleMenu extends MenuBase<MachineCrucibleBlockEntity> {

    public MachineCrucibleMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCrucibleBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCrucibleMenu(int id, Inventory inventory, MachineCrucibleBlockEntity be) {
        super(NtmMenuTypes.MACHINE_CRUCIBLE.get(), id, be);

        this.addSlots(be, 1, 107, 18, 3, 3);
        this.playerInv(inventory, 8, 132, 190);
    }

    /**
     * Kein Schnellablegen im Tiegel: die Faecher fassen nur je einen Gegenstand, und der
     * Sammelmodus wuerde sie ueberfuellen. Das Original sperrt dafuer ebenfalls Modus 2.
     */
    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if(type == ClickType.SWAP) return;
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index <= 8) {
                if(!this.moveItemStackTo(stack, 9, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if(!this.moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }

        return ret;
    }
}
