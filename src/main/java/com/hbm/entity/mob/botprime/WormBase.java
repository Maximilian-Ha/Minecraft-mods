package com.hbm.entity.mob.botprime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.botprime.EntityWormBaseNT samt EntityBurrowingNT.
 *
 * Die Grundlage des Wurms. Er ist kein einzelnes Wesen, sondern eine Kette: ein Kopf und
 * vierundsiebzig Glieder, die alle dieselbe KOPFKENNUNG tragen und sich daran wiederfinden.
 * Jedes Glied folgt seinem Vorgaenger auf drei ein halb Bloecke Abstand.
 *
 * ER LAEUFT NICHT, ER SCHWIMMT DURCH DIE WELT. Keines der Teile nutzt die Wegsuche; jedes
 * setzt seine Bewegung unmittelbar aus dem Abstand zu seinem Wegpunkt. Bloecke halten ihn
 * nicht auf (noPhysics), nur die Reibung unterscheidet sich: in der Luft 0,995, im Boden 0,98,
 * und ein Glied nimmt davon noch neun Zehntel.
 *
 * SCHADEN GEHT IMMER AN DEN KOPF. Trifft etwas ein Glied, reicht es den Schlag weiter; nur der
 * Kopf hat Lebensenergie, die zaehlt. Ertrinken und Ersticken prallen ab, und der Wurm kann
 * sich nicht selbst verletzen -- das Original prueft dafuer die Kopfkennung des Angreifers.
 */
public abstract class WormBase extends PathfinderMob {

    /** Die Entitaetskennung des Kopfes. Alle Teile einer Kette teilen sie. */
    private static final EntityDataAccessor<Integer> KOPF_KENNUNG =
            SynchedEntityData.defineId(WormBase.class, EntityDataSerializers.INT);

    /** Die Nummer dieses Glieds in der Kette; der Kopf fuehrt sie nicht. */
    private static final EntityDataAccessor<Integer> TEIL_NUMMER =
            SynchedEntityData.defineId(WormBase.class, EntityDataSerializers.INT);

    public double wegpunktX, wegpunktY, wegpunktZ;
    protected Entity gefolgt;
    protected LivingEntity vordermann;

    protected float reibungLuft = 0.995F;
    protected float reibungBoden = 0.98F;
    protected double gliedAbstand = 3.5D;
    protected double gliedTempo = 1.4D;
    protected double sucheWeite = 70D;
    protected double rueckstossTeiler = 1.0D;
    protected boolean gepruef;

    protected WormBase(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KOPF_KENNUNG, 0);
        builder.define(TEIL_NUMMER, 0);
    }

    public boolean istKopf() { return false; }

    public int getKopfKennung() { return this.entityData.get(KOPF_KENNUNG); }
    public void setKopfKennung(int kennung) { this.entityData.set(KOPF_KENNUNG, kennung); }

    public int getTeilNummer() { return this.entityData.get(TEIL_NUMMER); }
    public void setTeilNummer(int nummer) { this.entityData.set(TEIL_NUMMER, nummer); }

    public Entity getKopf() { return this.level().getEntity(this.getKopfKennung()); }

    /** Wie stark dieses Teil zuschlaegt. Kopf und Glied rechnen verschieden. */
    public abstract float getAngriffsstaerke(Entity ziel);

    @Override
    public boolean hurt(DamageSource quelle, float schaden) {

        if(this.isInvulnerableTo(quelle)) return false;
        if(quelle.is(DamageTypes.DROWN) || quelle.is(DamageTypes.IN_WALL)) return false;

        /* Ein Wurm verletzt sich nicht selbst: dieselbe Kopfkennung heisst dieselbe Kette. */
        if(quelle.getEntity() instanceof WormBase anderer && anderer.getKopfKennung() == this.getKopfKennung()) return false;

        if(this.istKopf()) return super.hurt(quelle, schaden);

        /* Ein Glied reicht den Schlag an seinen Vordermann weiter -- am Ende landet er
         * beim Kopf, der als einziger Lebensenergie fuehrt. */
        Entity vor = this.gefolgt;
        if(vor != null) return vor.hurt(quelle, schaden);

        return super.hurt(quelle, schaden);
    }

    /**
     * Was jedes Teil jeden Tick tut: unter minus zehn Bloecken steigt es auf, unter drei
     * langsamer, und alle fuenf Ticks schlaegt es zu, was ihm zu nahe kommt.
     */
    protected void wurmTick() {

        if(this.gefolgt != null && !this.gefolgt.isAlive()) this.gefolgt = null;

        if(this.getY() < -10.0D) {
            this.setDeltaMovement(this.getDeltaMovement().x, 1.0D, this.getDeltaMovement().z);
        } else if(this.getY() < 3.0D) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.3D, this.getDeltaMovement().z);
        }

        if(this.tickCount % 5 == 0) {
            List<Entity> nahe = this.level().getEntities(this, this.getBoundingBox().inflate(0.5D));
            for(Entity ziel : nahe) {
                if(!(ziel instanceof LivingEntity)) continue;
                if(ziel instanceof WormBase anderer && anderer.getKopfKennung() == this.getKopfKennung()) continue;
                this.schlagen(ziel);
            }
        }
    }

    /** Der Schlag samt Rueckstoss, wie im Original aus dem Abstandsvektor gerechnet. */
    protected void schlagen(Entity ziel) {

        if(!ziel.hurt(this.damageSources().mobAttack(this), this.getAngriffsstaerke(ziel))) return;

        Vec3 mitte = this.getBoundingBox().getCenter();
        double dx = ziel.getX() - mitte.x;
        double dy = ziel.getY() - mitte.y;
        double dz = ziel.getZ() - mitte.z;
        double teiler = this.rueckstossTeiler * (dx * dx + dy * dy + dz * dz + 0.1D);

        ziel.push(dx / teiler, dy / teiler, dz / teiler);
    }

    /**
     * Die Bewegung eines Glieds: es haelt auf seinen Vordermann zu und bremst, sobald es ihm
     * naeher als knapp neun Zehntel des Sollabstands kommt.
     */
    protected void gliedBewegen() {

        if(this.gefolgt != null) {
            this.wegpunktX = this.gefolgt.getX();
            this.wegpunktY = this.gefolgt.getY();
            this.wegpunktZ = this.gefolgt.getZ();
        }

        if((this.tickCount % 60 == 0 || this.tickCount == 1) && (this.gefolgt == null || this.vordermann == null)) {
            this.vordermannSuchen();
        }

        double dx = this.wegpunktX - this.getX();
        double dy = this.wegpunktY - this.getY();
        double dz = this.wegpunktZ - this.getZ();
        double weite = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if(weite < 1.0E-4D) return;

        double tempo = Math.max(0.0D, Math.min(weite - this.gliedAbstand, this.gliedTempo));

        if(weite < this.gliedAbstand * 0.895D) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
        } else {
            this.setDeltaMovement(dx / weite * tempo, dy / weite * tempo, dz / weite * tempo);
        }
    }

    /**
     * Wer vor mir liegt: das Glied mit der um eins kleineren Nummer, oder beim ersten Glied
     * der Kopf selbst. Gesucht wird nur in der eigenen Kette.
     */
    protected void vordermannSuchen() {

        List<WormBase> teile = this.level().getEntitiesOfClass(WormBase.class,
                this.getBoundingBox().inflate(this.sucheWeite));

        for(WormBase teil : teile) {

            if(teil.getKopfKennung() != this.getKopfKennung()) continue;

            if(teil.istKopf()) {
                if(this.getTeilNummer() == 0) this.gefolgt = teil;
                this.vordermann = teil;
            } else if(teil.getTeilNummer() == this.getTeilNummer() - 1) {
                this.gefolgt = teil;
            }
        }

        this.gepruef = true;
    }

    /** Die Reibung, mit der sich dieses Teil bewegt -- im Boden traeger als in der Luft. */
    protected float reibung() {
        float reibung = this.isInWall() || this.isInWater() ? this.reibungBoden : this.reibungLuft;
        return this.istKopf() ? reibung : reibung * 0.9F;
    }

    @Override public void push(double x, double y, double z) { }
    @Override public boolean causeFallDamage(float weite, float wucht, DamageSource quelle) { return false; }
    @Override public boolean onClimbable() { return false; }
    @Override public boolean removeWhenFarAway(double weite) { return false; }
    @Override protected float getSoundVolume() { return 5.0F; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WormID", this.getKopfKennung());
        tag.putInt("PartID", this.getTeilNummer());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setKopfKennung(tag.getInt("WormID"));
        this.setTeilNummer(tag.getInt("PartID"));
    }
}
