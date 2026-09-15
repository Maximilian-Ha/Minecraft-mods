package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKRod.
 * Die Fachkoordinaten sind unveraendert uebernommen.
 */
public class RBMKRodMenu extends MenuBase<RBMKRodBlockEntity> {

    public RBMKRodMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKRodBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKRodMenu(int id, Inventory inventory, RBMKRodBlockEntity be) {
        super(NtmMenuTypes.RBMK_ROD.get(), id, be);

        this.addSlots(be, 0, 80, 45, 1, 1);
        this.playerInv(inventory, 8, 104, 162);
    }

    /**
     * Ein heisser Brennstab laesst sich nicht von Hand ziehen. Das Original faengt das im
     * slotClick ab, hier passiert dasselbe.
     */
    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {

        if(slot == 0 && !player.hasInfiniteMaterials() && !this.be.coldEnoughForManual()) return;

        super.clicked(slot, button, type, player);
    }
}
