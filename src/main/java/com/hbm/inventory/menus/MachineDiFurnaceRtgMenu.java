package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineDiFurnaceRtgBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineDiFurnaceRTG.
 * Die Slot-Positionen entsprechen dem Original.
 */
public class MachineDiFurnaceRtgMenu extends MenuBase<MachineDiFurnaceRtgBlockEntity> {

    private static final int SLOT_COUNT = 9;

    public MachineDiFurnaceRtgMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineDiFurnaceRtgBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public MachineDiFurnaceRtgMenu(int id, Inventory inventory, MachineDiFurnaceRtgBlockEntity be) {
        super(NtmMenuTypes.MACHINE_DIFURNACE_RTG.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineDiFurnaceRtgBlockEntity.SLOT_INPUT_UPPER, 80, 18));
        this.addSlot(new SlotNonRetarded(be, MachineDiFurnaceRtgBlockEntity.SLOT_INPUT_LOWER, 80, 54));
        this.addSlot(new SlotTakeOnly(be, MachineDiFurnaceRtgBlockEntity.SLOT_OUTPUT, 134, 36));

        this.addSlot(new SlotNonRetarded(be, 3, 22, 18));
        this.addSlot(new SlotNonRetarded(be, 4, 40, 18));
        this.addSlot(new SlotNonRetarded(be, 5, 22, 36));
        this.addSlot(new SlotNonRetarded(be, 6, 40, 36));
        this.addSlot(new SlotNonRetarded(be, 7, 22, 54));
        this.addSlot(new SlotNonRetarded(be, 8, 40, 54));

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
                    if(!this.moveItemStackTo(stack, MachineDiFurnaceRtgBlockEntity.SLOT_RTG_FIRST, MachineDiFurnaceRtgBlockEntity.SLOT_RTG_LAST + 1, false)) return ItemStack.EMPTY;
                } else if(!this.moveItemStackTo(stack, MachineDiFurnaceRtgBlockEntity.SLOT_INPUT_UPPER, MachineDiFurnaceRtgBlockEntity.SLOT_INPUT_LOWER + 1, false)) {
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
