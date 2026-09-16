package com.zuxelus.energycontrol.crossmod;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.crossmod.CrossModLoader.
 *
 * Haelt die Anbindungen an fremde Mods und fragt sie der Reihe nach. Die Anbindung
 * selbst wird ueber ihren Klassennamen geladen, nicht ueber einen Verweis: so kennt der
 * Kern die Klassen der fremden Mod nicht und uebersetzt und laeuft auch ohne sie.
 */
public final class CrossModLoader {

    private static final CrossModBase FALLBACK = new CrossModBase();
    private static final Map<String, CrossModBase> MODS = new LinkedHashMap<>();

    private CrossModLoader() { }

    public static void init() {
        load(ModIDs.HBM, "com.zuxelus.energycontrol.crossmod.hbm.CrossHbm");
    }

    private static void load(String modId, String className) {
        if(!ModList.get().isLoaded(modId)) return;
        try {
            Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
            MODS.put(modId, (CrossModBase) instance);
            EnergyControl.LOGGER.info("Anbindung an {} geladen.", modId);
        } catch(ClassNotFoundException e) {
            // Die Bruecke wurde beim Bauen weggelassen, weil die JAR der fremden Mod fehlte.
            EnergyControl.LOGGER.warn("{} ist geladen, aber diese Ausgabe von Energy Control wurde ohne die Anbindung gebaut.", modId);
        } catch(Exception e) {
            EnergyControl.LOGGER.error("Anbindung an {} liess sich nicht laden.", modId, e);
        }
    }

    public static boolean isLoaded(String modId) {
        return MODS.containsKey(modId);
    }

    public static CrossModBase getCrossMod(String modId) {
        return MODS.getOrDefault(modId, FALLBACK);
    }

    /**
     * Energiestand eines Blocks. Zuerst fragen die Anbindungen -- sie kennen die eigenen
     * Einheiten --, danach bleibt die Energie-Schnittstelle von NeoForge als Rueckfall,
     * mit der jede Mod mit Forge-Energie ohne eigene Anbindung auskommt.
     */
    public static CompoundTag getEnergyData(BlockEntity be) {
        if(be == null) return null;

        for(CrossModBase mod : MODS.values()) {
            CompoundTag tag = mod.getEnergyData(be);
            if(tag != null) return tag;
        }

        Level level = be.getLevel();
        if(level == null) return null;

        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(), null);
        if(storage == null) return null;

        CompoundTag tag = new CompoundTag();
        tag.putString(DataHelper.EUTYPE, "FE");
        tag.putDouble(DataHelper.ENERGY, storage.getEnergyStored());
        tag.putDouble(DataHelper.CAPACITY, storage.getMaxEnergyStored());
        return tag;
    }

    /**
     * Alle Tanks eines Blocks. Wie oben: erst die Anbindungen, dann die
     * Fluid-Schnittstelle von NeoForge.
     */
    public static List<FluidInfo> getAllTanks(BlockEntity be) {
        if(be == null) return null;

        for(CrossModBase mod : MODS.values()) {
            List<FluidInfo> tanks = mod.getAllTanks(be);
            if(tanks != null && !tanks.isEmpty()) return tanks;
        }

        Level level = be.getLevel();
        if(level == null) return null;

        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
        if(handler == null) return null;

        List<FluidInfo> tanks = new ArrayList<>();
        for(int i = 0; i < handler.getTanks(); i++) tanks.add(FluidInfo.of(handler, i));
        return tanks;
    }

    /** Huellentemperatur eines Reaktors in der Naehe, fuer den Waermemelder. */
    public static int getHeat(Level level, BlockPos pos) {
        for(CrossModBase mod : MODS.values()) {
            int heat = mod.getHeat(level, pos);
            if(heat >= 0) return heat;
        }
        return -1;
    }

    public static int getMaxHeat(Level level, BlockPos pos) {
        for(CrossModBase mod : MODS.values()) {
            int heat = mod.getMaxHeat(level, pos);
            if(heat > 0) return heat;
        }
        return -1;
    }
}
