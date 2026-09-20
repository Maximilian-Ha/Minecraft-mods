package com.hbm.registry;

import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NtmSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, NuclearTechMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_REVOLVER_COCK = reg("weapon.reload.revolver_cock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_REVOLVER_CLOSE = reg("weapon.reload.revolver_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_REVOLVER_SPIN = reg("weapon.reload.revolver_spin");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_PISTOL_COCK = reg("weapon.reload.pistol_cock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_SMALL_REMOVE = reg("weapon.reload.mag_small_remove");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_SMALL_INSERT = reg("weapon.reload.mag_small_insert");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_REMOVE = reg("weapon.reload.mag_remove");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_INSERT = reg("weapon.reload.mag_insert");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LATCH_OPEN = reg("weapon.reload.latch_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LEVER_COCK = reg("weapon.reload.lever_cock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_LOAD = reg("weapon.reload.shotgun_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_OPEN = reg("weapon.reload.shotgun_cock_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_CLOSE = reg("weapon.reload.shotgun_cock_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_COCK = reg("weapon.reload.shotgun_cock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_DRY_FIRE = reg("weapon.reload.dry_fire_click");
    /** Der Faustschlag der Panzerruestung -- im Original NTMSounds "hbm:weapon.fire.smack". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SMACK = reg("weapon.fire.smack");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_HEAVY_REVOLVER_FIRE = reg("weapon.fire.44_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_FIRE = reg("weapon.fire.shotgun");
    /* Im Original heisst die Datei shotgunAlt; der Port schreibt Dateinamen klein mit Unterstrich. */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LIBERATOR_FIRE = reg("weapon.fire.shotgun_alt");
    /** Der Schlag der Teslaspule, vier Aufnahmen im Wechsel. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WEAPON_TESLA = reg("weapon.tesla");
    /* Die drei Schleifen des verseuchten Senders. Welche ein Sender spielt, haengt an
     * seinem Ort -- darum drei einzelne Ereignisse statt eines mit drei Aufnahmen. */
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_BROADCAST_1 = reg("block.broadcast1");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_BROADCAST_2 = reg("block.broadcast2");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_BROADCAST_3 = reg("block.broadcast3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_GREASEGUN_FIRE = reg("weapon.fire.greasegun");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_POWDER_FIRE = reg("weapon.fire.black_powder");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_FLAMER_LOOP = reg("weapon.fire.flame_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_TESLA_FIRE = reg("weapon.fire.tesla");
    /** Der lauteste Knall im Spiel -- das Original nennt die Kennung genauso. */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_FOLLY_FIRE = reg("weapon.fire.loudest_noise_on_earth");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_CHARGE_FIRE = reg("weapon.fire.grenade");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SCREW = reg("weapon.reload.screw");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_ROCKET_INSERT = reg("weapon.reload.insert_rocket");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_TESLA_BLAST = reg("entity.ufo_blast");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LASER_PISTOL_FIRE = reg("weapon.fire.laser_pistol");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LASER_RIFLE_FIRE = reg("weapon.fire.laser");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_VALVE = reg("weapon.reload.pressure_valve");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_EXTINGUISHER_FIRE = reg("weapon.extinguisher");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_PISTOL_FIRE = reg("weapon.fire.pistol");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_STARF_FIRE = reg("weapon.fire.pistol_light");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SILENCED_FIRE = reg("weapon.fire.silenced");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_UZI_FIRE = reg("weapon.fire.uzi");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_AMAT_FIRE = reg("weapon.fire.amat");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_ASSAULT_FIRE = reg("weapon.fire.assault");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_COIL_FIRE = reg("weapon.fire.coilgun");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_COIL_RELOAD = reg("weapon.reload.coilgun");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_RIFLE_FIRE = reg("weapon.fire.rifle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_HEAVY_RIFLE_FIRE = reg("weapon.fire.rifle_heavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_RIFLE_COCK = reg("weapon.reload.rifle_cock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_STAB = reg("weapon.fire.stab");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MINIGUN_FIRE = reg("weapon.fire.cal");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_ABERRATOR_FIRE = reg("weapon.fire.aberrator");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_UNDERBARREL_FIRE = reg("weapon.hk_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_CONGO_FIRE = reg("weapon.gl_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_GRENADE_RELOAD = reg("weapon.gl_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_GRENADE_OPEN = reg("weapon.gl_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_GRENADE_CLOSE = reg("weapon.gl_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_CANISTER_INSERT = reg("weapon.reload.insert_canister");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MK108_FIRE = reg("weapon.fire.mk108");
    /** Der Abschuss einer Rakete -- im Original NTMSounds.GUN_ROCKET_FIRE = "hbm:weapon.rpgShoot". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_ROCKET_FIRE = reg("weapon.rpg_shoot");
    /** Der laufende Suchton des Stingers -- im Original HBMSoundHandler.lockon = "hbm:weapon.fire.lockon". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LOCKON = reg("weapon.fire.lockon");
    /** Das Aufsetzen einer geworfenen Granate -- im Original "hbm:weapon.grenadeBounce". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GRENADE_BOUNCE = reg("weapon.grenade_bounce");
    /** Der Gueterwagen schlaegt auf -- im Original "hbm:weapon.trainImpact". */
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAIN_IMPACT = reg("weapon.train_impact");
    /** Das Signalhorn, mit dem der Lilmac seinen Gueterwagen ankuendigt. Im Original heisst die
     *  Konstante GUN_GO_GO_GADGET_FUCK_EVERYTHING_IN_THIS_GENERAL_DIRECTION und zeigt auf
     *  "hbm:alarm.trainHorn". */
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAIN_HORN = reg("alarm.train_horn");
    /** Das Nebelhorn der Duchess Gambit beim Aufschlag -- im Original "hbm:alarm.gambit". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GAMBIT = reg("alarm.gambit");
    /** Womit die schoene Autoschrotflinte ihr Luftschiff ankuendigt. Im Original heisst die
     *  Konstante GUN_SOLDIER_TF2_BOAT_EXE_WAV_MP3 und zeigt auf "hbm:weapon.boat". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_BOAT = reg("weapon.boat");
    /** Der Nachladetakt der Autoschrotflinte -- im Original "hbm:weapon.fire.shredderCycle". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHREDDER_CYCLE = reg("weapon.fire.shreddercycle");
    /** Der Schuss der Autoschrotflinte -- im Original "hbm:weapon.fire.shotgunAuto". */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHREDDER_FIRE = reg("weapon.fire.shotgunauto");
    /** Das Lacunae-Lasergatling -- im Original HBMSoundHandler.fireLaserGatling. */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LASER_GATLING = reg("weapon.fire.lasergatling");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SWITCHMODE_1 = reg("weapon.switchmode1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SWITCHMODE_2 = reg("weapon.switchmode2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SILENCER_SHOOT = reg("weapon.fire.silencer_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_BOLT_OPEN = reg("weapon.reload.bolt_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_BOLT_CLOSE = reg("weapon.reload.bolt_close");
    /** Wenn die Waffe angeschlagen oder ein Magazin fallen gelassen wird. */
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_IMPACT = reg("weapon.reload.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SPAS_FIRE = reg("weapon.shotgun_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_WHACK = reg("weapon.foley.gun_whack");
    /** Der Schlag des Hammers -- im Original NTMSounds.GAVEL = "hbm:weapon.whack". */
    public static final DeferredHolder<SoundEvent, SoundEvent> WEAPON_WHACK = reg("weapon.whack");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLINK_SHELL = reg("weapon.casing.shell");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLINK_SMALL = reg("weapon.casing.small");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLINK_MEDIUM = reg("weapon.casing.medium");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLINK_LARGE = reg("weapon.casing.large");
    public static final DeferredHolder<SoundEvent, SoundEvent> RICOCHET = reg("weapon.ricochet"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_TAKEOFF = reg("weapon.missile_takeoff"); // PLAYERS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> MUKE_EXPLOSION = reg("weapon.muke_explosion"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBIN_EXPLOSION = reg("weapon.robin_explosion"); // PLAYERS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEAR_EXPLOSION = reg("weapon.nuclear_explosion"); // BLOCKS CATEGORY, STREAM
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_LARGE_NEAR = reg("weapon.explosion_large_near"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_LARGE_FAR =  reg("weapon.explosion_large_far"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_SMALL_NEAR = reg("weapon.explosion_small_near"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_SMALL_FAR =  reg("weapon.explosion_small_far"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_TINY = reg("weapon.explosion_tiny"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> FSTBMB_START = reg("weapon.fstbmb_start"); // PLAYERS CATEGORY???
    public static final DeferredHolder<SoundEvent, SoundEvent> FSTBMB_PING = reg("weapon.fstbmb_ping"); // PLAYERS CATEGORY???
    // FIRE WEAPONS
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_DISINTEGRATION = reg("weapon.fire.disintegration"); // PLAYERS CATEGORY
    // ENTITIES
    public static final DeferredHolder<SoundEvent, SoundEvent> OLD_EXPLOSION = reg("entity.old_explosion"); // AMBIENT CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> SOYUZ_TAKE_OFF = reg("entity.soyuz_take_off"); // PLAYER CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_WHISTLE = reg("entity.bomb_whistle"); // PLAYER CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMBER_LOOP = reg("entity.bomber_loop"); // HOSTILE CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMBER_SMALL_LOOP = reg("entity.bomber_small_loop"); // HOSTILE CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANE_CRASH = reg("entity.plane_crash"); // HOSTILE CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANE_SHOT_DOWN = reg("entity.plane_shot_down"); // HOSTILE CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCC = reg("entity.ducc"); // NEUTRAL CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> SLICER = reg("entity.slicer"); // NEUTRAL CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> METEORITE_FALLING_LOOP = reg("entity.meteorite_falling_loop"); // BLOCKS CATEGORY???
    // PLAYERS
    public static final DeferredHolder<SoundEvent, SoundEvent> VOMIT = reg("player.vomit"); // PLAYERS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> COUGH = reg("player.cough"); // PLAYERS CATEGORY
    // BLOCKS
    public static final DeferredHolder<SoundEvent, SoundEvent> PRESS_OPERATE = reg("block.press_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> STEAM_ENGINE_OPERATE = reg("block.steam_engine_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> WARN_OVERSPEED = reg("block.warn_overspeed"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> BOLTGUN = reg("tool.boltgun"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> SONAR_PING = reg("block.sonar_ping"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_PLACED = reg("block.pipe_placed"); // BLOCKS CATEGORY
    // Geraeusche des Reaktorzweigs. Sie gehoeren zu Bauteilen, die noch nicht portiert sind --
    // Hebeltafel, Fusionsanlage, grosse Turbine, Reaktorgeigerzaehler --, stehen aber hier, weil
    // mehrere Teilsysteme sie gleichzeitig brauchen.
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVER_START = reg("block.lever_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVER_STOP = reg("block.lever_stop");
    /** Der grosse Hebel des Baggers, Runde 131. Im Original heisst er block.leverLarge. */
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVER_LARGE = reg("block.lever_large");
    /** Die Raketenmontage, Runde 132: das Arbeitsgeraeusch und der Ton beim Fertigstellen. */
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_ASSEMBLY = reg("block.missile_assembly");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_ASSEMBLY_DONE = reg("block.missile_assembly_done");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARK = reg("block.spark");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LARGE_LOOP = reg("block.large_turbine_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEL_LOOP = reg("block.fel");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_REACTOR_LOOP = reg("block.fusion_reactor_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_GEIGER_LOOP = reg("block.reactor_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> RBMK_EXPLOSION = reg("block.rbmk_explosion");
    public static final DeferredHolder<SoundEvent, SoundEvent> RBMK_AZ5_COVER = reg("block.rbmk_az5_cover");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LEVER = reg("block.chungus_lever");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOBBLE = reg("block.bobble"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> FENSU_HUM = reg("block.fensu_hum"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_HUM = reg("block.electric_hum"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> DEBRIS = reg("block.debris"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> LOCK_OPEN = reg("block.lock_open"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> SOYUZ_READY = reg("block.soyuz_ready"); // BLOCKS CATEGORY, STREAM
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATE_CLOSE = reg("crate_close"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATE_OPEN = reg("crate_open"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATE_BREAK = reg("crate_break"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUEAKY_TOY = reg("block.squeaky_toy"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> HUNDUNS_MAGNIFICENT_HOWL = reg("block.hunduns_magnificent_howl"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_MOTOR_LOOP = reg("block.motor"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> BOILER = reg("block.boiler"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LEVI_LOOP = reg("block.chungus_turbine_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOILER_GROAN = reg("block.boilergroan"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_STRIKE = reg("block.assembler_strike"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_CUT = reg("block.assembler_cut"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_START = reg("block.assembler_start"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_STOP = reg("block.assembler_stop"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEMICAL_PLANT_OPERATE = reg("block.chemical_plant_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> PYRO_OVEN_OPERATE = reg("block.pyro_oven_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> CENTRIFUGE_OPERATE = reg("block.centrifuge_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> IGENERATOR_OPERATE = reg("block.igenerator_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> GAS_TURBINE_RUNNING = reg("block.gas_turbine_running"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> GAS_TURBINE_STARTUP = reg("block.gas_turbine_startup"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> GAS_TURBINE_SHUTDOWN = reg("block.gas_turbine_shutdown"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBOFAN_LOOP = reg("block.turbofan_operate"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBOFAN_DAMAGE = reg("block.damage"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ENGINE_LOOP = reg("block.engine"); // BLOCKS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> METAL_IMPACT = reg("block.metal_impact"); // BLOCKS CATEGORY
    // DOORS
    public static final DeferredHolder<SoundEvent, SoundEvent> TRANSITION_SEAL_OPEN = reg("door.transition_seal_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM6 = reg("door.alarm6");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDING_DOOR_SHUT = reg("door.sliding_door_shut");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDING_DOOR_OPENED = reg("door.sliding_door_opened");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDING_DOOR_OPENING = reg("door.sliding_door_opening");
    public static final DeferredHolder<SoundEvent, SoundEvent> GARAGE_MOVE = reg("door.garage_move");
    public static final DeferredHolder<SoundEvent, SoundEvent> GARAGE_STOP = reg("door.garage_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVER = reg("door.lever");
    public static final DeferredHolder<SoundEvent, SoundEvent> WGH_START = reg("door.wgh_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> WGH_STOP = reg("door.wgh_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WGH_BIG_START = reg("door.wgh_big_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> WGH_BIG_STOP = reg("door.wgh_big_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> QE_SLIDING_SHUT = reg("door.qe_sliding_shut");
    public static final DeferredHolder<SoundEvent, SoundEvent> QE_SLIDING_OPENED = reg("door.qe_sliding_opened");
    public static final DeferredHolder<SoundEvent, SoundEvent> QE_SLIDING_OPENING = reg("door.qe_sliding_opening");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDING_SEAL_OPEN = reg("door.sliding_seal_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDING_SEAL_STOP = reg("door.sliding_seal_stop");
    // ITEMS
    public static final DeferredHolder<SoundEvent, SoundEvent> TECH_BLEEP = reg("item.tech_bleep"); // blee-boo-bee-boop
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_SENTRY_FIRE = reg("turret.sentry_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_SENTRY_LOCKON = reg("turret.sentry_lockon");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_JEREMY_FIRE = reg("turret.jeremy_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_JEREMY_RELOAD = reg("turret.jeremy_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_HOWARD_FIRE = reg("turret.howard_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_HOWARD_RELOAD = reg("turret.howard_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURRET_CHEKHOV_FIRE = reg("turret.chekhov_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> TECH_BOOP = reg("item.tech_boop"); // boop
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER1 = reg("item.geiger1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER2 = reg("item.geiger2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER3 = reg("item.geiger3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER4 = reg("item.geiger4");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER5 = reg("item.geiger5");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER6 = reg("item.geiger6");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIN_UNLOCK = reg("item.pin_unlock");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIN_BREAK = reg("item.pin_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> UNPACK = reg("item.unpack");
    public static final DeferredHolder<SoundEvent, SoundEvent> UPGRADE_PLUG = reg("item.upgrade_plug"); // plok
    public static final DeferredHolder<SoundEvent, SoundEvent> FILTER_SCREW = reg("item.gasmask_screw");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUIT_BATTERY = reg("item.battery");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE = reg("item.syringe");
    public static final DeferredHolder<SoundEvent, SoundEvent> RADAWAY = reg("item.radaway");
    // ALARMS
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_HATCH = reg("alarm.hatch"); // RECORDS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_SOYUZED = reg("alarm.soyuzed"); // RECORDS CATEGORY
    public static final DeferredHolder<SoundEvent, SoundEvent> CHIME = reg("alarm.chime"); // RECORDS CATEGORY
    /* Die Tonspuren der Kassetten, Runde 127.
     *
     * ACHTUNG, HIER STAND EIN ABSTURZ: die Namen waren zunaechst die des Originals, also
     * camelCase -- "alarm.airRaid". Ein Pfad einer ResourceLocation darf aber nur [a-z0-9/._-]
     * enthalten, und Minecraft wirft beim Registrieren eine ResourceLocationException. Das riss
     * den Mod beim Start mit, sobald er ueberhaupt bis hierher kam. Namen, Schluessel in der
     * sounds.json und die Tondateien selbst heissen jetzt durchgehend snake_case, wie die
     * uebrigen 163 Tonereignisse auch.
     *
     * Wo der Ereignisname vom Dateinamen abweicht, steht das weiterhin so: alarm.hatch liegt
     * als lpfhaiwg, alarm.autopilot als boeing707_autopilot_disconnected, alarm.classic als
     * classic_siren. */
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_AIR_RAID = reg("alarm.air_raid");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_AMS_SIREN = reg("alarm.ams_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_APC_LOOP = reg("alarm.apc_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_APC_PASS = reg("alarm.apc_pass");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_AUTOPILOT = reg("alarm.autopilot");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_BANK = reg("alarm.bank_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_BEEP_SIREN = reg("alarm.beep_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_BLAST_DOOR = reg("alarm.blast_door_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_CLASSIC = reg("alarm.classic");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_CONTAINER = reg("alarm.container_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_EAS = reg("alarm.eas_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_FO_KLAXON_A = reg("alarm.fo_klaxon_a");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_FO_KLAXON_B = reg("alarm.fo_klaxon_b");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_KLAXON = reg("alarm.klaxon");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_NOSTROMO = reg("alarm.nostromo_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_RAZORTRAIN = reg("alarm.razortrain_horn");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_REGULAR_SIREN = reg("alarm.regular_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_STRIDER_SIREN = reg("alarm.strider_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_SWEEP_SIREN = reg("alarm.sweep_siren");

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(NuclearTechMod.withDefaultNamespace(name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
