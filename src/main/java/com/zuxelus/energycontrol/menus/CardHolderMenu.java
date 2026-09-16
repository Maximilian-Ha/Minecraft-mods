package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.init.ECMenuTypes;
import com.zuxelus.energycontrol.items.ItemCardHolder;
import com.zuxelus.energycontrol.items.ItemInventory;
import com.zuxelus.energycontrol.items.cards.ItemCardBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerCardHolder.
 *
 * Vierundfuenfzig Faecher wie in einer grossen Truhe, aber nur fuer Karten. Das Fach, in
 * dem der Halter selbst liegt, ist gesperrt.
 */
public class CardHolderMenu extends ECMenuBase<ItemInventory> {

    public CardHolderMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, extraData.readEnum(InteractionHand.class));
    }

    public CardHolderMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ECMenuTypes.CARD_HOLDER.get(), id,
                new ItemInventory(inventory.player, hand, ItemCardHolder.SIZE));

        for(int row = 0; row < 6; row++) {
            for(int col = 0; col < 9; col++) {
                addSlot(new SlotCardOnly(be, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        addPlayerInventory(inventory, 8, 140, be.getLockedSlot());
    }

    /** In den Halter passen nur Karten. */
    private static class SlotCardOnly extends Slot {

        SlotCardOnly(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ItemCardBase.isCard(stack);
        }
    }
}
