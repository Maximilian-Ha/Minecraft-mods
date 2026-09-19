package com.hbm.entity.projectile;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.particle.helper.ExplosionCreator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityTorpedo.
 *
 * Ein Torpedo, der vom Himmel faellt. Die Signaturpatrone des Protege ruft ihn fuenfzig Bloecke
 * ueber dem Getroffenen; von da an ist er nur noch Schwerkraft. Wo er aufsetzt, steht eine
 * gewoehnliche Explosion der Staerke zwanzig.
 *
 * ER FAELLT SCHNELLER ALS SEINE BEIDEN GESCHWISTER: zweieinhalb Bloecke je Tick statt
 * anderthalb, und er beschleunigt mit 0,04 statt 0,03. Ein Torpedo eben.
 *
 * ABWEICHUNG, wie bei Gueterwagen und Luftschiff: das Original streut beim Erscheinen fuenfzehn
 * Balefire-Partikel. Die Partikelart "bf" hat der Port nicht.
 */
public class Torpedo extends ProjectileNT {

    /** Schneller als das faellt er nicht. */
    private static final double HOECHSTFALL = -2.5;

    public Torpedo(EntityType<? extends Torpedo> type, Level level) {
        super(type, level);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }

    @Override
    public void tick() {

        this.setPos(this.getX() + this.getDeltaMovement().x,
                this.getY() + this.getDeltaMovement().y,
                this.getZ() + this.getDeltaMovement().z);

        this.setDeltaMovement(this.getDeltaMovement().x,
                Math.max(HOECHSTFALL, this.getDeltaMovement().y - 0.04),
                this.getDeltaMovement().z);

        if(this.level.isClientSide) return;
        if(this.level.getBlockState(BlockPos.containing(this.position())).isAir()) return;

        this.schlagEin();
    }

    private void schlagEin() {

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        this.discard();

        /* Der Feuerball sitzt einen Block ueber der Sprengstelle, die Sprengung selbst darauf
         * -- so steht es im Original. */
        ExplosionCreator.composeEffectStandard(this.level, x, y + 1, z);

        ExplosionVNT sprengung = new ExplosionVNT(this.level, x, y, z, 20F);
        sprengung.makeStandard();
        sprengung.explode();
    }

    /* Er faellt mit eigener Rechnung, nicht mit der von ProjectileNT. */
    @Override protected double getDefaultGravity() { return 0; }

    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 25000; }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
}
