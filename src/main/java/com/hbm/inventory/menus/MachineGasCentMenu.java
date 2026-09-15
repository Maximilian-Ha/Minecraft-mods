package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineGasCentBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineGasCent.
 *
 * Vier Ausgabefaecher als Zweiergeviert, darunter Batterie, Fluidkennung und die Aufwertung.
 * Die vier Ausgaben sind reine Entnahmefaecher -- hineinlegen laesst sich dort nichts.
 */
public class MachineGasCentMenu extends MenuBase<MachineGasCentBlockEntity> {

    public MachineGasCentMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineGasCentBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineGasCentMenu(int id, Inventory inventory, MachineGasCentBlockEntity be) {
        super(NtmMenuTypes.MACHINE_GAS_CENT.get(), id, be);

        this.addTakeOnlySlots(be, MachineGasCentBlockEntity.SLOT_OUTPUT_START, 71, 53, 2, 2);

        this.addSlot(new SlotNonRetarded(be, MachineGasCentBlockEntity.SLOT_BATTERY, 182, 71));
        this.addSlot(new SlotNonRetarded(be, MachineGasCentBlockEntity.SLOT_IDENTIFIER, 91, 15));
        this.addSlot(new SlotNonRetarded(be, MachineGasCentBlockEntity.SLOT_UPGRADE, 69, 15));

        this.playerInv(inventory, 8, 122, 180);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < 7) {
            if(!this.moveItemStackTo(stack, 7, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, 4, 5, false)) return ItemStack.EMPTY;
        } else if(stack.getItem() instanceof IItemFluidIdentifier) {
            if(!this.moveItemStackTo(stack, 5, 6, false)) return ItemStack.EMPTY;
        } else if(stack.getItem() == NtmItems.UPGRADE_GC_SPEED.get()) {
            if(!this.moveItemStackTo(stack, 6, 7, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
