package com.hbm.entity.mob.botprime;

import com.hbm.entity.NtmEntityTypes;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.botprime.EntityBOTPrimeBody.
 *
 * Ein Glied der Kette. Es sucht sich kein eigenes Ziel im Wortsinn -- es folgt seinem
 * Vordermann und schiesst auf das, was ihm vor die Linse kommt.
 *
 * SEIN SCHLAG NIMMT DREI VIERTEL DER AKTUELLEN LEBENSENERGIE. Nicht der hoechsten: wer mit
 * einem Prozent Energie hineinlaeuft, verliert drei Viertel davon, nicht drei Viertel seines
 * Maximums. Gegen alles, was nicht lebt, sind es hundert Punkte.
 *
 * ES STIRBT MIT SEINEM VORDERMANN. Findet es niemanden mehr vor sich, zieht es sich
 * neunzehnhundertneunundneunzig Punkte ab -- das ist im Original der Weg, ein Glied zu
 * entfernen, ohne setDead zu rufen. Und hat es seinen Kopf verloren, zerplatzt es mit einer
 * Wahrscheinlichkeit von eins zu sechzig je Tick.
 *
 * TRAENKE WIRKEN NICHT. isPotionApplicable des Originals gibt immer false.
 */
public class BotPrimeBody extends BotPrimeBase {

    public BotPrimeBody(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.sucheWeite = 70.0D;
        this.gliedAbstand = 3.5D;
        this.gliedTempo = 1.4D;
    }

    public BotPrimeBody(Level level) {
        this(NtmEntityTypes.BOT_PRIME_BODY.get(), level);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public float getAngriffsstaerke(Entity ziel) {
        if(ziel instanceof LivingEntity lebend) return lebend.getHealth() * 0.75F;
        return 100F;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance wirkung) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.wurmTick();

        if(this.level().isClientSide) return;

        this.gliedBewegen();

        if(this.gepruef) {

            if(this.gefolgt == null || !this.gefolgt.isAlive()) {
                this.setHealth(this.getHealth() - 1999.0F);
            }

            if((this.vordermann == null || !this.vordermann.isAlive()) && this.random.nextInt(60) == 0) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
            }
        }

        this.schiessen();
        this.blickZumVordermann();
    }

    /** Alle dreissig Ticks ein Schuss: zehn aufwaerts zaehlen, dann zwanzig Pause. */
    private void schiessen() {

        LivingEntity ziel = this.getTarget();

        if(this.vordermann != null && this.vordermann.isAlive() && ziel != null && this.siehtDurchNichtfeste(ziel)) {

            this.angriffsZaehler++;

            if(this.angriffsZaehler == 10) {
                this.laserAngriff(ziel, false);
                this.angriffsZaehler = -20;
            }

        } else if(this.angriffsZaehler > 0) {
            this.angriffsZaehler--;
        }
    }

    /** Ein Glied schaut immer dorthin, wo sein Vordermann ist. */
    private void blickZumVordermann() {

        if(this.gefolgt == null) return;

        double dx = this.gefolgt.getX() - this.getX();
        double dy = this.gefolgt.getY() - this.getY();
        double dz = this.gefolgt.getZ() - this.getZ();
        float flach = (float) Math.sqrt(dx * dx + dz * dz);

        float gier = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
        float neigung = (float) (Math.atan2(dy, flach) * 180.0D / Math.PI);

        this.setYRot(gier);
        this.yRotO = gier;
        this.setXRot(neigung);
        this.xRotO = neigung;
    }
}
