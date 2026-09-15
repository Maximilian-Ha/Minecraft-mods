package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKStorageBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKStorage.
 * Die zwoelf Faecher liegen spaltenweise, genau wie im Original.
 */
public class RBMKStorageMenu extends MenuBase<RBMKStorageBlockEntity> {

    public RBMKStorageMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKStorageBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKStorageMenu(int id, Inventory inventory, RBMKStorageBlockEntity be) {
        super(NtmMenuTypes.RBMK_STORAGE.get(), id, be);

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 4; col++) {
                this.addSlot(new SlotNonRetarded(be, row + col * 3, 32 + 32 * col, 29 + 16 * row));
            }
        }

        this.playerInv(inventory, 8, 104, 162);
    }
}
