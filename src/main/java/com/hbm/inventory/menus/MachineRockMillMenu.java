package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineRockMillBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MachineRockMillMenu extends MenuBase<MachineRockMillBlockEntity> {

    public MachineRockMillMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineRockMillBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachineRockMillMenu(int id, Inventory inventory, MachineRockMillBlockEntity be) {
        super(NtmMenuTypes.MACHINE_ROCK_MILL.get(), id, be);

        // Batterie
        this.addSlot(new SlotNonRetarded(be, 0, 152, 91));
        // Schaltplan
        this.addSlot(new SlotNonRetarded(be, 1, 35, 90));
        // Eingang
        this.addSlots(be, 2, 8, 27, 1, 3);
        // Ausgang
        this.addOutputSlots(inventory.player, be, 5, 80, 27, 1, 3);

        this.playerInv(inventory, 8, 138);
    }
}
