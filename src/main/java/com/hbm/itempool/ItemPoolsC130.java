package com.hbm.itempool;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.itempool.ItemPoolsC130.
 *
 * Was die C-130 abwirft. Drei Vorraete: Nachschub, Waffen und Munition. Die Leuchtpatrone
 * entscheidet, welche gezogen werden -- die blaue holt fuenfmal Nachschub, die gruene ein bis
 * zwei Waffen und sechsmal Munition.
 *
 * WAS NOCH FEHLT, und zwar aus dem Vorrat des Originals, nicht aus Nachlaessigkeit: EIN
 * Eintrag haengt an einem Gegenstand, den der Port noch nicht hat.
 *
 *   Waffen: gun_n_i_4_n_i -- braucht XFactoryAccelerator, das der Port nicht hat
 *
 * BILANZ DIESER LISTE, die mit sieben fehlenden Eintraegen anfing: FUENF davon waren ein
 * Irrtum -- radaway lag nur als Trankeffekt vor, med_bag hatte alle Zutaten, pill_iodine und
 * definitelyfood sind gewoehnliche Nahrung, und der Kanister brauchte nur das Muster des
 * Fasses. Einer, der Henry, war wirklich eine Runde Arbeit. Bleibt einer.
 */
public class ItemPoolsC130 {

    public static final String POOL_SUPPLIES = "POOL_SUPPLIES";
    public static final String POOL_WEAPONS = "POOL_WEAPONS";
    public static final String POOL_AMMO = "POOL_AMMO";

    private static boolean initialized = false;

    public static void init() {

        /* Die Vorraete werden angehaengt, nicht ersetzt -- ein zweiter Aufruf wuerde alles
         * doppelt eintragen. */
        if(initialized) return;
        initialized = true;

        ItemPool.getOrCreate(POOL_SUPPLIES)
                .add(NtmItems.SYRINGE_METAL_STIMPAK.get(), 1, 3, 10)
                .add(NtmBlocks.MACHINE_DIESEL.get(), 1, 1, 1)
                .add(NtmItems.GEIGER_COUNTER.get(), 1, 1, 2)
                .add(NtmItems.RADAWAY.get(), 1, 5, 10)
                .add(NtmItems.MED_BAG.get(), 1, 1, 3)
                .add(NtmItems.PILL_IODINE.get(), 1, 2, 2)
                .add(NtmItems.DEFINITELYFOOD.get(), 3, 10, 25)
                .add(() -> kanister(Fluids.DIESEL), 1, 4, 5);

        ItemPool.getOrCreate(POOL_WEAPONS)
                .add(NtmItems.GUN_LIGHT_REVOLVER.get(), 1, 1, 100)
                .add(NtmItems.GUN_HENRY.get(), 1, 1, 100)
                .add(NtmItems.GUN_MARESLEG.get(), 1, 1, 100)
                .add(NtmItems.GUN_GREASEGUN.get(), 1, 1, 100)
                .add(NtmItems.GUN_CARBINE.get(), 1, 1, 50)
                .add(NtmItems.GUN_HEAVY_REVOLVER.get(), 1, 1, 50)
                .add(NtmItems.GUN_PANZERSCHRECK.get(), 1, 1, 20)
                .add(NtmItems.GUN_DOUBLE_BARREL.get(), 1, 1, 10);

        /* Die Munition liegt im Port als Spielart EINES Gegenstands, deshalb hier ueber
         * Metadatenstapel statt ueber den blossen Gegenstand. */
        ItemPool.getOrCreate(POOL_AMMO)
                .add(() -> munition(Ammo.M357_SP, 12), 10)
                .add(() -> munition(Ammo.M357_FMJ, 6), 10)
                .add(() -> munition(Ammo.M44_SP, 12), 5)
                .add(() -> munition(Ammo.M44_FMJ, 6), 5)
                .add(() -> munition(Ammo.P9_SP, 12), 10)
                .add(() -> munition(Ammo.P9_FMJ, 6), 10)
                .add(() -> munition(Ammo.R762_SP, 6), 5)
                .add(() -> munition(Ammo.G12_BP, 6), 10)
                .add(() -> munition(Ammo.ROCKET_HE, 1), 3)
                .add(NtmItems.AMMO_CONTAINER.get(), 1, 1, 1);
    }

    private static ItemStack munition(Ammo sorte, int menge) {
        return MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), menge, sorte.ordinal());
    }

    /** Ein Kanister mit genau dieser Fluessigkeit. Die Stueckzahl setzt der Vorrat. */
    private static ItemStack kanister(FluidType sorte) {
        return MetaHelper.newStack(NtmItems.CANISTER_FULL.get(), 1, sorte.getID());
    }
}
