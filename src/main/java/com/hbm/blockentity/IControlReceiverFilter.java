package com.hbm.blockentity;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.module.ModulePatternMatcher;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.IControlReceiverFilter.
 *
 * Gemeinsames Verhalten aller Maschinen, die einen Filter aus Mustern fuehren. Die Muster liegen
 * als Gegenstaende in Faechern der Maschine; WIE verglichen wird, haelt der Mustervergleicher
 * fest.
 *
 * NUR DREI METHODEN, und das mit Absicht. Auszieher und Greifer fuehren EINEN Vergleicher ueber
 * neun Muster, der Verteiler SECHS ueber je fuenf -- einen fuer jede Seite. Ein "gib mir deinen
 * Vergleicher" in dieser Schnittstelle haette den Verteiler gezwungen, einen davon
 * herauszugreifen und die uebrigen zu verschweigen. Der gemeinsame Teil ist deshalb der
 * Hilfsgriff unten, den jede Maschine mit ihren eigenen Grenzen aufruft.
 *
 * NICHT UEBERNOMMEN: das Original leitet diese Schnittstelle von ICopiable ab, damit sich ein
 * eingestellter Filter mit dem Abschreibwerkzeug auf die naechste Maschine uebertragen laesst.
 * Das Werkzeug ist eine eigene Sache.
 */
public interface IControlReceiverFilter extends IControlReceiver {

    /** Erstes und letztes Filterfach -- der Anfang zaehlt mit, das Ende nicht. */
    int[] getFilterSlots();

    /** Schaltet die Vergleichsart eines Faches eine Stelle weiter. */
    void nextMode(int i);

    /** Setzt die Vergleichsart eines frisch belegten Faches. */
    void initPattern(int i);

    /**
     * Passt der Gegenstand auf eines der Muster in den Faechern von "from" bis "to"? Ein leeres
     * Fach zaehlt nicht mit -- ein Filter ohne Muster passt also auf nichts.
     */
    static boolean matches(Container container, ModulePatternMatcher matcher, int from, int to, ItemStack stack) {

        for(int i = from; i < to; i++) {

            ItemStack filter = container.getItem(i);
            if(!filter.isEmpty() && matcher.isValidForFilter(filter, i - from, stack)) return true;
        }

        return false;
    }
}
