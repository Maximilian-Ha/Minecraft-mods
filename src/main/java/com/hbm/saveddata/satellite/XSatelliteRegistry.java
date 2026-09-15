package com.hbm.saveddata.satellite;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.NtmItems;
import com.hbm.items.special.SatelliteItem.SatType;
import com.hbm.saveddata.SatelliteSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XSatelliteRegistry {

    public static final List<Class<? extends SatelliteBase>> satellites = new ArrayList<>();
    public static final Map<ComparableStack, Class<? extends SatelliteBase>> itemToClass = new HashMap<>();

    public static void register() {

        registerSatellite(SatelliteMapper.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.SPY));
        registerSatellite(SatelliteScanner.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.SCANNER));
        registerSatellite(SatelliteRadar.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.RADAR));
        registerSatellite(SatelliteDeathRay.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.DEATH_RAY));
        registerSatellite(SatelliteResonator.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.XENIUM_RESONATOR));
        registerSatellite(SatelliteRelay.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.RELAY));
        /* Bis Runde 129 standen hier zweimal SatelliteRelay als Platzhalter, mit einem "todo"
         * daneben: die beiden Schuerfer gab es im Port nicht, also taten die Gegenstaende so,
         * als waeren sie Relais. Jetzt steht dahinter, was draufsteht.
         *
         * ACHTUNG, DAS VERSCHIEBT DIE KENNZAHLEN: registerSatellite hat die beiden Platzhalter
         * verworfen, weil SatelliteRelay schon in der Liste stand -- die Liste war also zwei
         * Eintraege kuerzer als die Aufrufe. Gespeichert wird der Listenplatz (getID), und alles
         * ab Horizons rueckt jetzt um zwei nach hinten. Aeltere Spielstaende des Ports lesen
         * ihre Satelliten danach falsch. Die Reihenfolge ist die des Originals, und der Port ist
         * noch in Arbeit; die Alternative waere, die Schuerfer ans Ende zu haengen und dauerhaft
         * von der Vorlage abzuweichen. */
        registerSatellite(SatelliteMiner.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.MINER_ASTRO));
        registerSatellite(SatelliteLunarMiner.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.MINER_LUNAR));
        registerSatellite(SatelliteHorizons.class, NtmItems.SAT_GERALD.get());
        registerSatellite(SatellitePrecisionLaser.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.PRECISION_LASER));
        registerSatellite(SatelliteDetector.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.DETECTOR));
        registerSatellite(SatelliteScience.class, new ComparableStack(NtmItems.SATELLITE.get(), 1, SatType.SCIENCE));

    }

    /**
     * Register satellite.
     * @param sat - Satellite class
     * @param item - Satellite item (which will be placed in a rocket)
     */
    @Deprecated
    public static void registerSatellite(Class<? extends SatelliteBase> sat, Item item) {
        if(!itemToClass.containsKey(item) && !itemToClass.containsValue(sat)) {
            satellites.add(sat);
            itemToClass.put(new ComparableStack(item), sat);
        }
    }

    public static void registerSatellite(Class<? extends SatelliteBase> sat, ComparableStack item) {
        if(!itemToClass.containsKey(item) && !itemToClass.containsValue(sat)) {
            satellites.add(sat);
            itemToClass.put(item, sat);
        }
    }

    public static void orbit(ServerLevel level, ItemStack stack, int freq, double x, double y, double z) {
        if(level.isClientSide) return;

        SatelliteBase sat = createFromItem(stack);

        if(sat != null) {
            SatelliteSavedData data = SatelliteSavedData.getData(level);
            data.sats.put(freq, sat);
            sat.onOrbit(level, x, y, z);
            data.setDirty();
        }
    }

    public static SatelliteBase createFromId(int i) {
        try {
            return satellites.get(i).newInstance();
        } catch(Exception e) { }
        return null;
    }

    public static SatelliteBase createFromItem(ItemStack stack) {
        try {
            return itemToClass.get(new ComparableStack(stack).makeSingular()).newInstance();
        } catch(Exception e) { }
        return null;
    }
}
