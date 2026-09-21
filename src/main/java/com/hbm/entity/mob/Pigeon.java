package com.hbm.entity.mob;

import com.hbm.entity.ai.ConditionalFloatGoal;
import com.hbm.entity.ai.ConditionalStrollGoal;
import com.hbm.entity.ai.EatBreadGoal;
import com.hbm.entity.ai.StartFlyingGoal;
import com.hbm.entity.ai.StopFlyingGoal;
import com.hbm.items.tools.FertilizerItem;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.util.particle.ParticleUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityPigeon.
 *
 * Die Taube. Sie laeuft, sie fliegt, sie frisst Brot -- und wenn sie satt ist, duengt sie im
 * Flug alles, was unter ihr waechst.
 *
 * SIE HAT ZWEI ZUSTAENDE, Laufen und Fliegen, und zwei Aufgaben schalten zwischen ihnen um:
 * abgehoben wird bei Angriff, bei Feuer oder aus Laune (eins zu sechshundert je Takt),
 * gelandet allein aus Laune (eins zu zweihundert). Im Flug steigt sie mit einem Rauschen um
 * 0,04 pro Takt, bis sie zehn Bloecke ueber dem Boden ist, und laeuft mit anderthalb
 * vorwaerts; alle zwanzig Takte dreht sie im Mittel ein Stueck.
 *
 * FETT WIRD SIE VOM BROT, und fett bleibt sie, bis sie im Flug duengt: eins zu fuenfzig je
 * Takt sucht sie sich fuenfundzwanzig Bloecke unter sich die erste Pflanze, die Knochenmehl
 * annimmt, und laesst etwas fallen. Danach wird sie mit einer Wahrscheinlichkeit von eins zu
 * zehn wieder duenn. Das Duengen ist ERZWUNGEN -- der Wurf, der sonst entscheidet, entfaellt.
 *
 * ZWEI SCHLAEGE MIT VOLLER WUCHT LASSEN SIE PLATZEN: wer ihr auf einmal doppelt so viel
 * Schaden zufuegt, wie sie Leben hat, bekommt keine Leiche, sondern zehn Federn in alle
 * Richtungen.
 *
 * SIE FAELLT NICHT: Sturzschaden gibt es fuer sie nicht, und Druckplatten loest sie nicht aus.
 */
public class Pigeon extends PathfinderMob implements IFlyingCreature {

    private static final EntityDataAccessor<Byte> FLUGZUSTAND =
            SynchedEntityData.defineId(Pigeon.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> FETT =
            SynchedEntityData.defineId(Pigeon.class, EntityDataSerializers.BOOLEAN);

    /** Nur fuer den Zeichner: der Fluegelschlag und sein Wert im vorigen Bild. */
    public float fallTime;
    public float prevFallTime;
    public float dest;
    public float prevDest;
    public float offGroundTimer = 1.0F;

    public Pigeon(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new StartFlyingGoal(this, this));
        this.goalSelector.addGoal(0, new StopFlyingGoal(this, this));
        this.goalSelector.addGoal(1, new ConditionalFloatGoal(this, wesen -> amBoden()));
        this.goalSelector.addGoal(2, new EatBreadGoal(this, 0.4D));
        this.goalSelector.addGoal(5, new ConditionalStrollGoal(this, 0.2D, wesen -> amBoden()));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Die Bedingung, die im Original als Predicate an drei Aufgaben haengt. */
    private boolean amBoden() {
        return this.getFlyingState() == IFlyingCreature.STATE_WALKING;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLUGZUSTAND, (byte) IFlyingCreature.STATE_WALKING);
        builder.define(FETT, false);
    }

    @Override
    public int getFlyingState() {
        return this.entityData.get(FLUGZUSTAND);
    }

    @Override
    public void setFlyingState(int state) {
        this.entityData.set(FLUGZUSTAND, (byte) state);
    }

    public boolean isFat() {
        return this.entityData.get(FETT);
    }

    public void setFat(boolean fett) {
        this.entityData.set(FETT, fett);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("flyingState", (byte) this.getFlyingState());
        tag.putBoolean("fat", this.isFat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFlyingState(tag.getByte("flyingState"));
        this.setFat(tag.getBoolean("fat"));
    }

    /** Doppelte Lebensenergie auf einen Schlag: keine Leiche, sondern zehn Federn. */
    @Override
    public boolean hurt(DamageSource quelle, float schaden) {

        if(schaden >= this.getMaxHealth() * 2 && !this.level().isClientSide) {

            for(int i = 0; i < 10; i++) {

                Vec3 richtung = new Vec3(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).normalize();

                ItemEntity feder = new ItemEntity(this.level(),
                        this.getX() + richtung.x,
                        this.getY() + this.getBbHeight() / 2D + richtung.y,
                        this.getZ() + richtung.z,
                        new ItemStack(Items.FEATHER));
                feder.setDeltaMovement(richtung.scale(0.5D));

                this.level().addFreshEntity(feder);
            }

            this.discard();
            return true;
        }

        return super.hurt(quelle, schaden);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource quelle, boolean kuerzlichGetroffen) {
        super.dropCustomDeathLoot(level, quelle, kuerzlichGetroffen);

        /* Null bis zwei Federn -- das Original wuerfelt rand(3) plus rand(1 + Pluenderung),
         * und rand(1) ist immer null, solange nicht gepluendert wird. */
        int federn = this.random.nextInt(3);
        for(int i = 0; i < federn; i++) this.spawnAtLocation(Items.FEATHER);

        /* Eine fette Taube gibt drei Haehnchen, eine duenne eines -- gebraten, wenn sie brennt. */
        int anzahl = this.isFat() ? 3 : 1;
        this.spawnAtLocation(new ItemStack(this.isOnFire() ? Items.COOKED_CHICKEN : Items.CHICKEN, anzahl));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if(this.getFlyingState() == IFlyingCreature.STATE_FLYING) {

            int boden = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
            boolean hochGenug = this.getY() - boden > 10;

            double steigen = this.random.nextGaussian() * 0.05
                    + (hochGenug ? 0 : 0.04)
                    + (this.isInWater() ? 0.2 : 0);

            Vec3 schwung = this.getDeltaMovement();
            this.setDeltaMovement(schwung.x, this.onGround() ? Math.abs(steigen) + 0.1D : steigen, schwung.z);

            this.zza = 1.5F;

            if(this.random.nextInt(20) == 0) this.setYRot(this.getYRot() + (float) (this.random.nextGaussian() * 30));

            if(this.isFat() && this.random.nextInt(50) == 0) this.duengen();

        } else if(!this.onGround() && this.getDeltaMovement().y < 0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.8D, 1));
        }
    }

    /** Was von oben kommt: fuenfundzwanzig Bloecke nach unten bis zur ersten Pflanze. */
    private void duengen() {

        if(this.level().isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putInt("count", 3);
        tag.put("state", NbtUtils.writeBlockState(Blocks.WHITE_WOOL.defaultBlockState()));
        tag.putInt("entity", this.getId());
        ParticleUtil.addParticle(this.level(), new NbtParticleOptions(NtmParticleTypes.SWEAT.get(), tag),
                this.getX(), this.getY(), this.getZ(), 50.0);

        BlockPos oben = this.blockPosition().below();

        for(int i = 0; i < 25; i++) {

            BlockPos pos = oben.below(i);

            if(FertilizerItem.duengen(this.level(), pos, true)) {
                this.level().levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                break;
            }
        }

        if(this.random.nextInt(10) == 0) this.setFat(false);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.prevFallTime = this.fallTime;
        this.prevDest = this.dest;

        this.dest += (this.onGround() ? -1 : 4) * 0.3F;
        if(this.dest < 0.0F) this.dest = 0.0F;
        if(this.dest > 1.0F) this.dest = 1.0F;

        if(!this.onGround() && this.offGroundTimer < 1.0F) this.offGroundTimer = 1.0F;
        this.offGroundTimer *= 0.9F;

        if(!this.onGround() && this.getDeltaMovement().y < 0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.6D, 1));
        }

        this.fallTime += this.offGroundTimer * 2.0F;
    }

    @Override
    public boolean causeFallDamage(float weite, float wucht, DamageSource quelle) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
