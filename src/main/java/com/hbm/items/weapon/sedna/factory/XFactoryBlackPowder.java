package com.hbm.items.weapon.sedna.factory;

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
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryBlackPowder.
 *
 * Die Schwarzpulvermunition und die Pfefferbuechse -- die erste Waffe des Spiels. Sie hat sechs
 * Laeufe, die von Hand weitergedreht werden; nachgeladen wird mit einem Schnelllader, der alle
 * sechs auf einmal fuellt.
 *
 * Die vier Sorten unterscheiden sich vor allem darin, wie sie abprallen: Stein streut und prallt
 * kaum ab, Feuerstein durchschlaegt, Eisen prallt bis zu fuenfmal in fast jedem Winkel ab, und
 * Schrot gibt sechs Kugeln mit einem Sechstel Schaden.
 */
public class XFactoryBlackPowder {

    public static BulletConfig stone;
    public static BulletConfig flint;
    public static BulletConfig iron;
    public static BulletConfig shot;

    public static void init(DeferredRegister.Items registry) {

        stone = new BulletConfig().setItem(Ammo.STONE).setBlackPowder(true).setHeadshot(1F).setSpread(0.025F).setRicochetAngle(15);
        flint = new BulletConfig().setItem(Ammo.STONE_AP).setBlackPowder(true).setHeadshot(1F).setSpread(0.01F).setRicochetAngle(5).setDoesPenetrate(true).setDamage(1.5F);
        iron = new BulletConfig().setItem(Ammo.STONE_IRON).setBlackPowder(true).setHeadshot(1F).setSpread(0F).setRicochetAngle(90).setRicochetCount(5).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F);
        shot = new BulletConfig().setItem(Ammo.STONE_SHOT).setBlackPowder(true).setHeadshot(1F).setSpread(0.1F).setRicochetAngle(45).setProjectiles(6, 6).setDamage(1F / 6F);

        NtmItems.GUN_PEPPERBOX = registry.register("gun_pepperbox", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(300).draw(4).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(5F).delay(27).reload(67).jam(58).sound(NtmSoundEvents.GUN_POWDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(stone, flint, iron, shot))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_PEPPERBOX))
                .setupStandardConfiguration()
                .anim(LAMBDA_PEPPERBOX_ANIMS).orchestra(Orchestras.ORCHESTRA_PEPPERBOX)
        ).setDefaultAmmo(Ammo.STONE, 12));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_PEPPERBOX = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_PEPPERBOX_ANIMS = (stack, type) -> {
        return switch(type) {
            case CYCLE -> new BusAnimation()
                    .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 0, 1025).addPos(60, 0, 0, 250))
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(45, 0, 0, 150, IType.SIN_DOWN).addPos(45, 0, 0, 50).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(80, 0, 0, 25).addPos(80, 0, 0, 1000).addPos(0, 0, 0, 250))
                    .addBus("TRIGGER", new BusAnimationSequence().addPos(1, 0, 0, 25).addPos(1, 0, 0, 250).addPos(0, 0, 0, 100));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 0, 525).addPos(60, 0, 0, 250))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(80, 0, 0, 25).addPos(80, 0, 0, 500).addPos(0, 0, 0, 250))
                    .addBus("TRIGGER", new BusAnimationSequence().addPos(1, 0, 0, 25).addPos(1, 0, 0, 250).addPos(0, 0, 0, 100));
            case EQUIP -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 200, IType.SIN_DOWN));
            case RELOAD -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(90, 0, 0, 500, IType.SIN_FULL).addPos(90, 0, 0, 1600).addPos(0, 0, 0, 500, IType.SIN_FULL).addPos(-5, 0, 0, 200, IType.SIN_UP).addPos(0, 0, 0, 200, IType.SIN_DOWN))
                    .addBus("TRANSLATE", new BusAnimationSequence().addPos(0, -12, 5, 500, IType.SIN_FULL).addPos(0, -12, 5, 700).addPos(0, -13, 5, 200).addPos(0, -12, 5, 200).addPos(0, -12, 5, 500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("LOADER", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 5, -5, 0).addPos(0, 0, -0.1, 500, IType.SIN_FULL).addPos(0, 0, -1, 200).addPos(0, 0, -1, 200).addPos(0, 0, -0.1, 200).addPos(0, 5, -5, 500, IType.SIN_FULL).addPos(0, 0, 0, 0))
                    .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 0, 2600).addPos(-360, 0, 0, 750, IType.SIN_FULL))
                    .addBus("SHOT", new BusAnimationSequence().addPos(1, 0, 0, 1400).addPos(0, 0, 0, 0));
            case INSPECT -> new BusAnimation()
                    .addBus("ROTATE", new BusAnimationSequence().addPos(-360, 0, 0, 750, IType.SIN_FULL))
                    .addBus("RECOIL", new BusAnimationSequence().addPos(-5, 0, 0, 200, IType.SIN_UP).addPos(0, 0, 0, 200, IType.SIN_DOWN));
            case JAMMED -> new BusAnimation()
                    .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 0, 1300).addPos(60, 0, 0, 500, IType.SIN_FULL).addPos(60, 0, 0, 400).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("TRANSLATE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, -6, 0, 400, IType.SIN_FULL).addPos(0, -6, 0, 2000).addPos(0, 0, 0, 400, IType.SIN_FULL))
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(45, 0, 0, 400, IType.SIN_FULL).addPos(45, 0, 0, 2000).addPos(0, 0, 0, 400, IType.SIN_FULL));
            default -> null;
        };
    };
}
