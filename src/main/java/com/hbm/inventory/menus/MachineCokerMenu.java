package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.oil.MachineCokerBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineCoker.
 * Beide Platzkoordinaten sind unveraendert uebernommen.
 */
public class MachineCokerMenu extends MenuBase<MachineCokerBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 1;

    public MachineCokerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCokerBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCokerMenu(int id, Inventory inventory, MachineCokerBlockEntity be) {
        super(NtmMenuTypes.MACHINE_COKER.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineCokerBlockEntity.SLOT_FLUID_ID, 35, 72));
        this.addSlot(new SlotTakeOnly(be, MachineCokerBlockEntity.SLOT_OUTPUT, 97, 27));

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
            if(!(stack.getItem() instanceof IItemFluidIdentifier)) return ItemStack.EMPTY;
            if(!this.moveItemStackTo(stack, MachineCokerBlockEntity.SLOT_FLUID_ID, MachineCokerBlockEntity.SLOT_FLUID_ID + 1, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return ret;
    }
}
