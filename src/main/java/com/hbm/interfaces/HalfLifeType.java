package com.hbm.interfaces;

/**
 * Portiert aus 1.7.10: com.hbm.interfaces.HalfLifeType.
 *
 * Einheit, in der eine Halbwertszeit angegeben wird. RTGUtil.getLifespan rechnet
 * damit die Lebensdauer eines Pellets in Ticks aus.
 */
public enum HalfLifeType {
    /** In Tagen gezaehlt. */
    SHORT,
    /** In Jahren gezaehlt. */
    MEDIUM,
    /** In hundert Jahren gezaehlt. */
    LONG
}
