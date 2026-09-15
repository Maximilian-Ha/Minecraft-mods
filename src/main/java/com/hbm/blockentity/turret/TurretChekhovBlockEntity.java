package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory50;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretChekhov.
 *
 * Die Gatling: .50 BMG, alle zwei Ticks ein Schuss -- aber erst, nachdem sie eine Sekunde lang
 * hochgelaufen ist. Das ist der Sinn des Zaehlers: er steigt, solange ein Ziel steht, und faellt
 * wieder, sobald keines mehr da ist. Wer nur kurz ins Blickfeld laeuft, wird nicht beschossen.
 *
 * Im Bild dreht sich der Laufkranz entsprechend: er beschleunigt, solange ein Ziel steht, und
 * laeuft sonst aus.
 */
public class TurretChekhovBlockEntity extends TurretDummyableBlockEntity {

    private static final List<Integer> CONFIGS = new ArrayList<>();

    public static void initAmmo() {
        if(!CONFIGS.isEmpty()) return;
        CONFIGS.add(XFactory50.bmg50_sp.id);
        CONFIGS.add(XFactory50.bmg50_fmj.id);
        CONFIGS.add(XFactory50.bmg50_jhp.id);
        CONFIGS.add(XFactory50.bmg50_ap.id);
        CONFIGS.add(XFactory50.bmg50_du.id);
    }

    protected int timer;
    /** Nur der Client: wie weit der Laufkranz gedreht ist und wie schnell er laeuft. */
    public float spin;
    public float lastSpin;
    private float accel;

    public TurretChekhovBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.TURRET_CHEKHOV.get(), pos, state);
    }

    public TurretChekhovBlockEntity(BlockEntityType<? extends TurretChekhovBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretChekhov"); }
    @Override protected List<Integer> getAmmoList() { return CONFIGS; }

    @Override public ResourceLocation getGuiTexture() { return NuclearTechMod.withDefaultNamespace("textures/gui/weapon/gui_turret_base.png"); }

    @Override public double getTurretElevation() { return 45D; }
    @Override public long getMaxPower() { return 10_000; }
    @Override public double getBarrelLength() { return 3.5D; }
    @Override public double getAcceptableInaccuracy() { return 15; }
    @Override public boolean usesCasings() { return true; }

    /** Ticks zwischen zwei Schuessen, sobald der Kranz auf Drehzahl ist. */
    public int getDelay() { return 2; }

    /** Der Schaden je Geschoss. */
    protected float getBulletDamage() { return 10F; }

    /** Welcher Ton beim Schuss. */
    protected SoundEvent getFireSound() { return NtmSoundEvents.TURRET_CHEKHOV_FIRE.get(); }

    @Override
    public void updateEntity() {

        super.updateEntity();

        if(this.level == null) return;

        if(this.level.isClientSide) {

            /* Der Kranz laeuft hoch, solange ein Ziel steht, und sonst aus. */
            if(this.tPos != null) this.accel = Math.min(45F, this.accel + 2);
            else this.accel = Math.max(0F, this.accel - 2);

            this.lastSpin = this.spin;
            this.spin += this.accel;

            if(this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }

        } else if(this.tPos == null) {

            /* Ohne Ziel faellt der Zaehler wieder -- aber nie unter null und nie ueber zwanzig. */
            this.timer--;
            if(this.timer > 20) this.timer = 20;
            if(this.timer < 0) this.timer = 0;
        }
    }

    @Override
    public void updateFiringTick() {

        if(this.level == null) return;

        this.timer++;

        if(this.timer <= 20 || this.timer % this.getDelay() != 0) return;

        BulletConfig conf = this.getFirstConfigLoaded();
        if(conf == null) return;

        this.cachedCasingConfig = conf.casing;
        this.spawnBullet(conf, this.getBulletDamage());
        this.consumeAmmo(conf.getAmmo());
        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), this.getFireSound(), SoundSource.BLOCKS, 2.0F, 1.0F);

        if(this.level instanceof ServerLevel serverLevel) {
            Vec3 pos = this.getTurretPos().add(rotate(new Vec3(this.getBarrelLength(), 0, 0), this.rotationPitch, this.rotationYaw));
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0D, 0D, 0D, 0D);
        }
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        return this.getTurretPos().add(rotate(new Vec3(-1.125, 0.125, 0.25), this.rotationPitch, this.rotationYaw));
    }

    @Override
    protected Vec3 getCasingMotion() { return new Vec3(-0.8, 0.8, 0); }
}
