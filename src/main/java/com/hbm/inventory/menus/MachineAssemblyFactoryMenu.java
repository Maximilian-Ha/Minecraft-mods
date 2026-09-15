package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineAssemblyFactoryBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineAssemblyFactory.
 * Alle Fachkoordinaten sind die des Originals: vier Felder in zwei Reihen zu zwei.
 */
public class MachineAssemblyFactoryMenu extends MenuBase<MachineAssemblyFactoryBlockEntity> {

    private static final int STRIDE = 14;

    public MachineAssemblyFactoryMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineAssemblyFactoryBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineAssemblyFactoryMenu(int id, Inventory inventory, MachineAssemblyFactoryBlockEntity be) {
        super(NtmMenuTypes.MACHINE_ASSEMBLY_FACTORY.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, 0, 234, 112));
        this.addSlots(be, 1, 214, 149, 3, 1);

        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {
            this.addSlots(be, 4 + i * STRIDE, 25 + (i % 2) * 109, 54 + (i / 2) * 56, 1, 1);
            this.addSlots(be, 5 + i * STRIDE, 7 + (i % 2) * 109, 20 + (i / 2) * 56, 2, 6, 16);
            this.addOutputSlots(inventory.player, be, 17 + i * STRIDE, 87 + (i % 2) * 109, 54 + (i / 2) * 56, 1, 1);
        }

        this.playerInv(inventory, 33, 158);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < MachineAssemblyFactoryBlockEntity.SLOTS) {

                if(!this.moveItemStackTo(stack, MachineAssemblyFactoryBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof IBatteryItem) {

                if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof BlueprintsItem) {

                if(!this.moveToRange(stack, 4, 5)) return ItemStack.EMPTY;

            } else if(stack.getItem() instanceof MachineUpgradeItem) {

                if(!this.moveItemStackTo(stack, 1, 4, false)) return ItemStack.EMPTY;

            } else {

                if(!this.moveToRange(stack, 5, 17)) return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return ret;
    }

    /** Derselbe Fachbereich in jedem der vier Felder, der Reihe nach. */
    private boolean moveToRange(ItemStack stack, int from, int to) {
        for(int i = 0; i < MachineAssemblyFactoryBlockEntity.FIELDS; i++) {
            if(this.moveItemStackTo(stack, from + i * STRIDE, to + i * STRIDE, false)) return true;
        }
        return false;
    }
}
