package com.hbm.inventory.menus;

import com.hbm.blockentity.network.CraneUnboxerBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCraneUnboxer.
 *
 * Drei Reihen zu sieben; alle Koordinaten sind die des Originals. Die beiden
 * Aufwertungsplaetze bei 152,23 und 152,47 bleiben leer, weil es die Aufwertungen im Port
 * nicht gibt.
 */
public class CraneUnboxerMenu extends MenuBase<CraneUnboxerBlockEntity> {

    public CraneUnboxerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (CraneUnboxerBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public CraneUnboxerMenu(int id, Inventory inventory, CraneUnboxerBlockEntity be) {
        super(NtmMenuTypes.CRANE_UNBOXER.get(), id, be);

        this.addSlots(be, 0, 8, 17, 3, 7);
        this.playerInv(inventory, 8, 103, 161);
    }
}
