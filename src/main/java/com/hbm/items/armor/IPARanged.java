package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;

import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.IPARanged.
 *
 * Das Gegenstueck zu IPAMelee fuer die Fernwaffe. Nur die NCR-Ruestung hat eine -- die
 * Remnant gibt hier null zurueck, und dann tut gun_pa_ranged nichts.
 */
public interface IPARanged {

    void clickPrimary(ItemStack stack, LambdaContext ctx);
    void clickSecondary(ItemStack stack, LambdaContext ctx);
}
