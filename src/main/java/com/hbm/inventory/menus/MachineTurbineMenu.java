package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineTurbineBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineTurbine.
 * Slot-Positionen unveraendert.
 */
public class MachineTurbineMenu extends MenuBase<MachineTurbineBlockEntity> {

    private static final int SLOT_COUNT = 7;

    public MachineTurbineMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineTurbineBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineTurbineMenu(int id, Inventory inventory, MachineTurbineBlockEntity be) {
        super(NtmMenuTypes.MACHINE_TURBINE.get(), id, be);

        // Fluid-Kennung
        this.addSlot(new SlotNonRetarded(be, MachineTurbineBlockEntity.SLOT_ID_IN, 8, 17));
        this.addSlot(new SlotTakeOnly(be, MachineTurbineBlockEntity.SLOT_ID_OUT, 8, 53));
        // Eingang befuellen
        this.addSlot(new SlotNonRetarded(be, MachineTurbineBlockEntity.SLOT_INPUT_LOAD, 44, 17));
        this.addSlot(new SlotTakeOnly(be, MachineTurbineBlockEntity.SLOT_INPUT_UNLOAD, 44, 53));
        // Batterie
        this.addSlot(new SlotNonRetarded(be, MachineTurbineBlockEntity.SLOT_BATTERY, 98, 53));
        // Ausgang abfuellen
        this.addSlot(new SlotNonRetarded(be, MachineTurbineBlockEntity.SLOT_OUTPUT_LOAD, 152, 17));
        this.addSlot(new SlotTakeOnly(be, MachineTurbineBlockEntity.SLOT_OUTPUT_UNLOAD, 152, 53));

        this.playerInv(inventory, 8, 84, 142);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index < SLOT_COUNT) {
                if(!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // Reihenfolge wie im Original: Batterie, Eingang, Ausgang, Kennung
                if(!this.moveItemStackTo(stack, 4, 5, false)) {
                    if(!this.moveItemStackTo(stack, 2, 3, false)) {
                        if(!this.moveItemStackTo(stack, 5, 6, false)) {
                            if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
                        }
                    }
                }
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
