package com.hbm.hazard.modifier;

import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.hazard.modifier.HazardModifierRBMKHot.
 *
 * Wie stark ein RBMK-Brennstab den Traeger verbrennt. Ab hundert Grad Huellentemperatur steigt
 * es um eine Stufe je zehn Grad, bei sechzig Stufen ist Schluss.
 */
public class HazardModifierRBMKHot extends HazardModifier {

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {

        if(!(stack.getItem() instanceof RBMKRodItem)) return 0F;

        double heat = RBMKRodItem.getHullHeat(stack);
        return (float) Math.min(Math.ceil((heat - 100D) / 10D), 60D);
    }
}
