package com.hbm.blockentity.machine.rbmk;

import com.hbm.saveddata.RBMKDialsSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Die Stellschrauben des RBMK. Jede Welt hat ihren eigenen Satz.
 *
 * Abweichung vom Original: 1.7.10 hat die Dials als frei anlegbare Gamerules gefuehrt. Seit 1.13
 * muessen Gamerules statisch registriert sein und koennen nur boolean oder int speichern, 18 der
 * 24 Dials sind aber Kommazahlen. Die Werte liegen daher in {@link RBMKDialsSavedData} und werden
 * ueber den Befehl /ntmrbmk gelesen und gesetzt. Die Klemmung der Werte entspricht dem Original
 * (dort GameRuleHelper).
 */
public class RBMKDials {

    public enum RBMKKeys {
        KEY_PASSIVE_COOLING("dialPassiveCooling", 2.5D),
        KEY_PASSIVE_COOLING_INNER("dialPassiveCoolingInner", 0.1D),
        KEY_COLUMN_HEAT_FLOW("dialColumnHeatFlow", 0.2D),
        KEY_FUEL_DIFFUSION_MOD("dialDiffusionMod", 1.0D),
        KEY_HEAT_PROVISION("dialHeatProvision", 0.2D),
        KEY_COLUMN_HEIGHT("dialColumnHeight", 4),
        KEY_PERMANENT_SCRAP("dialEnablePermaScrap", true),
        KEY_BOILER_HEAT_CONSUMPTION("dialBoilerHeatConsumption", 0.1D),
        KEY_CONTROL_SPEED_MOD("dialControlSpeed", 1.0D),
        KEY_REACTIVITY_MOD("dialReactivityMod", 1.0D),
        KEY_OUTGASSER_MOD("dialOutgasserSpeedMod", 1.0D),
        KEY_SURGE_MOD("dialControlSurgeMod", 1.0D),
        KEY_FLUX_RANGE("dialFluxRange", 5),
        KEY_REASIM_RANGE("dialReasimRange", 10),
        KEY_REASIM_BOILERS("dialReasimBoilers", false),
        KEY_REASIM_BOILER_SPEED("dialReasimBoilerSpeed", 0.05D),
        KEY_DISABLE_MELTDOWNS("dialDisableMeltdowns", false),
        KEY_ENABLE_MELTDOWN_OVERPRESSURE("dialEnableMeltdownOverpressure", false),
        KEY_MODERATOR_EFFICIENCY("dialModeratorEfficiency", 1.0D),
        KEY_ABSORBER_EFFICIENCY("dialAbsorberEfficiency", 1.0D),
        KEY_REFLECTOR_EFFICIENCY("dialReflectorEfficiency", 1.0D),
        KEY_DISABLE_DEPLETION("dialDisableDepletion", false),
        KEY_DISABLE_XENON("dialDisableXenon", false),
        KEY_ABSORBER_HEAT_CONVERSION("dialAbsorberHeatConversion", 0.05D);

        public final String keyString;
        public final Object defValue;

        RBMKKeys(String key, Object def) {
            this.keyString = key;
            this.defValue = def;
        }

        public static RBMKKeys byName(String name) {
            for(RBMKKeys key : values()) if(key.keyString.equalsIgnoreCase(name)) return key;
            return null;
        }
    }

    /**
     * Liefert den Speicher der Welt. Auf dem Client gibt es keinen, dort wird ueberall der
     * Standardwert benutzt -- genau wie im Original, das dort ebenfalls auf die Defaults faellt.
     */
    public static RBMKDialsSavedData getData(Level level) {
        if(!(level instanceof ServerLevel server)) return null;
        return server.getDataStorage().computeIfAbsent(RBMKDialsSavedData.factory(), RBMKDialsSavedData.DATA_NAME);
    }

    private static double getDoubleMinimum(Level level, RBMKKeys key, double min) {
        RBMKDialsSavedData data = getData(level);
        double def = (Double) key.defValue;
        if(data == null) return Math.max(def, min);
        return Math.max(data.getDouble(key.keyString, def), min);
    }

    private static double getClampedDouble(Level level, RBMKKeys key, double min, double max) {
        RBMKDialsSavedData data = getData(level);
        double def = (Double) key.defValue;
        if(data == null) return Mth.clamp(def, min, max);
        return Mth.clamp(data.getDouble(key.keyString, def), min, max);
    }

    private static int getClampedInt(Level level, RBMKKeys key, int min, int max) {
        RBMKDialsSavedData data = getData(level);
        int def = (Integer) key.defValue;
        if(data == null) return Mth.clamp(def, min, max);
        return Mth.clamp(data.getInt(key.keyString, def), min, max);
    }

    private static boolean getBoolean(Level level, RBMKKeys key) {
        RBMKDialsSavedData data = getData(level);
        boolean def = (Boolean) key.defValue;
        if(data == null) return def;
        return data.getBoolean(key.keyString, def);
    }

    /** Waermeverlust pro Tick am Rand des Reaktors. */
    public static double getPassiveCooling(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_PASSIVE_COOLING, 0.0D);
    }

    /** Waermeverlust pro Tick mitten im Reaktor. */
    public static double getPassiveCoolingInner(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_PASSIVE_COOLING_INNER, 0.0D);
    }

    /** Schrittweite des Waermeausgleichs zwischen Nachbarsaeulen, 1 waere sofortiger Ausgleich. */
    public static double getColumnHeatFlow(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_COLUMN_HEAT_FLOW, 0.0D, 1.0D);
    }

    /** Faktor fuer den Waermeausgleich zwischen Kern und Huelle eines Brennstabs. */
    public static double getFuelDiffusionMod(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_FUEL_DIFFUSION_MOD, 0.0D);
    }

    /** Schrittweite des Waermeausgleichs zwischen Huelle und Saeule. */
    public static double getFuelHeatProvision(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_HEAT_PROVISION, 0.0D, 1.0D);
    }

    /** Anzahl der Bloecke ueber dem Kernblock. Das Original zieht hier bewusst eins ab. */
    public static int getColumnHeight(Level level) {
        return getClampedInt(level, RBMKKeys.KEY_COLUMN_HEIGHT, 2, 16) - 1;
    }

    /** Ob Schrott-Entitaeten liegen bleiben statt zu despawnen. */
    public static boolean getPermaScrap(Level level) {
        return getBoolean(level, RBMKKeys.KEY_PERMANENT_SCRAP);
    }

    /** Waermeeinheiten pro mB verdampftem Wasser. */
    public static double getBoilerHeatConsumption(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_BOILER_HEAT_CONSUMPTION, 0.0D);
    }

    /** Faktor fuer die Fahrgeschwindigkeit der Steuerstaebe. */
    public static double getControlSpeed(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_CONTROL_SPEED_MOD, 0.0D);
    }

    /** Faktor fuer den Fluss, den die Brennstaebe abgeben. */
    public static double getReactivityMod(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_REACTIVITY_MOD, 0.0D);
    }

    /** Faktor fuer die Geschwindigkeit des Outgassers. */
    public static double getOutgasserMod(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_OUTGASSER_MOD, 0.0D);
    }

    /** Faktor fuer die Leistungsspitze beim Einfahren der Steuerstaebe. */
    public static double getSurgeMod(Level level) {
        return getDoubleMinimum(level, RBMKKeys.KEY_SURGE_MOD, 0.0D);
    }

    /** Reichweite des Flusses eines normalen Brennstabs. */
    public static int getFluxRange(Level level) {
        return getClampedInt(level, RBMKKeys.KEY_FLUX_RANGE, 1, 100);
    }

    /** Reichweite des Flusses eines ReaSim-Brennstabs. */
    public static int getReaSimRange(Level level) {
        return getClampedInt(level, RBMKKeys.KEY_REASIM_RANGE, 1, 100);
    }

    /** Ob sich alle Saeulen wie Dampferzeuger verhalten. */
    public static boolean getReasimBoilers(Level level) {
        return getBoolean(level, RBMKKeys.KEY_REASIM_BOILERS);
    }

    /** Anteil des moeglichen Dampfes, der pro Tick tatsaechlich entsteht. */
    public static double getReaSimBoilerSpeed(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_REASIM_BOILER_SPEED, 0.0D, 1.0D);
    }

    /** Ob ueberhitzte Brennstabsaeulen eine Kernschmelze ausloesen. Umgekehrt formuliert wie im Original. */
    public static boolean getMeltdownsDisabled(Level level) {
        return getBoolean(level, RBMKKeys.KEY_DISABLE_MELTDOWNS);
    }

    /** Ob angeschlossene Rohre und Turbinen bei der Kernschmelze mitgehen. */
    public static boolean getOverpressure(Level level) {
        return getBoolean(level, RBMKKeys.KEY_ENABLE_MELTDOWN_OVERPRESSURE);
    }

    /** Anteil der Neutronen, die ein Moderator von schnell auf langsam bremst. */
    public static double getModeratorEfficiency(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_MODERATOR_EFFICIENCY, 0.0D, 1.0D);
    }

    /** Anteil der Neutronen, die ein Absorber schluckt. */
    public static double getAbsorberEfficiency(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_ABSORBER_EFFICIENCY, 0.0D, 1.0D);
    }

    /** Grad Celsius pro Flusseinheit, die auf einen Absorber trifft. */
    public static double getAbsorberHeatConversion(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_ABSORBER_HEAT_CONVERSION, 0.0D, 1.0D);
    }

    /** Anteil der Neutronen, die ein Reflektor zurueckwirft. */
    public static double getReflectorEfficiency(Level level) {
        return getClampedDouble(level, RBMKKeys.KEY_REFLECTOR_EFFICIENCY, 0.0D, 1.0D);
    }

    /** Ob Brennstaebe abbrennen. */
    public static boolean getDepletion(Level level) {
        return !getBoolean(level, RBMKKeys.KEY_DISABLE_DEPLETION);
    }

    /** Ob Xenonvergiftung berechnet wird. */
    public static boolean getXenon(Level level) {
        return !getBoolean(level, RBMKKeys.KEY_DISABLE_XENON);
    }
}
