package com.hbm.config;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.IConfigurableMachine.
 *
 * Im Original ist das eine Schnittstelle, die jede konfigurierbare TileEntity implementiert.
 * MachineDynConfig legt dann von jeder eine wegwerfbare Instanz an -- ausdruecklich nur, um
 * getConfigName(), readIfPresent() und writeConfig() aufrufen zu koennen; die Werte selbst sind
 * auch dort statisch.
 *
 * Auf 1.21 geht das nicht mehr: ein BlockEntity braucht Position und Blockzustand im
 * Konstruktor, einen parameterlosen gibt es nicht. Da die Werte ohnehin statisch sind, braucht
 * es die Instanz aber gar nicht. Der Port meldet stattdessen ein Paar statischer Methoden je
 * Maschine an (siehe {@link MachineDynConfig#register}). Dateiformat, Schluessel und
 * Ueberschreibungsverhalten bleiben dabei unveraendert -- eine hbmMachines.json aus dem
 * Original laesst sich unveraendert weiterverwenden.
 *
 * Die Namenskonvention der Schluessel stammt ebenfalls aus dem Original: das Praefix nennt den
 * Datentyp (I: int, L: long, D: double, B: boolean, M: verschachteltes Objekt).
 */
public interface IConfigurableMachine {

    static boolean grab(JsonObject obj, String name, boolean def) {
        return obj.has(name) ? obj.get(name).getAsBoolean() : def;
    }

    static int grab(JsonObject obj, String name, int def) {
        return obj.has(name) ? obj.get(name).getAsInt() : def;
    }

    static long grab(JsonObject obj, String name, long def) {
        return obj.has(name) ? obj.get(name).getAsLong() : def;
    }

    static double grab(JsonObject obj, String name, double def) {
        return obj.has(name) ? obj.get(name).getAsDouble() : def;
    }

    /** Schreibt die Konfiguration einer Maschine in das bereits geoeffnete JSON-Objekt. */
    @FunctionalInterface
    interface ConfigWriter {
        void write(JsonWriter writer) throws IOException;
    }
}
