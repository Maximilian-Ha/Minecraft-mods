package com.hbm.entity.mob;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.grenade.GrenadeUniversal;
import com.hbm.items.weapon.grenade.GrenadeFillingItem.GrenadeFilling;
import com.hbm.items.weapon.grenade.GrenadeFuzeItem.GrenadeFuze;
import com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell;
import com.hbm.items.weapon.grenade.GrenadeUniversalItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityFBIDrone.
 *
 * Der Quadrokopter der Razzia. Er haelt sich sieben bis zehn Bloecke ueber seinem Ziel, und
 * steht er genau darueber, laesst er eine Splittergranate fallen -- keine geworfene, eine
 * fallende: das Original setzt sie auf seine eigene Stelle und gibt ihr keinen Schwung.
 *
 * DIE BEDINGUNG IST ENG: waagerecht weniger als fuenf Bloecke Abstand in BEIDEN Richtungen,
 * und er muss mehr als drei Bloecke HOEHER stehen. Danach wartet er sechzig Takte.
 *
 * ER SIEHT WEITER ALS DAS UFO: hundert Bloecke statt fuenfzig.
 *
 * ER BEWEGT SICH NUR, SOLANGE DER KURSZAEHLER LAEUFT, und zwar halb so schnell ohne Ziel wie
 * mit. Dass die Grundklasse den Zaehler nicht selbst herunterzaehlt, sondern die Unterklasse,
 * ist im Original so und hier uebernommen -- steht er auf null, bleibt der Kopter stehen, bis
 * ein neuer Kurs gesetzt ist.
 */
public class FbiDrone extends UfoBase {

    /** Wie lange bis zur naechsten Granate. */
    private int attackCooldown;

    public FbiDrone(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D);
    }

    @Override
    protected int getScanRange() { return 100; }

    @Override
    protected int targetHeightOffset() { return 7 + this.random.nextInt(4); }

    @Override
    protected int wanderHeightOffset() { return 7 + this.random.nextInt(4); }

    @Override
    protected void customServerAiStep() {

        super.customServerAiStep();

        if(this.courseChangeCooldown > 0) this.courseChangeCooldown--;
        if(this.scanCooldown > 0) this.scanCooldown--;
        if(this.attackCooldown > 0) this.attackCooldown--;

        if(this.target != null && this.attackCooldown <= 0) {

            double dx = this.getX() - this.target.getX();
            double dy = this.getY() - this.target.getY();
            double dz = this.getZ() - this.target.getZ();

            if(Math.abs(dx) < 5 && Math.abs(dz) < 5 && dy > 3) {
                this.attackCooldown = 60;
                this.granateFallenLassen();
            }
        }

        if(this.courseChangeCooldown > 0) this.approachPosition(this.target == null ? 0.25D : 0.5D);
    }

    /** Splitterhuelse, Sprengstoff, Sieben-Sekunden-Zuender -- und kein Schwung. */
    private void granateFallenLassen() {

        GrenadeUniversal granate = new GrenadeUniversal(NtmEntityTypes.GRENADE_UNIVERSAL.get(), this.level());
        granate.setGrenadeItem(GrenadeUniversalItem.make(GrenadeShell.FRAG, GrenadeFilling.HE, GrenadeFuze.S7));
        granate.setOwner(this);
        granate.setPos(this.getX(), this.getY(), this.getZ());

        this.level().addFreshEntity(granate);
    }
}
