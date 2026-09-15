package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineSirenBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MachineSirenMenu extends MenuBase<MachineSirenBlockEntity> {

    public MachineSirenMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineSirenBlockEntity) inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public MachineSirenMenu(int id, Inventory inventory, MachineSirenBlockEntity be) {
        super(NtmMenuTypes.SIREN.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, 0, 8, 35));

        this.playerInv(inventory, 8, 84);
    }
}
