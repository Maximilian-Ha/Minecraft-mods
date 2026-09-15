package com.hbm.itempool;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import net.minecraft.world.item.Items;

/**
 * Portiert aus 1.7.10: com.hbm.itempool.ItemPoolsSatellite.
 *
 * Was die beiden Bergbausatelliten heraufschaufeln. Gewichte und Stueckzahlen sind die des
 * Originals, Eintrag fuer Eintrag.
 *
 * DER ASTEROIDENSCHUERFER liefert Metallstaube und Kristalle, der MONDSCHUERFER vor allem
 * Mondstaub -- dreimal derselbe Eintrag mit sinkender Menge, damit grosse Ladungen seltener
 * sind als kleine. Das ist im Original genauso.
 */
public class ItemPoolsSatellite {

    public static final String POOL_SAT_MINER = "POOL_SAT_MINER";
    public static final String POOL_SAT_LUNAR = "POOL_SAT_LUNAR";

    private static boolean initialized = false;

    public static void init() {

        /* Die Vorraete werden angehaengt, nicht ersetzt -- ein zweiter Aufruf wuerde alles
         * doppelt eintragen. */
        if(initialized) return;
        initialized = true;

        ItemPool.getOrCreate(POOL_SAT_MINER)
                .add(NtmItems.POWDER_ALUMINIUM.get(), 3, 3, 10)
                .add(NtmItems.POWDER_IRON.get(), 3, 3, 10)
                .add(NtmItems.POWDER_TITANIUM.get(), 2, 2, 8)
                .add(NtmItems.CRYSTAL_TUNGSTEN.get(), 2, 2, 7)
                .add(NtmItems.POWDER_COAL.get(), 4, 4, 15)
                .add(NtmItems.POWDER_URANIUM.get(), 2, 2, 5)
                .add(NtmItems.POWDER_PLUTONIUM.get(), 1, 1, 5)
                .add(NtmItems.POWDER_THORIUM.get(), 2, 2, 7)
                .add(NtmItems.POWDER_DESH_MIX.get(), 3, 3, 5)
                .add(NtmItems.POWDER_DIAMOND.get(), 2, 2, 7)
                .add(Items.REDSTONE, 5, 5, 15)
                .add(NtmItems.POWDER_NITAN_MIX.get(), 2, 2, 5)
                .add(NtmItems.POWDER_POWER.get(), 2, 2, 5)
                .add(NtmItems.POWDER_COPPER.get(), 5, 5, 15)
                .add(NtmItems.POWDER_LEAD.get(), 3, 3, 10)
                .add(NtmItems.FLUORITE.get(), 4, 4, 15)
                .add(NtmItems.POWDER_LAPIS.get(), 4, 4, 10)
                .add(NtmItems.CRYSTAL_ALUMINIUM.get(), 1, 1, 5)
                .add(NtmItems.CRYSTAL_GOLD.get(), 1, 1, 5)
                .add(NtmItems.CRYSTAL_PHOSPHORUS.get(), 1, 1, 10)
                .add(NtmBlocks.GRAVEL_DIAMOND.get(), 1, 1, 3)
                .add(NtmItems.CRYSTAL_URANIUM.get(), 1, 1, 3)
                .add(NtmItems.CRYSTAL_PLUTONIUM.get(), 1, 1, 3)
                .add(NtmItems.CRYSTAL_TRIXITE.get(), 1, 1, 1)
                .add(NtmItems.CRYSTAL_STARMETAL.get(), 1, 1, 1)
                .add(NtmItems.CRYSTAL_LITHIUM.get(), 2, 2, 4);

        ItemPool.getOrCreate(POOL_SAT_LUNAR)
                .add(NtmBlocks.MOON_TURF.get(), 48, 48, 5)
                .add(NtmBlocks.MOON_TURF.get(), 32, 32, 7)
                .add(NtmBlocks.MOON_TURF.get(), 16, 16, 5)
                .add(NtmItems.POWDER_LITHIUM.get(), 3, 3, 5)
                .add(NtmItems.POWDER_IRON.get(), 3, 3, 5)
                .add(NtmItems.CRYSTAL_IRON.get(), 1, 1, 1)
                .add(NtmItems.CRYSTAL_LITHIUM.get(), 1, 1, 1);
    }
}
