package com.hbm.blocks.machine.rbmk;

import net.minecraft.util.StringRepresentable;

/**
 * Der Deckel einer RBMK-Saeule.
 *
 * Abweichung vom Original: dort steckte der Deckelzustand in der Blockmetadaten-Richtung
 * (NORTH = offen, EAST = Beton, SOUTH = Bleiglas). In 1.21 ist das eine eigene Blockstate-
 * Eigenschaft, weil die Richtung bei Multiblocks schon vergeben ist.
 */
public enum RBMKLid implements StringRepresentable {

    NONE("none"),
    CONCRETE("concrete"),
    GLASS("glass");

    private final String name;

    RBMKLid(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
