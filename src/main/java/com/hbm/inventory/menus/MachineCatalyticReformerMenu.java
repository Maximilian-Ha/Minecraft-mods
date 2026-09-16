package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.oil.MachineCatalyticReformerBlockEntity;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineCatalyticReformer.
 * Alle Platzkoordinaten sind unveraendert uebernommen.
 */
public class MachineCatalyticReformerMenu extends MenuBase<MachineCatalyticReformerBlockEntity> {

    private static final int LAST_MACHINE_SLOT = 10;

    public MachineCatalyticReformerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCatalyticReformerBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCatalyticReformerMenu(int id, Inventory inventory, MachineCatalyticReformerBlockEntity be) {
        super(NtmMenuTypes.MACHINE_CATALYTIC_REFORMER.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 17, 90));
        // Eingabe: das Oel, das umgebaut wird
        this.addSlot(new SlotNonRetarded(be, 1, 35, 90));
        this.addSlot(new SlotTakeOnly(be, 2, 35, 108));
        // Reformat
        this.addSlot(new SlotNonRetarded(be, 3, 107, 90));
        this.addSlot(new SlotTakeOnly(be, 4, 107, 108));
        // Nebengas
        this.addSlot(new SlotNonRetarded(be, 5, 125, 90));
        this.addSlot(new SlotTakeOnly(be, 6, 125, 108));
        // Wasserstoff
        this.addSlot(new SlotNonRetarded(be, 7, 143, 90));
        this.addSlot(new SlotTakeOnly(be, 8, 143, 108));
        // Fluidkennzeichner
        this.addSlot(new SlotNonRetarded(be, 9, 17, 108));
        // Katalysator
        this.addSlot(new SlotNonRetarded(be, 10, 71, 36));

        this.playerInv(inventory, 8, 156, 214);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index <= LAST_MACHINE_SLOT) {
            if(!this.moveItemStackTo(stack, LAST_MACHINE_SLOT + 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if(stack.getItem() instanceof IBatteryItem) {
                if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                if(!this.moveItemStackTo(stack, 9, 10, false)) return ItemStack.EMPTY;
            } else if(stack.getItem() == NtmItems.CATALYTIC_CONVERTER.get()) {
                if(!this.moveItemStackTo(stack, 10, 11, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[0].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[1].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[2].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 5, 6, false)) return ItemStack.EMPTY;
            } else if(!FluidContainerRegistry.getFullContainer(stack, this.be.tanks[3].getTankType()).isEmpty()) {
                if(!this.moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
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
