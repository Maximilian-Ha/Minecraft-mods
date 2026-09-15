package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.albion.MachinePARFCBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPARFC.
 *
 * Die Kavitaet hat nur ein Fach: die Batterie. Alles andere kommt aus der Leitung.
 */
public class MachinePARFCMenu extends MenuBase<MachinePARFCBlockEntity> {

    public static final int SLOTS = 1;

    public MachinePARFCMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePARFCBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePARFCMenu(int id, Inventory inventory, MachinePARFCBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PA_RFC.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePARFCBlockEntity.SLOT_BATTERY, 53, 72));

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
            if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
