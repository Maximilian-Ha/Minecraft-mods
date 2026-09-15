package com.hbm.config;

/**
 * Portiert aus 1.7.10: com.hbm.config.VersatileConfig -- allerdings nur die beiden
 * RTG-Schalter, alles andere aus der Originalklasse gehoert zu anderen Gruppen und
 * wird dort nachgezogen.
 *
 * Im Original lauten die beiden Methoden:
 *   rtgDecay()       = GeneralConfig.enable528 || MachineConfig.doRTGsDecay
 *   scaleRTGPower()  = GeneralConfig.enable528 || MachineConfig.scaleRTGPower
 *
 * MachineConfig gibt es im Port nicht; die beiden Optionen
 * "9.00_scaleRTGPower" (Standard false) und "9.01_doRTGsDecay" (Standard true)
 * fehlen in CommonConfig. Damit die Werte trotzdem 1:1 stimmen, stehen hier die
 * Originalstandards als Konstanten. Sobald die Optionen in CommonConfig
 * nachgetragen sind, muessen nur diese beiden Konstanten durch die Config-Abfrage
 * ersetzt werden.
 */
public class VersatileConfig {

    /** Originalstandard von MachineConfig.doRTGsDecay. */
    private static final boolean DEFAULT_DO_RTGS_DECAY = true;
    /** Originalstandard von MachineConfig.scaleRTGPower. */
    private static final boolean DEFAULT_SCALE_RTG_POWER = false;

    /** Zerfallen RTG-/Betavoltaik-Pellets ueberhaupt? */
    /**
     * Wie unwahrscheinlich es ist, dass aus einem verstrahlten Uranerz Schrabidium wird -- eins
     * zu diesem Wert.
     *
     * ABWEICHUNG: das Original laesst den Wert ueber GeneralConfig.enableLBSM/schrabRate
     * verstellen. Diese beiden Schalter gibt es im Port nicht; hier steht der Standardwert.
     */
    public static int getSchrabOreChance() {
        return 250;
    }

    public static boolean rtgDecay() {
        return NtmConfig.enable528() || DEFAULT_DO_RTGS_DECAY;
    }

    /** Sinkt die Leistung eines Pellets mit fortschreitendem Zerfall? */
    public static boolean scaleRTGPower() {
        return NtmConfig.enable528() || DEFAULT_SCALE_RTG_POWER;
    }
}
