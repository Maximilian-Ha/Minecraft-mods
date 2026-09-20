package com.hbm.items.armor;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.IAttackHandler.
 *
 * Ein Ruestungsteil, das einen Angriff noch ganz absagen darf. Anders als die gleichnamige
 * Methode an ArmorFSBItem, die nur an der Brustplatte gefragt wird, wird dieser Haken an
 * ALLEN VIER Plaetzen gefragt -- so steht es im Original (ModEventHandler Z. 682).
 *
 * In 1.21 heisst das Ereignis LivingIncomingDamageEvent statt LivingAttackEvent.
 */
public interface IAttackHandlerItem {

    void handleAttack(LivingIncomingDamageEvent event, ItemStack armor);
}
