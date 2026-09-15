package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineSuperComputerBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MachineSuperComputerMenu extends MenuBase<MachineSuperComputerBlockEntity> {

    public MachineSuperComputerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineSuperComputerBlockEntity) inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public MachineSuperComputerMenu(int id, Inventory inventory, MachineSuperComputerBlockEntity be) {
        super(NtmMenuTypes.SUPER_COMPUTER.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 152, 81));
        // Blaupause
        this.addSlot(new SlotNonRetarded(be, 1, 35, 80));
        // drei hinein
        this.addSlots(be, 2, 8, 27, 1, 3);
        // drei heraus
        this.addOutputSlots(inventory.player, be, 5, 80, 27, 1, 3);

        this.playerInv(inventory, 8, 129);
    }
}
