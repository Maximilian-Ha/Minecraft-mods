package com.hbm.itempool;

import com.hbm.items.NtmItems;

/**
 * Portiert aus 1.7.10: com.hbm.itempool.ItemPoolsPile.
 *
 * Die Vorraete, aus denen der Beutegenerator seine HAUFEN baut -- kein Behaelter, sondern
 * Gerumpel auf einem Beutesockel: ein paar Kronkorken, zwei Spritzen, eine Handvoll Pillen.
 *
 * NUR DIE DREI VORRAETE DES METEORITENVERLIESES stehen hier. Das Original hat zwoelf; die
 * uebrigen neun gehoeren zu Bauwerken, die der Port noch nicht baut, und wuerden hier ohne
 * Abnehmer stehen.
 */
public class ItemPoolsPile {

    public static final String POOL_PILE_CAPS = "POOL_PILE_CAPS";
    public static final String POOL_PILE_MED_SYRINGE = "POOL_PILE_MED_SYRINGE";
    public static final String POOL_PILE_MED_PILLS = "POOL_PILE_MED_PILLS";

    private static boolean initialized = false;

    public static void init() {

        if(initialized) return;
        initialized = true;

        /* Von den drei Kronkorkensorten hat der Port nur die eine. cap_quantum und
         * cap_sparkle fehlen ihm noch; zusammen waeren sie ein Fuenftel des Vorrats. */
        ItemPool.getOrCreate(POOL_PILE_CAPS)
                .add(NtmItems.CAP_NUKA.get(), 1, 4, 20);

        ItemPool.getOrCreate(POOL_PILE_MED_SYRINGE)
                .add(NtmItems.SYRINGE_METAL_STIMPAK.get(), 1, 1, 10)
                .add(NtmItems.SYRINGE_METAL_MEDX.get(), 1, 1, 5)
                .add(NtmItems.SYRINGE_METAL_PSYCHO.get(), 1, 1, 5);

        /* radx und siox fehlen dem Port. */
        ItemPool.getOrCreate(POOL_PILE_MED_PILLS)
                .add(NtmItems.RADAWAY.get(), 1, 1, 10)
                .add(NtmItems.IV_BLOOD.get(), 1, 1, 15);
    }
}
