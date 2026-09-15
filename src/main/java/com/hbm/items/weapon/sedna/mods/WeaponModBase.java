package com.hbm.items.weapon.sedna.mods;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModBase.
 *
 * Die Basis aller Waffenaufsaetze. Ein Aufsatz bekommt beim Anlegen eine feste Nummer -- sie steht
 * spaeter in der Waffe und darf sich nie verschieben, sonst wird aus einem Schalldaempfer ueber
 * Nacht ein Zielfernrohr.
 *
 * Die Reihenfolge, in der Aufsaetze ausgewertet werden, entscheidet, wie sie sich stapeln: was
 * multipliziert, muss vor dem stehen, was addiert, sonst kommt etwas anderes heraus.
 */
public abstract class WeaponModBase implements IWeaponMod {

    /** Setzt den Wert hart; alles davor ist damit wirkungslos. */
    public static final int PRIORITY_SET = 1_000_000;
    public static final int PRIORITY_MULTIPLICATIVE = 1_000;
    public static final int PRIORITY_ADDITIVE = 500;
    /** Multipliziert ganz zum Schluss, nach allen Summen. */
    public static final int PRIORITY_MULT_FINAL = -1;

    public final String[] slots;
    public int priority = 0;

    public WeaponModBase(int id, String... slots) {
        this.slots = slots;
        XWeaponModManager.idToMod.put(id, this);
    }

    public WeaponModBase setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override public int getModPriority() { return this.priority; }
    @Override public String[] getSlots() { return this.slots; }

    /**
     * Der Umweg ueber Object erspart den doppelten Cast: von int nach T ginge sonst nur als
     * (T) (Integer) int. Der zweite Parameter dient allein dazu, T abzuleiten.
     */
    @SuppressWarnings("unchecked")
    public <T> T cast(Object arg, T castTo) { return (T) arg; }
}
