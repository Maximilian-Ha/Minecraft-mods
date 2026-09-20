package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.IPAMelee.
 *
 * DER NAHKAMPF EINER PANZERRUESTUNG GEHOERT DER RUESTUNG, NICHT DER WAFFE. gun_pa_melee ist
 * nur ein Griff: es fragt bei jedem Klick die getragene Weste, was zu tun ist. Wer keine
 * traegt, haelt einen Gegenstand, der nichts kann -- und genau so ist es gedacht.
 */
public interface IPAMelee {

    /* Beide nur auf dem Client -- MultiBufferSource gibt es auf dem Server nicht. */
    @OnlyIn(Dist.CLIENT) void setupFirstPerson(ItemStack stack);
    @OnlyIn(Dist.CLIENT) void renderFirstPerson(ItemStack stack, MultiBufferSource buffer);

    BusAnimation playAnim(ItemStack stack, GunAnimation type);
    void orchestra(ItemStack stack, LambdaContext ctx);

    void clickPrimary(ItemStack stack, LambdaContext ctx);
    void clickSecondary(ItemStack stack, LambdaContext ctx);
}
