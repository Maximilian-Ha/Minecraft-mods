package com.hbm.entity.mob.glyphid;

import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Vec3NT;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidBrawler.
 *
 * Der Brawler springt. Etwa alle vier Sekunden wirft er sich selbst wie ein Geschoss auf
 * sein Ziel -- das Original sagt dazu selbst "yeag this is now a motherfucking projectile":
 * die Sprungbahn ist dieselbe ballistische Rechnung, mit der der Bombardier seine Bomben
 * wirft, nur dass hier der Glyphid das Geschoss ist.
 *
 * ANDERE ZAHLEN ALS BEIM BOMBARDIER: v0 ist 1,5 statt 1, die Schwerkraft in der Rechnung
 * ist 0,01 statt 0,04, und der gefundene Winkel wird durch 3,5 geteilt, bevor er die
 * Richtung dreht. Das ist keine saubere Ballistik mehr -- die Rechnung liefert den Winkel
 * fuer einen hohen Bogen, und ein Siebtel Neigung daraus macht daraus einen flachen Satz
 * nach vorn. Genau so steht es im Original.
 *
 * DIE VORHERSAGE DES ORIGINALS IST WIRKUNGSLOS und darum hier nicht nachgebaut: die Felder
 * lastX/lastY/lastZ werden in jedem Takt auf den Ort des Ziels gesetzt, und der Sprung
 * liest sie im selben Takt -- die Differenz ist also immer null. Das Feld lastTarget wird
 * ueberdies nie belegt, weshalb die Rechnung sie ohnehin verwerfen wuerde. Der Bombardier
 * misst dagegen nur alle zwanzig Takte, dort ist dieselbe Vorhersage echt.
 *
 * KEIN FALLSCHADEN BIS ZEHN: ein Sprung selbst soll ihm nichts tun, ein Sturz aus grosser
 * Hoehe aber schon. Darum die Grenze und nicht schlicht Unverwundbarkeit.
 */
public class GlyphidBrawler extends Glyphid {

    /** Takte bis zum naechsten Sprung. */
    public int timer = 0;

    public GlyphidBrawler(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().brawler;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_BRAWLER_TEX;
    }

    @Override
    public double getGlyphidScale() {
        return 1.25D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().brawler;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.getTarget() == null || !this.isAlive()) return;

        if(--this.timer <= 0) {
            this.springen();
            this.timer = 80 + this.random.nextInt(30);
        }
    }

    /** Ein Satz nach vorn. Nur auf dem Server, nur auf Lebendes, nur unter zwanzig Bloecken. */
    public void springen() {

        if(this.level().isClientSide) return;

        Entity ziel = this.getTarget();
        if(!(ziel instanceof LivingEntity) || this.distanceTo(ziel) >= 20) return;

        Vec3NT delta = new Vec3NT(
                ziel.getX() - this.getX(),
                (ziel.getY() + ziel.getBbHeight() / 2) - (this.getY() + 1),
                ziel.getZ() - this.getZ());

        if(delta.length() < 3) return;

        double zielGier = -Math.atan2(delta.xCoord, delta.zCoord);

        double x = Math.sqrt(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
        double y = delta.yCoord;
        double v0 = 1.5D;
        double v02 = v0 * v0;
        double g = 0.01D;
        double zielNeigung = Math.atan((v02 + Math.sqrt(v02 * v02 - g * (g * x * x + 2 * y * v02))) / (g * x));

        if(Double.isNaN(zielNeigung)) return;

        Vec3NT sprung = new Vec3NT(v0, 0, 0);
        sprung.rotateAroundZRad(-zielNeigung / 3.5D);
        sprung.rotateAroundYRad(-(zielGier + Math.PI * 0.5));

        this.richten(sprung.xCoord, sprung.yCoord, sprung.zCoord, (float) v0, this.random.nextFloat());
    }

    /** Wie beim Wurfkoerper -- dieselbe Streuung, nur dass hier der Glyphid selbst fliegt. */
    protected void richten(double x, double y, double z, float geschwindigkeit, float streuung) {

        Vec3 richtung = BobMathUtil.throwableHeading(this.random, x, y, z, geschwindigkeit, streuung);

        this.setDeltaMovement(richtung);
        this.hasImpulse = true;

        double flach = richtung.horizontalDistance();
        this.setYRot((float) (Math.atan2(richtung.x, richtung.z) * 180.0D / Math.PI));
        this.setXRot((float) (Math.atan2(richtung.y, flach) * 180.0D / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public boolean hurt(DamageSource quelle, float menge) {
        if(quelle.is(DamageTypes.FALL) && menge <= 10) return false;
        return super.hurt(quelle, menge);
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.25, 2), 100);
    }
}
