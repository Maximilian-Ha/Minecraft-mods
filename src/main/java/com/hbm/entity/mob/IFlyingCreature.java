package com.hbm.entity.mob;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.IFlyingCreature.
 *
 * Ein Wesen, das zwischen Laufen und Fliegen wechselt. Die Aufgaben StartFlyingGoal und
 * StopFlyingGoal schalten um, alle anderen fragen den Zustand ab.
 */
public interface IFlyingCreature {

    int STATE_WALKING = 0;
    int STATE_FLYING = 1;

    int getFlyingState();

    void setFlyingState(int state);
}
