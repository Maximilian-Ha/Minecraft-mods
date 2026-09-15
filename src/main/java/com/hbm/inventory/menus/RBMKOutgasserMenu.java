package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKOutgasserBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKOutgasser.
 * Ein Fach fuer das Ausgangsmaterial, eines fuer das Ergebnis.
 */
public class RBMKOutgasserMenu extends MenuBase<RBMKOutgasserBlockEntity> {

    public RBMKOutgasserMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKOutgasserBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKOutgasserMenu(int id, Inventory inventory, RBMKOutgasserBlockEntity be) {
        super(NtmMenuTypes.RBMK_OUTGASSER.get(), id, be);

        this.addSlots(be, 0, 48, 45, 1, 1);
        this.addOutputSlots(inventory.player, be, 1, 112, 69, 1, 1);
        this.playerInv(inventory, 8, 104, 162);
    }
}
