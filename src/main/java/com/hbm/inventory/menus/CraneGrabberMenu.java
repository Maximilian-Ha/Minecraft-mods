package com.hbm.inventory.menus;

import com.hbm.blockentity.network.CraneGrabberBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotPattern;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCraneGrabber.
 *
 * Nur neun Musterfaecher -- der Greifer haelt nichts fest. Alle Koordinaten sind die des
 * Originals; die beiden Aufwertungsplaetze bei 121,23 und 121,47 bleiben leer, weil es die
 * Aufwertungen im Port nicht gibt.
 */
public class CraneGrabberMenu extends FilterMenuBase<CraneGrabberBlockEntity> {

    public CraneGrabberMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (CraneGrabberBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public CraneGrabberMenu(int id, Inventory inventory, CraneGrabberBlockEntity be) {
        super(NtmMenuTypes.CRANE_GRABBER.get(), id, be);

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 3; col++) {
                this.addSlot(new SlotPattern(be, col + row * 3, 40 + col * 18, 17 + row * 18));
            }
        }

        this.playerInv(inventory, 8, 103, 161);
    }
}
