package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRTGBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineRTG.
 * Slot-Positionen unveraendert: drei Reihen zu fuenf Pelletslots.
 *
 * Das Original hielt ueber addCraftingToCrafters/detectAndSendChanges die Waerme
 * synchron; im Port kommt die Waerme mit dem BufPacket des BlockEntity, deshalb
 * entfaellt der Fortschrittsleisten-Sync hier ersatzlos.
 *
 * transferStackInSlot aus dem Original verschiebt genau zwischen den ersten 15 Slots
 * und dem Spielerinventar -- das macht MenuBase.quickMoveStack bereits so, ein
 * eigener Ueberschreiber ist nicht noetig.
 */
public class MachineRTGMenu extends MenuBase<MachineRTGBlockEntity> {

    public MachineRTGMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRTGBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineRTGMenu(int id, Inventory inventory, MachineRTGBlockEntity be) {
        super(NtmMenuTypes.MACHINE_RTG.get(), id, be);

        this.addSlots(be, 0, 16, 18, 3, 5);

        this.playerInv(inventory, 8, 106, 164);
    }
}
