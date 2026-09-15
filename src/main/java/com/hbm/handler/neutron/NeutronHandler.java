package com.hbm.handler.neutron;

import com.hbm.blockentity.machine.rbmk.RBMKDials;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Arbeitet einmal pro Server-Tick alle Neutronenstroeme ab. Gemeinsamer Handler fuer alle
 * Systeme, damit nicht jedes seinen eigenen Tick-Hook braucht.
 */
public class NeutronHandler {

    private static int ticks = 0;

    public static void updateSystem(MinecraftServer server) {

        // Den Knoten-Cache alle cacheTime Ticks auffrischen, sonst sammeln sich untaetige Knoten an.
        int cacheTime = 20;
        boolean cacheClear = ticks >= cacheTime;
        if(cacheClear) ticks = 0;
        ticks++;

        NeutronNodeWorld.removeEmptyWorlds();

        for(Map.Entry<Level, NeutronNodeWorld.StreamWorld> world : NeutronNodeWorld.streamWorlds.entrySet()) {

            Level level = world.getKey();

            // Die Dials werden einmal pro Welt und Tick gelesen statt einmal pro Strom.
            RBMKNeutronHandler.reflectorEfficiency = RBMKDials.getReflectorEfficiency(level);
            RBMKNeutronHandler.absorberEfficiency = RBMKDials.getAbsorberEfficiency(level);
            RBMKNeutronHandler.moderatorEfficiency = RBMKDials.getModeratorEfficiency(level);

            // getColumnHeight() liefert die Anzahl der Bloecke ueber dem Kern, hier wird die
            // Gesamthoehe der Saeule gebraucht -- daher das Plus eins. Im Original stand an
            // dieser Stelle jahrelang ein Off-by-one.
            RBMKNeutronHandler.columnHeight = RBMKDials.getColumnHeight(level) + 1;
            RBMKNeutronHandler.fluxRange = RBMKDials.getFluxRange(level);

            world.getValue().runStreamInteractions(level);
            world.getValue().removeAllStreams();

            if(cacheClear) world.getValue().cleanNodes();
        }
    }
}
