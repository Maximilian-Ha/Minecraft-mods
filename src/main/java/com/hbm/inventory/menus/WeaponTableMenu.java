package com.hbm.inventory.menus;

import com.hbm.inventory.NtmMenuTypes;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerWeaponTable.
 *
 * Der Waffentisch. Links liegt die Waffe, rechts daneben sieben Plaetze fuer Aufsaetze. Wer einen
 * Aufsatz hineinlegt, baut ihn sofort an; wer ihn herausnimmt, baut ihn ab.
 *
 * Der Tisch hat kein eigenes Inventar, das ueber das Schliessen hinaus bestuende -- beim Schliessen
 * faellt alles heraus, genau wie im Original. Gerade deshalb ist er sicher: es gibt keinen Zustand,
 * in dem Aufsaetze verlorengehen koennen.
 *
 * Beidhaendige Waffen haben zwei Empfaenger, jeder mit eigenen Aufsaetzen. Der Umschalter waehlt,
 * auf welchen von beiden die Plaetze gerade gehen: beim Umschalten wird angebaut, was liegt, und
 * herausgelegt, was am anderen Empfaenger steckt.
 *
 * ABWEICHUNG: das Original faedelt das Umschalten durch slotClick mit einem Sondermodus (999_999).
 * In 1.21 gibt es dafuer clickMenuButton, den vorgesehenen Weg -- der Knopf schickt die Nummer des
 * gewuenschten Empfaengers.
 */
public class WeaponTableMenu extends AbstractContainerMenu {

    public static final int MOD_SLOTS = 7;

    public final Container mods = new SimpleContainer(MOD_SLOTS);
    public final Container gun = new SimpleContainer(1);

    /** Auf welche Waffenkonfiguration die Aufsaetze gehen. */
    public int index = 0;

    public WeaponTableMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory);
    }

    public WeaponTableMenu(int id, Inventory inventory) {
        super(NtmMenuTypes.WEAPON_TABLE.get(), id);

        for(int i = 0; i < MOD_SLOTS; i++) this.addSlot(new ModSlot(this.mods, i, 44 + 18 * i, 108));

        this.addSlot(new Slot(this.gun, 0, 8, 108) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return WeaponTableMenu.this.gun.getItem(0).isEmpty() && stack.getItem() instanceof GunBaseNTItem;
            }

            @Override
            public void set(ItemStack stack) {

                WeaponTableMenu.this.index = 0;

                if(!stack.isEmpty()) {
                    ItemStack[] installed = XWeaponModManager.getUpgradeItems(stack, WeaponTableMenu.this.index);
                    for(int i = 0; i < Math.min(installed.length, MOD_SLOTS); i++) {
                        WeaponTableMenu.this.mods.setItem(i, installed[i]);
                    }
                }

                super.set(stack);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);

                WeaponTableMenu.this.applyMods(stack);

                /* Was angebaut wurde, steckt jetzt in der Waffe und liegt nicht mehr auf dem Tisch. */
                for(int i = 0; i < MOD_SLOTS; i++) {
                    ItemStack mod = WeaponTableMenu.this.mods.getItem(i);
                    if(XWeaponModManager.isApplicable(stack, mod, WeaponTableMenu.this.index, false)) {
                        WeaponTableMenu.this.mods.setItem(i, ItemStack.EMPTY);
                    }
                }

                WeaponTableMenu.this.index = 0;
            }
        });

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 158 + y * 18));
            }
        }

        for(int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 216));
        }
    }

    /**
     * Der Umschalter. Er bekommt die Nummer des Empfaengers, auf den die Plaetze umziehen sollen:
     * erst wird angebaut, was gerade liegt, dann geraeumt, was angebaut wurde, dann kommt heraus,
     * was am neuen Empfaenger steckt. Nichts geht dabei verloren -- was nicht an den alten
     * Empfaenger passte, bleibt einfach liegen.
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {

        ItemStack stack = this.gun.getItem(0);
        if(stack.isEmpty() || !(stack.getItem() instanceof GunBaseNTItem weapon)) return false;
        if(id < 0 || id >= weapon.getConfigCount()) return false;

        this.applyMods(stack);

        for(int i = 0; i < MOD_SLOTS; i++) {
            ItemStack mod = this.mods.getItem(i);
            if(XWeaponModManager.isApplicable(stack, mod, this.index, false)) this.mods.setItem(i, ItemStack.EMPTY);
        }

        this.index = id;

        ItemStack[] installed = XWeaponModManager.getUpgradeItems(stack, this.index);
        for(int i = 0; i < Math.min(installed.length, MOD_SLOTS); i++) {
            this.mods.setItem(i, installed[i]);
        }

        this.broadcastChanges();
        return true;
    }

    /** Wie viele Empfaenger die Waffe auf dem Tisch hat -- eins, wenn keine daliegt. */
    public int getConfigCount() {
        ItemStack stack = this.gun.getItem(0);
        if(stack.getItem() instanceof GunBaseNTItem weapon) return weapon.getConfigCount();
        return 1;
    }

    private void applyMods(ItemStack stack) {
        if(stack.isEmpty()) return;
        XWeaponModManager.install(stack, this.index,
                this.mods.getItem(0), this.mods.getItem(1), this.mods.getItem(2), this.mods.getItem(3),
                this.mods.getItem(4), this.mods.getItem(5), this.mods.getItem(6));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if(player.level().isClientSide) return;

        for(int i = 0; i < this.mods.getContainerSize(); i++) {
            ItemStack stack = this.mods.removeItemNoUpdate(i);
            if(!stack.isEmpty()) player.drop(stack, false);
        }

        ItemStack weapon = this.gun.removeItemNoUpdate(0);

        if(!weapon.isEmpty()) {
            /* Was noch auf dem Tisch lag, ist beim Schliessen heruntergefallen -- also auch ab. */
            XWeaponModManager.uninstall(weapon, this.index);
            player.drop(weapon, false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem()) {

            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if(index < 8) {
                if(!this.moveItemStackTo(stack, 8, this.slots.size(), true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, copy);
            } else {
                if(stack.getItem() instanceof GunBaseNTItem) {
                    if(!this.moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.moveItemStackTo(stack, 0, 7, false)) return ItemStack.EMPTY;
                }
            }

            if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }

        return copy;
    }

    /** Ein Aufsatzplatz. Er nimmt nur, was an die eingelegte Waffe passt und keinen Platz doppelt belegt. */
    public class ModSlot extends Slot {

        public ModSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            ItemStack weapon = WeaponTableMenu.this.gun.getItem(0);
            return !weapon.isEmpty() && XWeaponModManager.isApplicable(weapon, stack, WeaponTableMenu.this.index, true);
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            WeaponTableMenu.this.applyMods(WeaponTableMenu.this.gun.getItem(0));
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            WeaponTableMenu.this.applyMods(WeaponTableMenu.this.gun.getItem(0));
        }
    }
}
