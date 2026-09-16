package com.zuxelus.energycontrol.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Der Stromspeicher in Tafel und Bereichsmelder.
 *
 * NeoForges {@link EnergyStorage} kann alles Noetige; es fehlt nur ein Weg, den Fuellstand
 * beim Laden aus dem Speicherstand zu setzen. Genau diese Erweiterung fuehrt auch der
 * HBM-Port (NtmEnergyStorage) -- aus demselben Grund.
 *
 * Entnommen wird nichts: ein Kartenleser gibt keinen Strom ab, er verbraucht ihn. maxExtract
 * ist deshalb null, und {@link #canExtract()} sagt nein. Der eigene Verbrauch geht ueber
 * {@link #consume(int)} an der Schnittstelle vorbei.
 *
 * Dasselbe gilt fuer den Energiezaehler: er nimmt Strom auf einer Seite an und schiebt ihn
 * auf der anderen weiter, aber er laesst sich nicht leersaugen. Was er weitergibt, nimmt er
 * selbst ueber {@link #drain(int)} heraus.
 */
public class ECEnergyStorage extends EnergyStorage {

    public ECEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    public void setEnergyStored(int amount) {
        this.energy = Math.max(0, Math.min(amount, this.capacity));
    }

    /** Nimmt den eigenen Verbrauch heraus. Gibt zurueck, ob genug da war. */
    public boolean consume(int amount) {
        if(amount <= 0) return true;
        if(this.energy < amount) return false;
        this.energy -= amount;
        return true;
    }

    public boolean has(int amount) {
        return this.energy >= amount;
    }

    /** Nimmt bis zu {@code max} heraus und gibt zurueck, wieviel es wirklich war. */
    public int drain(int max) {
        int taken = Math.min(Math.max(0, max), this.energy);
        this.energy -= taken;
        return taken;
    }
}
