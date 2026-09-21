package com.hbm.itempool;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;

import net.minecraft.world.item.Items;

/**
 * Portiert aus 1.7.10: com.hbm.itempool.ItemPoolsPile.
 *
 * Die Vorraete, aus denen der Beutegenerator seine HAUFEN baut -- kein Behaelter, sondern
 * Gerumpel auf einem Beutesockel: ein paar Kronkorken, zwei Spritzen, eine Handvoll Pillen.
 *
 * Von den zwoelf Vorraeten des Originals stehen hier FUENF: die drei des
 * Meteoritenverlieses und, seit Runde 303, die beiden des Glyphidenbaus. Die uebrigen sieben
 * gehoeren zu Bauwerken, die der Port noch nicht baut, und stuenden hier ohne Abnehmer.
 */
public class ItemPoolsPile {

    public static final String POOL_PILE_HIVE = "POOL_PILE_HIVE";
    public static final String POOL_PILE_BONES = "POOL_PILE_BONES";
    public static final String POOL_PILE_CAPS = "POOL_PILE_CAPS";
    public static final String POOL_PILE_MED_SYRINGE = "POOL_PILE_MED_SYRINGE";
    public static final String POOL_PILE_MED_PILLS = "POOL_PILE_MED_PILLS";

    private static boolean initialized = false;

    public static void init() {

        if(initialized) return;
        initialized = true;

        /*
         * DER GLYPHIDENBAU, Runde 303. Die Gewichte sind die des Originals; was der Port
         * nicht hat, steht nicht drin: scrap, bottle_nuka, bottle_quantum und die beiden
         * Universalgranaten. Zusammen waeren sie knapp ein Viertel des Vorrats.
         */
        ItemPool.getOrCreate(POOL_PILE_HIVE)
                .add(Items.IRON_INGOT, 1, 3, 10)
                .add(NtmItems.INGOT_STEEL.get(), 1, 2, 10)
                .add(NtmItems.INGOT_ALUMINIUM.get(), 1, 2, 10)
                .add(NtmItems.GAS_MASK_M65.get(), 1, 1, 10)
                .add(NtmItems.STEEL_PLATE.get(), 1, 1, 5)
                .add(NtmItems.STEEL_LEGS.get(), 1, 1, 5)
                .add(NtmItems.STEEL_PICKAXE.get(), 1, 1, 5)
                .add(NtmItems.STEEL_SHOVEL.get(), 1, 1, 5)
                .add(NtmItems.GUN_MARESLEG.get(), 1, 1, 5)
                .add(NtmItems.GUN_LIGHT_REVOLVER.get(), 1, 1, 1)
                .add(() -> MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G12.ordinal()), 10)
                .add(() -> MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.M357_SP.ordinal()), 10)
                .add(() -> MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 1, Ammo.G40_HE.ordinal()), 2)
                .add(NtmItems.DEFINITELYFOOD.get(), 5, 12, 20)
                .add(NtmItems.EGG_GLYPHID.get(), 1, 3, 30)
                .add(NtmItems.SYRINGE_METAL_STIMPAK.get(), 1, 1, 5)
                .add(NtmItems.IV_BLOOD.get(), 1, 1, 10)
                .add(Items.EXPERIENCE_BOTTLE, 1, 3, 5);

        /* Der Knochenhaufen in der Kammer. Drei Eintraege, alle vorhanden. */
        ItemPool.getOrCreate(POOL_PILE_BONES)
                .add(Items.BONE, 1, 1, 10)
                .add(Items.ROTTEN_FLESH, 1, 1, 5)
                .add(NtmItems.BIOMASS.get(), 1, 1, 2);

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
