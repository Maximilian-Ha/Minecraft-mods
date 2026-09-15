package com.hbm.blockentity.network;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Kleine Erweiterung von NeoForges EnergyStorage um die beiden Zugriffe, die die Wandler aus
 * 1.7.10 brauchen und die COFHs Fassung noch hatte: den Fuellstand direkt setzen und die
 * Entnahmegrenze abfragen.
 */
public class NtmEnergyStorage extends EnergyStorage {

    public NtmEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergyStored(int energy) {
        this.energy = Math.max(0, Math.min(energy, this.capacity));
    }

    public int getMaxExtract() {
        return this.maxExtract;
    }
}
