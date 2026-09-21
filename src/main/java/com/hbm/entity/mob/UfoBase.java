package com.hbm.entity.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityUFOBase.
 *
 * Die Flugsteuerung der fliegenden Scheiben: sie sucht sich einen Spieler, setzt einen
 * Wegpunkt und steuert ihn an, solange der Kurszaehler laeuft. Wer davon erbt, muss nur noch
 * sagen, wie weit er sieht, wie hoch er fliegen will und was er tut, wenn er jemanden hat.
 *
 * VORSICHT, EIN NAMENSFALLE: im Original heissen die Lesestellen des Wegpunkts getX, getY und
 * getZ -- auf 1.7.10 hiess die Position posX, da ging das. Auf 1.21 sind getX/getY/getZ die
 * Position der Entitaet selbst. Der Wegpunkt heisst hier darum getWegpunkt, und jede Zeile,
 * die im Original "getX() - posX" rechnet, rechnet hier "wegpunkt.getX() - getX()".
 *
 * WER DEN TAKT GIBT: das Original haengt alles an updateEntityActionState, dem Takt der
 * kuenstlichen Bewegung; auf 1.21 ist customServerAiStep die Stelle, die dem entspricht. Das
 * Original stellt die Bewegung dort auf null und erwartet, dass die Unterklasse ZUERST super
 * ruft -- so steht es als Kommentar im Rumpf. Auch die beiden Zaehler zaehlt im Original die
 * Unterklasse herunter, nicht die Grundklasse; der Port haelt sich daran.
 *
 * DER PORT NIMMT MOB, NICHT DIE FLIEGENDEN: auf 1.7.10 leitet die Klasse von EntityFlying ab,
 * das im Wesentlichen die Schwerkraft abschaltet. Der Port erreicht dasselbe mit noPhysics --
 * so haelt es schon das Ufo aus Runde 269, und zwei verschiedene Wege waeren einer zu viel.
 */
public abstract class UfoBase extends Mob implements Enemy {

    private static final EntityDataAccessor<BlockPos> WEGPUNKT =
            SynchedEntityData.defineId(UfoBase.class, EntityDataSerializers.BLOCK_POS);

    /** Zaehlt herunter, bis wieder nach Zielen gesucht wird. Die Unterklasse zaehlt ihn. */
    protected int scanCooldown;
    /** Zaehlt herunter, bis ein neuer Wegpunkt gesetzt wird. Die Unterklasse zaehlt ihn. */
    protected int courseChangeCooldown;
    protected Entity target;

    protected UfoBase(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WEGPUNKT, BlockPos.ZERO);
    }

    public BlockPos getWegpunkt() { return this.entityData.get(WEGPUNKT); }
    public void setWegpunkt(BlockPos pos) { this.entityData.set(WEGPUNKT, pos); }

    /** Wie weit er nach Spielern sucht. */
    protected int getScanRange() { return 50; }

    /** Wie lange zwischen zwei Suchen liegt. */
    protected int getScanDelay() { return 100; }

    /** Wie hoch ueber dem Ziel er sich haelt. */
    protected int targetHeightOffset() { return 2 + this.random.nextInt(2); }

    /** Wie hoch ueber dem Boden er zieht, wenn er niemanden hat. */
    protected int wanderHeightOffset() { return 2 + this.random.nextInt(3); }

    @Override
    protected void customServerAiStep() {

        if(this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }

        this.setDeltaMovement(Vec3.ZERO);

        if(this.target != null && !this.target.isAlive()) this.target = null;

        this.scanForTarget();

        if(this.courseChangeCooldown <= 0) this.setCourse();
    }

    /**
     * Sucht den naechsten Spieler im Kasten um sich herum -- waagerecht die volle Weite,
     * senkrecht die halbe. Wer im Schoepfermodus ist oder unsichtbar, zaehlt nicht.
     */
    protected void scanForTarget() {

        if(this.scanCooldown > 0) return;

        int range = this.getScanRange();
        AABB kasten = this.getBoundingBox().inflate(range, range / 2D, range);

        this.target = null;

        for(Player spieler : this.level().getEntitiesOfClass(Player.class, kasten)) {

            if(!spieler.isAlive()) continue;
            if(spieler.isCreative() || spieler.isSpectator()) continue;
            if(spieler.hasEffect(MobEffects.INVISIBILITY)) continue;

            if(this.target == null || this.distanceToSqr(spieler) < this.distanceToSqr(this.target)) {
                this.target = spieler;
            }
        }

        this.scanCooldown = this.getScanDelay();
    }

    /** Ob der Weg zum Wegpunkt frei ist -- der eigene Umriss wird ihn entlanggeschoben. */
    protected boolean istWegFrei(double laenge) {

        BlockPos ziel = this.getWegpunkt();

        double dx = (ziel.getX() - this.getX()) / laenge;
        double dy = (ziel.getY() - this.getY()) / laenge;
        double dz = (ziel.getZ() - this.getZ()) / laenge;

        AABB kasten = this.getBoundingBox();

        for(int i = 1; i < laenge; i++) {
            kasten = kasten.move(dx, dy, dz);
            if(!this.level().noCollision(this, kasten)) return false;
        }

        return true;
    }

    /**
     * Fliegt auf den Wegpunkt zu. Naeher als fuenf Bloecke wird nicht mehr nachgeschoben, und
     * ist der Weg verstellt, wird der Kurszaehler geloescht -- dann sucht er sich im naechsten
     * Takt einen neuen Wegpunkt.
     */
    protected void approachPosition(double tempo) {

        BlockPos ziel = this.getWegpunkt();
        Vec3 weg = new Vec3(ziel.getX() - this.getX(), ziel.getY() - this.getY(), ziel.getZ() - this.getZ());
        double laenge = weg.length();

        if(laenge <= 5) return;

        if(this.istWegFrei(laenge)) {
            this.setDeltaMovement(weg.scale(tempo / laenge));
        } else {
            this.courseChangeCooldown = 0;
        }
    }

    /** Neuer Wegpunkt. Mit Ziel wird oefter nachgesteuert als ohne. */
    protected void setCourse() {

        if(this.target != null) {
            this.setCourseForTarget();
            this.courseChangeCooldown = 20 + this.random.nextInt(20);
        } else {
            this.setCourseWithoutTarget();
            this.courseChangeCooldown = 60 + this.random.nextInt(20);
        }
    }

    /** Ein Punkt zehn bis zwanzig Bloecke hinter dem Ziel, aus gedrehter Richtung. */
    protected void setCourseForTarget() {

        Vec3 weg = new Vec3(this.getX() - this.target.getX(), 0, this.getZ() - this.target.getZ())
                .yRot((float) (Math.PI * 2) * this.random.nextFloat());

        double laenge = weg.length();
        if(laenge < 1.0E-4D) return;

        double ueberschiessen = 10 + this.random.nextDouble() * 10;

        int wx = (int) Math.floor(this.target.getX() - weg.x / laenge * ueberschiessen);
        int wz = (int) Math.floor(this.target.getZ() - weg.z / laenge * ueberschiessen);
        int boden = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);

        this.setWegpunkt(new BlockPos(wx, Math.max(boden, (int) this.target.getY()) + this.targetHeightOffset(), wz));
    }

    /** Ohne Ziel zieht er in der Naehe umher. */
    protected void setCourseWithoutTarget() {

        int wx = (int) Math.floor(this.getX() + this.random.nextGaussian() * 5);
        int wz = (int) Math.floor(this.getZ() + this.random.nextGaussian() * 5);

        this.setWegpunkt(new BlockPos(wx, this.level().getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz) + this.wanderHeightOffset(), wz));
    }

    @Override
    public boolean causeFallDamage(float weite, float wucht, DamageSource quelle) {
        return false;
    }
}
