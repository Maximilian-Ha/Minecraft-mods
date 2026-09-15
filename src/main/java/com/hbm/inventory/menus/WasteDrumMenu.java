package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.WasteDrumBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerWasteDrum.
 * Die zwoelf Faecher liegen als Rundung, genau wie im Original.
 */
public class WasteDrumMenu extends MenuBase<WasteDrumBlockEntity> {

    /** Lage der zwoelf Faecher, Paar fuer Paar aus dem Original uebernommen. */
    private static final int[][] SLOTS = {
            {71, 21}, {89, 21},
            {53, 39}, {71, 39}, {89, 39}, {107, 39},
            {53, 57}, {71, 57}, {89, 57}, {107, 57},
            {71, 75}, {89, 75}
    };

    public WasteDrumMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (WasteDrumBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public WasteDrumMenu(int id, Inventory inventory, WasteDrumBlockEntity be) {
        super(NtmMenuTypes.WASTE_DRUM.get(), id, be);

        for(int i = 0; i < SLOTS.length; i++) {
            this.addSlot(new SlotNonRetarded(be, i, SLOTS[i][0], SLOTS[i][1]));
        }

        this.playerInv(inventory, 8, 107);
    }
}
