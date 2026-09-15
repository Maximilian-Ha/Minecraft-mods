package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKHeaterBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKHeater.
 * Ein Fach fuer die Fluessigkeitskennung, sonst nur das Inventar des Spielers.
 */
public class RBMKHeaterMenu extends MenuBase<RBMKHeaterBlockEntity> {

    public RBMKHeaterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKHeaterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKHeaterMenu(int id, Inventory inventory, RBMKHeaterBlockEntity be) {
        super(NtmMenuTypes.RBMK_HEATER.get(), id, be);

        this.addSlots(be, 0, 41, 45, 1, 1);
        this.playerInv(inventory, 8, 104, 162);
    }
}
