package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineFunnelBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerFunnel.
 * Alle Fachkoordinaten sind die des Originals.
 *
 * Zwei Reihen zu neun: oben hinein, unten heraus, jedes Fach ueber seinem eigenen Ausgang.
 */
public class MachineFunnelMenu extends MenuBase<MachineFunnelBlockEntity> {

    public MachineFunnelMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineFunnelBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineFunnelMenu(int id, Inventory inventory, MachineFunnelBlockEntity be) {
        super(NtmMenuTypes.MACHINE_FUNNEL.get(), id, be);

        for(int i = 0; i < MachineFunnelBlockEntity.SLOTS_PER_ROW; i++) {
            this.addSlot(new SlotNonRetarded(be, i, 8 + 18 * i, 18));
        }

        for(int i = 0; i < MachineFunnelBlockEntity.SLOTS_PER_ROW; i++) {
            this.addSlot(new SlotCraftingOutput(inventory.player, be, i + MachineFunnelBlockEntity.SLOTS_PER_ROW, 8 + 18 * i, 54));
        }

        this.playerInv(inventory, 8, 86, 144);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < MachineFunnelBlockEntity.SLOTS) {

                if(!this.moveItemStackTo(stack, MachineFunnelBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

            } else {

                /* Aus dem Rucksack geht es nur in die Eingangsreihe -- die Ausgaenge fuellt die
                 * Maschine. */
                if(!this.moveItemStackTo(stack, 0, MachineFunnelBlockEntity.SLOTS_PER_ROW, false)) return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return ret;
    }
}
