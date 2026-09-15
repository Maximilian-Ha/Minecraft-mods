package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineCyclotronBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineCyclotron.
 *
 * Links die drei Geschosse, in der Mitte die drei Ziele, rechts die drei Ausgaben -- Zeile fuer
 * Zeile eine Bahn. Darunter Aufwertungen und Batterie.
 */
public class MachineCyclotronMenu extends MenuBase<MachineCyclotronBlockEntity> {

    public MachineCyclotronMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineCyclotronBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineCyclotronMenu(int id, Inventory inventory, MachineCyclotronBlockEntity be) {
        super(NtmMenuTypes.MACHINE_CYCLOTRON.get(), id, be);

        for(int i = 0; i < MachineCyclotronBlockEntity.LANES; i++) {
            this.addSlot(new SlotNonRetarded(be, MachineCyclotronBlockEntity.SLOT_PARTICLE + i, 11, 18 + i * 18));
        }
        for(int i = 0; i < MachineCyclotronBlockEntity.LANES; i++) {
            this.addSlot(new SlotNonRetarded(be, MachineCyclotronBlockEntity.SLOT_INGREDIENT + i, 101, 18 + i * 18));
        }

        this.addTakeOnlySlots(be, MachineCyclotronBlockEntity.SLOT_OUTPUT, 131, 18, MachineCyclotronBlockEntity.LANES, 1);

        this.addSlot(new SlotNonRetarded(be, MachineCyclotronBlockEntity.SLOT_BATTERY, 168, 83));
        this.addSlot(new SlotNonRetarded(be, MachineCyclotronBlockEntity.SLOT_UPGRADE_START, 60, 81));
        this.addSlot(new SlotNonRetarded(be, MachineCyclotronBlockEntity.SLOT_UPGRADE_END, 78, 81));

        this.playerInv(inventory, 15, 133, 191);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < MachineCyclotronBlockEntity.SLOTS) {
            if(!this.moveItemStackTo(stack, MachineCyclotronBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, 9, 10, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof MachineUpgradeItem) {
            if(!this.moveItemStackTo(stack, 10, 12, false)) return ItemStack.EMPTY;

        } else if(CyclotronRecipes.isParticle(stack)) {
            if(!this.moveItemStackTo(stack, 0, 3, false)) return ItemStack.EMPTY;

        } else if(CyclotronRecipes.isIngredient(stack)) {
            if(!this.moveItemStackTo(stack, 3, 6, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
