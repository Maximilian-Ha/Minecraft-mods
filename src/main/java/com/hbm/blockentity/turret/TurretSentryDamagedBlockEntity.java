package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory9mm;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretSentryDamaged.
 *
 * Der zerschossene Wachturm, wie er in verlassenen Anlagen steht. Er braucht keinen Strom und
 * laesst sich nicht ausschalten -- er laeuft einfach. Dafuer dreht er langsamer, sein rechter Lauf
 * ist verbogen und feuert nur noch trocken, und er schiesst auf alles, was lebt: die Zielschalter
 * gelten fuer ihn nicht.
 *
 * Seine Munition kommt nicht aus den Faechern, sondern aus dem Nichts -- dafuer immer dieselbe.
 */
public class TurretSentryDamagedBlockEntity extends TurretSentryBlockEntity {

    public TurretSentryDamagedBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.TURRET_SENTRY_DAMAGED.get(), pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretSentryDamaged"); }

    @Override public boolean hasPower() { return true; }
    @Override public boolean isOn() { return true; }
    @Override public double getTurretYawSpeed() { return 3D; }
    @Override public double getTurretPitchSpeed() { return 2D; }
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

        if(this.timer % 10 != 0) return;

        BulletConfig conf = XFactory9mm.p9_fmj;
        if(conf == null) return;

        this.cachedCasingConfig = conf.casing;

        if(this.shotSide) {

            SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_SENTRY_FIRE.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
            this.spawnBullet(conf, 5F);
            this.spawnMuzzleParticle();

        } else {

            /* Der verbogene Lauf klickt nur und wirft eine Huelse aus. */
            SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_SENTRY_FIRE.get(), SoundSource.BLOCKS, 2.0F, 0.75F);
            this.spawnCasing();
        }

        if(this.shotSide) this.didJustShootLeft = true;
        else this.didJustShootRight = true;

        this.shotSide = !this.shotSide;
    }
}
