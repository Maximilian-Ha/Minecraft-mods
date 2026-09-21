package com.hbm.entity.mob.botprime;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.weapon.sedna.factory.XFactoryNPC;
import com.hbm.registry.NtmSoundEvents;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.botprime.EntityBOTPrimeBase.
 *
 * Was Kopf und Glieder gemeinsam haben: fuenfzehntausend Lebenspunkte, kein Rueckstoss, kein
 * Feuer- und kein Strahlenschaden -- und der Laser.
 *
 * ZWEI LASER. Der Kopf schiesst fuenf Strahlen auf einmal, jeden mit wachsender Streuung
 * (i mal 0,05); ein Glied schiesst einen einzigen, schwaecheren.
 *
 * ER SIEHT DURCH GLAS UND LAUB. Die Sichtpruefung des Originals
 * (canEntityBeSeenThroughNonSolids) fragt nur nach festen Bloecken -- deshalb feuert der Wurm
 * auch durch ein Fenster.
 */
public abstract class BotPrimeBase extends WormBase implements IRadiationImmune {

    public int angriffsZaehler = 0;

    protected BotPrimeBase(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.reibungLuft = 0.995F;
        this.reibungBoden = 0.98F;
        this.rueckstossTeiler = 1.0D;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15000.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D);
    }

    /** Sichtpruefung, die nur feste Bloecke zaehlen laesst. */
    public boolean siehtDurchNichtfeste(Entity ziel) {

        Vec3 von = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 nach = new Vec3(ziel.getX(), ziel.getEyeY(), ziel.getZ());

        return this.level().clip(new ClipContext(von, nach,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /** Der Laser. Der Kopf gibt fuenf Strahlen ab, ein Glied einen. */
    protected void laserAngriff(Entity ziel, boolean vomKopf) {

        if(!(ziel instanceof LivingEntity lebend)) return;

        Vec3 auge = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 richtung = new Vec3(lebend.getX() - auge.x,
                lebend.getEyeY() - auge.y,
                lebend.getZ() - auge.z).normalize();

        if(vomKopf) {

            for(int i = 0; i < 5; i++) {
                BulletBaseMK4 strahl = new BulletBaseMK4(this.level(), this, XFactoryNPC.worm_laser,
                        XFactoryNPC.worm_laser.damageMult, i * 0.05F, auge, richtung);
                this.level().addFreshEntity(strahl);
            }

            this.klang(0.75F);

        } else {
            BulletBaseMK4 strahl = new BulletBaseMK4(this.level(), this, XFactoryNPC.worm_bolt,
                    XFactoryNPC.worm_bolt.damageMult, 0.125F, auge, richtung.scale(0.5D));
            this.level().addFreshEntity(strahl);

            this.klang(1.0F);
        }
    }

    private void klang(float hoehe) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                NtmSoundEvents.WEAPON_BALLS_LASER.get(), SoundSource.HOSTILE, 5.0F, hoehe);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
