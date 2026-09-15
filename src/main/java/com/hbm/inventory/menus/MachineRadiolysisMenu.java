package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRadiolysisBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRadiolysis.
 *
 * Die Faecher der Entkeimung fehlen; die Begruendung steht im BlockEntity. Dadurch rutscht die
 * Batterie von Fach 14 auf Fach 12.
 */
public class MachineRadiolysisMenu extends MenuBase<MachineRadiolysisBlockEntity> {

    public MachineRadiolysisMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRadiolysisBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineRadiolysisMenu(int id, Inventory inventory, MachineRadiolysisBlockEntity be) {
        super(NtmMenuTypes.MACHINE_RADIOLYSIS.get(), id, be);

        /* Zehn Pelletfaecher, zwei Spalten zu fuenf -- durchgezaehlt wird spaltenweise. */
        for(int col = 0; col < 2; col++) {
            for(int row = 0; row < 5; row++) {
                this.addSlot(new SlotNonRetarded(be, row + col * 5, 188 + col * 18, 8 + row * 18));
            }
        }

        this.addSlot(new SlotNonRetarded(be, 10, 34, 17));
        this.addTakeOnlySlots(be, 11, 34, 53, 1, 1);
        this.addSlot(new SlotNonRetarded(be, 12, 8, 53));

        this.playerInv(inventory, 8, 84, 142);
    }
}
