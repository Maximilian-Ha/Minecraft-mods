package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKControlBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKControl.
 * Der Steuerstab hat kein eigenes Fach, nur das Inventar des Spielers.
 */
public class RBMKControlMenu extends MenuBase<RBMKControlBlockEntity> {

    public RBMKControlMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKControlBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKControlMenu(int id, Inventory inventory, RBMKControlBlockEntity be) {
        super(NtmMenuTypes.RBMK_CONTROL.get(), id, be);

        this.playerInv(inventory, 8, 104, 162);
    }

    /** Ohne eigene Faecher gibt es nichts zu verschieben. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
