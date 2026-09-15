package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineCrystallizerBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCrystallizer.
 * Slotpositionen unveraendert uebernommen.
 */
public class MachineCrystallizerMenu extends MenuBase<MachineCrystallizerBlockEntity> {

    public MachineCrystallizerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCrystallizerBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCrystallizerMenu(int id, Inventory inventory, MachineCrystallizerBlockEntity be) {
        super(NtmMenuTypes.MACHINE_CRYSTALLIZER.get(), id, be);

        // Eingang
        this.addSlot(new SlotNonRetarded(be, MachineCrystallizerBlockEntity.SLOT_INPUT, 62, 45));
        // Batterie
        this.addSlot(new SlotNonRetarded(be, MachineCrystallizerBlockEntity.SLOT_BATTERY, 152, 72));
        // Ausgang
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineCrystallizerBlockEntity.SLOT_OUTPUT, 113, 45));
        // Fluidslots
        this.addSlot(new SlotNonRetarded(be, MachineCrystallizerBlockEntity.SLOT_FLUID_IN, 17, 18));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineCrystallizerBlockEntity.SLOT_FLUID_OUT, 17, 54));
        // Aufwertungen
        this.addSlot(new SlotNonRetarded(be, MachineCrystallizerBlockEntity.SLOT_UPGRADE_START, 80, 18));
        this.addSlot(new SlotNonRetarded(be, MachineCrystallizerBlockEntity.SLOT_UPGRADE_END, 98, 18));
        // Fluidkennung
        this.addSlot(new SlotNonRetarded(be, MachineCrystallizerBlockEntity.SLOT_IDENTIFIER, 35, 72));

        this.playerInv(inventory, 8, 122, 180);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index <= 7) {
                if(!this.moveItemStackTo(stack, 8, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {

                if(stack.getItem() instanceof IBatteryItem || stack.getItem() == NtmItems.BATTERY_CREATIVE.get()) {
                    if(!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;

                } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;

                } else if(stack.getItem() instanceof MachineUpgradeItem) {
                    if(!this.moveItemStackTo(stack, 5, 7, false)) return ItemStack.EMPTY;

                } else {
                    if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
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
