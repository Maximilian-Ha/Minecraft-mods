package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineCombustionEngineBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.PistonsItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCombustionEngine.
 * Slot-Positionen unveraendert aus dem Original.
 */
public class MachineCombustionEngineMenu extends MenuBase<MachineCombustionEngineBlockEntity> {

    private static final int SLOT_COUNT = 5;

    public MachineCombustionEngineMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCombustionEngineBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCombustionEngineMenu(int id, Inventory inventory, MachineCombustionEngineBlockEntity be) {
        super(NtmMenuTypes.MACHINE_COMBUSTION_ENGINE.get(), id, be);

        this.be.startOpen(inventory.player);

        this.addSlot(new SlotNonRetarded(be, MachineCombustionEngineBlockEntity.SLOT_FLUID_IN, 17, 17));
        this.addSlot(new SlotCraftingOutput(inventory.player, be, MachineCombustionEngineBlockEntity.SLOT_FLUID_OUT, 17, 53));
        this.addSlot(new SlotNonRetarded(be, MachineCombustionEngineBlockEntity.SLOT_PISTONS, 88, 71));
        this.addSlot(new SlotNonRetarded(be, MachineCombustionEngineBlockEntity.SLOT_BATTERY, 143, 71));
        this.addSlot(new SlotNonRetarded(be, MachineCombustionEngineBlockEntity.SLOT_IDENTIFIER, 35, 71));

        this.playerInv(inventory, 8, 121, 179);
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
                if(stack.getItem() instanceof IBatteryItem) {
                    if(!this.moveItemStackTo(stack, MachineCombustionEngineBlockEntity.SLOT_BATTERY, MachineCombustionEngineBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
                } else if(stack.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.moveItemStackTo(stack, MachineCombustionEngineBlockEntity.SLOT_IDENTIFIER, MachineCombustionEngineBlockEntity.SLOT_IDENTIFIER + 1, false)) return ItemStack.EMPTY;
                } else if(stack.getItem() instanceof PistonsItem) {
                    if(!this.moveItemStackTo(stack, MachineCombustionEngineBlockEntity.SLOT_PISTONS, MachineCombustionEngineBlockEntity.SLOT_PISTONS + 1, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.moveItemStackTo(stack, MachineCombustionEngineBlockEntity.SLOT_FLUID_IN, MachineCombustionEngineBlockEntity.SLOT_FLUID_IN + 1, false)) return ItemStack.EMPTY;
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.be.stopOpen(player);
    }
}
