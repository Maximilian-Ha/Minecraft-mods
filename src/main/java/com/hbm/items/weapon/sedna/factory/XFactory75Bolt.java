package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.extprop.HbmLivingAttachments;
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
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory75Bolt.
 *
 * Die 75-mm-Bolzen. Sie sind keine Patronen im ueblichen Sinn: jeder Bolzen ist eine kleine
 * Rakete mit eigenem Antrieb, und jeder geht beim Aufschlag hoch. Der gewoehnliche reisst zwei
 * Bloecke weit, der Sprengbolzen fuenf; der Brandbolzen setzt das Ziel in Phosphorbrand.
 *
 * DER BOLTER verschiesst sie im Dauerfeuer, dreissig Stueck am Stueck. Er ist die lauteste Waffe
 * ihrer Groesse -- sein Ruecklauf wirft den Lauf in jede Richtung, und die Zahl der
 * verbleibenden Bolzen steht in roten Ziffern auf dem Gehaeuse.
 */
public class XFactory75Bolt {

    public static BulletConfig b75;
    public static BulletConfig b75_inc;
    public static BulletConfig b75_exp;

    /** Der eigene Schuss zaehlt in den ersten drei Ticks nicht -- sonst spraengte man sich selbst. */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_TINY_EXPLODE = (bullet, hr) -> {
        if(hr instanceof EntityHitResult ehr && bullet.tickCount < 3 && ehr.getEntity() == bullet.getOwner()) return;
        Lego.tinyExplode(bullet, hr, 2F); bullet.discard();
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_INC = (bullet, hr) -> {
        if(hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
            HbmLivingAttachments data = HbmLivingAttachments.getData(living);
            if(data.phosphorus < 300) data.phosphorus = 300;
        }
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE = (bullet, hr) -> {
        Lego.standardExplode(bullet, hr, 5F); bullet.discard();
    };

    public static void init(DeferredRegister.Items registry) {

        initAmmo();

        NtmItems.GUN_BOLTER = registry.register("gun_bolter", () -> new GunBaseNTItem(WeaponQuality.SPECIAL, new GunConfig()
                .dura(3_000).draw(20).inspect(31).crosshair(Crosshair.L_CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(15F).delay(2).auto(true).spread(0.005F).reload(40).jam(55).sound(NtmSoundEvents.GUN_POWDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(b75, b75_inc, b75_exp))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_BOLT))
                .setupStandardConfiguration()
                .anim(LAMBDA_BOLTER_ANIMS).orchestra(Orchestras.ORCHESTRA_BOLTER)
        ).setDefaultAmmo(Ammo.B75, 15));
    }

    public static void initAmmo() {

        SpentCasing casing75 = new SpentCasing(SpentCasingType.STRAIGHT).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(2F, 2F, 1.5F);

        b75 = new BulletConfig().setItem(Ammo.B75)
                .setCasing(casing75.clone().register("b75")).setOnImpact(LAMBDA_TINY_EXPLODE);
        b75_inc = new BulletConfig().setItem(Ammo.B75_INC).setDamage(0.8F).setArmorPiercing(0.1F)
                .setCasing(casing75.clone().register("b75inc")).setOnImpact(LAMBDA_INC);
        b75_exp = new BulletConfig().setItem(Ammo.B75_EXP).setDamage(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing75.clone().register("b75exp")).setOnImpact(LAMBDA_STANDARD_EXPLODE);
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 2000, 0.05D, 1.1D, 0);
    };

    /** Der Bolter schlaegt in jede Richtung aus -- in beiden Achsen gleich stark und zufaellig. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_BOLT = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 1.5), (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    /**
     * Der Bolter. TILT kippt die ganze Waffe zum Nachladen nach hinten, MAG schwenkt das Magazin
     * heraus und wieder herein. Die dritte Achse von MAG dient als Schalter: steht sie auf 1,
     * ist das Magazin leer -- dann faellt die Patrone darin weg und die Klappe schwenkt weiter.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_BOLTER_ANIMS = (stack, type) -> switch(type) {
        case CYCLE -> new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence().addPos(1, 0, 0, 25).addPos(0, 0, 0, 75));
        case RELOAD -> new BusAnimation()
                .addBus("TILT", new BusAnimationSequence().addPos(1, 0, 0, 250).addPos(1, 0, 0, 1500).addPos(0, 0, 0, 250))
                .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 1, 500).addPos(1, 0, 1, 500).addPos(0, 0, 0, 500));
        case JAMMED -> new BusAnimation()
                .addBus("TILT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(1, 0, 0, 250).addPos(1, 0, 0, 700).addPos(0, 0, 0, 250))
                .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 750).addPos(0.6, 0, 0, 250).addPos(0, 0, 0, 250));
        default -> null;
    };
}
