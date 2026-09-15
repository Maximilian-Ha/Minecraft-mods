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
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory22lr.
 *
 * Die .22-Patronen und die beiden Waffen, die sie verschiessen.
 *
 * Die AM180 ist ein Trommelmagazingewehr mit 177 Schuss und einem Schuss je Tick -- die
 * schnellste Waffe des Originals. Jede einzelne Kugel macht nur zwei Schaden, und der Rueckstoss
 * ist reines Rauschen; sie trifft durch Masse, nicht durch Genauigkeit. Ihre Patronen stossen
 * ausserdem nicht zurueck (setKnockback(0)), sonst schoebe ein Dauerfeuer das Ziel aus der Welt.
 *
 * Die Star-F ist die zugehoerige Pistole: fuenfzehn Schuss, halbautomatisch, deutlich mehr
 * Schaden je Treffer.
 *
 * ABWEICHUNGEN:
 * - NICHT UEBERNOMMEN ist die dritte Waffe, die beidhaendige Star-F-Akimbo. Sie gehoert zu den
 *   beidhaendigen Waffen und kommt mit ihnen, zusammen mit der DANI aus Runde 72.
 * - Der Namenswechsel bei gestecktem Schalldaempfer ist ab Runde 74 wieder da.
 * - Das Original haelt fuer die AM180 zwei Animationssaetze bereit, umgeschaltet ueber
 *   ClientConfig.GUN_ANIMS_LEGACY. Den Schalter gibt es im Port nicht; hier steht der neue Satz
 *   aus der Animationsdatei am180.json -- derselbe Weg, den die SPAS-12 schon geht.
 */
public class XFactory22lr {

    public static BulletConfig p22_sp;
    public static BulletConfig p22_fmj;
    public static BulletConfig p22_jhp;
    public static BulletConfig p22_ap;

    public static void init(DeferredRegister.Items registry) {

        SpentCasing casing22 = new SpentCasing(SpentCasingType.STRAIGHT).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(0.5F);

        p22_sp = new BulletConfig().setItem(Ammo.P22_SP).setCasing(CasingType.SMALL, 24).setKnockback(0F)
                .setCasing(casing22.clone().register("p22"));
        p22_fmj = new BulletConfig().setItem(Ammo.P22_FMJ).setCasing(CasingType.SMALL, 24).setKnockback(0F).setDamage(0.8F).setThresholdNegation(1F).setArmorPiercing(0.1F)
                .setCasing(casing22.clone().register("p22fmj"));
        p22_jhp = new BulletConfig().setItem(Ammo.P22_JHP).setCasing(CasingType.SMALL, 24).setKnockback(0F).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing22.clone().register("p22jhp"));
        p22_ap = new BulletConfig().setItem(Ammo.P22_AP).setCasing(CasingType.SMALL_STEEL, 24).setKnockback(0F).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(2.5F).setArmorPiercing(0.15F)
                .setCasing(casing22.clone().setColor(SpentCasing.COLOR_CASE_44).register("p22ap"));

        NtmItems.GUN_AM180 = registry.register("gun_am180", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(177 * 25).draw(15).inspect(38).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(2F).delay(1).dry(10).auto(true).spread(0.01F).reload(66).jam(30).sound(NtmSoundEvents.GUN_GREASEGUN_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 177).addConfigs(p22_sp, p22_fmj, p22_jhp, p22_ap))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AM180))
                .setupStandardConfiguration()
                .anim(LAMBDA_AM180_ANIMS).orchestra(Orchestras.ORCHESTRA_AM180)
        ).setDefaultAmmo(Ammo.P22_SP, 35).setNameMutator(LAMBDA_NAME_SILENCED));

        NtmItems.GUN_STAR_F = registry.register("gun_star_f", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(15 * 25).draw(15).inspect(38).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(12.5F).delay(5).dry(17).spread(0.01F).reload(40).jam(32).sound(NtmSoundEvents.GUN_STARF_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 15).addConfigs(p22_sp, p22_fmj, p22_jhp, p22_ap))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_STAR_F))
                .setupStandardConfiguration()
                .anim(LAMBDA_STAR_F_ANIMS).orchestra(Orchestras.ORCHESTRA_STAR_F)
        ).setDefaultAmmo(Ammo.P22_SP, 15).setNameMutator(LAMBDA_NAME_SILENCED));

        /*
         * Die beidhaendige Star-F: zweimal dieselbe Pistole, zweimal fuenfzehn Schuss, aber nur
         * eine einzige Waffe im Inventar. Beide Empfaenger haben ihr eigenes Magazin (0 und 1),
         * ihren eigenen Ladezustand und ihren eigenen Aufsatzsatz -- ein Schalldaempfer links
         * heisst nicht, dass rechts auch einer sitzt. Der erste Empfaenger haengt an der linken
         * Maustaste, der zweite an der rechten.
         */
        NtmItems.GUN_STAR_F_AKIMBO = registry.register("gun_star_f_akimbo", () -> new GunBaseNTItem(WeaponQuality.B_SIDE,
                new GunConfig()
                        .dura(15 * 25).draw(15).inspect(38).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(12.5F).delay(5).dry(17).spread(0.01F).reload(40).jam(32).sound(NtmSoundEvents.GUN_STARF_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineFullReload(0, 15).addConfigs(p22_sp, p22_fmj, p22_jhp, p22_ap))
                                .offset(1, -0.0625 * 1.5, 0.25D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_STAR_F))
                        .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_STAR_F_ANIMS).orchestra(Orchestras.ORCHESTRA_STAR_F_AKIMBO),
                new GunConfig()
                        .dura(15 * 25).draw(15).inspect(38).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(12.5F).delay(5).dry(17).spread(0.01F).reload(40).jam(32).sound(NtmSoundEvents.GUN_STARF_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineFullReload(1, 15).addConfigs(p22_sp, p22_fmj, p22_jhp, p22_ap))
                                .offset(1, -0.0625 * 1.5, -0.25D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_STAR_F))
                        .ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_STAR_F_ANIMS).orchestra(Orchestras.ORCHESTRA_STAR_F_AKIMBO)
        ).setDefaultAmmo(Ammo.P22_SP, 30));
    }

    /** Steckt ein Schalldaempfer, traegt die Waffe einen eigenen Namen. */
    public static Function<ItemStack, Component> LAMBDA_NAME_SILENCED = (stack) -> {
        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER)) {
            return Component.translatable(stack.getItem().getDescriptionId() + ".silenced");
        }
        return null;
    };

    /** Beide Waffen rauchen laenger und duenner als der Standard. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 3000, 0.05D, 1.1D, ctx.configIndex);
    };

    /** Reines Rauschen in beide Richtungen -- die AM180 zieht nicht, sie zittert. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_AM180 = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 0.25),
                (float) (ctx.getPlayer().random.nextGaussian() * 0.25));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_STAR_F = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(2.5F, (float) (ctx.getPlayer().random.nextGaussian() * 0.5));
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_AM180_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
            case CYCLE -> ResourceManager.am180_anim.get("Fire");
            case CYCLE_DRY -> ResourceManager.am180_anim.get("FireDry");
            case RELOAD -> ResourceManager.am180_anim.get("Reload");
            case JAMMED -> ResourceManager.am180_anim.get("Jammed");
            case INSPECT -> ResourceManager.am180_anim.get("Inspect");
            default -> null;
        };
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_STAR_F_ANIMS = (stack, type) -> {

        /* Die letzte Patrone bleibt im Lauf sichtbar; ist keine mehr da, wird sie weggeschoben. */
        int ammo = Minecraft.getInstance().player == null ? 0
                : ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0]
                        .getMagazine(stack).getAmount(stack, Minecraft.getInstance().player.getInventory());

        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, aiming ? -0.125 : -0.5, 15, IType.SIN_DOWN).addPos(0, 0, 0, 35, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, aiming ? -0.5 : -1, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_UP))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 50, IType.SIN_DOWN))
                    .addBus("BULLET", ammo <= 1 ? new BusAnimationSequence().setPos(100, 0, 0) : new BusAnimationSequence().addPos(0, 0, 0, 90).addPos(0, 0.5, 2.25, 50));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("HAMMER", new BusAnimationSequence().addPos(1, 0, 0, 50, IType.SIN_UP).hold(450).addPos(0, 0, 0, 50, IType.SIN_DOWN))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, aiming ? -0.5 : -1, 100, IType.SIN_FULL).hold(100).addPos(0, 0, 0, 75, IType.SIN_UP))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(-3, 0, 0, 175, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BULLET", new BusAnimationSequence().setPos(100, 0, 0));
            case RELOAD -> new BusAnimation()
                    .addBus("TILT", new BusAnimationSequence().addPos(-30, 0, 0, 250, IType.SIN_FULL).hold(1500).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, -1, 100, IType.SIN_FULL).hold(1125).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, -7, -1.5, 300, IType.SIN_UP).hold(400).addPos(0, 0, 0, 300, IType.SIN_UP))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(3, 0, 0, 750, IType.SIN_FULL).addPos(-3, 0, 0, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 200).addPos(0, 0, 15, 300, IType.SIN_FULL).hold(900).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("BULLET", new BusAnimationSequence().setPos(ammo <= 1 ? 100 : 0, 0, 0).hold(750).setPos(0, 0, 0).hold(750).addPos(0, 0.5, 2.25, 50));
            case JAMMED -> new BusAnimation()
                    .addBus("TILT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-30, 0, 0, 150, IType.SIN_FULL).hold(800).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 25, 150, IType.SIN_FULL).hold(800).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 750).addPos(0, 0, -0.5, 100, IType.SIN_FULL).hold(100).addPos(0, 0, 0, 100, IType.SIN_UP).hold(100).addPos(0, 0, -0.5, 100, IType.SIN_FULL).hold(100).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus("BULLET", new BusAnimationSequence().setPos(0, 0.5, 2.25).hold(750).addPos(0, 0.5, 1.25, 100, IType.SIN_FULL).hold(100).addPos(0, 0.5, 2.25, 100, IType.SIN_UP).hold(100).addPos(0, 0.5, 1.25, 100, IType.SIN_FULL).hold(100).addPos(0, 0.5, 2.25, 100, IType.SIN_UP));
            case INSPECT -> new BusAnimation()
                    .addBus("TILT", new BusAnimationSequence().addPos(-30, 0, 0, 250, IType.SIN_FULL).hold(1500).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 25, 250, IType.SIN_FULL).hold(1500).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 350).addPos(0, 0, -0.5, 100, IType.SIN_FULL).hold(1125).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus("BULLET", ammo <= 1 ? new BusAnimationSequence().setPos(100, 0, 0) : new BusAnimationSequence().setPos(0, 0.5, 2.25).hold(350).addPos(0, 0.5, 1.25, 100, IType.SIN_FULL).hold(1125).addPos(0, 0.5, 2.25, 100, IType.SIN_UP));
            default -> null;
        };
    };
}
