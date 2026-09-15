package com.hbm.entity.effect;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.particle.helper.FlameCreator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.effect.EntityFireLingering.
 *
 * Ein Feuer, das steht statt zu brennen. Es ist keine Blockflamme, sondern eine Entitaet mit
 * eigenem Kasten: wer hineingeraet, faengt an zu brennen, und zwar auf die Art, die der Typ
 * vorgibt -- gewoehnlich, Phosphor, Bannfeuer oder schwarzes Feuer.
 *
 * Sichtbar ist es ausschliesslich ueber die Flammenpartikel, die es selbst erzeugt; die sucht es
 * sich mit einem Strahl nach unten den Boden, damit sie auf dem Boden sitzen und nicht in der
 * Luft haengen.
 *
 * WER NICHT LEBT, brennt gewoehnlich -- fuer Gegenstaende und Loren gibt es keine vier Arten von
 * Feuer, nur die eine.
 *
 * ES WIRD NICHT GESPEICHERT. Beim Verlassen der Welt verschwindet es; ein stehendes Feuer, das
 * einen Serverneustart ueberlebt, waere eine Falle ohne Ursache.
 */
public class FireLingering extends Entity {

    public static final int TYPE_DIESEL = 0;
    public static final int TYPE_BALEFIRE = 1;
    public static final int TYPE_PHOSPHORUS = 2;
    public static final int TYPE_OXY = 3;
    public static final int TYPE_BLACK = 4;

    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(FireLingering.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AREA_WIDTH = SynchedEntityData.defineId(FireLingering.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AREA_HEIGHT = SynchedEntityData.defineId(FireLingering.class, EntityDataSerializers.FLOAT);

    public int maxAge = 150;

    public FireLingering(EntityType<? extends FireLingering> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public FireLingering(Level level) {
        this(NtmEntityTypes.FIRE_LINGERING.get(), level);
    }

    public FireLingering setArea(float width, float height) {
        this.entityData.set(AREA_WIDTH, width);
        this.entityData.set(AREA_HEIGHT, height);
        this.refreshDimensions();
        return this;
    }

    public FireLingering setDuration(int duration) {
        this.maxAge = duration;
        return this;
    }

    /** Heisst im Original setType/getType -- auf 1.21 ist getType() schon vergeben. */
    public FireLingering setFireType(int type) {
        this.entityData.set(TYPE, type);
        return this;
    }

    public int getFireType() {
        return this.entityData.get(TYPE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TYPE, 0);
        builder.define(AREA_WIDTH, 0F);
        builder.define(AREA_HEIGHT, 0F);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(Math.max(this.entityData.get(AREA_WIDTH), 0.1F), Math.max(this.entityData.get(AREA_HEIGHT), 0.1F));
    }

    @Override
    public void tick() {

        float width = this.entityData.get(AREA_WIDTH);
        float height = this.entityData.get(AREA_HEIGHT);

        if(!this.level().isClientSide) {

            if(this.tickCount >= this.maxAge) {
                this.discard();
                return;
            }

            AABB box = new AABB(this.getX() - width / 2, this.getY(), this.getZ() - width / 2,
                    this.getX() + width / 2, this.getY() + height, this.getZ() + width / 2);

            for(Entity e : this.level().getEntities(this, box)) {
                this.affect(e);
            }

        } else {

            for(int i = 0; i < (width >= 5 ? 2 : 1); i++) {

                double x = this.getX() - width / 2 + this.random.nextDouble() * width;
                double z = this.getZ() - width / 2 + this.random.nextDouble() * width;

                /* Der Strahl nach unten sucht den Boden, damit die Flamme nicht in der Luft steht. */
                Vec3 up = new Vec3(x, this.getY() + height, z);
                Vec3 down = new Vec3(x, this.getY() - height, z);
                BlockHitResult hit = this.level().clip(new ClipContext(up, down, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                double y = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().y : down.y;

                FlameCreator.composeEffectClient(x, y, z, this.getParticleMeta());
            }
        }
    }

    /** Bannfeuer und schwarzes Feuer haben eigene Partikel; alles andere brennt gewoehnlich. */
    private int getParticleMeta() {
        return switch(this.getFireType()) {
            case TYPE_BALEFIRE -> FlameCreator.META_BALEFIRE;
            case TYPE_BLACK -> FlameCreator.META_BLACK;
            default -> FlameCreator.META_FIRE;
        };
    }

    protected void affect(Entity e) {

        if(!(e instanceof LivingEntity living)) {
            e.setRemainingFireTicks(4 * 20);
            return;
        }

        HbmLivingAttachments props = HbmLivingAttachments.getData(living);

        switch(this.getFireType()) {
            case TYPE_DIESEL -> { if(props.fire < 60) props.fire = 60; }
            case TYPE_PHOSPHORUS -> { if(props.fire < 300) props.fire = 300; }
            case TYPE_BALEFIRE -> { if(props.balefire < 100) props.balefire = 100; }
            /* Schwarzes Feuer staut sich: wer darin stehen bleibt, sammelt weiter auf. */
            case TYPE_BLACK -> { if(props.blackFire < 200) props.blackFire = 200; else props.blackFire += 5; }
            default -> { }
        }
    }

    /** Das Feuer steht, wo es entstanden ist -- Stroemungen und Stoesse bewegen es nicht. */
    @Override public void push(double x, double y, double z) { }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected boolean canRide(Entity entity) { return false; }
    @Override public boolean displayFireAnimation() { return false; }

    /* Es wird nicht gespeichert: das Original loescht sich beim Laden selbst. */
    @Override protected void readAdditionalSaveData(CompoundTag tag) { this.discard(); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public boolean shouldBeSaved() { return false; }

    /** Setzt das Feuer an eine Stelle im Raum und gibt es zurueck, damit es sich spawnen laesst. */
    public FireLingering at(Vec3 pos) {
        this.setPos(pos.x, pos.y, pos.z);
        return this;
    }

    public FireLingering at(BlockPos pos) {
        this.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return this;
    }
}
