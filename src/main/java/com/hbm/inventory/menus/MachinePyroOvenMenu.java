package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.oil.MachinePyroOvenBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerPyroOven.
 * Alle Platzkoordinaten sind unveraendert uebernommen.
 */
public class MachinePyroOvenMenu extends MenuBase<MachinePyroOvenBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 5;

    public MachinePyroOvenMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePyroOvenBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePyroOvenMenu(int id, Inventory inventory, MachinePyroOvenBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PYRO_OVEN.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachinePyroOvenBlockEntity.SLOT_BATTERY, 152, 72));
        this.addSlot(new SlotNonRetarded(be, MachinePyroOvenBlockEntity.SLOT_INPUT, 35, 45));
        this.addSlot(new SlotTakeOnly(be, MachinePyroOvenBlockEntity.SLOT_OUTPUT, 89, 45));
        this.addSlot(new SlotNonRetarded(be, MachinePyroOvenBlockEntity.SLOT_FLUID_ID, 8, 72));
        this.addSlot(new SlotNonRetarded(be, MachinePyroOvenBlockEntity.SLOT_UPGRADE_START, 71, 72));
        this.addSlot(new SlotNonRetarded(be, MachinePyroOvenBlockEntity.SLOT_UPGRADE_END, 89, 72));

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
                if(!this.moveItemStackTo(stack, MachinePyroOvenBlockEntity.SLOT_BATTERY, MachinePyroOvenBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                if(!this.moveItemStackTo(stack, MachinePyroOvenBlockEntity.SLOT_FLUID_ID, MachinePyroOvenBlockEntity.SLOT_FLUID_ID + 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof MachineUpgradeItem) {
                if(!this.moveItemStackTo(stack, MachinePyroOvenBlockEntity.SLOT_UPGRADE_START, MachinePyroOvenBlockEntity.SLOT_UPGRADE_END + 1, false)) return ItemStack.EMPTY;
            } else {
                if(!this.moveItemStackTo(stack, MachinePyroOvenBlockEntity.SLOT_INPUT, MachinePyroOvenBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
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
