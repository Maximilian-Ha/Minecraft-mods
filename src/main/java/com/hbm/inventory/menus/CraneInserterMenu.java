package com.hbm.inventory.menus;

import com.hbm.blockentity.network.CraneInserterBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCraneInserter.
 *
 * Einundzwanzig Faecher in drei Reihen zu sieben -- der Zwischenspeicher fuer alles, was die
 * Maschine an der Ausgangsseite gerade nicht annimmt. Die Fachlage ist die des Originals.
 */
public class CraneInserterMenu extends MenuBase<CraneInserterBlockEntity> {

    public CraneInserterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (CraneInserterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public CraneInserterMenu(int id, Inventory inventory, CraneInserterBlockEntity be) {
        super(NtmMenuTypes.CRANE_INSERTER.get(), id, be);

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 7; col++) {
                this.addSlot(new SlotNonRetarded(be, col + row * 7, 26 + col * 18, 17 + row * 18));
            }
        }

        this.playerInv(inventory, 8, 84, 142);
    }
}
