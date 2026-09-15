package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineTurbofanBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineTurbofan.
 * Slot-Positionen und die Reihenfolge im Shift-Klick unveraendert.
 *
 * Nicht uebernommen: die Fortschrittsbalken-Synchronisation der Nachbrennerstufe
 * (addCraftingToCrafters/detectAndSendChanges/updateProgressBar). Der Wert steckt
 * bereits im Sammelpaket des Blockentity (serialize/deserialize) und braucht
 * keinen zweiten Weg.
 */
public class MachineTurbofanMenu extends MenuBase<MachineTurbofanBlockEntity> {

    private static final int SLOT_COUNT = 5;

    public MachineTurbofanMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineTurbofanBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineTurbofanMenu(int id, Inventory inventory, MachineTurbofanBlockEntity be) {
        super(NtmMenuTypes.MACHINE_TURBOFAN.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineTurbofanBlockEntity.SLOT_FLUID_IN, 17, 17));
        this.addSlot(new SlotTakeOnly(be, MachineTurbofanBlockEntity.SLOT_FLUID_OUT, 17, 53));
        this.addSlot(new SlotNonRetarded(be, MachineTurbofanBlockEntity.SLOT_UPGRADE, 98, 71));
        this.addSlot(new SlotNonRetarded(be, MachineTurbofanBlockEntity.SLOT_BATTERY, 143, 71));
        this.addSlot(new SlotNonRetarded(be, MachineTurbofanBlockEntity.SLOT_IDENTIFIER, 44, 71));

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

                if(ret.getItem() instanceof IBatteryItem) {
                    if(!this.moveItemStackTo(stack, MachineTurbofanBlockEntity.SLOT_BATTERY, MachineTurbofanBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
                } else if(ret.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.moveItemStackTo(stack, MachineTurbofanBlockEntity.SLOT_IDENTIFIER, MachineTurbofanBlockEntity.SLOT_IDENTIFIER + 1, false)) return ItemStack.EMPTY;
                } else if(ret.getItem() instanceof MachineUpgradeItem) {
                    if(!this.moveItemStackTo(stack, MachineTurbofanBlockEntity.SLOT_UPGRADE, MachineTurbofanBlockEntity.SLOT_UPGRADE + 1, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.moveItemStackTo(stack, MachineTurbofanBlockEntity.SLOT_FLUID_IN, MachineTurbofanBlockEntity.SLOT_FLUID_IN + 1, false)) return ItemStack.EMPTY;
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
