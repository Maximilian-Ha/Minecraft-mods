package com.hbm.itempool;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;

/**
 * Portiert aus 1.7.10: die beiden Vorraete des Meteoritenverlieses, im Original verstreut --
 * POOL_METEORITE_TREASURE steht in ItemPoolsSingle, POOL_METEOR_SAFE in ItemPoolsComponent.
 * Hier stehen sie beieinander, weil sie zusammengehoeren: der eine fuellt die Truhen der
 * Drachenkammern, der andere den Tresor im Buecherstueck.
 *
 * DER TRESOR IST IM PORT EINE TRUHE. Den Block hbm:tile.safe gibt es hier noch nicht; er ist
 * im Original ein abschliessbarer Behaelter mit eigener Oberflaeche. Bis dahin steht an seiner
 * Stelle eine Vanilla-Truhe mit demselben Vorrat -- die Beute ist erreichbar, das Schloss
 * fehlt.
 *
 * WAS AUS DEN VORRAETEN FEHLT, steht namentlich an der jeweiligen Stelle. Kein Eintrag
 * verschwindet stillschweigend.
 */
public class ItemPoolsMeteor {

    public static final String POOL_METEORITE_TREASURE = "POOL_METEORITE_TREASURE";
    public static final String POOL_METEOR_SAFE = "POOL_METEOR_SAFE";

    private static boolean initialized = false;

    public static void init() {

        if(initialized) return;
        initialized = true;

        /*
         * DIE SCHATZTRUHE. Sechs bis zwoelf Zuege je Truhe -- das legt der Beutestab fest,
         * nicht der Vorrat.
         *
         * Es fehlen vier der zwanzig Eintraege, weil es die Gegenstaende im Port noch nicht
         * gibt: pill_herbal, heart_piece, egg_glyphid und blueprint_folder (Spielart 1).
         * Ihr Gewicht faellt weg, die uebrigen ziehen entsprechend haeufiger.
         */
        ItemPool.getOrCreate(POOL_METEORITE_TREASURE)
                .add(NtmItems.COBALT_PICKAXE.get(), 1, 1, 10)
                .add(NtmItems.INGOT_ZIRCONIUM.get(), 1, 16, 10)
                .add(NtmItems.INGOT_NIOBIUM.get(), 1, 16, 10)
                .add(NtmItems.INGOT_COBALT.get(), 1, 16, 10)
                .add(NtmItems.INGOT_BORON.get(), 1, 16, 10)
                .add(NtmItems.INGOT_STARMETAL.get(), 1, 1, 5)
                .add(NtmItems.CRYSTAL_GOLD.get(), 1, 4, 10)
                .add(NtmItems.CIRCUIT_VACUUM_TUBE.get(), 4, 8, 10)
                .add(NtmItems.CIRCUIT_MICROCHIP.get(), 2, 4, 10)
                .add(NtmItems.DEFINITELYFOOD.get(), 16, 32, 25)
                .add(NtmBlocks.CRATE_CAN.get(), 1, 3, 10)
                .add(NtmItems.SERUM.get(), 1, 1, 5)
                .add(NtmItems.SCRUMPY.get(), 1, 1, 5)
                .add(NtmItems.LAUNCH_CODE_PIECE.get(), 1, 1, 5)
                .add(NtmItems.GEM_ALEXANDRITE.get(), 1, 1, 1);

        /*
         * DER TRESOR. Zwei bis drei Zuege.
         *
         * Im Original neun Eintraege: das Buch und acht Spielarten des Stempelbuchs. Die
         * Stempelbuecher gibt es im Port nicht -- damit bleibt ein einziger Eintrag, und der
         * Tresor gibt immer dasselbe her. Das ist keine Erfindung, sondern der Rest, der von
         * dem Vorrat heute uebrig ist; sobald die Stempelbuecher kommen, gehoeren sie hierher.
         */
        ItemPool.getOrCreate(POOL_METEOR_SAFE)
                .add(NtmItems.BOOK_OF_.get(), 1, 1, 1);
    }
}
