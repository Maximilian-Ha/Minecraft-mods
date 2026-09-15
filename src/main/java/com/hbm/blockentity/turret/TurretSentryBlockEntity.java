package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory9mm;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretSentry.
 *
 * Der Wachturm: der kleinste Turm des Spiels, ein einzelner Block mit zwei Laeufen, der
 * 9-mm-Munition verschiesst. Er sieht keine Unsichtbaren, reicht nur vierundzwanzig Bloecke weit
 * und braucht kaum Strom -- fuenf Einheiten je Tick aus einem Speicher von tausend.
 *
 * Er feuert abwechselnd links und rechts, alle zehn Ticks einen Schuss. Der abgefeuerte Lauf
 * faehrt im Bild zurueck und schiebt sich langsamer wieder vor; das laeuft ganz auf dem Client,
 * angestossen von einem Merker im Netzpaket.
 */
public class TurretSentryBlockEntity extends TurretBaseBlockEntity {

    protected boolean didJustShootLeft = false;
    protected boolean retractingLeft = false;
    public double barrelLeftPos = 0;
    public double lastBarrelLeftPos = 0;
    protected boolean didJustShootRight = false;
    protected boolean retractingRight = false;
    public double barrelRightPos = 0;
    public double lastBarrelRightPos = 0;

    /** Welcher Lauf als naechstes dran ist. */
    protected boolean shotSide = false;
    protected int timer;

    private static final List<Integer> CONFIGS = new ArrayList<>();

    /** Wird einmal aus GunFactory heraus gefuellt, sobald die Patronen stehen. */
    public static void initAmmo() {
        if(!CONFIGS.isEmpty()) return;
        CONFIGS.add(XFactory9mm.p9_sp.id);
        CONFIGS.add(XFactory9mm.p9_fmj.id);
        CONFIGS.add(XFactory9mm.p9_jhp.id);
        CONFIGS.add(XFactory9mm.p9_ap.id);
    }

    public TurretSentryBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.TURRET_SENTRY.get(), pos, state);
    }

    public TurretSentryBlockEntity(BlockEntityType<? extends TurretSentryBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretSentry"); }

    @Override protected List<Integer> getAmmoList() { return CONFIGS; }

    @Override public ResourceLocation getGuiTexture() { return NuclearTechMod.withDefaultNamespace("textures/gui/weapon/gui_turret_sentry.png"); }

    @Override public double getTurretDepression() { return 20D; }
    @Override public double getTurretElevation() { return 20D; }
    @Override public int getDetectorInterval() { return 10; }
    @Override public double getDetectorRange() { return 24D; }
    @Override public double getDetectorGrace() { return 2D; }
    @Override public long getMaxPower() { return 1_000; }
    @Override public long getConsumption() { return 5; }
    @Override public double getBarrelLength() { return 1.25D; }
    @Override public double getAcceptableInaccuracy() { return 15; }
    @Override public boolean hasThermalVision() { return false; }
    @Override public boolean usesCasings() { return true; }

    @Override
    public void updateEntity() {

        if(this.level != null && this.level.isClientSide) {

            this.lastBarrelLeftPos = this.barrelLeftPos;
            this.lastBarrelRightPos = this.barrelRightPos;

            /* Zurueck schnellt er doppelt so schnell, wie er wieder vorkommt. */
            float retractSpeed = 0.5F;
            float pushSpeed = 0.25F;

            if(this.retractingLeft) {
                this.barrelLeftPos += retractSpeed;
                if(this.barrelLeftPos >= 1) this.retractingLeft = false;
            } else {
                this.barrelLeftPos -= pushSpeed;
                if(this.barrelLeftPos < 0) this.barrelLeftPos = 0;
            }

            if(this.retractingRight) {
                this.barrelRightPos += retractSpeed;
                if(this.barrelRightPos >= 1) this.retractingRight = false;
            } else {
                this.barrelRightPos -= pushSpeed;
                if(this.barrelRightPos < 0) this.barrelRightPos = 0;
            }
        }

        super.updateEntity();
    }

    @Override
    public void updateFiringTick() {

        if(this.level == null) return;

        this.timer++;

        if(this.timer % 10 != 0) return;

        BulletConfig conf = this.getFirstConfigLoaded();
        if(conf == null) return;

        this.cachedCasingConfig = conf.casing;
        this.spawnBullet(conf, 5F);
        this.consumeAmmo(conf.getAmmo());
        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(this.getBlockPos()), NtmSoundEvents.TURRET_SENTRY_FIRE.get(), SoundSource.BLOCKS, 2.0F, 1.0F);

        this.spawnMuzzleParticle();

        if(this.shotSide) this.didJustShootLeft = true;
        else this.didJustShootRight = true;

        this.shotSide = !this.shotSide;
    }

    /** Der Blitz an der Muendung -- die grosse Explosionswolke der Vanilla-Partikel. */
    protected void spawnMuzzleParticle() {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        Vec3 pos = this.getTurretPos();
        Vec3 vec = rotate(new Vec3(this.getBarrelLength(), 0, 0), this.rotationPitch, this.rotationYaw);
        Vec3 side = new Vec3(0.125 * (this.shotSide ? 1 : -1), 0, 0).yRot((float) -this.rotationYaw);

        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                pos.x + vec.x + side.x, pos.y + vec.y, pos.z + vec.z + side.z, 1, 0D, 0D, 0D, 0D);
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        Vec3 pos = this.getTurretPos();
        Vec3 vec = rotate(new Vec3(0, 0.25, -0.125), this.rotationPitch, this.rotationYaw);
        return pos.add(vec);
    }

    @Override
    protected void seekNewTarget() {

        Entity lastTarget = this.target;
        super.seekNewTarget();

        if(this.level != null && lastTarget != this.target && this.target != null) {
            SoundUtils.playAtVec3(this.level, this.target.position(), NtmSoundEvents.TURRET_SENTRY_LOCKON.get(), SoundSource.BLOCKS, 2.0F, 1.5F);
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.didJustShootLeft);
        buf.writeBoolean(this.didJustShootRight);
        this.didJustShootLeft = false;
        this.didJustShootRight = false;
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.retractingLeft = buf.readBoolean();
        this.retractingRight = buf.readBoolean();
    }

    /** Der Wachturm haengt nur an dem Block unter sich. */
    @Override
    protected void updateConnections() {
        if(this.level == null) return;
        this.trySubscribe(this.level, new DirPos(this.getBlockPos().below(), Library.NEG_Y));
    }
}
