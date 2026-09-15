package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineStrandCasterBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineStrandCaster.
 * Alle Fachkoordinaten sind die des Originals.
 */
public class MachineStrandCasterMenu extends MenuBase<MachineStrandCasterBlockEntity> {

    public MachineStrandCasterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineStrandCasterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineStrandCasterMenu(int id, Inventory inventory, MachineStrandCasterBlockEntity be) {
        super(NtmMenuTypes.MACHINE_STRAND_CASTER.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineStrandCasterBlockEntity.SLOT_MOLD, 57, 62));

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 2; col++) {
                this.addSlot(new SlotCraftingOutput(inventory.player, be,
                        col + row * 2 + MachineStrandCasterBlockEntity.SLOT_OUTPUT_START, 125 + col * 18, 26 + row * 18));
            }
        }

        this.playerInv(inventory, 8, 132, 190);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < MachineStrandCasterBlockEntity.SLOTS) {

                if(!this.moveItemStackTo(stack, MachineStrandCasterBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

            } else {

                /* Aus dem Rucksack geht nur die Form hinein; die Ausgabefaecher fuellt die
                 * Maschine. */
                if(!this.moveItemStackTo(stack, MachineStrandCasterBlockEntity.SLOT_MOLD, MachineStrandCasterBlockEntity.SLOT_MOLD + 1, false)) return ItemStack.EMPTY;
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
