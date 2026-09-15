package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.albion.MachinePAQuadrupoleBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPAQuadrupole.
 *
 * Zwei Faecher: die Batterie und die Spule. Ohne Spule steht die Anlage.
 */
public class MachinePAQuadrupoleMenu extends MenuBase<MachinePAQuadrupoleBlockEntity> {

    public static final int SLOTS = 2;

    public MachinePAQuadrupoleMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePAQuadrupoleBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePAQuadrupoleMenu(int id, Inventory inventory, MachinePAQuadrupoleBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PA_QUADRUPOLE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePAQuadrupoleBlockEntity.SLOT_BATTERY, 26, 72));
        this.addSlot(new SlotNonRetarded(be, MachinePAQuadrupoleBlockEntity.SLOT_COIL, 71, 36));

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
            if(!this.moveItemStackTo(stack, MachinePAQuadrupoleBlockEntity.SLOT_BATTERY, MachinePAQuadrupoleBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof PACoilItem) {
            if(!this.moveItemStackTo(stack, MachinePAQuadrupoleBlockEntity.SLOT_COIL, MachinePAQuadrupoleBlockEntity.SLOT_COIL + 1, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
