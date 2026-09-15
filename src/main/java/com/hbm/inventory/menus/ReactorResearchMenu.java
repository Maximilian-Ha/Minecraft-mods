package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.ReactorResearchBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerReactorResearch.
 *
 * Die zwoelf Faecher stehen versetzt, wie die Platten im Becken liegen.
 */
public class ReactorResearchMenu extends MenuBase<ReactorResearchBlockEntity> {

    private static final int[][] SLOT_POSITIONS = new int[][] {
            {95, 22}, {131, 22},
            {77, 40}, {113, 40}, {149, 40},
            {95, 58}, {131, 58},
            {77, 76}, {113, 76}, {149, 76},
            {95, 94}, {131, 94}
    };

    public ReactorResearchMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (ReactorResearchBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public ReactorResearchMenu(int id, Inventory inventory, ReactorResearchBlockEntity be) {
        super(NtmMenuTypes.REACTOR_RESEARCH.get(), id, be);

        for(int i = 0; i < SLOT_POSITIONS.length; i++) {
            this.addSlot(new SlotNonRetarded(be, i, SLOT_POSITIONS[i][0], SLOT_POSITIONS[i][1]));
        }

        this.playerInv(inventory, 8, 140);
    }
}
