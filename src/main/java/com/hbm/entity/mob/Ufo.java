package com.hbm.entity.mob;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.XFactoryNPC;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityUFO.
 *
 * Zwanzigtausend Lebenspunkte, fuenfzehn Bloecke breit, und es schlaegt auf drei Arten zu:
 *
 *   DER FANGSTRAHL geht senkrecht nach unten auf den ersten festen Block. Wer darin steht,
 *   nimmt tausend Punkte, faengt fuenf Sekunden Feuer und bekommt fuenf Einheiten Strahlung.
 *   Er geht an, sobald das Ziel waagerecht naeher als fuenfundzwanzig Bloecke ist, und bleibt
 *   dreissig Ticks.
 *
 *   DER LASER kommt aus einem Drehpunkt ZEHN BLOECKE NEBEN dem UFO, in einem Winkel von minus
 *   achtzig bis plus achtzig Grad zur Zielrichtung -- deshalb wirkt es, als schoesse der Rand
 *   der Scheibe und nicht ihre Mitte.
 *
 *   DIE RAKETEN sind gelenkt. Sie richten keinen Blockschaden an und sprengen nicht; was sie
 *   toetet, ist der Aufschlag.
 *
 * ES WECHSELT DIE WAFFE NACH DER UHR, nicht nach der Lage: in den ersten zweihundert Ticks
 * jedes Dreihunderterblocks der Laser, in den letzten hundert die Raketen.
 *
 * SEIN FLUG IST EIN UEBERSCHIESSEN: es steuert einen Punkt an, der fuenfunddreissig Bloecke
 * HINTER seinem Ziel liegt, und zwar meistens aus einer zufaellig gedrehten Richtung --
 * daraus entsteht das Kreisen. Es bewegt sich nur, solange sein Kurszaehler laeuft.
 *
 * SEIN TOD IST EINE ATOMEXPLOSION: dreissig Ticks Sturz (deathTime beginnt bei minus dreissig),
 * dann eine gewoehnliche Sprengung und darauf die mittlere kleine Atomexplosion.
 */
public class Ufo extends Mob implements Enemy, IRadiationImmune {

    private static final EntityDataAccessor<Boolean> STRAHL =
            SynchedEntityData.defineId(Ufo.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> WEGPUNKT =
            SynchedEntityData.defineId(Ufo.class, EntityDataSerializers.BLOCK_POS);

    /** Wie lange es nach einem Treffer unverwundbar bleibt. */
    private int trefferPause;
    private int kursZaehler;
    private int suchZaehler;
    private int strahlZaehler;

    private Entity hauptziel;
    private final List<Entity> nebenziele = new ArrayList<>();

    /** Wie weit hinter dem Ziel der angesteuerte Punkt liegt. */
    private static final double UEBERSCHIESSEN = 35D;

    public Ufo(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.noCulling = true;
        this.noPhysics = true;
    }

    public Ufo(Level level) { this(NtmEntityTypes.UFO.get(), level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20000.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STRAHL, false);
        builder.define(WEGPUNKT, BlockPos.ZERO);
    }

    public boolean hatStrahl() { return this.entityData.get(STRAHL); }
    private void setStrahl(boolean an) { this.entityData.set(STRAHL, an); }

    public BlockPos getWegpunkt() { return this.entityData.get(WEGPUNKT); }
    private void setWegpunkt(BlockPos pos) { this.entityData.set(WEGPUNKT, pos); }

    /**
     * Jeder Treffer setzt fuenf Ticks Pause. Ohne sie waere das UFO gegen Schrotladungen und
     * Dauerfeuer wehrlos -- fuenfzehn Bloecke Breite treffen sich leicht.
     */
    @Override
    public boolean hurt(DamageSource quelle, float schaden) {

        if(this.trefferPause > 0) return false;

        boolean getroffen = super.hurt(quelle, schaden);
        if(getroffen) this.trefferPause = 5;

        return getroffen;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if(this.level().isClientSide) return;

        if(this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }

        if(this.trefferPause > 0) this.trefferPause--;
        if(this.kursZaehler > 0) this.kursZaehler--;
        if(this.suchZaehler > 0) this.suchZaehler--;

        if(this.hauptziel != null && !this.hauptziel.isAlive()) this.hauptziel = null;

        this.zieleSuchen();
        this.kursSetzen();
        this.fangstrahl();
        this.schiessen();
        this.fliegen();
    }

    /** Alle fuenfzig Ticks: das naechste Spielerziel und alles andere Lebende im Umkreis. */
    private void zieleSuchen() {

        if(this.suchZaehler > 0) return;
        this.suchZaehler = 50;

        this.nebenziele.clear();
        this.hauptziel = null;

        List<Entity> gefunden = this.level().getEntities(this, this.getBoundingBox().inflate(100, 50, 100));

        for(Entity wesen : gefunden) {

            if(!wesen.isAlive() || !this.greiftAn(wesen)) continue;

            if(wesen instanceof Player spieler) {

                if(spieler.isCreative()) continue;
                if(spieler.hasEffect(MobEffects.INVISIBILITY)) continue;

                if(this.hauptziel == null || this.distanceToSqr(spieler) < this.distanceToSqr(this.hauptziel)) {
                    this.hauptziel = spieler;
                }
            }

            if(wesen instanceof LivingEntity && this.distanceToSqr(wesen) < 100 * 100
                    && this.hasLineOfSight(wesen) && wesen != this.hauptziel) {
                this.nebenziele.add(wesen);
            }
        }

        if(this.hauptziel == null && !this.nebenziele.isEmpty()) {
            this.hauptziel = this.nebenziele.get(this.random.nextInt(this.nebenziele.size()));
        }
    }

    /** Der Punkt hinter dem Ziel, meistens aus gedrehter Richtung -- daher das Kreisen. */
    private void kursSetzen() {

        if(this.hauptziel == null || this.kursZaehler > 0) return;

        Vec3 weg = new Vec3(this.getX() - this.hauptziel.getX(), 0, this.getZ() - this.hauptziel.getZ());

        if(this.random.nextInt(3) > 0) {
            weg = weg.yRot((float) (Math.PI * 2 * this.random.nextFloat()));
        }

        double laenge = weg.length();
        if(laenge < 1.0E-4D) return;

        int wx = (int) Math.floor(this.hauptziel.getX() - weg.x / laenge * UEBERSCHIESSEN);
        int wz = (int) Math.floor(this.hauptziel.getZ() - weg.z / laenge * UEBERSCHIESSEN);
        int boden = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, wx, wz);
        int wy = Math.max(boden + 20 + this.random.nextInt(15), (int) this.hauptziel.getY() + 15);

        this.setWegpunkt(new BlockPos(wx, wy, wz));
        this.kursZaehler = 40 + this.random.nextInt(20);
    }

    /** Der Fangstrahl: senkrecht nach unten, auf alles, was zwischen UFO und Grund steht. */
    private void fangstrahl() {

        if(this.strahlZaehler <= 0 && this.hatStrahl()) this.setStrahl(false);

        if(this.hauptziel != null) {
            double flach = Math.abs(this.hauptziel.getX() - this.getX()) + Math.abs(this.hauptziel.getZ() - this.getZ());
            if(flach < 25) this.strahlZaehler = 30;
        }

        if(this.strahlZaehler <= 0) return;
        this.strahlZaehler--;

        if(!this.hatStrahl()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    NtmSoundEvents.UFO_BEAM.get(), SoundSource.HOSTILE, 10.0F, 1.0F);
            this.setStrahl(true);
        }

        int ix = (int) Math.floor(this.getX());
        int iz = (int) Math.floor(this.getZ());
        int boden = this.level().getMinBuildHeight();

        for(int y = (int) Math.ceil(this.getY()); y >= this.level().getMinBuildHeight(); y--) {
            if(!this.level().getBlockState(new BlockPos(ix, y, iz)).isAir()) { boden = y; break; }
        }

        if(boden >= this.getY()) return;

        AABB saeule = new AABB(this.getX(), boden, this.getZ(), this.getX(), this.getY(), this.getZ()).inflate(5, 0, 5);

        for(Entity getroffen : this.level().getEntities(this, saeule)) {

            if(!this.greiftAn(getroffen)) continue;

            getroffen.hurt(this.damageSources().source(NtmDamageTypes.LUNAR, this), 1000F);
            getroffen.igniteForSeconds(5);

            if(getroffen instanceof LivingEntity lebend) {
                ContaminationUtil.contaminate(lebend, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
            }
        }

        if(this.level() instanceof ServerLevel serverLevel) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", "ufo");
            PacketDistributor.sendToPlayersNear(serverLevel, null, this.getX(), boden + 0.5, this.getZ(), 150,
                    new AuxParticle(tag, this.getX(), boden + 0.5, this.getZ()));
        }
    }

    /** Die Uhr entscheidet: erst zweihundert Ticks Laser, dann hundert Ticks Raketen. */
    private void schiessen() {

        int takt = this.tickCount % 300;

        if(takt < 200) {
            if(this.tickCount % 4 == 0) this.aufNaechstes(this::laser);
            else if(this.tickCount % 4 == 2 && this.hauptziel != null) this.laser(this.hauptziel);
        } else {
            if(this.tickCount % 20 == 0) this.aufNaechstes(this::rakete);
            else if(this.tickCount % 20 == 10 && this.hauptziel != null) this.rakete(this.hauptziel);
        }
    }

    /** Erst ein zufaelliges Nebenziel, und nur wenn es keines gibt, das Hauptziel. */
    private void aufNaechstes(java.util.function.Consumer<Entity> waffe) {

        if(!this.nebenziele.isEmpty()) {
            Entity ziel = this.nebenziele.get(this.random.nextInt(this.nebenziele.size()));
            if(!ziel.isAlive()) this.nebenziele.remove(ziel);
            else waffe.accept(ziel);

        } else if(this.hauptziel != null) {
            waffe.accept(this.hauptziel);
        }
    }

    private void laser(Entity ziel) {

        Vec3 weg = new Vec3(this.getX() - ziel.getX(), 0, this.getZ() - ziel.getZ())
                .yRot((float) Math.toRadians(-80 + this.random.nextInt(160)))
                .normalize();

        Vec3 drehpunkt = new Vec3(this.getX() - weg.x * 10, this.getY() + 0.5, this.getZ() - weg.z * 10);
        Vec3 richtung = new Vec3(ziel.getX() - drehpunkt.x,
                ziel.getY() + ziel.getBbHeight() / 2 - drehpunkt.y,
                ziel.getZ() - drehpunkt.z).normalize();

        BulletBaseMK4 strahl = new BulletBaseMK4(this.level(), this, XFactoryNPC.worm_laser,
                XFactoryNPC.worm_laser.damageMult, 0.02F, drehpunkt, richtung.scale(2D));
        this.level().addFreshEntity(strahl);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                NtmSoundEvents.WEAPON_BALLS_LASER.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
    }

    private void rakete(Entity ziel) {

        Vec3 von = new Vec3(this.getX(), this.getY() - 0.5D, this.getZ());
        Vec3 richtung = new Vec3(ziel.getX() - von.x,
                ziel.getY() + ziel.getBbHeight() / 2 - von.y,
                ziel.getZ() - von.z).normalize();

        BulletBaseMK4 rakete = new BulletBaseMK4(this.level(), this, XFactoryNPC.ufo_rocket,
                XFactoryNPC.ufo_rocket.damageMult, 0.02F, von, richtung.scale(2D));
        this.level().addFreshEntity(rakete);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                NtmSoundEvents.UFO_BLAST.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
    }

    /**
     * Der Flug. Es steht still, solange kein Kurs laeuft, und bewegt sich sonst mit
     * gleichbleibendem Tempo -- fuenf auf einen Spieler zu, sonst zwei.
     */
    private void fliegen() {

        this.setDeltaMovement(Vec3.ZERO);

        if(this.kursZaehler <= 0) return;

        BlockPos ziel = this.getWegpunkt();
        Vec3 weg = new Vec3(ziel.getX() - this.getX(), ziel.getY() - this.getY(), ziel.getZ() - this.getZ());
        double laenge = weg.length();

        if(laenge <= 5) return;

        double tempo = this.hauptziel instanceof Player ? 5D : 2D;
        this.setDeltaMovement(weg.scale(tempo / laenge));
    }

    /** Es schiesst auf alles ausser auf seinesgleichen und auf Geschosse. */
    private boolean greiftAn(Entity wesen) {
        return !(wesen instanceof Ufo) && !(wesen instanceof BulletBaseMK4);
    }

    @Override
    protected void tickDeath() {

        if(this.hatStrahl()) this.setStrahl(false);

        this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.05D, 0));
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        this.deathTime++;

        if(this.deathTime == 20 && !this.level().isClientSide) {

            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 10F, Level.ExplosionInteraction.MOB);
            ExplosionNukeSmall.mittel(this.level(), this.getX(), this.getY(), this.getZ());

            List<Player> nahe = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(200));

            for(Player spieler : nahe) {
                if(spieler instanceof ServerPlayer server) NtmCriteria.marke(server, "boss_ufo");
                spieler.getInventory().add(new ItemStack(NtmItems.COIN_UFO.get()));
            }

            this.remove(RemovalReason.KILLED);
        }
    }

    @Override public boolean fireImmune() { return true; }
    @Override public boolean removeWhenFarAway(double weite) { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double abstand) { return abstand < 500000; }
    @Override public boolean causeFallDamage(float weite, float wucht, DamageSource quelle) { return false; }
    @Override protected float getSoundVolume() { return 10.0F; }
}
