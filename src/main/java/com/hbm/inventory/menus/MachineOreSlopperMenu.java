package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineOreSlopperBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Portiert aus 1.7.10: com.hbm.inventory.container.ContainerOreSlopper. */
public class MachineOreSlopperMenu extends MenuBase<MachineOreSlopperBlockEntity> {

    public MachineOreSlopperMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineOreSlopperBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineOreSlopperMenu(int id, Inventory inventory, MachineOreSlopperBlockEntity be) {
        super(NtmMenuTypes.ORE_SLOPPER.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 8, 72));
        // Fluidkennung
        this.addSlot(new SlotNonRetarded(be, 1, 26, 72));
        // Eingabe: die Rohprobe
        this.addSlot(new SlotNonRetarded(be, 2, 71, 27));
        // Ausgabe: sechs Faecher, zwei Spalten
        this.addOutputSlots(inventory.player, be, 3, 134, 18, 3, 2);
        // Aufwertungen
        this.addSlots(be, 9, 62, 72, 1, 2);

        this.playerInv(inventory, 8, 122);
    }
}
