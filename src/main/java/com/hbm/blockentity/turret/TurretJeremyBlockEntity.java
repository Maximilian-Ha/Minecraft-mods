package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryTurret;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretJeremy.
 *
 * Der Kanonenturm. Eine einzelne 240-mm-Kanone auf einem 2x2-Fuss: achtzig Bloecke Reichweite,
 * alle zwei Sekunden ein Schuss, fuenfzig Grundschaden. Unter sechzehn Bloecken sieht er gar
 * nichts -- so nah kann er den Lauf nicht mehr senken.
 *
 * Die Huelse faellt erst zweiundzwanzig Ticks nach dem Schuss, dann aber nach HINTEN und UNTEN aus
 * dem Verschluss; eine Sekunde spaeter ist der Nachlader zu hoeren.
 */
public class TurretJeremyBlockEntity extends TurretDummyableBlockEntity {

    private static final List<Integer> CONFIGS = new ArrayList<>();

    public static void initAmmo() {
        if(!CONFIGS.isEmpty()) return;
        CONFIGS.add(XFactoryTurret.shell_normal.id);
        CONFIGS.add(XFactoryTurret.shell_explosive.id);
        CONFIGS.add(XFactoryTurret.shell_ap.id);
        CONFIGS.add(XFactoryTurret.shell_du.id);
        CONFIGS.add(XFactoryTurret.shell_w9.id);
    }

    private int timer;
    private int reload;

    public TurretJeremyBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.TURRET_JEREMY.get(), pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretJeremy"); }
    @Override protected List<Integer> getAmmoList() { return CONFIGS; }

    @Override public ResourceLocation getGuiTexture() { return NuclearTechMod.withDefaultNamespace("textures/gui/weapon/gui_turret_cannon.png"); }

    @Override public double getDetectorGrace() { return 16D; }
    @Override public double getDetectorRange() { return 80D; }
    @Override public double getTurretDepression() { return 45D; }
    @Override public double getBarrelLength() { return 4.25D; }
    @Override public long getMaxPower() { return 10_000; }
    @Override public boolean usesCasings() { return true; }
    @Override public int casingDelay() { return 22; }

    @Override
    public void updateEntity() {

        if(this.reload > 0) this.reload--;

        if(this.reload == 1 && this.level != null) {
            SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_JEREMY_RELOAD.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }

        super.updateEntity();
    }

    @Override
    public void updateFiringTick() {

        if(this.level == null) return;

        this.timer++;

        if(this.timer % 40 != 0) return;

        BulletConfig conf = this.getFirstConfigLoaded();
        if(conf == null) return;

        this.cachedCasingConfig = conf.casing;
        this.spawnBullet(conf, 50F);
        this.consumeAmmo(conf.getAmmo());
        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_JEREMY_FIRE.get(), SoundSource.BLOCKS, 4.0F, 1.0F);

        this.reload = 20;

        if(this.level instanceof ServerLevel serverLevel) {
            Vec3 pos = this.getTurretPos().add(rotate(new Vec3(this.getBarrelLength(), 0, 0), this.rotationPitch, this.rotationYaw));
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 5, 0D, 0D, 0D, 0D);
        }
    }

    /** Die Huelse faellt raucht nach hinten unten aus dem Verschluss, nicht zur Seite. */
    @Override
    protected void spawnCasing() {

        if(this.level == null || this.cachedCasingConfig == null) return;

        Vec3 spawn = this.getCasingSpawnPos();

        CasingCreator.composeEffect(this.level, spawn.x, spawn.y, spawn.z,
                (float) Math.toDegrees(this.rotationYaw), (float) Math.toDegrees(-this.rotationPitch),
                -0.2, -0.2, 0, 0.01,
                -5F, 0F, this.cachedCasingConfig.getName(), true, 100, 0.5D, 20);

        this.cachedCasingConfig = null;
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        return this.getTurretPos().add(rotate(new Vec3(-2, 0, 0), this.rotationPitch, this.rotationYaw));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.reload = tag.getInt("reload");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("reload", this.reload);
    }
}
