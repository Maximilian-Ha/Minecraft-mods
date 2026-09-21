package com.hbm.entity.mob;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmDamageTypes;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityRADBeast.
 *
 * Das Strahlenbiest -- eine Lohe, die nicht brennt, sondern strahlt. Es steigt auf, wenn sein
 * Ziel ueber ihm ist, faellt nur gebremst und nimmt keinen Fallschaden; Wasser dagegen tut ihm
 * jede Runde weh.
 *
 * SEIN ANGRIFF IST KEIN FEUERBALL. Unter zwei Bloecken schlaegt es zu wie jedes Untier; bis
 * dreissig Bloecke bestrahlt es sein Ziel unmittelbar -- sechzehn Punkte, und der Chunk unter
 * ihm bekommt hundert Einheiten Strahlung dazu. Ein Geschoss fliegt dabei nicht; was man sieht,
 * ist der gruene Strahl des Darstellers.
 *
 * ZWEI GROESSEN. Das gewoehnliche Biest hat 120 Lebenspunkte, der Anfuehrer 360 -- und nur
 * seinetwegen gibt es den Erfolg. Das Original unterscheidet beide an der Hoechstenergie
 * (> 150), und genau daran haengt auch, welche Teilchen es versprueht und ob die Muenze faellt.
 *
 * NICHT UEBERNOMMEN: die eigene Geiger-Stimme (item.geiger1 bis 6). Der Port hat die Klaenge,
 * aber keinen Mob, der sie als Lebenslaut fuehrt; das waere eine Klangtabelle fuer sich.
 */
public class RadBeast extends Monster implements IRadiationImmune {

    /** Die Kennung dessen, den es gerade bestrahlt -- der Darsteller zieht daran seinen Strahl. */
    private static final EntityDataAccessor<Integer> OPFER =
            SynchedEntityData.defineId(RadBeast.class, EntityDataSerializers.INT);

    /** Ab dieser Hoechstenergie gilt es als Anfuehrer. */
    public static final double ANFUEHRER_SCHWELLE = 150D;

    /** Wie hoch ueber dem Ziel es schweben will. Das Original wuerfelt sie alle 100 Ticks neu. */
    private float schwebeHoehe = 0.5F;
    private int schwebeZaehler;

    private int angriffsPause;

    public RadBeast(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OPFER, 0);
    }

    /** Macht aus ihm den Anfuehrer: dreifache Energie und die Muenze in der Hand. */
    public RadBeast zumAnfuehrer() {
        this.setDropChance(EquipmentSlot.MAINHAND, 1F);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NtmItems.COIN_RADIATION.get()));
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(360.0D);
        this.heal(this.getMaxHealth());
        return this;
    }

    public boolean istAnfuehrer() {
        return this.getMaxHealth() > ANFUEHRER_SCHWELLE;
    }

    /** Wen es gerade bestrahlt, oder null. */
    public Entity getOpfer() {
        int kennung = this.entityData.get(OPFER);
        return kennung == 0 ? null : this.level().getEntity(kennung);
    }

    @Override
    public void aiStep() {

        if(!this.level().isClientSide) {

            if(this.isInWaterOrRain()) {
                this.hurt(this.damageSources().drown(), 1.0F);
            }

            if(--this.schwebeZaehler <= 0) {
                this.schwebeZaehler = 100;
                this.schwebeHoehe = 0.5F + (float) this.random.nextGaussian() * 3.0F;
            }

            LivingEntity ziel = this.getTarget();

            if(ziel != null && ziel.getEyeY() > this.getEyeY() + this.schwebeHoehe) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, (0.3D - this.getDeltaMovement().y) * 0.3D, 0));
            }

            this.angreifen(ziel);
        }

        if(!this.onGround() && this.getDeltaMovement().y < 0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.6D, 1));
        }

        this.teilchen();

        super.aiStep();
    }

    /**
     * Der Angriff. Unter zwei Bloecken der Schlag, bis dreissig die Strahlung -- beides mit
     * derselben Pause von zwanzig Ticks, wie im Original.
     */
    private void angreifen(LivingEntity ziel) {

        if(this.angriffsPause > 0) this.angriffsPause--;

        if(ziel == null || !ziel.isAlive()) {
            this.entityData.set(OPFER, 0);
            return;
        }

        double weite = this.distanceTo(ziel);

        if(this.angriffsPause > 0) return;

        if(weite < 2.0F) {
            this.angriffsPause = 20;
            this.entityData.set(OPFER, 0);
            this.doHurtTarget(ziel);
            return;
        }

        if(weite < 30.0F) {
            this.angriffsPause = 20;
            this.entityData.set(OPFER, ziel.getId());

            ChunkRadiationManager.proxy.incrementRad(this.level(), this.blockPosition(), 100F);
            ziel.hurt(this.damageSources().source(NtmDamageTypes.RADIATION, this), 16.0F);
            this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

            this.lookAt(ziel, 30F, 30F);
        }
    }

    /**
     * Was es aussendet: das gewoehnliche Biest Stadtaura und gelegentlich eine Flamme, der
     * Anfuehrer Lava. Das Original unterscheidet auch das an der Hoechstenergie.
     */
    private void teilchen() {

        if(!this.level().isClientSide) return;

        if(!this.istAnfuehrer()) {

            for(int i = 0; i < 6; i++) {
                this.level().addParticle(ParticleTypes.MYCELIUM,
                        this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth() * 1.5,
                        this.getY() + this.random.nextDouble() * this.getBbHeight(),
                        this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth() * 1.5, 0, 0, 0);
            }

            if(this.random.nextInt(6) == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                        this.getY() + this.random.nextDouble() * this.getBbHeight() * 0.75,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(), 0, 0, 0);
            }

        } else {
            this.level().addParticle(ParticleTypes.LAVA,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight() * 0.75,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(), 0, 0, 0);
        }
    }

    @Override
    public void die(DamageSource quelle) {
        super.die(quelle);

        if(!this.istAnfuehrer()) return;

        List<Player> nahe = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(50));
        for(Player spieler : nahe) {
            if(spieler instanceof ServerPlayer server) NtmCriteria.marke(server, "boss_meltdown");
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource quelle, boolean kuerzlichGetroffen) {
        super.dropCustomDeathLoot(level, quelle, kuerzlichGetroffen);

        if(!kuerzlichGetroffen) return;

        /* Ein bis drei Brennstaebe, und im Wasser stattdessen doppelt so viel Abfall. */
        int anzahl = this.random.nextInt(3) + 1;
        boolean nass = this.isInWaterOrRain();

        for(int i = 0; i < anzahl; i++) {
            switch(this.random.nextInt(3)) {
                case 0 -> this.spawnAtLocation(new ItemStack(nass ? NtmItems.WASTE_URANIUM.get() : NtmItems.ROD_ZIRNOX_URANIUM_FUEL_DEPLETED.get(), nass ? 2 : 1));
                case 1 -> this.spawnAtLocation(new ItemStack(nass ? NtmItems.WASTE_MOX.get() : NtmItems.ROD_ZIRNOX_MOX_FUEL_DEPLETED.get(), nass ? 2 : 1));
                default -> this.spawnAtLocation(new ItemStack(nass ? NtmItems.WASTE_PLUTONIUM.get() : NtmItems.ROD_ZIRNOX_PLUTONIUM_FUEL_DEPLETED.get(), nass ? 2 : 1));
            }
        }
    }

    @Override
    public boolean causeFallDamage(float weite, float wucht, DamageSource quelle) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double weite) {
        return false;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }
}
