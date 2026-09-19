package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums.CasingType;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.projectile.Boxcar;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.main.ResourceManager;
import com.hbm.util.SoundUtils;
import net.minecraft.sounds.SoundSource;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.*;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.factory.GunFactory.AmmoSecret;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class XFactory44 {

    public static BulletConfig m44_bp;
    public static BulletConfig m44_sp;
    public static BulletConfig m44_fmj;
    public static BulletConfig m44_jhp;
    public static BulletConfig m44_ap;
    public static BulletConfig m44_express;
    /** Die Signaturpatrone des Lilmac. Sie macht keinen Schaden -- sie ruft einen Gueterwagen. */
    public static BulletConfig m44_equestrian_pip;

    /**
     * Was die Signaturpatrone des Lilmac anrichtet: fuenfzig Bloecke ueber dem Getroffenen
     * erscheint ein Gueterwagen, und ein Signalhorn kuendigt ihn an. Den Rest erledigt die
     * Schwerkraft.
     *
     * Der Klang heisst im Original GUN_GO_GO_GADGET_FUCK_EVERYTHING_IN_THIS_GENERAL_DIRECTION
     * und zeigt auf das Zughorn; im Port steht er unter TRAIN_HORN.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_BOXCAR = (geschoss, treffer) -> {

        Vec3 stelle = treffer.getLocation();
        Level level = geschoss.level;

        Boxcar wagen = new Boxcar(NtmEntityTypes.BOXCAR.get(), level);
        wagen.setPos(stelle.x, stelle.y + 50, stelle.z);
        wagen.setOwner(geschoss.getOwner());
        level.addFreshEntity(wagen);

        SoundUtils.playAtVec3(level, wagen.position(), NtmSoundEvents.TRAIN_HORN.get(), SoundSource.HOSTILE, 100F, 1F);
        geschoss.discard();
    };

    public static void init(DeferredRegister.Items registry) {

        SpentCasing casing44 = new SpentCasing(SpentCasingType.STRAIGHT).setColor(SpentCasing.COLOR_CASE_BRASS).setupSmoke(1F, 0.5D, 60, 20);
        m44_bp = new BulletConfig().setItem(Ammo.M44_BP).setCasing(CasingType.SMALL, 12).setDamage(0.75F).setBlackPowder(true)
                .setCasing(casing44.clone().register("m44bp"));
        m44_sp = new BulletConfig().setItem(Ammo.M44_SP).setCasing(CasingType.SMALL, 6)
                .setCasing(casing44.clone().register("m44"));
        m44_fmj = new BulletConfig().setItem(Ammo.M44_FMJ).setCasing(CasingType.SMALL, 6).setDamage(0.8F).setThresholdNegation(3F).setArmorPiercing(0.1F)
                .setCasing(casing44.clone().register("m44fmj"));
        m44_jhp = new BulletConfig().setItem(Ammo.M44_JHP).setCasing(CasingType.SMALL, 6).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing44.clone().register("m44jhp"));
        m44_ap = new BulletConfig().setItem(Ammo.M44_AP).setCasing(CasingType.SMALL_STEEL, 6).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(7.5F).setArmorPiercing(0.15F)
                .setCasing(casing44.clone().setColor(SpentCasing.COLOR_CASE_44).register("m44ap"));
        m44_express = new BulletConfig().setItem(Ammo.M44_EXPRESS).setCasing(CasingType.SMALL, 6).setDoesPenetrate(true).setDamage(1.5F).setThresholdNegation(3F).setArmorPiercing(0.1F).setWear(1.5F)
                .setCasing(casing44.clone().register("m44express"));
        m44_equestrian_pip = new BulletConfig().setItem(AmmoSecret.M44_EQUESTRIAN).setDamage(0F).setOnImpact(LAMBDA_BOXCAR)
                .setCasing(casing44.clone().setColor(SpentCasing.COLOR_CASE_EQUESTRIAN).register("m44equestrianPip"));

        /*
         * Der schwere Revolver, XFactory44 Z. 110 des Originals. Er teilt sich das Modell mit
         * dem Lilmac -- dasselbe lilmac.obj, nur eine andere Textur.
         *
         * ABWEICHUNG: der setNameMutator des Originals haengt "_scoped" an den Namen, sobald
         * ein Zielfernrohr aufgesetzt ist. Der Aufsatz selbst ist im Port noch nicht da; ein
         * Namensschalter fuer einen Aufsatz, den es nicht gibt, waere toter Code.
         */
        NtmItems.GUN_HEAVY_REVOLVER = registry.register("gun_heavy_revolver", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(600).draw(10).inspect(23).crosshair(Crosshair.L_CLASSIC).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(15F).delay(14).reload(46).jam(23).sound(NtmSoundEvents.GUN_HEAVY_REVOLVER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(m44_bp, m44_sp, m44_fmj, m44_jhp, m44_ap, m44_express))
                        .offset(0.75, -0.0625, -0.3125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_NOPIP))
                .setupStandardConfiguration()
                .anim(LAMBDA_NOPIP_ANIMS).orchestra(Orchestras.ORCHESTRA_NOPIP)
        ).setDefaultAmmo(Ammo.M44_SP, 12));

        /*
         * Der Lilmac, XFactory44 Z. 121 des Originals. Aeusserlich der schwere Revolver mit
         * anderer Textur, innerlich eine ganz andere Waffe: einunddreissigtausend Schuss
         * Haltbarkeit, doppelter Schaden -- und eine Patrone, die Gueterwagen vom Himmel holt.
         *
         * NICHT UEBERNOMMEN: der Protege, die Schwesterwaffe mit m44_equestrian_mn7. Die
         * schiesst Torpedos, und EntityTorpedo hat der Port nicht.
         */
        NtmItems.GUN_HEAVY_REVOLVER_LILMAC = registry.register("gun_heavy_revolver_lilmac", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(31_000).draw(10).inspect(23).crosshair(Crosshair.L_CLASSIC).scopeTexture(ResourceManager.LILMAC_SCOPE_TEX).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(30F).delay(14).reload(46).jam(23).sound(NtmSoundEvents.GUN_HEAVY_REVOLVER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(m44_equestrian_pip, m44_bp, m44_sp, m44_fmj, m44_jhp, m44_ap, m44_express))
                        .offset(0.75, -0.0625, -0.3125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_NOPIP))
                .setupStandardConfiguration()
                .anim(LAMBDA_LILMAC_ANIMS).orchestra(Orchestras.ORCHESTRA_NOPIP)
        ).setDefaultAmmo(Ammo.M44_JHP, 12));

        NtmItems.GUN_HANGMAN = registry.register("gun_hangman", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(600).draw(10).inspect(31).inspectCancel(false).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(25F).delay(10).reload(46).jam(23).sound(NtmSoundEvents.GUN_HEAVY_REVOLVER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 8).addConfigs(m44_bp, m44_sp, m44_fmj, m44_jhp, m44_ap, m44_express))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_HANGMAN))
                .setupStandardConfiguration().ps(SMACK_A_FUCKER)
                .anim(LAMBDA_HANGMAN_ANIMS).orchestra(Orchestras.ORCHESTRA_HANGMAN)
        ).setDefaultAmmo(Ammo.M44_FMJ, 16));
    }

    public static BiConsumer<ItemStack, LambdaContext> SMACK_A_FUCKER = (stack, ctx) -> {
        if(GunBaseNTItem.getState(stack, ctx.configIndex) == GunState.IDLE || GunBaseNTItem.getLastAnim(stack, ctx.configIndex) == GunAnimation.CYCLE) {
            GunBaseNTItem.setIsAiming(stack, false);
            GunBaseNTItem.setState(stack, ctx.configIndex, GunState.DRAWING);
            GunBaseNTItem.setTimer(stack, ctx.configIndex, ctx.config.getInspectDuration(stack));
            GunBaseNTItem.playAnimation(ctx.getPlayer(), stack, GunAnimation.INSPECT, ctx.configIndex);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_NOPIP = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_HANGMAN = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(5, (float) (ctx.getPlayer().random.nextGaussian() * 1));
    };

    /* MUSS UNTER LAMBDA_NOPIP_ANIMS STEHEN: das Feld greift beim Auswerten darauf zu, und ein
     * statisches Feld darf im selben Klassenkoerper nur auf bereits deklarierte verweisen --
     * sonst "illegal forward reference". */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_NOPIP_ANIMS = (stack, type) -> {
        return switch(type) {
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(0, 0, 1, 50).addPos(0, 0, 1, 400).addPos(0, 0, 0, 200))
                    .addBus("DRUM", new BusAnimationSequence().addPos(0, 0, 0, 450).addPos(0, 0, 1, 200));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("HAMMER", new BusAnimationSequence().addPos(0, 0, 1, 50).addPos(0, 0, 1, 400).addPos(0, 0, 0, 200))
                    .addBus("DRUM", new BusAnimationSequence().addPos(0, 0, 0, 450).addPos(0, 0, 1, 200));
            case EQUIP -> new BusAnimation()
                    .addBus("ROTATE", new BusAnimationSequence().addPos(90, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            /* Die Schreibweise "RELAOD_TILT" ist ein Tippfehler des Originals. Er steht auch im
             * Zeichner, also muessen beide Seiten ihn behalten -- richtiggestellt waere die
             * Bewegung stumm. */
            case RELOAD -> new BusAnimation()
                    .addBus("RELAOD_TILT", new BusAnimationSequence().addPos(-15, 0, 0, 100).addPos(65, 0, 0, 100).addPos(45, 0, 0, 50).addPos(0, 0, 0, 200).addPos(0, 0, 0, 1450).addPos(-80, 0, 0, 100).addPos(-80, 0, 0, 100).addPos(0, 0, 0, 200))
                    .addBus("RELOAD_CYLINDER", new BusAnimationSequence().addPos(0, 0, 0, 200).addPos(90, 0, 0, 100).addPos(90, 0, 0, 1700).addPos(0, 0, 0, 70))
                    .addBus("RELOAD_LIFT", new BusAnimationSequence().addPos(0, 0, 0, 350).addPos(-45, 0, 0, 250).addPos(-45, 0, 0, 350).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 1050).addPos(0, 0, 0, 100))
                    .addBus("RELOAD_JOLT", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(2, 0, 0, 50).addPos(0, 0, 0, 100))
                    .addBus("RELOAD_BULLETS", new BusAnimationSequence().addPos(0, 0, 0, 650).addPos(10, 0, 0, 300).addPos(10, 0, 0, 200).addPos(0, 0, 0, 700))
                    .addBus("RELOAD_BULLETS_CON", new BusAnimationSequence().addPos(1, 0, 0, 0).addPos(1, 0, 0, 950).addPos(0, 0, 0, 1));
            case INSPECT, JAMMED -> new BusAnimation()
                    .addBus("RELAOD_TILT", new BusAnimationSequence().addPos(-15, 0, 0, 100).addPos(65, 0, 0, 100).addPos(45, 0, 0, 50).addPos(0, 0, 0, 200).addPos(0, 0, 0, 200).addPos(-80, 0, 0, 100).addPos(-80, 0, 0, 100).addPos(0, 0, 0, 200))
                    .addBus("RELOAD_CYLINDER", new BusAnimationSequence().addPos(0, 0, 0, 200).addPos(90, 0, 0, 100).addPos(90, 0, 0, 450).addPos(0, 0, 0, 70));
            default -> null;
        };
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_HANGMAN_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP ->
                    new BusAnimation().addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250));
            case RELOAD -> new BusAnimation()
                    .addBus("LID", new BusAnimationSequence().addPos(0, 0, -90, 250).addPos(0, 0, -90, 1500).addPos(0, 0, 0, 250))
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, -10, 0, 250, IType.SIN_UP).addPos(0, -10, 0, 500).addPos(0, 0, 0, 350, IType.SIN_FULL))
                    .addBus("BULLETS", new BusAnimationSequence().addPos(1, 1, 1, 0).addPos(0, 0, 0, 500))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-15, 0, 0, 500, IType.SIN_FULL).addPos(-15, 0, 0, 850).addPos(-25, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL))
                    .addBus("ROLL", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 25, 250, IType.SIN_FULL).addPos(0, 0, 25, 1000).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case INSPECT -> new BusAnimation()
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 170, 0, 500, IType.SIN_UP).addPos(0, 170, 0, 550).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("ROLL", new BusAnimationSequence().addPos(0, 0, 110, 500, IType.SIN_FULL).addPos(0, 0, 110, 550).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SMACK", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 1, 150, IType.SIN_DOWN).addPos(0, 0, -3, 150, IType.SIN_UP).addPos(0, 0, 0, 350, IType.SIN_FULL));
            case JAMMED -> new BusAnimation()
                    .addBus("LID", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, -90, 250).addPos(0, 0, -90, 300).addPos(0, 0, 0, 250))
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 250).addPos(0, -3, 0, 150, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(-10, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL))
                    .addBus("ROLL", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 25, 250, IType.SIN_FULL).addPos(0, 0, 25, 300).addPos(0, 0, 0, 250, IType.SIN_FULL));
            default -> null;
        };

    };

    /**
     * Der Lilmac bewegt sich wie der schwere Revolver -- nur beim Ziehen wirbelt er einmal um
     * sich selbst. Alles andere reicht er an LAMBDA_NOPIP_ANIMS weiter.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LILMAC_ANIMS = (stack, type) -> {
        if(type == GunAnimation.EQUIP) return new BusAnimation().addBus("SPIN", new BusAnimationSequence().addPos(-360, 0, 0, 350));
        return LAMBDA_NOPIP_ANIMS.apply(stack, type);
    };
}
