package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.MachinePUREXBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerMachinePUREX.
 * Alle Fachkoordinaten unveraendert uebernommen.
 */
public class MachinePUREXMenu extends MenuBase<MachinePUREXBlockEntity> {

    public MachinePUREXMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (MachinePUREXBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public MachinePUREXMenu(int id, Inventory inventory, MachinePUREXBlockEntity be) {
        super(NtmMenuTypes.MACHINE_PUREX.get(), id, be);

        this.addSlot(new SlotNonRetarded(be, 0, 152, 81));
        this.addSlot(new SlotNonRetarded(be, 1, 35, 126));
        this.addSlots(be, 2, 152, 108, 2, 1);
        this.addSlots(be, 4, 8, 90, 1, 3);
        this.addOutputSlots(inventory.player, be, 7, 80, 36, 3, 2);

        this.playerInv(inventory, 8, 174);
    }
}
