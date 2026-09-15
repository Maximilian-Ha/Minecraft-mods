package com.hbm.items.machine;

import com.hbm.items.EnumMultiItem;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemDrive.
 *
 * Datentraeger. Leer gehen sie ins Bandlaufwerk, voll kommen sie wieder heraus -- und was
 * darauf steht, entscheidet, wofuer sie danach taugen.
 *
 * ZWEI BAUFORMEN, DER KLEINE UND DER GROSSE: der Steckspeicher (FLASH) nimmt die Rechenergebnisse
 * des Grossrechners auf, die Platte (DISK) die Messungen aus der Umlaufbahn. Beide gibt es leer,
 * beschrieben und hinueber.
 *
 * DIE REIHENFOLGE DER AUFZAEHLUNG IST DIE DES ORIGINALS und darf nicht umgestellt werden: die
 * Stelle in der Aufzaehlung ist zugleich der Metawert im Gegenstandsstapel, sie steht also in
 * jedem gespeicherten Spielstand.
 */
public class DriveItem extends EnumMultiItem {

    public DriveItem(Properties properties) {
        super(properties, DriveType.class, true, true);
    }

    public enum DriveType {
        FLASH_EMPTY,
        DISK_EMPTY,
        FLASH_BROKEN,
        DISK_BROKEN,

        FLASH_FLIGHTSIM,            // Vorausberechnung fuer die Raumfahrt
        FLASH_PARTICLESIM,          // Vorausberechnung fuer die Fusion

        DISK_FLIGHTDATA,            // Rohdaten vom Satelliten
        DISK_FLIGHTDATA_PROCESSED,  // dieselben, ausgewertet
        DISK_ORBITDATA,             // Rohdaten der Messfuehler
        DISK_ORBITDATA_PROCESSED,   // dieselben, ausgewertet

        KLAUS,                      // kkklanker
    }
}
