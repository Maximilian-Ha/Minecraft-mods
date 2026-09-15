package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRadGenBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineRadGen.
 *
 * Links die zwoelf Eingaben in vier Reihen zu drei, rechts die zwoelf Ausgaben genauso. Dazwischen
 * die zwoelf Balken, einer je Bahn.
 */
public class MachineRadGenMenu extends MenuBase<MachineRadGenBlockEntity> {

    public MachineRadGenMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRadGenBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineRadGenMenu(int id, Inventory inventory, MachineRadGenBlockEntity be) {
        super(NtmMenuTypes.MACHINE_RAD_GEN.get(), id, be);

        this.addSlots(be, 0, 8, 17, 4, 3);
        this.addOutputSlots(inventory.player, be, MachineRadGenBlockEntity.LANES, 116, 17, 4, 3);

        this.playerInv(inventory, 8, 102);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < MachineRadGenBlockEntity.SLOTS) {
            if(!this.moveItemStackTo(stack, MachineRadGenBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else {
            /* Brennstoff geht auf die zwoelf Bahnen; die Fachpruefung sorgt dafuer, dass sie
             * sich gleichmaessig fuellen. */
            if(!this.moveItemStackTo(stack, 0, MachineRadGenBlockEntity.LANES, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
