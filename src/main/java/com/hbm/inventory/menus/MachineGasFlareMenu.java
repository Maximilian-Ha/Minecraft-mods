package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.oil.MachineGasFlareBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineGasFlare.
 * Alle Platzkoordinaten sind unveraendert uebernommen, samt des Versatzes von 37 Pixeln,
 * mit dem das Original das Spielerinventar nach unten schiebt.
 */
public class MachineGasFlareMenu extends MenuBase<MachineGasFlareBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 5;

    public MachineGasFlareMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineGasFlareBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineGasFlareMenu(int id, Inventory inventory, MachineGasFlareBlockEntity be) {
        super(NtmMenuTypes.MACHINE_GAS_FLARE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineGasFlareBlockEntity.SLOT_BATTERY, 143, 71));
        this.addSlot(new SlotNonRetarded(be, MachineGasFlareBlockEntity.SLOT_FLUID_IN, 17, 17));
        this.addSlot(new SlotTakeOnly(be, MachineGasFlareBlockEntity.SLOT_FLUID_OUT, 17, 53));
        this.addSlot(new SlotNonRetarded(be, MachineGasFlareBlockEntity.SLOT_FLUID_ID, 35, 71));
        this.addSlot(new SlotNonRetarded(be, MachineGasFlareBlockEntity.SLOT_UPGRADE_START, 80, 71));
        this.addSlot(new SlotNonRetarded(be, MachineGasFlareBlockEntity.SLOT_UPGRADE_END, 98, 71));

        this.playerInv(inventory, 8, 121, 179);
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
                if(!this.moveItemStackTo(stack, MachineGasFlareBlockEntity.SLOT_BATTERY, MachineGasFlareBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                if(!this.moveItemStackTo(stack, MachineGasFlareBlockEntity.SLOT_FLUID_ID, MachineGasFlareBlockEntity.SLOT_FLUID_ID + 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof MachineUpgradeItem) {
                if(!this.moveItemStackTo(stack, MachineGasFlareBlockEntity.SLOT_UPGRADE_START, MachineGasFlareBlockEntity.SLOT_UPGRADE_END + 1, false)) return ItemStack.EMPTY;
            } else {
                if(!this.moveItemStackTo(stack, MachineGasFlareBlockEntity.SLOT_FLUID_IN, MachineGasFlareBlockEntity.SLOT_FLUID_IN + 1, false)) return ItemStack.EMPTY;
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
