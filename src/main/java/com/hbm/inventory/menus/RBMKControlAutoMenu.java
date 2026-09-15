package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKControlAutoBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKControlAuto.
 * Kein eigenes Fach, nur das Inventar des Spielers.
 */
public class RBMKControlAutoMenu extends MenuBase<RBMKControlAutoBlockEntity> {

    public RBMKControlAutoMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKControlAutoBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKControlAutoMenu(int id, Inventory inventory, RBMKControlAutoBlockEntity be) {
        super(NtmMenuTypes.RBMK_CONTROL_AUTO.get(), id, be);
        this.playerInv(inventory, 8, 104, 162);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
