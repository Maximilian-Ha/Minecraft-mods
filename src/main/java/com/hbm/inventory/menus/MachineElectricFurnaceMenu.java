package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineElectricFurnaceBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerElectricFurnace.
 * Die Slot-Positionen entsprechen dem Original.
 */
public class MachineElectricFurnaceMenu extends MenuBase<MachineElectricFurnaceBlockEntity> {

    private static final int SLOT_COUNT = 4;

    public MachineElectricFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineElectricFurnaceBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public MachineElectricFurnaceMenu(int id, Inventory inventory, MachineElectricFurnaceBlockEntity be) {
        super(NtmMenuTypes.MACHINE_ELECTRIC_FURNACE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineElectricFurnaceBlockEntity.SLOT_BATTERY, 152, 54));
        this.addSlot(new SlotNonRetarded(be, MachineElectricFurnaceBlockEntity.SLOT_INPUT, 20, 35));
        this.addSlot(new SlotTakeOnly(be, MachineElectricFurnaceBlockEntity.SLOT_OUTPUT, 80, 35));
        this.addSlot(new SlotNonRetarded(be, MachineElectricFurnaceBlockEntity.SLOT_UPGRADE, 111, 34));

        this.playerInv(inventory, 8, 104, 162);
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
                    if(!this.moveItemStackTo(stack, MachineElectricFurnaceBlockEntity.SLOT_BATTERY, MachineElectricFurnaceBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
                } else if(stack.getItem() instanceof MachineUpgradeItem) {
                    if(!this.moveItemStackTo(stack, MachineElectricFurnaceBlockEntity.SLOT_UPGRADE, MachineElectricFurnaceBlockEntity.SLOT_UPGRADE + 1, false)) return ItemStack.EMPTY;
                } else if(!this.moveItemStackTo(stack, MachineElectricFurnaceBlockEntity.SLOT_INPUT, MachineElectricFurnaceBlockEntity.SLOT_INPUT + 1, false)) {
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
