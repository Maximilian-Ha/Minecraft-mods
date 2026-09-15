package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachinePrecAssBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachinePrecAss.
 *
 * Neun Eingabefaecher links, neun Ausgabefaecher rechts -- die Maschine schuettet beim
 * Einschmelzen von Ausschuss jede Zutat einzeln aus und braucht den Platz.
 */
public class MachinePrecAssMenu extends MenuBase<MachinePrecAssBlockEntity> {

    public MachinePrecAssMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePrecAssBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePrecAssMenu(int id, Inventory inventory, MachinePrecAssBlockEntity be) {
        super(NtmMenuTypes.PRECASS.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 152, 81));
        // Blaupause
        this.addSlot(new SlotNonRetarded(be, 1, 35, 126));
        // Aufwertungen
        this.addSlots(be, 2, 152, 108, 2, 1);
        // Eingabe
        this.addSlots(be, 4, 8, 27, 3, 3);
        // Ausgabe
        this.addOutputSlots(inventory.player, be, 13, 80, 27, 3, 3);

        this.playerInv(inventory, 8, 174);
    }
}
