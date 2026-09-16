package com.zuxelus.energycontrol.crossmod;

import com.zuxelus.energycontrol.utils.FluidInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.crossmod.CrossModBase.
 *
 * Was eine Anbindung an eine fremde Mod leisten kann. Alle Methoden antworten hier mit
 * "kenne ich nicht"; eine Anbindung ueberschreibt nur, was sie wirklich beantworten kann.
 */
public class CrossModBase {

    /** Energiestand eines Blocks fuer die Energiekarte. */
    public CompoundTag getEnergyData(BlockEntity be) {
        return null;
    }

    /** Alle Messwerte eines Blocks fuer die modeigene Karte. */
    public CompoundTag getCardData(Level level, BlockPos pos) {
        return null;
    }

    /** Alle Tanks eines Blocks fuer die Fluessigkeitskarte. */
    public List<FluidInfo> getAllTanks(BlockEntity be) {
        return null;
    }

    /**
     * Die Huellentemperatur eines Reaktors in der Naehe, fuer den Waermemelder.
     * Negativ, wenn kein Reaktor gefunden wurde.
     */
    public int getHeat(Level level, BlockPos pos) {
        return -1;
    }

    /** Die hoechste Temperatur, die der gefundene Reaktor vertraegt. */
    public int getMaxHeat(Level level, BlockPos pos) {
        return -1;
    }
}
