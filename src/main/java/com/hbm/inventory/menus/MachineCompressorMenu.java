package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineCompressorBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MachineCompressorMenu extends MenuBase<MachineCompressorBlockEntity> {

    public MachineCompressorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCompressorBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCompressorMenu(int id, Inventory inventory, MachineCompressorBlockEntity be) {
        super(NtmMenuTypes.MACHINE_COMPRESSOR.get(), id, be);

        // Fluid ID
        this.addSlot(new SlotNonRetarded(be, MachineCompressorBlockEntity.SLOT_IDENTIFIER, 17, 72));
        // Battery
        this.addSlot(new SlotNonRetarded(be, MachineCompressorBlockEntity.SLOT_BATTERY, 152, 72));
        // Upgrades
        this.addSlot(new SlotNonRetarded(be, MachineCompressorBlockEntity.SLOT_UPGRADE_START, 52, 72));
        this.addSlot(new SlotNonRetarded(be, MachineCompressorBlockEntity.SLOT_UPGRADE_END, 70, 72));

        this.playerInv(inventory, 8, 122, 180);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < 4) {
                if(!this.moveItemStackTo(stack, 4, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if(stack.getItem() instanceof IBatteryItem) {
                    if(!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
                } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.moveItemStackTo(stack, 2, 4, false)) return ItemStack.EMPTY;
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
