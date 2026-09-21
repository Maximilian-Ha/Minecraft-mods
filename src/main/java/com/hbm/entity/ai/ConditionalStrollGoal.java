package com.hbm.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

import java.util.function.Predicate;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIWanderConditional.
 *
 * Das gewoehnliche Umherziehen, aber nur solange eine Bedingung gilt -- bei der Taube:
 * solange sie am Boden ist. Das Original schreibt dafuer EntityAIWander noch einmal ab und
 * haengt die Bedingung davor; auf 1.21 genuegt es, RandomStrollGoal zu erben und die beiden
 * Fragen zu erweitern.
 *
 * DER WURF IST DERSELBE: das Original wuerfelt eins zu hundertzwanzig, und genau das ist
 * auch die Voreinstellung von RandomStrollGoal.
 */
public class ConditionalStrollGoal extends RandomStrollGoal {

    private final Predicate<PathfinderMob> bedingung;

    public ConditionalStrollGoal(PathfinderMob wirt, double tempo, Predicate<PathfinderMob> bedingung) {
        super(wirt, tempo);
        this.bedingung = bedingung;
    }

    @Override
    public boolean canUse() {
        return this.bedingung.test(this.mob) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.bedingung.test(this.mob) && super.canContinueToUse();
    }
}
