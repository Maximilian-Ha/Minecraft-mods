package com.zuxelus.energycontrol;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.EnergyControlConfig.
 *
 * Uebernommen sind die Einstellungen, die es im Port noch gibt. Alles, was an IC2 hing
 * (Stromverbrauch der Tafeln, Reaktorwerte), faellt weg: die Tafeln dieses Ports brauchen
 * keinen Strom.
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

    public static int infoPanelRefreshPeriod() {
        return COMMON.INFO_PANEL_REFRESH_PERIOD.get();
    }

    public static int rangeUpgradeRange() {
        return COMMON.RANGE_UPGRADE_RANGE.get();
    }

    public static int maxAlarmRange() {
        return COMMON.MAX_ALARM_RANGE.get();
    }

    public static class Common {

        public final ModConfigSpec.IntValue INFO_PANEL_REFRESH_PERIOD;
        public final ModConfigSpec.IntValue RANGE_UPGRADE_RANGE;
        public final ModConfigSpec.IntValue MAX_ALARM_RANGE;

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
        }
    }
}
