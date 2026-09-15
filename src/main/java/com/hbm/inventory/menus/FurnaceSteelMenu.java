package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.FurnaceSteelBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFurnaceSteel.
 * Slot-Positionen unveraendert.
 */
public class FurnaceSteelMenu extends MenuBase<FurnaceSteelBlockEntity> {

    private static final int SLOT_COUNT = 6;

    public FurnaceSteelMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FurnaceSteelBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FurnaceSteelMenu(int id, Inventory inventory, FurnaceSteelBlockEntity be) {
        super(NtmMenuTypes.FURNACE_STEEL.get(), id, be);

        // Eingang
        this.addSlot(new SlotNonRetarded(be, 0, 35, 17));
        this.addSlot(new SlotNonRetarded(be, 1, 35, 35));
        this.addSlot(new SlotNonRetarded(be, 2, 35, 53));
        // Ausgang
        this.addSlot(new SlotTakeOnly(be, 3, 125, 17));
        this.addSlot(new SlotTakeOnly(be, 4, 125, 35));
        this.addSlot(new SlotTakeOnly(be, 5, 125, 53));

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
                // Nur in die drei Eingangsslots, wie im Original.
                if(!this.moveItemStackTo(stack, 0, FurnaceSteelBlockEntity.LANES, false)) return ItemStack.EMPTY;
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
