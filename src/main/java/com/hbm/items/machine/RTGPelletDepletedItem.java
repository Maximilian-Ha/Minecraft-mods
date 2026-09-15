package com.hbm.items.machine;

import com.hbm.items.EnumMultiItem;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemRTGPelletDepleted.
 *
 * Das ausgebrannte Pellet, in das ein RTG-Pellet zerfaellt. Untertypen wie im
 * Original ueber ItemEnumMulti(multiName, multiTexture) = (true, true), im Port
 * entspricht das EnumMultiItem samt NtmDataComponents.META.
 */
public class RTGPelletDepletedItem extends EnumMultiItem {

    public RTGPelletDepletedItem(Properties properties) {
        super(properties, DepletedRTGMaterial.class, true, true);
    }

    /** Reihenfolge unveraendert aus dem Original -- die Ordinalzahl ist die Metadaten-ID. */
    public enum DepletedRTGMaterial {
        BISMUTH,
        MERCURY,
        NEPTUNIUM,
        LEAD,
        ZIRCONIUM,
        NICKEL
    }
}
