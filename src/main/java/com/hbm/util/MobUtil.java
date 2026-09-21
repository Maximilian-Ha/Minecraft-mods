package com.hbm.util;

import com.hbm.entity.ai.FireGunGoal;
import com.hbm.items.NtmItems;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.util.MobUtil, soweit der Port es braucht.
 *
 * WOZU: die Mob-Aktionen des Logikstabs setzen keine nackten Zombies und Skelette in die
 * Welt, sondern ausgeruestete. Woraus die Ausruestung gezogen wird, steht in gewichteten
 * Listen je Ruestungsteil -- ein Helm aus der Helmliste, eine Waffe aus der Waffenliste.
 *
 * VIER LISTEN, NICHT ACHT. Das Original fuehrt acht Pools; vier davon gehoeren zu den
 * Russ-Mobs und dem Gob-Block, die der Port nicht hat. Hier stehen nur die, die die fuenf
 * Aktionen des Logikstabs benutzen:
 *
 *   GEWOEHNLICH     der einfache Zombie -- Helm bis Stiefel und etwas in der Hand
 *   FORTGESCHRITTEN der zaehe Zombie -- Schutzanzuege statt Lumpen
 *   FERNKAMPF       die Ruestung der Schuetzen (ohne Hand, die fuellt die Waffenliste)
 *   WAFFEN 1/2/3    je eine Waffenliste, aufsteigend
 *
 * DIE MASKENLISTE FEHLT ABSICHTLICH. Im Original heisst sie slotPoolMasks, steht in
 * SKELETONS_GUN_TIER_1 und wird NIE GEFUELLT -- keine einzige put()-Zeile im ganzen
 * Quelltext (nachgemessen: zwei Vorkommen, die Erklaerung und der Aufruf). Der Aufruf dort
 * laeuft also ueber eine leere Abbildung und tut nichts. Hier steht er darum gar nicht erst.
 *
 * NEUN EINTRAEGE ENTFALLEN, weil es die Gegenstaende im Port nicht gibt: chernobylsign,
 * sopsign, stopsign (die drei Schilder), jackt und jackt2 (die Jacken), mask_of_infamy,
 * reer_graar, wrench und wrench_flipped. Ihre Gewichte fallen ersatzlos weg -- die
 * Verhaeltnisse der uebrigen Eintraege zueinander bleiben damit die des Originals, nur die
 * Summe ist kleiner. Ein Ersatzgegenstand waere geraten; ein Weglassen ist nachlesbar.
 *
 * EIN NAME GEHT AUSEINANDER: was im Original ModItems.hat heisst, ist dort als "nossy_hat"
 * angemeldet (ModItemsArmor.java:33 -- das Feld heisst anders als der Name). Der Port fuehrt
 * den angemeldeten Namen, also NOSSY_HAT.
 */
public class MobUtil {

    /**
     * Ein Eintrag einer Liste: ein Gegenstand (oder nichts) mit seinem Gewicht.
     *
     * Oeffentlich, weil die Aktionen des Logikstabs eine Liste als Argument nehmen und den
     * Typ dafuer nennen muessen.
     */
    public record Eintrag(Supplier<Item> gegenstand, int gewicht) {}

    /** Gewichtete Listen je Ruestungsplatz. Platz 0 ist die Hand, 1 bis 4 Stiefel bis Helm. */
    public static final Map<Integer, List<Eintrag>> GEWOEHNLICH = new LinkedHashMap<>();
    public static final Map<Integer, List<Eintrag>> FORTGESCHRITTEN = new LinkedHashMap<>();
    public static final Map<Integer, List<Eintrag>> FERNKAMPF = new LinkedHashMap<>();
    public static final Map<Integer, List<Eintrag>> FERNKAMPF_ADV = new LinkedHashMap<>();
    public static final Map<Integer, List<Eintrag>> WAFFEN_1 = new LinkedHashMap<>();
    public static final Map<Integer, List<Eintrag>> WAFFEN_2 = new LinkedHashMap<>();
    public static final Map<Integer, List<Eintrag>> WAFFEN_3 = new LinkedHashMap<>();

    /** Platz 0 bis 4 des Originals auf die Plaetze von 1.21. */
    private static final EquipmentSlot[] PLAETZE = {
            EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS,
            EquipmentSlot.CHEST, EquipmentSlot.HEAD };

    static {
        /* GEWOEHNLICH -- slotPoolCommon. Die erste Zahl von liste() ist das Leergewicht:
         * so oft bleibt der Platz leer. */
        GEWOEHNLICH.put(4, liste(0,
                e(NtmItems.GAS_MASK_M65, 16), e(NtmItems.GAS_MASK_OLDE, 12),
                e(NtmItems.GAS_MASK_MONO, 8), e(NtmItems.ROBES_HELMET, 32), e(NtmItems.NO9, 16),
                e(NtmItems.COBALT_HELMET, 2), e(NtmItems.RAG_PISS, 1), e(NtmItems.NOSSY_HAT, 1),
                e(NtmItems.ALLOY_HELMET, 2), e(NtmItems.TITANIUM_HELMET, 4), e(NtmItems.STEEL_HELMET, 8)));
        GEWOEHNLICH.put(3, liste(10,
                e(NtmItems.STARMETAL_PLATE, 1), e(NtmItems.COBALT_PLATE, 2), e(NtmItems.ROBES_PLATE, 32),
                e(NtmItems.ALLOY_PLATE, 2), e(NtmItems.STEEL_PLATE, 2)));
        GEWOEHNLICH.put(2, liste(20,
                e(NtmItems.ZIRCONIUM_LEGS, 1), e(NtmItems.COBALT_LEGS, 2), e(NtmItems.STEEL_LEGS, 16),
                e(NtmItems.TITANIUM_LEGS, 8), e(NtmItems.ROBES_LEGS, 32), e(NtmItems.ALLOY_LEGS, 2)));
        GEWOEHNLICH.put(1, liste(10,
                e(NtmItems.ROBES_BOOTS, 32), e(NtmItems.STEEL_BOOTS, 16),
                e(NtmItems.COBALT_BOOTS, 2), e(NtmItems.ALLOY_BOOTS, 2)));
        GEWOEHNLICH.put(0, liste(1000,
                e(NtmItems.PIPE_LEAD, 30), e(NtmItems.CROWBAR, 25), e(NtmItems.GEIGER_COUNTER, 20),
                e(NtmItems.STEEL_PICKAXE, 12), e(NtmItems.STEEL_SWORD, 15),
                e(NtmItems.TITANIUM_SWORD, 8), e(NtmItems.LEAD_GAVEL, 4)));

        /* FERNKAMPF -- slotPoolRanged. Kein Platz 0: die Hand fuellt die Waffenliste. */
        FERNKAMPF.put(4, liste(0,
                e(NtmItems.GAS_MASK_M65, 16), e(NtmItems.GAS_MASK_OLDE, 12),
                e(NtmItems.GAS_MASK_MONO, 8), e(NtmItems.ROBES_HELMET, 32), e(NtmItems.NO9, 16),
                e(NtmItems.RAG_PISS, 1), e(NtmItems.GOGGLES, 1), e(NtmItems.ALLOY_HELMET, 2),
                e(NtmItems.TITANIUM_HELMET, 4), e(NtmItems.STEEL_HELMET, 8)));
        FERNKAMPF.put(3, liste(10,
                e(NtmItems.STARMETAL_PLATE, 1), e(NtmItems.COBALT_PLATE, 2), e(NtmItems.ALLOY_PLATE, 2),
                e(NtmItems.STEEL_PLATE, 8), e(NtmItems.TITANIUM_PLATE, 4)));
        FERNKAMPF.put(2, liste(10,
                e(NtmItems.ZIRCONIUM_LEGS, 1), e(NtmItems.COBALT_LEGS, 2), e(NtmItems.STEEL_LEGS, 16),
                e(NtmItems.TITANIUM_LEGS, 8), e(NtmItems.ROBES_LEGS, 32), e(NtmItems.ALLOY_LEGS, 2)));
        FERNKAMPF.put(1, liste(10,
                e(NtmItems.ROBES_BOOTS, 32), e(NtmItems.STEEL_BOOTS, 16), e(NtmItems.COBALT_BOOTS, 2),
                e(NtmItems.ALLOY_BOOTS, 2), e(NtmItems.TITANIUM_BOOTS, 6)));

        /* FORTGESCHRITTEN -- slotPoolAdv. Ohne Leergewicht: jeder Platz wird belegt. */
        FORTGESCHRITTEN.put(4, liste(0,
                e(NtmItems.SECURITY_HELMET, 10), e(NtmItems.T51_HELMET, 4), e(NtmItems.ASBESTOS_HELMET, 12),
                e(NtmItems.LIQUIDATOR_HELMET, 4), e(NtmItems.NO9, 12), e(NtmItems.HAZMAT_HELMET, 6)));
        FORTGESCHRITTEN.put(3, liste(0,
                e(NtmItems.LIQUIDATOR_PLATE, 4), e(NtmItems.SECURITY_PLATE, 8), e(NtmItems.ASBESTOS_PLATE, 12),
                e(NtmItems.T51_PLATE, 4), e(NtmItems.HAZMAT_PLATE, 6), e(NtmItems.STEEL_PLATE, 8)));
        FORTGESCHRITTEN.put(2, liste(0,
                e(NtmItems.LIQUIDATOR_LEGS, 4), e(NtmItems.SECURITY_LEGS, 8), e(NtmItems.ASBESTOS_LEGS, 12),
                e(NtmItems.T51_LEGS, 4), e(NtmItems.HAZMAT_LEGS, 6), e(NtmItems.STEEL_LEGS, 8)));
        FORTGESCHRITTEN.put(1, liste(0,
                e(NtmItems.LIQUIDATOR_BOOTS, 4), e(NtmItems.SECURITY_BOOTS, 8), e(NtmItems.ASBESTOS_BOOTS, 12),
                e(NtmItems.T51_BOOTS, 4), e(NtmItems.HAZMAT_BOOTS, 6), e(NtmItems.ROBES_BOOTS, 8)));
        FORTGESCHRITTEN.put(0, liste(500,
                e(NtmItems.PIPE_LEAD, 20), e(NtmItems.CROWBAR, 10), e(NtmItems.GEIGER_COUNTER, 10),
                e(NtmItems.TITANIUM_SWORD, 18), e(NtmItems.LEAD_GAVEL, 8)));

        /* FERNKAMPF_ADV -- slotPoolAdvRanged, im Original woertlich die fortgeschrittene
         * Liste ohne ihren Platz 0. */
        FERNKAMPF_ADV.putAll(FORTGESCHRITTEN);
        FERNKAMPF_ADV.remove(0);

        WAFFEN_1.put(0, liste(0,
                e(NtmItems.GUN_LIGHT_REVOLVER, 16), e(NtmItems.GUN_GREASEGUN, 8),
                e(NtmItems.GUN_MARESLEG, 2), e(NtmItems.GUN_FLAREGUN, 1)));
        WAFFEN_2.put(0, liste(0,
                e(NtmItems.GUN_UZI, 12), e(NtmItems.GUN_MARESLEG, 8), e(NtmItems.GUN_HENRY, 12),
                e(NtmItems.GUN_HEAVY_REVOLVER, 8), e(NtmItems.GUN_FLAREGUN, 4), e(NtmItems.GUN_STAR_F, 8)));
        WAFFEN_3.put(0, liste(0,
                e(NtmItems.GUN_G3, 25), e(NtmItems.GUN_SPAS12, 20), e(NtmItems.GUN_CARBINE, 15),
                e(NtmItems.GUN_STAR_F, 20), e(NtmItems.GUN_AM180, 6), e(NtmItems.GUN_AMAT, 5)));
    }

    /**
     * Zieht aus jeder Liste einen Gegenstand und zieht ihn der Entitaet an.
     *
     * DIE GASMASKEN BEKOMMEN IHREN FILTER MIT, wie im Original: ohne ihn ist die Maske
     * wirkungslos, und ein Mob mit wirkungsloser Maske waere kein Gegner, sondern ein Witz.
     */
    public static void ausruesten(LivingEntity entity, Map<Integer, List<Eintrag>> listen, RandomSource zufall) {

        Level level = entity.level();

        for(Map.Entry<Integer, List<Eintrag>> eintrag : listen.entrySet()) {

            Item gezogen = ziehe(eintrag.getValue(), zufall);
            if(gezogen == null) continue;

            ItemStack stapel = new ItemStack(gezogen);

            if(stapel.is(NtmItems.GAS_MASK_M65.get()) || stapel.is(NtmItems.GAS_MASK_OLDE.get())
                    || stapel.is(NtmItems.GAS_MASK_MONO.get())) {
                ArmorUtil.installGasMaskFilter(level, stapel, new ItemStack(NtmItems.GAS_MASK_FILTER.get()));
            }

            entity.setItemSlot(PLAETZE[eintrag.getKey()], stapel);

            /* Ein Skelett, das etwas in die Hand bekommt, bekommt auch das Schuss-Ziel.
             *
             * DIE BEDINGUNG DES ORIGINALS IST EINE MEHR ALS NOETIG: dort steht
             * "slot == 0 && entity instanceof EntitySkeleton && pool == slotPools.get(0)"
             * (MobUtil.java:254). slotPools ist der Parameter, pool ist entry.getValue() --
             * bei slot == 0 sind beide dasselbe Objekt, der dritte Teil ist also immer wahr.
             * Hier steht darum nur, was er tatsaechlich prueft.
             *
             * DER KOMMENTAR DANEBEN STIMMT NICHT: er sagt "if it has a gun", die Bedingung
             * fragt aber nur nach dem Platz, nicht nach dem Gegenstand. Ein Skelett mit einer
             * Schaufel bekommt das Ziel ebenfalls. Schaden tut das nichts, denn canUse()
             * verlangt eine Waffe und laesst das Ziel sonst schlafen. Uebernommen wie es ist.
             *
             * Die hoeheren Schuetzenstufen haengen ihr eigenes, schaerfer eingestelltes Ziel
             * vorher an; schussZiel() laesst dann kein zweites zu. */
            if(eintrag.getKey() == 0 && entity instanceof AbstractSkeleton skelett) {
                schussZiel(skelett, new FireGunGoal(skelett));
            }
        }
    }

    /** Gewichtetes Ziehen; null heisst "der Platz bleibt leer". */
    private static Item ziehe(List<Eintrag> liste, RandomSource zufall) {

        int summe = 0;
        for(Eintrag eintrag : liste) summe += eintrag.gewicht();
        if(summe <= 0) return null;

        int wurf = zufall.nextInt(summe);
        for(Eintrag eintrag : liste) {
            wurf -= eintrag.gewicht();
            if(wurf < 0) return eintrag.gegenstand() == null ? null : eintrag.gegenstand().get();
        }
        return null;
    }

    private static List<Eintrag> liste(int leergewicht, Eintrag... eintraege) {
        List<Eintrag> liste = new ArrayList<>();
        if(leergewicht > 0) liste.add(new Eintrag(null, leergewicht));
        for(Eintrag eintrag : eintraege) liste.add(eintrag);
        return liste;
    }

    private static Eintrag e(Supplier<? extends Item> gegenstand, int gewicht) {
        return new Eintrag(gegenstand::get, gewicht);
    }

    /**
     * Haengt einem Mob das Schuss-Ziel an, aber hoechstens eines.
     *
     * Der Kommentar des Originals an dieser Stelle lautet sinngemaess, die Ziele wuerden sich
     * sonst uebereinanderstapeln -- die Aktionen des Logikstabs laufen mehrfach ueber dieselbe
     * Kreatur. Die Pruefung davor steht deshalb auch hier.
     *
     * Die Fallwahrscheinlichkeit der Hand wird auf null gesetzt, damit die Skelette ihre
     * Waffen nicht fallen lassen.
     */
    public static void schussZiel(Mob mob, FireGunGoal ziel) {

        mob.setDropChance(EquipmentSlot.MAINHAND, 0F);

        for(WrappedGoal vorhanden : mob.goalSelector.getAvailableGoals()) {
            if(vorhanden.getGoal() instanceof FireGunGoal) return;
        }

        mob.goalSelector.addGoal(3, ziel);
    }
}
