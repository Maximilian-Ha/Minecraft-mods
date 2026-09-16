package com.hbm.inventory;

import com.hbm.inventory.menus.*;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NtmMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, NuclearTechMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AnvilMenu>> ANVIL = reg("anvil", AnvilMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<MachineSolderingStationMenu>> MACHINE_SOLDERING_STATION = reg("machine_soldering_station", MachineSolderingStationMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineArcWelderMenu>> MACHINE_ARC_WELDER = reg("machine_arc_welder", MachineArcWelderMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineShredderMenu>> MACHINE_SHREDDER = reg("machine_shredder", MachineShredderMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineElectricFurnaceMenu>> MACHINE_ELECTRIC_FURNACE = reg("machine_electric_furnace", MachineElectricFurnaceMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRtgFurnaceMenu>> MACHINE_RTG_FURNACE = reg("machine_rtg_furnace", MachineRtgFurnaceMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineDiFurnaceRtgMenu>> MACHINE_DIFURNACE_RTG = reg("machine_difurnace_rtg", MachineDiFurnaceRtgMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePWRMenu>> MACHINE_PWR = reg("machine_pwr", MachinePWRMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineDiFurnaceMenu>> MACHINE_DIFURNACE = reg("machine_difurnace", MachineDiFurnaceMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCombustionEngineMenu>> MACHINE_COMBUSTION_ENGINE = reg("machine_combustion_engine", MachineCombustionEngineMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineTurbineGasMenu>> MACHINE_TURBINEGAS = reg("machine_turbinegas", MachineTurbineGasMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineTurbofanMenu>> MACHINE_TURBOFAN = reg("machine_turbofan", MachineTurbofanMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineAutocrafterMenu>> MACHINE_AUTOCRAFTER = reg("machine_autocrafter", MachineAutocrafterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineEPressMenu>> MACHINE_EPRESS = reg("machine_epress", MachineEPressMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineFunnelMenu>> MACHINE_FUNNEL = reg("machine_funnel", MachineFunnelMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineStrandCasterMenu>> MACHINE_STRAND_CASTER = reg("machine_strand_caster", MachineStrandCasterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineKeyForgeMenu>> MACHINE_KEY_FORGE = reg("machine_keyforge", MachineKeyForgeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineChemicalFactoryMenu>> MACHINE_CHEMICAL_FACTORY = reg("machine_chemical_factory", MachineChemicalFactoryMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineAssemblyFactoryMenu>> MACHINE_ASSEMBLY_FACTORY = reg("machine_assembly_factory", MachineAssemblyFactoryMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCrystallizerMenu>> MACHINE_CRYSTALLIZER = reg("machine_crystallizer", MachineCrystallizerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRTGMenu>> MACHINE_RTG = reg("machine_rtg", MachineRTGMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineBatteryMenu>> MACHINE_BATTERY = reg("machine_battery", MachineBatteryMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineDieselMenu>> MACHINE_DIESEL = reg("machine_diesel", MachineDieselMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMiningLaserMenu>> MACHINE_MINING_LASER = reg("machine_mining_laser", MachineMiningLaserMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCyclotronMenu>> MACHINE_CYCLOTRON = reg("machine_cyclotron", MachineCyclotronMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePASourceMenu>> MACHINE_PA_SOURCE = reg("machine_pa_source", MachinePASourceMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePARFCMenu>> MACHINE_PA_RFC = reg("machine_pa_rfc", MachinePARFCMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePAQuadrupoleMenu>> MACHINE_PA_QUADRUPOLE = reg("machine_pa_quadrupole", MachinePAQuadrupoleMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePADipoleMenu>> MACHINE_PA_DIPOLE = reg("machine_pa_dipole", MachinePADipoleMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePADetectorMenu>> MACHINE_PA_DETECTOR = reg("machine_pa_detector", MachinePADetectorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineExposureChamberMenu>> MACHINE_EXPOSURE_CHAMBER = reg("machine_exposure_chamber", MachineExposureChamberMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRadGenMenu>> MACHINE_RAD_GEN = reg("machine_rad_gen", MachineRadGenMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineGasCentMenu>> MACHINE_GAS_CENT = reg("machine_gas_cent", MachineGasCentMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCompressorMenu>> MACHINE_COMPRESSOR = reg("machine_compressor", MachineCompressorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMixerMenu>> MACHINE_MIXER = reg("machine_mixer", MachineMixerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineTurbineMenu>> MACHINE_TURBINE = reg("machine_turbine", MachineTurbineMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FurnaceIronMenu>> FURNACE_IRON = reg("furnace_iron", FurnaceIronMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FurnaceSteelMenu>> FURNACE_STEEL = reg("furnace_steel", FurnaceSteelMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRockMillMenu>> MACHINE_ROCK_MILL = reg("machine_rock_mill", MachineRockMillMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineOilWellMenu<?>>> MACHINE_OIL_WELL = reg("machine_oil_well", MachineOilWellMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRefineryMenu>> MACHINE_REFINERY = reg("machine_refinery", MachineRefineryMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCatalyticReformerMenu>> MACHINE_CATALYTIC_REFORMER = reg("machine_catalytic_reformer", MachineCatalyticReformerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineHydrotreaterMenu>> MACHINE_HYDROTREATER = reg("machine_hydrotreater", MachineHydrotreaterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineVacuumDistillMenu>> MACHINE_VACUUM_DISTILL = reg("machine_vacuum_distill", MachineVacuumDistillMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineSolidifierMenu>> MACHINE_SOLIDIFIER = reg("machine_solidifier", MachineSolidifierMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineFurnaceCombinationMenu>> FURNACE_COMBINATION = reg("furnace_combination", MachineFurnaceCombinationMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineBlastFurnaceMenu>> MACHINE_BLAST_FURNACE = reg("machine_blast_furnace", MachineBlastFurnaceMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineWoodBurnerMenu>> MACHINE_WOOD_BURNER = reg("machine_wood_burner", MachineWoodBurnerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<AshpitMenu>> MACHINE_ASHPIT = reg("machine_ashpit", AshpitMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCentrifugeMenu>> MACHINE_CENTRIFUGE = reg("machine_centrifuge", MachineCentrifugeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePUREXMenu>> MACHINE_PUREX = reg("machine_purex", MachinePUREXMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRadiolysisMenu>> MACHINE_RADIOLYSIS = reg("machine_radiolysis", MachineRadiolysisMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineChemicalPlantMenu>> MACHINE_CHEMICAL_PLANT = reg("machine_chemical_plant", MachineChemicalPlantMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineArcFurnaceLargeMenu>> MACHINE_ARC_FURNACE = reg("machine_arc_furnace", MachineArcFurnaceLargeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineCrucibleMenu>> MACHINE_CRUCIBLE = reg("machine_crucible", MachineCrucibleMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRotaryFurnaceMenu>> MACHINE_ROTARY_FURNACE = reg("machine_rotary_furnace", MachineRotaryFurnaceMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineSatLinkerMenu>> SAT_LINKER = reg("sat_linker", MachineSatLinkerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineSatDockMenu>> SAT_DOCK = reg("sat_dock", MachineSatDockMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineTapeDriveMenu>> TAPE_DRIVE = reg("tape_drive", MachineTapeDriveMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineSuperComputerMenu>> SUPER_COMPUTER = reg("super_computer", MachineSuperComputerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineAmmoPressMenu>> AMMO_PRESS = reg("ammo_press", MachineAmmoPressMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineRadarMenu>> RADAR = reg("radar", MachineRadarMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineSirenMenu>> SIREN = reg("siren", MachineSirenMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineAnnihilatorMenu>> ANNIHILATOR = reg("annihilator", MachineAnnihilatorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<BarrelMenu>> BARREL = reg("barrel", BarrelMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CrateMenu>> CRATE = reg("crate", CrateMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<HeaterFireboxMenu>> HEATER_FIREBOX = reg("heater_firebox", HeaterFireboxMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<HeaterOvenMenu>> HEATER_OVEN = reg("heater_oven", HeaterOvenMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<HeaterOilburnerMenu>> HEATER_OILBURNER = reg("heater_oilburner", HeaterOilburnerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<HeaterHeatexMenu>> HEATER_HEATEX = reg("heater_heatex", HeaterHeatexMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<MachineAssemblyMachineMenu>> ASSEMBLY_MACHINE = reg("assembly_machine", MachineAssemblyMachineMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePrecAssMenu>> PRECASS = reg("precass", MachinePrecAssMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineOreSlopperMenu>> ORE_SLOPPER = reg("ore_slopper", MachineOreSlopperMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineExcavatorMenu>> EXCAVATOR = reg("excavator", MachineExcavatorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMissileAssemblyMenu>> MISSILE_ASSEMBLY = reg("missile_assembly", MachineMissileAssemblyMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachinePressMenu>> PRESS = reg("press", MachinePressMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<RBMKControlAutoMenu>> RBMK_CONTROL_AUTO = reg("rbmk_control_auto", RBMKControlAutoMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKOutgasserMenu>> RBMK_OUTGASSER = reg("rbmk_outgasser", RBMKOutgasserMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKHeaterMenu>> RBMK_HEATER = reg("rbmk_heater", RBMKHeaterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKBoilerMenu>> RBMK_BOILER = reg("rbmk_boiler", RBMKBoilerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKStorageMenu>> RBMK_STORAGE = reg("rbmk_storage", RBMKStorageMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CraneInserterMenu>> CRANE_INSERTER = reg("crane_inserter", CraneInserterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CraneExtractorMenu>> CRANE_EXTRACTOR = reg("crane_extractor", CraneExtractorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CraneGrabberMenu>> CRANE_GRABBER = reg("crane_grabber", CraneGrabberMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CraneBoxerMenu>> CRANE_BOXER = reg("crane_boxer", CraneBoxerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CraneUnboxerMenu>> CRANE_UNBOXER = reg("crane_unboxer", CraneUnboxerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CraneRouterMenu>> CRANE_ROUTER = reg("crane_router", CraneRouterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKAutoloaderMenu>> RBMK_AUTOLOADER = reg("rbmk_autoloader", RBMKAutoloaderMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<WasteDrumMenu>> WASTE_DRUM = reg("waste_drum", WasteDrumMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKRodMenu>> RBMK_ROD = reg("rbmk_rod", RBMKRodMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RBMKControlMenu>> RBMK_CONTROL = reg("rbmk_control", RBMKControlMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorZirnoxMenu>> REACTOR_ZIRNOX = reg("machine_zirnox", ReactorZirnoxMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<WatzMenu>> WATZ = reg("watz", WatzMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ICFMenu>> ICF = reg("icf", ICFMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ICFPressMenu>> ICF_PRESS = reg("icf_press", ICFPressMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FusionTorusMenu>> FUSION_TORUS = reg("fusion_torus", FusionTorusMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FusionKlystronMenu>> FUSION_KLYSTRON = reg("fusion_klystron", FusionKlystronMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FusionBreederMenu>> FUSION_BREEDER = reg("fusion_breeder", FusionBreederMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FusionPlasmaForgeMenu>> FUSION_PLASMA_FORGE = reg("fusion_plasma_forge", FusionPlasmaForgeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ReactorResearchMenu>> REACTOR_RESEARCH = reg("reactor_research", ReactorResearchMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineReactorBreedingMenu>> MACHINE_REACTOR_BREEDING = reg("machine_reactor_breeding", MachineReactorBreedingMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<TurretBaseMenu>> TURRET_BASE = reg("turret_base", TurretBaseMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ReactorControlMenu>> REACTOR_CONTROL = reg("reactor_control", ReactorControlMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<WeaponTableMenu>> WEAPON_TABLE = reg("weapon_table", WeaponTableMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ArmorTableMenu>> ARMOR_TABLE = reg("armor_table", ArmorTableMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<MachineFluidTankMenu>> FLUID_TANK = reg("fluid_tank", MachineFluidTankMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<BatterySocketMenu>> BATTERY_SOCKET = reg("battery_socket", BatterySocketMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<BatteryREDDMenu>> BATTERY_REDD = reg("battery_redd", BatteryREDDMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<NukeGadgetMenu>> NUKE_GADGET = reg("nuke_gadget", NukeGadgetMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeLittleBoyMenu>> NUKE_LITTLE_BOY = reg("nuke_little_boy", NukeLittleBoyMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeFatManMenu>> NUKE_FAT_MAN = reg("nuke_fat_man", NukeFatManMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeIvyMikeMenu>> NUKE_IVY_MIKE = reg("nuke_ivy_mike", NukeIvyMikeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeTsarBombaMenu>> NUKE_TSAR_BOMBA = reg("nuke_tsar_bomba", NukeTsarBombaMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukePrototypeMenu>> NUKE_PROTOTYPE = reg("nuke_prototype", NukePrototypeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeFleijaMenu>> NUKE_FLEIJA = reg("nuke_fleija", NukeFleijaMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeSoliniumMenu>> NUKE_SOLINIUM = reg("nuke_solinium", NukeSoliniumMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeN2Menu>> NUKE_N2 = reg("nuke_n2", NukeN2Menu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<NukeFstbmbMenu>> NUKE_FSTBMB = reg("nuke_fstbmb", NukeFstbmbMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<LaunchPadLargeMenu>> LAUNCH_PAD_LARGE = reg("launch_pad_large", LaunchPadLargeMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<SoyuzLauncherMenu>> SOYUZ_LAUNCHER = reg("soyuz_launcher", SoyuzLauncherMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> reg(String name, IContainerFactory<T> factory) {
        return MENU_TYPES.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
