package com.hbm.entity.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityParasiteMaggot.
 *
 * Die Made. Acht Leben, doppeltes Lauftempo, zwei Schaden -- und sie greift jeden Spieler an,
 * den sie sechzehn Bloecke weit sieht, bei jedem Licht.
 *
 * SIE IST EIN GLIEDERFUESSER, das Mal der Gliederfuesser wirkt also gegen sie. Das Original
 * sagt das ueber getCreatureAttribute; auf 1.21 gibt es diese Methode nicht mehr, die Frage
 * entscheidet der Tag sensitive_to_bane_of_arthropods. Er steht im Tag-Erzeuger.
 *
 * WAS DER PORT DAZUTUN MUSS: ein MeleeAttackGoal. Das Original sucht sein Ziel in
 * findPlayerToAttack und laeuft in EntityMob.attackEntity von selbst hin; in 1.21 gibt es
 * beides nicht mehr, und ohne diese Aufgabe stuende die Made nur da.
 *
 * NICHT UEBERNOMMEN: isValidLightLevel, das im Original immer wahr zurueckgibt. Es gehoert zu
 * getCanSpawnHere, und die Made erscheint nirgends von selbst -- sie kommt aus dem Glyphid
 * oder aus dem Ei. Eine Regel fuer eine Erscheinung, die es nicht gibt, waere toter Code.
 */
public class ParasiteMaggot extends Monster {

    public ParasiteMaggot(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Sie huscht, statt zu stapfen: keine Schrittgeraeusche, keine Druckplatten. */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** Der Koerper folgt dem Kopf -- das Original setzt renderYawOffset in jedem Takt gleich. */
    @Override
    public void aiStep() {
        this.yBodyRot = this.getYRot();
        super.aiStep();
    }
}
