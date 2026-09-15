package com.hbm.inventory.menus;

import com.hbm.blockentity.IControlReceiverFilter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Gemeinsamer Unterbau der Menues mit Musterfiltern. Portiert aus den slotClick-Methoden von
 * ContainerCraneExtractor und ContainerCraneGrabber, die dort Wort fuer Wort dieselben sind.
 *
 * DER KERN IST DER ABGEFANGENE KLICK. Ein Musterfach haelt kein Gut, sondern ein Abbild; ein
 * Klick darauf darf deshalb nichts bewegen. Linksklick legt eine Kopie des Gehaltenen hinein,
 * der Gegenstand bleibt in der Hand. Rechtsklick auf ein belegtes Fach schaltet die
 * Vergleichsart weiter. Und weil beides nichts verschiebt, geht der Klick nie an Minecraft
 * weiter.
 *
 * Die Filterfaecher muessen die ERSTEN Faecher des Menues sein -- so haelt es das Original,
 * und die Nummern hier zaehlen darauf.
 */
public abstract class FilterMenuBase<T extends Container & IControlReceiverFilter> extends MenuBase<T> {

    public FilterMenuBase(MenuType<? extends MenuBase> menuType, int id, T be) {
        super(menuType, id, be);
    }

    /** Anzahl der Musterfaecher am Anfang des Menues. */
    public int patternSlotCount() {
        int[] range = this.be.getFilterSlots();
        return range[1] - range[0];
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {

        if(slotId < 0 || slotId >= this.patternSlotCount()) {
            super.clicked(slotId, button, type, player);
            return;
        }

        Slot slot = this.getSlot(slotId);

        /* Rechtsklick auf ein belegtes Fach: weiterschalten, sonst nichts. */
        if(button == 1 && type == ClickType.PICKUP && slot.hasItem()) {
            this.be.nextMode(slotId);
            return;
        }

        ItemStack held = this.getCarried();
        slot.set(held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
        this.be.initPattern(slotId);
    }

    /**
     * Aus einem Musterfach laesst sich nichts herausschieben, und hinein auch nichts: das
     * Einlegen geht nur ueber den Klick oben.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        if(index < this.patternSlotCount()) return ItemStack.EMPTY;

        return super.quickMoveStack(player, index);
    }
}
