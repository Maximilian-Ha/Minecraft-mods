package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.BiFunction;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.factory.GunFactory.AmmoSecret;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import net.minecraft.world.phys.HitResult;

import java.util.function.BiConsumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory50.
 *
 * Die .50-BMG-Patronen. Sie sind die schwerste Gewehrmunition des Spiels: schon die gewoehnliche
 * durchschlaegt siebeneinhalb Punkte Panzerung, die Wolframkerne fast das Dreifache.
 *
 * Die Sprengpatrone traegt eine kleine Ladung und nutzt den Lauf dreifach ab; die
 * Schrabidiumpatrone zehnfach -- sie ist als Notloesung gedacht, nicht als Dauermunition.
 *
 * NICHT UEBERNOMMEN: die Equestrian-Patrone, die beim Aufschlag ein ganzes Gebaeude vom Himmel
 * fallen laesst. Die zugehoerige Entitaet fehlt im Port.
 *
 * DAS ANTIMATERIEGEWEHR gibt es dreimal. Die gewoehnliche Ausfuehrung, die SUBTLETY mit zwei
 * Dritteln mehr Schaden und dreifacher Haltbarkeit, und die PENANCE: sie hat von Haus aus einen
 * Schalldaempfer, keine Hueftstreuung, eine Waermebildoptik -- und als einzige Waffe im Spiel
 * frisst sie die schwarze Patrone.
 *
 * Die M2 des Kalibers kommt mit dem Gurtmagazin, das der Port noch nicht hat.
 */
public class XFactory50 {

    public static BulletConfig bmg50_sp;
    public static BulletConfig bmg50_fmj;
    public static BulletConfig bmg50_jhp;
    public static BulletConfig bmg50_ap;
    public static BulletConfig bmg50_du;
    public static BulletConfig bmg50_he;
    public static BulletConfig bmg50_sm;
    public static BulletConfig bmg50_black;

    /** Die Sprengpatrone reisst ein kleines Loch -- zwei Bloecke, nicht mehr. */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE = (bullet, hr) -> {
        Lego.standardExplode(bullet, hr, 2F); bullet.discard();
    };

    public static final ResourceLocation SCOPE = NuclearTechMod.withDefaultNamespace("textures/misc/scope_amat.png");
    public static final ResourceLocation SCOPE_THERMAL = NuclearTechMod.withDefaultNamespace("textures/misc/scope_penance.png");

    public static void init(DeferredRegister.Items registry) {

        initAmmo();

        NtmItems.GUN_AMAT = registry.register("gun_amat", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, amat(30F, 350F)
                .scopeTexture(SCOPE)
                .rec(new Receiver(0)
                        .dmg(30F).delay(25).dry(25).spreadHipfire(0.05F).reload(51).jam(43).sound(NtmSoundEvents.GUN_AMAT_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 7).addConfigs(bmg50_sp, bmg50_fmj, bmg50_jhp, bmg50_ap, bmg50_du, bmg50_sm, bmg50_he))
                        .offset(1, -0.0625 * 1.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AMAT))
        ).setDefaultAmmo(Ammo.BMG50_SP, 7));

        NtmItems.GUN_AMAT_SUBTLETY = registry.register("gun_amat_subtlety", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, amat(50F, 1_000F)
                .scopeTexture(SCOPE)
                .rec(new Receiver(0)
                        .dmg(50F).delay(25).dry(25).spreadHipfire(0.05F).reload(51).jam(43).sound(NtmSoundEvents.GUN_AMAT_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 7).addConfigs(bmg50_sp, bmg50_fmj, bmg50_jhp, bmg50_ap, bmg50_du, bmg50_sm, bmg50_he))
                        .offset(1, -0.0625 * 1.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AMAT))
        ).setDefaultAmmo(Ammo.BMG50_JHP, 7));

        NtmItems.GUN_AMAT_PENANCE = registry.register("gun_amat_penance", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, amat(45F, 5_000F)
                .scopeTexture(SCOPE_THERMAL).thermalSights(true)
                .rec(new Receiver(0)
                        .dmg(45F).delay(25).dry(25).spreadHipfire(0F).reload(51).jam(43).sound(NtmSoundEvents.GUN_SILENCER_SHOOT, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 7).addConfigs(bmg50_sp, bmg50_fmj, bmg50_jhp, bmg50_ap, bmg50_du, bmg50_sm, bmg50_he, bmg50_black))
                        .offset(1, -0.0625 * 1.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AMAT))
        ).setDefaultAmmo(Ammo.BMG50_JHP, 7));

        /*
         * Die M2 aus der Hand. Sie hat kein Magazin, sondern einen Gurt: sie frisst unmittelbar
         * aus dem Rucksack, und nachgeladen wird nie. Ihr Schussgeraeusch ist dasselbe wie das der
         * Gatling -- es ist dieselbe Patrone im selben Kaliber.
         */
        NtmItems.GUN_M2 = registry.register("gun_m2", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(3_000).draw(10).inspect(31).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(7.5F).delay(2).dry(10).auto(true).spread(0.005F).sound(NtmSoundEvents.TURRET_CHEKHOV_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineBelt().addConfigs(bmg50_sp, bmg50_fmj, bmg50_jhp, bmg50_ap, bmg50_du, bmg50_he))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_M2))
                .setupStandardConfiguration()
                .anim(LAMBDA_M2_ANIMS).orchestra(Orchestras.ORCHESTRA_M2)
        ).setDefaultAmmo(Ammo.BMG50_FMJ, 25));
    }

    /** Was alle drei Ausfuehrungen teilen. Empfaenger und Optik traegt jede selbst ein. */
    private static GunConfig amat(float damage, float durability) {
        return new GunConfig()
                .dura(durability).draw(20).inspect(50).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .setupStandardConfiguration()
                .anim(LAMBDA_AMAT_ANIMS).orchestra(Orchestras.ORCHESTRA_AMAT);
    }

    /** Die Patronen. Getrennt, weil die Tuerme sie brauchen, auch wenn keine Waffe sie fuehrt. */
    public static void initAmmo() {

        SpentCasing casing50 = new SpentCasing(SpentCasingType.BOTTLENECK).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(1.5F);

        bmg50_sp = new BulletConfig().setItem(Ammo.BMG50_SP).setCasing(CasingType.LARGE, 12)
                .setCasing(casing50.clone().register("bmg50"));
        bmg50_fmj = new BulletConfig().setItem(Ammo.BMG50_FMJ).setCasing(CasingType.LARGE, 12).setDamage(0.8F).setThresholdNegation(7F).setArmorPiercing(0.1F)
                .setCasing(casing50.clone().register("bmg50fmj"));
        bmg50_jhp = new BulletConfig().setItem(Ammo.BMG50_JHP).setCasing(CasingType.LARGE, 12).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing50.clone().register("bmg50jhp"));
        bmg50_ap = new BulletConfig().setItem(Ammo.BMG50_AP).setCasing(CasingType.LARGE_STEEL, 12).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(17.5F).setArmorPiercing(0.15F)
                .setCasing(casing50.clone().setColor(SpentCasing.COLOR_CASE_44).register("bmg50ap"));
        bmg50_du = new BulletConfig().setItem(Ammo.BMG50_DU).setCasing(CasingType.LARGE_STEEL, 12).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F).setThresholdNegation(21F).setArmorPiercing(0.25F)
                .setCasing(casing50.clone().setColor(SpentCasing.COLOR_CASE_44).register("bmg50du"));
        bmg50_he = new BulletConfig().setItem(Ammo.BMG50_HE).setCasing(CasingType.LARGE_STEEL, 12).setWear(3F).setDamage(1.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE)
                .setCasing(casing50.clone().setColor(SpentCasing.COLOR_CASE_44).register("bmg50he"));
        bmg50_sm = new BulletConfig().setItem(Ammo.BMG50_SM).setCasing(CasingType.LARGE_STEEL, 6).setWear(10F).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(2.5F).setThresholdNegation(30F).setArmorPiercing(0.35F)
                .setCasing(casing50.clone().setColor(SpentCasing.COLOR_CASE_44).register("bmg50sm"));

        /* Die schwarze Patrone geht durch Waende hindurch -- sie ist ein Fundstueck, kein Bauteil. */
        bmg50_black = new BulletConfig().setItem(AmmoSecret.BMG50_BLACK).setWear(5F).setDoesPenetrate(true).setDamageFalloffByPen(false).setSpectral(true).setDamage(1.5F).setHeadshot(3F).setThresholdNegation(30F).setArmorPiercing(0.35F)
                .setCasing(casing50.clone().setColor(SpentCasing.COLOR_CASE_EQUESTRIAN).register("bmg50black"));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 2000, 0.05D, 1.1D, 0);
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_M2 = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 0.5), (float) (ctx.getPlayer().random.nextGaussian() * 0.5));
    };

    /** Die M2 hat nur zwei Bewegungen: das Anheben beim Ziehen und den Ruecklauf beim Schuss. */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_M2_ANIMS = (stack, type) -> switch(type) {
        case EQUIP -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(80, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        case CYCLE -> new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -0.25, 25).addPos(0, 0, 0, 75));
        default -> null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_AMAT = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(12.5F, (float) ctx.getPlayer().random.nextGaussian());
    };

    /**
     * Das Antimateriegewehr laedt von Hand durch: der Verschluss dreht sich erst auf, wird dann
     * zurueckgezogen und wieder vorgeschoben. Die Waffe hebt sich dabei sichtbar (LIFT).
     *
     * Beim Betrachten wirft der Schuetze das Zielfernrohr in die Luft und faengt es wieder auf --
     * das ist der SCOPE_THROW; SCOPE_SPIN dreht es dabei einmal um sich selbst.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_AMAT_ANIMS = (stack, type) -> {

        double turn = -60;
        double pullAmount = -2.5;
        double side = 4;
        double down = -2;
        double detach = 0.5;
        double apex = 7;

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("BIPOD", new BusAnimationSequence().hold(500).addPos(80, 0, 0, 350).addPos(80, 25, 0, 150));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250).addPos(0, 0, 0, 200, IType.LINEAR))
                    .addBus("LIFT", new BusAnimationSequence().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250).addPos(0, 0, 0, 200, IType.LINEAR))
                    .addBus("LIFT", new BusAnimationSequence().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case RELOAD -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence().addPos(0, -10, 0, 350, IType.SIN_UP).addPos(0, 0, 0, 650, IType.SIN_UP))
                    .addBus("LIFT", new BusAnimationSequence().hold(1000).addPos(-2, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL).hold(450).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(1500).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(1600).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250).addPos(0, 0, 0, 200, IType.LINEAR));
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().hold(250).addPos(-15, 0, 0, 500, IType.SIN_FULL).holdUntil(1650).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(250).addPos(0, 0, turn, 150).holdUntil(1250).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).addPos(0, 0, 0, 200, IType.LINEAR).addPos(0, 0, pullAmount, 250, IType.SIN_UP).addPos(0, 0, 0, 200, IType.LINEAR));
            case INSPECT -> new BusAnimation()
                    .addBus("SCOPE_THROW", new BusAnimationSequence().addPos(0, detach, 0, 100, IType.SIN_FULL).addPos(side, down, 0, 500, IType.SIN_FULL).addPos(side, down - 0.5, 0, 100).addPos(side, apex, 0, 350, IType.SIN_FULL).addPos(side, down - 0.5, 0, 350, IType.SIN_DOWN).addPos(side, down, 0, 100).hold(250).addPos(0, detach, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("SCOPE_SPIN", new BusAnimationSequence().hold(700).addPos(-360, 0, 0, 700));
            default -> null;
        };
    };
}
