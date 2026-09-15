package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachinePWRControllerBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.NtmItems;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPWR.
 * Die Slot-Positionen entsprechen dem Original.
 */
public class MachinePWRMenu extends MenuBase<MachinePWRControllerBlockEntity> {

    private static final int SLOT_COUNT = 3;

    public MachinePWRMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePWRControllerBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public MachinePWRMenu(int id, Inventory inventory, MachinePWRControllerBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PWR.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePWRControllerBlockEntity.SLOT_FUEL_IN, 53, 5));
        this.addSlot(new SlotTakeOnly(be, MachinePWRControllerBlockEntity.SLOT_FUEL_OUT, 89, 32));
        this.addSlot(new SlotNonRetarded(be, MachinePWRControllerBlockEntity.SLOT_FLUID_ID, 8, 59));

        this.playerInv(inventory, 8, 106, 164);
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
                if(stack.getItem() == NtmItems.PWR_FUEL.get()) {
                    if(!this.moveItemStackTo(stack, MachinePWRControllerBlockEntity.SLOT_FUEL_IN, MachinePWRControllerBlockEntity.SLOT_FUEL_IN + 1, false)) return ItemStack.EMPTY;
                } else if(!this.moveItemStackTo(stack, MachinePWRControllerBlockEntity.SLOT_FLUID_ID, MachinePWRControllerBlockEntity.SLOT_FLUID_ID + 1, false)) {
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
