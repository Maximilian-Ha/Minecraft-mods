package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.NtmItems;
import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPARanged;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryPA.
 *
 * DIE BEIDEN WAFFEN DER PANZERRUESTUNGEN, und sie sind keine Waffen. Beide tun aus sich
 * heraus gar nichts: sie reichen jeden Klick, jeden Bewegungssatz und jeden Ton an die
 * getragene Brustplatte weiter. Wer keine Panzerruestung traegt, haelt zwei Gegenstaende, die
 * sich weigern, irgendetwas zu tun -- und genau so steht es im Original.
 *
 * WARUM DAS SO GEBAUT IST: der Nahkampf einer Panzerruestung sind ihre Arme, und die gehoeren
 * der Ruestung, nicht der Waffe. Die Remnant schlaegt mit Faeusten, die NCR mit Klingen; die
 * Bewegungen, der Schaden und die Klaenge unterscheiden sich vollstaendig. Waeren sie in der
 * Waffe, muesste jede Waffe jede Ruestung kennen.
 *
 * DIE FERNWAFFE HAT NUR EINEN GEBER: die NCR-Ruestung. Die Remnant gibt hier null zurueck,
 * und dann tut gun_pa_ranged nichts. Das ist kein Loch im Port, sondern die Regel des
 * Originals.
 */
public class XFactoryPA {

    public static void init(DeferredRegister.Items registry) {

        NtmItems.GUN_PA_MELEE = registry.register("gun_pa_melee", () -> new GunPAItem(WeaponQuality.UTILITY, new GunConfig()
                .draw(10).crosshair(Crosshair.NONE)
                .rec(new Receiver(0))
                .pp(LAMBDA_CLICK_MELEE_PRIMARY).ps(LAMBDA_CLICK_MELEE_SECONDARY)
                .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                .anim(LAMBDA_MELEE_ANIMS).orchestra(ORCHESTRA)
        ));

        NtmItems.GUN_PA_RANGED = registry.register("gun_pa_ranged", () -> new GunPAItem(WeaponQuality.UTILITY, new GunConfig()
                .draw(0).crosshair(Crosshair.CROSS)
                .rec(new Receiver(0))
                .pp(LAMBDA_CLICK_RANGED_PRIMARY).ps(LAMBDA_CLICK_RANGED_SECONDARY)
                .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
        ));
    }

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA = (stack, ctx) -> {
        IPAMelee bauteil = IPAWeaponsProvider.getMeleeComponentCommon(ctx.getPlayer());
        if(bauteil != null) bauteil.orchestra(stack, ctx);
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MELEE_ANIMS = (stack, type) -> {
        IPAMelee bauteil = IPAWeaponsProvider.getMeleeComponentClient();
        return bauteil != null ? bauteil.playAnim(stack, type) : null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_CLICK_MELEE_PRIMARY = (stack, ctx) -> {
        IPAMelee bauteil = IPAWeaponsProvider.getMeleeComponentCommon(ctx.getPlayer());
        if(bauteil != null) bauteil.clickPrimary(stack, ctx);
    };
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_CLICK_MELEE_SECONDARY = (stack, ctx) -> {
        IPAMelee bauteil = IPAWeaponsProvider.getMeleeComponentCommon(ctx.getPlayer());
        if(bauteil != null) bauteil.clickSecondary(stack, ctx);
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_CLICK_RANGED_PRIMARY = (stack, ctx) -> {
        IPARanged bauteil = IPAWeaponsProvider.getRangedComponentCommon(ctx.getPlayer());
        if(bauteil != null) bauteil.clickPrimary(stack, ctx);
    };
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_CLICK_RANGED_SECONDARY = (stack, ctx) -> {
        IPARanged bauteil = IPAWeaponsProvider.getRangedComponentCommon(ctx.getPlayer());
        if(bauteil != null) bauteil.clickSecondary(stack, ctx);
    };

    /**
     * Einen Schlag ansetzen -- aber nur aus dem Ruhezustand heraus. Waehrend die vorige
     * Bewegung noch laeuft, tut ein weiterer Klick nichts; das Abklingen ist die ganze
     * Schlagfrequenz.
     */
    public static void doSwing(ItemStack stack, LambdaContext ctx, GunAnimation anim, int cooldown) {

        Player spieler = ctx.getPlayer();
        if(spieler == null) return;

        int index = ctx.configIndex;
        if(GunBaseNTItem.getState(stack, index) != GunState.IDLE) return;

        GunBaseNTItem.playAnimation(spieler, stack, anim, index);
        GunBaseNTItem.setState(stack, index, GunState.COOLDOWN);
        GunBaseNTItem.setTimer(stack, index, cooldown);
    }

    /**
     * Die Waffe selbst. Sie unterscheidet sich von einer gewoehnlichen nur in einem Punkt:
     * sie sagt nichts ueber sich. Eine Munitionsliste, eine Schadenszahl oder eine Angabe zur
     * Haltbarkeit waere hier irrefuehrend -- all das gehoert der Ruestung.
     */
    public static class GunPAItem extends GunBaseNTItem {

        public GunPAItem(WeaponQuality quality, GunConfig... cfg) {
            super(quality, cfg);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) { }
    }
}
