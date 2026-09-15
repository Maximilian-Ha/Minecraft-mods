package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryTurret;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretHoward.
 *
 * Das Nahbereichsgeschuetz: zwei Gatlings uebereinander, zweihundertfuenfzig Bloecke Reichweite,
 * und es dreht schnell genug, um Raketen im Flug zu treffen -- zwoelf Grad je Tick seitlich, acht
 * hoch, und der Lauf geht bis senkrecht.
 *
 * Es schiesst KEINE Geschosse. Es trifft unmittelbar, mit einer festen Wahrscheinlichkeit je
 * Schuss; was man sieht, sind nur Muendungsfeuer und Huelsen. Das ist im Original so gedacht:
 * Geschosse waeren bei dieser Kadenz nicht zu berechnen.
 *
 * Ein Gurt fasst zweihundert Schuss und wird aus einem Munitionsfach nachgeladen; das dauert,
 * und man hoert es.
 */
public class TurretHowardBlockEntity extends TurretDummyableBlockEntity {

    /** Wie viele von hundert Schuessen treffen. Im Original ein Konfigurationswert. */
    public static final int HIT_RATE = 50;

    public static final SpentCasing CASING_DGK = new SpentCasing(SpentCasingType.STRAIGHT)
            .setScale(1.5F).setBounceMotion(1F, 0.5F).setColor(SpentCasing.COLOR_CASE_BRASS).register("DGK");

    private static final List<Integer> CONFIGS = new ArrayList<>();

    public static void initAmmo() {
        if(!CONFIGS.isEmpty()) return;
        CONFIGS.add(XFactoryTurret.dgk_normal.id);
    }

    protected int loaded;
    protected int timer;
    /** Nur der Client: wie weit die Laeufe schon herumgedreht sind. */
    public float spin;
    public float lastSpin;

    public TurretHowardBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.TURRET_HOWARD.get(), pos, state);
    }

    public TurretHowardBlockEntity(BlockEntityType<? extends TurretHowardBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretHoward"); }
    @Override protected List<Integer> getAmmoList() { return CONFIGS; }

    @Override public ResourceLocation getGuiTexture() { return NuclearTechMod.withDefaultNamespace("textures/gui/weapon/gui_turret_howard.png"); }

    @Override public double getHeightOffset() { return 2.25D; }
    @Override public double getDetectorGrace() { return 3D; }
    @Override public double getDetectorRange() { return 250D; }
    @Override public double getTurretYawSpeed() { return 12D; }
    @Override public double getTurretPitchSpeed() { return 8D; }
    @Override public double getTurretElevation() { return 90D; }
    @Override public double getTurretDepression() { return 50D; }
    @Override public double getBarrelLength() { return 3.25D; }
    @Override public long getMaxPower() { return 50_000; }
    @Override public long getConsumption() { return 500; }
    @Override public boolean usesCasings() { return true; }

    @Override
    public void updateEntity() {

        if(this.level != null && this.level.isClientSide) {

            this.lastSpin = this.spin;

            /* Die Laeufe drehen sich, solange ein Ziel steht -- auch wenn gerade nicht gefeuert wird. */
            if(this.tPos != null) this.spin += 45;

            if(this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }

        } else if(this.level != null) {

            if(this.loaded <= 0) {
                BulletConfig conf = this.getFirstConfigLoaded();
                if(conf != null) {
                    this.consumeAmmo(conf.getAmmo());
                    SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_HOWARD_RELOAD.get(), SoundSource.BLOCKS, 4.0F, 1F);
                    this.loaded = 200;
                }
            }
        }

        super.updateEntity();
    }

    @Override
    public void updateFiringTick() {

        if(this.level == null) return;

        this.timer++;

        if(this.loaded <= 0 || this.tPos == null || this.target == null) return;

        /* Zwei Laeufe, zwei Schuesse -- darum zweimal derselbe Ton mit leicht anderer Hoehe. */
        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_HOWARD_FIRE.get(), SoundSource.BLOCKS, 4.0F, 0.9F + this.level.random.nextFloat() * 0.3F);
        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_HOWARD_FIRE.get(), SoundSource.BLOCKS, 4.0F, 1.0F + this.level.random.nextFloat() * 0.3F);

        for(int i = 0; i < 2; i++) {
            this.cachedCasingConfig = CASING_DGK;
            this.spawnCasing();
        }

        if(this.timer % 2 != 0) return;

        this.loaded--;

        if(this.level.random.nextInt(100) + 1 <= HIT_RATE) {
            EntityDamageUtil.hurtIgnoreIFrame(this.target,
                    this.level.damageSources().source(NtmDamageTypes.SHRAPNEL),
                    2F + this.level.random.nextInt(2));
        }

        this.spawnMuzzleParticles();
    }

    /** Das Muendungsfeuer sitzt an beiden Laeufen, einen Viertelblock ueber und unter der Achse. */
    private void spawnMuzzleParticles() {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        Vec3 pos = this.getTurretPos().add(rotate(new Vec3(this.getBarrelLength(), 0, 0), this.rotationPitch, this.rotationYaw));
        Vec3 hOff = rotate(new Vec3(0, 0.25, 0), this.rotationPitch, this.rotationYaw);

        for(int i = 0; i < 2; i++) {
            Vec3 muzzle = i == 0 ? pos.add(hOff) : pos.subtract(hOff);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, muzzle.x, muzzle.y, muzzle.z, 1, 0D, 0D, 0D, 0D);
        }
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        return this.getTurretPos().add(rotate(new Vec3(-0.875, 0.2, -0.125), this.rotationPitch, this.rotationYaw));
    }

    @Override
    protected Vec3 getCasingMotion() { return new Vec3(0.4, 0, 0); }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.loaded);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.loaded = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.loaded = tag.getInt("loaded");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("loaded", this.loaded);
    }
}
