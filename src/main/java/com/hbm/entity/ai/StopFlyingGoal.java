package com.hbm.entity.ai;

import com.hbm.entity.mob.IFlyingCreature;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIStopFlying.
 *
 * Landen: ein Wurf von eins zu zweihundert je Takt, ohne jede Bedingung. Wer oben ist, kommt
 * im Mittel nach zehn Sekunden wieder herunter.
 */
public class StopFlyingGoal extends Goal {

    private final Mob wirt;
    private final IFlyingCreature flieger;

    public StopFlyingGoal(Mob wirt, IFlyingCreature flieger) {
        this.wirt = wirt;
        this.flieger = flieger;
    }

    @Override
    public boolean canUse() {
        return this.flieger.getFlyingState() == IFlyingCreature.STATE_FLYING && this.wirt.getRandom().nextInt(200) == 0;
    }

    @Override
    public void start() {
        this.flieger.setFlyingState(IFlyingCreature.STATE_WALKING);
    }
}
