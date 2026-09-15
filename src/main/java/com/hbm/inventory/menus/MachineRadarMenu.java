package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRadarBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachineRadarNT.
 *
 * Die zehn Faecher des Radars: acht Koordinatenspeicher in einer Reihe, darunter links der
 * Radar-Verbinder und rechts die Batterie.
 *
 * ES GAB SIE IM PORT BIS RUNDE 126 NICHT. Das Radar oeffnete nur die Karte, seine Faecher waren
 * damit ausschliesslich ueber Trichter erreichbar -- Batterie eingeschlossen. Aufgefallen war
 * das schon in Runde 120 und steht seither in der Roadmap; mit dem Verbinder wurde es
 * dringend, denn der gehoert in das neunte Fach.
 */
public class MachineRadarMenu extends MenuBase<MachineRadarBlockEntity> {

    public MachineRadarMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRadarBlockEntity) inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public MachineRadarMenu(int id, Inventory inventory, MachineRadarBlockEntity be) {
        super(NtmMenuTypes.RADAR.get(), id, be);

        for(int i = 0; i < 8; i++) this.addSlot(new SlotNonRetarded(be, i, 26 + i * 18, 17));

        this.addSlot(new SlotNonRetarded(be, 8, 26, 44));
        this.addSlot(new SlotNonRetarded(be, 9, 152, 44));

        this.playerInv(inventory, 8, 103);
    }
}
