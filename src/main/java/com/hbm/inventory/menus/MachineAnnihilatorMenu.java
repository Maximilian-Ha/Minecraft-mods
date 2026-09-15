package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachineAnnihilatorBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MachineAnnihilatorMenu extends MenuBase<MachineAnnihilatorBlockEntity> {

    public MachineAnnihilatorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachineAnnihilatorBlockEntity) inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public MachineAnnihilatorMenu(int id, Inventory inventory, MachineAnnihilatorBlockEntity be) {
        super(NtmMenuTypes.ANNIHILATOR.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, 0, 17, 45));   // Muell
        this.addSlot(new SlotNonRetarded(be, 1, 35, 45));   // Fluidkennung
        this.addOutputSlots(inventory.player, be, 2, 80, 36, 2, 3);
        this.addSlot(new SlotNonRetarded(be, 8, 152, 18));  // Beobachtung
        this.addSlot(new SlotNonRetarded(be, 9, 152, 62));  // Anforderung
        this.addSlot(new SlotCraftingOutput(inventory.player, be, 10, 152, 80));

        this.playerInv(inventory, 8, 126);
    }
}
