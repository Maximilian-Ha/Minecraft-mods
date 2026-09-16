package com.zuxelus.energycontrol.crossmod.mekanism;

import com.zuxelus.energycontrol.crossmod.CrossModBase;
import com.zuxelus.energycontrol.crossmod.MekanismFields;
import com.zuxelus.energycontrol.crossmod.ModIDs;
import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.ArrayList;
import java.util.List;

/**
 * Anbindung an Mekanism (Mod-Kennung {@code mekanism}).
 *
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.crossmod.CrossMekanism. Das Original war
 * eine Kette aus rund achtzig {@code instanceof}-Abfragen -- eine je Maschinenklasse --,
 * weil Mekanism 9 seine Gastanks als oeffentliche Felder je Maschine fuehrte. Mekanism 10
 * hat dafuer Schnittstellen: {@code IStrictEnergyHandler}, {@code IChemicalHandler} und
 * {@code IHeatHandler} beantworten Strom, Chemikalien und Waerme fuer *jede* Maschine.
 * Einzeln steht hier nur, was darueber hinaus etwas zu sagen hat -- die Mehrblockbauten.
 *
 * Die drei Schnittstellen holt diese Klasse ueber ihre Namen aus dem Capability-Register
 * von NeoForge statt ueber Mekanisms interne Klasse {@code Capabilities}. Der Name ist
 * Mekanisms oeffentliche Kennung ({@code mekanism:chemical_handler} und so fort); traegt
 * NeoForge sie schon, kommt genau dieselbe Kennung zurueck, sonst wird sie angelegt und
 * Mekanism findet sie spaeter wieder.
 *
 * Diese Klasse wird nur uebersetzt, wenn beim Bauen die Mekanism-JARs vorlagen, und nur
 * geladen, wenn Mekanism im Spiel ist -- {@code CrossModLoader} holt sie ueber ihren Namen.
 */
public class CrossMekanism extends CrossModBase {

    static final BlockCapability<IChemicalHandler, Direction> CHEMICAL = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath(ModIDs.MEKANISM, "chemical_handler"), IChemicalHandler.class);

    static final BlockCapability<IStrictEnergyHandler, Direction> STRICT_ENERGY = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath(ModIDs.MEKANISM, "strict_energy_handler"), IStrictEnergyHandler.class);

    static final BlockCapability<IHeatHandler, Direction> HEAT = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath(ModIDs.MEKANISM, "heat_handler"), IHeatHandler.class);

    /**
     * Die Generatoren sind eine eigene Mod. Alles, was Spalt-, Fusionsreaktor oder Turbine
     * betrifft, steht deshalb in {@link MekGenerators} und wird nur von hier aus
     * angesprochen, wenn die Mod geladen ist -- so wird die Klasse ohne sie nie geladen und
     * ihre Verweise nie aufgeloest.
     */
    private static final boolean GENERATORS = ModList.get().isLoaded(ModIDs.MEKANISM_GENERATORS);

    /** Bis hierhin sucht der Waermemelder nach einem Reaktor, wie bei HBM. */
    private static final int HEAT_SEARCH_HORIZONTAL = 3;
    private static final int HEAT_SEARCH_VERTICAL = 1;

    // ---------------------------------------------------------------- Energie

    @Override
    public CompoundTag getEnergyData(BlockEntity be) {
        Level level = be.getLevel();
        if(level == null) return null;

        IStrictEnergyHandler power = level.getCapability(STRICT_ENERGY, be.getBlockPos(), null);
        if(power == null) return null;

        long energy = 0L;
        long capacity = 0L;
        for(int i = 0; i < power.getEnergyContainerCount(); i++) {
            energy += power.getEnergy(i);
            capacity += power.getMaxEnergy(i);
        }
        if(capacity <= 0L) return null;

        CompoundTag tag = new CompoundTag();
        tag.putString(DataHelper.EUTYPE, "J");
        tag.putDouble(DataHelper.ENERGY, energy);
        tag.putDouble(DataHelper.CAPACITY, capacity);
        return tag;
    }

    // ------------------------------------------------------------ Fluessigkeit

    @Override
    public List<FluidInfo> getAllTanks(BlockEntity be) {
        Level level = be.getLevel();
        if(level == null) return null;

        List<FluidInfo> tanks = MekTanks.around(level, be.getBlockPos());
        return tanks.isEmpty() ? null : tanks;
    }

    // ------------------------------------------------------------------ Karte

    @Override
    public CompoundTag getCardData(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if(be == null) return null;

        CompoundTag tag = new CompoundTag();
        boolean any = false;

        any |= writeEnergy(tag, level, pos);
        any |= MekTanks.writeTanks(tag, MekTanks.around(level, pos));
        any |= writeHeat(tag, level, pos);
        any |= writeMultiblocks(tag, be);
        any |= writeMachine(tag, be);

        if(GENERATORS) any |= MekGenerators.writeCardData(tag, be);

        return any ? tag : null;
    }

    private boolean writeEnergy(CompoundTag tag, Level level, BlockPos pos) {
        IStrictEnergyHandler power = level.getCapability(STRICT_ENERGY, pos, null);
        if(power == null) return false;

        long energy = 0L;
        long capacity = 0L;
        for(int i = 0; i < power.getEnergyContainerCount(); i++) {
            energy += power.getEnergy(i);
            capacity += power.getMaxEnergy(i);
        }
        if(capacity <= 0L) return false;

        tag.putLong(DataHelper.ENERGY, energy);
        tag.putLong(DataHelper.CAPACITY, capacity);
        return true;
    }

    private boolean writeHeat(CompoundTag tag, Level level, BlockPos pos) {
        IHeatHandler heat = level.getCapability(HEAT, pos, null);
        if(heat == null || heat.getHeatCapacitorCount() == 0) return false;

        tag.putDouble(MekanismFields.TEMPERATURE, heat.getTotalTemperature());
        return true;
    }

    /** Die Mehrblockbauten der Hauptmod: Kessel, Induktionsmatrix, Verdunstung, SPS. */
    private boolean writeMultiblocks(CompoundTag tag, BlockEntity be) {

        if(be instanceof TileEntityBoilerCasing casing) {
            BoilerMultiblockData boiler = casing.getMultiblock();
            if(!boiler.isFormed()) return false;
            tag.putDouble(MekanismFields.TEMPERATURE, boiler.heatCapacitor.getTemperature());
            tag.putLong(MekanismFields.BOIL_RATE, boiler.lastBoilRate);
            tag.putLong(MekanismFields.MAX_BOIL_RATE, boiler.lastMaxBoil);
            tag.putLong(MekanismFields.SUPERHEATERS, boiler.superheatingElements);
            tag.putDouble(MekanismFields.ENV_LOSS, boiler.lastEnvironmentLoss);
            writeTanks(tag, MekTanks.of(boiler.waterTank), MekTanks.of(boiler.steamTank),
                    MekTanks.of(boiler.superheatedCoolantTank), MekTanks.of(boiler.cooledCoolantTank));
            return true;
        }

        if(be instanceof TileEntityInductionCasing casing) {
            MatrixMultiblockData matrix = casing.getMultiblock();
            if(!matrix.isFormed()) return false;
            tag.putLong(DataHelper.ENERGY, matrix.getEnergy());
            tag.putLong(DataHelper.CAPACITY, matrix.getStorageCap());
            tag.putLong(MekanismFields.LAST_INPUT, matrix.getLastInput());
            tag.putLong(MekanismFields.LAST_OUTPUT, matrix.getLastOutput());
            tag.putLong(MekanismFields.TRANSFER_CAP, matrix.getTransferCap());
            tag.putLong(MekanismFields.CELLS, matrix.getCellCount());
            tag.putLong(MekanismFields.PROVIDERS, matrix.getProviderCount());
            // Was hereinkommt minus was hinausgeht: die Zahl, wegen der man eine Tafel
            // an eine Matrix haengt.
            tag.putLong(DataHelper.DIFF, matrix.getLastInput() - matrix.getLastOutput());
            return true;
        }

        if(be instanceof TileEntityThermalEvaporationBlock block) {
            EvaporationMultiblockData evaporation = block.getMultiblock();
            if(!evaporation.isFormed()) return false;
            tag.putDouble(MekanismFields.TEMPERATURE, evaporation.getTemperature());
            tag.putDouble(MekanismFields.GAIN, evaporation.lastGain);
            tag.putDouble(MekanismFields.ENV_LOSS, evaporation.lastEnvironmentLoss);
            writeTanks(tag, MekTanks.of(evaporation.inputTank), MekTanks.of(evaporation.outputTank));
            return true;
        }

        if(be instanceof TileEntitySPSCasing casing) {
            SPSMultiblockData sps = casing.getMultiblock();
            if(!sps.isFormed()) return false;
            tag.putInt(DataHelper.PROGRESS, (int) Math.round(sps.getScaledProgress() * 100D));
            tag.putInt(DataHelper.MAXPROGRESS, 100);
            tag.putDouble(MekanismFields.PROCESS_RATE, sps.getProcessRate());
            tag.putLong(MekanismFields.PROCESSED, sps.inputProcessed);
            tag.putLong(MekanismFields.RECEIVED_ENERGY, sps.lastReceivedEnergy);
            writeTanks(tag, MekTanks.of(sps.inputTank), MekTanks.of(sps.outputTank));
            return true;
        }

        return false;
    }

    /** Was eine gewoehnliche Maschine hergibt: Fortschritt, Ein/Aus, Bergmannsdaten. */
    private boolean writeMachine(CompoundTag tag, BlockEntity be) {
        boolean any = false;

        if(be instanceof TileEntityProgressMachine<?> machine) {
            tag.putInt(DataHelper.PROGRESS, machine.getOperatingTicks());
            tag.putInt(DataHelper.MAXPROGRESS, machine.getTicksRequired());
            any = true;
        }

        if(be instanceof TileEntityDigitalMiner miner) {
            tag.putLong(MekanismFields.TO_MINE, miner.getToMine());
            tag.putString(MekanismFields.MINER_STATE, miner.searcher.state.name());
            tag.putLong(MekanismFields.RADIUS, miner.getRadius());
            tag.putLong(MekanismFields.MIN_Y, miner.getMinY());
            tag.putLong(MekanismFields.MAX_Y, miner.getMaxY());
            any = true;
        }

        if(be instanceof TileEntityMekanism tile) {
            tag.putBoolean(DataHelper.ACTIVE, tile.getActive());
            any = true;
        }

        return any;
    }

    /** Tanks eines Mehrblockbaus: die stehen nicht an der Huelle, sondern im Kern. */
    static void writeTanks(CompoundTag tag, FluidInfo... tanks) {
        List<FluidInfo> list = new ArrayList<>(tanks.length);
        for(FluidInfo tank : tanks) {
            if(tank != null) list.add(tank);
        }
        MekTanks.writeTanks(tag, list);
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
     * Der Waermemelder sucht seinen Reaktor in der Nachbarschaft: erst die sechs
     * angrenzenden Bloecke, dann ein Kasten darum herum. Gemessen wird die Temperatur des
     * Spaltreaktors -- der einzige Bau bei Mekanism, der schmelzen kann.
     */
    private int searchHeat(Level level, BlockPos pos, boolean max) {
        if(!GENERATORS) return -1;

        for(Direction side : Direction.values()) {
            int heat = MekGenerators.reactorHeat(level.getBlockEntity(pos.relative(side)), max);
            if(heat >= 0) return heat;
        }

        for(int x = -HEAT_SEARCH_HORIZONTAL; x <= HEAT_SEARCH_HORIZONTAL; x++) {
            for(int y = -HEAT_SEARCH_VERTICAL; y <= HEAT_SEARCH_VERTICAL; y++) {
                for(int z = -HEAT_SEARCH_HORIZONTAL; z <= HEAT_SEARCH_HORIZONTAL; z++) {
                    int heat = MekGenerators.reactorHeat(level.getBlockEntity(pos.offset(x, y, z)), max);
                    if(heat >= 0) return heat;
                }
            }
        }

        return -1;
    }
}
