package com.hbm.entity.missile;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.items.ISatChip;
import com.hbm.items.NtmItems;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.saveddata.satellite.XSatelliteRegistry;
import com.hbm.util.SoundUtils;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Soyuz extends Entity {

    public static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(Soyuz.class, EntityDataSerializers.INT);

    private double acceleration = 0.00D;
    public int mode;
    public int targetX;
    public int targetZ;
    private boolean memed = false;

    private final NonNullList<ItemStack> payload;

    public Soyuz(EntityType<? extends Soyuz> entityType, Level level) {
        super(entityType, level);

        this.noCulling = true;
        this.payload = NonNullList.withSize(18, ItemStack.EMPTY);
    }

    public Soyuz(Level level) { this(NtmEntityTypes.SOYUZ_MISSILE.get(), level); }

    @Override
    public void tick() {

        if(this.getDeltaMovement().y < 2.0D) {
            acceleration += 0.00025D;
            this.setDeltaMovement(this.getDeltaMovement().add(0, acceleration, 0));
        }

        this.moveTo(this.position().add(this.getDeltaMovement()), 0F, 0F);

        if(!level.isClientSide) {
            List<Entity> entities = this.level.getEntities(this, new AABB(this.position().add(-5, -15, -5), this.position().add(5, 0, 5)));

            for(Entity e : entities) {
                e.igniteForSeconds(15);
                e.hurt(e.damageSources().source(NtmDamageTypes.EXHAUST), 100F);

                if(e instanceof Player) {
                    if(!memed) {
                        memed = true;
                        SoundUtils.playAtVec3(this.level, this.position(), NtmSoundEvents.ALARM_SOYUZED.get(), SoundSource.AMBIENT, 100F, 1F);
                    }

                    if(e instanceof ServerPlayer spieler) NtmCriteria.marke(spieler, "soyuz");
                }
            }
        } else {
            this.spawnExhaust(this.position());
            this.spawnExhaust(this.position().add(2.75, 0.0, 0.0));
            this.spawnExhaust(this.position().add(-2.75, 0.0, 0.0));
            this.spawnExhaust(this.position().add(0.0, 0.0, 2.75));
            this.spawnExhaust(this.position().add(0.0, 0.0, -2.75));
        }

        if(this.position().y > 600) this.deployPayload();
    }

    private void spawnExhaust(Vec3 position) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("count", 1);
        tag.putDouble("width", this.level.random.nextDouble() * 0.25 - 0.5);

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.EXHAUST_SOYUZ.get(), tag), position.x, position.y, position.z, 350.0);
    }

    private void deployPayload() {

        if(mode == 0) {

            ItemStack stack = this.payload.get(0);

            // NEUNZIG MILLIONEN DOLLAR FUER EIN PLUESCHPONY. Das Original laesst die
            // Rakete in diesem Fall nichts weiter tun, als fuenfundzwanzig Leuchtspuren
            // zu versprengen -- und gibt JEDEM Spieler der Welt den Erfolg, nicht nur dem,
            // der gestartet hat.
            if(stack.is(NtmItems.FLAME_PONY.get()) && this.level instanceof ServerLevel serverLevel) {
                ExplosionLarge.spawnTracers(serverLevel, this.getX(), this.getY(), this.getZ(), 25);
                NtmCriteria.markeFuerAlle(serverLevel, "space");
            }

            if(stack.getItem() instanceof ISatChip) {
                int freq = ISatChip.getFreqS(stack);
                if(this.level instanceof ServerLevel serverLevel) {
                    XSatelliteRegistry.orbit(serverLevel, stack, freq, this.getX(), this.getY(), this.getZ());
                }
            }
        }

        // MODUS 1 IST DER RUECKFLUG: statt in den Orbit zu gehen, setzt die Rakete ihre
        // Nutzlast ueber dem Zielpunkt ab. Das Original laedt dabei den Zielchunk von Hand
        // (provider.loadChunk), damit die Kapsel nicht ins Leere faellt. getChunk tut
        // dasselbe -- es laedt synchron und laesst den Chunk danach wieder los, waehrend
        // setChunkForced ihn dauerhaft festhielte.
        if(mode == 1 && this.level() instanceof ServerLevel serverLevel) {

            ChunkPos chunk = new ChunkPos(new BlockPos(this.targetX, 0, this.targetZ));
            serverLevel.getChunk(chunk.x, chunk.z);

            SoyuzCapsule kapsel = new SoyuzCapsule(serverLevel);
            kapsel.setPayload(this.payload);
            kapsel.soyuz = this.getSkin();
            kapsel.setPos(this.targetX + 0.5, 600, this.targetZ + 0.5);
            serverLevel.addFreshEntity(kapsel);
        }

        this.discard();
    }

    public void setSat(ItemStack stack) {
        this.payload.set(0, stack);
    }

    public void setPayload(NonNullList<ItemStack> payload) {
        for(int i = 0; i < payload.size(); i++) {
            this.payload.set(i, payload.get(i));
        }
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(SKIN, 0);
    }

    public void setSkin(int i) {
        this.entityData.set(SKIN, i);
    }

    public int getSkin() {
        return this.entityData.get(SKIN);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        this.setSkin(tag.getInt("Skin"));
        this.targetX = tag.getInt("TargetX");
        this.targetZ = tag.getInt("TargetZ");
        this.mode = tag.getInt("Mode");

        ContainerHelper.loadAllItems(tag, this.payload, this.registryAccess());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

        tag.putInt("Skin", this.getSkin());
        tag.putInt("TargetX", this.targetX);
        tag.putInt("TargetZ", this.targetZ);
        tag.putInt("Mode", this.mode);

        ContainerHelper.saveAllItems(tag, this.payload, this.registryAccess());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 500000;
    }
}
