package com.hbm.main;

import com.hbm.render.anim.AnimationLoader;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.loader.IObjRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class ResourceManager {
    public static final ResourceLocation EMPTY = ResourceLocation.withDefaultNamespace("missingno");

    public static final ResourceLocation MINE_AP_STONE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_ap.png");
    public static final ResourceLocation MINE_AP_SNOW_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_ap_snow.png");
    public static final ResourceLocation MINE_AP_GRASS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_ap_grass.png");
    public static final ResourceLocation MINE_AP_DESERT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_ap_desert.png");
    public static final ResourceLocation MINE_HE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_he.png");
    public static final ResourceLocation MINE_SHRAPNEL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_shrap.png");
    public static final ResourceLocation MINE_NAVAL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_naval.png");
    public static final ResourceLocation MINE_FAT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/mine_fat.png");

    public static final ResourceLocation NUKE_GADGET_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_gadget.png");
    public static final ResourceLocation NUKE_LITTLE_BOY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_little_boy.png");
    public static final ResourceLocation NUKE_FAT_MAN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_fatman.png");
    public static final ResourceLocation NUKE_IVY_MIKE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_ivy_mike.png");
    public static final ResourceLocation NUKE_TSAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_tsar.png");
    public static final ResourceLocation NUKE_PROTOTYPE = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_prototype.png");
    public static final ResourceLocation NUKE_FLEIJA_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_fleija.png");
    public static final ResourceLocation NUKE_SOLINIUM_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_solinium.png");
    public static final ResourceLocation NUKE_N2_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_n2.png");
    public static final ResourceLocation NUKE_FSTBMB_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/nuke_fstbmb.png");

    public static final ResourceLocation DUD_BALEFIRE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/dud_balefire.png");
    public static final ResourceLocation DUD_CONVENTIONAL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/dud_conventional.png");
    public static final ResourceLocation DUD_NUKE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/dud_nuke.png");
    public static final ResourceLocation DUD_SALTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bombs/dud_salted.png");

    public static final ResourceLocation TANK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/tank.png");
    public static final ResourceLocation BIGASSTANK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/bigasstank.png");
    public static final ResourceLocation TANK_INNER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/tank_inner.png");

    //Large Turbine
    public static final ResourceLocation CHUNGUS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/chungus.png");

    public static final ResourceLocation BATTERY_SOCKET_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/battery_socket.png");
    public static final ResourceLocation SOLDERING_STATION_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/soldering_station.png");
    public static final ResourceLocation ROTARY_FURNACE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/rotary_furnace.png");
    public static final ResourceLocation CRUCIBLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/crucible_heat.png");
    public static final ResourceLocation ARC_FURNACE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/arc_furnace.png");
    public static final ResourceLocation ARC_WELDER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/arc_welder.png");
    public static final ResourceLocation DIESEL_GENERATOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/diesel_generator.png");
    public static final ResourceLocation COMPRESSOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/compressor.png");
    public static final ResourceLocation COMPRESSOR_COMPACT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/compressor_compact.png");
    public static final ResourceLocation SOLAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/solar_boiler.png");
    public static final ResourceLocation SOLAR_MIRROR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/solar_mirror.png");
    public static final ResourceLocation FURNACE_IRON_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/furnace_iron.png");
    public static final ResourceLocation FURNACE_STEEL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/furnace_steel.png");
    public static final ResourceLocation MIXER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/mixer.png");
    public static final ResourceLocation ROCK_MILL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/rockmill.png");
    public static final ResourceLocation STEAM_ENGINE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/steam_engine.png");
    public static final ResourceLocation STIRLING_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/stirling.png");
    public static final ResourceLocation DERRICK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/derrick.png");
    public static final ResourceLocation PUMPJACK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/pumpjack.png");
    public static final ResourceLocation FRACKING_TOWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/fracking_tower.png");
    public static final ResourceLocation REFINERY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/refinery.png");
    public static final ResourceLocation FRACTION_TOWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/fraction_tower.png");
    public static final ResourceLocation FRACTION_SPACER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/fraction_spacer.png");
    public static final ResourceLocation CATALYTIC_REFORMER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/catalytic_reformer.png");
    public static final ResourceLocation HYDROTREATER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/hydrotreater.png");
    public static final ResourceLocation DEUTERIUM_TOWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/machine_deuterium_tower.png");
    public static final ResourceLocation REFUELER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/refueler.png");
    public static final ResourceLocation VACUUM_DISTILL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/vacuum_distill.png");
    public static final ResourceLocation SOLIDIFIER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/solidifier.png");
    public static final ResourceLocation PYRO_OVEN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/pyrooven.png");
    public static final ResourceLocation LIQUEFACTOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/liquefactor.png");
    public static final ResourceLocation FLARE_STACK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/flare_stack.png");
    public static final ResourceLocation COKER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/coker.png");
    public static final ResourceLocation CATALYTIC_CRACKER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/catalytic_cracker.png");
    public static final ResourceLocation FURNACE_COMBINATION_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/combination_oven.png");
    public static final ResourceLocation BLAST_FURNACE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/blast_furnace.png");
    public static final ResourceLocation WOOD_BURNER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/wood_burner.png");
    public static final ResourceLocation CENTRIFUGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/centrifuge_new.png");
    public static final ResourceLocation RBMK_GAUGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/gauge.png");
    public static final ResourceLocation PUREX_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/purex.png");
    public static final ResourceLocation RADIOLYSIS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/radiolysis.png");
    public static final ResourceLocation CHEMICAL_PLANT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/chemical_plant.png");
    public static final ResourceLocation CHEMICAL_PLANT_FLUID_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/chemical_plant_fluid.png");
    public static final ResourceLocation BOILER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/boiler.png");
    public static final ResourceLocation INDUSTRIAL_BOILER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/industrial_boiler.png");
    public static final ResourceLocation HEATER_FIREBOX_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/firebox.png");
    public static final ResourceLocation HEATER_OVEN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/heating_oven.png");
    public static final ResourceLocation ASHPIT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/ashpit.png");
    public static final ResourceLocation CHIMNEY_BRICK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/chimney_brick.png");
    public static final ResourceLocation CHIMNEY_INDUSTRIAL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/chimney_industrial.png");
    public static final ResourceLocation RBMK_AUTOLOADER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/rbmk_autoloader.png");
    public static final ResourceLocation RBMK_CRANE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/rbmk_crane.png");
    public static final ResourceLocation RBMK_CRANE_CONSOLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/rbmk_crane_console.png");
    public static final ResourceLocation HEATER_OILBURNER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/oilburner.png");
    public static final ResourceLocation HEATER_ELECTRIC_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/electric_heater.png");
    public static final ResourceLocation HEATER_HEATEX_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/heater_heatex.png");
    public static final ResourceLocation TESLA_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/tesla.png");
    /* Der Sockel nimmt im Original die Partikeltextur des Skeletts -- keine eigene Haut. */
    public static final ResourceLocation SKELETON_HOLDER_TEX = NuclearTechMod.withDefaultNamespace("textures/particle/skeleton.png");
    public static final ResourceLocation DEMON_LAMP_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/demon_lamp.png");
    public static final ResourceLocation BATTERY_SC_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/battery_sc.png");
    public static final ResourceLocation BATTERY_REDD_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/fensu2.png");

    // Radar
    public static final ResourceLocation RADAR_BASE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/radar_base.png");
    public static final ResourceLocation RADAR_DISH_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/radar_dish.png");
    public static final ResourceLocation RADAR_LARGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/radar_large.png");
    public static final ResourceLocation RADAR_SCREEN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/radar_screen.png");

    // Nachgereichte Geraete (Runde 161)
    public static final ResourceLocation SATLINK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/satlink.png");
    public static final ResourceLocation TAPE_DRIVE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/tape_drive.png");
    public static final ResourceLocation SUPERCOMPUTER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/supercomputer.png");
    public static final ResourceLocation SUPERCOMPUTER_SCAN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/supercomputer_scan.png");
    public static final ResourceLocation AMMO_PRESS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/ammo_press.png");
    public static final ResourceLocation AUTOSAW_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/autosaw.png");

    // Nachgereichte Geraete (Runde 162)
    public static final ResourceLocation GASCENT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/gascent.png");
    public static final ResourceLocation ANNIHILATOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/annihilator.png");
    public static final ResourceLocation ANNIHILATOR_BELT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/annihilator_belt.png");
    public static final ResourceLocation EXPOSURE_CHAMBER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/exposure_chamber.png");
    public static final ResourceLocation MINING_LASER_BASE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/mining_laser_base.png");
    public static final ResourceLocation MINING_LASER_PIVOT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/mining_laser_pivot.png");
    public static final ResourceLocation MINING_LASER_LASER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/mining_laser_laser.png");
    public static final ResourceLocation RADGEN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/radgen.png");

    // Zyklotron (Runde 163): je Sockel ein Bild fuer leer und eines fuer gesteckt
    public static final ResourceLocation CYCLOTRON_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron.png");
    public static final ResourceLocation CYCLOTRON_ASHES_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_ashes.png");
    public static final ResourceLocation CYCLOTRON_ASHES_FILLED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_ashes_filled.png");
    public static final ResourceLocation CYCLOTRON_BOOK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_book.png");
    public static final ResourceLocation CYCLOTRON_BOOK_FILLED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_book_filled.png");
    public static final ResourceLocation CYCLOTRON_GAVEL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_gavel.png");
    public static final ResourceLocation CYCLOTRON_GAVEL_FILLED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_gavel_filled.png");
    public static final ResourceLocation CYCLOTRON_COIN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_coin.png");
    public static final ResourceLocation CYCLOTRON_COIN_FILLED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/cyclotron_coin_filled.png");

    // Teilchenbeschleuniger (Runde 168)
    public static final ResourceLocation PA_SOURCE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/particleaccelerator/source.png");
    public static final ResourceLocation PA_BEAMLINE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/particleaccelerator/beamline.png");
    public static final ResourceLocation PA_RFC_TEX = NuclearTechMod.withDefaultNamespace("textures/models/particleaccelerator/rfc.png");
    public static final ResourceLocation PA_QUADRUPOLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/particleaccelerator/quadrupole.png");
    public static final ResourceLocation PA_DIPOLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/particleaccelerator/dipole.png");
    public static final ResourceLocation PA_DETECTOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/particleaccelerator/detector.png");

    // Kuehltuerme (Runde 169)
    public static final ResourceLocation TOWER_SMALL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/tower_small.png");
    public static final ResourceLocation FLOODLIGHT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/floodlight.png");
    public static final ResourceLocation CHARGER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/charger.png");
    public static final ResourceLocation TELEX_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/telex.png");
    public static final ResourceLocation TOWER_LARGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/tower_large.png");

    //Press
    public static final ResourceLocation PRESS_BODY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/press_body.png");
    public static final ResourceLocation PRESS_HEAD_TEX = NuclearTechMod.withDefaultNamespace("textures/models/press_head.png");

    public static final ResourceLocation GEIGER_TEX = NuclearTechMod.withDefaultNamespace("textures/block/geiger.png");
    public static final ResourceLocation ASSEMBLY_MACHINE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/assembly_machine.png");
    /** Die Praezisionsmontage teilt sich das Modell der Montagemaschine und hat nur eine eigene Haut. */
    public static final ResourceLocation PRECASS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/precass.png");
    public static final ResourceLocation ORE_SLOPPER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/ore_slopper.png");
    public static final ResourceLocation MINING_DRILL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/mining_drill.png");

    public static final ResourceLocation FUSION_PLASMA_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/plasma.png");
    public static final ResourceLocation FUSION_PLASMA_GLOW_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/plasma_glow.png");
    public static final ResourceLocation FUSION_PLASMA_SPARKLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/plasma_sparkle.png");

    // Blast
    public static final ResourceLocation TOM_BLAST_TEX = NuclearTechMod.withDefaultNamespace("textures/models/explosion/tom_blast.png");

    // Boxcar
    public static final ResourceLocation TOM_MAIN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/tom_main.png");
    public static final ResourceLocation TOM_FLAME_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/tom_flame.png");

    // Projectiles
    public static final ResourceLocation CASINGS_TEX = NuclearTechMod.withDefaultNamespace("textures/particle/casings.png");

    // Bomber
    public static final ResourceLocation DORNIER_1_TEX = NuclearTechMod.withDefaultNamespace("textures/models/dornier_1.png");
    public static final ResourceLocation DORNIER_2_TEX = NuclearTechMod.withDefaultNamespace("textures/models/dornier_2.png");
    public static final ResourceLocation DORNIER_4_TEX = NuclearTechMod.withDefaultNamespace("textures/models/dornier_4.png");
    public static final ResourceLocation B29_0_TEX = NuclearTechMod.withDefaultNamespace("textures/models/b29_0.png");
    public static final ResourceLocation B29_1_TEX = NuclearTechMod.withDefaultNamespace("textures/models/b29_1.png");
    public static final ResourceLocation B29_2_TEX = NuclearTechMod.withDefaultNamespace("textures/models/b29_2.png");
    public static final ResourceLocation B29_3_TEX = NuclearTechMod.withDefaultNamespace("textures/models/b29_3.png");
    public static final ResourceLocation C130_0_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/c130_0.png");

    // Missiles
    public static final ResourceLocation MISSILE_MICRO_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_micro.png");
    public static final ResourceLocation MISSILE_MICRO_TAINT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_micro_taint.png");
    public static final ResourceLocation MISSILE_MICRO_BHOLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_micro_bhole.png");
    public static final ResourceLocation MISSILE_MICRO_SCHRABIDIUM_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_micro_schrab.png");
    public static final ResourceLocation MISSILE_MICRO_EMP_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_micro_emp.png");
    public static final ResourceLocation MISSILE_V2_HE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_v2.png");
    public static final ResourceLocation MISSILE_V2_IN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_v2_inc.png");
    public static final ResourceLocation MISSILE_V2_CL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_v2_cl.png");
    public static final ResourceLocation MISSILE_V2_BU_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_v2_bu.png");
    public static final ResourceLocation MISSILE_V2_DECOY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_v2_decoy.png");
    public static final ResourceLocation MISSILE_STEALTH_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_stealth.png");
    public static final ResourceLocation MISSILE_AA_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_abm.png");
    public static final ResourceLocation MISSILE_STRONG_HE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_strong.png");
    public static final ResourceLocation MISSILE_STRONG_EMP_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_strong_emp.png");
    public static final ResourceLocation MISSILE_STRONG_IN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_strong_inc.png");
    public static final ResourceLocation MISSILE_STRONG_CL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_strong_cl.png");
    public static final ResourceLocation MISSILE_STRONG_BU_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_strong_bu.png");
    public static final ResourceLocation MISSILE_HUGE_HE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_huge.png");
    public static final ResourceLocation MISSILE_HUGE_IN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_huge_inc.png");
    public static final ResourceLocation MISSILE_HUGE_CL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_huge_cl.png");
    public static final ResourceLocation MISSILE_HUGE_BU_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_huge_bu.png");
    public static final ResourceLocation MISSILE_NUCLEAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_atlas_nuclear.png");
    public static final ResourceLocation MISSILE_THERMO_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_atlas_thermo.png");
    public static final ResourceLocation MISSILE_VOLCANO_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_atlas_tectonic.png");
    public static final ResourceLocation MISSILE_DOOMSDAY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_atlas_doomsday.png");
    public static final ResourceLocation MISSILE_DOOMSDAY_RUSTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_atlas_doomsday_weathered.png");
    public static final ResourceLocation MISSILE_SHUTTLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/missile/missile_shuttle.png");

    public static final ResourceLocation SOYUZ_ENGINEBLOCK = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/engineblock.png");
    public static final ResourceLocation SOYUZ_BOTTOMSTAGE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/bottomstage.png");
    public static final ResourceLocation SOYUZ_TOPSTAGE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/topstage.png");
    public static final ResourceLocation SOYUZ_PAYLOAD = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/payload.png");
    public static final ResourceLocation SOYUZ_PAYLOADBLOCKS = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/payloadblocks.png");
    public static final ResourceLocation SOYUZ_LES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/les.png");
    public static final ResourceLocation SOYUZ_LESTHRUSTERS = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/lesthrusters.png");
    public static final ResourceLocation SOYUZ_MAINENGINES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/mainengines.png");
    public static final ResourceLocation SOYUZ_SIDEENGINES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/sideengines.png");
    public static final ResourceLocation SOYUZ_BOOSTER = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/booster.png");
    public static final ResourceLocation SOYUZ_BOOSTERSIDE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz/boosterside.png");
    public static final ResourceLocation SOYUZ_LUNA_ENGINEBLOCK = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/engineblock.png");
    public static final ResourceLocation SOYUZ_LUNA_BOTTOMSTAGE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/bottomstage.png");
    public static final ResourceLocation SOYUZ_LUNA_TOPSTAGE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/topstage.png");
    public static final ResourceLocation SOYUZ_LUNA_PAYLOAD =NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/payload.png");
    public static final ResourceLocation SOYUZ_LUNA_PAYLOADBLOCKS = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/payloadblocks.png");
    public static final ResourceLocation SOYUZ_LUNA_LES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/les.png");
    public static final ResourceLocation SOYUZ_LUNA_LESTHRUSTERS = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/lesthrusters.png");
    public static final ResourceLocation SOYUZ_LUNA_MAINENGINES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/mainengines.png");
    public static final ResourceLocation SOYUZ_LUNA_SIDEENGINES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/sideengines.png");
    public static final ResourceLocation SOYUZ_LUNA_BOOSTER = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/booster.png");
    public static final ResourceLocation SOYUZ_LUNA_BOOSTERSIDE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_luna/boosterside.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_ENGINEBLOCK = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/engineblock.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_BOTTOMSTAGE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/bottomstage.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_TOPSTAGE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/topstage.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_PAYLOAD = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/payload.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_PAYLOADBLOCKS = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/payloadblocks.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_LES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/les.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_LESTHRUSTERS = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/lesthrusters.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_MAINENGINES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/mainengines.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_SIDEENGINES = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/sideengines.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_BOOSTER = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/booster.png");
    public static final ResourceLocation SOYUZ_AUTHENTIC_BOOSTERSIDE = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_authentic/boosterside.png");
    public static final ResourceLocation SOYUZ_MEMENTO = NuclearTechMod.withDefaultNamespace("textures/items/polaroid_memento.png");

    public static final ResourceLocation SOYUZ_LANDER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/soyuz_lander.png");
    public static final ResourceLocation SOYUZ_LANDER_RUST_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/soyuz_lander_rust.png");
    public static final ResourceLocation SOYUZ_CHUTE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/soyuz_chute.png");
    public static final ResourceLocation SUPPLY_CRATE = NuclearTechMod.withDefaultNamespace("textures/blocks/crate_can.png");

    public static final ResourceLocation DROPSHIP_TEX = NuclearTechMod.withDefaultNamespace("textures/models/dropship/dropship.png");

    public static final ResourceLocation SOYUZ_MODULE_DOME_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/module_dome.png");
    public static final ResourceLocation SOYUZ_MODULE_LANDER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/module_lander.png");
    public static final ResourceLocation SOYUZ_MODULE_PROPULSION_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/module_propulsion.png");
    public static final ResourceLocation SOYUZ_MODULE_SOLAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_capsule/module_solar.png");

    public static final ResourceLocation SOYUZ_LAUNCHER_LEGS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_launcher/launcher_leg.png");
    public static final ResourceLocation SOYUZ_LAUNCHER_TABLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_launcher/launcher_table.png");
    public static final ResourceLocation SOYUZ_LAUNCHER_TOWER_BASE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_launcher/launcher_tower_base.png");
    public static final ResourceLocation SOYUZ_LAUNCHER_TOWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_launcher/launcher_tower.png");
    public static final ResourceLocation SOYUZ_LAUNCHER_SUPPORT_BASE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_launcher/launcher_support_base.png");
    public static final ResourceLocation SOYUZ_LAUNCHER_SUPPORT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/soyuz_launcher/launcher_support.png");

    // Missile Parts
    public static final ResourceLocation MISSILE_PAD_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/silo.png");
    public static final ResourceLocation MISSILE_PAD_RUSTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/silo_rusted.png");
    public static final ResourceLocation MISSILE_ERECTOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/pad.png");
    public static final ResourceLocation MISSILE_ERECTOR_MICRO_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/erector_micro.png");
    public static final ResourceLocation MISSILE_ERECTOR_V2_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/erector_v2.png");
    public static final ResourceLocation MISSILE_ERECTOR_STRONG_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/erector_strong.png");
    public static final ResourceLocation MISSILE_ERECTOR_HUGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/erector_huge.png");
    public static final ResourceLocation MISSILE_ERECTOR_ATLAS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/erector_atlas.png");
    public static final ResourceLocation MISSILE_ERECTOR_ABM_TEX = NuclearTechMod.withDefaultNamespace("textures/models/launchpad/erector_abm.png");

    public static final ResourceLocation BOMBLET_ZETA_TEX = NuclearTechMod.withDefaultNamespace("textures/models/bomblet_zeta.png");

    public static final ResourceLocation DETONATOR_LASER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/detonator_laser.png");

    // Shimmer Sledge
    public static final ResourceLocation SHIMMER_SLEDGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/shimmer_sledge.png");
    public static final ResourceLocation SHIMMER_AXE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/shimmer_axe.png");

    public static final ResourceLocation CHIP_GOLD_TEX = NuclearTechMod.withDefaultNamespace("textures/models/trinkets/chip_gold.png");
    public static final ResourceLocation NI4NI_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/n_i_4_n_i.png");
    public static final ResourceLocation HENRY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/henry.png");
    public static final ResourceLocation HENRY_LINCOLN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/henry_lincoln.png");
    public static final ResourceLocation MARESLEG_TEX =  NuclearTechMod.withDefaultNamespace("textures/models/weapon/maresleg.png");
    public static final ResourceLocation MARESLEG_BROKEN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/maresleg_broken.png");
    public static final ResourceLocation DEBUG_GUN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/debug_gun.png");
    public static final ResourceLocation HEAVY_REVOLVER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/heavy_revolver.png");
    public static final ResourceLocation SPAS_12_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/spas-12.png");
    public static final ResourceLocation FATMAN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fatman.png");
    public static final ResourceLocation FATMAN_MININUKE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fatman_mininuke.png");
    public static final ResourceLocation FATMAN_BALEFIRE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fatman_balefire.png");
    public static final ResourceLocation CLUSTER_SUBMUNITION_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fatman_submunition.png");
    public static final ResourceLocation HANGMAN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/hangman.png");
    public static final ResourceLocation GREASEGUN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/greasegun.png");
    public static final ResourceLocation PEPPERBOX_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/pepperbox.png");
    public static final ResourceLocation AM180_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/am180.png");
    public static final ResourceLocation STAR_F_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/star_f.png");
    public static final ResourceLocation STAR_F_ELITE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/star_f_elite.png");
    public static final ResourceLocation TURRET_SENTRY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/sentry.png");
    public static final ResourceLocation TURRET_SENTRY_DAMAGED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/sentry_damaged.png");
    public static final ResourceLocation TURRET_BASE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/base.png");
    public static final ResourceLocation TURRET_BASE_RUSTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/rusted/base.png");
    public static final ResourceLocation TURRET_CARRIAGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/carriage.png");
    public static final ResourceLocation TURRET_BASE_FRIENDLY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/base_friendly.png");
    public static final ResourceLocation TURRET_CARRIAGE_FRIENDLY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/carriage_friendly.png");
    public static final ResourceLocation TURRET_CHEKHOV_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/chekhov.png");
    public static final ResourceLocation TURRET_CHEKHOV_BARRELS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/chekhov_barrels.png");
    public static final ResourceLocation TURRET_CARRIAGE_CIWS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/carriage_ciws.png");
    public static final ResourceLocation TURRET_CARRIAGE_CIWS_RUSTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/rusted/carriage_ciws.png");
    public static final ResourceLocation TURRET_CONNECTOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/connector.png");
    public static final ResourceLocation TURRET_JEREMY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/jeremy.png");
    public static final ResourceLocation TURRET_HOWARD_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/howard.png");
    public static final ResourceLocation TURRET_HOWARD_RUSTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/rusted/howard.png");
    public static final ResourceLocation TURRET_HOWARD_BARRELS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/howard_barrels.png");
    public static final ResourceLocation TURRET_HOWARD_BARRELS_RUSTED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/turret/rusted/howard_barrels.png");
    public static final ResourceLocation UZI_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/uzi.png");
    public static final ResourceLocation UZI_SATURNITE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/uzi_saturnite.png");
    public static final ResourceLocation AMAT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/amat.png");
    public static final ResourceLocation AMAT_SUBTLETY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/amat_subtlety.png");
    public static final ResourceLocation AMAT_PENANCE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/amat_penance.png");
    public static final ResourceLocation G3_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/g3.png");
    public static final ResourceLocation G3_ZEBRA_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/g3_zebra.png");
    public static final ResourceLocation G3_GREEN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/g3_polymer_green.png");
    public static final ResourceLocation G3_BLACK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/g3_polymer_black.png");
    public static final ResourceLocation G3_ATTACHMENTS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/g3_attachments.png");
    public static final ResourceLocation STG77_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/stg77.png");
    /* Der Karabiner heisst im Original "Huntsman" -- die Textur traegt den alten Namen. */
    public static final ResourceLocation CARBINE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/huntsman.png");
    public static final ResourceLocation CARBINE_SCOPE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/carbine_scope.png");
    public static final ResourceLocation CARBINE_BAYONET_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/carbine_bayonet.png");
    public static final ResourceLocation MAS36_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/mas36.png");
    public static final ResourceLocation MINIGUN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/minigun.png");
    public static final ResourceLocation MINIGUN_DUAL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/minigun_dual.png");
    public static final ResourceLocation BOLTER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/bolter.png");
    public static final ResourceLocation ABERRATOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/aberrator.png");
    public static final ResourceLocation CONGOLAKE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/congolake.png");
    public static final ResourceLocation FLAMETHROWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/flamethrower.png");
    public static final ResourceLocation DRILL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/drill.png");
    public static final ResourceLocation TESLA_CANNON_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/tesla_cannon.png");
    /* ABWEICHUNG IM DATEINAMEN: im Original heisst diese Datei moonlight.png. Der Port
     * benennt sie nach der Waffe, wie alle uebrigen Waffentexturen. */
    public static final ResourceLocation FOLLY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/folly.png");
    /* Im Original heisst die Grundfassung fireext_normal.png -- der Port nennt sie wie die
     * Waffe, die uebrigen zwei behalten ihre Endung. */
    public static final ResourceLocation FIREEXT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fireext.png");
    public static final ResourceLocation FIREEXT_FOAM_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fireext_foam.png");
    public static final ResourceLocation FIREEXT_SAND_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/fireext_sand.png");
    public static final ResourceLocation CHARGE_THROWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/charge_thrower.png");
    public static final ResourceLocation CHARGE_THROWER_HOOK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/charge_thrower_hook.png");
    public static final ResourceLocation CHARGE_THROWER_MORTAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/charge_thrower_mortar.png");
    public static final ResourceLocation CHEMTHROWER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/chemthrower.png");
    public static final ResourceLocation LASER_PISTOL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/laser_pistol.png");
    public static final ResourceLocation LASER_PISTOL_PEW_PEW_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/laser_pistol_pew_pew.png");
    public static final ResourceLocation LASER_PISTOL_MORNING_GLORY_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/laser_pistol_morning_glory.png");
    public static final ResourceLocation LASRIFLE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/lasrifle.png");
    public static final ResourceLocation LASER_FLASH_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/laser_flash.png");
    /** Das Zielbild des Lasergewehrs zeigt im Original auf scope_amat.png -- dieselbe Datei. */
    public static final ResourceLocation SCOPE_LUNA_TEX = NuclearTechMod.withDefaultNamespace("textures/misc/scope_amat.png");
    public static final ResourceLocation FLAMETHROWER_TOPAZ_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/flamethrower_topaz.png");
    public static final ResourceLocation FLAMETHROWER_DAYBREAKER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/flamethrower_daybreaker.png");
    public static final ResourceLocation FLAREGUN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/flaregun.png");
    public static final ResourceLocation MK108_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/mk108.png");
    public static final ResourceLocation M2_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/m2_browning.png");
    public static final ResourceLocation DANI_CELESTIAL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/dani_celestial.png");
    public static final ResourceLocation DANI_LUNAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/dani_lunar.png");
    public static final ResourceLocation BIO_REVOLVER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/bio_revolver.png");
    public static final ResourceLocation BIO_REVOLVER_ATLAS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/bio_revolver_atlas.png");
    public static final ResourceLocation DOUBLE_BARREL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/double_barrel.png");
    public static final ResourceLocation LIBERATOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/liberator.png");
    public static final ResourceLocation PANZERSCHRECK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/panzerschreck.png");
    public static final ResourceLocation BOLTGUN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/boltgun.png");
    public static final ResourceLocation QUADRO_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/quadro.png");
    public static final ResourceLocation QUADRO_ROCKET_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/quadro_rocket.png");
    public static final ResourceLocation MISSILE_LAUNCHER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/missile_launcher.png");
    public static final ResourceLocation BOXCAR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/boxcar.png");
    public static final ResourceLocation DUCHESSGAMBIT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/duchessgambit.png");
    public static final ResourceLocation TORPEDO_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/torpedo.png");
    public static final ResourceLocation SHREDDER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/shredder.png");
    public static final ResourceLocation LILMAC_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/lilmac.png");
    public static final ResourceLocation PROTEGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/protege.png");
    public static final ResourceLocation LILMAC_SCOPE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/lilmac_scope.png");
    public static final ResourceLocation DOUBLE_BARREL_SACRED_DRAGON_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/double_barrel_sacred_dragon.png");
    public static final ResourceLocation N_I_4_N_I_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/n_i_4_n_i.png");
    public static final ResourceLocation N_I_4_N_I_GREYSCALE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/weapon/n_i_4_n_i_greyscale.png");

    public static final ResourceLocation HEV_HELMET = NuclearTechMod.withDefaultNamespace("textures/armor/hev_helmet.png");
    public static final ResourceLocation HEV_LEG = NuclearTechMod.withDefaultNamespace("textures/armor/hev_leg.png");
    public static final ResourceLocation HEV_CHEST = NuclearTechMod.withDefaultNamespace("textures/armor/hev_chest.png");
    public static final ResourceLocation HEV_ARM = NuclearTechMod.withDefaultNamespace("textures/armor/hev_arm.png");
    public static final ResourceLocation RPA_HELMET = NuclearTechMod.withDefaultNamespace("textures/armor/rpa_helmet.png");
    public static final ResourceLocation RPA_CHEST = NuclearTechMod.withDefaultNamespace("textures/armor/rpa_chest.png");
    public static final ResourceLocation RPA_ARM = NuclearTechMod.withDefaultNamespace("textures/armor/rpa_arm.png");
    public static final ResourceLocation RPA_LEG = NuclearTechMod.withDefaultNamespace("textures/armor/rpa_leg.png");
    public static final ResourceLocation NCRPA_HELMET = NuclearTechMod.withDefaultNamespace("textures/armor/ncrpa_helmet.png");
    public static final ResourceLocation NCRPA_CHEST = NuclearTechMod.withDefaultNamespace("textures/armor/ncrpa_chest.png");
    public static final ResourceLocation NCRPA_ARM = NuclearTechMod.withDefaultNamespace("textures/armor/ncrpa_arm.png");
    public static final ResourceLocation NCRPA_LEG = NuclearTechMod.withDefaultNamespace("textures/armor/ncrpa_leg.png");

    public static final ResourceLocation HAT_TEX = NuclearTechMod.withDefaultNamespace("textures/armor/hat.png");
    public static final ResourceLocation NO9_TEX = NuclearTechMod.withDefaultNamespace("textures/armor/no9.png");
    public static final ResourceLocation NO9_INSIGNIA_TEX = NuclearTechMod.withDefaultNamespace("textures/armor/no9_insignia.png");

    //ZIRNOX
    //ICF
    public static final ResourceLocation ICF_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/icf.png");
    public static final ResourceLocation FUSION_TORUS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/torus.png");
    public static final ResourceLocation FUSION_PLASMA_FORGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/plasma_forge.png");
    public static final ResourceLocation BREEDER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/breeder.png");
    public static final ResourceLocation REACTOR_SMALL_BASE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/reactors/reactor_small_base.png");
    public static final ResourceLocation REACTOR_SMALL_RODS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/reactors/reactor_small_rods.png");
    public static final ResourceLocation FUSION_KLYSTRON_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/klystron.png");
    public static final ResourceLocation FUSION_COLLECTOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/collector.png");
    public static final ResourceLocation FUSION_BREEDER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/breeder.png");
    public static final ResourceLocation FUSION_COUPLER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/coupler.png");
    public static final ResourceLocation FUSION_BOILER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/boiler.png");
    public static final ResourceLocation FUSION_MHDT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/fusion/mhdt.png");

    //CHICAGO PILE
    public static final ResourceLocation PILE_LOADER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/pile/pile_loader.png");
    public static final ResourceLocation PILE_VENT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/pile/pile_vent.png");
    public static final ResourceLocation PILE_CONTROL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/pile/pile_control.png");

    //WATZ
    public static final ResourceLocation WATZ_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/watz.png");
    public static final ResourceLocation WATZ_PUMP_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/watz_pump.png");

    public static final ResourceLocation ZIRNOX_TEX = NuclearTechMod.withDefaultNamespace("textures/models/zirnox.png");
    public static final ResourceLocation ZIRNOX_DESTROYED_TEX = NuclearTechMod.withDefaultNamespace("textures/models/zirnox_destroyed.png");

    //NETZWERK: Strommasten, Umspannwerk, Anschlusskaesten
    public static final ResourceLocation PYLON_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/pylon.png");
    public static final ResourceLocation PYLON_STEEL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/pylon_steel.png");
    public static final ResourceLocation PYLON_MEDIUM_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/pylon_medium.png");
    public static final ResourceLocation PYLON_MEDIUM_STEEL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/pylon_medium_steel.png");
    public static final ResourceLocation PYLON_LARGE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/pylon_large.png");
    public static final ResourceLocation SUBSTATION_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/substation.png");
    public static final ResourceLocation WIRE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/wire.png");
    public static final ResourceLocation WIRE_GREYSCALE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/wire_greyscale.png");
    public static final ResourceLocation CONNECTOR_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/connector.png");
    public static final ResourceLocation CONNECTOR_SUPER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/network/connector_super.png");
    public static final ResourceLocation COMBUSTION_ENGINE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/combustion_engine.png");
    public static final ResourceLocation TURBINEGAS_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/turbinegas.png");
    public static final ResourceLocation TURBOFAN_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/turbofan.png");
    public static final ResourceLocation CRYSTALLIZER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/acidizer.png");
    public static final ResourceLocation THRESHER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/thresher.png");
    public static final ResourceLocation SAWMILL_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/sawmill.png");
    public static final ResourceLocation TURBOFAN_BACK_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/turbofan_back.png");
    public static final ResourceLocation TURBOFAN_AFTERBURNER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/turbofan_afterburner.png");
    public static final ResourceLocation RTG_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/rtg.png");
    public static final ResourceLocation CONDENSER_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/condenser.png");

    //DOPPELOFEN-AUFSATZ: je Modellteil eine eigene Blocktextur wie im Original
    public static final ResourceLocation DIFURNACE_EXTENSION_SIDE_TEX = NuclearTechMod.withDefaultNamespace("textures/block/difurnace_extension.png");
    public static final ResourceLocation DIFURNACE_EXTENSION_TOP_TEX = NuclearTechMod.withDefaultNamespace("textures/block/difurnace_top_off_alt.png");
    public static final ResourceLocation DIFURNACE_EXTENSION_BOTTOM_TEX = NuclearTechMod.withDefaultNamespace("textures/block/brick_fire.png");

    public static final ResourceLocation WHITE_TEX = NuclearTechMod.withDefaultNamespace("textures/models/white.png");

    // Landmines
    public static IModelCustom mine_ap;
    public static IModelCustom mine_he;
    public static IModelCustom mine_naval;
    public static IModelCustom mine_fat;

    // Tank
    public static IModelCustom bigasstank;
    public static IModelCustom fluid_tank;
    public static IModelCustom fluid_tank_exploded;

    // Large Turbine
    public static IModelCustom chungus;

    // Press
    public static IObjRenderer press_body;
    public static IObjRenderer press_head;

    // Assembler
    public static IModelCustom assembly_machine;
    public static IModelCustom ore_slopper;
    public static IModelCustom mining_drill;
    public static IModelCustom assembly_factory;

    public static IModelCustom nuke_gadget;
    public static IModelCustom nuke_little_boy;
    public static IObjRenderer nuke_fat_man;
    public static IModelCustom nuke_ivy_mike;
    public static IModelCustom nuke_tsar;
    public static IModelCustom nuke_prototype;
    public static IModelCustom nuke_fleija;
    public static IModelCustom nuke_solinium;
    public static IModelCustom nuke_n2;
    public static IModelCustom nuke_fstbmb;

    public static IModelCustom dud_balefire;
    public static IModelCustom dud_conventional;
    public static IModelCustom dud_nuke;
    public static IModelCustom dud_salted;

    public static HFRWavefrontObject barrel;
    public static HFRWavefrontObject barbed_wire;
    public static HFRWavefrontObject anvil;
    public static HFRWavefrontObject spikes;
    public static HFRWavefrontObject cable_neo;
    public static HFRWavefrontObject pipe_neo;
    public static IModelCustom difurnace_extension;

    // Netzwerk
    public static IModelCustom pylon;
    public static IModelCustom pylon_medium;
    public static IModelCustom pylon_large;
    public static IModelCustom substation;
    public static IModelCustom connector;
    public static IModelCustom connector_super;
    public static IModelCustom combustion_engine;
    public static IModelCustom turbinegas;
    public static IModelCustom turbofan;
    public static IModelCustom crystallizer;
    public static IModelCustom thresher;
    public static IModelCustom sawmill;
    public static IModelCustom rtg;

    public static IModelCustom geiger;

    // FENSU
    public static IModelCustom battery_socket;
    public static IModelCustom battery_redd;
    public static IModelCustom soldering_station;
    public static IModelCustom rotary_furnace;
    public static IModelCustom crucible;
    public static IModelCustom arc_furnace;
    public static IModelCustom arc_welder;
    public static IModelCustom diesel_generator;
    public static IModelCustom compressor;
    public static IModelCustom condenser;
    public static IModelCustom solar_boiler;
    public static IModelCustom solar_mirror;
    public static IModelCustom furnace_iron;
    public static IModelCustom furnace_steel;
    public static IModelCustom mixer;
    public static IModelCustom rock_mill;
    public static IModelCustom steam_engine;
    public static IModelCustom stirling;
    //Oil Pumps
    public static IModelCustom oil_derrick;
    public static IModelCustom pumpjack;
    public static IModelCustom fracking_tower;
    public static IModelCustom refinery;
    public static IModelCustom fractionTower;
    public static IModelCustom fractionSpacer;
    public static IModelCustom catalyticReformer;
    public static IModelCustom hydrotreater;
    public static IModelCustom deuterium_tower;
    public static IModelCustom refueler;
    public static IModelCustom vacuumDistill;
    public static IModelCustom solidifier;
    public static IModelCustom pyroOven;
    public static IModelCustom liquefactor;
    public static IModelCustom flareStack;
    public static IModelCustom coker;
    public static IModelCustom catalyticCracker;
    public static IModelCustom furnace_combination;
    public static IModelCustom blast_furnace;
    public static IModelCustom wood_burner;
    public static IModelCustom centrifuge;
    /* Runde 42: die Truemmer einer Kernschmelze. */
    public static IModelCustom deb_blank;
    public static IModelCustom deb_element;
    public static IModelCustom deb_fuel;
    public static IModelCustom deb_rod;
    public static IModelCustom deb_lid;
    public static IModelCustom deb_graphite;
    public static IModelCustom deb_zirnox_blank;
    public static IModelCustom deb_zirnox_concrete;
    public static IModelCustom deb_zirnox_element;
    public static IModelCustom deb_zirnox_exchanger;
    public static IModelCustom deb_zirnox_shrapnel;
    public static IModelCustom rbmk_gauge;
    public static IModelCustom rbmk_indicator;
    public static IModelCustom rbmk_numitron;
    public static IModelCustom rbmk_lever;
    public static IModelCustom rbmk_button;
    public static IModelCustom rbmk_terminal;
    public static IModelCustom rbmk_console;
    public static IModelCustom purex;
    public static IModelCustom radiolysis;
    public static IModelCustom chemical_plant;
    public static IModelCustom boiler;
    public static IModelCustom boiler_burst;
    public static IModelCustom industrial_boiler;
    public static IModelCustom heater_firebox;
    public static IModelCustom heater_oven;
    public static IModelCustom chimney_brick;
    public static IModelCustom chimney_industrial;
    public static IModelCustom rbmk_autoloader;
    public static IModelCustom rbmk_rods;
    public static IModelCustom rbmk_element_rods;
    /** Kappe und Innenrohr eines Brennstoffkanals -- im Original ueber ObjUtil gezeichnet. */
    public static IModelCustom rbmk_element;
    public static IModelCustom rbmk_crane;
    public static IModelCustom rbmk_crane_console;
    public static IModelCustom heater_oilburner;
    public static IModelCustom heater_electric;
    public static IModelCustom heater_heatex;
    public static IModelCustom tesla;
    public static IModelCustom skeleton_holder;
    public static IModelCustom demon_lamp;

    // Radar
    public static IModelCustom radar_body;
    public static IModelCustom radar;
    public static IModelCustom radar_large;
    public static IModelCustom radar_screen;

    // Nachgereichte Geraete (Runde 161)
    public static IModelCustom satlink;
    public static IModelCustom tape_drive;
    public static IModelCustom supercomputer;
    public static IModelCustom ammo_press;
    public static IModelCustom autosaw;

    // Nachgereichte Geraete (Runde 162)
    public static IModelCustom gascent;
    public static IModelCustom annihilator;
    public static IModelCustom exposure_chamber;
    public static IModelCustom mining_laser;
    public static IModelCustom radgen;
    public static IModelCustom cyclotron;

    // Teilchenbeschleuniger (Runde 168)
    public static IModelCustom pa_source;
    public static IModelCustom pa_beamline;
    public static IModelCustom pa_rfc;
    public static IModelCustom pa_quadrupole;
    public static IModelCustom pa_dipole;
    public static IModelCustom pa_detector;

    // Kuehltuerme (Runde 169)
    public static IModelCustom tower_small;
    public static IModelCustom floodlight;
    public static IModelCustom charger;
    public static IModelCustom telex;
    public static IModelCustom tower_large;

    // Boxcar
    public static IModelCustom tom_main;
    public static IModelCustom tom_flame;

    public static IObjRenderer casings;

    // Bomber
    public static IModelCustom dornier;
    public static IModelCustom b29;
    public static IModelCustom c130;

    // Missiles
    public static IModelCustom missileV2;
    public static IModelCustom missileABM;
    public static IModelCustom missileStealth;
    public static IModelCustom missileStrong;
    public static IModelCustom missileHuge;
    public static IModelCustom missileNuclear;
    public static IModelCustom missileMicro;
    public static IModelCustom missileShuttle;
    public static IModelCustom soyuz;
    public static IModelCustom soyuz_lander;
    public static IModelCustom dropship;
    public static IModelCustom soyuz_module;
    public static IModelCustom soyuz_launcher_legs;
    public static IModelCustom soyuz_launcher_table;
    public static IModelCustom soyuz_launcher_tower_base;
    public static IModelCustom soyuz_launcher_tower;
    public static IModelCustom soyuz_launcher_support_base;
    public static IModelCustom soyuz_launcher_support;

    //Missile Parts
    public static IModelCustom missile_pad;
    public static IModelCustom missile_erector;

    public static IModelCustom bomblet_zeta;

    public static IModelCustom detonator_laser;

    //Shimmer Sledge
    public static IModelCustom shimmer_sledge;
    public static IModelCustom shimmer_axe;

    public static IModelCustom chip;
    public static IModelCustom ni4ni;
    public static IModelCustom henry;
    public static IModelCustom maresleg;
    public static IModelCustom pepperbox;
    public static IModelCustom am180;
    public static IModelCustom star_f;
    public static IModelCustom uzi;
    public static IModelCustom amat;
    public static IModelCustom m2;
    public static IModelCustom g3;
    public static IModelCustom stg77;
    public static IModelCustom carbine;
    public static IModelCustom mas36;
    public static IModelCustom minigun;
    public static IModelCustom bolter;
    public static IModelCustom aberrator;
    public static IModelCustom congolake;
    public static IModelCustom flamethrower;
    public static IModelCustom drill;
    public static IModelCustom tesla_cannon;
    public static IModelCustom folly;
    public static IModelCustom fireext;
    public static IModelCustom charge_thrower;
    public static IModelCustom chemthrower;
    public static IModelCustom laser_pistol;
    public static IModelCustom lasrifle;
    public static IModelCustom flaregun;
    public static IModelCustom mk108;
    public static IModelCustom turret_sentry;
    public static IModelCustom turret_chekhov;
    public static IModelCustom turret_jeremy;
    public static IModelCustom turret_howard;
    public static IModelCustom bio_revolver;
    public static IModelCustom lilmac;
    public static IModelCustom spas_12;
    public static IModelCustom fatman;
    public static IModelCustom hangman;
    public static IModelCustom greasegun;
    public static IModelCustom double_barrel;
    public static IModelCustom liberator;
    public static IModelCustom panzerschreck;
    public static IModelCustom boltgun;
    public static IModelCustom quadro;
    public static IModelCustom missile_launcher;
    public static IModelCustom boxcar;
    public static IModelCustom duchessgambit;
    public static IModelCustom torpedo;
    public static IModelCustom shredder;
    public static IModelCustom n_i_4_n_i;

    public static HashMap<String, BusAnimation> spas_12_anim;
    public static HashMap<String, BusAnimation> am180_anim;
    public static HashMap<String, BusAnimation> stg77_anim;
    public static HashMap<String, BusAnimation> congolake_anim;
    public static HashMap<String, BusAnimation> flamethrower_anim;

    public static IModelCustom armor_hev;
    public static IModelCustom armor_remnant;
    public static IModelCustom armor_ncrpa;
    public static IModelCustom armor_hat;
    public static IModelCustom armor_no9;

    public static IObjRenderer sphere;
    public static IModelCustom sphere_new;

    // ZIRNOX
    // ICF
    public static IModelCustom icf;
    public static IModelCustom fusion_torus;
    public static IModelCustom fusion_klystron;
    public static IModelCustom fusion_collector;
    public static IModelCustom fusion_breeder;
    public static IModelCustom fusion_coupler;
    public static IModelCustom fusion_boiler;
    public static IModelCustom fusion_mhdt;
    public static IModelCustom fusion_plasma_forge;
    public static IModelCustom breeder;
    public static IModelCustom reactor_small_base;
    public static IModelCustom reactor_small_rods;

    // CHICAGO PILE
    public static IModelCustom pile_loader;
    public static IModelCustom pile_vent;
    public static IModelCustom pile_control;

    // WATZ
    public static IModelCustom watz;
    public static IModelCustom watz_pump;

    public static IModelCustom zirnox;
    public static IModelCustom zirnox_destroyed;

    public static void init() {

        mine_ap = new HFRWavefrontObject("models/obj/bomb/mine_ap.obj").asVBO();
        mine_he = new HFRWavefrontObject("models/obj/bomb/mine_he.obj").asVBO();
        mine_naval = new HFRWavefrontObject("models/obj/bomb/mine_naval.obj").asVBO();
        mine_fat = new HFRWavefrontObject("models/obj/bomb/mine_fat.obj").asVBO();

        bigasstank = new HFRWavefrontObject("models/obj/machines/bigasstank.obj").asVBO();
        fluid_tank = new HFRWavefrontObject("models/obj/machines/fluid_tank.obj").asVBO();
        fluid_tank_exploded = new HFRWavefrontObject("models/obj/machines/fluid_tank_exploded.obj").asVBO();

        chungus = new HFRWavefrontObject("models/obj/machines/chungus.obj").asVBO();

        press_body = new HFRWavefrontObject("models/obj/press_body.obj").getRenderer();
        press_head = new HFRWavefrontObject("models/obj/press_head.obj").getRenderer();

        assembly_machine = new HFRWavefrontObject("models/obj/machines/assembly_machine.obj").asVBO();
        ore_slopper = new HFRWavefrontObject("models/obj/machines/ore_slopper.obj").asVBO();
        mining_drill = new HFRWavefrontObject("models/obj/machines/mining_drill.obj").asVBO();
        assembly_factory = new HFRWavefrontObject("models/obj/machines/assembly_factory.obj").asVBO();

        nuke_gadget = new HFRWavefrontObject("models/obj/bomb/nuke_gadget.obj").asVBO();
        nuke_little_boy = new HFRWavefrontObject("models/obj/bomb/nuke_little_boy.obj").asVBO();
        nuke_fat_man = new HFRWavefrontObject("models/obj/bomb/nuke_fatman.obj").getRenderer();
        nuke_ivy_mike = new HFRWavefrontObject("models/obj/bomb/nuke_ivy_mike.obj").asVBO();
        nuke_tsar = new HFRWavefrontObject("models/obj/bomb/nuke_tsar.obj").asVBO();
        nuke_prototype = new HFRWavefrontObject("models/obj/bomb/nuke_prototype.obj").asVBO();
        nuke_fleija = new HFRWavefrontObject("models/obj/bomb/nuke_fleija.obj").asVBO();
        nuke_solinium = new HFRWavefrontObject("models/obj/bomb/nuke_solinium.obj").asVBO();
        nuke_n2 = new HFRWavefrontObject("models/obj/bomb/nuke_n2.obj").asVBO();
        nuke_fstbmb = new HFRWavefrontObject("models/obj/bomb/nuke_fstbmb.obj").asVBO();

        dud_balefire = new HFRWavefrontObject("models/obj/bomb/dud_balefire.obj").asVBO();
        dud_conventional = new HFRWavefrontObject("models/obj/bomb/dud_conventional.obj").asVBO();
        dud_nuke = new HFRWavefrontObject("models/obj/bomb/dud_nuke.obj").asVBO();
        dud_salted = new HFRWavefrontObject("models/obj/bomb/dud_salted.obj").asVBO();

        barrel = new HFRWavefrontObject("models/obj/block/barrel.obj");
        barbed_wire = new HFRWavefrontObject("models/obj/block/barbed_wire.obj");
        anvil = new HFRWavefrontObject("models/obj/block/anvil.obj");
        spikes = new HFRWavefrontObject("models/obj/block/spikes.obj");
        cable_neo = new HFRWavefrontObject("models/obj/block/cable_neo.obj");
        pipe_neo = new HFRWavefrontObject("models/obj/block/pipe_neo.obj");
        difurnace_extension = new HFRWavefrontObject("models/obj/block/difurnace_extension.obj").asVBO();

        // Das Original laedt die Masten mit noSmooth(), das Umspannwerk bewusst ohne.
        pylon = new HFRWavefrontObject("models/obj/network/pylon.obj").noSmooth().asVBO();
        pylon_medium = new HFRWavefrontObject("models/obj/network/pylon_medium.obj").noSmooth().asVBO();
        pylon_large = new HFRWavefrontObject("models/obj/network/pylon_large.obj").noSmooth().asVBO();
        substation = new HFRWavefrontObject("models/obj/network/substation.obj").asVBO();
        connector = new HFRWavefrontObject("models/obj/network/connector.obj").noSmooth().asVBO();
        connector_super = new HFRWavefrontObject("models/obj/network/connector_super.obj").noSmooth().asVBO();
        combustion_engine = new HFRWavefrontObject("models/obj/machines/combustion_engine.obj").asVBO();
        turbinegas = new HFRWavefrontObject("models/obj/machines/turbinegas.obj").asVBO();
        turbofan = new HFRWavefrontObject("models/obj/machines/turbofan.obj").asVBO();
        crystallizer = new HFRWavefrontObject("models/obj/machines/acidizer.obj").asVBO();
        thresher = new HFRWavefrontObject("models/obj/machines/thresher.obj").asVBO();
        sawmill = new HFRWavefrontObject("models/obj/machines/sawmill.obj").asVBO();
        // noSmooth() wie im Original (ResourceManager.java:211 von 1.7.10)
        rtg = new HFRWavefrontObject("models/obj/machines/rtg.obj").noSmooth().asVBO();

        geiger = new HFRWavefrontObject("models/obj/block/geiger.obj").asVBO();

        battery_socket = new HFRWavefrontObject("models/obj/machines/battery.obj").asVBO();
        battery_redd = new HFRWavefrontObject("models/obj/machines/fensu2.obj").asVBO();
        soldering_station = new HFRWavefrontObject("models/obj/machines/soldering_station.obj").asVBO();
        rotary_furnace = new HFRWavefrontObject("models/obj/machines/rotary_furnace.obj").asVBO();
        crucible = new HFRWavefrontObject("models/obj/machines/crucible.obj").asVBO();
        arc_furnace = new HFRWavefrontObject("models/obj/machines/arc_furnace.obj").asVBO();
        arc_welder = new HFRWavefrontObject("models/obj/machines/arc_welder.obj").asVBO();
        diesel_generator = new HFRWavefrontObject("models/obj/machines/diesel_generator.obj").asVBO();
        compressor = new HFRWavefrontObject("models/obj/machines/compressor.obj").asVBO();
        condenser = new HFRWavefrontObject("models/obj/machines/condenser.obj").asVBO();
        solar_boiler = new HFRWavefrontObject("models/obj/machines/solar_boiler.obj").asVBO();
        solar_mirror = new HFRWavefrontObject("models/obj/machines/solar_mirror.obj").noSmooth().asVBO();
        furnace_iron = new HFRWavefrontObject("models/obj/machines/furnace_iron.obj").asVBO();
        furnace_steel = new HFRWavefrontObject("models/obj/machines/furnace_steel.obj").asVBO();
        mixer = new HFRWavefrontObject("models/obj/machines/mixer.obj").asVBO();
        rock_mill = new HFRWavefrontObject("models/obj/machines/rockmill.obj").asVBO();
        steam_engine = new HFRWavefrontObject("models/obj/machines/steam_engine.obj").asVBO();
        stirling = new HFRWavefrontObject("models/obj/machines/stirling.obj").asVBO();
        oil_derrick = new HFRWavefrontObject("models/obj/machines/oil_derrick.obj").asVBO();
        pumpjack = new HFRWavefrontObject("models/obj/machines/pumpjack.obj").asVBO();
        fracking_tower = new HFRWavefrontObject("models/obj/machines/fracking_tower.obj").asVBO();
        refinery = new HFRWavefrontObject("models/obj/machines/refinery.obj").asVBO();
        fractionTower = new HFRWavefrontObject("models/obj/machines/fraction_tower.obj").asVBO();
        fractionSpacer = new HFRWavefrontObject("models/obj/machines/fraction_spacer.obj").asVBO();
        catalyticReformer = new HFRWavefrontObject("models/obj/machines/catalytic_reformer.obj").asVBO();
        hydrotreater = new HFRWavefrontObject("models/obj/machines/hydrotreater.obj").asVBO();
        deuterium_tower = new HFRWavefrontObject("models/obj/machines/machine_deuterium_tower.obj").asVBO();
        refueler = new HFRWavefrontObject("models/obj/machines/refueler.obj").asVBO();
        vacuumDistill = new HFRWavefrontObject("models/obj/machines/vacuum_distill.obj").asVBO();
        solidifier = new HFRWavefrontObject("models/obj/machines/solidifier.obj").asVBO();
        pyroOven = new HFRWavefrontObject("models/obj/machines/pyrooven.obj").asVBO();
        liquefactor = new HFRWavefrontObject("models/obj/machines/liquefactor.obj").asVBO();
        flareStack = new HFRWavefrontObject("models/obj/machines/flare_stack.obj").asVBO();
        coker = new HFRWavefrontObject("models/obj/machines/coker.obj").asVBO();
        catalyticCracker = new HFRWavefrontObject("models/obj/machines/catalytic_cracker.obj").asVBO();
        furnace_combination = new HFRWavefrontObject("models/obj/machines/combination_oven.obj").asVBO();
        blast_furnace = new HFRWavefrontObject("models/obj/machines/blast_furnace.obj").asVBO();
        wood_burner = new HFRWavefrontObject("models/obj/machines/wood_burner.obj").asVBO();
        centrifuge = new HFRWavefrontObject("models/obj/machines/centrifuge.obj").asVBO();
        deb_blank = new HFRWavefrontObject("models/obj/debris/deb_blank.obj").asVBO();
        deb_element = new HFRWavefrontObject("models/obj/debris/deb_element.obj").asVBO();
        deb_fuel = new HFRWavefrontObject("models/obj/debris/deb_fuel.obj").asVBO();
        deb_rod = new HFRWavefrontObject("models/obj/debris/deb_rod.obj").asVBO();
        deb_lid = new HFRWavefrontObject("models/obj/debris/deb_lid.obj").asVBO();
        deb_graphite = new HFRWavefrontObject("models/obj/debris/deb_graphite.obj").asVBO();
        deb_zirnox_blank = new HFRWavefrontObject("models/obj/debris/deb_zirnox_blank.obj").asVBO();
        deb_zirnox_concrete = new HFRWavefrontObject("models/obj/debris/deb_zirnox_concrete.obj").asVBO();
        deb_zirnox_element = new HFRWavefrontObject("models/obj/debris/deb_zirnox_element.obj").asVBO();
        deb_zirnox_exchanger = new HFRWavefrontObject("models/obj/debris/deb_zirnox_exchanger.obj").asVBO();
        deb_zirnox_shrapnel = new HFRWavefrontObject("models/obj/debris/deb_zirnox_shrapnel.obj").asVBO();
        rbmk_gauge = new HFRWavefrontObject("models/obj/machines/rbmk_gauge.obj").asVBO();
        rbmk_indicator = new HFRWavefrontObject("models/obj/machines/rbmk_indicator.obj").asVBO();
        rbmk_numitron = new HFRWavefrontObject("models/obj/machines/rbmk_numitron.obj").asVBO();
        rbmk_lever = new HFRWavefrontObject("models/obj/machines/rbmk_lever.obj").asVBO();
        rbmk_button = new HFRWavefrontObject("models/obj/machines/rbmk_button.obj").asVBO();
        rbmk_terminal = new HFRWavefrontObject("models/obj/machines/rbmk_terminal.obj").asVBO();
        rbmk_console = new HFRWavefrontObject("models/obj/machines/rbmk_console.obj").asVBO();
        purex = new HFRWavefrontObject("models/obj/machines/purex.obj").asVBO();
        radiolysis = new HFRWavefrontObject("models/obj/machines/radiolysis.obj").asVBO();
        chemical_plant = new HFRWavefrontObject("models/obj/machines/chemical_plant.obj").asVBO();
        boiler = new HFRWavefrontObject("models/obj/machines/boiler.obj").asVBO();
        boiler_burst = new HFRWavefrontObject("models/obj/machines/boiler_burst.obj").asVBO();
        industrial_boiler = new HFRWavefrontObject("models/obj/machines/industrial_boiler.obj").asVBO();
        heater_firebox = new HFRWavefrontObject("models/obj/machines/firebox.obj").asVBO();
        heater_oven = new HFRWavefrontObject("models/obj/machines/heating_oven.obj").asVBO();
        chimney_brick = new HFRWavefrontObject("models/obj/machines/chimney_brick.obj").asVBO();
        chimney_industrial = new HFRWavefrontObject("models/obj/machines/chimney_industrial.obj").asVBO();
        rbmk_autoloader = new HFRWavefrontObject("models/obj/machines/rbmk_autoloader.obj").asVBO();
        rbmk_rods = new HFRWavefrontObject("models/obj/machines/rbmk_rods.obj").asVBO();
        rbmk_element_rods = new HFRWavefrontObject("models/obj/machines/rbmk_element_rods.obj").asVBO();
        rbmk_element = new HFRWavefrontObject("models/obj/machines/rbmk_element.obj").asVBO();
        rbmk_crane = new HFRWavefrontObject("models/obj/machines/rbmk_crane.obj").asVBO();
        rbmk_crane_console = new HFRWavefrontObject("models/obj/machines/rbmk_crane_console.obj").asVBO();
        heater_oilburner = new HFRWavefrontObject("models/obj/machines/oilburner.obj").asVBO();
        heater_electric = new HFRWavefrontObject("models/obj/machines/electric_heater.obj").asVBO();
        heater_heatex = new HFRWavefrontObject("models/obj/machines/heatex.obj").asVBO();
        tesla = new HFRWavefrontObject("models/obj/machines/tesla.obj").asVBO();
        skeleton_holder = new HFRWavefrontObject("models/obj/blocks/skeleton_holder.obj").noSmooth().asVBO();
        demon_lamp = new HFRWavefrontObject("models/obj/blocks/demon_lamp.obj").asVBO();

        radar_body = new HFRWavefrontObject("models/obj/radar_base.obj").noSmooth().asVBO();
        radar = new HFRWavefrontObject("models/obj/machines/radar.obj").noSmooth().asVBO();
        radar_large = new HFRWavefrontObject("models/obj/machines/radar_large.obj").noSmooth().asVBO();
        radar_screen = new HFRWavefrontObject("models/obj/machines/radar_screen.obj").noSmooth().asVBO();

        satlink = new HFRWavefrontObject("models/obj/machines/satlink.obj").noSmooth().asVBO();
        tape_drive = new HFRWavefrontObject("models/obj/machines/tape_drive.obj").noSmooth().asVBO();
        supercomputer = new HFRWavefrontObject("models/obj/machines/supercomputer.obj").asVBO();
        ammo_press = new HFRWavefrontObject("models/obj/machines/ammo_press.obj").asVBO();
        autosaw = new HFRWavefrontObject("models/obj/machines/autosaw.obj").noSmooth().asVBO();

        gascent = new HFRWavefrontObject("models/obj/machines/gascent.obj").asVBO();
        annihilator = new HFRWavefrontObject("models/obj/machines/annihilator.obj").asVBO();
        exposure_chamber = new HFRWavefrontObject("models/obj/machines/exposure_chamber.obj").asVBO();
        mining_laser = new HFRWavefrontObject("models/obj/machines/mining_laser.obj").asVBO();
        /* Das Original laedt den Radiothermalgenerator als einziges Modell ohne asVBO -- das
         * geht hier nicht: dort ist der Lader selbst ein IModelCustom, in diesem Port liefert
         * erst asVBO() eines. Die Unterscheidung existiert also gar nicht. */
        radgen = new HFRWavefrontObject("models/obj/machines/radgen.obj").asVBO();
        cyclotron = new HFRWavefrontObject("models/obj/machines/cyclotron.obj").asVBO();

        pa_source = new HFRWavefrontObject("models/obj/particleaccelerator/source.obj").asVBO();
        pa_beamline = new HFRWavefrontObject("models/obj/particleaccelerator/beamline.obj").asVBO();
        pa_rfc = new HFRWavefrontObject("models/obj/particleaccelerator/rfc.obj").asVBO();
        pa_quadrupole = new HFRWavefrontObject("models/obj/particleaccelerator/quadrupole.obj").asVBO();
        pa_dipole = new HFRWavefrontObject("models/obj/particleaccelerator/dipole.obj").asVBO();
        pa_detector = new HFRWavefrontObject("models/obj/particleaccelerator/detector.obj").asVBO();

        tower_small = new HFRWavefrontObject("models/obj/machines/tower_small.obj").asVBO();
        floodlight = new HFRWavefrontObject("models/obj/block/floodlight.obj").asVBO();
        charger = new HFRWavefrontObject("models/obj/block/charger.obj").asVBO();
        telex = new HFRWavefrontObject("models/obj/machines/telex.obj").asVBO();
        tower_large = new HFRWavefrontObject("models/obj/machines/tower_large.obj").asVBO();

        tom_main = new HFRWavefrontObject("models/obj/weapons/tom_main.obj").asVBO();
        tom_flame = new HFRWavefrontObject("models/obj/weapons/tom_flame.obj").asVBO();

        casings = new HFRWavefrontObject("models/obj/effect/casings.obj").getRenderer();

        dornier = new HFRWavefrontObject("models/obj/dornier.obj").asVBO();
        b29 = new HFRWavefrontObject("models/obj/b29.obj").asVBO();
        c130 = new HFRWavefrontObject("models/obj/weapons/c130.obj").asVBO();

        missileV2 = new HFRWavefrontObject("models/obj/missile_v2.obj").asVBO();
        missileABM = new HFRWavefrontObject("models/obj/missile_abm.obj").asVBO();
        missileStealth = new HFRWavefrontObject("models/obj/missile_stealth.obj").asVBO();
        missileStrong = new HFRWavefrontObject("models/obj/missile_strong.obj").asVBO();
        missileHuge = new HFRWavefrontObject("models/obj/missile_huge.obj").asVBO();
        missileNuclear = new HFRWavefrontObject("models/obj/missile_atlas.obj").asVBO();
        missileMicro = new HFRWavefrontObject("models/obj/missile_micro.obj").asVBO();
        missileShuttle = new HFRWavefrontObject("models/obj/missile_shuttle.obj").asVBO();
        soyuz = new HFRWavefrontObject("models/obj/soyuz.obj").asVBO();
        soyuz_lander = new HFRWavefrontObject("models/obj/soyuz_lander.obj").asVBO();
        dropship = new HFRWavefrontObject("models/obj/dropship.obj").asVBO();
        soyuz_module = new HFRWavefrontObject("models/obj/soyuz_module.obj").asVBO();
        soyuz_launcher_legs = new HFRWavefrontObject("models/obj/launch_table/soyuz_launcher_legs.obj").noSmooth().asVBO();
        soyuz_launcher_table = new HFRWavefrontObject("models/obj/launch_table/soyuz_launcher_table.obj").noSmooth().asVBO();
        soyuz_launcher_tower_base = new HFRWavefrontObject("models/obj/launch_table/soyuz_launcher_tower_base.obj").noSmooth().asVBO();
        soyuz_launcher_tower = new HFRWavefrontObject("models/obj/launch_table/soyuz_launcher_tower.obj").noSmooth().asVBO();
        soyuz_launcher_support_base = new HFRWavefrontObject("models/obj/launch_table/soyuz_launcher_support_base.obj").noSmooth().asVBO();
        soyuz_launcher_support = new HFRWavefrontObject("models/obj/launch_table/soyuz_launcher_support.obj").noSmooth().asVBO();

        missile_pad = new HFRWavefrontObject("models/obj/weapons/launch_pad_silo.obj").asVBO();
        missile_erector = new HFRWavefrontObject("models/obj/weapons/launch_pad_erector.obj").asVBO();

        bomblet_zeta = new HFRWavefrontObject("models/obj/bomblet_zeta.obj").asVBO();

        detonator_laser = new HFRWavefrontObject("models/obj/weapons/detonator_laser.obj").asVBO();

        //shimmer_sledge = new HFRWavefrontObject("models/obj/shimmer_sledge.obj").asVBO();
        shimmer_axe = new HFRWavefrontObject("models/obj/shimmer_axe.obj").asVBO();

        chip = new HFRWavefrontObject("models/obj/trinkets/chip.obj").asVBO();
        ni4ni = new HFRWavefrontObject("models/obj/weapons/n_i_4_n_i.obj").asVBO();
        henry = new HFRWavefrontObject("models/obj/weapons/henry.obj").asVBO();
        maresleg = new HFRWavefrontObject("models/obj/weapons/maresleg.obj").asVBO();
        pepperbox = new HFRWavefrontObject("models/obj/weapons/pepperbox.obj").asVBO();
        am180 = new HFRWavefrontObject("models/obj/weapons/am180.obj").asVBO();
        star_f = new HFRWavefrontObject("models/obj/weapons/star_f.obj").asVBO();
        uzi = new HFRWavefrontObject("models/obj/weapons/uzi.obj").asVBO();
        amat = new HFRWavefrontObject("models/obj/weapons/amat.obj").asVBO();
        m2 = new HFRWavefrontObject("models/obj/weapons/m2_browning.obj").asVBO();
        g3 = new HFRWavefrontObject("models/obj/weapons/g3.obj").asVBO();
        stg77 = new HFRWavefrontObject("models/obj/weapons/stg77.obj").asVBO();
        carbine = new HFRWavefrontObject("models/obj/weapons/carbine.obj").asVBO();
        mas36 = new HFRWavefrontObject("models/obj/weapons/mas36.obj").asVBO();
        minigun = new HFRWavefrontObject("models/obj/weapons/minigun.obj").asVBO();
        bolter = new HFRWavefrontObject("models/obj/weapons/bolter.obj").asVBO();
        aberrator = new HFRWavefrontObject("models/obj/weapons/aberrator.obj").asVBO();
        congolake = new HFRWavefrontObject("models/obj/weapons/congolake.obj").asVBO();
        flamethrower = new HFRWavefrontObject("models/obj/weapons/flamethrower.obj").asVBO();
        drill = new HFRWavefrontObject("models/obj/weapons/drill.obj").asVBO();
        tesla_cannon = new HFRWavefrontObject("models/obj/weapons/tesla_cannon.obj").asVBO();
        folly = new HFRWavefrontObject("models/obj/weapons/folly.obj").asVBO();
        fireext = new HFRWavefrontObject("models/obj/weapons/fireext.obj").asVBO();
        charge_thrower = new HFRWavefrontObject("models/obj/weapons/charge_thrower.obj").asVBO();
        chemthrower = new HFRWavefrontObject("models/obj/weapons/chemthrower.obj").asVBO();
        laser_pistol = new HFRWavefrontObject("models/obj/weapons/laser_pistol.obj").asVBO();
        lasrifle = new HFRWavefrontObject("models/obj/weapons/lasrifle.obj").asVBO();
        flaregun = new HFRWavefrontObject("models/obj/weapons/flaregun.obj").asVBO();
        mk108 = new HFRWavefrontObject("models/obj/weapons/mk108.obj").asVBO();
        turret_sentry = new HFRWavefrontObject("models/obj/turrets/turret_sentry.obj").asVBO();
        turret_chekhov = new HFRWavefrontObject("models/obj/turrets/turret_chekhov.obj").asVBO();
        turret_jeremy = new HFRWavefrontObject("models/obj/turrets/turret_jeremy.obj").asVBO();
        turret_howard = new HFRWavefrontObject("models/obj/turrets/turret_howard.obj").asVBO();
        bio_revolver = new HFRWavefrontObject("models/obj/weapons/bio_revolver.obj").asVBO();
        lilmac = new HFRWavefrontObject("models/obj/weapons/lilmac.obj").asVBO();
        spas_12 = new HFRWavefrontObject("models/obj/weapons/spas-12.obj").asVBO();
        fatman = new HFRWavefrontObject("models/obj/weapons/fatman.obj").asVBO();
        hangman = new HFRWavefrontObject("models/obj/weapons/hangman.obj").asVBO();
        greasegun = new HFRWavefrontObject("models/obj/weapons/greasegun.obj").asVBO();
        double_barrel = new HFRWavefrontObject("models/obj/weapons/sacred_dragon.obj").asVBO();
        liberator = new HFRWavefrontObject("models/obj/weapons/liberator.obj").asVBO();
        panzerschreck = new HFRWavefrontObject("models/obj/weapons/panzerschreck.obj").asVBO();
        boltgun = new HFRWavefrontObject("models/obj/weapons/boltgun.obj").asVBO();
        quadro = new HFRWavefrontObject("models/obj/weapons/quadro.obj").asVBO();
        missile_launcher = new HFRWavefrontObject("models/obj/weapons/missile_launcher.obj").asVBO();
        boxcar = new HFRWavefrontObject("models/obj/boxcar.obj").asVBO();
        duchessgambit = new HFRWavefrontObject("models/obj/duchessgambit.obj").asVBO();
        torpedo = new HFRWavefrontObject("models/obj/weapons/torpedo.obj").asVBO();
        shredder = new HFRWavefrontObject("models/obj/weapons/shredder.obj").asVBO();
        n_i_4_n_i = new HFRWavefrontObject("models/obj/weapons/n_i_4_n_i.obj").asVBO();

        spas_12_anim = AnimationLoader.load(NuclearTechMod.withDefaultNamespace("models/animations/spas12.json"));
        am180_anim = AnimationLoader.load(NuclearTechMod.withDefaultNamespace("models/animations/am180.json"));
        stg77_anim = AnimationLoader.load(NuclearTechMod.withDefaultNamespace("models/animations/stg77.json"));
        congolake_anim = AnimationLoader.load(NuclearTechMod.withDefaultNamespace("models/animations/congolake.json"));
        flamethrower_anim = AnimationLoader.load(NuclearTechMod.withDefaultNamespace("models/animations/flamethrower.json"));

        armor_hev = new HFRWavefrontObject("models/obj/armor/hev.obj").asVBO();
        armor_remnant = new HFRWavefrontObject("models/obj/armor/remnant.obj").asVBO();
        armor_ncrpa = new HFRWavefrontObject("models/obj/armor/ncrpa.obj").asVBO();
        armor_hat = new HFRWavefrontObject("models/obj/armor/hat.obj").asVBO();
        armor_no9 = new HFRWavefrontObject("models/obj/armor/no9.obj").asVBO();

        sphere = new HFRWavefrontObject("models/obj/sphere.obj").getRenderer();
        sphere_new = new HFRWavefrontObject("models/obj/sphere_new.obj").asVBO();

        icf = new HFRWavefrontObject("models/obj/machines/icf.obj").asVBO();
        fusion_torus = new HFRWavefrontObject("models/obj/fusion/torus.obj").asVBO();
        fusion_klystron = new HFRWavefrontObject("models/obj/fusion/klystron.obj").asVBO();
        fusion_collector = new HFRWavefrontObject("models/obj/fusion/collector.obj").asVBO();
        fusion_breeder = new HFRWavefrontObject("models/obj/fusion/breeder.obj").asVBO();
        fusion_coupler = new HFRWavefrontObject("models/obj/fusion/coupler.obj").asVBO();
        fusion_boiler = new HFRWavefrontObject("models/obj/fusion/boiler.obj").asVBO();
        fusion_mhdt = new HFRWavefrontObject("models/obj/fusion/mhdt.obj").asVBO();
        fusion_plasma_forge = new HFRWavefrontObject("models/obj/fusion/plasma_forge.obj").asVBO();
        breeder = new HFRWavefrontObject("models/obj/reactors/breeder.obj").asVBO();
        reactor_small_base = new HFRWavefrontObject("models/obj/reactors/reactor_small_base.obj").asVBO();
        reactor_small_rods = new HFRWavefrontObject("models/obj/reactors/reactor_small_rods.obj").asVBO();
        pile_loader = new HFRWavefrontObject("models/obj/pile/pile_loader.obj").asVBO();
        pile_vent = new HFRWavefrontObject("models/obj/pile/pile_vent.obj").asVBO();
        pile_control = new HFRWavefrontObject("models/obj/pile/pile_control.obj").asVBO();
        watz = new HFRWavefrontObject("models/obj/machines/watz.obj").asVBO();
        watz_pump = new HFRWavefrontObject("models/obj/machines/watz_pump.obj").asVBO();
        zirnox = new HFRWavefrontObject("models/obj/zirnox.obj").asVBO();
        zirnox_destroyed = new HFRWavefrontObject("models/obj/zirnox_destroyed.obj").asVBO();
    }
}
