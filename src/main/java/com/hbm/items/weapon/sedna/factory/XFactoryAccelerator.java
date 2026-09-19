package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.CoinEntity;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.NI4NIGunItem;
import com.hbm.items.weapon.sedna.mags.MagazineInfinite;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.SoundUtils;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryAccelerator.
 *
 * Im Original stehen hier drei Waffen: die Tau-Kanone, die Spulenkanone und die NI4NI.
 * PORTIERT IST NUR DIE DRITTE. Die beiden anderen brauchen Munition, die der Port nicht hat
 * (Tau-Ladungen und Spulengeschosse aus Wolfram und Ferrouran), und die gehoeren in eine
 * eigene Runde -- nicht in diese, die von der C-130 herkommt.
 *
 * DIE NI4NI IST EINE MUENZWAFFE. Sie hat unendlich Munition und keine Haltbarkeit; ihr Witz
 * liegt darin, dass der Zweitdruck eine Muenze in die Luft wirft und der Strahl an ihr
 * abknickt, um sich das naechste Ziel zu suchen. Ohne Muenze ist sie eine mittelmaessige
 * Pistole, mit Muenzen schiesst sie um die Ecke.
 */
public class XFactoryAccelerator {

    public static BulletConfig ni4ni_arc;

    public static void init(DeferredRegister.Items registry) {

        /* Der Lichtbogen. Er durchschlaegt zehn Punkte Panzerung, laesst ein Fuenftel des
         * Rests weg und bleibt am ersten Ziel stehen -- durchschlagen tut er NICHT, sonst
         * waere der Muenzknick sinnlos. */
        ni4ni_arc = new BulletConfig().setupDamageClass(DamageClass.PHYSICAL).setBeam().setLife(5)
                .setThresholdNegation(10F).setArmorPiercing(0.2F).setRenderRotations(false).setDoesPenetrate(false)
                .setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);

        NtmItems.GUN_N_I_4_N_I = registry.register("gun_n_i_4_n_i", () -> new NI4NIGunItem(WeaponQuality.SPECIAL, new GunConfig()
                .dura(0).draw(5).inspect(39).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(35F).delay(10).sound(NtmSoundEvents.GUN_COIL_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineInfinite(ni4ni_arc))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().fire(Lego.LAMBDA_NOWEAR_FIRE))
                .setupStandardConfiguration()
                .ps(LAMBDA_NI4NI_SECONDARY_PRESS)
                .anim(LAMBDA_NI4NI_ANIMS).orchestra(Orchestras.ORCHESTRA_COILGUN)
        ));
    }

    /**
     * Der Zweitdruck wirft eine Muenze. Sie fliegt aus Augenhoehe mit vier Fuenfteln der
     * Blickrichtung los und bekommt einen halben Block Auftrieb -- sie steigt also erst, bevor
     * sie faellt, und steht dadurch einen Moment lang still genug, um sie zu treffen.
     *
     * OHNE MUENZE PASSIERT NICHTS. Der Zaehler laeuft in NI4NIGunItem weiter und fuellt ihn
     * nach.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_NI4NI_SECONDARY_PRESS = (stack, ctx) -> {

        Player spieler = ctx.getPlayer();
        if(spieler == null) return;
        if(spieler.level.isClientSide) return;
        if(NI4NIGunItem.getCoinCount(stack) <= 0) return;

        Vec3 blick = spieler.getLookAngle().scale(0.8D);

        CoinEntity muenze = new CoinEntity(spieler.level, spieler);
        muenze.setPos(spieler.getX(), spieler.getEyeY() - 0.125D, spieler.getZ());
        muenze.setDeltaMovement(blick.x, blick.y + 0.5D, blick.z);
        muenze.setYRot(spieler.getYRot());
        spieler.level.addFreshEntity(muenze);

        SoundUtils.playAtVec3(spieler.level, spieler.position(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                spieler.getSoundSource(), 1.0F, 1F + spieler.getRandom().nextFloat() * 0.25F);

        NI4NIGunItem.setCoinCount(stack, NI4NIGunItem.getCoinCount(stack) - 1);
    };

    /**
     * Drei Bewegungen. Beim Ziehen und beim Betrachten wirbelt die Waffe um die eigene Achse
     * -- zweimal herum beim Ziehen, dreimal beim Betrachten.
     *
     * DER RUECKSTOSS HAENGT AM ZIELEN: aus der Huefte kippt sie dreissig Grad hoch, ueber Kimme
     * und Korn nur fuenf. Die Trommel dreht sich dabei um hundertzwanzig Grad weiter, also um
     * ein Drittel.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_NI4NI_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-360 * 2, 0, 0, 500));
            case CYCLE -> {
                boolean zielt = GunBaseNTItem.getIsAiming(stack);
                yield new BusAnimation()
                        .addBus("RECOIL", new BusAnimationSequence().addPos(zielt ? -5 : -30, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                        .addBus("DRUM", new BusAnimationSequence().hold(50).addPos(0, 0, 120, 300, IType.SIN_FULL));
            }
            case INSPECT -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-360 * 3, 0, 0, 750).hold(100).addPos(0, 0, 0, 750));
            default -> null;
        };
    };
}
