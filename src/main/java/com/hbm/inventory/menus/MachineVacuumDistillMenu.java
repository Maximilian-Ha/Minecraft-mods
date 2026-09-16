package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.oil.MachineVacuumDistillBlockEntity;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineVacuumDistill.
 * Alle Platzkoordinaten sind unveraendert uebernommen.
 *
 * Die beiden Plaetze fuer die Eingabe sind gesperrt -- das Oel muss unter Druck ankommen und
 * laesst sich nicht aus einem Kanister einfuellen. Im Original heisst dieser Platz
 * SlotDeprecated; hier weist die Blockentitaet ihn in canPlaceItem ab, und SlotNonRetarded
 * fragt genau danach.
 */
public class MachineVacuumDistillMenu extends MenuBase<MachineVacuumDistillBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 11;

    public MachineVacuumDistillMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineVacuumDistillBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineVacuumDistillMenu(int id, Inventory inventory, MachineVacuumDistillBlockEntity be) {
        super(NtmMenuTypes.MACHINE_VACUUM_DISTILL.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 26, 90));
        // Eingabe -- gesperrt, siehe oben
        this.addSlot(new SlotNonRetarded(be, 1, 44, 90));
        this.addSlot(new SlotTakeOnly(be, 2, 44, 108));
        // Schweroel
        this.addSlot(new SlotNonRetarded(be, 3, 80, 90));
        this.addSlot(new SlotTakeOnly(be, 4, 80, 108));
        // Reformat
        this.addSlot(new SlotNonRetarded(be, 5, 98, 90));
        this.addSlot(new SlotTakeOnly(be, 6, 98, 108));
        // Leichtoel
        this.addSlot(new SlotNonRetarded(be, 7, 116, 90));
        this.addSlot(new SlotTakeOnly(be, 8, 116, 108));
        // Sauergas
        this.addSlot(new SlotNonRetarded(be, 9, 134, 90));
        this.addSlot(new SlotTakeOnly(be, 10, 134, 108));
        // Fluidkennzeichner
        this.addSlot(new SlotNonRetarded(be, 11, 26, 108));

        this.playerInv(inventory, 8, 156, 214);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index <= LAST_MACHINE_SLOT) {
            if(!this.moveItemStackTo(stack, LAST_MACHINE_SLOT + 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if(stack.getItem() instanceof IBatteryItem) {
                if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                if(!this.moveItemStackTo(stack, 11, 12, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[1].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[2].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 5, 6, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[3].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[4].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 9, 10, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if(stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return ret;
    }
}
