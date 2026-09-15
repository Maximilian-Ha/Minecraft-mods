package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineExcavatorBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineExcavator.
 *
 * Eine breite Oberflaeche: links das Bedienfeld mit den fuenf Schaltern, rechts der
 * Zwischenspeicher aus neun Faechern.
 */
public class MachineExcavatorMenu extends MenuBase<MachineExcavatorBlockEntity> {

    public MachineExcavatorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineExcavatorBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineExcavatorMenu(int id, Inventory inventory, MachineExcavatorBlockEntity be) {
        super(NtmMenuTypes.EXCAVATOR.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 220, 72));
        // Fluidkennung
        this.addSlot(new SlotNonRetarded(be, 1, 202, 72));
        // Aufwertungen und Bohrkopf
        this.addSlots(be, 2, 136, 75, 1, 3);
        // Zwischenspeicher: reine Entnahme
        this.addTakeOnlySlots(be, 5, 136, 5, 3, 3);

        this.playerInv(inventory, 41, 122);
    }
}
