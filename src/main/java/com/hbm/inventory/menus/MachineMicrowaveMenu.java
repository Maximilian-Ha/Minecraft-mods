package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineMicrowaveBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMicrowave.
 * Die Slot-Positionen entsprechen dem Original.
 */
public class MachineMicrowaveMenu extends MenuBase<MachineMicrowaveBlockEntity> {

    private static final int SLOT_COUNT = 3;

    public MachineMicrowaveMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineMicrowaveBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public MachineMicrowaveMenu(int id, Inventory inventory, MachineMicrowaveBlockEntity be) {
        super(NtmMenuTypes.MACHINE_MICROWAVE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineMicrowaveBlockEntity.SLOT_INPUT, 80, 35));
        this.addSlot(new SlotTakeOnly(be, MachineMicrowaveBlockEntity.SLOT_OUTPUT, 140, 35));
        this.addSlot(new SlotNonRetarded(be, MachineMicrowaveBlockEntity.SLOT_BATTERY, 8, 53));

        this.playerInv(inventory, 8, 84, 142);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < SLOT_COUNT) {
                if(!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof IBatteryItem) {
                if(!this.moveItemStackTo(stack, MachineMicrowaveBlockEntity.SLOT_BATTERY, MachineMicrowaveBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
            } else if(!this.moveItemStackTo(stack, MachineMicrowaveBlockEntity.SLOT_INPUT, MachineMicrowaveBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return ret;
    }
}
