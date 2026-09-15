package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPASource.
 *
 * Oben die beiden Ausgangsstoffe nebeneinander, darunter die beiden Rueckgabefaecher fuer ihre
 * Huellen, links unten die Batterie.
 */
public class MachinePASourceMenu extends MenuBase<MachinePASourceBlockEntity> {

    public static final int SLOTS = 5;

    public MachinePASourceMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePASourceBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePASourceMenu(int id, Inventory inventory, MachinePASourceBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PA_SOURCE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePASourceBlockEntity.SLOT_BATTERY, 8, 72));

        this.addSlot(new SlotNonRetarded(be, MachinePASourceBlockEntity.SLOT_INPUT_1, 62, 16));
        this.addSlot(new SlotNonRetarded(be, MachinePASourceBlockEntity.SLOT_INPUT_2, 80, 16));

        this.addSlot(new SlotTakeOnly(be, MachinePASourceBlockEntity.SLOT_CONTAINER_1, 62, 43));
        this.addSlot(new SlotTakeOnly(be, MachinePASourceBlockEntity.SLOT_CONTAINER_2, 80, 43));

        this.playerInv(inventory, 8, 122);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < SLOTS) {
            if(!this.moveItemStackTo(stack, SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, MachinePASourceBlockEntity.SLOT_BATTERY, MachinePASourceBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;

        } else {
            if(!this.moveItemStackTo(stack, MachinePASourceBlockEntity.SLOT_INPUT_1, MachinePASourceBlockEntity.SLOT_INPUT_2 + 1, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
