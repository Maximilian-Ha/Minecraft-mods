package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineChemicalFactoryBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.BlueprintsItem;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineChemicalFactory.
 * Alle Fachkoordinaten sind die des Originals; die Faecher stehen enger als sonst (sechzehn
 * statt achtzehn Pixel), damit vier Rezeptfelder untereinander passen.
 */
public class MachineChemicalFactoryMenu extends MenuBase<MachineChemicalFactoryBlockEntity> {

    public MachineChemicalFactoryMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineChemicalFactoryBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineChemicalFactoryMenu(int id, Inventory inventory, MachineChemicalFactoryBlockEntity be) {
        super(NtmMenuTypes.MACHINE_CHEMICAL_FACTORY.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, 0, 224, 88));
        this.addSlots(be, 1, 206, 125, 3, 1);

        for(int i = 0; i < MachineChemicalFactoryBlockEntity.FIELDS; i++) {
            this.addSlots(be, 4 + i * 7, 93, 20 + i * 22, 1, 1, 16);
            this.addSlots(be, 5 + i * 7, 10, 20 + i * 22, 1, 3, 16);
            this.addOutputSlots(inventory.player, be, 8 + i * 7, 139, 20 + i * 22, 1, 3, 16);
        }

        this.playerInv(inventory, 26, 134);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < MachineChemicalFactoryBlockEntity.SLOTS) {

                if(!this.moveItemStackTo(stack, MachineChemicalFactoryBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof IBatteryItem) {

                if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof BlueprintsItem) {

                /* Die Vorlage geht in das erste freie der vier Vorlagenfaecher. */
                if(!this.moveToTemplate(stack)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof MachineUpgradeItem) {

                if(!this.moveItemStackTo(stack, 1, 4, false)) return ItemStack.EMPTY;

            } else {

                if(!this.moveToInput(stack)) return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return ret;
    }

    private boolean moveToTemplate(ItemStack stack) {
        for(int i = 0; i < MachineChemicalFactoryBlockEntity.FIELDS; i++) {
            if(this.moveItemStackTo(stack, 4 + i * 7, 5 + i * 7, false)) return true;
        }
        return false;
    }

    private boolean moveToInput(ItemStack stack) {
        for(int i = 0; i < MachineChemicalFactoryBlockEntity.FIELDS; i++) {
            if(this.moveItemStackTo(stack, 5 + i * 7, 8 + i * 7, false)) return true;
        }
        return false;
    }
}
