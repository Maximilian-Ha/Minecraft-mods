package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineArcFurnaceLargeBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.items.machine.ArcElectrodeItem;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineArcFurnaceLarge.
 * Alle Fachkoordinaten sind unveraendert uebernommen.
 */
public class MachineArcFurnaceLargeMenu extends MenuBase<MachineArcFurnaceLargeBlockEntity> {

    public MachineArcFurnaceLargeMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineArcFurnaceLargeBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineArcFurnaceLargeMenu(int id, Inventory inventory, MachineArcFurnaceLargeBlockEntity be) {
        super(NtmMenuTypes.MACHINE_ARC_FURNACE.get(), id, be);

        // Elektroden
        for(int i = 0; i < 3; i++) this.addSlot(new SlotNonRetarded(be, i, 62 + i * 18, 22));
        // Batterie
        this.addSlot(new SlotNonRetarded(be, 3, 8, 108));
        // Aufruestung
        this.addSlot(new SlotNonRetarded(be, 4, 152, 108));
        // Schmelzgut
        for(int i = 0; i < 4; i++) for(int j = 0; j < 5; j++) this.addSlot(new SlotArcFurnace(be, 5 + j + i * 5, 44 + j * 18, 54 + i * 18));
        // Ein- und Ausgabe
        for(int i = 0; i < 5; i++) this.addSlot(new SlotNonRetarded(be, i + 25, 44 + i * 18, 129));

        this.playerInv(inventory, 8, 174, 232);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem()) {

            ItemStack stack = slot.getItem();
            ret = stack.copy();

            if(index <= 29) {
                if(!this.moveItemStackTo(stack, 30, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if(stack.getItem() instanceof IBatteryItem) {
                    if(!this.moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
                } else if(stack.getItem() instanceof ArcElectrodeItem) {
                    if(!this.moveItemStackTo(stack, 0, 3, false)) return ItemStack.EMPTY;
                } else if(stack.getItem() instanceof MachineUpgradeItem) {
                    if(!this.moveItemStackTo(stack, 4, 5, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.moveItemStackTo(stack, 25, 30, false)) return ItemStack.EMPTY;
                }
            }

            if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }

        return ret;
    }

    /**
     * Das Schmelzgutfach nimmt nur an, was sich auch verarbeiten laesst, und hoechstens so viel,
     * wie eine Aufruestungsstufe zulaesst.
     */
    public static class SlotArcFurnace extends SlotNonRetarded {

        public SlotArcFurnace(Container container, int id, int x, int y) {
            super(container, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {

            MachineArcFurnaceLargeBlockEntity furnace = (MachineArcFurnaceLargeBlockEntity) this.container;
            if(furnace.liquidMode) return true;

            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(furnace.getLevel(), stack, furnace.liquidMode);
            if(recipe == null || recipe.solidOutput == null) return false;

            return recipe.solidOutput.getCount() * stack.getCount() <= recipe.solidOutput.getMaxStackSize() && stack.getCount() <= furnace.getMaxInputSize();
        }

        @Override
        public int getMaxStackSize() {
            MachineArcFurnaceLargeBlockEntity furnace = (MachineArcFurnaceLargeBlockEntity) this.container;
            return this.hasItem() ? furnace.getMaxInputSize() : 1;
        }
    }
}
