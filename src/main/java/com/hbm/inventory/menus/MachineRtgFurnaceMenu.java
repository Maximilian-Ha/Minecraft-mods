package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRtgFurnaceBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.RTGPelletItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRtgFurnace.
 * Die Slot-Positionen entsprechen dem Original.
 */
public class MachineRtgFurnaceMenu extends MenuBase<MachineRtgFurnaceBlockEntity> {

    private static final int SLOT_COUNT = 5;

    public MachineRtgFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRtgFurnaceBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public MachineRtgFurnaceMenu(int id, Inventory inventory, MachineRtgFurnaceBlockEntity be) {
        super(NtmMenuTypes.MACHINE_RTG_FURNACE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineRtgFurnaceBlockEntity.SLOT_INPUT, 56, 17));
        this.addSlot(new SlotNonRetarded(be, 1, 38, 53));
        this.addSlot(new SlotNonRetarded(be, 2, 56, 53));
        this.addSlot(new SlotNonRetarded(be, 3, 74, 53));
        this.addSlot(new SlotTakeOnly(be, MachineRtgFurnaceBlockEntity.SLOT_OUTPUT, 116, 35));

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
            } else {
                if(stack.getItem() instanceof RTGPelletItem) {
                    if(!this.moveItemStackTo(stack, MachineRtgFurnaceBlockEntity.SLOT_RTG_FIRST, MachineRtgFurnaceBlockEntity.SLOT_RTG_LAST + 1, false)) return ItemStack.EMPTY;
                } else if(!this.moveItemStackTo(stack, MachineRtgFurnaceBlockEntity.SLOT_INPUT, MachineRtgFurnaceBlockEntity.SLOT_INPUT + 1, false)) {
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
