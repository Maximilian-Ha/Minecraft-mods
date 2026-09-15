package com.hbm.items.weapon.sedna.factory;

import com.hbm.extprop.HbmLivingAttachments;
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
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory556mm.
 *
 * Die 5,56-mm-Patronen. Jede gibt es zweimal: gewoehnlich und als Brandsatz. Die Brandsaetze sind
 * keine eigenen Gegenstaende -- sie entstehen aus denselben Patronen, wenn die Waffe einen
 * Brandaufsatz traegt, und setzen das Ziel in Phosphorbrand.
 *
 * DIE G3 gibt es zweimal. Die gewoehnliche ist das Arbeitspferd des Kalibers: dreissig Schuss,
 * Dauerfeuer, und sie nimmt mehr Aufsaetze als jede andere Waffe des Spiels -- Schalldaempfer,
 * Zielfernrohr, abgesaegter Schaft und zwei Kunststoffschaefte, deren Zusammenstellung ihr
 * eigene Namen gibt. Die Zebra ist ihre Sonderausfuehrung: doppelte Haltbarkeit, anderthalbfacher
 * Schaden, Schalldaempfer und Zielfernrohr fest verbaut, und sie verschiesst ausschliesslich
 * Brandmunition.
 *
 * DIE STG 77 ist die Waffe des grossen Berges: zehn Schaden je Schuss, kein Ladehemmer, und ein
 * eigener Entscheider, weil bei ihr das Zielen auf der rechten Maustaste sitzt und das Dauerfeuer
 * trotzdem weiterlaufen muss.
 */
public class XFactory556mm {

    public static BulletConfig r556_sp;
    public static BulletConfig r556_fmj;
    public static BulletConfig r556_jhp;
    public static BulletConfig r556_ap;

    public static BulletConfig r556_inc_sp;
    public static BulletConfig r556_inc_fmj;
    public static BulletConfig r556_inc_jhp;
    public static BulletConfig r556_inc_ap;

    /** Setzt das getroffene Wesen in Phosphorbrand -- denselben, den die Leuchtspurschrote legt. */
    public static BiConsumer<BulletBaseMK4, HitResult> INCENDIARY = (bullet, hr) -> {
        if(hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
            HbmLivingAttachments data = HbmLivingAttachments.getData(living);
            if(data.phosphorus < 300) data.phosphorus = 300;
        }
    };

    /** Das Bild der Zieloptik. Es gehoert hierher, weil der Zielfernrohraufsatz es allen leiht. */
    public static final ResourceLocation SCOPE = NuclearTechMod.withDefaultNamespace("textures/misc/scope_bolt.png");

    public static void init(DeferredRegister.Items registry) {

        initAmmo();

        /*
         * Die G3. Der einzige Empfaenger ist gewoehnlich -- was sie ausmacht, sind die Aufsaetze:
         * ohne Schaft zieht sie doppelt so schnell, mit Kunststoffschaft schlaegt sie halb so
         * stark zurueck, und wer Schalldaempfer, kurzen Schaft, schwarzen Kunststoff und Optik
         * zugleich verbaut, haelt am Ende eine Waffe mit eigenem Namen in der Hand.
         */
        NtmItems.GUN_G3 = registry.register("gun_g3", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(3_000).draw(10).inspect(33).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(5F).delay(2).auto(true).dry(15).reload(50).jam(47).sound(NtmSoundEvents.GUN_ASSAULT_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(r556_sp, r556_fmj, r556_jhp, r556_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_G3))
                .setupStandardConfiguration().ps(Lego.LAMBDA_STANDARD_CLICK_SECONDARY)
                .anim(LAMBDA_G3_ANIMS).orchestra(Orchestras.ORCHESTRA_G3)
        ).setDefaultAmmo(Ammo.R556_SP, 30).setNameMutator(LAMBDA_NAME_G3));

        /*
         * Die Zebra. Sie traegt Daempfer und Optik von Haus aus -- der Renderer prueft dafuer die
         * Waffe selbst, nicht die Aufsatzliste -- und laedt nur Brandmunition. Die geringere
         * Hueftstreuung und der schwaechere Ruecklauf sind ihr Ausgleich dafuer, dass sie nur
         * durch das Rohr wirklich trifft.
         */
        NtmItems.GUN_G3_ZEBRA = registry.register("gun_g3_zebra", () -> new GunBaseNTItem(WeaponQuality.B_SIDE, new GunConfig()
                .dura(6_000).draw(10).inspect(33).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE).scopeTexture(SCOPE)
                .rec(new Receiver(0)
                        .dmg(7.5F).delay(2).auto(true).dry(15).spreadHipfire(0.01F).reload(50).jam(47).sound(NtmSoundEvents.GUN_SILENCED_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(r556_inc_sp, r556_inc_fmj, r556_inc_jhp, r556_inc_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ZEBRA))
                .setupStandardConfiguration().ps(Lego.LAMBDA_STANDARD_CLICK_SECONDARY)
                .anim(LAMBDA_G3_ANIMS).orchestra(Orchestras.ORCHESTRA_G3)
        ).setDefaultAmmo(Ammo.R556_JHP, 30).setNameMutator(LAMBDA_NAME_G3));

        /*
         * Die StG 77. Sie hat keinen Ladehemmer (jam(0)) und keinen Ruecklauf -- die Waffe steht
         * beim Dauerfeuer still. Gezielt wird mit der rechten Maustaste, deshalb liegt der Schuss
         * auf BEIDEN Tasten und das Zielen auf der mittleren.
         */
        NtmItems.GUN_STG77 = registry.register("gun_stg77", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(3_000).draw(10).inspect(125).crosshair(Crosshair.CIRCLE).scopeTexture(SCOPE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(10F).delay(2).dry(15).auto(true).reload(46).jam(0).sound(NtmSoundEvents.GUN_ASSAULT_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(r556_sp, r556_fmj, r556_jhp, r556_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_STG))
                .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD).pt(Lego.LAMBDA_TOGGLE_AIM)
                .decider(LAMBDA_STG77_DECIDER)
                .anim(LAMBDA_STG77_ANIMS).orchestra(Orchestras.ORCHESTRA_STG77)
        ).setDefaultAmmo(Ammo.R556_FMJ, 30));
    }

    /** Die Patronen. Getrennt, weil der Begleitturm sie auch ohne Waffe verschiesst. */
    public static void initAmmo() {

        SpentCasing casing556 = new SpentCasing(SpentCasingType.BOTTLENECK).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(0.8F);

        r556_sp = new BulletConfig().setItem(Ammo.R556_SP).setCasing(CasingType.SMALL, 8)
                .setCasing(casing556.clone().register("r556"));
        r556_fmj = new BulletConfig().setItem(Ammo.R556_FMJ).setCasing(CasingType.SMALL, 8).setDamage(0.8F).setThresholdNegation(4F).setArmorPiercing(0.1F)
                .setCasing(casing556.clone().register("r556fmj"));
        r556_jhp = new BulletConfig().setItem(Ammo.R556_JHP).setCasing(CasingType.SMALL, 8).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing556.clone().register("r556jhp"));
        r556_ap = new BulletConfig().setItem(Ammo.R556_AP).setCasing(CasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.25F).setThresholdNegation(10F).setArmorPiercing(0.15F)
                .setCasing(casing556.clone().setColor(SpentCasing.COLOR_CASE_44).register("r556ap"));

        r556_inc_sp = r556_sp.clone().setOnImpact(INCENDIARY);
        r556_inc_fmj = r556_fmj.clone().setOnImpact(INCENDIARY);
        r556_inc_jhp = r556_jhp.clone().setOnImpact(INCENDIARY);
        r556_inc_ap = r556_ap.clone().setOnImpact(INCENDIARY);
    }

    /**
     * Die G3 bekommt mit den Aufsaetzen einen anderen Namen. Zwei Zusammenstellungen sind
     * benannt: Daempfer, kurzer Schaft, schwarzer Kunststoff und Optik zugleich ergeben den
     * INFILTRATOR, gruener Kunststoff mit erhaltenem Schaft die A3.
     */
    public static Function<ItemStack, Component> LAMBDA_NAME_G3 = (stack) -> {

        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER) &&
                XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NO_STOCK) &&
                XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_FURNITURE_BLACK) &&
                XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE)) {
            return Component.translatable(stack.getItem().getDescriptionId() + ".infiltrator");
        }

        if(!XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NO_STOCK) &&
                XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_FURNITURE_GREEN)) {
            return Component.translatable(stack.getItem().getDescriptionId() + ".a3");
        }

        return null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 1500, 0.075D, 1.1D, 0);
    };

    /**
     * Wie der Standardentscheider, nur dass das Dauerfeuer an der rechten Maustaste haengt --
     * bei der StG 77 liegt der Schuss auf beiden Tasten, das Zielen auf der mittleren.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_STG77_DECIDER = (stack, ctx) -> {
        int index = ctx.configIndex;
        GunState lastState = GunBaseNTItem.getState(stack, index);
        GunStateDecider.deciderStandardFinishDraw(stack, lastState, index);
        GunStateDecider.deciderStandardClearJam(stack, lastState, index);
        GunStateDecider.deciderStandardReload(stack, ctx, lastState, 0, index);
        GunStateDecider.deciderAutoRefire(stack, ctx, lastState, 0, index, () -> GunBaseNTItem.getSecondary(stack, index));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_G3 = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 0.25), (float) (ctx.getPlayer().random.nextGaussian() * 0.25));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_ZEBRA = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 0.125), (float) (ctx.getPlayer().random.nextGaussian() * 0.125));
    };

    /** Die StG 77 steht still: sie hat gar keinen Ruecklauf. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_STG = (stack, ctx) -> { };

    /**
     * Die G3. Der Verschluss (BOLT) laeuft im Rohr, der Stopfen (PLUG) folgt ihm versetzt, der
     * Spanngriff (HANDLE) dreht sich dabei aus seiner Rast. LIFT hebt die ganze Waffe an, wenn
     * der Schuetze zum Magazin greift, SPEEN dreht das Magazin beim Betrachten um sich selbst.
     *
     * Der Ruecklauf faellt geringer aus, wenn die Waffe angelegt ist ODER einen Schaft hat --
     * ohne Schaft aus der Hueften geschossen springt sie dreimal so weit zurueck.
     *
     * BULLET blendet die Patrone im Magazin aus, sobald das Magazin leer ist.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_G3_ANIMS = (stack, type) -> {

        boolean empty = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory) <= 0;

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
            case CYCLE -> new BusAnimation()
                    .addBus("BOLT", new BusAnimationSequence().addPos(0, 0, 0, 20).addPos(0, 0, -4.5, 40).addPos(0, 0, 0, 40))
                    .addBus("RECOIL", new BusAnimationSequence()
                            .addPos(0, 0, (GunBaseNTItem.getIsAiming(stack) || !XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NO_STOCK)) ? -0.25 : -0.75, 25, IType.SIN_DOWN)
                            .addPos(0, 0, 0, 75, IType.SIN_FULL));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("BOLT", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, -0.3125, 100).hold(25).addPos(0, 0, -2.75, 130).hold(50).addPos(0, 0, -2.4375, 50).addPos(0, 0, 0, 85))
                    .addBus("PLUG", new BusAnimationSequence().addPos(0, 0, 0, 250).hold(125).addPos(0, 0, -2.4375, 130).hold(100).addPos(0, 0, 0, 85))
                    .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 90, 0, 100).hold(25).hold(180).addPos(0, 0, 0, 50))
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 400).addPos(-1, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case RELOAD -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence()
                            .addPos(0, -8, 0, 250, IType.SIN_UP)    //250
                            .addPos(0, -8, 0, 1050)                 //1300
                            .addPos(0, 0, 0, 250))                  //1550
                    .addBus("BOLT", new BusAnimationSequence()
                            .addPos(0, 0, 0, 200)                   //200
                            .addPos(0, 0, -0.3125, 100)             //300
                            .hold(10)                               //310
                            .addPos(0, 0, -3.25, 200)               //510
                            .holdUntil(1875)                        //1875
                            .addPos(0, 0, -2.9375, 50)              //1925
                            .addPos(0, 0, 0, 100))                  //2025
                    .addBus("PLUG", new BusAnimationSequence()
                            .addPos(0, 0, 0, 310)                   //310
                            .addPos(0, 0, -2.9375, 200)             //510
                            .holdUntil(1925)                        //1925
                            .addPos(0, 0, 0, 100))                  //2025
                    .addBus("HANDLE", new BusAnimationSequence()
                            .addPos(0, 0, 0, 200)                   //200
                            .addPos(0, 90, 0, 100)                  //300
                            .hold(210)                              //510
                            .addPos(0, 90, 45, 75)                  //685
                            .holdUntil(1775)                        //1775
                            .addPos(0, 90, 0, 100)                  //1875
                            .addPos(0, 0, 0, 50))                   //1925
                    .addBus("LIFT", new BusAnimationSequence()
                            .addPos(0, 0, 0, 750)                   //750
                            .addPos(-25, 0, 0, 500, IType.SIN_FULL) //1250
                            .holdUntil(1550)                        //1550
                            .addPos(-26, 0, 0, 100, IType.SIN_DOWN) //1650
                            .addPos(-25, 0, 0, 100, IType.SIN_FULL) //1750
                            .holdUntil(2000)                        //2000
                            .addPos(0, 0, 0, 500, IType.SIN_FULL))  //2500
                    .addBus("BULLET", new BusAnimationSequence().addPos(empty ? 1 : 0, 0, 0, 0).addPos(0, 0, 0, 1000));
            case INSPECT -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence()
                            .addPos(0, -1, 0, 150)                  //150
                            .addPos(2, -1, 0, 150)                  //300
                            .addPos(2, 8, 0, 350, IType.SIN_DOWN)   //650
                            .addPos(2, -2, 0, 350, IType.SIN_UP)    //1000
                            .addPos(2, -1, 0, 50)                   //1050
                            .addPos(2, -1, 0, 100)                  //1150
                            .addPos(0, -1, 0, 150, IType.SIN_FULL)  //1300
                            .addPos(0, 0, 0, 150, IType.SIN_UP))    //1450
                    .addBus("SPEEN", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(0, 360, 360, 700))
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 1450).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BULLET", new BusAnimationSequence().addPos(empty ? 1 : 0, 0, 0, 0));
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1250).addPos(0, 0, 0, 350, IType.SIN_FULL))
                    .addBus("BOLT", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100).addPos(0, 0, 0, 250).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100))
                    .addBus("PLUG", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100).addPos(0, 0, 0, 250).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100));
            default -> null;
        };
    };

    /**
     * Die StG 77. Nachladen, Durchladen und Betrachten kommen aus der Animationsdatei -- das
     * Betrachten zerlegt die Waffe halb, das laesst sich von Hand nicht mehr schreiben.
     *
     * ABWEICHUNG: das Original haelt daneben einen zweiten, von Hand geschriebenen Satz und
     * schaltet mit ClientConfig.GUN_ANIMS_LEGACY um. Den Schalter gibt es im Port nicht; hier
     * steht der neue Satz.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_STG77_ANIMS = (stack, type) -> switch(type) {
        case EQUIP -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        case CYCLE -> new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.125 : -0.375, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL))
                .addBus("SAFETY", new BusAnimationSequence().addPos(0.25, 0, 0, 0).addPos(0.25, 0, 0, 2000).addPos(0, 0, 0, 50));
        case CYCLE_DRY -> ResourceManager.stg77_anim.get("FireDry");
        case RELOAD -> ResourceManager.stg77_anim.get("Reload");
        case INSPECT -> ResourceManager.stg77_anim.get("Inspect");
        default -> null;
    };
}
