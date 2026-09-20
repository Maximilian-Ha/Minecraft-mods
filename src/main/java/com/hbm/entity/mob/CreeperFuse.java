package com.hbm.entity.mob;

import net.minecraft.world.entity.monster.Creeper;

/**
 * Der Zaehler des Creepers -- und die Stelle, an der der Port ihn abgreift.
 *
 * DAS PROBLEM. Alle Creeper dieser Mod ersetzen den Knall des Originals durch einen
 * eigenen. Im Original geht das ueber func_146077_cc, die Methode, die der Creeper am Ende
 * seines Zaehlers ruft. In 1.21 heisst sie explodeCreeper und ist PRIVAT -- man kann sie
 * weder ueberschreiben noch rufen.
 *
 * WARUM ignite() NICHT GENUEGT. Naheliegend waere ignite(), aber dieser Weg wird im Spiel
 * fast nie gegangen: SwellGoal ruft setSwellDir(1), wenn ein Spieler nahe genug ist, und
 * Creeper.tick() laesst den Zaehler dann bis maxSwell laufen und ruft explodeCreeper. Durch
 * ignite() kommt nur, wer mit Feuerzeug und Stein anzuendet. Ein Creeper, der einfach auf
 * einen Spieler zulaeuft, haette also den gewoehnlichen Knall gemacht.
 *
 * DIE LOESUNG. getSwelling ist oeffentlich -- der Darsteller braucht es, um den Creeper
 * aufblaehen zu lassen. Es liefert den Zaehlerstand geteilt durch (maxSwell - 2); mit
 * maxSwell = 30 also den Stand geteilt durch 28. Damit laesst sich der Stand zurueckrechnen
 * und der eigene Knall zwei Ticks vor dem gewoehnlichen ausloesen.
 *
 * WAS DAS KOSTET: die Lunte ist zwei Ticks kuerzer als bei einem gewoehnlichen Creeper --
 * ein Zehntel einer Sekunde. Dafuer geht jeder Creeper dieser Mod auf jedem Weg so hoch,
 * wie er soll.
 */
public final class CreeperFuse {

    private CreeperFuse() { }

    /**
     * Creeper.maxSwell (30) minus die zwei, mit denen getSwelling rechnet. Hoeher als
     * dieser Wert kommt der zurueckgerechnete Zaehlerstand nicht, bevor Vanilla selbst
     * sprengt.
     */
    public static final int SPANNE = 28;

    /** Der zurueckgerechnete Zaehlerstand in Ticks. */
    public static float zaehlerstand(Creeper creeper) {
        return creeper.getSwelling(1.0F) * SPANNE;
    }

    /**
     * Ist die Lunte abgebrannt? Gilt nur auf dem Server und nur fuer einen lebenden
     * Creeper -- wer schon gesprengt hat, ist nicht mehr am Leben.
     */
    public static boolean abgebrannt(Creeper creeper, int zuendzeit) {
        return !creeper.level().isClientSide && creeper.isAlive() && zaehlerstand(creeper) >= zuendzeit;
    }
}
