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
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityBoxcar.
 *
 * Ein Gueterwagen, der vom Himmel faellt. Der Lilmac ruft ihn mit seiner Signaturpatrone
 * fuenfzig Bloecke ueber dem Ziel herbei; von da an ist er nur noch Schwerkraft.
 *
 * WAS BEIM AUFSCHLAG PASSIERT: ein schwerer Knall, drei Druckwellen uebereinander, tausend
 * Punkte Schaden auf alles im Umkreis von zwei Bloecken -- absolut und ruestungsdurchdringend,
 * dagegen hilft nichts -- und dort, wo er liegen bleibt, steht danach ein Gueterwagenblock.
 *
 * ABWEICHUNG: das Original streut beim Erscheinen fuenfzig Balefire-Partikel um den Wagen. Die
 * Partikelart "bf" hat der Port nicht; der Wagen faellt hier ohne dieses Vorspiel.
 *
 * ABWEICHUNG, die Steuerung des Falls: das Original setzt Ort und Geschwindigkeit von Hand und
 * umgeht damit die uebliche Bewegung. Hier genuegt die Schwerkraft von ProjectileNT, begrenzt
 * auf dieselbe Endgeschwindigkeit von anderthalb Bloecken je Tick.
 */
public class Boxcar extends ProjectileNT {

    /** Schneller als das faellt er nicht, so wie im Original. */
    private static final double HOECHSTFALL = -1.5;

    public Boxcar(EntityType<? extends Boxcar> type, Level level) {
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

        SoundUtils.playAtVec3(this.level, this.position(), NtmSoundEvents.TRAIN_IMPACT.get(), SoundSource.BLOCKS, 100.0F, 1.0F);
        this.discard();

        if(this.level instanceof ServerLevel serverLevel) {
            /* Drei Wellen mit abnehmender Staerke -- das Original hat zwei weitere, die es
             * selbst auskommentiert hat. */
            ExplosionLarge.spawnShock(serverLevel, x, y + 1, z, 24, 3);
            ExplosionLarge.spawnShock(serverLevel, x, y + 1, z, 24, 2.5);
            ExplosionLarge.spawnShock(serverLevel, x, y + 1, z, 24, 2);
        }

        DamageSource quelle = this.level.damageSources().source(NtmDamageTypes.BOXCAR, this, this.getOwner());

        for(Entity getroffen : this.level.getEntities(this, new AABB(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2))) {
            EntityDamageUtil.hurtIgnoreIFrame(getroffen, quelle, 1000F);
        }

        this.level.setBlockAndUpdate(BlockPos.containing(x, y + 0.5, z), NtmBlocks.BOXCAR.get().defaultBlockState());
    }

    /* Er faellt mit eigener Rechnung, nicht mit der von ProjectileNT. */
    @Override protected double getDefaultGravity() { return 0; }

    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 25000; }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
}
