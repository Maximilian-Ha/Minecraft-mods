package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretHowardDamaged.
 *
 * Das verrostete Nahbereichsgeschuetz aus verlassenen Anlagen. Es braucht keinen Strom, keine
 * Munition und keinen Schalter -- und laesst sich auch nicht oeffnen: es hat keine Oberflaeche.
 * Dafuer reicht es nur sechzehn Bloecke weit, dreht viermal langsamer und trifft halb so oft.
 *
 * Es schiesst auf alles, was lebt.
 */
public class TurretHowardDamagedBlockEntity extends TurretHowardBlockEntity {

    public TurretHowardDamagedBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.TURRET_HOWARD_DAMAGED.get(), pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretHowardDamaged"); }

    @Override public boolean hasPower() { return true; }
    @Override public boolean isOn() { return true; }
    @Override public double getTurretYawSpeed() { return 3D; }
    @Override public double getTurretPitchSpeed() { return 2D; }
    @Override public double getDetectorRange() { return 16D; }
    @Override public double getDetectorGrace() { return 5D; }
    @Override public boolean hasThermalVision() { return false; }

    @Override
    public boolean entityAcceptableTarget(Entity e) {
        if(e instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return e instanceof LivingEntity && e.isAlive();
    }

    @Override
    public void updateFiringTick() {

        if(this.level == null) return;

        this.timer++;

        if(this.tPos == null || this.target == null) return;
        if(this.timer % 4 != 0) return;

        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_HOWARD_FIRE.get(), SoundSource.BLOCKS, 4.0F, 0.7F + this.level.random.nextFloat() * 0.3F);

        this.cachedCasingConfig = CASING_DGK;
        this.spawnCasing();

        if(this.level.random.nextInt(100) + 1 <= HIT_RATE * 0.5) {
            EntityDamageUtil.hurtIgnoreIFrame(this.target,
                    this.level.damageSources().source(NtmDamageTypes.SHRAPNEL),
                    2F + this.level.random.nextInt(2));
        }

        if(this.level instanceof ServerLevel serverLevel) {
            Vec3 pos = this.getTurretPos()
                    .add(rotate(new Vec3(this.getBarrelLength(), 0, 0), this.rotationPitch, this.rotationYaw))
                    .add(rotate(new Vec3(0, 0.25, 0), this.rotationPitch, this.rotationYaw));
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0D, 0D, 0D, 0D);
        }
    }

    /** Ohne Oberflaeche: das Wrack laesst sich nicht bestuecken. */
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return null; }
}
