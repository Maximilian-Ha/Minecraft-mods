package com.hbm.entity.ai;

import com.hbm.entity.mob.IFlyingCreature;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIStartFlying.
 *
 * Abheben: wer angegriffen wird, brennt oder einfach Lust hat. Die Lust ist ein Wurf von eins
 * zu sechshundert je Takt -- im Mittel alle dreissig Sekunden.
 */
public class StartFlyingGoal extends Goal {

    private final Mob wirt;
    private final IFlyingCreature flieger;

    public StartFlyingGoal(Mob wirt, IFlyingCreature flieger) {
        this.wirt = wirt;
        this.flieger = flieger;
    }

    @Override
    public boolean canUse() {
        if(this.flieger.getFlyingState() != IFlyingCreature.STATE_WALKING) return false;
        return this.wirt.getLastHurtByMob() != null || this.wirt.isOnFire() || this.wirt.getRandom().nextInt(600) == 0;
    }

    @Override
    public void start() {
        this.flieger.setFlyingState(IFlyingCreature.STATE_FLYING);
    }
}
