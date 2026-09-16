package com.zuxelus.energycontrol.crossmod;

/**
 * Die Feldnamen der Mekanism-Anbindung.
 *
 * Warum hier und nicht als Zeichenketten auf beiden Seiten: die Anbindung schreibt die
 * Felder, die Karte liest sie, und zwischen beiden steht nichts, was einen Tippfehler
 * bemerken wuerde -- er kaeme als leere Zeile auf dem Schirm heraus. Die Namen stehen
 * deshalb einmal. Diese Klasse kennt keine Mekanism-Klasse und liegt darum im Kern; die
 * Anbindung selbst liegt in {@code crossmod/mekanism} und wird ohne Mekanism gar nicht
 * erst uebersetzt.
 */
public final class MekanismFields {

    /** Waerme, in Kelvin -- Mekanisms eigene Einheit. */
    public static final String TEMPERATURE = "temp";
    public static final String MAX_TEMPERATURE = "maxTemp";

    // Spaltreaktor
    public static final String BURN_RATE = "burnRate";
    public static final String MAX_BURN_RATE = "maxBurnRate";
    public static final String RATE_LIMIT = "rateLimit";
    public static final String DAMAGE = "damage";
    public static final String ASSEMBLIES = "assemblies";
    public static final String SURFACE_AREA = "surface";

    // Kessel und Spaltreaktor: verdampfte Menge je Tick
    public static final String BOIL_RATE = "boilRate";
    public static final String MAX_BOIL_RATE = "maxBoilRate";
    public static final String SUPERHEATERS = "superheaters";

    /** Waermeverlust an die Umgebung, je Tick. */
    public static final String ENV_LOSS = "envLoss";

    // Fusionsanlage
    public static final String PLASMA_TEMPERATURE = "plasma";
    public static final String CASE_TEMPERATURE = "caseTemp";
    public static final String INJECTION_RATE = "injection";
    public static final String PASSIVE_GENERATION = "passiveGen";
    public static final String STEAM_PER_TICK = "steamTick";

    // Turbine
    public static final String PRODUCTION = "production";
    public static final String MAX_PRODUCTION = "maxProduction";
    public static final String FLOW = "flow";
    public static final String MAX_FLOW = "maxFlow";
    public static final String BLADES = "blades";
    public static final String COILS = "coils";
    public static final String VENTS = "vents";
    public static final String DISPERSERS = "dispersers";
    public static final String CONDENSERS = "condensers";

    // Induktionsmatrix
    public static final String LAST_INPUT = "lastInput";
    public static final String LAST_OUTPUT = "lastOutput";
    public static final String TRANSFER_CAP = "transferCap";
    public static final String CELLS = "cells";
    public static final String PROVIDERS = "providers";

    // Verdunstungsanlage
    public static final String GAIN = "gain";

    // Supercritical Phase Shifter
    public static final String PROCESS_RATE = "processRate";
    public static final String PROCESSED = "processed";
    public static final String RECEIVED_ENERGY = "receivedEnergy";

    // Digitaler Bergmann
    public static final String TO_MINE = "toMine";
    public static final String MINER_STATE = "minerState";
    public static final String RADIUS = "radius";
    public static final String MIN_Y = "minY";
    public static final String MAX_Y = "maxY";

    private MekanismFields() { }
}
