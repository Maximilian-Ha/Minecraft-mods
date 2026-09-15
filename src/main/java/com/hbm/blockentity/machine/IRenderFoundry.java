package com.hbm.blockentity.machine;

import com.hbm.inventory.material.NTMMaterial;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.IRenderFoundry.
 *
 * Was der Renderer ueber einen Giessereiblock wissen muss, um die Schmelze darin zu zeichnen:
 * ob ueberhaupt etwas drin ist, wie hoch es steht, welche Farbe es hat und wie gross die
 * Flaeche ist, die gefuellt wird.
 */
public interface IRenderFoundry {

    /** Ob eine Schicht fluessigen Metalls gezeichnet werden soll. */
    boolean shouldRender();

    /**
     * Auf welcher Hoehe die Schicht liegt.
     *
     * ABWEICHUNG: im Original heisst diese Methode getLevel. Auf 1.21 ist der Name in
     * BlockEntity mit der Welt belegt, deshalb heisst sie hier getFillLevel.
     */
    double getFillLevel();

    /** Das Material -- gebraucht wird vor allem seine Farbe. */
    NTMMaterial getMat();

    /* Die Abmessungen des Rechtecks, das gezeichnet wird. */
    double minX();
    double maxX();
    double minZ();
    double maxZ();

    /** Hoehe, auf der die eingelegte Form liegt. */
    double moldHeight();

    /** Hoehe, auf der das fertige Gussstueck liegt. */
    double outHeight();
}
