package com.zuxelus.energycontrol.crossmod.mekanism;

import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IEnergyConversionHelper;
import net.minecraft.nbt.CompoundTag;

/**
 * Rechnet Mekanisms Joule in Forge-Energie um.
 *
 * Mekanism zaehlt intern in Joule; jede Zahl, die {@code IStrictEnergyHandler},
 * {@code IEnergyContainer} oder ein Mehrblockbau herausgibt, ist eine Joule-Zahl. Auf der
 * Tafel steht aber eine Zahl neben denen anderer Mods, und die rechnen in Forge-Energie --
 * auch die Anzeige von Mekanism selbst, wenn man sie umstellt. Eine Tafel, die "J" schreibt,
 * wo das Kabel daneben FE fuehrt, ist darum irrefuehrend.
 *
 * Der Umrechnungsfaktor ist bei Mekanism einstellbar (Vorgabe 2,5 J je FE), er darf also
 * nicht fest im Quelltext stehen. {@code IEnergyConversionHelper} ist Mekanisms
 * oeffentlicher Zugang zu genau diesem Wert; er liest die gleiche Einstellung, die auch
 * Mekanisms eigene Kabel und Anzeigen verwenden.
 *
 * Wer die Forge-Energie bei Mekanism abschaltet ({@code blacklistForge}), bekommt weiter
 * Joule zu sehen -- dann gibt es im Spiel keine Forge-Energie, in die umzurechnen waere.
 */
final class MekEnergy {

    private MekEnergy() { }

    private static final String JOULES = "J";
    private static final String FORGE_ENERGY = "FE";

    /** Das Kuerzel, das auf der Tafel hinter den Zahlen steht. */
    static String unit() {
        return conversion().isEnabled() ? FORGE_ENERGY : JOULES;
    }

    /** Eine Joule-Zahl in der Einheit, die {@link #unit()} nennt. */
    static double convert(long joules) {
        IEnergyConversion fe = conversion();
        return fe.isEnabled() ? fe.convertToDouble(joules) : joules;
    }

    /**
     * Schreibt eine Joule-Zahl umgerechnet in den Beutel. Als Gleitkommazahl, denn bei
     * 2,5 J je FE geht jede zweite Zahl nicht glatt auf.
     */
    static void put(CompoundTag tag, String key, long joules) {
        tag.putDouble(key, convert(joules));
    }

    private static IEnergyConversion conversion() {
        return IEnergyConversionHelper.INSTANCE.feConversion();
    }
}
