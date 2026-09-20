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
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.SoundUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory10ga.
 *
 * Die 10-Gauge-Schrote. Sie ist die schwerste Schrotmunition des Spiels: zehn Kugeln je Schuss
 * statt neun, und die Uranschrote durchschlaegt zehn Punkte Panzerung.
 *
 * Die Splitterschrote prallt bis zu fuenfzehnmal ab und das in fast jedem Winkel -- sie ist
 * dafuer gedacht, in einen Gang geschossen zu werden und dort eine Weile zu bleiben. Die
 * Sprengschrote traegt zehn kleine Ladungen und nutzt den Lauf dreifach ab.
 *
 * DIE DOPPELFLINTE hat zwei Laeufe auf zwei Maustasten: links feuert einen, rechts den anderen,
 * und wer beide zugleich drueckt, verschiesst beide Schuss auf einmal. Der HEILIGE DRACHE ist
 * ihre Sonderausfuehrung -- halb so viel Schaden mehr, sechsfache Haltbarkeit, dafuer ein
 * Drittel mehr Streuung.
 *
 * DIE KETZER-SELBSTLADEFLINTE borgt sich Bewegungen, Rueckstoss und Orchester von der
 * Schredder-Flinte der 12 Gauge -- sie ist dieselbe Waffe in diesem Kaliber, mit
 * zweihundertfuenfzig Schuss im Magazin und ohne Haltbarkeit.
 *
 * BERICHTIGUNG: hier stand bis Runde 200, sie sei nicht uebernommen, weil die Schredder-Flinte
 * der 12 Gauge im Port fehle. Nachgemessen falsch -- sie steht in XFactory12ga, samt
 * LAMBDA_SEXY_ANIMS, LAMBDA_RECOIL_SEXY und ORCHESTRA_SHREDDER_SEXY.
 */
public class XFactory10ga {

    public static BulletConfig g10;
    public static BulletConfig g10_shrapnel;
    public static BulletConfig g10_du;
    public static BulletConfig g10_slug;
    public static BulletConfig g10_explosive;

    /** Wie bei der 7,62: der eigene Schuss zaehlt in den ersten drei Ticks nicht. */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_TINY_EXPLODE = (bullet, hr) -> {
        if(hr instanceof EntityHitResult ehr && bullet.tickCount < 3 && ehr.getEntity() == bullet.getOwner()) return;
        Lego.tinyExplode(bullet, hr, 1.5F); bullet.discard();
    };

    public static void init(DeferredRegister.Items registry) {

        initAmmo();

        /* Die Doppelflinte. Zwei Laeufe, zwei Tasten, dreissig Schaden je Lauf. */
        NtmItems.GUN_DOUBLE_BARREL = registry.register("gun_double_barrel", () -> new GunBaseNTItem(WeaponQuality.SPECIAL, new GunConfig()
                .dura(1_000).draw(10).inspect(39).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(30F).rounds(2).delay(10).reload(41).reloadOnEmpty(true).sound(NtmSoundEvents.GUN_SHOTGUN_FIRE, 1.0F, 0.9F)
                        .mag(new MagazineFullReload(0, 2).addConfigs(g10, g10_shrapnel, g10_du, g10_slug, g10_explosive))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_DOUBLE_BARREL))
                .setupStandardConfiguration().ps(LAMBDA_DOUBLE_SECONDARY)
                .anim(LAMBDA_DOUBLE_BARREL_ANIMS).orchestra(Orchestras.ORCHESTRA_DOUBLE_BARREL)
        ).setDefaultAmmo(Ammo.G10, 6));

        /* Der Heilige Drache. Mehr Schaden, mehr Haltbarkeit, dafuer mehr Streuung. */
        NtmItems.GUN_DOUBLE_BARREL_SACRED_DRAGON = registry.register("gun_double_barrel_sacred_dragon", () -> new GunBaseNTItem(WeaponQuality.B_SIDE, new GunConfig()
                .dura(6_000).draw(10).inspect(39).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(45F).spreadAmmo(1.35F).rounds(2).delay(10).reload(41).reloadOnEmpty(true).sound(NtmSoundEvents.GUN_SHOTGUN_FIRE, 1.0F, 0.9F)
                        .mag(new MagazineFullReload(0, 2).addConfigs(g10, g10_shrapnel, g10_du, g10_slug, g10_explosive))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_DOUBLE_BARREL))
                .setupStandardConfiguration().ps(LAMBDA_DOUBLE_SECONDARY)
                .anim(LAMBDA_DOUBLE_BARREL_ANIMS).orchestra(Orchestras.ORCHESTRA_DOUBLE_BARREL)
        ).setDefaultAmmo(Ammo.G10_DU, 6));

        /*
         * DIE KETZER-SELBSTLADEFLINTE. Sie ist eine Schredder-Flinte im Kaliber 10 Gauge und
         * borgt sich alles von ihr: Bewegungen, Rueckstoss und Orchester. Zweihundertfuenfzig
         * Schuss im Magazin, hundert Schaden je Schuss, und sie nutzt sich nicht ab
         * (LAMBDA_NOWEAR_FIRE) -- sie hat im Original gar keine Haltbarkeit.
         */
        NtmItems.GUN_AUTOSHOTGUN_HERETIC = registry.register("gun_autoshotgun_heretic", () -> new GunBaseNTItem(WeaponQuality.DEBUG, new GunConfig()
                .draw(20).inspect(65).reloadSequential(true).inspectCancel(false).crosshair(Crosshair.L_CIRCLE).hideCrosshair(false).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(100F).delay(3).auto(true).dryfireAfterAuto(true).reload(110).jam(19).sound(NtmSoundEvents.GUN_SHREDDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 250).addConfigs(g10, g10_shrapnel, g10_du, g10_slug, g10_explosive))
                        .offset(0.75, -0.125, -0.25)
                        .canFire(Lego.LAMBDA_STANDARD_CAN_FIRE).fire(Lego.LAMBDA_NOWEAR_FIRE).recoil(XFactory12ga.LAMBDA_RECOIL_SEXY))
                .setupStandardConfiguration()
                .anim(XFactory12ga.LAMBDA_SEXY_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER_SEXY)
        ).setDefaultAmmo(Ammo.G10, 50));
    }

    public static void initAmmo() {

        float buckshotSpread = 0.035F;

        g10 = new BulletConfig().setItem(Ammo.G10).setCasing(CasingType.BUCKSHOT_ADVANCED, 4).setProjectiles(10).setDamage(1F / 10F).setSpread(buckshotSpread).setRicochetAngle(15).setThresholdNegation(5F)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xB52B2B, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GA"));

        /* Die Splitterschrote prallt in fast jedem Winkel ab, und das bis zu fuenfzehnmal. */
        g10_shrapnel = new BulletConfig().setItem(Ammo.G10_SHRAPNEL).setCasing(CasingType.BUCKSHOT_ADVANCED, 4).setProjectiles(10).setDamage(1F / 10F).setSpread(buckshotSpread).setRicochetAngle(90).setRicochetCount(15).setThresholdNegation(5F)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xE5DD00, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GAShrapnel"));

        g10_du = new BulletConfig().setItem(Ammo.G10_DU).setCasing(CasingType.BUCKSHOT_ADVANCED, 4).setProjectiles(10).setDamage(1F / 4F).setSpread(buckshotSpread).setRicochetAngle(15).setThresholdNegation(10F).setArmorPiercing(0.2F).setDoesPenetrate(true).setDamageFalloffByPen(false)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0x538D53, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GADU"));

        /* Die Flintenlaufgeschoss-Patrone: eine einzige Kugel statt zehn. */
        g10_slug = new BulletConfig().setItem(Ammo.G10_SLUG).setCasing(CasingType.BUCKSHOT_ADVANCED, 4).setRicochetAngle(15).setThresholdNegation(10F).setArmorPiercing(0.1F).setDoesPenetrate(true)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0x808080, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GASlug"));

        g10_explosive = new BulletConfig().setItem(Ammo.G10_EXPLOSIVE).setCasing(CasingType.BUCKSHOT_ADVANCED, 4).setWear(3F).setProjectiles(10).setDamage(1F / 4F).setSpread(buckshotSpread).setOnImpact(LAMBDA_TINY_EXPLODE)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xFAC943, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GAEXP"));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_DOUBLE_BARREL = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    /**
     * Der zweite Lauf. Das ist derselbe Ablauf wie beim ersten, nur ohne Mehrfachschuss: der
     * Standardklick feuert getRoundsPerCycle Schuss auf einmal, dieser hier immer genau einen.
     * So laesst sich mit zwei Tasten einzeln oder mit beiden zugleich doppelt schiessen.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_DOUBLE_SECONDARY = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Player player = ctx.getPlayer();
        Receiver rec = ctx.config.getReceivers(stack)[0];
        int index = ctx.configIndex;
        GunState state = GunBaseNTItem.getState(stack, index);

        if(state == GunState.IDLE) {

            if(rec.getCanFire(stack).apply(stack, ctx)) {
                rec.getOnFire(stack).accept(stack, ctx);

                if(rec.getFireSound(stack) != null) {
                    SoundUtils.playAtVec3(entity.level, entity.position(), rec.getFireSound(stack).value(), SoundSource.BLOCKS, rec.getFireVolume(stack), rec.getFirePitch(stack));
                }

                GunBaseNTItem.setState(stack, index, GunState.COOLDOWN);
                GunBaseNTItem.setTimer(stack, index, rec.getDelayAfterFire(stack));

            } else {

                if(rec.getDoesDryFire(stack)) {
                    GunBaseNTItem.playAnimation(player, stack, GunAnimation.CYCLE_DRY, index);
                    GunBaseNTItem.setState(stack, index, rec.getRefireAfterDry(stack) ? GunState.COOLDOWN : GunState.DRAWING);
                    GunBaseNTItem.setTimer(stack, index, rec.getDelayAfterDryFire(stack));
                }
            }
        }

        if(state == GunState.RELOADING) {
            GunBaseNTItem.setReloadCancel(stack, true);
        }
    };

    /**
     * Die Doppelflinte. Zum Nachladen wird der Hebel (LEVER) umgelegt, die Laeufe klappen nach
     * oben (BARREL), die leeren Huelsen fliegen heraus (SHELLS, SHELL_FLIP dreht sie dabei einmal
     * um sich selbst), und die Waffe wird zweimal angehoben (LIFT): einmal beim Oeffnen, einmal
     * beim Zuwerfen am Ende.
     *
     * BUCKLE ist der Ruck, den der Kolben beim Schuss bekommt.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_DOUBLE_BARREL_ANIMS = (stack, type) -> switch(type) {
        case EQUIP -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
        case CYCLE -> new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                .addBus("BUCKLE", new BusAnimationSequence().addPos(0, -60, 0, 50).addPos(0, 0, 0, 250));
        case RELOAD -> new BusAnimation()
                .addBus("TURN", new BusAnimationSequence()
                        .addPos(0, 30, 0, 350, IType.SIN_FULL)
                        .addPos(0, 30, 0, 1150)
                        .addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("LEVER", new BusAnimationSequence()
                        .addPos(0, 0, 0, 250)
                        .addPos(0, 0, -90, 100, IType.SIN_FULL)
                        .addPos(0, 0, -90, 1300)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BARREL", new BusAnimationSequence()
                        .addPos(0, 0, 0, 300)
                        .addPos(60, 0, 0, 150, IType.SIN_UP)
                        .addPos(60, 0, 0, 1150)
                        .addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("LIFT", new BusAnimationSequence()
                        .addPos(0, 0, 0, 350)
                        .addPos(-5, 0, 0, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 700)
                        .addPos(-5, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_UP)      //1500
                        .addPos(45, 0, 0, 150)
                        .addPos(45, 0, 0, 150)
                        .addPos(-5, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))   //2050
                .addBus("SHELLS", new BusAnimationSequence()
                        .addPos(0, 0, 0, 450)
                        .addPos(0, 0, -2.5, 100)
                        .addPos(0, -5, -5, 350, IType.SIN_DOWN)
                        .addPos(0, -3, -2, 0)
                        .addPos(0, 0, -2, 250)
                        .addPos(0, 0, 0, 150, IType.SIN_UP))     //1300
                .addBus("SHELL_FLIP", new BusAnimationSequence().addPos(0, 0, 0, 450).addPos(-360, 0, 0, 450).addPos(0, 0, 0, 0));
        case INSPECT -> new BusAnimation()
                .addBus("LEVER", new BusAnimationSequence()
                        .addPos(0, 0, 0, 250)
                        .addPos(0, 0, -90, 100, IType.SIN_FULL)
                        .addPos(0, 0, -90, 800)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BARREL", new BusAnimationSequence()
                        .addPos(0, 0, 0, 300)
                        .addPos(60, 0, 0, 150, IType.SIN_UP)
                        .addPos(60, 0, 0, 650)
                        .addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("LIFT", new BusAnimationSequence()
                        .addPos(0, 0, 0, 350)
                        .addPos(-5, 0, 0, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 200)
                        .addPos(-5, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_UP)      //1500
                        .addPos(45, 0, 0, 150)
                        .addPos(45, 0, 0, 150)
                        .addPos(-5, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL));
        default -> null;
    };
}
