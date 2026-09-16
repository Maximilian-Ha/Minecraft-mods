package com.zuxelus.energycontrol.crossmod.mekanism;

import com.zuxelus.energycontrol.crossmod.MekanismFields;
import com.zuxelus.energycontrol.utils.DataHelper;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Was die Generatoren-Mod von Mekanism ({@code mekanismgenerators}) an Messwerten hergibt:
 * Spaltreaktor, Fusionsanlage und Turbine.
 *
 * Eigene Klasse, weil das eine eigene Mod ist. {@link CrossMekanism} spricht sie nur an,
 * wenn die Mod geladen ist; bleibt der Aufruf aus, laedt die JVM diese Klasse nie und
 * loest ihre Verweise nie auf. Uebersetzt wird sie zusammen mit der uebrigen
 * Mekanism-Anbindung -- ohne die JARs faellt das ganze Paket aus dem Quelltextsatz.
 */
public final class MekGenerators {

    private MekGenerators() { }

    /** Hier faengt Mekanism an, den Spaltreaktor zu beschaedigen. */
    private static final double DAMAGE_TEMPERATURE = FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE;

    static boolean writeCardData(CompoundTag tag, BlockEntity be) {

        if(be instanceof TileEntityFissionReactorCasing casing) {
            FissionReactorMultiblockData reactor = casing.getMultiblock();
            if(!reactor.isFormed()) return false;

            tag.putDouble(MekanismFields.TEMPERATURE, reactor.heatCapacitor.getTemperature());
            tag.putDouble(MekanismFields.MAX_TEMPERATURE, DAMAGE_TEMPERATURE);
            tag.putDouble(MekanismFields.BURN_RATE, reactor.lastBurnRate);
            tag.putLong(MekanismFields.MAX_BURN_RATE, reactor.getMaxBurnRate());
            tag.putDouble(MekanismFields.RATE_LIMIT, reactor.rateLimit);
            tag.putDouble(MekanismFields.DAMAGE, reactor.getDamagePercent());
            tag.putLong(MekanismFields.BOIL_RATE, reactor.lastBoilRate);
            tag.putDouble(MekanismFields.ENV_LOSS, reactor.lastEnvironmentLoss);
            tag.putLong(MekanismFields.ASSEMBLIES, reactor.assemblies.size());
            tag.putLong(MekanismFields.SURFACE_AREA, reactor.surfaceArea);
            tag.putBoolean(DataHelper.ACTIVE, reactor.isBurning());

            CrossMekanism.writeTanks(tag,
                    MekTanks.of(reactor.fuelTank),
                    MekTanks.of(reactor.coolantTank.getFluidTank()),
                    MekTanks.of(reactor.heatedCoolantTank),
                    MekTanks.of(reactor.wasteTank));
            return true;
        }

        if(be instanceof TileEntityFusionReactorBlock block) {
            FusionReactorMultiblockData fusion = block.getMultiblock();
            if(!fusion.isFormed()) return false;

            tag.putDouble(MekanismFields.PLASMA_TEMPERATURE, fusion.getLastPlasmaTemp());
            tag.putDouble(MekanismFields.CASE_TEMPERATURE, fusion.getLastCaseTemp());
            tag.putDouble(MekanismFields.TEMPERATURE, fusion.getLastCaseTemp());
            tag.putLong(MekanismFields.INJECTION_RATE, fusion.getInjectionRate());
            tag.putLong(MekanismFields.PASSIVE_GENERATION, fusion.getPassiveGeneration(false, true));
            tag.putLong(MekanismFields.STEAM_PER_TICK, fusion.getSteamPerTick(true));
            tag.putDouble(MekanismFields.ENV_LOSS, fusion.lastEnvironmentLoss);
            tag.putBoolean(DataHelper.ACTIVE, fusion.isBurning());
            tag.putLong(DataHelper.ENERGY, fusion.energyContainer.getEnergy());
            tag.putLong(DataHelper.CAPACITY, fusion.energyContainer.getMaxEnergy());

            CrossMekanism.writeTanks(tag,
                    MekTanks.of(fusion.deuteriumTank),
                    MekTanks.of(fusion.tritiumTank),
                    MekTanks.of(fusion.fuelTank),
                    MekTanks.of(fusion.waterTank),
                    MekTanks.of(fusion.steamTank));
            return true;
        }

        if(be instanceof TileEntityTurbineCasing casing) {
            TurbineMultiblockData turbine = casing.getMultiblock();
            if(!turbine.isFormed()) return false;

            tag.putLong(MekanismFields.PRODUCTION, turbine.getProductionRate());
            tag.putLong(MekanismFields.MAX_PRODUCTION, turbine.getMaxProduction());
            tag.putLong(MekanismFields.FLOW, turbine.lastSteamInput);
            tag.putLong(MekanismFields.MAX_FLOW, turbine.getMaxFlowRate());
            tag.putLong(MekanismFields.BLADES, turbine.blades);
            tag.putLong(MekanismFields.COILS, turbine.coils);
            tag.putLong(MekanismFields.VENTS, turbine.vents);
            tag.putLong(MekanismFields.CONDENSERS, turbine.condensers);
            tag.putLong(MekanismFields.DISPERSERS, turbine.getDispersers());
            tag.putLong(DataHelper.ENERGY, turbine.energyContainer.getEnergy());
            tag.putLong(DataHelper.CAPACITY, turbine.energyContainer.getMaxEnergy());

            CrossMekanism.writeTanks(tag,
                    MekTanks.of(turbine.chemicalTank),
                    MekTanks.of(turbine.ventTank));
            return true;
        }

        return false;
    }

    /**
     * Die Temperatur eines Spaltreaktors, fuer den Waermemelder -- oder die Temperatur,
     * ab der Mekanism ihn beschaedigt, wenn {@code max} gesetzt ist. Negativ, wenn an
     * dieser Stelle kein gebildeter Reaktor steht.
     */
    static int reactorHeat(BlockEntity be, boolean max) {
        if(!(be instanceof TileEntityFissionReactorCasing casing)) return -1;

        FissionReactorMultiblockData reactor = casing.getMultiblock();
        if(!reactor.isFormed()) return -1;

        return max ? (int) DAMAGE_TEMPERATURE : (int) reactor.heatCapacitor.getTemperature();
    }
}
