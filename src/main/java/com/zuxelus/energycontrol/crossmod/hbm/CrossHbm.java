package com.zuxelus.energycontrol.crossmod.hbm;

import api.hbm.energymk2.IEnergyHandlerMK2;
import api.hbm.fluidmk2.IFluidUserMK2;
import api.hbm.redstoneoverradio.IRORInfo;
import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.machine.ReactorResearchBlockEntity;
import com.hbm.blockentity.machine.ReactorZirnoxBlockEntity;
import com.hbm.blockentity.machine.WatzBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKBaseBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.blockentity.machine.storage.MachineBatteryBlockEntity;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.CompatExternal;
import com.zuxelus.energycontrol.crossmod.CrossModBase;
import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Anbindung an HBM's Nuclear Tech Mod (1.21.1-Port, Mod-Kennung {@code hbmsntm}).
 *
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.crossmod.CrossHBMCE. Das Original war
 * eine Kette aus rund 120 {@code instanceof}-Abfragen, eine je Maschine -- auf 1.7.10
 * hatte jede Maschine ihr eigenes {@code getPower()} ohne gemeinsame Oberklasse. Der
 * Port braucht das nicht mehr: {@code IEnergyHandlerMK2} und {@code IFluidUserMK2} sind
 * die Schnittstellen, ueber die alle Maschinen ihren Strom und ihre Tanks anbieten. Was
 * hier einzeln steht, sind nur die Reaktoren, die darueber hinaus etwas zu sagen haben.
 *
 * Diese Klasse wird nur uebersetzt, wenn beim Bauen eine HBM-JAR vorlag, und nur geladen,
 * wenn der HBM-Port im Spiel ist -- {@code CrossModLoader} holt sie ueber ihren Namen.
 */
public class CrossHbm extends CrossModBase {

    /** Bis hierhin sucht der Waermemelder nach einem Reaktor, wie im Original. */
    private static final int HEAT_SEARCH_HORIZONTAL = 3;
    private static final int HEAT_SEARCH_VERTICAL = 1;

    /**
     * Mehrblockmaschinen stehen im Spiel als Huelle aus Platzhalterbloecken da; die Daten
     * haelt der Kern. CompatExternal ist HBMs eigener Weg dorthin und ausdruecklich fuer
     * fremde Mods gedacht ("DO NOT CHANGE METHOD NAMES/PARAMS ONCE CREATED").
     */
    private static BlockEntity core(Level level, BlockPos pos) {
        return CompatExternal.getCoreFromPos(level, pos);
    }

    private static BlockEntity core(BlockEntity be) {
        if(be == null || be.getLevel() == null) return be;
        BlockEntity resolved = core(be.getLevel(), be.getBlockPos());
        return resolved != null ? resolved : be;
    }

    // ---------------------------------------------------------------- Energie

    @Override
    public CompoundTag getEnergyData(BlockEntity be) {
        BlockEntity target = core(be);
        if(!(target instanceof IEnergyHandlerMK2 handler)) return null;

        CompoundTag tag = new CompoundTag();
        tag.putString(DataHelper.EUTYPE, "HE");
        tag.putDouble(DataHelper.ENERGY, handler.getPower());
        tag.putDouble(DataHelper.CAPACITY, handler.getMaxPower());
        return tag;
    }

    // ------------------------------------------------------------ Fluessigkeit

    @Override
    public List<FluidInfo> getAllTanks(BlockEntity be) {
        BlockEntity target = core(be);
        if(!(target instanceof IFluidUserMK2 user)) return null;

        FluidTank[] tanks = user.getAllTanks();
        if(tanks == null) return null;

        List<FluidInfo> result = new ArrayList<>(tanks.length);
        for(FluidTank tank : tanks) {
            if(tank != null) result.add(HbmFluids.toInfo(tank));
        }
        return result;
    }

    // ------------------------------------------------------------------ Karte

    @Override
    public CompoundTag getCardData(Level level, BlockPos pos) {
        BlockEntity be = core(level, pos);
        if(be == null) return null;

        CompoundTag tag = new CompoundTag();
        boolean any = false;

        if(be instanceof IEnergyHandlerMK2 handler) {
            tag.putLong(DataHelper.ENERGY, handler.getPower());
            tag.putLong(DataHelper.CAPACITY, handler.getMaxPower());
            any = true;
        }

        if(be instanceof MachineBatteryBlockEntity battery) {
            // HE pro Tick, positiv beim Laden, negativ beim Entladen -- im Original als
            // Differenz aus einem Ringpuffer nachgerechnet, hier fuehrt HBM sie selbst.
            tag.putLong(DataHelper.DIFF, battery.delta / 20L);
            any = true;
        }

        if(be instanceof IFluidUserMK2 user) {
            any |= HbmFluids.writeTanks(tag, user.getAllTanks());
        }

        any |= writeReactorData(tag, be);
        any |= writeGenericMachineData(tag, be);
        any |= writeRorValues(tag, be);

        float radiation = ChunkRadiationManager.proxy.getRadiation(level, pos);
        if(radiation > 0F) {
            tag.putDouble("chunkRad", radiation);
            any = true;
        }

        return any ? tag : null;
    }

    /** Was die Reaktoren ueber Strom und Tanks hinaus zu berichten haben. */
    private boolean writeReactorData(CompoundTag tag, BlockEntity be) {

        if(be instanceof RBMKBaseBlockEntity column) {
            tag.putDouble("heatD", column.heat);
            tag.putDouble("melt", column.maxHeat());

            /*
             * Was die Saeule ohnehin an die Reaktorkonsole meldet, gilt auch hier: Abbrand,
             * Xenon, Kern- und Huellentemperatur des Brennstabs, Ventilstellung eines
             * Verdampfers und so fort. Der Weg ueber die Konsole ist der richtige, weil er
             * auf dem Server rechnet -- die Felder fuelYield/fuelXenon/fuelHeat der Saeule
             * fuellt erst das Anzeigepaket, die sind serverseitig leer.
             */
            CompoundTag console = column.getNBTForConsole();
            for(String key : console.getAllKeys()) {
                Tag value = console.get(key);
                if(value != null) tag.put(key, value.copy());
            }

            if(be instanceof RBMKRodBlockEntity rod) {
                tag.putBoolean(DataHelper.ACTIVE, rod.hasRod);
                // lastFlux*: der Fluss der abgeschlossenen Runde. fluxQuantity wird waehrend
                // der Runde aufsummiert und ist beim Auslesen meist noch null.
                tag.putDouble("fluxSlow", rod.lastFluxQuantity * (1D - rod.lastFluxRatio));
                tag.putDouble("fluxFast", rod.lastFluxQuantity * rod.lastFluxRatio);
                writeFuelName(tag, be);
            }
            return true;
        }

        if(be instanceof ReactorZirnoxBlockEntity zirnox) {
            tag.putDouble("heatD", zirnox.heat);
            tag.putInt(DataHelper.PRESSURE, zirnox.pressure);
            tag.putBoolean(DataHelper.ACTIVE, zirnox.isOn);
            return true;
        }

        if(be instanceof ReactorResearchBlockEntity research) {
            tag.putDouble("heatD", research.getDisplayHeat());
            tag.putDouble("level", research.rodLevel);
            tag.putDouble("fluxSlow", research.totalFlux);
            return true;
        }

        if(be instanceof WatzBlockEntity watz) {
            tag.putDouble("heatD", watz.heat);
            tag.putDouble("fluxSlow", watz.fluxDisplay);
            tag.putBoolean(DataHelper.ACTIVE, watz.isOn);
            return true;
        }

        return false;
    }

    /** Der Name des Brennstoffs im ersten Fach, damit die Tafel sagt, was da steckt. */
    private void writeFuelName(CompoundTag tag, BlockEntity be) {
        if(!(be instanceof Container container) || container.getContainerSize() == 0) return;
        ItemStack stack = container.getItem(0);
        if(!stack.isEmpty()) tag.putString("fuelText", stack.getHoverName().getString());
    }

    /**
     * Fortschritt, Temperatur und Ein-/Aus-Zustand gewoehnlicher Maschinen. HBM hat dafuer
     * keine gemeinsame Schnittstelle -- jede Maschine fuehrt ihre eigenen Felder mit
     * denselben Namen. Das Original las sie ebenso ueber Reflexion aus (DataHelper), und
     * das ist hier die bessere Wahl als eine Liste aller Maschinenklassen, die bei jeder
     * neuen Maschine im Port nachgezogen werden muesste.
     */
    private boolean writeGenericMachineData(CompoundTag tag, BlockEntity be) {
        boolean any = false;

        Integer progress = HbmFields.readInt(be, "progress");
        Integer maxProgress = HbmFields.readInt(be, "maxProgress");
        if(progress != null) {
            tag.putInt(DataHelper.PROGRESS, progress);
            any = true;
        }
        if(maxProgress != null && maxProgress > 0) {
            tag.putInt(DataHelper.MAXPROGRESS, maxProgress);
            any = true;
        }

        Integer consumption = HbmFields.readInt(be, "consumption");
        if(consumption != null) {
            tag.putDouble(DataHelper.CONSUMPTION, consumption);
            any = true;
        }

        if(!tag.contains("heatD")) {
            Integer heat = HbmFields.readInt(be, "heat");
            if(heat != null) {
                tag.putDouble("heatD", heat);
                any = true;
            }
        }

        if(!tag.contains(DataHelper.ACTIVE)) {
            Boolean isOn = HbmFields.readBoolean(be, "isOn");
            if(isOn != null) {
                tag.putBoolean(DataHelper.ACTIVE, isOn);
                any = true;
            }
        }

        return any;
    }

    /**
     * Die Werte, die eine Maschine ohnehin schon fuer "Redstone ueber Funk" herausgibt.
     * Das kostet nichts und wirkt fuer jede Maschine, die HBM kuenftig dafuer einrichtet.
     */
    private boolean writeRorValues(CompoundTag tag, BlockEntity be) {
        if(!(be instanceof IRORValueProvider provider)) return false;

        boolean any = false;
        String[] info = provider.getFunctionInfo();
        if(info == null) return false;

        for(String entry : info) {
            if(entry == null || !entry.startsWith(IRORInfo.PREFIX_VALUE)) continue;
            String value = provider.provideRORValue(entry);
            if(value == null) continue;
            tag.putString(DataHelper.ROR_PREFIX + entry.substring(IRORInfo.PREFIX_VALUE.length()), value);
            any = true;
        }
        return any;
    }

    // ------------------------------------------------------------ Waermemelder

    @Override
    public int getHeat(Level level, BlockPos pos) {
        return searchHeat(level, pos, false);
    }

    @Override
    public int getMaxHeat(Level level, BlockPos pos) {
        return searchHeat(level, pos, true);
    }

    /**
     * Sucht einen Reaktor in der Nachbarschaft: erst die sechs anliegenden Bloecke, dann
     * ein Quader von sieben mal drei mal sieben. Dieselbe Suche wie im Original, damit der
     * Melder an denselben Stellen wirkt.
     */
    private int searchHeat(Level level, BlockPos pos, boolean max) {
        if(level == null) return -1;

        for(Direction dir : Direction.values()) {
            int heat = heatOf(level.getBlockEntity(pos.relative(dir)), max);
            if(heat >= 0) return heat;
        }

        for(int x = -HEAT_SEARCH_HORIZONTAL; x <= HEAT_SEARCH_HORIZONTAL; x++) {
            for(int y = -HEAT_SEARCH_VERTICAL; y <= HEAT_SEARCH_VERTICAL; y++) {
                for(int z = -HEAT_SEARCH_HORIZONTAL; z <= HEAT_SEARCH_HORIZONTAL; z++) {
                    int heat = heatOf(level.getBlockEntity(pos.offset(x, y, z)), max);
                    if(heat >= 0) return heat;
                }
            }
        }

        return -1;
    }

    private int heatOf(BlockEntity be, boolean max) {
        if(be instanceof RBMKBaseBlockEntity column) return (int) (max ? column.maxHeat() : column.heat);
        if(be instanceof ReactorZirnoxBlockEntity zirnox) return max ? ReactorZirnoxBlockEntity.maxHeat : zirnox.heat;
        if(be instanceof ReactorResearchBlockEntity research) return max ? ReactorResearchBlockEntity.MAX_HEAT : research.getDisplayHeat();
        // Der Watz-Reaktor kennt keine feste Obergrenze; fuer ihn gibt es nur den Istwert.
        if(be instanceof WatzBlockEntity watz) return max ? -1 : watz.heat;
        return -1;
    }
}
