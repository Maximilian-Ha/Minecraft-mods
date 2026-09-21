package com.hbm.datagen;

import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class NtmSoundDefinitionsProvider extends SoundDefinitionsProvider {

    protected NtmSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, NuclearTechMod.MODID, helper);
    }

    @Override
    public void registerSounds() {

        // WEAPONS
        this.add(NtmSoundEvents.GUN_REVOLVER_COCK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/revolver_cock")))
        );
        this.add(NtmSoundEvents.GUN_REVOLVER_CLOSE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/revolver_close")))
        );
        this.add(NtmSoundEvents.GUN_REVOLVER_SPIN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/revolver_spin")))
        );
        this.add(NtmSoundEvents.GUN_PISTOL_COCK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/pistol_cock")))
        );
        this.add(NtmSoundEvents.GUN_MAG_SMALL_REMOVE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/mag_small_remove")))
        );
        this.add(NtmSoundEvents.GUN_MAG_SMALL_INSERT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/mag_small_insert")))
        );
        this.add(NtmSoundEvents.GUN_MAG_REMOVE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/mag_remove")))
        );
        this.add(NtmSoundEvents.GUN_MAG_INSERT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/mag_insert")))
        );
        this.add(NtmSoundEvents.GUN_LATCH_OPEN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/latch_open")))
        );
        this.add(NtmSoundEvents.GUN_LEVER_COCK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/lever_cock")))
        );
        this.add(NtmSoundEvents.GUN_SHOTGUN_LOAD, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/reload/shotgun_reload1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/reload/shotgun_reload2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/reload/shotgun_reload3"))
                )
        );
        this.add(NtmSoundEvents.BLOCK_BROADCAST_1, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/broadcast1")).stream())
        );
        this.add(NtmSoundEvents.BLOCK_BROADCAST_2, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/broadcast2")).stream())
        );
        this.add(NtmSoundEvents.BLOCK_BROADCAST_3, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/broadcast3")).stream())
        );

        this.add(NtmSoundEvents.ENTITY_CYBERCRAB, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random1")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random2")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random3")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random4")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random5")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random6")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random7")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random8")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random9")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random10")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random11")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random12")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random13")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random14")),
                        sound(NuclearTechMod.withDefaultNamespace("entity/radio_random15"))
                )
        );
        this.add(NtmSoundEvents.WEAPON_SAW_SHOOT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/saw_shoot")))
        );
        this.add(NtmSoundEvents.WEAPON_TESLA, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/tesla1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/tesla2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/tesla3")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/tesla4"))
                )
        );
        this.add(NtmSoundEvents.GUN_SHOTGUN_OPEN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/shotgun_cock_open")))
        );
        this.add(NtmSoundEvents.GUN_SHOTGUN_CLOSE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/shotgun_cock_close")))
        );
        this.add(NtmSoundEvents.GUN_SHOTGUN_COCK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/shotgun_cock")))
        );
        this.add(NtmSoundEvents.GUN_DRY_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/dry_fire_click")))
        );
        this.add(NtmSoundEvents.GUN_HEAVY_REVOLVER_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/44_shoot")))
        );
        this.add(NtmSoundEvents.GUN_SHOTGUN_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/shotgun")))
        );
        this.add(NtmSoundEvents.GUN_LIBERATOR_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/shotgun_alt")))
        );
        this.add(NtmSoundEvents.GUN_GREASEGUN_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/greasegun")))
        );
        this.add(NtmSoundEvents.GUN_POWDER_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/black_powder")))
        );
        this.add(NtmSoundEvents.GUN_FLAMER_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/flameloop")))
        );
        this.add(NtmSoundEvents.GUN_TESLA_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/tesla")))
        );
        this.add(NtmSoundEvents.GUN_FOLLY_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/loudestnoiseonearth")))
        );
        this.add(NtmSoundEvents.GUN_CHARGE_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/grenade")))
        );
        this.add(NtmSoundEvents.GUN_SCREW, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/screw")))
        );
        this.add(NtmSoundEvents.GUN_ROCKET_INSERT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/insertrocket")))
        );
        this.add(NtmSoundEvents.GUN_FATMAN_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/fatman")))
        );
        this.add(NtmSoundEvents.GUN_FATMAN_RELOAD, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/fatmanfull")))
        );
        this.add(NtmSoundEvents.UFO_BLAST, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("entity/ufoblast")))
        );
        this.add(NtmSoundEvents.GUN_LASER_PISTOL_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/laserpistol")))
        );
        this.add(NtmSoundEvents.GUN_LASER_RIFLE_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/laser")))
        );
        this.add(NtmSoundEvents.GUN_VALVE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/pressurevalve")))
        );
        this.add(NtmSoundEvents.GUN_EXTINGUISHER_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/extinguisher")))
        );
        this.add(NtmSoundEvents.GUN_SMACK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/smack")))
        );
        this.add(NtmSoundEvents.WEAPON_IMMOLATOR_SHOOT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/immolator_shoot")))
        );
        this.add(NtmSoundEvents.GUN_PISTOL_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/pistol")))
        );
        this.add(NtmSoundEvents.TURRET_CHEKHOV_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/chekhov_fire")))
        );
        this.add(NtmSoundEvents.TURRET_JEREMY_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/jeremy_fire1")))
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/jeremy_fire2")))
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/jeremy_fire3")))
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/jeremy_fire4")))
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/jeremy_fire5")))
        );
        this.add(NtmSoundEvents.TURRET_JEREMY_RELOAD, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/jeremy_reload")))
        );
        this.add(NtmSoundEvents.TURRET_HOWARD_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/howard_fire")))
        );
        this.add(NtmSoundEvents.TURRET_HOWARD_RELOAD, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/howard_reload")))
        );
        this.add(NtmSoundEvents.TURRET_SENTRY_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/sentry_fire")))
        );
        this.add(NtmSoundEvents.TURRET_SENTRY_LOCKON, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("turret/sentry_lockon")))
        );
        this.add(NtmSoundEvents.GUN_AMAT_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/amat")))
        );
        this.add(NtmSoundEvents.GUN_ASSAULT_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/assault")))
        );
        this.add(NtmSoundEvents.GUN_RIFLE_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/rifle")))
        );
        this.add(NtmSoundEvents.GUN_COIL_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/coilgun")))
        );
        this.add(NtmSoundEvents.GUN_COIL_RELOAD, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/coilgun")))
        );
        this.add(NtmSoundEvents.GUN_HEAVY_RIFLE_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/rifle_heavy")))
        );
        this.add(NtmSoundEvents.GUN_RIFLE_COCK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/rifle_cock")))
        );
        this.add(NtmSoundEvents.GUN_STAB, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/fire/stab1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/fire/stab2"))
                )
        );
        this.add(NtmSoundEvents.GUN_MINIGUN_FIRE, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/cal1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/cal2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/cal3"))
                )
        );
        this.add(NtmSoundEvents.GUN_ABERRATOR_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/aberrator")))
        );
        this.add(NtmSoundEvents.GUN_UNDERBARREL_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/hk_shoot")))
        );
        this.add(NtmSoundEvents.GUN_CONGO_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/gl_shoot")))
        );
        this.add(NtmSoundEvents.GUN_GRENADE_RELOAD, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/gl_reload")))
        );
        this.add(NtmSoundEvents.GUN_GRENADE_OPEN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/gl_open")))
        );
        this.add(NtmSoundEvents.GUN_GRENADE_CLOSE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/gl_close")))
        );
        this.add(NtmSoundEvents.GUN_CANISTER_INSERT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/insert_canister")))
        );
        this.add(NtmSoundEvents.GUN_MK108_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/mk108")))
        );
        this.add(NtmSoundEvents.GUN_ROCKET_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/rpg_shoot")))
        );
        this.add(NtmSoundEvents.GUN_LOCKON, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/lockon")))
        );
        this.add(NtmSoundEvents.TRAIN_IMPACT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/train_impact")))
        );
        this.add(NtmSoundEvents.TRAIN_HORN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("alarm/train_horn")))
        );
        this.add(NtmSoundEvents.GAMBIT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("alarm/gambit")))
        );
        this.add(NtmSoundEvents.GUN_BOAT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/boat")))
        );
        this.add(NtmSoundEvents.GUN_SHREDDER_CYCLE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/shreddercycle")))
        );
        this.add(NtmSoundEvents.GUN_SHREDDER_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/shotgunauto")))
        );
        this.add(NtmSoundEvents.GUN_LASER_GATLING, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/lasergatling")))
        );
        this.add(NtmSoundEvents.GUN_TAU_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/tau")))
        );
        this.add(NtmSoundEvents.GUN_TAU_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/tauloop")))
        );
        this.add(NtmSoundEvents.GUN_TAU_RELEASE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/taurelease1")))
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/taurelease2")))
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/taurelease3")))
        );
        /* Drei Aufnahmen, wie im Original -- welche kommt, entscheidet das Spiel. */
        this.add(NtmSoundEvents.GRENADE_BOUNCE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/grenade_bounce1")))
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/grenade_bounce2")))
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/grenade_bounce3")))
        );
        this.add(NtmSoundEvents.GUN_SWITCHMODE_1, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/switchmode1")))
        );
        this.add(NtmSoundEvents.GUN_SWITCHMODE_2, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/switchmode2")))
        );
        this.add(NtmSoundEvents.GUN_SILENCER_SHOOT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/silencer_shoot")))
        );
        this.add(NtmSoundEvents.GUN_BOLT_OPEN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/bolt_open")))
        );
        this.add(NtmSoundEvents.GUN_BOLT_CLOSE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/reload/bolt_close")))
        );
        this.add(NtmSoundEvents.GUN_UZI_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/uzi")))
        );
        this.add(NtmSoundEvents.GUN_STARF_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/pistol_light")))
        );
        this.add(NtmSoundEvents.GUN_SILENCED_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/fire/silenced")))
        );
        this.add(NtmSoundEvents.GUN_IMPACT, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/reload/impact1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/reload/impact2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/reload/impact3"))
                )
        );
        this.add(NtmSoundEvents.GUN_SPAS_FIRE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/shotgun_shoot")))
        );
        this.add(NtmSoundEvents.GUN_WHACK, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/foley/gun_whack1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/foley/gun_whack2"))
                )
        );
        this.add(NtmSoundEvents.WEAPON_WHACK, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("weapon/whack")))
        );
        this.add(NtmSoundEvents.PLINK_SHELL, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/shell1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/shell2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/shell3"))
                )
        );
        this.add(NtmSoundEvents.PLINK_SMALL, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/small1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/small2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/small3"))
                )
        );
        this.add(NtmSoundEvents.PLINK_MEDIUM, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/medium1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/medium2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/medium3"))
                )
        );
        this.add(NtmSoundEvents.PLINK_LARGE, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/large1")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/large2")),
                        sound(NuclearTechMod.withDefaultNamespace("weapon/casing/large3"))
                )
        );
        this.add(NtmSoundEvents.RICOCHET, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:weapon/ric1"),
                        sound("hbmsntm:weapon/ric2"),
                        sound("hbmsntm:weapon/ric3"),
                        sound("hbmsntm:weapon/ric4"),
                        sound("hbmsntm:weapon/ric5")
                )
        );
        this.add(NtmSoundEvents.MISSILE_TAKEOFF, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/missile_takeoff"))
        );
        this.add(NtmSoundEvents.MUKE_EXPLOSION, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/muke_explosion"))
        );
        this.add(NtmSoundEvents.ROBIN_EXPLOSION, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/robin_explosion"))
        );
        this.add(NtmSoundEvents.NUCLEAR_EXPLOSION, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/nuclear_explosion").stream())
        );
        this.add(NtmSoundEvents.EXPLOSION_LARGE_NEAR, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/explosion_large_near"))
        );
        this.add(NtmSoundEvents.EXPLOSION_LARGE_FAR, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/explosion_large_far"))
        );
        this.add(NtmSoundEvents.EXPLOSION_SMALL_NEAR, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:weapon/explosion_small_near1"),
                        sound("hbmsntm:weapon/explosion_small_near2"),
                        sound("hbmsntm:weapon/explosion_small_near3")
                )
        );
        this.add(NtmSoundEvents.EXPLOSION_SMALL_FAR, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:weapon/explosion_small_far1"),
                        sound("hbmsntm:weapon/explosion_small_far2")
                )
        );
        this.add(NtmSoundEvents.EXPLOSION_TINY, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:weapon/explosion_tiny1"),
                        sound("hbmsntm:weapon/explosion_tiny2")
                )
        );
        this.add(NtmSoundEvents.FSTBMB_START, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/fstbmb_start"))
        );
        this.add(NtmSoundEvents.FSTBMB_PING, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/fstbmb_ping"))
        );
        // FIRE WEAPONS
        this.add(NtmSoundEvents.FIRE_DISINTEGRATION, SoundDefinition.definition()
                .with(sound("hbmsntm:weapon/fire/disintegration"))
        );
        // ENTITIES
        this.add(NtmSoundEvents.OLD_EXPLOSION, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/old_explosion"))
        );
        this.add(NtmSoundEvents.SOYUZ_TAKE_OFF, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/soyuz_take_off"))
        );
        this.add(NtmSoundEvents.BOMB_WHISTLE, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/bomb_whistle"))
        );
        this.add(NtmSoundEvents.BOMBER_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/bomber_loop"))
        );
        this.add(NtmSoundEvents.BOMBER_SMALL_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/bomber_small_loop"))
        );
        this.add(NtmSoundEvents.PLANE_CRASH, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/plane_crash"))
        );
        this.add(NtmSoundEvents.PLANE_SHOT_DOWN, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/plane_shot_down"))
        );
        this.add(NtmSoundEvents.DUCC, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:entity/ducc1"),
                        sound("hbmsntm:entity/ducc2")
                )
        );
        this.add(NtmSoundEvents.SLICER, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:entity/slicer1"),
                        sound("hbmsntm:entity/slicer2"),
                        sound("hbmsntm:entity/slicer3"),
                        sound("hbmsntm:entity/slicer4")
                )
        );
        this.add(NtmSoundEvents.METEORITE_FALLING_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:entity/meteorite_falling_loop"))
        );
        // PLAYERS
        this.add(NtmSoundEvents.VOMIT, SoundDefinition.definition()
                .with(sound("hbmsntm:player/vomit"))
        );
        this.add(NtmSoundEvents.COUGH, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:player/cough1"),
                        sound("hbmsntm:player/cough2"),
                        sound("hbmsntm:player/cough3"),
                        sound("hbmsntm:player/cough4")
                )
        );
        // BLOCKS
        this.add(NtmSoundEvents.PRESS_OPERATE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/press_operate")))
        );
        this.add(NtmSoundEvents.SONAR_PING, SoundDefinition.definition()
                .with(sound("hbmsntm:block/sonar_ping"))
        );
        this.add(NtmSoundEvents.PIPE_PLACED, SoundDefinition.definition()
                .with(sound("hbmsntm:block/pipe_placed"))
        );
        // REAKTORZWEIG
        this.add(NtmSoundEvents.LEVER_START, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/lever_start")))
        );
        this.add(NtmSoundEvents.LEVER_STOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/lever_stop")))
        );
        this.add(NtmSoundEvents.LEVER_LARGE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/lever_large")))
        );
        this.add(NtmSoundEvents.MISSILE_ASSEMBLY, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/missile_assembly")))
        );
        this.add(NtmSoundEvents.MISSILE_ASSEMBLY_DONE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/missile_assembly_done")))
        );
        this.add(NtmSoundEvents.SPARK, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("block/spark1")),
                        sound(NuclearTechMod.withDefaultNamespace("block/spark2")),
                        sound(NuclearTechMod.withDefaultNamespace("block/spark3")),
                        sound(NuclearTechMod.withDefaultNamespace("block/spark4")),
                        sound(NuclearTechMod.withDefaultNamespace("block/spark5")),
                        sound(NuclearTechMod.withDefaultNamespace("block/spark6"))
                )
        );
        this.add(NtmSoundEvents.TURBINE_LARGE_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/large_turbine")))
        );
        this.add(NtmSoundEvents.FEL_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/fel")))
        );
        this.add(NtmSoundEvents.FUSION_REACTOR_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/fusion_reactor_spin")))
        );
        this.add(NtmSoundEvents.REACTOR_GEIGER_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/reactor_loop")))
        );
        this.add(NtmSoundEvents.RBMK_EXPLOSION, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/rbmk_explosion")))
        );
        this.add(NtmSoundEvents.RBMK_AZ5_COVER, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/rbmk_az5_cover")))
        );
        this.add(NtmSoundEvents.TURBINE_LEVER, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/chungus_lever")))
        );
        this.add(NtmSoundEvents.BOBBLE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/bobble"))
        );
        this.add(NtmSoundEvents.FENSU_HUM, SoundDefinition.definition()
                .with(sound("hbmsntm:block/fensu_hum"))
        );
        this.add(NtmSoundEvents.DEBRIS, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:block/debris1"),
                        sound("hbmsntm:block/debris2"),
                        sound("hbmsntm:block/debris3")
                )
        );
        this.add(NtmSoundEvents.LOCK_OPEN, SoundDefinition.definition()
                .with(sound("hbmsntm:block/lock_open"))
        );
        this.add(NtmSoundEvents.SOYUZ_READY, SoundDefinition.definition()
                .with(sound("hbmsntm:block/soyuz_ready"))
        );
        this.add(NtmSoundEvents.CRATE_CLOSE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/crate_close"))
        );
        this.add(NtmSoundEvents.CRATE_OPEN, SoundDefinition.definition()
                .with(sound("hbmsntm:block/crate_open"))
        );
        this.add(NtmSoundEvents.CRATE_BREAK, SoundDefinition.definition()
                .with(sound("hbmsntm:block/crate_break1"))
                .with(sound("hbmsntm:block/crate_break2"))
                .with(sound("hbmsntm:block/crate_break3"))
                .with(sound("hbmsntm:block/crate_break4"))
                .with(sound("hbmsntm:block/crate_break5"))
        );
        this.add(NtmSoundEvents.SQUEAKY_TOY, SoundDefinition.definition()
                .with(sound("hbmsntm:block/squeaky_toy"))
        );
        this.add(NtmSoundEvents.HUNDUNS_MAGNIFICENT_HOWL, SoundDefinition.definition()
                .with(sound("hbmsntm:block/hunduns_magnificent_howl"))
        );
        this.add(NtmSoundEvents.ELECTRIC_MOTOR_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:block/motor"))
        );
        this.add(NtmSoundEvents.BOILER, SoundDefinition.definition()
                .with(sound("hbmsntm:block/boiler"))
        );
        this.add(NtmSoundEvents.STEAM_ENGINE_OPERATE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/steam_engine_operate"))
        );
        this.add(NtmSoundEvents.WARN_OVERSPEED, SoundDefinition.definition()
                .with(sound("hbmsntm:block/warn_overspeed"))
        );
        this.add(NtmSoundEvents.BOLTGUN, SoundDefinition.definition()
                .with(sound("hbmsntm:tool/boltgun"))
        );
        this.add(NtmSoundEvents.FILTER_SCREW, SoundDefinition.definition()
                .with(sound("hbmsntm:tool/gasmask_screw"))
        );
        this.add(NtmSoundEvents.IGENERATOR_OPERATE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/igenerator_operate"))
        );
        this.add(NtmSoundEvents.GAS_TURBINE_RUNNING, SoundDefinition.definition()
                .with(sound("hbmsntm:block/gas_turbine_running"))
        );
        this.add(NtmSoundEvents.GAS_TURBINE_STARTUP, SoundDefinition.definition()
                .with(sound("hbmsntm:block/gas_turbine_startup"))
        );
        this.add(NtmSoundEvents.GAS_TURBINE_SHUTDOWN, SoundDefinition.definition()
                .with(sound("hbmsntm:block/gas_turbine_shutdown"))
        );
        this.add(NtmSoundEvents.TURBOFAN_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:block/turbofan_operate").stream())
        );
        this.add(NtmSoundEvents.ENGINE_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:block/engine"))
        );
        this.add(NtmSoundEvents.TURBOFAN_DAMAGE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/dam1"))
                .with(sound("hbmsntm:block/dam2"))
                .with(sound("hbmsntm:block/dam3"))
                .with(sound("hbmsntm:block/dam4"))
        );
        this.add(NtmSoundEvents.BOILER_GROAN, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:block/boilergroan0"),
                        sound("hbmsntm:block/boilergroan1"),
                        sound("hbmsntm:block/boilergroan2")
                )
        );
        this.add(NtmSoundEvents.TURBINE_LEVI_LOOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/chungus_turbine")))
        );
        this.add(NtmSoundEvents.ASSEMBLER_STRIKE, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:block/assembler_strike1"),
                        sound("hbmsntm:block/assembler_strike2")
                )
        );
        this.add(NtmSoundEvents.ASSEMBLER_CUT, SoundDefinition.definition()
                .with(sound("hbmsntm:block/assembler_cut"))
        );
        this.add(NtmSoundEvents.ASSEMBLER_START, SoundDefinition.definition()
                .with(sound("hbmsntm:block/assembler_start"))
        );
        this.add(NtmSoundEvents.ASSEMBLER_STOP, SoundDefinition.definition()
                .with(sound("hbmsntm:block/assembler_stop"))
        );
        this.add(NtmSoundEvents.CENTRIFUGE_OPERATE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/centrifugeoperate"))
        );
        this.add(NtmSoundEvents.CHEMICAL_PLANT_OPERATE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/chemicalplant"))
        );
        this.add(NtmSoundEvents.PYRO_OVEN_OPERATE, SoundDefinition.definition()
                .with(sound("hbmsntm:block/pyrooven"))
        );
        this.add(NtmSoundEvents.ELECTRIC_HUM, SoundDefinition.definition()
                .with(sound("hbmsntm:block/electric_hum"))
        );
        this.add(NtmSoundEvents.METAL_IMPACT, SoundDefinition.definition()
                .with(
                        sound(NuclearTechMod.withDefaultNamespace("block/metal_impact1")),
                        sound(NuclearTechMod.withDefaultNamespace("block/metal_impact2"))
                )
        );
        // DOORS
        this.add(NtmSoundEvents.TRANSITION_SEAL_OPEN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/transition_seal_open")))
        );
        this.add(NtmSoundEvents.ALARM6, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/alarm6")))
        );
        this.add(NtmSoundEvents.SLIDING_DOOR_SHUT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/sliding_door_shut")))
        );
        this.add(NtmSoundEvents.SLIDING_DOOR_OPENED, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/sliding_door_opened")))
        );
        this.add(NtmSoundEvents.SLIDING_DOOR_OPENING, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/sliding_door_opening")))
        );
        this.add(NtmSoundEvents.GARAGE_MOVE, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/garage_move")))
        );
        this.add(NtmSoundEvents.GARAGE_STOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/garage_stop")))
        );
        this.add(NtmSoundEvents.LEVER, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/lever")))
        );
        this.add(NtmSoundEvents.WGH_START, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/wgh_start")))
        );
        this.add(NtmSoundEvents.WGH_STOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/wgh_stop")))
        );
        this.add(NtmSoundEvents.WGH_BIG_START, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/wgh_big_start")))
        );
        this.add(NtmSoundEvents.WGH_BIG_STOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/wgh_big_stop")))
        );
        this.add(NtmSoundEvents.QE_SLIDING_SHUT, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/qe_sliding_shut")))
        );
        this.add(NtmSoundEvents.QE_SLIDING_OPENED, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/qe_sliding_opened")))
        );
        this.add(NtmSoundEvents.QE_SLIDING_OPENING, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/qe_sliding_opening")))
        );
        this.add(NtmSoundEvents.SLIDING_SEAL_OPEN, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/sliding_seal_open")))
        );
        this.add(NtmSoundEvents.SLIDING_SEAL_STOP, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("block/door/sliding_seal_stop")))
        );
        // ITEMS
        this.add(NtmSoundEvents.TECH_BLEEP, SoundDefinition.definition()
                .with(sound("hbmsntm:tool/tech_bleep"))
        );
        this.add(NtmSoundEvents.TECH_BOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:tool/tech_boop"))
        );
        /* Die acht Ansagen der Kartoffelbatterie -- im Original liegen sie unter
         * hbm:potatos.random als Gruppe von randresponse0 bis randresponse7. */
        this.add(NtmSoundEvents.POTATOS, SoundDefinition.definition()
                .with(sound("hbmsntm:potatos/randresponse0"))
                .with(sound("hbmsntm:potatos/randresponse1"))
                .with(sound("hbmsntm:potatos/randresponse2"))
                .with(sound("hbmsntm:potatos/randresponse3"))
                .with(sound("hbmsntm:potatos/randresponse4"))
                .with(sound("hbmsntm:potatos/randresponse5"))
                .with(sound("hbmsntm:potatos/randresponse6"))
                .with(sound("hbmsntm:potatos/randresponse7"))
        );
        this.add(NtmSoundEvents.SUIT_BATTERY, SoundDefinition.definition().with(sound("hbmsntm:tool/battery")));
        this.add(NtmSoundEvents.SYRINGE, SoundDefinition.definition().with(sound("hbmsntm:tool/stim")));
        this.add(NtmSoundEvents.WEAPON_BANG, SoundDefinition.definition().with(sound("hbmsntm:weapon/bang")));
        this.add(NtmSoundEvents.WEAPON_SLICE, SoundDefinition.definition().with(sound("hbmsntm:weapon/slice")));
        this.add(NtmSoundEvents.WEAPON_KAPENG, SoundDefinition.definition().with(sound("hbmsntm:weapon/kapeng")));
        this.add(NtmSoundEvents.RADAWAY, SoundDefinition.definition().with(sound("hbmsntm:tool/radaway")));
        this.add(NtmSoundEvents.GEIGER1, SoundDefinition.definition().with(sound("hbmsntm:tool/geiger1")));
        this.add(NtmSoundEvents.GEIGER2, SoundDefinition.definition().with(sound("hbmsntm:tool/geiger2")));
        this.add(NtmSoundEvents.GEIGER3, SoundDefinition.definition().with(sound("hbmsntm:tool/geiger3")));
        this.add(NtmSoundEvents.GEIGER4, SoundDefinition.definition().with(sound("hbmsntm:tool/geiger4")));
        this.add(NtmSoundEvents.GEIGER5, SoundDefinition.definition().with(sound("hbmsntm:tool/geiger5")));
        this.add(NtmSoundEvents.GEIGER6, SoundDefinition.definition().with(sound("hbmsntm:tool/geiger6")));
        this.add(NtmSoundEvents.PIN_UNLOCK, SoundDefinition.definition()
                .with(sound("hbmsntm:tool/pin_unlock"))
        );
        this.add(NtmSoundEvents.PIN_BREAK, SoundDefinition.definition()
                .with(sound("hbmsntm:tool/pin_break"))
        );
        this.add(NtmSoundEvents.UNPACK, SoundDefinition.definition()
                .with(
                        sound("hbmsntm:tool/extract1"),
                        sound("hbmsntm:tool/extract2")
                )
        );
        this.add(NtmSoundEvents.UPGRADE_PLUG, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("tool/upgrade_plug")))
        );
        // ALARMS
        this.add(NtmSoundEvents.ALARM_HATCH, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("alarm/lpfhaiwg")))
        );
        this.add(NtmSoundEvents.ALARM_SOYUZED, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("alarm/soyuzed")))
        );
        this.add(NtmSoundEvents.CHIME, SoundDefinition.definition()
                .with(sound(NuclearTechMod.withDefaultNamespace("alarm/chime")))
        );
        /* Die Sirenen der Runde 127. Sie standen bis hierher nur in der handgeschriebenen
         * sounds.json -- und die lag im selben Pfad, den dieser Erzeuger schreibt. Zwei
         * Dateien auf einem Pfad heisst: eine gewinnt, und die Sirenen waeren beim ersten
         * runData-Lauf verschwunden. Jetzt stehen alle Tonereignisse an einer Stelle. */
        this.add(NtmSoundEvents.ALARM_AIR_RAID, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/air_raid").stream())
        );
        this.add(NtmSoundEvents.ALARM_AMS_SIREN, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/ams_siren"))
        );
        this.add(NtmSoundEvents.ALARM_APC_LOOP, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/apc_loop"))
        );
        this.add(NtmSoundEvents.ALARM_APC_PASS, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/apc_pass"))
        );
        this.add(NtmSoundEvents.ALARM_AUTOPILOT, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/boeing707_autopilot_disconnected"))
        );
        this.add(NtmSoundEvents.ALARM_BANK, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/bank_alarm"))
        );
        this.add(NtmSoundEvents.ALARM_BEEP_SIREN, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/beep_siren"))
        );
        this.add(NtmSoundEvents.ALARM_BLAST_DOOR, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/blast_door_alarm"))
        );
        this.add(NtmSoundEvents.ALARM_CLASSIC, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/classic_siren"))
        );
        this.add(NtmSoundEvents.ALARM_CONTAINER, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/container_alarm"))
        );
        this.add(NtmSoundEvents.ALARM_EAS, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/eas_alarm"))
        );
        this.add(NtmSoundEvents.ALARM_FO_KLAXON_A, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/fo_klaxon_a"))
        );
        this.add(NtmSoundEvents.ALARM_FO_KLAXON_B, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/fo_klaxon_b"))
        );
        this.add(NtmSoundEvents.ALARM_KLAXON, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/klaxon"))
        );
        this.add(NtmSoundEvents.ALARM_NOSTROMO, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/nostromo_siren"))
        );
        this.add(NtmSoundEvents.ALARM_RAZORTRAIN, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/razortrain_horn"))
        );
        this.add(NtmSoundEvents.ALARM_REGULAR_SIREN, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/regular_siren"))
        );
        this.add(NtmSoundEvents.ALARM_STRIDER_SIREN, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/strider_siren"))
        );
        this.add(NtmSoundEvents.ALARM_SWEEP_SIREN, SoundDefinition.definition()
                .with(sound("hbmsntm:alarm/sweep_siren"))
        );
    }
}
