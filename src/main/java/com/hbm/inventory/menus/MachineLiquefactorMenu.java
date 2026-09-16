package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.oil.MachineLiquefactorBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerLiquefactor.
 * Alle Platzkoordinaten sind unveraendert uebernommen.
 */
public class MachineLiquefactorMenu extends MenuBase<MachineLiquefactorBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 3;

    public MachineLiquefactorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineLiquefactorBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineLiquefactorMenu(int id, Inventory inventory, MachineLiquefactorBlockEntity be) {
        super(NtmMenuTypes.MACHINE_LIQUEFACTOR.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineLiquefactorBlockEntity.SLOT_INPUT, 35, 54));
        this.addSlot(new SlotNonRetarded(be, MachineLiquefactorBlockEntity.SLOT_BATTERY, 134, 72));
        this.addSlot(new SlotNonRetarded(be, MachineLiquefactorBlockEntity.SLOT_UPGRADE_START, 98, 36));
        this.addSlot(new SlotNonRetarded(be, MachineLiquefactorBlockEntity.SLOT_UPGRADE_END, 98, 54));

        this.playerInv(inventory, 8, 122, 180);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret;
        Slot slot = this.slots.get(index);

        if(!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index <= LAST_MACHINE_SLOT) {
            if(!this.moveItemStackTo(stack, LAST_MACHINE_SLOT + 1, this.slots.size(), true)) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        } else {
            if(stack.getItem() instanceof IBatteryItem) {
                if(!this.moveItemStackTo(stack, MachineLiquefactorBlockEntity.SLOT_BATTERY, MachineLiquefactorBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof MachineUpgradeItem) {
                if(!this.moveItemStackTo(stack, MachineLiquefactorBlockEntity.SLOT_UPGRADE_START, MachineLiquefactorBlockEntity.SLOT_UPGRADE_END + 1, false)) return ItemStack.EMPTY;
            } else {
                if(!this.moveItemStackTo(stack, MachineLiquefactorBlockEntity.SLOT_INPUT, MachineLiquefactorBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
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
