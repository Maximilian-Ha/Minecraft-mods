package com.hbm.inventory.menus;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.blockentity.machine.MachineMiningLaserBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMiningLaser.
 *
 * Acht Aufwertungsfaecher oben rechts, einundzwanzig Ausgabefaecher in drei Reihen zu sieben,
 * die Batterie links unten.
 */
public class MachineMiningLaserMenu extends MenuBase<MachineMiningLaserBlockEntity> {

    public MachineMiningLaserMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineMiningLaserBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineMiningLaserMenu(int id, Inventory inventory, MachineMiningLaserBlockEntity be) {
        super(NtmMenuTypes.MACHINE_MINING_LASER.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineMiningLaserBlockEntity.SLOT_BATTERY, 8, 108));

        for(int row = 0; row < 2; row++) {
            for(int col = 0; col < 4; col++) {
                this.addSlot(new SlotNonRetarded(be, MachineMiningLaserBlockEntity.SLOT_UPGRADE_START + row * 4 + col,
                        98 + col * 18, 18 + row * 18));
            }
        }

        this.addTakeOnlySlots(be, MachineMiningLaserBlockEntity.SLOT_OUTPUT_START, 44, 72, 3, 7);

        this.playerInv(inventory, 8, 140, 198);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(!slot.hasItem()) return ret;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if(index < MachineMiningLaserBlockEntity.SLOTS) {
            if(!this.moveItemStackTo(stack, MachineMiningLaserBlockEntity.SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof IBatteryItem) {
            if(!this.moveItemStackTo(stack, MachineMiningLaserBlockEntity.SLOT_BATTERY,
                    MachineMiningLaserBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof MachineUpgradeItem) {
            if(!this.moveItemStackTo(stack, MachineMiningLaserBlockEntity.SLOT_UPGRADE_START,
                    MachineMiningLaserBlockEntity.SLOT_UPGRADE_END + 1, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return ret;
    }
}
