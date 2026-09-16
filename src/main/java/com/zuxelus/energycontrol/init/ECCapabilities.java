package com.zuxelus.energycontrol.init;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Meldet den Stromspeicher von Tafel und Bereichsmelder als Forge-Energie an.
 *
 * Damit findet jede Leitung, die Forge-Energie fuehrt, den Anschluss -- ohne dass hier eine
 * Zeile je Mod steht. Abgegeben wird nichts: {@code ECEnergyStorage} laesst nichts entnehmen.
 */
public class ECCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ECBlockEntityTypes.INFO_PANEL.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ECBlockEntityTypes.RANGE_TRIGGER.get(),
                (be, side) -> be.getEnergyStorage());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ECBlockEntityTypes.KIT_ASSEMBLER.get(),
                (be, side) -> be.getEnergyStorage());

        // Der Zaehler nimmt auf allen Seiten ausser der Schauseite an; abgeben laesst er
        // sich nirgends -- er schiebt selbst weiter.
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ECBlockEntityTypes.ENERGY_COUNTER.get(),
                (be, side) -> be.getEnergyStorage(side));
    }
}
