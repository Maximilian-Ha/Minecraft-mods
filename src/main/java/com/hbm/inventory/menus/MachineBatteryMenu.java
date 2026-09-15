package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.storage.MachineBatteryBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineBattery.
 * Slot-Positionen und Reihenfolge wie im Original; das Verschieben per
 * Umschalt-Klick macht MenuBase bereits genauso wie die Vorlage.
 */
public class MachineBatteryMenu extends MenuBase<MachineBatteryBlockEntity> {

    public MachineBatteryMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineBatteryBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineBatteryMenu(int id, Inventory inventory, MachineBatteryBlockEntity be) {
        super(NtmMenuTypes.MACHINE_BATTERY.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, MachineBatteryBlockEntity.SLOT_DISCHARGE, 26, 17));
        this.addSlot(new SlotNonRetarded(be, MachineBatteryBlockEntity.SLOT_CHARGE, 26, 53));

        this.playerInv(inventory, 8, 84, 142);
    }
}
