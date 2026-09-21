package com.hbm.entity.mob.glyphid;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.GlyphidStats.
 *
 * Die Werte aller neun Glyphidenarten an einer Stelle. Jede Art hat Leben, Tempo und Schaden,
 * dazu zwei Zahlen fuer die Panzerung: wieviel Schadensschwelle ein Panzerstueck traegt
 * (thresholdMultForArmor) und wieviel Schaden die Panzerung insgesamt schluckt
 * (resistanceMult).
 *
 * NUR EINE TABELLE, UND DAS IST GEMESSEN: das Original fuehrt zwei, GlyphidStats70K und
 * GlyphidStatsNT, und stellt beide in statische Felder. Aber getStats() gibt IMMER die
 * NT-Tabelle zurueck, und keine andere Stelle im ganzen Original nennt die 70K-Tabelle je
 * wieder. Sie in den Port zu uebernehmen hiesse, Zahlen mitzuschleppen, die nichts
 * entscheiden.
 *
 * ZWEI FELDER DES ORIGINALS FEHLEN: divisor und damageThreshold sind dort mit @Deprecated
 * gekennzeichnet, und die NT-Tabelle liest sie nicht -- nur die alte 70K-Tabelle tut das.
 */
public class GlyphidStats {

    private static final GlyphidStats NT = new GlyphidStats();

    public static GlyphidStats getStats() {
        return NT;
    }

    /** Die Werte einer Art. */
    public record StatBundle(double health, double speed, double damage,
                             float thresholdMultForArmor, float resistanceMult) { }

    /** Stufe 1 */
    public final StatBundle grunt = new StatBundle(20D, 1D, 2D, 1F, 0.1F);
    /** Stufe 1, Fernkampf */
    public final StatBundle bombardier = new StatBundle(15D, 1D, 2D, 1F, 0.1F);
    /** Stufe 2 */
    public final StatBundle brawler = new StatBundle(35D, 1D, 10D, 2F, 0.15F);
    /** Stufe 2, Spezialist */
    public final StatBundle digger = new StatBundle(50D, 1D, 10D, 3F, 0.20F);
    /** Stufe 2, Fernkampf */
    public final StatBundle blaster = new StatBundle(35D, 1D, 10D, 2F, 0.15F);
    /** Stufe 3 */
    public final StatBundle behemoth = new StatBundle(125D, 0.8D, 25D, 5F, 0.35F);
    /** Stufe 4 */
    public final StatBundle brenda = new StatBundle(250D, 1.2D, 50D, 10F, 0.5F);
    /** Stufe 4, Spezialist */
    public final StatBundle nuclear = new StatBundle(100D, 0.8D, 50D, 10F, 0.5F);
    /** Stufe 0 */
    public final StatBundle scout = new StatBundle(20D, 1.5D, 5D, 0.5F, 0.5F);
}
