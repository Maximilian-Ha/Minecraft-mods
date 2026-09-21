package com.hbm.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIMaskmanCasualApproach.
 *
 * ER GEHT NICHT AUF EINEN ZU. Das Original nimmt die Melee-Annaeherung und tauscht das Ziel
 * aus: statt zum Spieler zu laufen, laeuft der Maskenmann auf einen Punkt zu, der in etwa
 * ZEHN Bloecke vom Spieler entfernt liegt -- die Entfernung, in der seine Minigun greift.
 * Die Rechnung des Originals (getApproachPos): der Vektor vom Ziel zu ihm, auf
 * min(Abstand, 20) - 10 gekuerzt, dazu ein Rauschen von zwei Bloecken in der Waagerechten und
 * einer Streuung von minus fuenf bis plus fuenf in der Hoehe.
 *
 * ZUM ANGRIFF KOMMT ES HIER NICHT. Der Angriffszweig des Originals steht dort auskommentiert;
 * der Maskenmann schlaegt nicht zu, er schiesst.
 */
public class MaskmanApproachGoal extends Goal {

    private final PathfinderMob wirt;
    private final double tempo;

    /** Wie lange bis zur naechsten Wegsuche. Das Original wuerfelt 4 + rand(7). */
    private int wegZaehler;

    public MaskmanApproachGoal(PathfinderMob wirt, double tempo) {
        this.wirt = wirt;
        this.tempo = tempo;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity ziel = this.wirt.getTarget();
        return ziel != null && ziel.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.wirt.getNavigation().stop();
        this.wegZaehler = 0;
    }

    @Override
    public void tick() {

        LivingEntity ziel = this.wirt.getTarget();
        if(ziel == null) return;

        this.wirt.getLookControl().setLookAt(ziel, 30.0F, 30.0F);

        if(--this.wegZaehler > 0) return;
        this.wegZaehler = 4 + this.wirt.getRandom().nextInt(7);

        Vec3 punkt = annaeherungsPunkt(ziel);
        this.wirt.getNavigation().moveTo(punkt.x, punkt.y, punkt.z, this.tempo);
    }

    /** Der Punkt, auf den er zugeht -- zehn Bloecke vor dem Ziel, mit Rauschen. */
    private Vec3 annaeherungsPunkt(LivingEntity ziel) {

        Vec3 weg = this.wirt.position().subtract(ziel.position());
        double weite = Math.min(weg.length(), 20D) - 10D;
        Vec3 richtung = weg.normalize();

        return new Vec3(
                this.wirt.getX() + richtung.x * weite + this.wirt.getRandom().nextGaussian() * 2,
                this.wirt.getY() + richtung.y - 5 + this.wirt.getRandom().nextInt(11),
                this.wirt.getZ() + richtung.z * weite + this.wirt.getRandom().nextGaussian() * 2);
    }
}
