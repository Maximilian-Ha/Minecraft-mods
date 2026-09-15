package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineTapeDriveBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MachineTapeDriveMenu extends MenuBase<MachineTapeDriveBlockEntity> {

    public MachineTapeDriveMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineTapeDriveBlockEntity) inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public MachineTapeDriveMenu(int id, Inventory inventory, MachineTapeDriveBlockEntity be) {
        super(NtmMenuTypes.TAPE_DRIVE.get(), id, be);

        /* Zwei Reihen zu sechs, wie im Original. */
        this.addSlots(be, 0, 35, 27, 2, 6);

        this.playerInv(inventory, 8, 104);
    }
}
