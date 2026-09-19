package com.hbm.inventory.menus;

import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.recipes.MagicRecipes;
import com.hbm.items.NtmItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerBook.
 *
 * Die Werkbank des Buchs: vier Plaetze und ein Ergebnis. Anders als die gewoehnliche Werkbank
 * hat sie keine Form -- es zaehlt nur, was in den belegten Plaetzen liegt (siehe MagicRecipes).
 *
 * Das Buch bleibt im Inventar liegen; die Maske haengt an keinem Block. Beim Schliessen faellt
 * heraus, was noch auf den vier Plaetzen liegt, damit nichts verlorengeht -- so macht es auch
 * das Original.
 *
 * Alle Plaetzkoordinaten sind unveraendert aus ContainerBook uebernommen.
 */
public class BookMenu extends AbstractContainerMenu {

    private static final int FIRST_PLAYER_SLOT = 5;

    public final Container matrix = new SimpleContainer(4);
    public final Container result = new SimpleContainer(1);

    public BookMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory);
    }

    public BookMenu(int id, Inventory inventory) {
        super(NtmMenuTypes.BOOK.get(), id);

        this.addSlot(new ResultSlot(inventory.player, 124, 35));

        for(int y = 0; y < 2; y++) {
            for(int x = 0; x < 2; x++) {
                this.addSlot(new Slot(this.matrix, x + y * 2, 30 + x * 36, 17 + y * 36));
            }
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for(int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 142));
        }

        this.slotsChanged(this.matrix);
    }

    @Override
    public void slotsChanged(Container container) {
        this.result.setItem(0, MagicRecipes.getRecipe(this.matrix));
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().contains(new ItemStack(NtmItems.BOOK_OF_.get()));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if(player.level().isClientSide) return;

        for(int i = 0; i < this.matrix.getContainerSize(); i++) {
            ItemStack stack = this.matrix.removeItemNoUpdate(i);
            if(!stack.isEmpty()) player.drop(stack, false);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        Slot slot = this.slots.get(index);
        if(slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if(index == 0) {

            /* Das Ergebnis geht nur ins Inventar, und dort von hinten nach vorn. */
            if(!this.moveItemStackTo(stack, FIRST_PLAYER_SLOT, this.slots.size(), true)) return ItemStack.EMPTY;

        } else if(index < FIRST_PLAYER_SLOT) {

            if(!this.moveItemStackTo(stack, FIRST_PLAYER_SLOT, this.slots.size(), false)) return ItemStack.EMPTY;

        } else {

            if(!this.moveItemStackTo(stack, 1, FIRST_PLAYER_SLOT, false)) return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if(stack.getCount() == copy.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.result && super.canTakeItemForPickAll(stack, slot);
    }

    /**
     * Der Ergebnisplatz. Das Abrechnen des Handwerks steht schon in SlotCraftingOutput; hier
     * kommt nur dazu, dass jeder der vier Plaetze einen Gegenstand abgibt -- genauso wie der
     * SlotCrafting des Originals an einer 2x2-Matrix.
     */
    private class ResultSlot extends SlotCraftingOutput {

        ResultSlot(Player player, int x, int y) {
            super(player, BookMenu.this.result, 0, x, y);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);

            for(int i = 0; i < BookMenu.this.matrix.getContainerSize(); i++) {
                BookMenu.this.matrix.removeItem(i, 1);
            }

            BookMenu.this.slotsChanged(BookMenu.this.matrix);
        }
    }
}
