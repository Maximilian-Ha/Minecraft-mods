package com.hbm.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityThrowableNT.
 *
 * Ein Wurfkoerper, der einen Aufschlag ueberlebt. Das ist der Unterschied zu ProjectileNT, an
 * dem alle Geschosse des Ports haengen: dort beendet der erste Treffer den Flug. Eine Granate
 * dagegen springt vom Boden ab, rollt weiter und geht erst hoch, wenn ihr Zuender es sagt --
 * deshalb entscheidet hier die Unterklasse in onImpact, was ein Treffer bedeutet.
 *
 * ZWEI ZUSTAENDE: in der Luft zaehlt ticksInAir und der Koerper fliegt; steckt er fest
 * (getStuck, fuer die Klebegranate), zaehlt ticksInGround und er ruehrt sich nicht mehr. Wird
 * der Block, in dem er steckt, abgebaut, faellt er wieder und fliegt mit einem Bruchteil
 * seiner alten Geschwindigkeit weiter -- so macht es das Original.
 *
 * NICHT UEBERNOMMEN aus dem Original: die Verwaltung des Werfers (thrower, throwerName samt
 * NBT). Auf 1.21 traegt Projectile das schon selbst, mitsamt Speicherung der Kennung.
 * Ebenso die Zwischenschicht EntityThrowableInterp, die nur die Bewegung zwischen zwei
 * Netzpaketen glaettet -- das macht 1.21 in lerpTo, siehe ProjectileLerping.
 */
public abstract class ThrowableNT extends Projectile {

    /** Die Seite, in der der Koerper steckt. Der Zeichner liest sie, deshalb wird sie
     *  uebertragen. Ordnungszahl aus Direction; -1 heisst: steckt nicht. */
    private static final EntityDataAccessor<Integer> STECKT_IN = SynchedEntityData.defineId(ThrowableNT.class, EntityDataSerializers.INT);

    private BlockPos steckBlock;
    private BlockState steckZustand;
    protected boolean imBoden;

    public int ticksInGround;
    public int ticksInAir;

    protected ThrowableNT(EntityType<? extends ThrowableNT> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STECKT_IN, -1);
    }

    public int getStuckIn() { return this.entityData.get(STECKT_IN); }

    /**
     * Setzt den Koerper in einem Block fest. Das Original merkt sich dafuer Ort und Blockart,
     * um zu bemerken, wenn der Block verschwindet; hier steht dasselbe als BlockPos und
     * BlockState.
     */
    public void getStuck(BlockPos pos, Direction seite) {
        this.steckBlock = pos;
        this.steckZustand = this.level.getBlockState(pos);
        this.imBoden = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(STECKT_IN, seite.ordinal());
        this.hasImpulse = true;
    }

    @Override
    public void tick() {

        super.tick();

        if(this.imBoden) {
            this.imBodenTick();
            return;
        }

        this.ticksInAir++;

        Vec3 pos = this.position();
        Vec3 ziel = pos.add(this.getDeltaMovement().scale(this.motionMult()));

        HitResult treffer = this.isSpectral() ? null : this.blockTreffer(pos, ziel);
        if(treffer != null) ziel = treffer.getLocation();

        if(!this.level.isClientSide && this.doesImpactEntities()) {
            EntityHitResult wesen = this.wesenTreffer(pos, ziel);
            if(wesen != null) treffer = wesen;
        }

        if(treffer != null) this.onImpact(treffer);
        if(this.isRemoved()) return;

        if(!this.onGround()) this.drehung();

        Vec3 bewegung = this.getDeltaMovement();

        if(this.fullBlockCollisions()) {
            this.move(net.minecraft.world.entity.MoverType.SELF, bewegung.scale(this.motionMult()));
        } else {
            this.setPos(this.getX() + bewegung.x * this.motionMult(),
                    this.getY() + bewegung.y * this.motionMult(),
                    this.getZ() + bewegung.z * this.motionMult());
        }

        float bremse = this.getAirDrag();

        if(this.isInWater()) {
            for(int i = 0; i < 4; i++) {
                this.level.addParticle(ParticleTypes.BUBBLE,
                        this.getX() - bewegung.x * 0.25, this.getY() - bewegung.y * 0.25, this.getZ() - bewegung.z * 0.25,
                        bewegung.x, bewegung.y, bewegung.z);
            }
            bremse = this.getWaterDrag();
        }

        bewegung = this.getDeltaMovement().scale(bremse);
        this.setDeltaMovement(bewegung.x, bewegung.y - this.getGravityVelocity(), bewegung.z);
    }

    /** Der Koerper steckt fest. Verschwindet der Block unter ihm, faellt er weiter. */
    private void imBodenTick() {

        if(this.level.getBlockState(this.steckBlock) == this.steckZustand) {

            this.ticksInGround++;
            if(this.groundDespawn() > 0 && this.ticksInGround >= this.groundDespawn()) this.discard();
            return;
        }

        this.imBoden = false;
        this.entityData.set(STECKT_IN, -1);
        this.setDeltaMovement(this.getDeltaMovement().multiply(
                this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F));
        this.ticksInGround = 0;
        this.ticksInAir = 0;
    }

    private HitResult blockTreffer(Vec3 pos, Vec3 ziel) {
        BlockHitResult treffer = this.level.clip(new ClipContext(pos, ziel, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return treffer.getType() == HitResult.Type.MISS ? null : treffer;
    }

    /**
     * Das naechste getroffene Wesen auf dem Weg. Der Werfer selbst zaehlt erst nach
     * selfDamageDelay Ticks -- sonst ginge die Granate in der eigenen Hand hoch.
     */
    private EntityHitResult wesenTreffer(Vec3 pos, Vec3 ziel) {

        AABB bereich = this.getBoundingBox().expandTowards(this.getDeltaMovement().scale(this.motionMult())).inflate(1.0);

        Entity naechstes = null;
        double naechste = Double.MAX_VALUE;

        for(Entity entity : this.level.getEntities(this, bereich, this::kannTreffen)) {

            Optional<Vec3> schnitt = entity.getBoundingBox().inflate(0.3).clip(pos, ziel);
            if(schnitt.isEmpty()) continue;

            if(this.doesPenetrate()) {
                this.onImpact(new EntityHitResult(entity));
                if(this.isRemoved()) return null;
                continue;
            }

            double abstand = pos.distanceToSqr(schnitt.get());
            if(abstand < naechste) {
                naechstes = entity;
                naechste = abstand;
            }
        }

        return naechstes == null ? null : new EntityHitResult(naechstes);
    }

    private boolean kannTreffen(Entity ziel) {
        if(!ziel.isAlive() || !ziel.canBeHitByProjectile()) return false;
        return ziel != this.getOwner() || this.ticksInAir >= this.selfDamageDelay();
    }

    /** Nase in Flugrichtung, aber nur zu einem Fuenftel je Tick -- das glaettet den Bogen. */
    private void drehung() {

        Vec3 bewegung = this.getDeltaMovement();
        double flach = bewegung.horizontalDistance();

        this.yRot = (float) (Math.atan2(bewegung.x, bewegung.z) * 180.0 / Math.PI);
        this.xRot = (float) (Math.atan2(bewegung.y, flach) * 180.0 / Math.PI);

        while(this.xRot - this.xRotO < -180.0F) this.xRotO -= 360.0F;
        while(this.xRot - this.xRotO >= 180.0F) this.xRotO += 360.0F;
        while(this.yRot - this.yRotO < -180.0F) this.yRotO -= 360.0F;
        while(this.yRot - this.yRotO >= 180.0F) this.yRotO += 360.0F;

        this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
        this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
    }

    protected abstract void onImpact(HitResult treffer);

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("imBoden", this.imBoden);
        tag.putInt("ticksInGround", this.ticksInGround);
        tag.putInt("ticksInAir", this.ticksInAir);
        tag.putInt("stecktIn", this.getStuckIn());
        if(this.steckBlock != null) {
            tag.putInt("steckX", this.steckBlock.getX());
            tag.putInt("steckY", this.steckBlock.getY());
            tag.putInt("steckZ", this.steckBlock.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.imBoden = tag.getBoolean("imBoden");
        this.ticksInGround = tag.getInt("ticksInGround");
        this.ticksInAir = tag.getInt("ticksInAir");
        this.entityData.set(STECKT_IN, tag.contains("stecktIn") ? tag.getInt("stecktIn") : -1);
        if(tag.contains("steckX")) {
            this.steckBlock = new BlockPos(tag.getInt("steckX"), tag.getInt("steckY"), tag.getInt("steckZ"));
            this.steckZustand = this.level.getBlockState(this.steckBlock);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double groesse = this.getBoundingBox().getSize() * 4.0;
        if(Double.isNaN(groesse)) groesse = 4.0;
        groesse *= 64.0;
        return distance < groesse * groesse;
    }

    protected double motionMult() { return 1.0; }
    protected double getGravityVelocity() { return 0.03; }
    protected float getAirDrag() { return 0.99F; }
    protected float getWaterDrag() { return 0.8F; }
    protected int groundDespawn() { return 1200; }
    protected int selfDamageDelay() { return 5; }
    public boolean fullBlockCollisions() { return false; }
    public boolean doesImpactEntities() { return true; }
    public boolean doesPenetrate() { return false; }
    public boolean isSpectral() { return false; }
}
