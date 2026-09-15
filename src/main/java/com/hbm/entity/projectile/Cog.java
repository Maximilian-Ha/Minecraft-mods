package com.hbm.entity.projectile;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityCog.
 *
 * Das Zahnrad, das ein ueberdrehter Stirlingmotor auswirft. Solange der
 * Orientierungswert unter 6 liegt, rollt es und zerlegt alles auf seinem Weg;
 * ab 6 liegt es flach und kann aufgesammelt werden. Die Zahl ist die
 * Seitenkennziffer aus 1.7.10 (NORTH 2, SOUTH 3, WEST 4, EAST 5), damit der
 * Renderer unveraendert bleiben kann.
 *
 * Aufgebaut wie Sawblade: EntityThrowableInterp gibt es im Port nicht, das
 * Gegenstueck ist ProjectileNT.
 *
 * PacketThreading und AuxParticlePacketNT fehlen im Port. Gebraucht wird davon
 * nur das Giblet-Partikelpaket, und dafuer gibt es AuxParticle plus
 * PacketDistributor.sendToPlayersNear -- genau wie in Sawblade. Ein eigener
 * IParticleCreator-Aufruf waere hier falsch, weil der Partikeltyp "giblets" die
 * Entitaets-ID braucht und ueber AuxParticle bereits vorhanden ist.
 */
public class Cog extends ProjectileNT {

    private static final EntityDataAccessor<Integer> ORIENTATION = SynchedEntityData.defineId(Cog.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> META = SynchedEntityData.defineId(Cog.class, EntityDataSerializers.INT);

    /** Liegt das Zahnrad fest? Ersatz fuer EntityThrowableNT.inGround. */
    private boolean inGround = false;

    public Cog(EntityType<? extends Cog> type, Level level) {
        super(type, level);
    }

    public Cog(Level level, double x, double y, double z) {
        super(NtmEntityTypes.COG.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ORIENTATION, 0);
        builder.define(META, 0);
    }

    public Cog setOrientation(int rot) {
        this.entityData.set(ORIENTATION, rot);
        return this;
    }

    public Cog setMeta(int meta) {
        this.entityData.set(META, meta);
        return this;
    }

    public int getOrientation() {
        return this.entityData.get(ORIENTATION);
    }

    public int getMeta() {
        return this.entityData.get(META);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {

        if(!this.level.isClientSide) {

            if(player.getInventory().add(MetaHelper.newStack(NtmItems.GEAR_LARGE.get(), 1, this.getMeta()))) {
                this.discard();
            }

            player.containerMenu.broadcastChanges();
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult ehr) {

        Entity e = ehr.getEntity();

        if(!e.isAlive()) return;

        e.hurt(e.damageSources().source(NtmDamageTypes.RUBBLE), 1000);

        if(!e.isAlive() && e instanceof LivingEntity && this.level instanceof ServerLevel serverLevel) {
            CompoundTag vdat = new CompoundTag();
            vdat.putString("type", "giblets");
            vdat.putInt("ent", e.getId());
            vdat.putInt("cDiv", 5);

            double px = e.getX();
            double py = e.getY() + e.getBbHeight() * 0.5;
            double pz = e.getZ();

            PacketDistributor.sendToPlayersNear(serverLevel, null, px, py, pz, 150, new AuxParticle(vdat, px, py, pz));

            SoundUtils.playAtVec3(serverLevel, new Vec3(e.getX(), e.getY(), e.getZ()),
                    SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS,
                    2.0F, 0.95F + serverLevel.random.nextFloat() * 0.2F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult bhr) {
        super.onHitBlock(bhr);

        if(this.tickCount <= 1) return;

        int orientation = this.entityData.get(ORIENTATION);

        if(orientation < 6) {

            Vec3 motion = this.getDeltaMovement();

            if(motion.length() < 0.75) {
                this.entityData.set(ORIENTATION, orientation + 6);
                orientation += 6;
            } else {
                Direction side = bhr.getDirection();
                this.setDeltaMovement(
                        motion.x * (1 - (Math.abs(side.getStepX()) * 2)),
                        motion.y * (1 - (Math.abs(side.getStepY()) * 2)),
                        motion.z * (1 - (Math.abs(side.getStepZ()) * 2)));

                // Das Original ruft createExplosion(..., 3F, false); der letzte Parameter ist
                // isSmoking und steuert in 1.7.10 den Blockschaden -- false heisst KEINER.
                // Zerstoert wird nur der eine getroffene Block, wenn seine Sprengfestigkeit
                // unter 50 liegt. Mit TNT waere daraus ein Krater geworden.
                this.level.explode(this, this.getX(), this.getY(), this.getZ(), 3F, Level.ExplosionInteraction.NONE);

                BlockPos hit = bhr.getBlockPos();
                if(this.level.getBlockState(hit).getBlock().getExplosionResistance() < 50) {
                    this.level.destroyBlock(hit, false);
                }
            }
        }

        if(orientation >= 6) {
            this.setDeltaMovement(Vec3.ZERO);
            this.inGround = true;
        }
    }

    @Override
    public void tick() {

        if(!this.level.isClientSide) {
            int orientation = this.entityData.get(ORIENTATION);
            if(orientation >= 6 && !this.inGround) {
                this.entityData.set(ORIENTATION, orientation - 6);
            }
        }

        super.tick();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected double getDefaultGravity() {
        return this.inGround ? 0 : 0.03;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("rot", this.getOrientation());
        tag.putInt("meta", this.getMeta());
        // Zusaetzlich zum Original: inGround steckte in 1.7.10 in EntityThrowableNT,
        // im Port ist es ein eigenes Feld und muss selbst gesichert werden.
        tag.putBoolean("inGround", this.inGround);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setOrientation(tag.getInt("rot"));
        this.setMeta(tag.getInt("meta"));
        this.inGround = tag.getBoolean("inGround");
    }
}
