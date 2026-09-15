package com.hbm.inventory.menus;

import com.hbm.blockentity.machine.rbmk.RBMKBoilerBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerRBMKGeneric.
 * Der Dampferzeuger hat kein eigenes Fach, nur das Inventar des Spielers.
 */
public class RBMKBoilerMenu extends MenuBase<RBMKBoilerBlockEntity> {

    public RBMKBoilerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (RBMKBoilerBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public RBMKBoilerMenu(int id, Inventory inventory, RBMKBoilerBlockEntity be) {
        super(NtmMenuTypes.RBMK_BOILER.get(), id, be);

        this.playerInv(inventory, 8, 104, 162);
    }

    /** Ohne eigene Faecher gibt es nichts zu verschieben. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
