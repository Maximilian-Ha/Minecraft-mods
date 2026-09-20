package com.hbm.entity.projectile;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.registry.NtmDamageTypes;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Portiert aus 1.7.10: der Tau-Schuss der EntityBullet.
 *
 * Das Original hat fuer alle Geschosse eine einzige Klasse mit dreizehn Konstruktoren und
 * drei Schaltern (kritisch, Tau, Hacker), aus denen sich die Schadensart ergibt. Die Krabbe
 * nimmt davon genau eine Einstellung: kritisch und Tau, Schaden drei. Diese eine Einstellung
 * steht hier, statt die ganze Geschossklasse mitzunehmen -- den Rest hat der Port laengst im
 * Sedna-System (BulletBaseMK4), das aber eine BulletConfig und damit eine Munitionssorte
 * braucht, die es fuer die Krabbe nicht gibt.
 *
 * WAS "KRITISCH" IM ORIGINAL BEDEUTET, ist nicht mehr Schaden, sondern: das Geschoss bleibt
 * in keinem Block stecken (der Zweig, der es einrammt, haengt an !getIsCritical()) und es
 * stirbt auch nicht beim Treffer. Es fliegt weiter, bis seine zweihundertfuenfzig Ticks um
 * sind. Genau das leisten hier spektral und durchschlagend.
 *
 * DER SCHADEN IST FEST DREI. Das Original rechnet zwar ein k aus Geschwindigkeit mal Schaden
 * aus, uebergibt an attackEntityFrom aber die rohe drei -- k wird nie benutzt.
 *
 * KEIN MODELL: die Spur aus rotem Staub zeichnet das Original selbst, den Koerper aber ueber
 * ResourceManager.projectiles, Teil "BulletRifle". Diese OBJ-Datei liegt nicht im Port; der
 * Schuss ist darum an seiner Staubspur zu erkennen und sonst unsichtbar.
 */
public class TauShot extends ProjectileNT {

    /** Zweihundertfuenfzig Ticks -- danach raeumt das Original das Geschoss weg. */
    public static final int LEBENSDAUER = 250;

    /** Der rote Staub der Tau-Spur, achtmal je Tick entlang des Weges. */
    private static final DustParticleOptions STAUB = new DustParticleOptions(new Vector3f(1F, 0F, 0F), 1F);

    public TauShot(EntityType<? extends TauShot> type, Level level) {
        super(type, level);
    }

    public TauShot(Level level, LivingEntity schuetze) {
        super(NtmEntityTypes.TAU_SHOT.get(), level);
        this.setOwner(schuetze);
        this.setPos(schuetze.getX(), schuetze.getEyeY() - 0.1, schuetze.getZ());
    }

    /**
     * Der Bolzen hat nichts zu uebertragen. Die Methode muss trotzdem stehen: Entity
     * erklaert sie abstrakt, und weder Projectile noch ProjectileNT fuellen sie aus.
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) { }

    @Override
    protected void onHitEntity(EntityHitResult ehr) {

        Entity getroffen = ehr.getEntity();
        if(!getroffen.isAlive()) return;

        Entity urheber = this.getOwner();
        getroffen.hurt(this.damageSources().source(NtmDamageTypes.TAU_BLAST, this, urheber == null ? this : urheber), 3F);
    }

    @Override
    public void tick() {

        super.tick();

        if(this.level().isClientSide) {
            Vec3 schritt = this.getDeltaMovement();
            for(int i = 0; i < 8; i++) {
                this.level().addParticle(STAUB,
                        this.getX() + schritt.x * i / 8.0,
                        this.getY() + schritt.y * i / 8.0,
                        this.getZ() + schritt.z * i / 8.0, 0, 0, 0);
            }
        }

        if(this.tickCount > LEBENSDAUER) this.discard();
    }

    /* Das Original setzt die Schwerkraft nur ueber einen Konstruktor, den die Krabbe nicht
     * nimmt -- ihr Schuss faellt also nicht. */
    @Override protected double getDefaultGravity() { return 0; }
    @Override public boolean doesPenetrate() { return true; }
    @Override public boolean isSpectral() { return true; }
}
