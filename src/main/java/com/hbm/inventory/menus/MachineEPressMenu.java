package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineEPressBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.StampItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineEPress.
 * Alle Fachkoordinaten sind die des Originals.
 */
public class MachineEPressMenu extends MenuBase<MachineEPressBlockEntity> {

    public MachineEPressMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineEPressBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineEPressMenu(int id, Inventory inventory, MachineEPressBlockEntity be) {
        super(NtmMenuTypes.MACHINE_EPRESS.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineEPressBlockEntity.SLOT_BATTERY, 152, 54));
        this.addSlot(new SlotNonRetarded(be, MachineEPressBlockEntity.SLOT_STAMP, 19, 15));
        this.addSlot(new SlotNonRetarded(be, MachineEPressBlockEntity.SLOT_INPUT, 19, 51));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineEPressBlockEntity.SLOT_OUTPUT, 79, 33));
        this.addSlot(new SlotNonRetarded(be, MachineEPressBlockEntity.SLOT_UPGRADE, 111, 32));

        this.playerInv(inventory, 8, 104, 162);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < MachineEPressBlockEntity.SLOT_UPGRADE + 1) {

                if(!this.moveItemStackTo(stack, MachineEPressBlockEntity.SLOT_UPGRADE + 1, this.slots.size(), true)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof IBatteryItem) {

                if(!this.moveItemStackTo(stack, MachineEPressBlockEntity.SLOT_BATTERY, MachineEPressBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof StampItem) {

                if(!this.moveItemStackTo(stack, MachineEPressBlockEntity.SLOT_STAMP, MachineEPressBlockEntity.SLOT_STAMP + 1, false)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof MachineUpgradeItem) {

                if(!this.moveItemStackTo(stack, MachineEPressBlockEntity.SLOT_UPGRADE, MachineEPressBlockEntity.SLOT_UPGRADE + 1, false)) return ItemStack.EMPTY;

            } else {

                if(!this.moveItemStackTo(stack, MachineEPressBlockEntity.SLOT_INPUT, MachineEPressBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
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
