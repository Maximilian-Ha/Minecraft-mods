package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineSILEXBlockEntity;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
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
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerSILEX.
 *
 * Oben der Eingang, links darunter Fluidkennung und Behaelterwechsel, rechts unten die sechs
 * Faecher der Warteschlange. Das Ausgabefach dazwischen ist kein Lager: was darin steht, wandert
 * im naechsten Tick in die Warteschlange.
 */
public class MachineSILEXMenu extends MenuBase<MachineSILEXBlockEntity> {

    public MachineSILEXMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineSILEXBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineSILEXMenu(int id, Inventory inventory, MachineSILEXBlockEntity be) {
        super(NtmMenuTypes.MACHINE_SILEX.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineSILEXBlockEntity.SLOT_INPUT, 80, 12));

        this.addSlot(new SlotNonRetarded(be, MachineSILEXBlockEntity.SLOT_FLUID_ID, 8, 24));
        this.addSlot(new SlotNonRetarded(be, MachineSILEXBlockEntity.SLOT_CONTAINER_IN, 26, 24));
        this.addSlot(new SlotTakeOnly(be, MachineSILEXBlockEntity.SLOT_CONTAINER_OUT, 44, 24));

        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineSILEXBlockEntity.SLOT_OUTPUT, 116, 90));

        /* Die Warteschlange steht zweispaltig, drei Zeilen tief. */
        for(int i = 0; i < MachineSILEXBlockEntity.QUEUE_SIZE; i++) {
            int slot = MachineSILEXBlockEntity.SLOT_QUEUE + i;
            this.addSlot(new SlotCraftingOutput(inventory.player, be, slot, 134 + (i % 2) * 18, 72 + (i / 2) * 18));
        }

        this.playerInv(inventory, 8, 140, 198);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < MachineSILEXBlockEntity.SLOTS) {
            if(!this.moveItemStackTo(stack, MachineSILEXBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IItemFluidIdentifier) {
            if(!this.moveItemStackTo(stack, MachineSILEXBlockEntity.SLOT_FLUID_ID, MachineSILEXBlockEntity.SLOT_FLUID_ID + 1, false)) return ItemStack.EMPTY;

        } else if(FluidContainerRegistry.getFluidContent(stack, this.be.tank.getTankType()) > 0) {
            if(!this.moveItemStackTo(stack, MachineSILEXBlockEntity.SLOT_CONTAINER_IN, MachineSILEXBlockEntity.SLOT_CONTAINER_IN + 1, false)) return ItemStack.EMPTY;

        } else {
            if(!this.moveItemStackTo(stack, MachineSILEXBlockEntity.SLOT_INPUT, MachineSILEXBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }

    /** Der einzige Knopf im Fenster kippt die geloeste Ladung weg. */
    @Override
    public boolean clickMenuButton(Player player, int id) {

        if(id != 0) return false;

        this.be.voidContents();
        this.be.setChanged();
        return true;
    }
}
