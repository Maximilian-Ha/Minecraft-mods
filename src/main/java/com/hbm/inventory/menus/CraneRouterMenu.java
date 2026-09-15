package com.hbm.inventory.menus;

import com.hbm.blockentity.network.CraneRouterBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotPattern;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCraneRouter.
 *
 * Dreissig Musterfaecher: zwei Saeulen zu je drei Reihen zu fuenf. Jede Reihe gehoert zu einer
 * Seite des Verteilers. Alle Koordinaten sind die des Originals.
 */
public class CraneRouterMenu extends FilterMenuBase<CraneRouterBlockEntity> {

    public CraneRouterMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (CraneRouterBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public CraneRouterMenu(int id, Inventory inventory, CraneRouterBlockEntity be) {
        super(NtmMenuTypes.CRANE_ROUTER.get(), id, be);

        for(int col = 0; col < 2; col++) {
            for(int row = 0; row < 3; row++) {
                for(int k = 0; k < CraneRouterBlockEntity.PATTERNS_PER_SIDE; k++) {
                    this.addSlot(new SlotPattern(be, k + col * 15 + row * 5, 34 + k * 18 + col * 98, 17 + row * 26));
                }
            }
        }

        this.playerInv(inventory, 47, 119, 177);
    }
}
