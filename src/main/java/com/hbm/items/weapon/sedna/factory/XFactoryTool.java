package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorDebris;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.impl.GunChargeThrowerItem;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryTool.
 *
 * DIE HAELFTE DIESER FABRIK, die im Port moeglich ist: der Ladungswerfer. Der Feuerloescher
 * aus derselben Fabrik fehlt, und das ist gemessen -- er verschiesst Wasser, Schaum und Sand,
 * und dafuer braucht er ammo_fireext sowie die Loeschbloecke foam_layer, sand_boron_layer,
 * sand_mix und volcanic_lava_block. Von denen gibt es im Port nur block_foam. Eine Waffe, die
 * Schaum verschiesst, der nirgends liegenbleibt, waere keine Waffe.
 *
 * DER LADUNGSWERFER IST DREI WAFFEN IN EINER, je nachdem was geladen ist: ein Enterhaken zum
 * Fortbewegen, eine kleine Moerserladung und eine grosse. Die Munition wechselt man nur ueber
 * das Nachladen (reloadChangeType), nicht im Flug.
 *
 * Der Enterhaken bleibt in der Wand stecken -- dafuer hat BulletBaseMK4 seit dieser Runde
 * einen Steckzustand. Was daran haengt, steht in GunChargeThrowerItem.
 */
public class XFactoryTool {

    public static BulletConfig ct_hook;
    public static BulletConfig ct_mortar;
    public static BulletConfig ct_mortar_charge;

    /**
     * Merkt sich den Haken in der Waffe, die ihn abgeschossen hat. Ohne diese Zeile wuesste
     * der Ladungswerfer nicht, an welchem der Haken in der Welt der Schuetze haengt.
     *
     * Er laeuft nur in den ersten beiden Zuegen: danach steht die Nummer, und der Haken hat
     * nichts mehr zu melden.
     */
    public static Consumer<Entity> LAMBDA_SET_HOOK = (entity) -> {

        if(!(entity instanceof BulletBaseMK4 geschoss)) return;
        if(geschoss.level.isClientSide || geschoss.tickCount >= 2) return;
        if(!(geschoss.getOwner() instanceof Player spieler)) return;

        ItemStack inHand = spieler.getMainHandItem();
        if(inHand.getItem() instanceof GunChargeThrowerItem) {
            GunChargeThrowerItem.setLastHook(inHand, geschoss.getId());
        }
    };

    /**
     * Der Haken schlaegt ein und bleibt. Er rueckt dabei ein Zwanzigstel entgegen seiner
     * Flugrichtung zurueck, damit er nicht halb in der Wand verschwindet.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_HOOK = (geschoss, treffer) -> {

        if(!(treffer instanceof BlockHitResult bhr)) return;

        Vec3 zurueck = geschoss.getDeltaMovement().scale(-1D).normalize().scale(0.05D);
        geschoss.setPos(bhr.getLocation().add(zurueck));
        geschoss.getStuck(bhr.getBlockPos(), bhr.getDirection());
    };

    /** Die kleine Ladung: Radius 5, und sie raeumt grob auf (BlockAllocatorBulkie). */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_MORTAR = (geschoss, treffer) -> {

        if(treffer instanceof EntityHitResult ehr && geschoss.tickCount < 3 && ehr.getEntity() == geschoss.getOwner()) return;

        Vec3 ort = treffer.getLocation();
        new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 5F, geschoss.getOwner())
                .setBlockAllocator(new BlockAllocatorBulkie(60, 8))
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, geschoss.damage)
                        .setupPiercing(geschoss.config.armorThresholdNegation, geschoss.config.armorPiercingPercent))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                .explode();
        geschoss.discard();
    };

    /**
     * Die grosse: Radius 15, kein Bruchstueck bleibt liegen, dafuer Schlacke.
     *
     * ABWEICHUNG: das Original setzt die Schlacke mit Metadaten 1 -- das ist die gesprungene
     * Fassung derselben Textur. Der Port hat block_slag als einen Block ohne Zustaende; die
     * Textur block_slag_broken.png liegt zwar im Baum, aber kein Block zeigt sie. Bis es den
     * gesprungenen Block gibt, bleibt hier die glatte Schlacke stehen.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_MORTAR_CHARGE = (geschoss, treffer) -> {

        if(treffer instanceof EntityHitResult ehr && geschoss.tickCount < 3 && ehr.getEntity() == geschoss.getOwner()) return;

        Vec3 ort = treffer.getLocation();
        new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 15F, geschoss.getOwner())
                .setBlockAllocator(new BlockAllocatorStandard())
                .setBlockProcessor(new BlockProcessorStandard().setNoDrop()
                        .withBlockEffect(new BlockMutatorDebris(NtmBlocks.BLOCK_SLAG.get())))
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, geschoss.damage)
                        .setupPiercing(geschoss.config.armorThresholdNegation, geschoss.config.armorPiercingPercent))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .explode();
        ExplosionCreator.composeEffectSmall(geschoss.level, ort.x, ort.y + 0.5, ort.z);
        geschoss.discard();
    };

    public static void init(DeferredRegister.Items registry) {

        /*
         * Der Haken hat kein Ablaufdatum im ueblichen Sinn -- 6000 Zuege sind fuenf Minuten --
         * und er durchschlaegt, damit er nicht an einem Schwein haengenbleibt. Sein Schaden
         * faellt nicht mit der Strecke ab, weil er gar keinen macht.
         */
        ct_hook = new BulletConfig().setItem(Ammo.CT_HOOK).setRenderRotations(false)
                .setLife(6_000).setVel(3F).setGrav(0.035D).setDoesPenetrate(true).setDamageFalloffByPen(false)
                .setOnUpdate(LAMBDA_SET_HOOK).setOnImpact(LAMBDA_HOOK);
        ct_mortar = new BulletConfig().setItem(Ammo.CT_MORTAR).setDamage(2.5F).setLife(200).setVel(3F).setGrav(0.035D)
                .setOnImpact(LAMBDA_MORTAR);
        ct_mortar_charge = new BulletConfig().setItem(Ammo.CT_MORTAR_CHARGE).setDamage(5F).setLife(200).setVel(3F).setGrav(0.035D)
                .setOnImpact(LAMBDA_MORTAR_CHARGE);

        NtmItems.GUN_CHARGE_THROWER = registry.register("gun_charge_thrower", () -> new GunChargeThrowerItem(WeaponQuality.UTILITY, new GunConfig()
                .dura(3_000).draw(10).inspect(55).reloadChangeType(true).hideCrosshair(false).crosshair(Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(10F).delay(4).dry(10).auto(true).spread(0F).spreadHipfire(0F).reload(60).jam(0)
                        .sound(NtmSoundEvents.GUN_CHARGE_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 1).addConfigs(ct_hook, ct_mortar, ct_mortar_charge))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_CT))
                .setupStandardConfiguration()
                .anim(LAMBDA_CT_ANIMS).orchestra(Orchestras.ORCHESTRA_CHARGE_THROWER)
        ).setDefaultAmmo(Ammo.CT_MORTAR, 3));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_CT = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().getRandom().nextGaussian() * 1.5));
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_CT_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case RELOAD: return new BusAnimation()
                    .addBus("RAISE", new BusAnimationSequence().addPos(-45, 0, 0, 500, IType.SIN_FULL).hold(2000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("AMMO", new BusAnimationSequence().setPos(0, -10, -5).hold(500).addPos(0, 0, 5, 750, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP).hold(4000))
                    .addBus("TWIST", new BusAnimationSequence().setPos(0, 0, 25).hold(2000).addPos(0, 0, 0, 150));
            case INSPECT: return new BusAnimation()
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 60, 0, 500, IType.SIN_FULL).hold(1750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("ROLL", new BusAnimationSequence().hold(750).addPos(0, 0, -90, 500, IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_FULL));
            default: return null;
        }
    };
}
