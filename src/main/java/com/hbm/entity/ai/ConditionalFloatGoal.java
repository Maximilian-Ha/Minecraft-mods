package com.hbm.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;

import java.util.function.Predicate;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAISwimmingConditional.
 *
 * Schwimmen, aber nur solange eine Bedingung gilt. Die Taube paddelt nur, wenn sie am Boden
 * ist -- fliegt sie, soll sie sich vom Wasser nicht stoeren lassen.
 */
public class ConditionalFloatGoal extends FloatGoal {

    private final Mob wirt;
    private final Predicate<Mob> bedingung;

    public ConditionalFloatGoal(Mob wirt, Predicate<Mob> bedingung) {
        super(wirt);
        this.wirt = wirt;
        this.bedingung = bedingung;
    }

    @Override
    public boolean canUse() {
        return this.bedingung.test(this.wirt) && super.canUse();
    }
}
