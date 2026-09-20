package com.hbm.entity.mob;

import com.hbm.entity.projectile.TauShot;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityCyberCrab.
 *
 * Die Kybernetische Krabbe. Vier Lebenspunkte, schnell, scheu -- und sie schiesst aus
 * fuenfzehn Bloecken Entfernung Tau-Bolzen, gegen die keine Panzerung hilft. Wer sie
 * erschlaegt, hoert einen kleinen Knall: sie zerplatzt. Aufgestellt wird sie vom
 * meteor_spawner, dem Block aus den Sternenmetall-Ruinen.
 *
 * SIE MEIDET WASSER, WEIL WASSER SIE TOETET: Nass werden, brennen oder im Wasser stehen
 * kostet je Tick zehn Schaden -- bei vier Lebenspunkten ein einziger Tick. Das Original
 * stellt dafuer den Wegfinder auf setAvoidsWater(true) und laesst sie trotzdem mit dem
 * gewoehnlichen EntityAIWander umherlaufen; in 1.21 ist das WaterAvoidingRandomStrollGoal
 * plus eine Sperre fuer Wasserfelder im Wegfinder genau diese Kombination.
 *
 * SIE IST GEGEN TAU-SCHADEN IMMUN -- sonst wuerden sich zwei Krabben gegenseitig mit einem
 * Schuss umbringen -- und gegen Strahlung (IRadiationImmune).
 *
 * WAS SIE ANGREIFT: Spieler und jedes andere Lebewesen ausser ihresgleichen und Creepern.
 * Der nukleare Creeper des Ports ist ein Creeper und faellt darum unter dieselbe Ausnahme;
 * im Original steht er als eigener Fall daneben, weil er dort nicht von EntityCreeper erbt.
 *
 * NICHT UEBERNOMMEN: getMaxSafePointTries. Das Original erhoeht dort die Suchtiefe des
 * Wegfinders mit der Lebensenergie -- ein Haken, den 1.21 nicht mehr kennt.
 */
public class CyberCrab extends Monster implements RangedAttackMob, IRadiationImmune {

    /** Alles ausser ihresgleichen und Creepern ist Ziel. */
    private static final Predicate<LivingEntity> AUSWAHL = ziel -> !(ziel instanceof CyberCrab || ziel instanceof Creeper);

    public CyberCrab(EntityType<? extends CyberCrab> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.75D);
    }

    @Override
    protected void registerGoals() {

        /* Die Sumpfkrabbe flieht nicht -- sie ist der Hausherr. */
        if(this.flieht()) this.goalSelector.addGoal(0, new PanicGoal(this, 0.75D));

        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(4, this.fernkampf());

        /* Zufallsabstand null: das Original prueft jeden Tick, ohne zu wuerfeln. */
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, true, AUSWAHL));
    }

    /** Alle sechzig bis achtzig Ticks ein Schuss, aus hoechstens fuenfzehn Bloecken. */
    protected RangedAttackGoal fernkampf() {
        return new RangedAttackGoal(this, 0.5D, 60, 80, 15.0F);
    }

    /** Die Taint-Krabbe ueberschreibt das: sie bekommt kein PanicGoal. */
    protected boolean flieht() {
        return true;
    }

    /**
     * Die Sprengkraft beim Tod. Das Original entscheidet sie ueber ein
     * instanceof EntityTaintCrab mitten im Takt; hier fragt jede Art fuer sich.
     */
    protected float sprengkraft() {
        return 0.1F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source.is(NtmDamageTypes.TAU_BLAST)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        /* Wasser, Naesse und Feuer sind toedlich -- zehn Schaden auf vier Lebenspunkte. */
        if(this.isInWater() || this.isInWaterOrRain() || this.isOnFire()) {
            this.hurt(this.damageSources().generic(), 10F);
        }
    }

    /**
     * Das Original prueft im Takt auf Lebensenergie kleiner gleich null und sprengt dort.
     * In 1.21 ist der Tod ein eigener Aufruf -- er kommt genau einmal und genau dann.
     */
    @Override
    public void die(DamageSource source) {
        super.die(source);

        if(!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    this.sprengkraft(), Level.ExplosionInteraction.NONE);
        }
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return NtmSoundEvents.ENTITY_CYBERCRAB.get(); }
    @Override protected SoundEvent getDeathSound() { return NtmSoundEvents.ENTITY_CYBERCRAB.get(); }

    /**
     * Der Tau-Bolzen. Das Original nimmt Geschwindigkeit 1,6 und Streuung 2 und zielt auf
     * die Brust des Ziels.
     */
    @Override
    public void performRangedAttack(LivingEntity ziel, float staerke) {

        TauShot schuss = new TauShot(this.level(), this);

        Vec3 richtung = new Vec3(
                ziel.getX() - this.getX(),
                ziel.getY() + ziel.getBbHeight() / 2F - schuss.getY(),
                ziel.getZ() - this.getZ());

        schuss.shoot(richtung.x, richtung.y, richtung.z, 1.6F, 2F);
        this.level().addFreshEntity(schuss);

        this.playSound(NtmSoundEvents.WEAPON_SAW_SHOOT.get(), 1.0F, 2.0F);
    }
}
