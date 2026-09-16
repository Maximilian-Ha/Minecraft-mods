package com.zuxelus.energycontrol;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.EnergyControlConfig.
 *
 * Alle Zugriffe gehen ueber die Methoden unten, nicht ueber die Felder. Der Grund steht an
 * {@link #value}: eine Einstellung, die noch nicht geladen ist, wirft.
 */
public final class ECConfig {

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        Pair<Common, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    private ECConfig() { }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    /**
     * Liest eine Einstellung und faellt auf den Vorgabewert zurueck, solange die Datei noch
     * nicht geladen ist. Ohne das wirft jeder Zugriff vor dem Laden eine IllegalStateException
     * -- und die Datengeneratoren laufen ohne geladene Konfiguration.
     */
    private static <T> T value(ModConfigSpec.ConfigValue<T> config, T fallback) {
        try {
            T current = config.get();
            return current != null ? current : fallback;
        } catch(IllegalStateException e) {
            return fallback;
        }
    }

    public static int infoPanelRefreshPeriod() { return value(COMMON.INFO_PANEL_REFRESH_PERIOD, 20); }

    public static int rangeUpgradeRange() { return value(COMMON.RANGE_UPGRADE_RANGE, 8); }

    public static int maxAlarmRange() { return value(COMMON.MAX_ALARM_RANGE, 64); }

    /** Ob Tafeln und Bereichsmelder ueberhaupt Strom brauchen. */
    public static boolean requirePower() { return value(COMMON.REQUIRE_POWER, true); }

    /** Verbrauch eines Kartenlesers in FE je Tick. */
    public static int energyConsumption() { return value(COMMON.ENERGY_CONSUMPTION, 2); }

    /** Fassungsvermoegen des eingebauten Stromspeichers in FE. */
    public static int energyCapacity() { return value(COMMON.ENERGY_CAPACITY, 10000); }

    /** Wieviel FE je Tick der Energiezaehler hoechstens durchleitet. */
    public static int counterTransferRate() { return value(COMMON.COUNTER_TRANSFER_RATE, 32000); }

    /** Was die Bausatzmontage je Tick verbraucht, solange sie arbeitet. */
    public static int assemblerConsumption() { return value(COMMON.ASSEMBLER_CONSUMPTION, 20); }

    /** Fassungsvermoegen des Stromspeichers der Bausatzmontage in FE. */
    public static int assemblerCapacity() { return value(COMMON.ASSEMBLER_CAPACITY, 20000); }

    public static class Common {

        public final ModConfigSpec.IntValue INFO_PANEL_REFRESH_PERIOD;
        public final ModConfigSpec.IntValue RANGE_UPGRADE_RANGE;
        public final ModConfigSpec.IntValue MAX_ALARM_RANGE;
        public final ModConfigSpec.BooleanValue REQUIRE_POWER;
        public final ModConfigSpec.IntValue ENERGY_CONSUMPTION;
        public final ModConfigSpec.IntValue ENERGY_CAPACITY;
        public final ModConfigSpec.IntValue COUNTER_TRANSFER_RATE;
        public final ModConfigSpec.IntValue ASSEMBLER_CONSUMPTION;
        public final ModConfigSpec.IntValue ASSEMBLER_CAPACITY;

        Common(ModConfigSpec.Builder builder) {
            builder.push("general");

            INFO_PANEL_REFRESH_PERIOD = builder
                    .comment("Ticks zwischen zwei Messungen einer Informationstafel.")
                    .defineInRange("infoPanelRefreshPeriod", 20, 1, 1200);

            RANGE_UPGRADE_RANGE = builder
                    .comment("Zusaetzliche Bloecke Reichweite je Reichweitenaufwertung.")
                    .defineInRange("rangeUpgradeRange", 8, 1, 256);

            MAX_ALARM_RANGE = builder
                    .comment("Groesste einstellbare Hoerweite des Heulers.")
                    .defineInRange("maxAlarmRange", 64, 16, 256);

            builder.pop();
            builder.push("power");

            REQUIRE_POWER = builder
                    .comment("Ob Informationstafel und Bereichsmelder Strom brauchen.",
                             "Aus: beide arbeiten ohne Anschluss, wie in der ersten Fassung dieses Ports.")
                    .define("requirePower", true);

            ENERGY_CONSUMPTION = builder
                    .comment("Verbrauch in FE je Tick, solange ein Kartenleser laeuft.")
                    .defineInRange("energyConsumption", 2, 0, 100000);

            ENERGY_CAPACITY = builder
                    .comment("Fassungsvermoegen des eingebauten Stromspeichers in FE.")
                    .defineInRange("energyCapacity", 10000, 1, 100000000);

            COUNTER_TRANSFER_RATE = builder
                    .comment("Wieviel FE je Tick der Energiezaehler hoechstens durchleitet.",
                             "Hoeher heisst nur, dass er nicht bremst -- gezaehlt wird, was wirklich durchgeht.")
                    .defineInRange("counterTransferRate", 32000, 1, 100000000);

            ASSEMBLER_CONSUMPTION = builder
                    .comment("Verbrauch der Bausatzmontage in FE je Tick, solange sie arbeitet.")
                    .defineInRange("assemblerConsumption", 20, 0, 100000);

            ASSEMBLER_CAPACITY = builder
                    .comment("Fassungsvermoegen des Stromspeichers der Bausatzmontage in FE.")
                    .defineInRange("assemblerCapacity", 20000, 1, 100000000);

            builder.pop();
        }
    }
}
