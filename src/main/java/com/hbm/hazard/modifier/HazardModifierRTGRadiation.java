package com.hbm.hazard.modifier;

import com.hbm.items.machine.RTGPelletItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.hazard.modifier.HazardModifierRTGRadiation.
 *
 * Ein RTG-Pellet strahlt umso naeher am Zielwert, je weiter es zerfallen ist.
 *
 * Anders als die allgemeine Brennstoffkurve rechnet dieser Modifikator den Zerfall direkt aus
 * Rest- und Hoechstlebensdauer statt aus dem Fortschrittsbalken -- das ist genauer, weil der
 * Balken auf vierzehn Stufen gerastert ist.
 */
public class HazardModifierRTGRadiation extends HazardModifier {

    private final float target;

    public HazardModifierRTGRadiation(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {

        if(!(stack.getItem() instanceof RTGPelletItem)) return level;

        long max = RTGPelletItem.getMaxLifespan(stack);
        if(max <= 0) return level;

        double depletion = 1D - (double) RTGPelletItem.getLifespan(stack) / (double) max;

        return (float) (level + (this.target - level) * depletion);
    }
}
