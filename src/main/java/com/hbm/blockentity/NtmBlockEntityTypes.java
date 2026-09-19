package com.hbm.blockentity;

import com.hbm.blockentity.bomb.*;
import com.hbm.blockentity.machine.*;
import com.hbm.blockentity.machine.albion.*;
import com.hbm.blockentity.machine.fusion.*;
import com.hbm.blockentity.machine.icf.*;
import com.hbm.blockentity.machine.pile.*;
import com.hbm.blockentity.machine.rbmk.RBMKBoilerBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKHeaterBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKInletBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKOutgasserBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKOutletBlockEntity;
import com.hbm.blockentity.machine.WasteDrumBlockEntity;
import com.hbm.blockentity.machine.rbmk.CraneConsoleBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKAutoloaderBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKControlAutoBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKControlBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKCoolerBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKGaugeBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKIndicatorBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKNumitronBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKLeverBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKKeyPadBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKGraphBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKDisplayBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKTerminalBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKStorageBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKPassiveBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKRodReaSimBlockEntity;
import com.hbm.blockentity.machine.boiler.MachineHeatBoilerBlockEntity;
import com.hbm.blockentity.machine.boiler.MachineIndustrialBoilerBlockEntity;
import com.hbm.blockentity.machine.heater.*;
import com.hbm.blockentity.machine.oil.MachineFractionTowerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineCatalyticReformerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineHydrotreaterBlockEntity;
import com.hbm.blockentity.machine.oil.MachineCatalyticCrackerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineCokerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineGasFlareBlockEntity;
import com.hbm.blockentity.machine.oil.MachineLiquefactorBlockEntity;
import com.hbm.blockentity.machine.oil.MachinePyroOvenBlockEntity;
import com.hbm.blockentity.machine.oil.MachineSolidifierBlockEntity;
import com.hbm.blockentity.machine.oil.MachineVacuumDistillBlockEntity;
import com.hbm.blockentity.machine.oil.MachineFrackingTowerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineOilWellBlockEntity;
import com.hbm.blockentity.machine.oil.MachinePumpjackBlockEntity;
import com.hbm.blockentity.machine.oil.SpacerBlockEntity;
import com.hbm.blockentity.machine.oil.MachineRefineryBlockEntity;
import com.hbm.blockentity.machine.storage.*;
import com.hbm.blockentity.network.CableBaseBlockEntity;
import com.hbm.blockentity.network.CableSwitchBlockEntity;
import com.hbm.blockentity.network.DiodeBlockEntity;
import com.hbm.blockentity.network.CableGaugeBlockEntity;
import com.hbm.blockentity.network.ConnectorBlockEntity;
import com.hbm.blockentity.network.ConverterHeRfBlockEntity;
import com.hbm.blockentity.network.ConverterRfHeBlockEntity;
import com.hbm.blockentity.network.ConnectorSuperBlockEntity;
import com.hbm.blockentity.network.PipeBaseBlockEntity;
import com.hbm.blockentity.network.PipeCounterValveBlockEntity;
import com.hbm.blockentity.network.PipeGaugeBlockEntity;
import com.hbm.blockentity.network.PipeValveBlockEntity;
import com.hbm.blockentity.machine.FloodlightBlockEntity;
import com.hbm.blockentity.machine.FloodlightBeamBlockEntity;
import com.hbm.blockentity.machine.DecoPoleSatelliteReceiverBlockEntity;
import com.hbm.blockentity.machine.ChargerBlockEntity;
import com.hbm.blockentity.machine.MachineMicrowaveBlockEntity;
import com.hbm.blockentity.machine.TeslaBlockEntity;
import com.hbm.blockentity.network.RadioRecBlockEntity;
import com.hbm.blockentity.network.RadioTelexBlockEntity;
import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blockentity.network.PylonLargeBlockEntity;
import com.hbm.blockentity.network.PylonMediumBlockEntity;
import com.hbm.blockentity.network.RadioTorchReceiverBlockEntity;
import com.hbm.blockentity.network.RadioTorchSenderBlockEntity;
import com.hbm.blockentity.network.SubstationBlockEntity;
import com.hbm.blockentity.turret.TurretChekhovBlockEntity;
import com.hbm.blockentity.turret.TurretFriendlyBlockEntity;
import com.hbm.blockentity.turret.TurretHowardBlockEntity;
import com.hbm.blockentity.turret.TurretHowardDamagedBlockEntity;
import com.hbm.blockentity.turret.TurretJeremyBlockEntity;
import com.hbm.blockentity.turret.TurretSentryBlockEntity;
import com.hbm.blockentity.turret.TurretSentryDamagedBlockEntity;
import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.hbm.blockentity.network.CraneGrabberBlockEntity;
import com.hbm.blockentity.network.CraneBoxerBlockEntity;
import com.hbm.blockentity.network.CraneUnboxerBlockEntity;
import com.hbm.blockentity.network.CraneRouterBlockEntity;
import com.hbm.blockentity.network.CranePartitionerBlockEntity;
import com.hbm.blockentity.network.CraneSplitterBlockEntity;
import com.hbm.blockentity.network.CraneInserterBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.bomb.VolcanoBlock.VolcanoCoreBlockEntity;
import com.hbm.blocks.generic.BobbleBlock.BobbleBlockEntity;
import com.hbm.blocks.generic.PlushieBlock.PlushieBlockEntity;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue") // kill yourself
public class NtmBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, NuclearTechMod.MODID);

    // Machines
    public static final Supplier<BlockEntityType<MachineSolderingStationBlockEntity>> MACHINE_SOLDERING_STATION = BLOCK_ENTITY_TYPES.register(
            "machine_soldering_station",
            () -> BlockEntityType.Builder.of(
                            MachineSolderingStationBlockEntity::new,
                            NtmBlocks.MACHINE_SOLDERING_STATION.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineArcWelderBlockEntity>> MACHINE_ARC_WELDER = BLOCK_ENTITY_TYPES.register(
            "machine_arc_welder",
            () -> BlockEntityType.Builder.of(
                            MachineArcWelderBlockEntity::new,
                            NtmBlocks.MACHINE_ARC_WELDER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineHeatBoilerBlockEntity>> HEAT_BOILER = BLOCK_ENTITY_TYPES.register(
            "heat_boiler",
            () -> BlockEntityType.Builder.of(
                            MachineHeatBoilerBlockEntity::new,
                            NtmBlocks.HEAT_BOILER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineIndustrialBoilerBlockEntity>> MACHINE_INDUSTRIAL_BOILER = BLOCK_ENTITY_TYPES.register(
            "machine_industrial_boiler",
            () -> BlockEntityType.Builder.of(
                            MachineIndustrialBoilerBlockEntity::new,
                            NtmBlocks.MACHINE_INDUSTRIAL_BOILER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<HeaterFireboxBlockEntity>> HEATER_FIREBOX = BLOCK_ENTITY_TYPES.register(
            "heater_firebox",
            () -> BlockEntityType.Builder.of(
                            HeaterFireboxBlockEntity::new,
                            NtmBlocks.HEATER_FIREBOX.get())
                    .build(null));

    public static final Supplier<BlockEntityType<HeaterOvenBlockEntity>> HEATER_OVEN = BLOCK_ENTITY_TYPES.register(
            "heater_oven",
            () -> BlockEntityType.Builder.of(
                            HeaterOvenBlockEntity::new,
                            NtmBlocks.HEATER_OVEN.get())
                    .build(null));

    public static final Supplier<BlockEntityType<HeaterOilburnerBlockEntity>> HEATER_OILBURNER = BLOCK_ENTITY_TYPES.register(
            "heater_oilburner",
            () -> BlockEntityType.Builder.of(
                            HeaterOilburnerBlockEntity::new,
                            NtmBlocks.HEATER_OILBURNER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<HeaterElectricBlockEntity>> HEATER_ELECTRIC = BLOCK_ENTITY_TYPES.register(
            "heater_electric",
            () -> BlockEntityType.Builder.of(
                            HeaterElectricBlockEntity::new,
                            NtmBlocks.HEATER_ELECTRIC.get())
                    .build(null));

    public static final Supplier<BlockEntityType<HeaterHeatexBlockEntity>> HEATER_HEATEX = BLOCK_ENTITY_TYPES.register(
            "heater_heatex",
            () -> BlockEntityType.Builder.of(
                            HeaterHeatexBlockEntity::new,
                            NtmBlocks.HEATER_HEATEX.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineTurbineBlockEntity>> MACHINE_TURBINE = BLOCK_ENTITY_TYPES.register("machine_turbine", () -> BlockEntityType.Builder.of(MachineTurbineBlockEntity::new, NtmBlocks.MACHINE_TURBINE.get()).build(null));
    public static final Supplier<BlockEntityType<SolarBoilerBlockEntity>> MACHINE_SOLAR_BOILER = BLOCK_ENTITY_TYPES.register("machine_solar_boiler", () -> BlockEntityType.Builder.of(SolarBoilerBlockEntity::new, NtmBlocks.MACHINE_SOLAR_BOILER.get()).build(null));
    public static final Supplier<BlockEntityType<SolarMirrorBlockEntity>> SOLAR_MIRROR = BLOCK_ENTITY_TYPES.register("solar_mirror", () -> BlockEntityType.Builder.of(SolarMirrorBlockEntity::new, NtmBlocks.SOLAR_MIRROR.get()).build(null));
    public static final Supplier<BlockEntityType<FurnaceIronBlockEntity>> FURNACE_IRON = BLOCK_ENTITY_TYPES.register("furnace_iron", () -> BlockEntityType.Builder.of(FurnaceIronBlockEntity::new, NtmBlocks.FURNACE_IRON.get()).build(null));
    public static final Supplier<BlockEntityType<FurnaceBrickBlockEntity>> FURNACE_BRICK = BLOCK_ENTITY_TYPES.register("furnace_brick", () -> BlockEntityType.Builder.of(FurnaceBrickBlockEntity::new, NtmBlocks.MACHINE_FURNACE_BRICK.get()).build(null));
    public static final Supplier<BlockEntityType<FurnaceSteelBlockEntity>> FURNACE_STEEL = BLOCK_ENTITY_TYPES.register("furnace_steel", () -> BlockEntityType.Builder.of(FurnaceSteelBlockEntity::new, NtmBlocks.FURNACE_STEEL.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCompressorCompactBlockEntity>> MACHINE_COMPRESSOR_COMPACT = BLOCK_ENTITY_TYPES.register("machine_compressor_compact", () -> BlockEntityType.Builder.of(MachineCompressorCompactBlockEntity::new, NtmBlocks.MACHINE_COMPRESSOR_COMPACT.get()).build(null));
    public static final Supplier<BlockEntityType<MachineMixerBlockEntity>> MACHINE_MIXER = BLOCK_ENTITY_TYPES.register("machine_mixer", () -> BlockEntityType.Builder.of(MachineMixerBlockEntity::new, NtmBlocks.MACHINE_MIXER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineRockMillBlockEntity>> MACHINE_ROCK_MILL = BLOCK_ENTITY_TYPES.register("machine_rock_mill", () -> BlockEntityType.Builder.of(MachineRockMillBlockEntity::new, NtmBlocks.MACHINE_ROCK_MILL.get()).build(null));
    public static final Supplier<BlockEntityType<MachineMiningLaserBlockEntity>> MACHINE_MINING_LASER = BLOCK_ENTITY_TYPES.register("machine_mining_laser", () -> BlockEntityType.Builder.of(MachineMiningLaserBlockEntity::new, NtmBlocks.MACHINE_MINING_LASER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCyclotronBlockEntity>> MACHINE_CYCLOTRON = BLOCK_ENTITY_TYPES.register("machine_cyclotron", () -> BlockEntityType.Builder.of(MachineCyclotronBlockEntity::new, NtmBlocks.MACHINE_CYCLOTRON.get()).build(null));

    public static final Supplier<BlockEntityType<MachinePASourceBlockEntity>> MACHINE_PA_SOURCE = BLOCK_ENTITY_TYPES.register("machine_pa_source", () -> BlockEntityType.Builder.of(MachinePASourceBlockEntity::new, NtmBlocks.MACHINE_PA_SOURCE.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePABeamlineBlockEntity>> MACHINE_PA_BEAMLINE = BLOCK_ENTITY_TYPES.register("machine_pa_beamline", () -> BlockEntityType.Builder.of(MachinePABeamlineBlockEntity::new, NtmBlocks.MACHINE_PA_BEAMLINE.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePARFCBlockEntity>> MACHINE_PA_RFC = BLOCK_ENTITY_TYPES.register("machine_pa_rfc", () -> BlockEntityType.Builder.of(MachinePARFCBlockEntity::new, NtmBlocks.MACHINE_PA_RFC.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePAQuadrupoleBlockEntity>> MACHINE_PA_QUADRUPOLE = BLOCK_ENTITY_TYPES.register("machine_pa_quadrupole", () -> BlockEntityType.Builder.of(MachinePAQuadrupoleBlockEntity::new, NtmBlocks.MACHINE_PA_QUADRUPOLE.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePADipoleBlockEntity>> MACHINE_PA_DIPOLE = BLOCK_ENTITY_TYPES.register("machine_pa_dipole", () -> BlockEntityType.Builder.of(MachinePADipoleBlockEntity::new, NtmBlocks.MACHINE_PA_DIPOLE.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePADetectorBlockEntity>> MACHINE_PA_DETECTOR = BLOCK_ENTITY_TYPES.register("machine_pa_detector", () -> BlockEntityType.Builder.of(MachinePADetectorBlockEntity::new, NtmBlocks.MACHINE_PA_DETECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<MachineExposureChamberBlockEntity>> MACHINE_EXPOSURE_CHAMBER = BLOCK_ENTITY_TYPES.register("machine_exposure_chamber", () -> BlockEntityType.Builder.of(MachineExposureChamberBlockEntity::new, NtmBlocks.MACHINE_EXPOSURE_CHAMBER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineRadGenBlockEntity>> MACHINE_RAD_GEN = BLOCK_ENTITY_TYPES.register("machine_rad_gen", () -> BlockEntityType.Builder.of(MachineRadGenBlockEntity::new, NtmBlocks.MACHINE_RAD_GEN.get()).build(null));
    public static final Supplier<BlockEntityType<MachineGasCentBlockEntity>> MACHINE_GAS_CENT = BLOCK_ENTITY_TYPES.register("machine_gas_cent", () -> BlockEntityType.Builder.of(MachineGasCentBlockEntity::new, NtmBlocks.MACHINE_GAS_CENT.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCompressorBlockEntity>> MACHINE_COMPRESSOR = BLOCK_ENTITY_TYPES.register("machine_compressor", () -> BlockEntityType.Builder.of(MachineCompressorBlockEntity::new, NtmBlocks.MACHINE_COMPRESSOR.get()).build(null));
    public static final Supplier<BlockEntityType<MachineSteamEngineBlockEntity>> MACHINE_STEAM_ENGINE = BLOCK_ENTITY_TYPES.register("machine_steam_engine", () -> BlockEntityType.Builder.of(MachineSteamEngineBlockEntity::new, NtmBlocks.MACHINE_STEAM_ENGINE.get()).build(null));
    public static final Supplier<BlockEntityType<MachineStirlingBlockEntity>> MACHINE_STIRLING = BLOCK_ENTITY_TYPES.register("machine_stirling", () -> BlockEntityType.Builder.of(MachineStirlingBlockEntity::new, NtmBlocks.MACHINE_STIRLING.get()).build(null));

    public static final Supplier<BlockEntityType<MachineDieselBlockEntity>> MACHINE_DIESEL = BLOCK_ENTITY_TYPES.register(
            "machine_diesel",
            () -> BlockEntityType.Builder.of(
                            MachineDieselBlockEntity::new,
                            NtmBlocks.MACHINE_DIESEL.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineElectricFurnaceBlockEntity>> MACHINE_ELECTRIC_FURNACE = BLOCK_ENTITY_TYPES.register(
            "machine_electric_furnace",
            () -> BlockEntityType.Builder.of(
                            MachineElectricFurnaceBlockEntity::new,
                            NtmBlocks.MACHINE_ELECTRIC_FURNACE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineRtgFurnaceBlockEntity>> MACHINE_RTG_FURNACE = BLOCK_ENTITY_TYPES.register(
            "machine_rtg_furnace",
            () -> BlockEntityType.Builder.of(
                            MachineRtgFurnaceBlockEntity::new,
                            NtmBlocks.MACHINE_RTG_FURNACE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineDiFurnaceRtgBlockEntity>> MACHINE_DIFURNACE_RTG = BLOCK_ENTITY_TYPES.register(
            "machine_difurnace_rtg",
            () -> BlockEntityType.Builder.of(
                            MachineDiFurnaceRtgBlockEntity::new,
                            NtmBlocks.MACHINE_DIFURNACE_RTG.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachinePWRControllerBlockEntity>> MACHINE_PWR_CONTROLLER = BLOCK_ENTITY_TYPES.register(
            "machine_pwr_controller",
            () -> BlockEntityType.Builder.of(
                            MachinePWRControllerBlockEntity::new,
                            NtmBlocks.PWR_CONTROLLER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<PWRBlockEntity>> PWR_BLOCK = BLOCK_ENTITY_TYPES.register(
            "pwr_block",
            () -> BlockEntityType.Builder.of(
                            PWRBlockEntity::new,
                            NtmBlocks.PWR_BLOCK.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineShredderBlockEntity>> MACHINE_SHREDDER = BLOCK_ENTITY_TYPES.register(
            "machine_shredder",
            () -> BlockEntityType.Builder.of(
                            MachineShredderBlockEntity::new,
                            NtmBlocks.MACHINE_SHREDDER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<ReactorZirnoxBlockEntity>> REACTOR_ZIRNOX = BLOCK_ENTITY_TYPES.register("machine_zirnox", () -> BlockEntityType.Builder.of(ReactorZirnoxBlockEntity::new, NtmBlocks.REACTOR_ZIRNOX.get()).build(null));
    public static final Supplier<BlockEntityType<ZirnoxDestroyedBlockEntity>> ZIRNOX_DESTROYED = BLOCK_ENTITY_TYPES.register("zirnox_destroyed", () -> BlockEntityType.Builder.of(ZirnoxDestroyedBlockEntity::new, NtmBlocks.ZIRNOX_DESTROYED.get()).build(null));
    public static final Supplier<BlockEntityType<WatzBlockEntity>> WATZ = BLOCK_ENTITY_TYPES.register("watz", () -> BlockEntityType.Builder.of(WatzBlockEntity::new, NtmBlocks.WATZ.get()).build(null));
    public static final Supplier<BlockEntityType<WatzPumpBlockEntity>> WATZ_PUMP = BLOCK_ENTITY_TYPES.register("watz_pump", () -> BlockEntityType.Builder.of(WatzPumpBlockEntity::new, NtmBlocks.WATZ_PUMP.get()).build(null));
    public static final Supplier<BlockEntityType<WatzStructBlockEntity>> WATZ_STRUCT = BLOCK_ENTITY_TYPES.register("struct_watz_core", () -> BlockEntityType.Builder.of(WatzStructBlockEntity::new, NtmBlocks.STRUCT_WATZ_CORE.get()).build(null));
    public static final Supplier<BlockEntityType<PileBaseBlockEntity>> PILE_BASE = BLOCK_ENTITY_TYPES.register("pile_base", () -> BlockEntityType.Builder.of(PileBaseBlockEntity::new, NtmBlocks.PILE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PileCoreBlockEntity>> PILE_CORE = BLOCK_ENTITY_TYPES.register("pile_core", () -> BlockEntityType.Builder.of(PileCoreBlockEntity::new, NtmBlocks.PILE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PileLoaderBlockEntity>> PILE_LOADER = BLOCK_ENTITY_TYPES.register("pile_loader", () -> BlockEntityType.Builder.of(PileLoaderBlockEntity::new, NtmBlocks.PILE_LOADER.get()).build(null));
    public static final Supplier<BlockEntityType<PileVentBlockEntity>> PILE_VENT = BLOCK_ENTITY_TYPES.register("pile_vent", () -> BlockEntityType.Builder.of(PileVentBlockEntity::new, NtmBlocks.PILE_VENT.get()).build(null));
    public static final Supplier<BlockEntityType<PileControlBlockEntity>> PILE_CONTROL = BLOCK_ENTITY_TYPES.register("pile_control", () -> BlockEntityType.Builder.of(PileControlBlockEntity::new, NtmBlocks.PILE_CONTROL.get()).build(null));
    public static final Supplier<BlockEntityType<ICFBlockEntity>> ICF = BLOCK_ENTITY_TYPES.register("icf", () -> BlockEntityType.Builder.of(ICFBlockEntity::new, NtmBlocks.ICF.get()).build(null));
    public static final Supplier<BlockEntityType<ICFControllerBlockEntity>> ICF_CONTROLLER = BLOCK_ENTITY_TYPES.register("icf_controller", () -> BlockEntityType.Builder.of(ICFControllerBlockEntity::new, NtmBlocks.ICF_CONTROLLER.get()).build(null));
    public static final Supplier<BlockEntityType<ICFWrapperBlockEntity>> ICF_BLOCK = BLOCK_ENTITY_TYPES.register("icf_block", () -> BlockEntityType.Builder.of(ICFWrapperBlockEntity::new, NtmBlocks.ICF_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<FusionBreederBlockEntity>> FUSION_BREEDER = BLOCK_ENTITY_TYPES.register("fusion_breeder", () -> BlockEntityType.Builder.of(FusionBreederBlockEntity::new, NtmBlocks.FUSION_BREEDER.get()).build(null));
    public static final Supplier<BlockEntityType<FusionCollectorBlockEntity>> FUSION_COLLECTOR = BLOCK_ENTITY_TYPES.register("fusion_collector", () -> BlockEntityType.Builder.of(FusionCollectorBlockEntity::new, NtmBlocks.FUSION_COLLECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<FusionCouplerBlockEntity>> FUSION_COUPLER = BLOCK_ENTITY_TYPES.register("fusion_coupler", () -> BlockEntityType.Builder.of(FusionCouplerBlockEntity::new, NtmBlocks.FUSION_COUPLER.get()).build(null));
    public static final Supplier<BlockEntityType<FusionBoilerBlockEntity>> FUSION_BOILER = BLOCK_ENTITY_TYPES.register("fusion_boiler", () -> BlockEntityType.Builder.of(FusionBoilerBlockEntity::new, NtmBlocks.FUSION_BOILER.get()).build(null));
    public static final Supplier<BlockEntityType<FusionMHDTBlockEntity>> FUSION_MHDT = BLOCK_ENTITY_TYPES.register("fusion_mhdt", () -> BlockEntityType.Builder.of(FusionMHDTBlockEntity::new, NtmBlocks.FUSION_MHDT.get()).build(null));
    public static final Supplier<BlockEntityType<FusionKlystronBlockEntity>> FUSION_KLYSTRON = BLOCK_ENTITY_TYPES.register("fusion_klystron", () -> BlockEntityType.Builder.of(FusionKlystronBlockEntity::new, NtmBlocks.FUSION_KLYSTRON.get()).build(null));
    public static final Supplier<BlockEntityType<FusionTorusBlockEntity>> FUSION_TORUS = BLOCK_ENTITY_TYPES.register("fusion_torus", () -> BlockEntityType.Builder.of(FusionTorusBlockEntity::new, NtmBlocks.FUSION_TORUS.get()).build(null));
    public static final Supplier<BlockEntityType<FusionPlasmaForgeBlockEntity>> FUSION_PLASMA_FORGE = BLOCK_ENTITY_TYPES.register("fusion_plasma_forge", () -> BlockEntityType.Builder.of(FusionPlasmaForgeBlockEntity::new, NtmBlocks.FUSION_PLASMA_FORGE.get()).build(null));
    public static final Supplier<BlockEntityType<ReactorResearchBlockEntity>> REACTOR_RESEARCH = BLOCK_ENTITY_TYPES.register("reactor_research", () -> BlockEntityType.Builder.of(ReactorResearchBlockEntity::new, NtmBlocks.REACTOR_RESEARCH.get()).build(null));
    public static final Supplier<BlockEntityType<MachineReactorBreedingBlockEntity>> MACHINE_REACTOR_BREEDING = BLOCK_ENTITY_TYPES.register("machine_reactor_breeding", () -> BlockEntityType.Builder.of(MachineReactorBreedingBlockEntity::new, NtmBlocks.MACHINE_REACTOR_BREEDING.get()).build(null));
    public static final Supplier<BlockEntityType<ReactorControlBlockEntity>> REACTOR_CONTROL = BLOCK_ENTITY_TYPES.register("reactor_control", () -> BlockEntityType.Builder.of(ReactorControlBlockEntity::new, NtmBlocks.REACTOR_CONTROL.get()).build(null));
    public static final Supplier<BlockEntityType<TurretSentryBlockEntity>> TURRET_SENTRY = BLOCK_ENTITY_TYPES.register("turret_sentry", () -> BlockEntityType.Builder.of(TurretSentryBlockEntity::new, NtmBlocks.TURRET_SENTRY.get()).build(null));
    public static final Supplier<BlockEntityType<TurretSentryDamagedBlockEntity>> TURRET_SENTRY_DAMAGED = BLOCK_ENTITY_TYPES.register("turret_sentry_damaged", () -> BlockEntityType.Builder.of(TurretSentryDamagedBlockEntity::new, NtmBlocks.TURRET_SENTRY_DAMAGED.get()).build(null));
    public static final Supplier<BlockEntityType<TurretJeremyBlockEntity>> TURRET_JEREMY = BLOCK_ENTITY_TYPES.register("turret_jeremy", () -> BlockEntityType.Builder.of(TurretJeremyBlockEntity::new, NtmBlocks.TURRET_JEREMY.get()).build(null));
    public static final Supplier<BlockEntityType<TurretHowardBlockEntity>> TURRET_HOWARD = BLOCK_ENTITY_TYPES.register("turret_howard", () -> BlockEntityType.Builder.of(TurretHowardBlockEntity::new, NtmBlocks.TURRET_HOWARD.get()).build(null));
    public static final Supplier<BlockEntityType<TurretHowardDamagedBlockEntity>> TURRET_HOWARD_DAMAGED = BLOCK_ENTITY_TYPES.register("turret_howard_damaged", () -> BlockEntityType.Builder.of(TurretHowardDamagedBlockEntity::new, NtmBlocks.TURRET_HOWARD_DAMAGED.get()).build(null));
    public static final Supplier<BlockEntityType<TurretChekhovBlockEntity>> TURRET_CHEKHOV = BLOCK_ENTITY_TYPES.register("turret_chekhov", () -> BlockEntityType.Builder.of(TurretChekhovBlockEntity::new, NtmBlocks.TURRET_CHEKHOV.get()).build(null));
    public static final Supplier<BlockEntityType<TurretFriendlyBlockEntity>> TURRET_FRIENDLY = BLOCK_ENTITY_TYPES.register("turret_friendly", () -> BlockEntityType.Builder.of(TurretFriendlyBlockEntity::new, NtmBlocks.TURRET_FRIENDLY.get()).build(null));
    public static final Supplier<BlockEntityType<ICFPressBlockEntity>> ICF_PRESS = BLOCK_ENTITY_TYPES.register("icf_press", () -> BlockEntityType.Builder.of(ICFPressBlockEntity::new, NtmBlocks.MACHINE_ICF_PRESS.get()).build(null));
    public static final Supplier<BlockEntityType<ICFStructBlockEntity>> ICF_STRUCT = BLOCK_ENTITY_TYPES.register("struct_icf", () -> BlockEntityType.Builder.of(ICFStructBlockEntity::new, NtmBlocks.STRUCT_ICF.get()).build(null));

    public static final Supplier<BlockEntityType<RadioTorchReceiverBlockEntity>> RADIO_TORCH_RECEIVER = BLOCK_ENTITY_TYPES.register("radio_torch_receiver", () -> BlockEntityType.Builder.of(RadioTorchReceiverBlockEntity::new, NtmBlocks.RADIO_TORCH_RECEIVER.get()).build(null));
    public static final Supplier<BlockEntityType<RadioTorchSenderBlockEntity>> RADIO_TORCH_SENDER = BLOCK_ENTITY_TYPES.register("radio_torch_sender", () -> BlockEntityType.Builder.of(RadioTorchSenderBlockEntity::new, NtmBlocks.RADIO_TORCH_SENDER.get()).build(null));

    public static final Supplier<BlockEntityType<MachineOilWellBlockEntity>> MACHINE_OIL_WELL = BLOCK_ENTITY_TYPES.register("machine_oil_well", () -> BlockEntityType.Builder.of(MachineOilWellBlockEntity::new, NtmBlocks.MACHINE_WELL.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePumpjackBlockEntity>> MACHINE_PUMPJACK = BLOCK_ENTITY_TYPES.register("machine_pumpjack", () -> BlockEntityType.Builder.of(MachinePumpjackBlockEntity::new, NtmBlocks.MACHINE_PUMPJACK.get()).build(null));
    public static final Supplier<BlockEntityType<MachineFrackingTowerBlockEntity>> MACHINE_FRACKING_TOWER = BLOCK_ENTITY_TYPES.register("machine_fracking_tower", () -> BlockEntityType.Builder.of(MachineFrackingTowerBlockEntity::new, NtmBlocks.MACHINE_FRACKING_TOWER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineFractionTowerBlockEntity>> MACHINE_FRACTION_TOWER = BLOCK_ENTITY_TYPES.register("machine_fraction_tower", () -> BlockEntityType.Builder.of(MachineFractionTowerBlockEntity::new, NtmBlocks.MACHINE_FRACTION_TOWER.get()).build(null));
    public static final Supplier<BlockEntityType<SpacerBlockEntity>> FRACTION_SPACER = BLOCK_ENTITY_TYPES.register("fraction_spacer", () -> BlockEntityType.Builder.of(SpacerBlockEntity::new, NtmBlocks.FRACTION_SPACER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCatalyticReformerBlockEntity>> MACHINE_CATALYTIC_REFORMER = BLOCK_ENTITY_TYPES.register("machine_catalytic_reformer", () -> BlockEntityType.Builder.of(MachineCatalyticReformerBlockEntity::new, NtmBlocks.MACHINE_CATALYTIC_REFORMER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineHydrotreaterBlockEntity>> MACHINE_HYDROTREATER = BLOCK_ENTITY_TYPES.register("machine_hydrotreater", () -> BlockEntityType.Builder.of(MachineHydrotreaterBlockEntity::new, NtmBlocks.MACHINE_HYDROTREATER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineVacuumDistillBlockEntity>> MACHINE_VACUUM_DISTILL = BLOCK_ENTITY_TYPES.register("machine_vacuum_distill", () -> BlockEntityType.Builder.of(MachineVacuumDistillBlockEntity::new, NtmBlocks.MACHINE_VACUUM_DISTILL.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCatalyticCrackerBlockEntity>> MACHINE_CATALYTIC_CRACKER = BLOCK_ENTITY_TYPES.register("machine_catalytic_cracker", () -> BlockEntityType.Builder.of(MachineCatalyticCrackerBlockEntity::new, NtmBlocks.MACHINE_CATALYTIC_CRACKER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCokerBlockEntity>> MACHINE_COKER = BLOCK_ENTITY_TYPES.register("machine_coker", () -> BlockEntityType.Builder.of(MachineCokerBlockEntity::new, NtmBlocks.MACHINE_COKER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineGasFlareBlockEntity>> MACHINE_GAS_FLARE = BLOCK_ENTITY_TYPES.register("machine_gas_flare", () -> BlockEntityType.Builder.of(MachineGasFlareBlockEntity::new, NtmBlocks.MACHINE_GAS_FLARE.get()).build(null));
    public static final Supplier<BlockEntityType<MachineLiquefactorBlockEntity>> MACHINE_LIQUEFACTOR = BLOCK_ENTITY_TYPES.register("machine_liquefactor", () -> BlockEntityType.Builder.of(MachineLiquefactorBlockEntity::new, NtmBlocks.MACHINE_LIQUEFACTOR.get()).build(null));
    public static final Supplier<BlockEntityType<MachinePyroOvenBlockEntity>> MACHINE_PYRO_OVEN = BLOCK_ENTITY_TYPES.register("machine_pyro_oven", () -> BlockEntityType.Builder.of(MachinePyroOvenBlockEntity::new, NtmBlocks.MACHINE_PYRO_OVEN.get()).build(null));
    public static final Supplier<BlockEntityType<MachineSolidifierBlockEntity>> MACHINE_SOLIDIFIER = BLOCK_ENTITY_TYPES.register("machine_solidifier", () -> BlockEntityType.Builder.of(MachineSolidifierBlockEntity::new, NtmBlocks.MACHINE_SOLIDIFIER.get()).build(null));

    public static final Supplier<BlockEntityType<MachineRefineryBlockEntity>> MACHINE_REFINERY = BLOCK_ENTITY_TYPES.register(
            "machine_refinery",
            () -> BlockEntityType.Builder.of(
                            MachineRefineryBlockEntity::new,
                            NtmBlocks.MACHINE_REFINERY.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineFurnaceCombinationBlockEntity>> FURNACE_COMBINATION = BLOCK_ENTITY_TYPES.register(
            "furnace_combination",
            () -> BlockEntityType.Builder.of(
                            MachineFurnaceCombinationBlockEntity::new,
                            NtmBlocks.FURNACE_COMBINATION.get())
                    .build(null));

    /* Runde 105: die drei Anschluesse an die Welt. Die Pumpe hat zwei Bauarten und deshalb zwei
     * Typen; beide sitzen im selben Block. */
    public static final Supplier<BlockEntityType<MachineDrainBlockEntity>> MACHINE_DRAIN = BLOCK_ENTITY_TYPES.register(
            "machine_drain",
            () -> BlockEntityType.Builder.of(
                            MachineDrainBlockEntity::new,
                            NtmBlocks.MACHINE_DRAIN.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineIntakeBlockEntity>> MACHINE_INTAKE = BLOCK_ENTITY_TYPES.register(
            "machine_intake",
            () -> BlockEntityType.Builder.of(
                            MachineIntakeBlockEntity::new,
                            NtmBlocks.MACHINE_INTAKE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachinePumpElectricBlockEntity>> MACHINE_PUMP_ELECTRIC = BLOCK_ENTITY_TYPES.register(
            "machine_pump_electric",
            () -> BlockEntityType.Builder.of(
                            MachinePumpElectricBlockEntity::new,
                            NtmBlocks.PUMP_ELECTRIC.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachinePumpSteamBlockEntity>> MACHINE_PUMP_STEAM = BLOCK_ENTITY_TYPES.register(
            "machine_pump_steam",
            () -> BlockEntityType.Builder.of(
                            MachinePumpSteamBlockEntity::new,
                            NtmBlocks.PUMP_STEAM.get())
                    .build(null));

    /* Runde 114: die Montagefabrik. */
    public static final Supplier<BlockEntityType<MachineAssemblyFactoryBlockEntity>> MACHINE_ASSEMBLY_FACTORY = BLOCK_ENTITY_TYPES.register(
            "machine_assembly_factory",
            () -> BlockEntityType.Builder.of(
                            MachineAssemblyFactoryBlockEntity::new,
                            NtmBlocks.MACHINE_ASSEMBLY_FACTORY.get())
                    .build(null));

    /* Runde 113: die Chemiefabrik. */
    public static final Supplier<BlockEntityType<MachineChemicalFactoryBlockEntity>> MACHINE_CHEMICAL_FACTORY = BLOCK_ENTITY_TYPES.register(
            "machine_chemical_factory",
            () -> BlockEntityType.Builder.of(
                            MachineChemicalFactoryBlockEntity::new,
                            NtmBlocks.MACHINE_CHEMICAL_FACTORY.get())
                    .build(null));

    /* Runde 112: die Schluesselschmiede und der Strommelder. */
    public static final Supplier<BlockEntityType<MachineKeyForgeBlockEntity>> MACHINE_KEY_FORGE = BLOCK_ENTITY_TYPES.register(
            "machine_keyforge",
            () -> BlockEntityType.Builder.of(
                            MachineKeyForgeBlockEntity::new,
                            NtmBlocks.MACHINE_KEY_FORGE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineDetectorBlockEntity>> MACHINE_DETECTOR = BLOCK_ENTITY_TYPES.register(
            "machine_detector",
            () -> BlockEntityType.Builder.of(
                            MachineDetectorBlockEntity::new,
                            NtmBlocks.MACHINE_DETECTOR.get())
                    .build(null));

    /* Runde 111: der Teleporter. */
    public static final Supplier<BlockEntityType<MachineTeleporterBlockEntity>> MACHINE_TELEPORTER = BLOCK_ENTITY_TYPES.register(
            "machine_teleporter",
            () -> BlockEntityType.Builder.of(
                            MachineTeleporterBlockEntity::new,
                            NtmBlocks.MACHINE_TELEPORTER.get())
                    .build(null));

    /* Runde 110: der Hephaestus. */
    public static final Supplier<BlockEntityType<MachineHephaestusBlockEntity>> MACHINE_HEPHAESTUS = BLOCK_ENTITY_TYPES.register(
            "machine_hephaestus",
            () -> BlockEntityType.Builder.of(
                            MachineHephaestusBlockEntity::new,
                            NtmBlocks.MACHINE_HEPHAESTUS.get())
                    .build(null));

    /* Runde 109: der Strangguss. */
    public static final Supplier<BlockEntityType<MachineStrandCasterBlockEntity>> MACHINE_STRAND_CASTER = BLOCK_ENTITY_TYPES.register(
            "machine_strand_caster",
            () -> BlockEntityType.Builder.of(
                            MachineStrandCasterBlockEntity::new,
                            NtmBlocks.MACHINE_STRAND_CASTER.get())
                    .build(null));

    /* Runde 108: die Industrieturbine. */
    public static final Supplier<BlockEntityType<MachineIndustrialTurbineBlockEntity>> MACHINE_INDUSTRIAL_TURBINE = BLOCK_ENTITY_TYPES.register(
            "machine_industrial_turbine",
            () -> BlockEntityType.Builder.of(
                            MachineIndustrialTurbineBlockEntity::new,
                            NtmBlocks.MACHINE_INDUSTRIAL_TURBINE.get())
                    .build(null));

    /* Runde 107: die elektrische Presse und der Trichter. */
    public static final Supplier<BlockEntityType<MachineEPressBlockEntity>> MACHINE_EPRESS = BLOCK_ENTITY_TYPES.register(
            "machine_epress",
            () -> BlockEntityType.Builder.of(
                            MachineEPressBlockEntity::new,
                            NtmBlocks.MACHINE_EPRESS.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineFunnelBlockEntity>> MACHINE_FUNNEL = BLOCK_ENTITY_TYPES.register(
            "machine_funnel",
            () -> BlockEntityType.Builder.of(
                            MachineFunnelBlockEntity::new,
                            NtmBlocks.MACHINE_FUNNEL.get())
                    .build(null));

    /* Runde 106: der Selbstbauer. */
    public static final Supplier<BlockEntityType<MachineAutocrafterBlockEntity>> MACHINE_AUTOCRAFTER = BLOCK_ENTITY_TYPES.register(
            "machine_autocrafter",
            () -> BlockEntityType.Builder.of(
                            MachineAutocrafterBlockEntity::new,
                            NtmBlocks.MACHINE_AUTOCRAFTER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineBlastFurnaceBlockEntity>> MACHINE_BLAST_FURNACE = BLOCK_ENTITY_TYPES.register(
            "machine_blast_furnace",
            () -> BlockEntityType.Builder.of(
                            MachineBlastFurnaceBlockEntity::new,
                            NtmBlocks.MACHINE_BLAST_FURNACE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineWoodBurnerBlockEntity>> MACHINE_WOOD_BURNER = BLOCK_ENTITY_TYPES.register(
            "machine_wood_burner",
            () -> BlockEntityType.Builder.of(
                            MachineWoodBurnerBlockEntity::new,
                            NtmBlocks.MACHINE_WOOD_BURNER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineCentrifugeBlockEntity>> MACHINE_CENTRIFUGE = BLOCK_ENTITY_TYPES.register(
            "machine_centrifuge",
            () -> BlockEntityType.Builder.of(
                            MachineCentrifugeBlockEntity::new,
                            NtmBlocks.MACHINE_CENTRIFUGE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineRotaryFurnaceBlockEntity>> MACHINE_ROTARY_FURNACE = BLOCK_ENTITY_TYPES.register(
            "machine_rotary_furnace",
            () -> BlockEntityType.Builder.of(
                            MachineRotaryFurnaceBlockEntity::new,
                            NtmBlocks.MACHINE_ROTARY_FURNACE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKBoilerBlockEntity>> RBMK_BOILER = BLOCK_ENTITY_TYPES.register(
            "rbmk_boiler",
            () -> BlockEntityType.Builder.of(
                            RBMKBoilerBlockEntity::new,
                            NtmBlocks.RBMK_BOILER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKCoolerBlockEntity>> RBMK_COOLER = BLOCK_ENTITY_TYPES.register(
            "rbmk_cooler",
            () -> BlockEntityType.Builder.of(
                            RBMKCoolerBlockEntity::new,
                            NtmBlocks.RBMK_COOLER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKStorageBlockEntity>> RBMK_STORAGE = BLOCK_ENTITY_TYPES.register(
            "rbmk_storage",
            () -> BlockEntityType.Builder.of(
                            RBMKStorageBlockEntity::new,
                            NtmBlocks.RBMK_STORAGE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneInserterBlockEntity>> CRANE_INSERTER = BLOCK_ENTITY_TYPES.register(
            "crane_inserter",
            () -> BlockEntityType.Builder.of(
                            CraneInserterBlockEntity::new,
                            NtmBlocks.CRANE_INSERTER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneExtractorBlockEntity>> CRANE_EXTRACTOR = BLOCK_ENTITY_TYPES.register(
            "crane_extractor",
            () -> BlockEntityType.Builder.of(
                            CraneExtractorBlockEntity::new,
                            NtmBlocks.CRANE_EXTRACTOR.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneGrabberBlockEntity>> CRANE_GRABBER = BLOCK_ENTITY_TYPES.register(
            "crane_grabber",
            () -> BlockEntityType.Builder.of(
                            CraneGrabberBlockEntity::new,
                            NtmBlocks.CRANE_GRABBER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneBoxerBlockEntity>> CRANE_BOXER = BLOCK_ENTITY_TYPES.register(
            "crane_boxer",
            () -> BlockEntityType.Builder.of(
                            CraneBoxerBlockEntity::new,
                            NtmBlocks.CRANE_BOXER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneUnboxerBlockEntity>> CRANE_UNBOXER = BLOCK_ENTITY_TYPES.register(
            "crane_unboxer",
            () -> BlockEntityType.Builder.of(
                            CraneUnboxerBlockEntity::new,
                            NtmBlocks.CRANE_UNBOXER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneRouterBlockEntity>> CRANE_ROUTER = BLOCK_ENTITY_TYPES.register(
            "crane_router",
            () -> BlockEntityType.Builder.of(
                            CraneRouterBlockEntity::new,
                            NtmBlocks.CRANE_ROUTER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CranePartitionerBlockEntity>> CRANE_PARTITIONER = BLOCK_ENTITY_TYPES.register(
            "crane_partitioner",
            () -> BlockEntityType.Builder.of(
                            CranePartitionerBlockEntity::new,
                            NtmBlocks.CRANE_PARTITIONER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneSplitterBlockEntity>> CRANE_SPLITTER = BLOCK_ENTITY_TYPES.register(
            "crane_splitter",
            () -> BlockEntityType.Builder.of(
                            CraneSplitterBlockEntity::new,
                            NtmBlocks.CRANE_SPLITTER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKHeaterBlockEntity>> RBMK_HEATER = BLOCK_ENTITY_TYPES.register(
            "rbmk_heater",
            () -> BlockEntityType.Builder.of(
                            RBMKHeaterBlockEntity::new,
                            NtmBlocks.RBMK_HEATER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKInletBlockEntity>> RBMK_INLET = BLOCK_ENTITY_TYPES.register(
            "rbmk_inlet",
            () -> BlockEntityType.Builder.of(
                            RBMKInletBlockEntity::new,
                            NtmBlocks.RBMK_STEAM_INLET.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKOutletBlockEntity>> RBMK_OUTLET = BLOCK_ENTITY_TYPES.register(
            "rbmk_outlet",
            () -> BlockEntityType.Builder.of(
                            RBMKOutletBlockEntity::new,
                            NtmBlocks.RBMK_STEAM_OUTLET.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKOutgasserBlockEntity>> RBMK_OUTGASSER = BLOCK_ENTITY_TYPES.register(
            "rbmk_outgasser",
            () -> BlockEntityType.Builder.of(
                            RBMKOutgasserBlockEntity::new,
                            NtmBlocks.RBMK_OUTGASSER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKRodReaSimBlockEntity>> RBMK_ROD_REASIM = BLOCK_ENTITY_TYPES.register(
            "rbmk_rod_reasim",
            () -> BlockEntityType.Builder.of(
                            RBMKRodReaSimBlockEntity::new,
                            NtmBlocks.RBMK_ROD_REASIM.get(),
                            NtmBlocks.RBMK_ROD_REASIM_MOD.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKControlAutoBlockEntity>> RBMK_CONTROL_AUTO = BLOCK_ENTITY_TYPES.register(
            "rbmk_control_auto",
            () -> BlockEntityType.Builder.of(
                            RBMKControlAutoBlockEntity::new,
                            NtmBlocks.RBMK_CONTROL_AUTO.get(),
                            NtmBlocks.RBMK_CONTROL_REASIM_AUTO.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKConsoleBlockEntity>> RBMK_CONSOLE = BLOCK_ENTITY_TYPES.register(
            "rbmk_console",
            () -> BlockEntityType.Builder.of(
                            RBMKConsoleBlockEntity::new,
                            NtmBlocks.RBMK_CONSOLE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<WasteDrumBlockEntity>> WASTE_DRUM = BLOCK_ENTITY_TYPES.register(
            "waste_drum",
            () -> BlockEntityType.Builder.of(
                            WasteDrumBlockEntity::new,
                            NtmBlocks.MACHINE_WASTE_DRUM.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKAutoloaderBlockEntity>> RBMK_AUTOLOADER = BLOCK_ENTITY_TYPES.register(
            "rbmk_autoloader",
            () -> BlockEntityType.Builder.of(
                            RBMKAutoloaderBlockEntity::new,
                            NtmBlocks.RBMK_AUTOLOADER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CraneConsoleBlockEntity>> CRANE_CONSOLE = BLOCK_ENTITY_TYPES.register(
            "rbmk_crane_console",
            () -> BlockEntityType.Builder.of(
                            CraneConsoleBlockEntity::new,
                            NtmBlocks.RBMK_CRANE_CONSOLE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKPassiveBlockEntity>> RBMK_PASSIVE = BLOCK_ENTITY_TYPES.register(
            "rbmk_passive",
            () -> BlockEntityType.Builder.of(
                            RBMKPassiveBlockEntity::new,
                            NtmBlocks.RBMK_BLANK.get(),
                            NtmBlocks.RBMK_MODERATOR.get(),
                            NtmBlocks.RBMK_ABSORBER.get(),
                            NtmBlocks.RBMK_REFLECTOR.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKRodBlockEntity>> RBMK_ROD = BLOCK_ENTITY_TYPES.register(
            "rbmk_rod",
            () -> BlockEntityType.Builder.of(
                            RBMKRodBlockEntity::new,
                            NtmBlocks.RBMK_ROD.get(),
                            NtmBlocks.RBMK_ROD_MOD.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKControlBlockEntity>> RBMK_CONTROL = BLOCK_ENTITY_TYPES.register(
            "rbmk_control",
            () -> BlockEntityType.Builder.of(
                            RBMKControlBlockEntity::new,
                            NtmBlocks.RBMK_CONTROL.get(),
                            NtmBlocks.RBMK_CONTROL_MOD.get(),
                            NtmBlocks.RBMK_CONTROL_REASIM.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineCrucibleBlockEntity>> MACHINE_CRUCIBLE = BLOCK_ENTITY_TYPES.register(
            "machine_crucible",
            () -> BlockEntityType.Builder.of(
                            MachineCrucibleBlockEntity::new,
                            NtmBlocks.MACHINE_CRUCIBLE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<FoundryChannelBlockEntity>> FOUNDRY_CHANNEL = BLOCK_ENTITY_TYPES.register(
            "foundry_channel",
            () -> BlockEntityType.Builder.of(
                            FoundryChannelBlockEntity::new,
                            NtmBlocks.FOUNDRY_CHANNEL.get())
                    .build(null));
    public static final Supplier<BlockEntityType<FoundryMoldBlockEntity>> FOUNDRY_MOLD = BLOCK_ENTITY_TYPES.register(
            "foundry_mold",
            () -> BlockEntityType.Builder.of(
                            FoundryMoldBlockEntity::new,
                            NtmBlocks.FOUNDRY_MOLD.get())
                    .build(null));
    public static final Supplier<BlockEntityType<FoundryBasinBlockEntity>> FOUNDRY_BASIN = BLOCK_ENTITY_TYPES.register(
            "foundry_basin",
            () -> BlockEntityType.Builder.of(
                            FoundryBasinBlockEntity::new,
                            NtmBlocks.FOUNDRY_BASIN.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineArcFurnaceLargeBlockEntity>> MACHINE_ARC_FURNACE = BLOCK_ENTITY_TYPES.register(
            "machine_arc_furnace",
            () -> BlockEntityType.Builder.of(
                            MachineArcFurnaceLargeBlockEntity::new,
                            NtmBlocks.MACHINE_ARC_FURNACE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKIndicatorBlockEntity>> RBMK_INDICATOR = BLOCK_ENTITY_TYPES.register(
            "rbmk_indicator",
            () -> BlockEntityType.Builder.of(
                            RBMKIndicatorBlockEntity::new,
                            NtmBlocks.RBMK_INDICATOR.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKNumitronBlockEntity>> RBMK_NUMITRON = BLOCK_ENTITY_TYPES.register(
            "rbmk_numitron",
            () -> BlockEntityType.Builder.of(
                            RBMKNumitronBlockEntity::new,
                            NtmBlocks.RBMK_NUMITRON.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKLeverBlockEntity>> RBMK_LEVER = BLOCK_ENTITY_TYPES.register(
            "rbmk_lever",
            () -> BlockEntityType.Builder.of(
                            RBMKLeverBlockEntity::new,
                            NtmBlocks.RBMK_LEVER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKKeyPadBlockEntity>> RBMK_KEYPAD = BLOCK_ENTITY_TYPES.register(
            "rbmk_keypad",
            () -> BlockEntityType.Builder.of(
                            RBMKKeyPadBlockEntity::new,
                            NtmBlocks.RBMK_KEYPAD.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKGraphBlockEntity>> RBMK_GRAPH = BLOCK_ENTITY_TYPES.register(
            "rbmk_graph",
            () -> BlockEntityType.Builder.of(
                            RBMKGraphBlockEntity::new,
                            NtmBlocks.RBMK_GRAPH.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKDisplayBlockEntity>> RBMK_DISPLAY = BLOCK_ENTITY_TYPES.register(
            "rbmk_display",
            () -> BlockEntityType.Builder.of(
                            RBMKDisplayBlockEntity::new,
                            NtmBlocks.RBMK_DISPLAY.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKTerminalBlockEntity>> RBMK_TERMINAL = BLOCK_ENTITY_TYPES.register(
            "rbmk_terminal",
            () -> BlockEntityType.Builder.of(
                            RBMKTerminalBlockEntity::new,
                            NtmBlocks.RBMK_TERMINAL.get())
                    .build(null));
    public static final Supplier<BlockEntityType<RBMKGaugeBlockEntity>> RBMK_GAUGE = BLOCK_ENTITY_TYPES.register(
            "rbmk_gauge",
            () -> BlockEntityType.Builder.of(
                            RBMKGaugeBlockEntity::new,
                            NtmBlocks.RBMK_GAUGE.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachinePUREXBlockEntity>> MACHINE_PUREX = BLOCK_ENTITY_TYPES.register(
            "machine_purex",
            () -> BlockEntityType.Builder.of(
                            MachinePUREXBlockEntity::new,
                            NtmBlocks.MACHINE_PUREX.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineRadiolysisBlockEntity>> MACHINE_RADIOLYSIS = BLOCK_ENTITY_TYPES.register(
            "machine_radiolysis",
            () -> BlockEntityType.Builder.of(
                            MachineRadiolysisBlockEntity::new,
                            NtmBlocks.MACHINE_RADIOLYSIS.get())
                    .build(null));
    public static final Supplier<BlockEntityType<MachineChemicalPlantBlockEntity>> MACHINE_CHEMICAL_PLANT = BLOCK_ENTITY_TYPES.register(
            "machine_chemical_plant",
            () -> BlockEntityType.Builder.of(
                            MachineChemicalPlantBlockEntity::new,
                            NtmBlocks.MACHINE_CHEMICAL_PLANT.get())
                    .build(null));

    public static final Supplier<BlockEntityType<BarrelBlockEntity>> BARREL = BLOCK_ENTITY_TYPES.register(
            "barrel",
            () -> BlockEntityType.Builder.of(
                            BarrelBlockEntity::new,
                            NtmBlocks.BARREL_PLASTIC.get(),
                            NtmBlocks.BARREL_STEEL.get(),
                            NtmBlocks.BARREL_TCALLOY.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineSatLinkerBlockEntity>> MACHINE_SATLINKER = BLOCK_ENTITY_TYPES.register(
            "machine_satlinker",
            () -> BlockEntityType.Builder.of(
                            MachineSatLinkerBlockEntity::new,
                            NtmBlocks.MACHINE_SATLINKER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineAnnihilatorBlockEntity>> MACHINE_ANNIHILATOR = BLOCK_ENTITY_TYPES.register(
            "machine_annihilator",
            () -> BlockEntityType.Builder.of(
                            MachineAnnihilatorBlockEntity::new,
                            NtmBlocks.MACHINE_ANNIHILATOR.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineSirenBlockEntity>> MACHINE_SIREN = BLOCK_ENTITY_TYPES.register(
            "machine_siren",
            () -> BlockEntityType.Builder.of(
                            MachineSirenBlockEntity::new,
                            NtmBlocks.MACHINE_SIREN.get())
                    .build(null));

    public static final Supplier<BlockEntityType<RadarScreenBlockEntity>> RADAR_SCREEN = BLOCK_ENTITY_TYPES.register(
            "radar_screen",
            () -> BlockEntityType.Builder.of(
                            RadarScreenBlockEntity::new,
                            NtmBlocks.RADAR_SCREEN.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineAmmoPressBlockEntity>> MACHINE_AMMO_PRESS = BLOCK_ENTITY_TYPES.register(
            "machine_ammo_press",
            () -> BlockEntityType.Builder.of(
                            MachineAmmoPressBlockEntity::new,
                            NtmBlocks.MACHINE_AMMO_PRESS.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineSuperComputerBlockEntity>> MACHINE_SUPER_COMPUTER = BLOCK_ENTITY_TYPES.register(
            "machine_supercomputer",
            () -> BlockEntityType.Builder.of(
                            MachineSuperComputerBlockEntity::new,
                            NtmBlocks.MACHINE_SUPER_COMPUTER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineTapeDriveBlockEntity>> MACHINE_TAPE_DRIVE = BLOCK_ENTITY_TYPES.register(
            "machine_tape_drive",
            () -> BlockEntityType.Builder.of(
                            MachineTapeDriveBlockEntity::new,
                            NtmBlocks.MACHINE_TAPE_DRIVE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineSatLinkBlockEntity>> MACHINE_SAT_LINK = BLOCK_ENTITY_TYPES.register(
            "machine_satlink",
            () -> BlockEntityType.Builder.of(
                            MachineSatLinkBlockEntity::new,
                            NtmBlocks.MACHINE_SAT_LINK.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineSatDockBlockEntity>> MACHINE_SAT_DOCK = BLOCK_ENTITY_TYPES.register(
            "sat_dock",
            () -> BlockEntityType.Builder.of(
                            MachineSatDockBlockEntity::new,
                            NtmBlocks.MACHINE_SAT_DOCK.get())
                    .build(null));

    public static final Supplier<BlockEntityType<CrateIronBlockEntity>> CRATE_IRON = BLOCK_ENTITY_TYPES.register(
            "crate_iron",
            () -> BlockEntityType.Builder.of(
                            CrateIronBlockEntity::new,
                            NtmBlocks.CRATE_IRON.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CrateTungstenBlockEntity>> CRATE_TUNGSTEN = BLOCK_ENTITY_TYPES.register(
            "crate_tungsten",
            () -> BlockEntityType.Builder.of(
                            CrateTungstenBlockEntity::new,
                            NtmBlocks.CRATE_TUNGSTEN.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CrateSteelBlockEntity>> CRATE_STEEL = BLOCK_ENTITY_TYPES.register(
            "crate_steel",
            () -> BlockEntityType.Builder.of(
                            CrateSteelBlockEntity::new,
                            NtmBlocks.CRATE_STEEL.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CrateDeshBlockEntity>> CRATE_DESH = BLOCK_ENTITY_TYPES.register(
            "crate_desh",
            () -> BlockEntityType.Builder.of(
                            CrateDeshBlockEntity::new,
                            NtmBlocks.CRATE_DESH.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CrateTemplateBlockEntity>> CRATE_TEMPLATE = BLOCK_ENTITY_TYPES.register(
            "crate_template",
            () -> BlockEntityType.Builder.of(
                            CrateTemplateBlockEntity::new,
                            NtmBlocks.CRATE_TEMPLATE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachinePressBlockEntity>> PRESS = BLOCK_ENTITY_TYPES.register("press", () -> BlockEntityType.Builder.of(MachinePressBlockEntity::new, NtmBlocks.MACHINE_PRESS.get()).build(null));

    public static final Supplier<BlockEntityType<MachineBigAssTankBlockEntity>> MACHINE_BIGASSTANK = BLOCK_ENTITY_TYPES.register("machine_bigasstank", () -> BlockEntityType.Builder.of(MachineBigAssTankBlockEntity::new, NtmBlocks.MACHINE_BIGASSTANK.get()).build(null));
    public static final Supplier<BlockEntityType<MachineFluidTankBlockEntity>> FLUID_TANK = BLOCK_ENTITY_TYPES.register("fluid_tank", () -> BlockEntityType.Builder.of(MachineFluidTankBlockEntity::new, NtmBlocks.MACHINE_FLUID_TANK.get()).build(null));
    public static final Supplier<BlockEntityType<ChungusBlockEntity>> MACHINE_CHUNGUS = BLOCK_ENTITY_TYPES.register("machine_chungus", () -> BlockEntityType.Builder.of(ChungusBlockEntity::new, NtmBlocks.MACHINE_CHUNGUS.get()).build(null));

    public static final Supplier<BlockEntityType<BatterySocketBlockEntity>> BATTERY_SOCKET = BLOCK_ENTITY_TYPES.register(
            "battery_socket",
            () -> BlockEntityType.Builder.of(
                            BatterySocketBlockEntity::new,
                            NtmBlocks.MACHINE_BATTERY_SOCKET.get())
                    .build(null));

    public static final Supplier<BlockEntityType<BatteryREDDBlockEntity>> BATTERY_REDD = BLOCK_ENTITY_TYPES.register(
            "battery_redd",
            () -> BlockEntityType.Builder.of(
                            BatteryREDDBlockEntity::new,
                            NtmBlocks.MACHINE_BATTERY_REDD.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineAssemblyMachineBlockEntity>> ASSEMBLY_MACHINE = BLOCK_ENTITY_TYPES.register(
            "assembly_machine",
            () -> BlockEntityType.Builder.of(
                            MachineAssemblyMachineBlockEntity::new,
                            NtmBlocks.MACHINE_ASSEMBLY_MACHINE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachinePrecAssBlockEntity>> MACHINE_PRECASS = BLOCK_ENTITY_TYPES.register(
            "machine_precass",
            () -> BlockEntityType.Builder.of(
                            MachinePrecAssBlockEntity::new,
                            NtmBlocks.MACHINE_PRECASS.get())
                    .build(null));

    public static final Supplier<BlockEntityType<BedrockOreBlockEntity>> BEDROCK_ORE = BLOCK_ENTITY_TYPES.register(
            "bedrock_ore",
            () -> BlockEntityType.Builder.of(
                            BedrockOreBlockEntity::new,
                            NtmBlocks.ORE_BEDROCK.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineOreSlopperBlockEntity>> MACHINE_ORE_SLOPPER = BLOCK_ENTITY_TYPES.register(
            "machine_ore_slopper",
            () -> BlockEntityType.Builder.of(
                            MachineOreSlopperBlockEntity::new,
                            NtmBlocks.MACHINE_ORE_SLOPPER.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineExcavatorBlockEntity>> MACHINE_EXCAVATOR = BLOCK_ENTITY_TYPES.register(
            "machine_excavator",
            () -> BlockEntityType.Builder.of(
                            MachineExcavatorBlockEntity::new,
                            NtmBlocks.MACHINE_EXCAVATOR.get())
                    .build(null));

    public static final Supplier<BlockEntityType<MachineMissileAssemblyBlockEntity>> MACHINE_MISSILE_ASSEMBLY = BLOCK_ENTITY_TYPES.register(
            "machine_missile_assembly",
            () -> BlockEntityType.Builder.of(
                            MachineMissileAssemblyBlockEntity::new,
                            NtmBlocks.MACHINE_MISSILE_ASSEMBLY.get())
                    .build(null));

    public static final Supplier<BlockEntityType<ProxyComboBlockEntity>> PROXY_COMBO = BLOCK_ENTITY_TYPES.register("proxy_combo", () -> BlockEntityType.Builder.of(ProxyComboBlockEntity::new).build(null));
    public static final Supplier<BlockEntityType<ProxyDynBlockEntity>> PROXY_DYN = BLOCK_ENTITY_TYPES.register("proxy_dyn", () -> BlockEntityType.Builder.of(ProxyDynBlockEntity::new).build(null));

    public static final Supplier<BlockEntityType<BobbleBlockEntity>> BOBBLEHEAD = BLOCK_ENTITY_TYPES.register("bobblehead", () -> BlockEntityType.Builder.of(BobbleBlockEntity::new, NtmBlocks.BOBBLEHEAD.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> PLUSHIE = BLOCK_ENTITY_TYPES.register("plushie", () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, NtmBlocks.PLUSHIE.get()).build(null));

    public static final Supplier<BlockEntityType<CableBaseBlockEntity>> NETWORK_CABLE = BLOCK_ENTITY_TYPES.register("network_cable", () -> BlockEntityType.Builder.of(CableBaseBlockEntity::new, NtmBlocks.RED_CABLE.get(), NtmBlocks.RED_WIRE_COATED.get()).build(null));
    public static final Supplier<BlockEntityType<CableSwitchBlockEntity>> NETWORK_CABLE_SWITCH = BLOCK_ENTITY_TYPES.register("network_cable_switch", () -> BlockEntityType.Builder.of(CableSwitchBlockEntity::new, NtmBlocks.CABLE_SWITCH.get(), NtmBlocks.CABLE_DETECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<ConnectorBlockEntity>> NETWORK_CONNECTOR = BLOCK_ENTITY_TYPES.register("network_connector", () -> BlockEntityType.Builder.of(ConnectorBlockEntity::new, NtmBlocks.RED_CONNECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<ConnectorSuperBlockEntity>> NETWORK_CONNECTOR_SUPER = BLOCK_ENTITY_TYPES.register("network_connector_super", () -> BlockEntityType.Builder.of(ConnectorSuperBlockEntity::new, NtmBlocks.RED_CONNECTOR_SUPER.get()).build(null));
    public static final Supplier<BlockEntityType<PylonBlockEntity>> NETWORK_PYLON = BLOCK_ENTITY_TYPES.register("network_pylon", () -> BlockEntityType.Builder.of(PylonBlockEntity::new, NtmBlocks.RED_PYLON.get(), NtmBlocks.RED_PYLON_STEEL.get()).build(null));
    public static final Supplier<BlockEntityType<PylonMediumBlockEntity>> NETWORK_PYLON_MEDIUM = BLOCK_ENTITY_TYPES.register("network_pylon_medium", () -> BlockEntityType.Builder.of(PylonMediumBlockEntity::new, NtmBlocks.RED_PYLON_MEDIUM_WOOD.get(), NtmBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get(), NtmBlocks.RED_PYLON_MEDIUM_STEEL.get(), NtmBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get()).build(null));
    public static final Supplier<BlockEntityType<PylonLargeBlockEntity>> NETWORK_PYLON_LARGE = BLOCK_ENTITY_TYPES.register("network_pylon_large", () -> BlockEntityType.Builder.of(PylonLargeBlockEntity::new, NtmBlocks.RED_PYLON_LARGE.get()).build(null));
    public static final Supplier<BlockEntityType<SubstationBlockEntity>> NETWORK_SUBSTATION = BLOCK_ENTITY_TYPES.register("network_substation", () -> BlockEntityType.Builder.of(SubstationBlockEntity::new, NtmBlocks.SUBSTATION.get()).build(null));
    public static final Supplier<BlockEntityType<MachineDiFurnaceBlockEntity>> MACHINE_DIFURNACE = BLOCK_ENTITY_TYPES.register("machine_difurnace", () -> BlockEntityType.Builder.of(MachineDiFurnaceBlockEntity::new, NtmBlocks.MACHINE_DIFURNACE.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCombustionEngineBlockEntity>> MACHINE_COMBUSTION_ENGINE = BLOCK_ENTITY_TYPES.register("machine_combustion_engine", () -> BlockEntityType.Builder.of(MachineCombustionEngineBlockEntity::new, NtmBlocks.MACHINE_COMBUSTION_ENGINE.get()).build(null));
    public static final Supplier<BlockEntityType<MachineTurbineGasBlockEntity>> MACHINE_TURBINEGAS = BLOCK_ENTITY_TYPES.register("machine_turbinegas", () -> BlockEntityType.Builder.of(MachineTurbineGasBlockEntity::new, NtmBlocks.MACHINE_TURBINEGAS.get()).build(null));
    public static final Supplier<BlockEntityType<MachineTurbofanBlockEntity>> MACHINE_TURBOFAN = BLOCK_ENTITY_TYPES.register("machine_turbofan", () -> BlockEntityType.Builder.of(MachineTurbofanBlockEntity::new, NtmBlocks.MACHINE_TURBOFAN.get()).build(null));
    public static final Supplier<BlockEntityType<MachineCrystallizerBlockEntity>> MACHINE_CRYSTALLIZER = BLOCK_ENTITY_TYPES.register("machine_crystallizer", () -> BlockEntityType.Builder.of(MachineCrystallizerBlockEntity::new, NtmBlocks.MACHINE_CRYSTALLIZER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineAutosawBlockEntity>> MACHINE_AUTOSAW = BLOCK_ENTITY_TYPES.register("machine_autosaw", () -> BlockEntityType.Builder.of(MachineAutosawBlockEntity::new, NtmBlocks.MACHINE_AUTOSAW.get()).build(null));
    public static final Supplier<BlockEntityType<MachineThresherBlockEntity>> MACHINE_THRESHER = BLOCK_ENTITY_TYPES.register("machine_thresher", () -> BlockEntityType.Builder.of(MachineThresherBlockEntity::new, NtmBlocks.MACHINE_THRESHER.get()).build(null));
    public static final Supplier<BlockEntityType<SawmillBlockEntity>> SAWMILL = BLOCK_ENTITY_TYPES.register("sawmill", () -> BlockEntityType.Builder.of(SawmillBlockEntity::new, NtmBlocks.MACHINE_SAWMILL.get()).build(null));
    public static final Supplier<BlockEntityType<MachineRTGBlockEntity>> MACHINE_RTG = BLOCK_ENTITY_TYPES.register("machine_rtg", () -> BlockEntityType.Builder.of(MachineRTGBlockEntity::new, NtmBlocks.MACHINE_RTG.get()).build(null));
    public static final Supplier<BlockEntityType<CondenserBlockEntity>> CONDENSER = BLOCK_ENTITY_TYPES.register("condenser", () -> BlockEntityType.Builder.of(CondenserBlockEntity::new, NtmBlocks.MACHINE_CONDENSER.get()).build(null));
    public static final Supplier<BlockEntityType<TowerSmallBlockEntity>> MACHINE_TOWER_SMALL = BLOCK_ENTITY_TYPES.register("machine_tower_small", () -> BlockEntityType.Builder.of(TowerSmallBlockEntity::new, NtmBlocks.MACHINE_TOWER_SMALL.get()).build(null));
    public static final Supplier<BlockEntityType<TowerLargeBlockEntity>> MACHINE_TOWER_LARGE = BLOCK_ENTITY_TYPES.register("machine_tower_large", () -> BlockEntityType.Builder.of(TowerLargeBlockEntity::new, NtmBlocks.MACHINE_TOWER_LARGE.get()).build(null));
    public static final Supplier<BlockEntityType<CondenserPoweredBlockEntity>> CONDENSER_POWERED = BLOCK_ENTITY_TYPES.register("condenser_powered", () -> BlockEntityType.Builder.of(CondenserPoweredBlockEntity::new, NtmBlocks.MACHINE_CONDENSER_POWERED.get()).build(null));
    public static final Supplier<BlockEntityType<AshpitBlockEntity>> MACHINE_ASHPIT = BLOCK_ENTITY_TYPES.register("machine_ashpit", () -> BlockEntityType.Builder.of(AshpitBlockEntity::new, NtmBlocks.MACHINE_ASHPIT.get()).build(null));
    public static final Supplier<BlockEntityType<ChimneyBrickBlockEntity>> CHIMNEY_BRICK = BLOCK_ENTITY_TYPES.register("chimney_brick", () -> BlockEntityType.Builder.of(ChimneyBrickBlockEntity::new, NtmBlocks.CHIMNEY_BRICK.get()).build(null));
    public static final Supplier<BlockEntityType<ChimneyIndustrialBlockEntity>> CHIMNEY_INDUSTRIAL = BLOCK_ENTITY_TYPES.register("chimney_industrial", () -> BlockEntityType.Builder.of(ChimneyIndustrialBlockEntity::new, NtmBlocks.CHIMNEY_INDUSTRIAL.get()).build(null));
    public static final Supplier<BlockEntityType<ConverterHeRfBlockEntity>> CONVERTER_HE_RF = BLOCK_ENTITY_TYPES.register("converter_he_rf", () -> BlockEntityType.Builder.of(ConverterHeRfBlockEntity::new, NtmBlocks.MACHINE_CONVERTER_HE_RF.get()).build(null));
    public static final Supplier<BlockEntityType<ConverterRfHeBlockEntity>> CONVERTER_RF_HE = BLOCK_ENTITY_TYPES.register("converter_rf_he", () -> BlockEntityType.Builder.of(ConverterRfHeBlockEntity::new, NtmBlocks.MACHINE_CONVERTER_RF_HE.get()).build(null));
    public static final Supplier<BlockEntityType<DiodeBlockEntity>> NETWORK_CABLE_DIODE = BLOCK_ENTITY_TYPES.register("network_cable_diode", () -> BlockEntityType.Builder.of(DiodeBlockEntity::new, NtmBlocks.CABLE_DIODE.get()).build(null));
    public static final Supplier<BlockEntityType<CableGaugeBlockEntity>> NETWORK_CABLE_GAUGE = BLOCK_ENTITY_TYPES.register("network_cable_gauge", () -> BlockEntityType.Builder.of(CableGaugeBlockEntity::new, NtmBlocks.RED_CABLE_GAUGE.get()).build(null));
    public static final Supplier<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY = BLOCK_ENTITY_TYPES.register("machine_battery", () -> BlockEntityType.Builder.of(MachineBatteryBlockEntity::new, NtmBlocks.MACHINE_BATTERY_POTATO.get(), NtmBlocks.MACHINE_BATTERY.get(), NtmBlocks.MACHINE_LITHIUM_BATTERY.get(), NtmBlocks.MACHINE_SCHRABIDIUM_BATTERY.get(), NtmBlocks.MACHINE_DINEUTRONIUM_BATTERY.get()).build(null));
    public static final Supplier<BlockEntityType<DiFurnaceExtensionBlockEntity>> MACHINE_DIFURNACE_EXTENSION = BLOCK_ENTITY_TYPES.register("machine_difurnace_extension", () -> BlockEntityType.Builder.of(DiFurnaceExtensionBlockEntity::new, NtmBlocks.MACHINE_DIFURNACE_EXTENSION.get()).build(null));
    public static final Supplier<BlockEntityType<PipeBaseBlockEntity>> FLUID_DUCT = BLOCK_ENTITY_TYPES.register("fluid_duct", () -> BlockEntityType.Builder.of(PipeBaseBlockEntity::new, NtmBlocks.FLUID_DUCT_NEO.get()).build(null));
    public static final Supplier<BlockEntityType<PipeGaugeBlockEntity>> FLUID_DUCT_GAUGE = BLOCK_ENTITY_TYPES.register("fluid_duct_gauge", () -> BlockEntityType.Builder.of(PipeGaugeBlockEntity::new, NtmBlocks.FLUID_DUCT_GAUGE.get()).build(null));
    /* Handventil und Redstoneventil teilen sich eine Blockentitaet -- im Original wie hier. */
    public static final Supplier<BlockEntityType<PipeValveBlockEntity>> FLUID_VALVE = BLOCK_ENTITY_TYPES.register("fluid_valve", () -> BlockEntityType.Builder.of(PipeValveBlockEntity::new, NtmBlocks.FLUID_VALVE.get(), NtmBlocks.FLUID_SWITCH.get()).build(null));
    public static final Supplier<BlockEntityType<PipeCounterValveBlockEntity>> FLUID_COUNTER_VALVE = BLOCK_ENTITY_TYPES.register("fluid_counter_valve", () -> BlockEntityType.Builder.of(PipeCounterValveBlockEntity::new, NtmBlocks.FLUID_COUNTER_VALVE.get()).build(null));
    public static final Supplier<BlockEntityType<FloodlightBlockEntity>> FLOODLIGHT = BLOCK_ENTITY_TYPES.register("floodlight", () -> BlockEntityType.Builder.of(FloodlightBlockEntity::new, NtmBlocks.FLOODLIGHT.get()).build(null));
    public static final Supplier<BlockEntityType<FloodlightBeamBlockEntity>> FLOODLIGHT_BEAM = BLOCK_ENTITY_TYPES.register("floodlight_beam", () -> BlockEntityType.Builder.of(FloodlightBeamBlockEntity::new, NtmBlocks.FLOODLIGHT_BEAM.get()).build(null));
    public static final Supplier<BlockEntityType<DecoPoleSatelliteReceiverBlockEntity>> POLE_SATELLITE_RECEIVER = BLOCK_ENTITY_TYPES.register("pole_satellite_receiver", () -> BlockEntityType.Builder.of(DecoPoleSatelliteReceiverBlockEntity::new, NtmBlocks.POLE_SATELLITE_RECEIVER.get()).build(null));
    public static final Supplier<BlockEntityType<ChargerBlockEntity>> CHARGER = BLOCK_ENTITY_TYPES.register("charger", () -> BlockEntityType.Builder.of(ChargerBlockEntity::new, NtmBlocks.CHARGER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineMicrowaveBlockEntity>> MACHINE_MICROWAVE = BLOCK_ENTITY_TYPES.register("machine_microwave", () -> BlockEntityType.Builder.of(MachineMicrowaveBlockEntity::new, NtmBlocks.MACHINE_MICROWAVE.get()).build(null));
    public static final Supplier<BlockEntityType<RadioRecBlockEntity>> RADIO_REC = BLOCK_ENTITY_TYPES.register("radiorec", () -> BlockEntityType.Builder.of(RadioRecBlockEntity::new, NtmBlocks.RADIOREC.get()).build(null));
    public static final Supplier<BlockEntityType<RadioTelexBlockEntity>> RADIO_TELEX = BLOCK_ENTITY_TYPES.register("radio_telex", () -> BlockEntityType.Builder.of(RadioTelexBlockEntity::new, NtmBlocks.RADIO_TELEX.get()).build(null));
    public static final Supplier<BlockEntityType<TeslaBlockEntity>> TESLA = BLOCK_ENTITY_TYPES.register("tesla", () -> BlockEntityType.Builder.of(TeslaBlockEntity::new, NtmBlocks.TESLA.get()).build(null));
    public static final Supplier<BlockEntityType<BroadcasterBlockEntity>> BROADCASTER = BLOCK_ENTITY_TYPES.register("broadcaster", () -> BlockEntityType.Builder.of(BroadcasterBlockEntity::new, NtmBlocks.BROADCASTER_PC.get()).build(null));
    public static final Supplier<BlockEntityType<SkeletonHolderBlockEntity>> SKELETON_HOLDER = BLOCK_ENTITY_TYPES.register("skeleton_holder", () -> BlockEntityType.Builder.of(SkeletonHolderBlockEntity::new, NtmBlocks.SKELETON_HOLDER.get()).build(null));

    public static final Supplier<BlockEntityType<DecontaminatorBlockEntity>> DECONTAMINATOR = BLOCK_ENTITY_TYPES.register(
            "decontaminator",
            () -> BlockEntityType.Builder.of(
                            DecontaminatorBlockEntity::new,
                            NtmBlocks.DECONTAMINATOR.get())
                    .build(null));

    public static final Supplier<BlockEntityType<NukeGadgetBlockEntity>> NUKE_GADGET = BLOCK_ENTITY_TYPES.register("nuke_gadget", () -> BlockEntityType.Builder.of(NukeGadgetBlockEntity::new, NtmBlocks.NUKE_GADGET.get()).build(null));
    public static final Supplier<BlockEntityType<NukeLittleBoyBlockEntity>> NUKE_LITTLE_BOY = BLOCK_ENTITY_TYPES.register("nuke_little_boy", () -> BlockEntityType.Builder.of(NukeLittleBoyBlockEntity::new, NtmBlocks.NUKE_LITTLE_BOY.get()).build(null));
    public static final Supplier<BlockEntityType<NukeFatManBlockEntity>> NUKE_FAT_MAN = BLOCK_ENTITY_TYPES.register("nuke_fat_man", () -> BlockEntityType.Builder.of(NukeFatManBlockEntity::new, NtmBlocks.NUKE_FAT_MAN.get()).build(null));
    public static final Supplier<BlockEntityType<NukeIvyMikeBlockEntity>> NUKE_IVY_MIKE = BLOCK_ENTITY_TYPES.register("nuke_ivy_mike", () -> BlockEntityType.Builder.of(NukeIvyMikeBlockEntity::new, NtmBlocks.NUKE_IVY_MIKE.get()).build(null));
    public static final Supplier<BlockEntityType<NukeTsarBombaBlockEntity>> NUKE_TSAR_BOMBA = BLOCK_ENTITY_TYPES.register("nuke_tsar_bomba", () -> BlockEntityType.Builder.of(NukeTsarBombaBlockEntity::new, NtmBlocks.NUKE_TSAR_BOMBA.get()).build(null));
    public static final Supplier<BlockEntityType<NukePrototypeBlockEntity>> NUKE_PROTOTYPE = BLOCK_ENTITY_TYPES.register("nuke_prototype", () -> BlockEntityType.Builder.of(NukePrototypeBlockEntity::new, NtmBlocks.NUKE_PROTOTYPE.get()).build(null));
    public static final Supplier<BlockEntityType<NukeFleijaBlockEntity>> NUKE_FLEIJA = BLOCK_ENTITY_TYPES.register("nuke_fleija", () -> BlockEntityType.Builder.of(NukeFleijaBlockEntity::new, NtmBlocks.NUKE_FLEIJA.get()).build(null));
    public static final Supplier<BlockEntityType<NukeSoliniumBlockEntity>> NUKE_SOLINUIM = BLOCK_ENTITY_TYPES.register("nuke_solinium", () -> BlockEntityType.Builder.of(NukeSoliniumBlockEntity::new, NtmBlocks.NUKE_SOLINIUM.get()).build(null));
    public static final Supplier<BlockEntityType<NukeN2BlockEntity>> NUKE_N2 = BLOCK_ENTITY_TYPES.register("nuke_n2", () -> BlockEntityType.Builder.of(NukeN2BlockEntity::new, NtmBlocks.NUKE_N2.get()).build(null));
    public static final Supplier<BlockEntityType<NukeBalefireBlockEntity>> NUKE_FSTBMB = BLOCK_ENTITY_TYPES.register("nuke_fstbmb", () -> BlockEntityType.Builder.of(NukeBalefireBlockEntity::new, NtmBlocks.NUKE_FSTBMB.get()).build(null));

    public static final Supplier<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD = BLOCK_ENTITY_TYPES.register("launch_pad", () -> BlockEntityType.Builder.of(LaunchPadBlockEntity::new, NtmBlocks.LAUNCH_PAD.get()).build(null));
    public static final Supplier<BlockEntityType<LaunchPadLargeBlockEntity>> LAUNCH_PAD_LARGE = BLOCK_ENTITY_TYPES.register("launch_pad_large", () -> BlockEntityType.Builder.of(LaunchPadLargeBlockEntity::new, NtmBlocks.LAUNCH_PAD_LARGE.get()).build(null));
    public static final Supplier<BlockEntityType<SoyuzLauncherBlockEntity>> SOYUZ_LAUNCHER = BLOCK_ENTITY_TYPES.register("soyuz_launcher", () -> BlockEntityType.Builder.of(SoyuzLauncherBlockEntity::new, NtmBlocks.SOYUZ_LAUNCHER.get()).build(null));
    public static final Supplier<BlockEntityType<MachineRadarLargeBlockEntity>> MACHINE_RADAR_LARGE = BLOCK_ENTITY_TYPES.register("machine_radar_large", () -> BlockEntityType.Builder.of(MachineRadarLargeBlockEntity::new, NtmBlocks.MACHINE_RADAR_LARGE.get()).build(null));
    public static final Supplier<BlockEntityType<MachineRadarBlockEntity>> MACHINE_RADAR = BLOCK_ENTITY_TYPES.register("machine_radar", () -> BlockEntityType.Builder.of(MachineRadarBlockEntity::new, NtmBlocks.MACHINE_RADAR.get()).build(null));

    public static final Supplier<BlockEntityType<GeigerBlockEntity>> GEIGER_COUNTER = BLOCK_ENTITY_TYPES.register("geiger_counter", () -> BlockEntityType.Builder.of(GeigerBlockEntity::new, NtmBlocks.GEIGER.get()).build(null));

    public static final Supplier<BlockEntityType<LandmineBlockEntity>> LANDMINE = BLOCK_ENTITY_TYPES.register("landmine", () -> BlockEntityType.Builder.of(LandmineBlockEntity::new, NtmBlocks.MINE_AP.get(), NtmBlocks.MINE_HE.get(), NtmBlocks.MINE_SHRAP.get(), NtmBlocks.MINE_FAT.get(), NtmBlocks.MINE_NAVAL.get()).build(null));
    public static final Supplier<BlockEntityType<VolcanoCoreBlockEntity>> VOLCANO_CORE = BLOCK_ENTITY_TYPES.register("volcano_core", () -> BlockEntityType.Builder.of(VolcanoCoreBlockEntity::new, NtmBlocks.VOLCANO_CORE.get(), NtmBlocks.VOLCANO_RAD_CORE.get()).build(null));

    public static final Supplier<BlockEntityType<CrashedBombBlockEntity>> CRASHED_BOMB = BLOCK_ENTITY_TYPES.register("crashed_bomb", () -> BlockEntityType.Builder.of(CrashedBombBlockEntity::new, NtmBlocks.CRASHED_BOMB.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
