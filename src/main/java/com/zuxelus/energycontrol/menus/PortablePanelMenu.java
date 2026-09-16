package com.zuxelus.energycontrol.menus;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.IItemCard;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import com.zuxelus.energycontrol.items.ItemInventory;
import com.zuxelus.energycontrol.items.ItemPortablePanel;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.ItemUpgrade.UpgradeType;
import com.zuxelus.energycontrol.items.cards.ItemCardBase;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.containers.ContainerPortablePanel.
 *
 * Die tragbare Tafel misst, solange man sie offen haelt. Gemessen wird in
 * {@link #broadcastChanges()} -- die Methode ruft Minecraft ohnehin jeden Tick auf dem
 * Server auf, und der Weg ueber eine eigene Block-Entitaet steht hier nicht zur Verfuegung.
 * Das Original hat es genauso gemacht (dort hiess die Methode detectAndSendChanges).
 */
public class PortablePanelMenu extends ECMenuBase<ItemInventory> {

    /** Reichweite ohne Aufwertung, in Bloecken -- wie bei der Tafel an der Wand. */
    private static final int BASE_RANGE = 8;

    private final Player player;

    public PortablePanelMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, extraData.readEnum(InteractionHand.class));
    }

    public PortablePanelMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ECMenuTypes.PORTABLE_PANEL.get(), id,
                new ItemInventory(inventory.player, hand, ItemPortablePanel.SIZE));
        this.player = inventory.player;

        addSlot(new SlotForCard(be, ItemPortablePanel.SLOT_CARD, 174, 17));
        addSlot(new SlotForRange(be, ItemPortablePanel.SLOT_UPGRADE_RANGE, 174, 35));

        addPlayerHotbar(inventory, 8, 164, be.getLockedSlot());
    }

    @Override
    public void broadcastChanges() {
        measure();
        super.broadcastChanges();
    }

    /** Liest die Karte neu ein, von dort, wo der Spieler gerade steht. */
    private void measure() {
        if(player.level().isClientSide) return;

        ItemStack stack = be.getItem(ItemPortablePanel.SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return;

        ItemCardReader reader = new ItemCardReader(stack);
        IItemCard card = (IItemCard) stack.getItem();
        int range = card.isRemoteCard(stack) ? getRange() : -1;

        CardState state;
        try {
            state = card.update(player.level(), reader, range, player.blockPosition());
        } catch(Exception e) {
            // Eine fremde Karte darf die Oberflaeche nicht mit in den Abgrund reissen.
            state = CardState.CUSTOM_ERROR;
        }
        reader.setState(state);
        be.setChanged();
    }

    public int getRange() {
        ItemStack stack = be.getItem(ItemPortablePanel.SLOT_UPGRADE_RANGE);
        if(!ItemUpgrade.is(stack, UpgradeType.RANGE)) return BASE_RANGE;
        return BASE_RANGE + stack.getCount() * ECConfig.rangeUpgradeRange();
    }

    private static class SlotForCard extends Slot {

        SlotForCard(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ItemCardBase.isCard(stack);
        }
    }

    private static class SlotForRange extends Slot {

        SlotForRange(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ItemUpgrade.is(stack, UpgradeType.RANGE);
        }
    }
}
