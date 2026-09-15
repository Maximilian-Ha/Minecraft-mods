package com.hbm.saveddata.satellite;

import com.hbm.itempool.ItemPoolsSatellite;

/**
 * Portiert aus 1.7.10: com.hbm.saveddata.satellites.SatelliteLunarMiner.
 *
 * Derselbe Schuerfer, anderer Vorrat: der Mondschuerfer bringt vor allem Mondstaub herauf.
 */
public class SatelliteLunarMiner extends SatelliteMiner {

    @Override public String getType() { return "LUNAR_MINER"; }

    @Override protected String getCargo() { return ItemPoolsSatellite.POOL_SAT_LUNAR; }
}
