package com.hbm.blocks.states;

import net.minecraft.util.StringRepresentable;

/**
 * Portiert aus 1.7.10: die neun Metadatenwerte von com.hbm.blocks.machine.pile.BlockPile.
 *
 * Woraus ein zusammengebauter Chicago Pile besteht. Der weitaus groesste Teil ist DUMMY --
 * blosser Graphitblock. Alles andere kennzeichnet eine Stelle, an der etwas hinein- oder
 * herausgeht, und begrenzt, wo sich noch ein Kanal bohren laesst.
 */
public enum PileBlockType implements StringRepresentable {

    /** Blosser Fuellblock. Den Loewenanteil macht der aus. */
    DUMMY("dummy"),
    /** Der Kern. Er fuehrt die Rechnung und gibt es nur einmal je Anlage. */
    CORE("core"),
    /** Mittelstueck eines Kanals -- dient dazu, Kreuzungen zu erkennen. */
    CHANNEL("channel"),
    /** Anfang eines Brennstoffkanals: hier werden die Staebe eingeschoben. */
    FUEL_IN("fuel_in"),
    /** Ende eines Brennstoffkanals: hier fallen sie heraus. */
    FUEL_OUT("fuel_out"),
    /** Anfang eines Lueftungskanals. */
    AIR_IN("air_in"),
    /** Ende eines Lueftungskanals. */
    AIR_OUT("air_out"),
    /** Senkrechter Kanal fuer einen Steuerstab. */
    CONTROL("control"),
    /** Kante des Quaders. Hier darf nicht gebohrt werden, sonst faellt die Anlage auseinander. */
    EDGE("edge");

    private final String name;

    PileBlockType(String name) {
        this.name = name;
    }

    @Override public String getSerializedName() { return this.name; }
}
