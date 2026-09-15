package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.WatzBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerWatz.
 *
 * Die vierundzwanzig Faecher liegen in einem abgerundeten Sechseck: aus dem Sechs-mal-sechs-Feld
 * fallen die vier Ecken heraus. Die Bedingung dafuer steht unveraendert aus dem Original hier --
 * sie zaehlt die Faecher in derselben Reihenfolge, und damit stimmen auch die Fachnummern.
 */
public class WatzMenu extends MenuBase<WatzBlockEntity> {

    public WatzMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (WatzBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level, extraData.readBlockPos()));
    }

    public WatzMenu(int id, Inventory inventory, WatzBlockEntity be) {
        super(NtmMenuTypes.WATZ.get(), id, be);

        int index = 0;
        for(int j = 0; j < 6; j++) {
            for(int i = 0; i < 6; i++) {
                if(i + j > 1 && i + j < 9 && 5 - i + j > 1 && i + 5 - j > 1) {
                    this.addSlot(new SlotNonRetarded(be, index, 17 + i * 18, 8 + j * 18));
                    index++;
                }
            }
        }

        this.playerInv(inventory, 8, 147);
    }
}
