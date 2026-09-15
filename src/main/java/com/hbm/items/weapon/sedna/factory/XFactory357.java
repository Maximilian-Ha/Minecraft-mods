package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory357.
 *
 * Die .357-Patronen und der leichte Revolver. Zwei Ausfuehrungen derselben Waffe: die gewoehnliche
 * und die Atlas, die bei gleicher Bedienung zwei Drittel mehr Schaden macht.
 *
 * Die dritte Ausfuehrung ist die DANI: ein beidhaendiges Paar, dessen zwei Revolver sich nicht
 * einmal die Textur teilen -- der linke traegt die Sonne, der rechte den Mond. Sie schiessen
 * ausserdem verschieden hoch, der eine einen Zehntel ueber, der andere einen Zehntel unter dem
 * gewoehnlichen Ton.
 */
public class XFactory357 {

    public static BulletConfig m357_bp;
    public static BulletConfig m357_sp;
    public static BulletConfig m357_fmj;
    public static BulletConfig m357_jhp;
    public static BulletConfig m357_ap;
    public static BulletConfig m357_express;

    public static void init(DeferredRegister.Items registry) {

        m357_bp = new BulletConfig().setItem(Ammo.M357_BP).setCasing(CasingType.SMALL, 16).setDamage(0.75F).setBlackPowder(true);
        m357_sp = new BulletConfig().setItem(Ammo.M357_SP).setCasing(CasingType.SMALL, 8);
        m357_fmj = new BulletConfig().setItem(Ammo.M357_FMJ).setCasing(CasingType.SMALL, 8).setDamage(0.8F).setThresholdNegation(2F).setArmorPiercing(0.1F);
        m357_jhp = new BulletConfig().setItem(Ammo.M357_JHP).setCasing(CasingType.SMALL, 8).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F);
        m357_ap = new BulletConfig().setItem(Ammo.M357_AP).setCasing(CasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(5F).setArmorPiercing(0.15F);
        m357_express = new BulletConfig().setItem(Ammo.M357_EXPRESS).setCasing(CasingType.SMALL, 8).setDoesPenetrate(true).setDamage(1.5F).setThresholdNegation(2F).setArmorPiercing(0.1F).setWear(1.5F);

        NtmItems.GUN_LIGHT_REVOLVER = registry.register("gun_light_revolver", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, revolver(7.5F))
                .setDefaultAmmo(Ammo.M357_SP, 12));

        NtmItems.GUN_LIGHT_REVOLVER_ATLAS = registry.register("gun_light_revolver_atlas", () -> new GunBaseNTItem(WeaponQuality.B_SIDE, revolver(12.5F))
                .setDefaultAmmo(Ammo.M357_JHP, 12));

        NtmItems.GUN_LIGHT_REVOLVER_DANI = registry.register("gun_light_revolver_dani", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY,
                dani(0, 1.1F).pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_DANI_ANIMS).orchestra(Orchestras.ORCHESTRA_DANI),
                dani(1, 0.9F).ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_DANI_ANIMS).orchestra(Orchestras.ORCHESTRA_DANI)
        ).setDefaultAmmo(Ammo.M357_EXPRESS, 24));
    }

    /**
     * Ein Lauf der DANI. Die Tonhoehe und der Versatz nach links oder rechts unterscheiden die
     * beiden; alles andere ist gleich. Das Magazin haengt am Empfaengerindex, damit jede Haelfte
     * ihre eigenen sechs Patronen hat.
     */
    private static GunConfig dani(int index, float pitch) {
        return new GunConfig()
                .dura(30_000).draw(20).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(15F).spreadHipfire(0F).delay(11).reload(55).jam(45).sound(NtmSoundEvents.GUN_PISTOL_FIRE, 1.0F, pitch)
                        .mag(new MagazineFullReload(index, 6).addConfigs(m357_bp, m357_sp, m357_fmj, m357_jhp, m357_ap, m357_express))
                        .offset(0.75, -0.0625, index == 0 ? 0.3125D : -0.3125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_DANI));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_DANI = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(5, (float) (ctx.getPlayer().random.nextGaussian() * 0.75));
    };

    /** Beide Ausfuehrungen sind bis auf den Schaden gleich. */
    private static GunConfig revolver(float damage) {
        return new GunConfig()
                .dura(300).draw(4).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(damage).delay(11).reload(55).jam(45).sound(NtmSoundEvents.GUN_PISTOL_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(m357_bp, m357_sp, m357_fmj, m357_jhp, m357_ap, m357_express))
                        .offset(0.75, -0.0625, -0.3125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ATLAS))
                .setupStandardConfiguration()
                .anim(LAMBDA_ATLAS_ANIMS).orchestra(Orchestras.ORCHESTRA_ATLAS);
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_ATLAS = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_ATLAS_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-90, 0, 0, 0).addPos(0, 0, 0, 350, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(0, 0, 1, 50).addPos(0, 0, 1, 300).addPos(0, 0, 0, 200))
                    .addBus("DRUM", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, 1, 200));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("HAMMER", new BusAnimationSequence().addPos(0, 0, 1, 50).addPos(0, 0, 1, 200).addPos(0, 0, 0, 200))
                    .addBus("DRUM", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, 1, 200));
            case RELOAD -> new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(0, 0, 90, 300).addPos(0, 0, 90, 2000).addPos(0, 0, 0, 150))
                    .addBus("FRONT", new BusAnimationSequence().addPos(0, 0, 0, 200).addPos(0, 0, 45, 150).addPos(0, 0, 45, 2000).addPos(0, 0, 0, 75))
                    .addBus("RELOAD_ROT", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(60, 0, 0, 500).addPos(60, 0, 0, 500).addPos(0, -90, -90, 0).addPos(0, -90, -90, 600).addPos(0, 0, 0, 300).addPos(0, 0, 0, 100).addPos(-45, 0, 0, 50).addPos(-45, 0, 0, 100).addPos(0, 0, 0, 300))
                    .addBus("RELOAD_MOVE", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(0, -15, 0, 1000).addPos(0, 0, 0, 450))
                    .addBus("DRUM_PUSH", new BusAnimationSequence().addPos(0, 0, 0, 1600).addPos(0, 0, -5, 0).addPos(0, 0, 0, 300));
            case INSPECT -> new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(0, 0, 90, 300).addPos(0, 0, 90, 1000).addPos(0, 0, 0, 150))
                    .addBus("FRONT", new BusAnimationSequence().addPos(0, 0, 0, 200).addPos(0, 0, 45, 150).addPos(0, 0, 45, 1000).addPos(0, 0, 0, 75))
                    .addBus("RELOAD_ROT", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(45, 0, 0, 500, IType.SIN_FULL).addPos(45, 0, 0, 500).addPos(-45, 0, 0, 50).addPos(-45, 0, 0, 100).addPos(0, 0, 0, 300))
                    .addBus("RELOAD_MOVE", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(0, -2.5, 0, 500, IType.SIN_FULL).addPos(0, -2.5, 0, 500).addPos(0, 0, 0, 350));
            case JAMMED -> new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 90, 300).addPos(0, 0, 90, 1000).addPos(0, 0, 0, 150))
                    .addBus("FRONT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 200).addPos(0, 0, 45, 150).addPos(0, 0, 45, 1000).addPos(0, 0, 0, 75))
                    .addBus("RELOAD_ROT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 300).addPos(45, 0, 0, 500, IType.SIN_FULL).addPos(45, 0, 0, 500).addPos(-45, 0, 0, 50).addPos(-45, 0, 0, 100).addPos(0, 0, 0, 300))
                    .addBus("RELOAD_MOVE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 300).addPos(0, -2.5, 0, 500, IType.SIN_FULL).addPos(0, -2.5, 0, 500).addPos(0, 0, 0, 350));
            default -> null;
        };
    };

    /** Die DANI wirbelt beim Ziehen dreimal um die eigene Achse; sonst ist sie der Atlas gleich. */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_DANI_ANIMS = (stack, type) -> {
        if(type == GunAnimation.EQUIP) return new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(360 * 3, 0, 0, 1000, IType.SIN_DOWN));
        return LAMBDA_ATLAS_ANIMS.apply(stack, type);
    };
}
