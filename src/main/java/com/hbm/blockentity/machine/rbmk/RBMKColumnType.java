package com.hbm.blockentity.machine.rbmk;

/**
 * Portiert aus 1.7.10: TileEntityRBMKConsole.ColumnType.
 *
 * Womit das Reaktorpult eine Saeule im Raster zeichnet. Der Versatz ist die Zeile in der
 * Bildvorlage des Pults und stammt unveraendert aus dem Original.
 */
public enum RBMKColumnType {

    BLANK(0),
    FUEL(10),
    FUEL_SIM(90),
    CONTROL(20),
    CONTROL_AUTO(30),
    BOILER(40),
    MODERATOR(50),
    ABSORBER(60),
    REFLECTOR(70),
    OUTGASSER(80),
    BREEDER(100),
    STORAGE(110),
    COOLER(120),
    HEATEX(130);

    public final int offset;

    RBMKColumnType(int offset) {
        this.offset = offset;
    }
}
