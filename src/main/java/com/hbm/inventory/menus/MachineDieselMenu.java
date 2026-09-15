package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineDieselBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineDiesel.
 * Slot-Positionen wie im Original.
 */
public class MachineDieselMenu extends MenuBase<MachineDieselBlockEntity> {

    private static final int SLOT_COUNT = 4;

    public MachineDieselMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineDieselBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineDieselMenu(int id, Inventory inventory, MachineDieselBlockEntity be) {
        super(NtmMenuTypes.MACHINE_DIESEL.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineDieselBlockEntity.SLOT_FLUID_IN, 17, 17));
        this.addSlot(new SlotTakeOnly(be, MachineDieselBlockEntity.SLOT_FLUID_OUT, 17, 53));
        this.addSlot(new SlotNonRetarded(be, MachineDieselBlockEntity.SLOT_BATTERY, 141, 71));
        this.addSlot(new SlotNonRetarded(be, MachineDieselBlockEntity.SLOT_IDENTIFIER, 35, 71));

        this.playerInv(inventory, 8, 121, 179);
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
            } else {
                if(stack.getItem() instanceof IBatteryItem) {
                    if(!this.moveItemStackTo(stack, MachineDieselBlockEntity.SLOT_BATTERY, MachineDieselBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
                } else if(!this.moveItemStackTo(stack, MachineDieselBlockEntity.SLOT_FLUID_IN, MachineDieselBlockEntity.SLOT_FLUID_IN + 1, false)) {
                    return ItemStack.EMPTY;
                }
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
