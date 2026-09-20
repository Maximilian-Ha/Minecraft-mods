package com.hbm.items.armor;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.IDamageHandler.
 *
 * Ein Ruestungsteil, das den Schaden verrechnen darf, der durchgekommen ist. Wie
 * IAttackHandlerItem wird auch dieser Haken an ALLEN VIER Plaetzen gefragt
 * (ModEventHandler Z. 736), nicht nur an der Brustplatte.
 *
 * In 1.21 heisst das Ereignis LivingDamageEvent.Pre statt LivingHurtEvent.
 */
public interface IDamageHandlerItem {

    void handleDamage(LivingDamageEvent.Pre event, ItemStack stack);
}
