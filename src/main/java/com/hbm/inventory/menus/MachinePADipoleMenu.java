package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.albion.MachinePADipoleBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.PACoilItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPADipole.
 *
 * Zwei Faecher: die Batterie und die Spule. Ohne Spule steht die Anlage.
 */
public class MachinePADipoleMenu extends MenuBase<MachinePADipoleBlockEntity> {

    public static final int SLOTS = 2;

    public MachinePADipoleMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePADipoleBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePADipoleMenu(int id, Inventory inventory, MachinePADipoleBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PA_DIPOLE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePADipoleBlockEntity.SLOT_BATTERY, 8, 72));
        this.addSlot(new SlotNonRetarded(be, MachinePADipoleBlockEntity.SLOT_COIL, 89, 26));

        this.playerInv(inventory, 8, 122);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < SLOTS) {
            if(!this.moveItemStackTo(stack, SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, MachinePADipoleBlockEntity.SLOT_BATTERY, MachinePADipoleBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof PACoilItem) {
            if(!this.moveItemStackTo(stack, MachinePADipoleBlockEntity.SLOT_COIL, MachinePADipoleBlockEntity.SLOT_COIL + 1, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
