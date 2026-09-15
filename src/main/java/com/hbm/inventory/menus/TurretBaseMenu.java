package com.hbm.inventory.menus;

import com.hbm.blockentity.turret.TurretBaseBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.NtmItems;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerTurretBase.
 *
 * Oben links das Fach fuer den Zielchip, in der Mitte die neun Munitionsfaecher, rechts unten die
 * Batterie. Beim Hineinschieben landet ein Zielchip im Chipfach, alles andere in der Munition.
 */
public class TurretBaseMenu extends MenuBase<TurretBaseBlockEntity> {

    public TurretBaseMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (TurretBaseBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public TurretBaseMenu(int id, Inventory inventory, TurretBaseBlockEntity be) {
        super(NtmMenuTypes.TURRET_BASE.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, TurretBaseBlockEntity.SLOT_CHIP, 98, 27));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlot(new SlotNonRetarded(be, 1 + i * 3 + j, 80 + j * 18, 63 + i * 18));
            }
        }

        this.addSlot(new SlotNonRetarded(be, TurretBaseBlockEntity.SLOT_BATTERY, 152, 99));

        this.playerInv(inventory, 8, 140);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        newStack = stack.copy();

        if(index <= be.getContainerSize() - 1) {

            if(!this.moveItemStackTo(stack, be.getContainerSize(), this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() == NtmItems.TURRET_CHIP.get()) {

            if(!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;

        } else if(!this.moveItemStackTo(stack, 1, be.getContainerSize(), false)) {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        return newStack;
    }
}
