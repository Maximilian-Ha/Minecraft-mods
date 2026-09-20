package com.hbm.itempool;

import com.hbm.blockentity.LootDecoBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.ItemEnums.SecretType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.ModSpecial;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.itempool.ItemPoolsRedRoom.
 *
 * Was in den Zimmern hinter den Schluessellochbloecken liegt. Drei Vorraete: der Sockel des
 * steinernen Zimmers, die Tafel des Ziegelzimmers und dessen vier Beisockel.
 *
 * DER ERSTE VORRAT IST FAST VOLLSTAENDIG, und was fehlt, steht hier statt im Verborgenen.
 * Von den fuenfundzwanzig Eintraegen des Originals stehen seit Runde 241 dreiundzwanzig;
 * ZWEI Gegenstaende gibt es im Port nicht -- gemessen ueber den Registriernamen, nicht
 * ueber den Feldnamen:
 *
 *   starmetal_sword, flask_infusion
 *
 * Die zehn Ruestungsaufsaetze, die bis Runde 240 gefehlt haben, sind nachgekommen und mit
 * ihren Gewichten aus dem Original eingetragen. Die Gewichte der uebrigen bleiben, wie sie
 * sind: sie umzurechnen, damit die Summe wieder stimmt, waere eine Erfindung, und sobald
 * die zwei nachkommen, muesste man zweimal umrechnen. Was fehlt, fehlt sichtbar.
 *
 * DER ZWEITE VORRAT stand in Runde 233 noch leer da: er besteht im Original aus einem
 * einzigen Eintrag, der Tontafel, und die kam erst in Runde 234. Jetzt ist er vollstaendig.
 */
public class ItemPoolsRedRoom {

    public static final String POOL_RED_PEDESTAL = "POOL_RED_PEDESTAL";
    public static final String POOL_BLACK_SLAB = "POOL_BLACK_SLAB";
    public static final String POOL_BLACK_PART = "POOL_BLACK_PART";

    private static boolean initialized = false;

    public static void init() {

        /* Wie bei den anderen Vorraeten: ein zweiter Aufruf wuerde alles doppelt eintragen. */
        if(initialized) return;
        initialized = true;

        ItemPool.getOrCreate(POOL_RED_PEDESTAL)
                .add(NtmItems.BALLISTIC_GAUNTLET.get(), 1, 1, 10)
                .add(NtmItems.ARMOR_POLISH.get(), 1, 1, 10)
                .add(NtmItems.BANDAID.get(), 1, 1, 10)
                .add(NtmItems.SERUM.get(), 1, 1, 10)
                .add(NtmItems.QUARTZ_PLUTONIUM.get(), 1, 1, 10)
                .add(NtmItems.MORNING_GLORY.get(), 1, 1, 10)
                .add(NtmItems.SPIDER_MILK.get(), 1, 1, 10)
                .add(NtmItems.INK.get(), 1, 1, 10)
                .add(NtmItems.HEART_CONTAINER.get(), 1, 1, 10)
                .add(NtmItems.BLACK_DIAMOND.get(), 1, 1, 10)
                .add(NtmItems.SCRUMPY.get(), 1, 1, 10)

                .add(NtmItems.WILD_P.get(), 1, 1, 5)
                .add(NtmItems.CARD_AOS.get(), 1, 1, 5)
                .add(NtmItems.CARD_QOS.get(), 1, 1, 5)
                .add(NtmItems.GEM_ALEXANDRITE.get(), 1, 1, 5)
                .add(NtmItems.CRACKPIPE.get(), 1, 1, 5)
                .add(NtmBlocks.BOXCAR.get(), 1, 1, 5)
                .add(NtmItems.BOOK_OF_.get(), 1, 1, 5)

                .add(NtmItems.GUN_HANGMAN.get(), 1, 1, 1)
                .add(NtmItems.GUN_MAS36.get(), 1, 1, 1)
                .add(() -> MetaHelper.newStack(NtmItems.ITEM_SECRET.get(), 1, SecretType.FOLLY.ordinal()), 1)
                .add(() -> MetaHelper.newStack(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, ModSpecial.NICKEL.ordinal()), 1)
                .add(() -> MetaHelper.newStack(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, ModSpecial.DOUBLOONS.ordinal()), 1);

        /* Die Tafel in der Mitte des schwarzen Zimmers. Sie ist im Original der einzige
         * Eintrag dieses Vorrats, und seit Runde 234 gibt es sie. */
        ItemPool.getOrCreate(POOL_BLACK_SLAB)
                .add(NtmItems.CLAY_TABLET.get(), 1, 1, 10);

        /* Die vier Beisockel: im Original dieselben Geheimstuecke, die auch der Sockel des
         * Rituals braucht. Sie sind der einzige Weg, an ein Aberrator-Teil zu kommen. */
        ItemPool.getOrCreate(POOL_BLACK_PART)
                .add(() -> MetaHelper.newStack(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR.ordinal()), 10)
                .add(() -> MetaHelper.newStack(NtmItems.ITEM_SECRET.get(), 1, SecretType.CONTROLLER.ordinal()), 5)
                .add(() -> MetaHelper.newStack(NtmItems.ITEM_SECRET.get(), 1, SecretType.FOLLY.ordinal()), 5);
    }

    /**
     * Die Beutekiste statt des Sockels, einer von zwanzig. Sie traegt einen der beiden
     * Panzerruestungssaetze -- mit einem Fuenftel die NCRPA, sonst den Grabenmeister. Die
     * Versatzwerte sind die des Originals: alle vier Teile liegen aufeinander.
     */
    public static void fuelleBeutekiste(Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof LootDecoBlockEntity kiste)) return;

        boolean ncrpa = level.random.nextInt(5) == 0;

        kiste.addItem(new ItemStack(ncrpa ? NtmItems.NCRPA_HELMET.get() : NtmItems.TRENCHMASTER_HELMET.get()), 0, 0, 0)
             .addItem(new ItemStack(ncrpa ? NtmItems.NCRPA_PLATE.get() : NtmItems.TRENCHMASTER_PLATE.get()), 0, 0, 0)
             .addItem(new ItemStack(ncrpa ? NtmItems.NCRPA_LEGS.get() : NtmItems.TRENCHMASTER_LEGS.get()), 0, 0, 0)
             .addItem(new ItemStack(ncrpa ? NtmItems.NCRPA_BOOTS.get() : NtmItems.TRENCHMASTER_BOOTS.get()), 0, 0, 0);

        kiste.setChanged();
    }
}
