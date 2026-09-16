package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.oil.MachineHydrotreaterBlockEntity;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineHydrotreater.
 * Alle Platzkoordinaten sind unveraendert uebernommen.
 *
 * Die beiden Plaetze fuer den Wasserstoff sind gesperrt -- er muss unter Druck stehen und
 * laesst sich nicht aus einem Kanister einfuellen. Im Original heisst dieser Platz
 * SlotDeprecated; hier weist die Blockentitaet ihn in canPlaceItem ab, und SlotNonRetarded
 * fragt genau danach.
 */
public class MachineHydrotreaterMenu extends MenuBase<MachineHydrotreaterBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 10;

    public MachineHydrotreaterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineHydrotreaterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineHydrotreaterMenu(int id, Inventory inventory, MachineHydrotreaterBlockEntity be) {
        super(NtmMenuTypes.MACHINE_HYDROTREATER.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 17, 90));
        // Eingabe: das Oel, das entschwefelt wird
        this.addSlot(new SlotNonRetarded(be, 1, 35, 90));
        this.addSlot(new SlotTakeOnly(be, 2, 35, 108));
        // Wasserstoff -- gesperrt, siehe oben
        this.addSlot(new SlotNonRetarded(be, 3, 53, 90));
        this.addSlot(new SlotTakeOnly(be, 4, 53, 108));
        // Entschwefeltes Oel
        this.addSlot(new SlotNonRetarded(be, 5, 125, 90));
        this.addSlot(new SlotTakeOnly(be, 6, 125, 108));
        // Sauergas
        this.addSlot(new SlotNonRetarded(be, 7, 143, 90));
        this.addSlot(new SlotTakeOnly(be, 8, 143, 108));
        // Fluidkennzeichner
        this.addSlot(new SlotNonRetarded(be, 9, 17, 108));
        // Katalysator
        this.addSlot(new SlotNonRetarded(be, 10, 89, 36));

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
                if(!this.moveItemStackTo(stack, 9, 10, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() == NtmItems.CATALYTIC_CONVERTER.get()) {
                if(!this.moveItemStackTo(stack, 10, 11, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[0].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[2].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 5, 6, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[3].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;
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
