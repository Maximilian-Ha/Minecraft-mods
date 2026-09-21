package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.util.DamageResistanceHandler.DamageClass;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.handler.guncfg.GunNPCFactory, der Maskenmann-Teil.
 *
 * Die Munition, die kein Spieler abfeuert. Das Original legt sie im alten Geschosssystem an
 * (BulletConfiguration, EntityBulletBaseNT) und meldet sie in einer Zahlentabelle an
 * (BulletConfigSyncingUtil); der Port kennt nur das neue -- BulletConfig und BulletBaseMK4 --
 * und braucht die Tabelle nicht, weil die Geschosse ihre Einstellung selbst mitbringen.
 *
 * VIER GESCHOSSE, DREI ANGRIFFE. Die Kugel ist der Kern des Fernangriffs: sie fliegt langsam,
 * haelt eine Minute und schickt alle zehn Ticks einen Bolzen auf JEDEN Spieler im Umkreis von
 * fuenfzig Bloecken. Die Rakete ist ein Wurfgeschoss mit Sprengwirkung, der Leuchtspur folgt
 * beim Aufschlag ein Meteor aus dreissig Bloecken Hoehe.
 *
 * DER MUNITIONSGEGENSTAND IST DIE MUENZE. Das Original setzt bei allen vier
 * ammo = coin_maskman -- nicht weil die Muenze verschossen wuerde, sondern weil das alte
 * System ohne Munitionsangabe nicht auskam. Der Port behaelt sie bei: das Feld dient dort nur
 * noch dem Nachschlagen, und eine falsche Angabe waere eine Aenderung ohne Anlass.
 */
public class XFactoryNPC {

    public static BulletConfig maskman_orb;
    public static BulletConfig maskman_bolt;
    public static BulletConfig maskman_rocket;
    public static BulletConfig maskman_tracer;
    public static BulletConfig maskman_meteor;

    public static BulletConfig worm_laser;
    public static BulletConfig worm_bolt;

    public static BulletConfig ufo_rocket;

    /** Wie weit die Kugel nach Spielern sucht, in Bloecken. */
    private static final double KUGEL_REICHWEITE = 50D;

    public static void init() {

        maskman_orb = new BulletConfig().setItem(NtmItems.COIN_MASKMAN).setVel(0.25F).setSpread(0F)
                .setDamage(100F).setGrav(0D).setLife(60).setRicochetCount(0)
                .setOnUpdate(XFactoryNPC::kugelTick)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 1.5F); geschoss.discard(); });

        maskman_bolt = new BulletConfig().setItem(NtmItems.COIN_MASKMAN).setSpread(0F)
                .setDamage(20F).setupDamageClass(DamageClass.LASER).setRicochetCount(0)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 0.5F); geschoss.discard(); });

        maskman_rocket = new BulletConfig().setItem(NtmItems.COIN_MASKMAN).setVel(1F).setGrav(0.1D)
                .setDamage(20F)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 5F); geschoss.discard(); });

        maskman_tracer = new BulletConfig().setItem(NtmItems.COIN_MASKMAN).setSpread(0F)
                .setDamage(20F).setupDamageClass(DamageClass.LASER).setRicochetCount(0)
                .setOnImpact((geschoss, treffer) -> { meteorWerfen(geschoss); geschoss.discard(); });

        maskman_meteor = new BulletConfig().setItem(NtmItems.COIN_MASKMAN).setVel(1F).setGrav(0.1D)
                .setDamage(30F)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 2.5F); geschoss.discard(); });

        /*
         * DIE BEIDEN DES WURMS. Der Kopf schiesst den staerkeren (getWormHeadBolt: 35 bis 60,
         * hundert Ticks Flugzeit), die Glieder den schwaecheren (getWormBolt: 15 bis 25,
         * sechzig Ticks). Beide prallen nicht ab und streuen nicht von sich aus -- die
         * Streuung kommt vom Schuetzen.
         */
        worm_laser = new BulletConfig().setItem(NtmItems.COIN_WORM).setSpread(0F).setLife(100)
                .setDamage(60F).setupDamageClass(DamageClass.LASER).setRicochetCount(0).setGrav(0D);

        worm_bolt = new BulletConfig().setItem(NtmItems.COIN_WORM).setSpread(0F).setLife(60)
                .setDamage(25F).setupDamageClass(DamageClass.LASER).setRicochetCount(0).setGrav(0D);

        /*
         * DIE RAKETE DES UFOS. Das Original nimmt die gewoehnliche Rakete und nimmt ihr
         * zweierlei: den Blockschaden und die Sprengwirkung (destroysBlocks = false,
         * explosive = 0F). Was bleibt, ist der Aufschlagschaden -- und die Lenkung, die das
         * UFO ihr beim Abschuss mitgibt.
         */
        ufo_rocket = new BulletConfig().setItem(NtmItems.COIN_UFO).setVel(2F).setGrav(0D).setLife(200)
                .setDamage(30F).setRicochetCount(0);
    }

    /**
     * Die Kugel im Flug: alle zehn Ticks ein Bolzen auf jeden Spieler im Umkreis. Das Original
     * zaehlt dafuer ticksExisted % 10 != 5 herunter -- also der fuenfte Tick jedes Zehnerblocks,
     * nicht der erste.
     */
    private static void kugelTick(Entity entity) {

        if(entity.level().isClientSide) return;
        if(!(entity instanceof BulletBaseMK4 geschoss)) return;
        if(geschoss.tickCount % 10 != 5) return;

        Vec3 ort = geschoss.position();
        List<Player> spieler = geschoss.level().getEntitiesOfClass(Player.class,
                new AABB(ort, ort).inflate(KUGEL_REICHWEITE));

        for(Player ziel : spieler) {

            Vec3 richtung = new Vec3(
                    ziel.getX() - ort.x,
                    ziel.getEyeY() - ort.y,
                    ziel.getZ() - ort.z).normalize();

            LivingEntity schuetze = geschoss.getOwner() instanceof LivingEntity lebend ? lebend : null;
            BulletBaseMK4 bolzen = new BulletBaseMK4(geschoss.level(), schuetze, maskman_bolt,
                    maskman_bolt.damageMult, 0.05F, ort, richtung.scale(0.5D));
            geschoss.level().addFreshEntity(bolzen);
        }
    }

    /**
     * Der Meteor der Leuchtspur: er entsteht dreissig bis vierzig Bloecke ueber der
     * Aufschlagstelle und faellt senkrecht herab.
     */
    private static void meteorWerfen(BulletBaseMK4 geschoss) {

        if(geschoss.level().isClientSide) return;

        LivingEntity schuetze = geschoss.getOwner() instanceof LivingEntity lebend ? lebend : null;
        Vec3 oben = new Vec3(geschoss.getX(), geschoss.getY() + 30 + geschoss.level().random.nextInt(10), geschoss.getZ());

        BulletBaseMK4 meteor = new BulletBaseMK4(geschoss.level(), schuetze, maskman_meteor,
                maskman_meteor.damageMult, 0F, oben, new Vec3(0, -1D, 0));
        geschoss.level().addFreshEntity(meteor);
    }
}
