package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineFELBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.FelCrystalItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFEL.
 * Zwei Faecher: die Batterie unten rechts, der Kristall oben.
 */
public class MachineFELMenu extends MenuBase<MachineFELBlockEntity> {

    public MachineFELMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineFELBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineFELMenu(int id, Inventory inventory, MachineFELBlockEntity be) {
        super(NtmMenuTypes.MACHINE_FEL.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineFELBlockEntity.SLOT_BATTERY, 182, 144));
        this.addSlot(new SlotNonRetarded(be, MachineFELBlockEntity.SLOT_CRYSTAL, 141, 23));

        this.playerInv(inventory, 8, 83, 141);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < 2) {
            if(!this.moveItemStackTo(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof FelCrystalItem) {
            if(!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }

    /** Der Schalter im Fenster: er legt den Strahl um. */
    @Override
    public boolean clickMenuButton(Player player, int id) {

        if(id != 0) return false;

        this.be.toggle();
        return true;
    }
}
