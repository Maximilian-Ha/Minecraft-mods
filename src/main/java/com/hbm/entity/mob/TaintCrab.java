package com.hbm.entity.mob;

import com.hbm.blockentity.machine.TeslaBlockEntity;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.lib.ModEffect;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityTaintCrab.
 *
 * Die Taint-Krabbe. Aus einer Teslakrabbe wird sie, sobald sie in den Taint laeuft: dort
 * waechst sie auf fuenfundzwanzig Lebenspunkte und anderthalb Bloecke Kantenlaenge, schlaegt
 * Blitze im Umkreis von zehn statt drei Bloecken, verseucht alles binnen fuenf Bloecken und
 * feuert nicht mehr Tau-Bolzen, sondern brennende 7,62-mm-Geschosse -- alle fuenf Ticks
 * eines, aus bis zu fuenfzig Bloecken.
 *
 * SIE FLIEHT NICHT. Das Original prueft dafuer im Konstruktor der Grundkrabbe, ob es sich
 * um eine Taint-Krabbe handelt, und laesst dann das Panik-Ziel weg; hier fragt es die
 * Grundkrabbe ueber flieht().
 *
 * SIE ZERPLATZT DREISSIGMAL SO STARK: Sprengkraft drei statt nullkommaeins.
 *
 * WAS SIE VERSEUCHT: im Original Strahlung, Staerke sechzehn, zehn Ticks lang -- nicht der
 * Taint-Effekt, obwohl sie aus ihm kommt. (Die CE-Abspaltung hat das spaeter auf Taint
 * geaendert; der Port folgt dem Original.) Ihresgleichen bleibt verschont.
 */
public class TaintCrab extends CyberCrab {

    /** Die Endpunkte der Blitze, nur zum Zeichnen. */
    public List<double[]> targets = new ArrayList<>();

    public TaintCrab(EntityType<? extends TaintCrab> type, Level level) {
        super(type, level);

        /* ignoreFrustumCheck des Originals -- ihre Blitze reichen zehn Bloecke weit. */
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected RangedAttackGoal fernkampf() {
        return new RangedAttackGoal(this, 0.5D, 5, 5, 50.0F);
    }

    @Override protected boolean flieht() { return false; }
    @Override protected float sprengkraft() { return 3F; }

    @Override
    public void aiStep() {

        this.targets = TeslaBlockEntity.zap(this.level(),
                this.getX(), this.getY() + 1.25, this.getZ(), 10, this, !this.level().isClientSide);

        List<LivingEntity> nachbarn = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(this.getX() - 5, this.getY() - 5, this.getZ() - 5,
                         this.getX() + 5, this.getY() + 5, this.getZ() + 5));

        for(LivingEntity e : nachbarn) {
            if(!(e instanceof CyberCrab)) e.addEffect(new MobEffectInstance(ModEffect.RADIATION, 10, 15));
        }

        super.aiStep();
    }

    /**
     * Null bis zwei Kupferspulen, und mit etwa zweieinhalb Prozent eine magnetisierte
     * Wolframspule. Im Original sind das getDropItem und dropRareDrop; die Pluenderungs-
     * verzauberung geht in 1.21 nicht mehr durch diesen Aufruf und bleibt ohne Wirkung.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        int spulen = this.random.nextInt(3);
        for(int i = 0; i < spulen; i++) this.spawnAtLocation(NtmItems.COIL_COPPER.get());

        if(recentlyHit && this.random.nextInt(200) < 5) {
            this.spawnAtLocation(NtmItems.COIL_MAGNETIZED_TUNGSTEN.get());
        }
    }

    /**
     * Ein brennendes 7,62-mm-Vollmantelgeschoss mit Schaden zehn, aus der Mitte der Krabbe.
     *
     * NICHT UEBERNOMMEN: das Original rechnet hier einen Richtungsvektor aus und benutzt ihn
     * nie -- und in dessen zweiter Zeile steht ohnehin posZ, wo posY stehen muesste.
     */
    @Override
    public void performRangedAttack(LivingEntity ziel, float staerke) {

        BulletBaseMK4 geschoss = new BulletBaseMK4(this, XFactory762mm.r762_fmj, 10F, 0F, 0F, 0F, 0F);

        if(this.level() instanceof ServerLevel serverLevel) {
            Vec3 flug = geschoss.getDeltaMovement();
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    geschoss.getX(), geschoss.getY(), geschoss.getZ(), 0,
                    flug.x * 0.3, flug.y * 0.3, flug.z * 0.3, 1.0);
        }

        this.level().addFreshEntity(geschoss);
        this.playSound(NtmSoundEvents.WEAPON_SAW_SHOOT.get(), 1.0F, 0.5F);
    }
}
