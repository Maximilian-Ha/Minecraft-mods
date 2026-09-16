package com.hbm.inventory.menus;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.items.armor.ItemArmorMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerArmorTable.
 *
 * Der Ruestungstisch. In der Mitte liegt das Ruestungsteil, darum herum die neun Modulplaetze
 * -- einer je Bauart. Wer ein Modul hineinlegt, baut es sofort ein; wer es herausnimmt, baut es
 * aus. Legt man ein Teil auf den Tisch, werden seine schon eingebauten Module ausgelegt.
 *
 * Wie der Waffentisch hat er kein eigenes Inventar ueber das Schliessen hinaus: beim Schliessen
 * faellt alles heraus. Damit kann nichts verlorengehen.
 *
 * Alle Plaetzkoordinaten sind unveraendert aus GUIArmorTable uebernommen, um den Betrag 22
 * verschoben, den das Original erst beim Zeichnen aufaddiert.
 */
public class ArmorTableMenu extends AbstractContainerMenu {

    /** Der Platz fuer das Ruestungsteil sitzt hinter den neun Modulplaetzen. */
    public static final int ARMOR_SLOT = ArmorModHandler.MOD_SLOTS;
    private static final int FIRST_PLAYER_SLOT = ArmorModHandler.MOD_SLOTS + 5;

    public final Container mods = new SimpleContainer(ArmorModHandler.MOD_SLOTS);
    public final Container armor = new SimpleContainer(1);

    /** Gebraucht wird davon nur die Welt: ArmorModHandler liest daraus die Registrierung. */
    private final Level level;

    public ArmorTableMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory);
    }

    public ArmorTableMenu(int id, Inventory inventory) {
        super(NtmMenuTypes.ARMOR_TABLE.get(), id);

        this.level = inventory.player.level();

        this.addSlot(new ModSlot(ArmorModHandler.HELMET_ONLY, 48, 27));
        this.addSlot(new ModSlot(ArmorModHandler.PLATE_ONLY, 84, 27));
        this.addSlot(new ModSlot(ArmorModHandler.LEGS_ONLY, 120, 27));
        this.addSlot(new ModSlot(ArmorModHandler.BOOTS_ONLY, 156, 45));
        this.addSlot(new ModSlot(ArmorModHandler.SERVOS, 156, 81));
        this.addSlot(new ModSlot(ArmorModHandler.CLADDING, 120, 99));
        this.addSlot(new ModSlot(ArmorModHandler.KEVLAR, 84, 99));
        this.addSlot(new ModSlot(ArmorModHandler.EXTRA, 48, 99));
        this.addSlot(new ModSlot(ArmorModHandler.BATTERY, 30, 63));

        this.addSlot(new ArmorSlot(66, 63));

        /* Die vier getragenen Teile am linken Rand, damit man sie nicht erst ablegen muss. */
        for(int i = 0; i < 4; i++) {
            final EquipmentSlot equipment = ArmorModHandler.ARMOR_SLOTS[i];
            this.addSlot(new Slot(inventory, 39 - i, 5, 36 + i * 18) {

                @Override public int getMaxStackSize() { return 1; }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.canEquip(equipment, inventory.player);
                }
            });
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 30 + x * 18, 140 + y * 18));
            }
        }

        for(int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inventory, x, 30 + x * 18, 198));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if(player.level().isClientSide) return;

        /* Erst die Module: was auf dem Tisch liegt, faellt heraus und wird damit auch
         * wieder ausgebaut. */
        for(int i = 0; i < this.mods.getContainerSize(); i++) {
            ItemStack mod = this.mods.removeItemNoUpdate(i);
            if(!mod.isEmpty()) {
                ArmorModHandler.removeMod(this.armor.getItem(0), i);
                player.drop(mod, false);
            }
        }

        ItemStack piece = this.armor.removeItemNoUpdate(0);
        if(!piece.isEmpty()) player.drop(piece, false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        copy = stack.copy();

        if(index < FIRST_PLAYER_SLOT) {

            /* Vom Tisch ins Inventar. */
            if(!this.moveItemStackTo(stack, FIRST_PLAYER_SLOT, this.slots.size(), true)) return ItemStack.EMPTY;
            slot.onTake(player, stack);

        } else if(stack.getItem() instanceof ArmorItem) {

            if(!this.moveItemStackTo(stack, ARMOR_SLOT, ARMOR_SLOT + 1, false)) return ItemStack.EMPTY;

        } else if(stack.getItem() instanceof ItemArmorMod mod) {

            /* Ein Modul kennt seinen eigenen Platz -- es geht nur dorthin. */
            if(!this.slots.get(mod.type).mayPlace(stack)) return ItemStack.EMPTY;
            if(!this.moveItemStackTo(stack, mod.type, mod.type + 1, false)) return ItemStack.EMPTY;

        } else {
            return ItemStack.EMPTY;
        }

        if(stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    /** Ein Modulplatz. Er nimmt nur Module seiner eigenen Bauart, und nur passende. */
    private class ModSlot extends Slot {

        private final int type;

        ModSlot(int type, int x, int y) {
            super(ArmorTableMenu.this.mods, type, x, y);
            this.type = type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            ItemStack piece = ArmorTableMenu.this.armor.getItem(0);
            return ArmorModHandler.isApplicable(piece, stack) && ((ItemArmorMod) stack.getItem()).type == this.type;
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);

            ItemStack piece = ArmorTableMenu.this.armor.getItem(0);

            if(!stack.isEmpty() && ArmorModHandler.isApplicable(piece, stack)) {
                ArmorModHandler.applyMod(ArmorTableMenu.this.level, piece, stack);
            }
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            ArmorModHandler.removeMod(ArmorTableMenu.this.armor.getItem(0), this.type);
        }
    }

    /** Der Platz in der Mitte. Was hineinkommt, legt seine Module auf den Tisch. */
    private class ArmorSlot extends Slot {

        ArmorSlot(int x, int y) {
            super(ArmorTableMenu.this.armor, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ArmorItem;
        }

        @Override
        public void set(ItemStack stack) {

            if(!stack.isEmpty()) {
                ItemStack[] installed = ArmorModHandler.pryMods(ArmorTableMenu.this.level, stack);
                for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
                    ArmorTableMenu.this.mods.setItem(i, installed[i]);
                }
            }

            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);

            /* Das Teil nimmt mit, was in ihm steckt -- der Tisch bleibt leer zurueck. */
            for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
                if(ArmorModHandler.isApplicable(stack, ArmorTableMenu.this.mods.getItem(i))) {
                    ArmorTableMenu.this.mods.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

}
