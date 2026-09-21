package com.hbm.entity.mob.glyphid;

import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.entity.projectile.AcidBomb;
import com.hbm.main.ResourceManager;
import com.hbm.util.Vec3NT;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidBombardier.
 *
 * Der Bombardier wirft. Alle sechzig Takte spuckt er fuenf Saeureklekse in einem Bogen auf
 * sein Ziel -- und er trifft, weil er die Wurfbahn ausrechnet statt sie zu raten.
 *
 * DIE BALLISTISCHE LOESUNG, Schritt fuer Schritt wie im Original:
 * - Alle zwanzig Takte merkt er sich, wo das Ziel gerade steht. Die Differenz zum jetzigen
 *   Ort ist seine Schaetzung der Zielgeschwindigkeit.
 * - Er rechnet den Ort voraus, an dem das Ziel sein wird: zwanzig Takte bei nahem Ziel,
 *   sechzig bei fernem. Wechselt das Ziel, oder ist die gemessene Geschwindigkeit
 *   unsinnig gross (ueber 30 pro Takt, also ein Teleport), verwirft er die Vorhersage.
 * - Aus Abstand (x), Hoehenunterschied (y), Anfangsgeschwindigkeit (v0) und Schwerkraft
 *   (g = 0,04, genau die der Saeurebombe) folgt der Abwurfwinkel. Die Wurzel hat zwei
 *   Loesungen: die flache Bahn fuer nahe Ziele, die steile fuer ferne. Daher upperLower.
 * - Ist die Wurzel negativ (Ziel ausser Reichweite), ist targetPitch NaN und er wirft nicht.
 *
 * STREUUNG UEBER DIE SALVE: die i-te Bombe bekommt die Streuung i mal getSpreadMult -- die
 * erste fliegt genau, die letzte am weitesten daneben. So wird aus einer Salve ein Faecher.
 */
public class GlyphidBombardier extends Glyphid {

    /** Das zuletzt beobachtete Ziel und sein Ort -- die Grundlage der Vorhersage. */
    @Nullable protected Entity lastTarget;
    protected double lastX;
    protected double lastY;
    protected double lastZ;

    public GlyphidBombardier(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().bombardier;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_BOMBARDIER_TEX;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().bombardier;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) return;

        Entity ziel = this.getTarget();
        if(!(ziel instanceof LivingEntity)) return;

        if(this.tickCount % 20 == 0) {
            this.lastTarget = ziel;
            this.lastX = ziel.getX();
            this.lastY = ziel.getY();
            this.lastZ = ziel.getZ();
        }

        if(this.tickCount % 60 == 1) this.werfen(ziel);
    }

    /** Eine Salve. Trifft die Rechnung nicht zu, geschieht nichts -- auch kein Ausholen. */
    protected void werfen(Entity ziel) {

        /* Ueber zwanzig Bloecke Abstand geht es im hohen Bogen, darunter flach. */
        boolean hochwurf = this.distanceTo(ziel) > 20;

        double vx = ziel.getX() - this.lastX;
        double vy = ziel.getY() - this.lastY;
        double vz = ziel.getZ() - this.lastZ;

        if(this.lastTarget != ziel || new Vec3NT(vx, vy, vz).length() > 30) {
            vx = vy = vz = 0;
        }

        int vorhersage = hochwurf ? 60 : 20;

        Vec3NT delta = new Vec3NT(
                ziel.getX() - this.getX() + vx * vorhersage,
                (ziel.getY() + ziel.getBbHeight() / 2) - (this.getY() + 1) + vy * vorhersage,
                ziel.getZ() - this.getZ() + vz * vorhersage);

        if(delta.length() < 3) return;

        double zielGier = -Math.atan2(delta.xCoord, delta.zCoord);

        double x = Math.sqrt(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
        double y = delta.yCoord;
        double v0 = this.getV0();
        double v02 = v0 * v0;
        double g = 0.04D;
        double zweig = hochwurf ? 1 : -1;
        double zielNeigung = Math.atan((v02 + Math.sqrt(v02 * v02 - g * (g * x * x + 2 * y * v02)) * zweig) / (g * x));

        if(Double.isNaN(zielNeigung)) return;

        Vec3NT wurf = new Vec3NT(v0, 0, 0);
        wurf.rotateAroundZRad(-zielNeigung);
        wurf.rotateAroundYRad(-(zielGier + Math.PI * 0.5));

        for(int i = 0; i < this.getBombCount(); i++) {
            AcidBomb bombe = new AcidBomb(this.level(), this.getX(), this.getY() + 1, this.getZ());
            bombe.setOwner(this);
            bombe.richten(wurf.xCoord, wurf.yCoord, wurf.zCoord, (float) v0, i * this.getSpreadMult());
            bombe.damage = this.getBombDamage();
            this.level().addFreshEntity(bombe);
        }

        this.swing(InteractionHand.MAIN_HAND);
    }

    /** Was eine Bombe abzieht. */
    public float getBombDamage() {
        return 5F;
    }

    /** Wie viele Bomben eine Salve hat. */
    public int getBombCount() {
        return 5;
    }

    /** Wie schnell die Streuung ueber die Salve waechst. */
    public float getSpreadMult() {
        return 1F;
    }

    /** Die Anfangsgeschwindigkeit -- sie bestimmt die Reichweite. */
    public double getV0() {
        return 1D;
    }
}
