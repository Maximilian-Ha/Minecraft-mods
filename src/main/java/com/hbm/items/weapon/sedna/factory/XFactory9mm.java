package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.main.NuclearTechMod;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;

/**
 * Die Fabrik fuer die 9-mm-Munition und die Waffen, die sie verschiessen.
 *
 * Vom Original sind die Patronen, die M3 und die Uzi uebernommen; die LAG-Pistole folgt, sobald
 * ihr Modell portiert ist.
 */
public class XFactory9mm {

    public static BulletConfig p9_sp;
    public static BulletConfig p9_fmj;
    public static BulletConfig p9_jhp;
    public static BulletConfig p9_ap;

    public static void init(DeferredRegister.Items registry) {

        SpentCasing casing9 = new SpentCasing(SpentCasingType.STRAIGHT).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(1F, 1F, 0.75F);
        p9_sp = new BulletConfig().setItem(Ammo.P9_SP).setCasing(CasingType.SMALL, 12)
                .setCasing(casing9.clone().register("p9"));
        p9_fmj = new BulletConfig().setItem(Ammo.P9_FMJ).setCasing(CasingType.SMALL, 12).setDamage(0.8F).setThresholdNegation(2F).setArmorPiercing(0.1F)
                .setCasing(casing9.clone().register("p9fmj"));
        p9_jhp = new BulletConfig().setItem(Ammo.P9_JHP).setCasing(CasingType.SMALL, 12).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing9.clone().register("p9jhp"));
        p9_ap = new BulletConfig().setItem(Ammo.P9_AP).setCasing(CasingType.SMALL_STEEL, 12).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(5F).setArmorPiercing(0.15F)
                .setCasing(casing9.clone().setColor(SpentCasing.COLOR_CASE_44).register("p9ap"));

        /* Steckt der aufgearbeitete Schaft, heisst die Waffe M3 statt Grease Gun. */
        NtmItems.GUN_GREASEGUN = registry.register("gun_greasegun", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(3_000).draw(20).inspect(31).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(3F).delay(4).dry(40).auto(true).spread(0.015F).reload(60).jam(55).sound(NtmSoundEvents.GUN_GREASEGUN_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(p9_sp, p9_fmj, p9_jhp, p9_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_GREASEGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_GREASEGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_GREASEGUN)
        ).setDefaultAmmo(Ammo.P9_SP, 30).setNameMutator(LAMBDA_NAME_GREASEGUN));

        /*
         * Die Uzi: dreissig Schuss, zwei Ticks Abstand, drei Schaden je Treffer. Mit Schalldaempfer
         * traegt sie einen anderen Namen.
         */
        NtmItems.GUN_UZI = registry.register("gun_uzi", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(3_000).draw(15).inspect(31).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(3F).delay(2).dry(25).auto(true).spread(0.005F).reload(55).jam(50).sound(NtmSoundEvents.GUN_UZI_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(p9_sp, p9_fmj, p9_jhp, p9_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_UZI))
                .setupStandardConfiguration()
                .anim(LAMBDA_UZI_ANIMS).orchestra(Orchestras.ORCHESTRA_UZI)
        ).setDefaultAmmo(Ammo.P9_SP, 30).setNameMutator(LAMBDA_NAME_UZI));

        /*
         * Die beidhaendige Uzi. Sechzig Schuss auf zwei Magazine, keine Hueftstreuung, und der
         * zweite Empfaenger braucht einen eigenen Entscheider: der Standardentscheider laedt beim
         * Dauerfeuer nur nach, wenn die LINKE Maustaste haelt -- der zweite Lauf haengt aber an
         * der rechten.
         */
        NtmItems.GUN_UZI_AKIMBO = registry.register("gun_uzi_akimbo", () -> new GunBaseNTItem(WeaponQuality.B_SIDE,
                new GunConfig()
                        .dura(3_000).draw(15).inspect(31).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(3F).spreadHipfire(0F).delay(2).dry(25).auto(true).spread(0.005F).reload(55).jam(50).sound(NtmSoundEvents.GUN_UZI_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineFullReload(0, 30).addConfigs(p9_sp, p9_fmj, p9_jhp, p9_ap))
                                .offset(1, -0.0625 * 2.5, 0.375D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_UZI))
                        .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_UZI_ANIMS).orchestra(Orchestras.ORCHESTRA_UZI_AKIMBO),
                new GunConfig()
                        .dura(3_000).draw(15).inspect(31).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(3F).spreadHipfire(0F).delay(2).dry(25).auto(true).spread(0.005F).reload(55).jam(50).sound(NtmSoundEvents.GUN_UZI_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineFullReload(1, 30).addConfigs(p9_sp, p9_fmj, p9_jhp, p9_ap))
                                .offset(1, -0.0625 * 2.5, -0.375D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_UZI))
                        .ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(LAMBDA_SECOND_UZI)
                        .anim(LAMBDA_UZI_ANIMS).orchestra(Orchestras.ORCHESTRA_UZI_AKIMBO)
        ).setDefaultAmmo(Ammo.P9_SP, 60));
    }

    /** Steckt ein Schalldaempfer, heisst die Uzi Richter. */
    public static Function<ItemStack, Component> LAMBDA_NAME_UZI = (stack) -> {
        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER)) {
            return Component.translatable(stack.getItem().getDescriptionId() + ".richter");
        }
        return null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_UZI = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(1, (float) (ctx.getPlayer().random.nextGaussian() * 0.25));
    };

    /** Wie der Standardentscheider, nur dass das Dauerfeuer an der rechten Maustaste haengt. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SECOND_UZI = (stack, ctx) -> {
        int index = ctx.configIndex;
        GunState lastState = GunBaseNTItem.getState(stack, index);
        GunStateDecider.deciderStandardFinishDraw(stack, lastState, index);
        GunStateDecider.deciderStandardClearJam(stack, lastState, index);
        GunStateDecider.deciderStandardReload(stack, ctx, lastState, 0, index);
        GunStateDecider.deciderAutoRefire(stack, ctx, lastState, 0, index,
                () -> GunBaseNTItem.getSecondary(stack, index) && GunBaseNTItem.getMode(stack, ctx.configIndex) == 0);
    };

    /** Mit dem aufgearbeiteten Schaft ist es keine zusammengeschusterte Kriegsware mehr. */
    public static Function<ItemStack, Component> LAMBDA_NAME_GREASEGUN = (stack) -> {
        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_GREASEGUN_CLEAN)) {
            return Component.translatable("item.hbmsntm.gun_greasegun.refurbished");
        }
        return null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_GREASEGUN = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(2, (float) (ctx.getPlayer().random.nextGaussian() * 0.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 2000, 0.05D, 1.1D, ctx.configIndex);
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_GREASEGUN_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(80, 0, 0, 0).addPos(80, 0, 0, 500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("STOCK", new BusAnimationSequence().addPos(0, 0, -4, 0).addPos(0, 0, -4, 200).addPos(0, 0, 0, 300, IType.SIN_FULL));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.25 : -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("FLAP", new BusAnimationSequence().addPos(0, 0, 15, 100, IType.SIN_DOWN).addPos(0, 0, -5, 100, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_FULL));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, -45, 250, IType.SIN_FULL).addPos(0, 0, -45, 750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 250).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case RELOAD -> {
                boolean empty = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().getInventory()) <= 0;
                yield new BusAnimation()
                        .addBus("MAG", new BusAnimationSequence().addPos(0, -8, 0, 250, IType.SIN_UP).addPos(0, -8, 0, 750).addPos(0, 0, 0, 500, IType.SIN_DOWN))
                        .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                        .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 1750).addPos(0, 0, -45, 250, IType.SIN_FULL).addPos(0, 0, -45, 500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                        .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, 0, 2000).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                        .addBus("BULLET", new BusAnimationSequence().addPos(empty ? 1 : 0, 0, 0, 0).addPos(0, 0, 0, 1000));
            }
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, -45, 250, IType.SIN_FULL).addPos(0, 0, -45, 1500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 250).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case INSPECT -> new BusAnimation()
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, -45, 150).addPos(0, 0, 45, 150).addPos(0, 0, 45, 50).addPos(0, 0, 0, 250).addPos(0, 0, 0, 500).addPos(0, 0, 45, 150).addPos(0, 0, -45, 150).addPos(0, 0, 0, 150))
                    .addBus("FLAP", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(0, 0, 180, 150).addPos(0, 0, 180, 850).addPos(0, 0, 0, 150));
            default -> null;
        };
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_UZI_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(80, 0, 0, 0).addPos(80, 0, 0, 500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("STOCKBACK", new BusAnimationSequence().addPos(-200, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("STOCKFRONT", new BusAnimationSequence().addPos(180, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.5 : -0.75, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 500).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, -2, 150, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
            case RELOAD -> {
                boolean empty = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().getInventory()) <= 0;
                yield new BusAnimation()
                        .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, -10, 0, 250, IType.SIN_UP).addPos(0, -10, 0, 750).addPos(0, 0, 0, 500, IType.SIN_DOWN))
                        .addBus("LIFT", new BusAnimationSequence().addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 2000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                        .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 2000).addPos(0, 0, -2, 150, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("BULLET", new BusAnimationSequence().addPos(empty ? 0 : 1, 0, 0, 0).addPos(empty ? 0 : 1, 0, 0, 500).addPos(1, 0, 0, 0));
            }
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1250).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -2, 150, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 500).addPos(0, 0, -2, 150, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
            case INSPECT -> new BusAnimation()
                    .addBus("YEET", new BusAnimationSequence().addPos(0, -1, 0, 100).addPos(0, 0, 0, 100, IType.SIN_UP).addPos(0, 12, 0, 350, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_UP).addPos(0, -1, 0, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("SPEEN", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(-360, 0, 0, 600));
            default -> null;
        };
    };
}
