package com.zuxelus.energycontrol.utils;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.utils.DataHelper.
 *
 * Die Feldnamen, unter denen Anbindungen ihre Messwerte im Kartenspeicher ablegen und
 * unter denen die Karten sie wieder herausholen. Nur Namen -- wer sie fuellt, weiss die
 * jeweilige Anbindung, wer sie liest, die jeweilige Karte.
 */
public final class DataHelper {

    public static final String ACTIVE = "active";
    public static final String AMOUNT = "amount";
    public static final String CAPACITY = "capacity";
    public static final String CONSUMPTION = "consumption";
    public static final String DIFF = "diff";
    public static final String ENERGY = "energy";
    public static final String EUTYPE = "euType";
    public static final String FUEL = "fuel";
    public static final String HEAT = "heat";
    public static final String MAXHEAT = "maxHeat";
    public static final String OUTPUT = "output";
    public static final String PRESSURE = "pressure";
    public static final String PROGRESS = "progress";
    public static final String MAXPROGRESS = "maxProgress";
    public static final String STATUS = "status";

    /** Fuellstaende: die Anbindung legt bis zu fuenf Tanks als fertige Zeichenketten ab. */
    public static final String TANK = "tank";
    public static final String TANK_COUNT = "tankCount";

    /** Vorsatz der Felder, die aus dem Funk-Wertgeber einer Maschine stammen. */
    public static final String ROR_PREFIX = "ror_";

    private DataHelper() { }

    /** Feldname des n-ten Tanks: tank, tank1, tank2 ... */
    public static String tank(int index) {
        return index == 0 ? TANK : TANK + index;
    }
}
