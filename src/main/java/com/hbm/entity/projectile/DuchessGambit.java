package com.hbm.entity.projectile;

import com.hbm.blocks.NtmBlocks;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.SoundUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityDuchessGambit.
 *
 * Ein Luftschiff, das vom Himmel faellt -- dasselbe Spiel wie beim Gueterwagen, nur eine
 * Nummer groesser. Die Signaturpatrone der schoenen Autoschrotflinte ruft es fuenfzig Bloecke
 * ueber dem Getroffenen herbei.
 *
 * WAS BEIM AUFSCHLAG PASSIERT: ein Nebelhorn, tausend Punkte Schaden in einem langgezogenen
 * Kasten (zehn Bloecke breit, achtzehn lang -- es ist ein Schiff), fuenf Explosionen entlang
 * seiner Laengsachse, fuenf Druckwellen uebereinander, und wo es liegen bleibt, ein
 * Schiffsblock.
 *
 * ABWEICHUNG: wie beim Gueterwagen streut das Original beim Erscheinen fuenfzig
 * Balefire-Partikel; die Partikelart "bf" hat der Port nicht.
 */
public class DuchessGambit extends ProjectileNT {

    private static final double HOECHSTFALL = -1.5;

    public DuchessGambit(EntityType<? extends DuchessGambit> type, Level level) {
        super(type, level);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }

    @Override
    public void tick() {

        this.setPos(this.getX() + this.getDeltaMovement().x,
                this.getY() + this.getDeltaMovement().y,
                this.getZ() + this.getDeltaMovement().z);

        this.setDeltaMovement(this.getDeltaMovement().x,
                Math.max(HOECHSTFALL, this.getDeltaMovement().y - 0.03),
                this.getDeltaMovement().z);

        if(this.level.isClientSide) return;
        if(this.level.getBlockState(BlockPos.containing(this.position())).isAir()) return;

        this.schlagEin();
    }

    private void schlagEin() {

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        SoundUtils.playAtVec3(this.level, this.position(), NtmSoundEvents.GAMBIT.get(), SoundSource.HOSTILE, 10_000.0F, 1.0F);
        this.discard();

        DamageSource quelle = this.level.damageSources().source(NtmDamageTypes.BOAT, this, this.getOwner());

        for(Entity getroffen : this.level.getEntities(this, new AABB(x - 5, y - 2, z - 9, x + 5, y + 2, z + 9))) {
            EntityDamageUtil.hurtIgnoreIFrame(getroffen, quelle, 1000F);
        }

        if(this.level instanceof ServerLevel serverLevel) {

            /* Fuenf Explosionen entlang des Rumpfes, von Heck zu Bug. */
            for(int versatz = -6; versatz <= 6; versatz += 3) {
                ExplosionLarge.explode(serverLevel, x, y, z + versatz, 2, true, false, false);
            }

            for(double staerke = 3; staerke >= 1; staerke -= 0.5) {
                ExplosionLarge.spawnShock(serverLevel, x, y + 1, z, 24, staerke);
            }
        }

        this.level.setBlockAndUpdate(BlockPos.containing(x - 0.5, y + 0.5, z - 0.5), NtmBlocks.BOAT.get().defaultBlockState());
    }

    @Override protected double getDefaultGravity() { return 0; }

    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 25000; }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
}
