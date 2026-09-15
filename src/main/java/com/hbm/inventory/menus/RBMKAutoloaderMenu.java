package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKAutoloaderBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKAutoloader.
 * Links die neun Faecher fuer frische Staebe, rechts die neun Ausgabefaecher.
 */
public class RBMKAutoloaderMenu extends MenuBase<RBMKAutoloaderBlockEntity> {

    public RBMKAutoloaderMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKAutoloaderBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKAutoloaderMenu(int id, Inventory inventory, RBMKAutoloaderBlockEntity be) {
        super(NtmMenuTypes.RBMK_AUTOLOADER.get(), id, be);

        this.addSlots(be, 0, 17, 18, 3, 3);
        this.addTakeOnlySlots(be, 9, 107, 18, 3, 3);

        this.playerInv(inventory, 8, 100);
    }
}
