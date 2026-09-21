package com.hbm.entity.effect;

import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.effect.EntitySpear.
 *
 * Der Digamma-Speer. Eine RBMK-Kernschmelze, in der ein DRX-Stab lag, laesst ihn hundert
 * Bloecke ueber der Anlage entstehen; von dort sinkt er mit 0,2 je Tick herab und saet dabei
 * Digamma um sich. Trifft er auf Grund, wartet er hundert Ticks und verseucht dann ALLES, was
 * in der Welt lebt -- nicht nur die Umgebung.
 *
 * WAS ER IM SINKEN TUT: jeden Tick sucht er sich eine Stelle im Umkreis (zwei
 * Gauss-Zufallszahlen mal 25) und laesst dort eine Digamma-Explosion vom Radius 7,5 los. Liegt
 * die Stelle naeher als zwanzig Bloecke, wird daraus das Gittermuster -- ein Schachbrett aus
 * Digamma-Schutt -, sonst schlichte Asche. Alle Spieler der Welt bekommen dabei 0,05 Digamma
 * und den Erfolg.
 *
 * ZWEI NAMEN, EIN BLOCK. Was das Original pribris_digamma nennt, heisst im Port
 * rbmk_debris_digamma -- es ist derselbe Block aus derselben Klasse (nachgemessen:
 * ModBlocks.java:2144 ist die einzige Stelle, die RBMKDebrisDigamma anlegt). Das Gittermuster
 * setzt also den Block, den der Port schon hat.
 *
 * DIGAMMA2 GIBT ES HIER NICHT. Das Original unterscheidet beim Verseuchen zwischen DIGAMMA und
 * DIGAMMA2; der Port kennt nur DIGAMMA, und seine contaminate() schaut bei Digamma ohnehin
 * nicht auf die Art (ContaminationUtil.java:176). Beide Aufrufe gehen deshalb ueber DIGAMMA.
 */
public class DigammaSpear extends Entity {

    /** Wie lange er schon steht. Der Darsteller blendet daran seinen zweiten Durchgang ein. */
    private static final EntityDataAccessor<Integer> IM_BODEN =
            SynchedEntityData.defineId(DigammaSpear.class, EntityDataSerializers.INT);

    public DigammaSpear(EntityType<? extends DigammaSpear> type, Level level) {
        super(type, level);
    }

    public DigammaSpear(Level level) {
        this(NtmEntityTypes.DIGAMMA_SPEAR.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IM_BODEN, 0);
    }

    public int getTicksInGround() { return this.entityData.get(IM_BODEN); }

    @Override
    public void tick() {

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        BlockPos unten = BlockPos.containing(this.getX(), this.getY() - 1, this.getZ());

        if(this.level().getBlockState(unten).isAir()) {
            this.setPos(this.getX(), this.getY() - 0.2D, this.getZ());
            if(this.level() instanceof ServerLevel serverLevel) this.saeen(serverLevel);
            return;
        }

        this.entityData.set(IM_BODEN, this.getTicksInGround() + 1);

        if(this.level() instanceof ServerLevel serverLevel && this.getTicksInGround() > 100) this.blitz(serverLevel);
    }

    /** Eine Digamma-Explosion irgendwo im Umkreis, und ein Hauch Digamma fuer jeden Spieler. */
    private void saeen(ServerLevel level) {

        double ix = this.getX() + this.random.nextGaussian() * 25D;
        double iz = this.getZ() + this.random.nextGaussian() * 25D;
        double iy = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(ix), (int) Math.floor(iz)) + 2;

        boolean gitter = new Vec3(ix - this.getX(), 0D, iz - this.getZ()).length() < 20D;
        this.digammaExplosion(level, ix, iy, iz, 7.5D, gitter);

        for(ServerPlayer spieler : level.players()) {
            ContaminationUtil.contaminate(spieler, HazardType.DIGAMMA, ContaminationType.DIGAMMA, 0.05F);
            NtmCriteria.marke(spieler, "digamma_kauai_moho");
        }
    }

    /**
     * Die Digamma-Explosion: eine Kugel voll Asche, mit Flammen darueber.
     *
     * Das Original faehrt dafuer ExplosionNT mit den Fahnen NOHURT, NOPARTICLE, NODROP und
     * NOSOUND -- also eine Explosion, die NICHTS von dem tut, wofuer eine Explosion sonst da
     * ist: sie setzt nur Bloecke. Genau das steht hier, ohne den Umweg.
     *
     * DAS GITTER: jeder dritte Block in beiden Richtungen wird zu Digamma-Schutt, dazwischen
     * mit halber Wahrscheinlichkeit auf einer der beiden Linien. Der Rest bleibt Asche. Die
     * Rechnung ist die des Originals (ExplosionNT.java:270-281), einschliesslich des
     * Umstands, dass sie mit den WELTkoordinaten rechnet und nicht mit denen zum Mittelpunkt
     * -- das Muster liegt darum fest im Raum, nicht in der Explosion.
     */
    private void digammaExplosion(ServerLevel level, double x, double y, double z, double radius, boolean gitter) {

        int r = (int) Math.ceil(radius);
        BlockPos mitte = BlockPos.containing(x, y, z);

        for(BlockPos pos : BlockPos.betweenClosed(mitte.offset(-r, -r, -r), mitte.offset(r, r, r))) {

            if(mitte.distSqr(pos) > radius * radius) continue;
            if(level.getBlockState(pos).isAir()) continue;
            if(level.getBlockState(pos).getBlock().getExplosionResistance() >= 6000F) continue;

            boolean schutt = gitter && (pos.getX() % 3 == 0 && pos.getZ() % 3 == 0
                    || (pos.getX() % 3 == 0 || pos.getZ() % 3 == 0) && this.random.nextBoolean());

            if(schutt) {
                level.setBlock(pos, NtmBlocks.RBMK_DEBRIS_DIGAMMA.get().defaultBlockState(), 3);
                continue;
            }

            level.setBlock(pos, NtmBlocks.ASH_DIGAMMA.get().defaultBlockState(), 3);

            if(this.random.nextInt(5) == 0 && level.getBlockState(pos.above()).isAir()) {
                level.setBlock(pos.above(), NtmBlocks.FIRE_DIGAMMA.get().defaultBlockState(), 3);
            }
        }
    }

    /** Der Schluss: jedes Lebewesen der Welt bekommt zehn Digamma, dann verschwindet er. */
    private void blitz(ServerLevel level) {

        /* Das Original laeuft ueber worldObj.loadedEntityList -- also ueber ALLES, was die
         * Welt gerade geladen hat, ohne jede Entfernungsgrenze. getAllEntities ist dasselbe. */
        for(Entity entity : level.getAllEntities()) {

            if(entity instanceof LivingEntity lebewesen) {
                ContaminationUtil.contaminate(lebewesen, HazardType.DIGAMMA, ContaminationType.DIGAMMA, 10F);
            }
        }

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                NtmSoundEvents.WEAPON_DFLASH.get(), SoundSource.BLOCKS, 25000.0F, 1.0F);

        this.discard();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { this.entityData.set(IM_BODEN, tag.getInt("ticksInGround")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putInt("ticksInGround", this.getTicksInGround()); }

    @Override public boolean fireImmune() { return true; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 25_000D * 25_000D; }
}
