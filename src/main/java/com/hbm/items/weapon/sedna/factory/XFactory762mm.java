package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.BulletBaseMK4;
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
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.main.NuclearTechMod;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory762mm.
 *
 * Die 7,62-mm-Patronen. Sie stehen zwischen der 5,56 und der .50: mehr Durchschlag als die eine,
 * handlicher als die andere. Die Sprengpatrone traegt eine kleine Ladung und nutzt den Lauf
 * dreifach ab.
 *
 * DER KARABINER ist die Jagdwaffe des Kalibers -- vierzehn Schuss im Roehrenmagazin, einzeln
 * nachgeladen, fuenfzehn Schaden je Treffer. Er nimmt Zielfernrohr und Bajonett.
 *
 * DIE MAS-36 ist ihr schweres Gegenstueck: sieben Schuss, dreissig Schaden, von Hand
 * durchgeladen. Sie ist eine Fundwaffe, kein Bauteil.
 *
 * DIE MINIGUN frisst wie die M2 unmittelbar aus dem Rucksack: sechs Schaden je Schuss, einer je
 * Tick, fuenfzigtausend Schuss Haltbarkeit. Die Doppel-Minigun ist zweimal dieselbe Waffe, jede
 * Haelfte auf ihrer Maustaste.
 *
 * NICHT UEBERNOMMEN: das Lacunae-Lasergatling. Es verschiesst Kondensatoren aus XFactoryEnergy,
 * die der Port noch nicht hat.
 */
public class XFactory762mm {

    public static BulletConfig r762_sp;
    public static BulletConfig r762_fmj;
    public static BulletConfig r762_jhp;
    public static BulletConfig r762_ap;
    public static BulletConfig r762_du;
    public static BulletConfig r762_he;

    /**
     * Die Sprengpatrone reisst anderthalb Bloecke weit. Trifft sie in den ersten drei Ticks den
     * Schuetzen selbst, geschieht nichts -- sonst spraengte sich, wer aus der Deckung heraus
     * schiesst, regelmaessig selbst in die Luft.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_TINY_EXPLODE = (bullet, hr) -> {
        if(hr instanceof EntityHitResult ehr && bullet.tickCount < 3 && ehr.getEntity() == bullet.getOwner()) return;
        Lego.tinyExplode(bullet, hr, 1.5F); bullet.discard();
    };

    public static void init(DeferredRegister.Items registry) {

        initAmmo();

        /*
         * Der Karabiner. Er laedt einzeln nach (reloadSequential): dreissig Ticks zum Oeffnen,
         * fuenfzehn je Patrone, und am Ende schiebt RELOAD_END den Verschluss wieder vor.
         */
        NtmItems.GUN_CARBINE = registry.register("gun_carbine", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(3_000).draw(10).inspect(31).reloadSequential(true).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(15F).delay(5).dry(15).spread(0.0F).reload(30, 0, 15, 0).jam(60).sound(NtmSoundEvents.GUN_POWDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 14).addConfigs(r762_sp, r762_fmj, r762_jhp, r762_ap, r762_du, r762_he))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_CARBINE))
                .setupStandardConfiguration()
                .anim(LAMBDA_CARBINE_ANIMS).orchestra(Orchestras.ORCHESTRA_CARBINE)
        ).setDefaultAmmo(Ammo.R762_SP, 14));

        /*
         * Die MAS-36. Der Verschluss dreht sich auf (BOLT_TURN), wird zurueckgezogen (BOLT_PULL)
         * und wieder vorgeschoben; nachgeladen wird mit einem Ladestreifen, den der Schuetze von
         * oben einschiebt (CLIP) und wieder herauszieht.
         */
        NtmItems.GUN_MAS36 = registry.register("gun_mas36", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(5_000).draw(20).inspect(31).reloadSequential(true).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(30F).delay(25).dry(25).spread(0.0F).reload(43).jam(43).sound(NtmSoundEvents.GUN_HEAVY_RIFLE_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 7).addConfigs(r762_sp, r762_fmj, r762_jhp, r762_ap, r762_du, r762_he))
                        .offset(1, -0.0625 * 1.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_CARBINE))
                .setupStandardConfiguration()
                .anim(LAMBDA_MAS36_ANIMS).orchestra(Orchestras.ORCHESTRA_MAS36)
        ).setDefaultAmmo(Ammo.R762_AP, 14));

        /*
         * Die Minigun. Sie hat kein Magazin, sondern einen Gurt, und braucht deshalb kein
         * Nachladen -- der Laufkranz dreht sich (ROTATE) und laeuft nach dem letzten Schuss aus.
         */
        NtmItems.GUN_MINIGUN = registry.register("gun_minigun", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(50_000).draw(20).inspect(20).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(6F).delay(1).auto(true).dry(15).spread(0.01F).sound(NtmSoundEvents.GUN_MINIGUN_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineBelt().addConfigs(r762_sp, r762_fmj, r762_jhp, r762_ap, r762_du, r762_he))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_MINIGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_MINIGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_MINIGUN)
        ).setDefaultAmmo(Ammo.R762_FMJ, 30));

        /*
         * Die Doppel-Minigun. Zwei vollstaendige Konfigurationen, die linke auf der linken
         * Maustaste, die rechte auf der rechten -- die zweite braucht deshalb einen eigenen
         * Entscheider, weil der Standardentscheider das Dauerfeuer an der linken Taste festmacht.
         *
         * Sie steht im Original auf WeaponQuality.DEBUG: sie ist Werkzeug des Autors, nicht zu
         * bauen. Das bleibt so.
         */
        NtmItems.GUN_MINIGUN_DUAL = registry.register("gun_minigun_dual", () -> new GunBaseNTItem(WeaponQuality.DEBUG,
                new GunConfig()
                        .dura(50_000).draw(20).inspect(20).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(6F).delay(1).auto(true).dry(15).spread(0.01F).sound(NtmSoundEvents.GUN_MINIGUN_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineBelt().addConfigs(r762_sp, r762_fmj, r762_jhp, r762_ap, r762_du, r762_he))
                                .offset(1, -0.0625 * 2.5, 0.25D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_MINIGUN))
                        .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_MINIGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_MINIGUN_DUAL),
                new GunConfig()
                        .dura(50_000).draw(20).inspect(20).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(6F).delay(1).auto(true).dry(15).spread(0.01F).sound(NtmSoundEvents.GUN_MINIGUN_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineBelt().addConfigs(r762_sp, r762_fmj, r762_jhp, r762_ap, r762_du, r762_he))
                                .offset(1, -0.0625 * 2.5, -0.25D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_MINIGUN))
                        .ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(LAMBDA_SECOND_MINIGUN)
                        .anim(LAMBDA_MINIGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_MINIGUN_DUAL)
        ).setDefaultAmmo(Ammo.R762_SP, 50));
    }

    /** Die Patronen. Getrennt, weil der Kaliberumbau der G3 sie braucht, auch ohne eigene Waffe. */
    public static void initAmmo() {

        SpentCasing casing762 = new SpentCasing(SpentCasingType.BOTTLENECK).setColor(SpentCasing.COLOR_CASE_BRASS);

        r762_sp = new BulletConfig().setItem(Ammo.R762_SP).setCasing(CasingType.SMALL, 6)
                .setCasing(casing762.clone().register("r762"));
        r762_fmj = new BulletConfig().setItem(Ammo.R762_FMJ).setCasing(CasingType.SMALL, 6).setDamage(0.8F).setThresholdNegation(5F).setArmorPiercing(0.1F)
                .setCasing(casing762.clone().register("r762fmj"));
        r762_jhp = new BulletConfig().setItem(Ammo.R762_JHP).setCasing(CasingType.SMALL, 6).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing762.clone().register("r762jhp"));
        r762_ap = new BulletConfig().setItem(Ammo.R762_AP).setCasing(CasingType.SMALL_STEEL, 6).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(12.5F).setArmorPiercing(0.15F)
                .setCasing(casing762.clone().setColor(SpentCasing.COLOR_CASE_44).register("r762ap"));
        r762_du = new BulletConfig().setItem(Ammo.R762_DU).setCasing(CasingType.SMALL_STEEL, 6).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F).setThresholdNegation(15F).setArmorPiercing(0.25F)
                .setCasing(casing762.clone().setColor(SpentCasing.COLOR_CASE_44).register("r762du"));
        r762_he = new BulletConfig().setItem(Ammo.R762_HE).setCasing(CasingType.SMALL_STEEL, 6).setWear(3F).setDamage(1.75F).setOnImpact(LAMBDA_TINY_EXPLODE)
                .setCasing(casing762.clone().setColor(SpentCasing.COLOR_CASE_44).register("r762he"));
    }

    /** Wie der Standardentscheider, nur dass das Dauerfeuer an der rechten Maustaste haengt. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SECOND_MINIGUN = (stack, ctx) -> {
        int index = ctx.configIndex;
        GunState lastState = GunBaseNTItem.getState(stack, index);
        GunStateDecider.deciderStandardFinishDraw(stack, lastState, index);
        GunStateDecider.deciderStandardClearJam(stack, lastState, index);
        GunStateDecider.deciderStandardReload(stack, ctx, lastState, 0, index);
        GunStateDecider.deciderAutoRefire(stack, ctx, lastState, 0, index,
                () -> GunBaseNTItem.getSecondary(stack, index) && GunBaseNTItem.getMode(stack, ctx.configIndex) == 0);
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 1500, 0.075D, 1.1D, 0);
    };

    /** Beide Gewehre steigen fest um fuenf Grad und streuen seitlich zufaellig. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_CARBINE = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(5, (float) (ctx.getPlayer().random.nextGaussian() * 0.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_MINIGUN = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 0.5), (float) (ctx.getPlayer().random.nextGaussian() * 0.5));
    };

    /**
     * Die Minigun. ROTATE dreht den Laufkranz: jeder Schuss gibt ihm sechzig Grad mit, danach
     * laeuft er ueber zwei volle Umdrehungen aus. Weil jeder neue Schuss die Bewegung neu
     * anstoesst, dreht sich der Kranz bei Dauerfeuer durchgehend.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MINIGUN_ANIMS = (stack, type) -> switch(type) {
        case EQUIP -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_FULL));
        case CYCLE -> new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence()
                        .addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.25 : -0.5, 0)
                        .addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.25 : -0.5, 100)
                        .addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 60, 50).addPos(0, 0, 720, 1000, IType.SIN_DOWN));
        case CYCLE_DRY -> new BusAnimation()
                .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 60, 50).addPos(0, 0, 720, 1000, IType.SIN_DOWN));
        case RELOAD -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(-15, 0, 0, 250, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 60, 50).addPos(0, 0, 720, 1000, IType.SIN_DOWN));
        case INSPECT -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(3, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, -720, 1000, IType.SIN_DOWN));
        default -> null;
    };

    /**
     * Der Karabiner. SLIDE ist der Verschluss, REL die ausgeworfene Huelse am Auswerfer, LIFT
     * hebt die Waffe beim Nachladen an.
     *
     * Ist nur noch eine Patrone im Rohr, laeuft die Huelsenbewegung auf einen Kanal namens NULL,
     * den kein Modellteil liest -- so faellt sie weg, ohne dass der Satz umgebaut werden muesste.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_CARBINE_ANIMS = (stack, type) -> {

        int ammo = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory);

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.25 : -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus(ammo <= 1 ? "NULL" : "REL", new BusAnimationSequence().addPos(0, 0, 0.25, 50).addPos(0, 0.125, 1.25, 100, IType.SIN_UP));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, -1, 50).addPos(0, 0, 0, 100, IType.SIN_UP));
            case RELOAD -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence().addPos(0, -4, 0, 250, IType.SIN_UP).addPos(0, -4, 0, 750).addPos(0, 0, 0, 500, IType.SIN_DOWN))
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1000))
                    .addBus("BULLET", new BusAnimationSequence().addPos(ammo == 0 ? 1 : 0, 0, 0, 0).addPos(0, 0, 0, 1000));
            case RELOAD_END -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(-25, 0, 0, 0).addPos(-25, 0, 0, 750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, -1, 50).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus("REL", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, 0.25, 150).addPos(0, 0.125, 1.25, 100, IType.SIN_UP));
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(-25, 0, 0, 0).addPos(-25, 0, 0, 750).addPos(0, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 250).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, -1, 50).addPos(0, 0, -0.25, 100, IType.SIN_UP).addPos(0, 0, -0.25, 1250).addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, -1, 50).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus("REL", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, 0.25, 150).addPos(0, 0.125, 1, 100, IType.SIN_UP).addPos(0, 0.125, 1, 1250).addPos(0, 0.125, 0.25, 100, IType.SIN_DOWN).addPos(0, 0.125, 1, 100, IType.SIN_UP));
            case INSPECT -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SLIDE", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, -0.75, 150, IType.SIN_DOWN).addPos(0, 0, -0.75, 1000).addPos(0, 0, 0, 100, IType.SIN_UP))
                    .addBus(ammo == 0 ? "NULL" : "REL", new BusAnimationSequence().addPos(0, 0.125, 1.25, 0).addPos(0, 0.125, 1.25, 500).addPos(0, 0.125, 0.5, 150, IType.SIN_DOWN).addPos(0, 0.125, 0.5, 1000).addPos(0, 0.125, 1.25, 100, IType.SIN_UP));
            default -> null;
        };
    };

    /**
     * Die MAS-36. Beim Ziehen klappt der Schaft aus (STOCK, von -158 Grad in die Waagerechte).
     * BULLET sitzt auf -100, solange keine Patrone im Rohr liegt -- das schiebt sie so weit aus
     * dem Bild, dass sie nicht mehr zu sehen ist.
     *
     * Der Ladestreifen wird ueber SHOW_CLIP eingeblendet und mit CLIP von oben eingeschoben;
     * BULLETS sind die Patronen darin, die beim Herunterdruecken im Gehaeuse verschwinden.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MAS36_ANIMS = (stack, type) -> {

        int mag = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory);
        double turn = -90;
        double pullAmount = GunBaseNTItem.getIsAiming(stack) ? -1D : -1.5D;

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("STOCK", new BusAnimationSequence().setPos(-158, 0, 0).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("EQUIP", new BusAnimationSequence().setPos(45, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL).hold(500).addPos(1, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250).addPos(0, 0, 0, 200, IType.LINEAR))
                    .addBus("LIFT", new BusAnimationSequence().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("BULLET", mag <= 1 ? new BusAnimationSequence().setPos(-100, 0, 0) : new BusAnimationSequence().hold(850).addPos(0, 0.1875, 1.5, 200, IType.LINEAR));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250).addPos(0, 0, 0, 200, IType.LINEAR))
                    .addBus("LIFT", new BusAnimationSequence().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("BULLET", new BusAnimationSequence().setPos(-100, 0, 0));
            case RELOAD -> new BusAnimation()
                    .addBus("BOLT_TURN", new BusAnimationSequence().addPos(0, 0, turn, 150).holdUntil(2000).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(100).addPos(0, 0, -1.5D, 250, IType.SIN_UP).holdUntil(1800).addPos(0, 0, 0, 200, IType.LINEAR))
                    .addBus("BULLET", new BusAnimationSequence().setPos(-100, 0, 0).holdUntil(1200).setPos(0, 0, 0).hold(600).addPos(0, 0.1875, 1.5, 200, IType.LINEAR))
                    .addBus("LIFT", new BusAnimationSequence().hold(200).addPos(30, 0, 0, 500, IType.SIN_FULL).holdUntil(1200).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SHOW_CLIP", new BusAnimationSequence().setPos(1, 1, 1))
                    .addBus("CLIP", new BusAnimationSequence().setPos(2, -3, 0).hold(250).addPos(0.5, 1, 0, 500, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL).hold(400).addPos(-0.5, 0.5, 0, 150).addPos(-3, -3, 0, 250, IType.SIN_UP))
                    .addBus("BULLETS", new BusAnimationSequence().setPos(2, -3, 0).hold(250).addPos(0.5, 1, 0, 500, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL).hold(150).addPos(0, -1.5, 0, 250, IType.SIN_DOWN));
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().hold(250).addPos(-15, 0, 0, 500, IType.SIN_FULL).holdUntil(1650).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("BOLT_TURN", new BusAnimationSequence().hold(250).addPos(0, 0, turn, 150).holdUntil(1250).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).addPos(0, 0, 0, 200, IType.LINEAR).addPos(0, 0, pullAmount, 250, IType.SIN_UP).addPos(0, 0, 0, 200, IType.LINEAR));
            case INSPECT -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().hold(350).addPos(-3, 0, 0, 150, IType.SIN_DOWN).holdUntil(1050).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("BOLT_TURN", new BusAnimationSequence().addPos(0, 0, turn, 150).holdUntil(1050).addPos(0, 0, 0, 150))
                    .addBus("BOLT_PULL", new BusAnimationSequence().hold(100).addPos(0, 0, -1D, 250, IType.SIN_UP).hold(500).addPos(0, 0, 0, 200, IType.LINEAR))
                    .addBus("BULLET", mag == 0 ? new BusAnimationSequence().setPos(-100, 0, 0) : new BusAnimationSequence().setPos(0, 0.1875, 1.5).hold(100).addPos(0, 0.125, 0.5, 250, IType.SIN_UP).hold(500).addPos(0, 0.1875, 1.5, 200, IType.LINEAR));
            default -> null;
        };
    };
}
