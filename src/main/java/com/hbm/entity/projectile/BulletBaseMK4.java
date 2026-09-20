package com.hbm.entity.projectile;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class BulletBaseMK4 extends ProjectileLerping {

    public BulletConfig config;
    //used for rendering tracers
    public float velocity;
    public float prevVelocity;
    public double accel;
    public float damage;
    public int ricochets = 0;
    @Nullable public Entity lockonTarget = null;

    /*
     * DER STECKZUSTAND. Ein Geschoss, das in einer Wand steckt, statt an ihr zu zerschellen --
     * der Enterhaken des Ladungswerfers braucht genau das, denn er ist der Ankerpunkt, an dem
     * sich der Schuetze heranzieht.
     *
     * IM ORIGINAL ERBT DAS GESCHOSS DAS. Dort steht EntityBulletBaseMK4 unter
     * EntityThrowableInterp und damit unter EntityThrowableNT, wo getStuck zuhause ist. Der
     * Port hat die Vererbung anders geschnitten: ThrowableNT und ProjectileNT sind zwei
     * getrennte Zweige unter Projectile. Deshalb steht derselbe Zustand hier ein zweites Mal.
     *
     * ER STEHT UND FAELLT MIT DEM BLOCK. Verschwindet der, an dem der Haken haengt, faellt das
     * Geschoss wieder -- sonst haenge man an einer Wand, die es nicht mehr gibt.
     */
    @Nullable protected BlockPos steckBlock;
    @Nullable protected BlockState steckZustand;
    protected boolean imBoden;

    public boolean istImBoden() { return this.imBoden; }

    private static final EntityDataAccessor<Integer> BULLET_CONFIG = SynchedEntityData.defineId(BulletBaseMK4.class, EntityDataSerializers.INT);

    public BulletBaseMK4(EntityType<? extends BulletBaseMK4> entityType, Level level) {
        super(entityType, level);
    }

    public BulletBaseMK4(Level level) { super(NtmEntityTypes.BULLET_MK4.get(), level); }

    /** For submunitions! */
    public BulletBaseMK4(Level level, LivingEntity living, BulletConfig config, float damage, float gunSpread, Vec3 pos, Vec3 delta) {
        this(level);

        this.setOwner(living);
        this.setBulletConfig(config);

        this.damage = damage;

        this.moveTo(pos, 0, 0);
        this.setPos(this.position());

        this.setDeltaMovement(delta);

        this.shoot(delta.x, delta.y, delta.z, 1F, this.config.spread + gunSpread);
    }

    /** For standard guns */
    public BulletBaseMK4(LivingEntity living, BulletConfig config, float baseDamage, float gunSpread, double sideOffset, double heightOffset, double frontOffset) {
        this(living.level);

        this.setOwner(living);
        this.setBulletConfig(config);

        this.damage = baseDamage * this.config.damageMult;

        this.moveTo(living.getX(), living.getY() + living.getEyeHeight(), living.getZ(), living.yRot, living.xRot);

        Vec3 offset = new Vec3(sideOffset, heightOffset, frontOffset);
        offset = offset.xRot(-this.xRot / 180F * (float) Math.PI);
        offset = offset.yRot(-this.yRot / 180F * (float) Math.PI);

        Vec3 positionOffset = this.position().add(offset);
        this.setPos(positionOffset);

        float xd = -Mth.sin(this.yRot / 180.0F * (float) Math.PI) * Mth.cos(this.xRot / 180.0F * (float) Math.PI);
        float yd = (-Mth.sin(this.xRot / 180.0F * (float) Math.PI));
        float zd = Mth.cos(this.yRot / 180.0F * (float) Math.PI) * Mth.cos(this.xRot / 180.0F * (float) Math.PI);
        this.shoot(xd, yd, zd, 1F, gunSpread);
    }

    /** For turrets - angles are in radians, and pitch is negative! */
    public BulletBaseMK4(Level level, BulletConfig config, float baseDamage, float gunSpread, float yRot, float xRot) {
        this(level);

        this.setBulletConfig(config);

        this.damage = baseDamage * this.config.damageMult;

        this.yRotO = this.yRot = yRot * 180F / (float) Math.PI;
        this.xRotO = this.xRot = -xRot * 180F / (float) Math.PI;

        float xd = -Mth.sin(this.yRot / 180.0F * (float) Math.PI) * Mth.cos(this.xRot / 180.0F * (float) Math.PI);
        float yd = (-Mth.sin(this.xRot / 180.0F * (float) Math.PI));
        float zd = Mth.cos(this.yRot / 180.0F * (float) Math.PI) * Mth.cos(this.xRot / 180.0F * (float) Math.PI);
        this.shoot(xd, yd, zd, 1F, gunSpread);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BULLET_CONFIG, 0);
    }

    public void setBulletConfig(BulletConfig config) {
        this.config = config;
        this.getEntityData().set(BULLET_CONFIG, config.id);
    }

    @Nullable
    public BulletConfig getBulletConfig() {
        int id = this.getEntityData().get(BULLET_CONFIG);
        if(id < 0 || id > BulletConfig.configs.size()) return null;
        return BulletConfig.configs.get(id);
    }

    @Override
    public void tick() {

        if(config == null) config = this.getBulletConfig();

        if(config == null) {
            this.discard();
            return;
        }

        this.xo = this.position.x;
        this.yo = this.position.y;
        this.zo = this.position.z;

        super.tick();

        double dX = this.position.x - this.xo;
        double dY = this.position.y - this.yo;
        double dZ = this.position.z - this.zo;

        if(this.lockonTarget != null && this.lockonTarget.isAlive()) {
            Vec3 deltaMotion = this.getDeltaMovement();
            double vel = deltaMotion.length();
            Vec3 delta = new Vec3(lockonTarget.getX() - this.getX(), lockonTarget.getY() + lockonTarget.getBbHeight() / 2D - this.getY(), lockonTarget.getZ() - this.getZ());
            float turn = Math.min(0.005F * this.tickCount, 1F);
            Vec3 newDeltaMotion = new Vec3(
                    Mth.lerp(deltaMotion.x, delta.x, turn),
                    Mth.lerp(deltaMotion.y, delta.y, turn),
                    Mth.lerp(deltaMotion.z, delta.z, turn)).normalize().scale(vel);
            this.setDeltaMovement(newDeltaMotion);
            if(this.level instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));
            }
        }

        this.prevVelocity = this.velocity;
        this.velocity = (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ);

        if(this.imBoden) this.steckTick();

        if(!level.isClientSide && this.tickCount > config.expires) this.discard();

        if(this.config.onUpdate != null) this.config.onUpdate.accept(this);
    }

    /**
     * Setzt das Geschoss in einem Block fest. Das Original merkt sich dafuer Ort und Blockart;
     * hier stehen BlockPos und BlockState.
     */
    public void getStuck(BlockPos pos, Direction seite) {
        this.steckBlock = pos;
        this.steckZustand = this.level.getBlockState(pos);
        this.imBoden = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.prevVelocity = 0F;
        this.velocity = 0F;
        this.hasImpulse = true;
    }

    /** Haelt das Geschoss still -- oder laesst es los, wenn der Block verschwunden ist. */
    private void steckTick() {

        if(this.steckBlock != null && this.level.getBlockState(this.steckBlock) == this.steckZustand) {
            this.setDeltaMovement(Vec3.ZERO);
            this.prevVelocity = 0F;
            this.velocity = 0F;
            return;
        }

        this.imBoden = false;
        this.steckBlock = null;
        this.steckZustand = null;
    }

    @Override protected void rotation() { }

    @Override
    protected void onHit(HitResult hr) {
        super.onHit(hr);

        if(!level.isClientSide) {
            if(this.config.onImpact != null) this.config.onImpact.accept(this, hr);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult ehr) {

        if(!this.isAlive()) return;
        if(this.config.onEntityHit != null) this.config.onEntityHit.accept(this, ehr);
    }

    @Override
    protected void onHitBlock(BlockHitResult bhr) {
        super.onHitBlock(bhr);

        if(this.config.onRicochet != null) this.config.onRicochet.accept(this, bhr);
    }

    @Override protected double getHeadingForceMult() { return 1.0; }
    /**
     * Solange das Geschoss steckt, zieht nichts mehr an ihm. Das ist der ganze Kniff: die
     * Bewegung steht auf null, und ohne Schwerkraft bleibt sie das auch. Ein frueher Ausstieg
     * aus tick() waere der naheliegendere Weg gewesen und der falsche -- dann zaehlte
     * tickCount nicht weiter, und das Geschoss liefe nie ab.
     */
    @Override protected double getDefaultGravity() { return this.imBoden ? 0D : this.config.gravity; }
    @Override protected double getMotionMult() { return this.config.velocity + this.accel; }
    @Override protected float getAirDrag() { return 1F; }
    @Override protected float getWaterDrag() { return 1F; }
    @Override public boolean doesPenetrate() { return this.config.doesPenetrate; }
    @Override public boolean isSpectral() { return this.config.isSpectral; }

    /**
     * DER SCHUETZE IST FUER DIE ERSTEN TICKS UNVERWUNDBAR. Das Geschoss entsteht eine
     * Handbreit vor ihm und liegt damit noch in seinem eigenen Umriss; ohne diese Sperre
     * traefe sich jeder Schuetze selbst. Wie lange, sagt die Geschossart -- zwei Ticks
     * gewoehnlich, zwanzig beim Flammenwerfer, dessen Flammen langsam und dicht am Lauf
     * entstehen.
     *
     * Das Original macht dieselbe Pruefung eine Ebene tiefer, beim Austeilen des Schadens
     * (EntityBulletBaseNT Z. 227).
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        if(!target.canBeHitByProjectile()) return false;
        if(target == this.getOwner() && this.tickCount < this.config.selfDamageDelay) return false;
        return this.config.impactsEntities;
    }
}
