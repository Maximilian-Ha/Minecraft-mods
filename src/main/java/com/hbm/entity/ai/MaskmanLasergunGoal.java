package com.hbm.entity.ai;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.weapon.sedna.factory.XFactoryNPC;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIMaskmanLasergun.
 *
 * Der Fernbereich, ab zehn Bloecken. Drei Angriffe wechseln sich ab, jeder mit eigener
 * Schlagzahl und eigener Wiederholung:
 *
 *   KUGEL   alle 60 Ticks, fuenfmal   -- eine schwebende Kugel, die von sich aus Bolzen
 *                                        auf jeden Spieler im Umkreis schickt
 *   RAKETE  alle 10 Ticks, zehnmal    -- im Bogen geworfen, die Hoehe gewuerfelt
 *   SALVE   alle 40 Ticks, dreimal    -- fuenf Leuchtspuren auf einmal, und wo eine
 *                                        aufschlaegt, faellt ein Meteor
 *
 * DER WECHSEL IST NICHT REIHUM. Das Original zaehlt die Ordnungszahl des laufenden Angriffs
 * um eine Zufallszahl von null bis eins weiter und nimmt den Rest bei drei -- der naechste
 * Angriff ist also der uebernaechste oder der folgende, nie derselbe zweimal hintereinander.
 */
public class MaskmanLasergunGoal extends Goal {

    private final PathfinderMob wirt;
    private Angriff angriff;
    private int zaehler;
    private int schlagZahl;

    public MaskmanLasergunGoal(PathfinderMob wirt) {
        this.wirt = wirt;
        this.angriff = Angriff.values()[wirt.getRandom().nextInt(Angriff.values().length)];
    }

    @Override
    public boolean canUse() {
        LivingEntity ziel = this.wirt.getTarget();
        if(ziel == null) return false;
        return this.wirt.position().distanceTo(ziel.position()) > 10;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.wirt.getNavigation().isDone();
    }

    @Override
    public void tick() {

        LivingEntity ziel = this.wirt.getTarget();

        if(--this.zaehler <= 0) {
            this.zaehler = this.angriff.pause;

            if(ziel != null && !this.wirt.level().isClientSide) this.feuern(ziel);

            this.schlagZahl++;

            if(this.schlagZahl >= this.angriff.anzahl) {
                this.schlagZahl = 0;
                int naechster = this.angriff.ordinal() + this.wirt.getRandom().nextInt(Angriff.values().length - 1);
                this.angriff = Angriff.values()[naechster % Angriff.values().length];
            }
        }

        this.wirt.setYRot(this.wirt.yHeadRot);
    }

    private void feuern(LivingEntity ziel) {

        Vec3 auge = this.wirt.getEyePosition();
        Vec3 zum = ziel.getEyePosition().subtract(auge).normalize();

        switch(this.angriff) {

            case KUGEL -> {
                BulletBaseMK4 kugel = new BulletBaseMK4(this.wirt.level(), this.wirt, XFactoryNPC.maskman_orb,
                        XFactoryNPC.maskman_orb.damageMult, 0F, auge, zum.scale(2.0D).add(0, 0.5D, 0));
                this.wirt.level().addFreshEntity(kugel);
                this.klang(NtmSoundEvents.WEAPON_TESLA.get());
            }

            case RAKETE -> {
                /* Das Original nimmt nur die waagerechte Richtung, zwanzigfach verkleinert,
                 * und wuerfelt die Steighoehe zwischen einem halben und einem ganzen Block. */
                Vec3 flach = new Vec3(ziel.getX() - this.wirt.getX(), 0, ziel.getZ() - this.wirt.getZ());
                Vec3 wurf = new Vec3(flach.x * 0.05D, 0.5D + this.wirt.getRandom().nextDouble() * 0.5D, flach.z * 0.05D);

                BulletBaseMK4 rakete = new BulletBaseMK4(this.wirt.level(), this.wirt, XFactoryNPC.maskman_rocket,
                        XFactoryNPC.maskman_rocket.damageMult, 0F, auge, wurf);
                this.wirt.level().addFreshEntity(rakete);
                this.klang(NtmSoundEvents.GUN_UNDERBARREL_FIRE.get());
            }

            case SALVE -> {
                for(int i = 0; i < 5; i++) {
                    BulletBaseMK4 spur = new BulletBaseMK4(this.wirt.level(), this.wirt, XFactoryNPC.maskman_tracer,
                            XFactoryNPC.maskman_tracer.damageMult, 0.05F, auge, zum);
                    this.wirt.level().addFreshEntity(spur);
                }
            }
        }
    }

    private void klang(SoundEvent klang) {
        this.wirt.level().playSound(null, this.wirt.getX(), this.wirt.getY(), this.wirt.getZ(),
                klang, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    /** Die drei Angriffe mit den Zahlen des Originals. */
    private enum Angriff {

        KUGEL(60, 5),
        RAKETE(10, 10),
        SALVE(40, 3);

        public final int pause;
        public final int anzahl;

        Angriff(int pause, int anzahl) {
            this.pause = pause;
            this.anzahl = anzahl;
        }
    }
}
