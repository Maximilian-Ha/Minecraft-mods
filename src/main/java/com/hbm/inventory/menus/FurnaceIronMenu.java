package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.FurnaceIronBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFurnaceIron.
 * Slot-Positionen unveraendert.
 */
public class FurnaceIronMenu extends MenuBase<FurnaceIronBlockEntity> {

    private static final int SLOT_COUNT = 5;

    public FurnaceIronMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (FurnaceIronBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public FurnaceIronMenu(int id, Inventory inventory, FurnaceIronBlockEntity be) {
        super(NtmMenuTypes.FURNACE_IRON.get(), id, be);

        // Eingang
        this.addSlot(new SlotNonRetarded(be, FurnaceIronBlockEntity.SLOT_INPUT, 53, 17));
        // Brennstoff
        this.addSlot(new SlotNonRetarded(be, FurnaceIronBlockEntity.SLOT_FUEL_1, 53, 53));
        this.addSlot(new SlotNonRetarded(be, FurnaceIronBlockEntity.SLOT_FUEL_2, 71, 53));
        // Ausgang
        this.addSlot(new SlotTakeOnly(be, FurnaceIronBlockEntity.SLOT_OUTPUT, 125, 35));
        // Upgrade
        this.addSlot(new SlotNonRetarded(be, FurnaceIronBlockEntity.SLOT_UPGRADE, 17, 35));

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
                if(!this.moveItemStackTo(stack, 0, SLOT_COUNT, false)) return ItemStack.EMPTY;
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
