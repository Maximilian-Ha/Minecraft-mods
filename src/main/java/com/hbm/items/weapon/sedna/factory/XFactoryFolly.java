package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.logic.NukeExplosionMK5;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.entity.projectile.BulletBeamBase;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.AmmoSecret;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.particle.helper.NukeTorexCreator;
import com.hbm.particle.helper.PlasmaBlastCreator;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.EntityDamageUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryFolly.
 *
 * Die Folly. Eine einzige Waffe, zwei Geschosse, und beide sind masslos.
 *
 * DER SM-STRAHL IST KEIN STRAHL IM UEBLICHEN SINN. Er tastet nicht ab, er faehrt: ein
 * spektrales Geschoss, das mit Schwerkraft fliegt und dabei in jedem Zug eine drei mal drei
 * Bloecke dicke Roehre aus der Welt loescht und alles darin erschlaegt. Deshalb steht die
 * ganze Wirkung in setOnUpdate und nicht in einem Einschlaghaken -- sie passiert einmal, im
 * zweiten Zug, ueber die volle Laenge.
 *
 * DER ATOMKOPF ist dagegen schlicht: er fliegt, und wo er auftrifft, steht eine
 * Kernexplosion der Staerke 100 samt Pilz.
 *
 * ZIELEN IST EIN EIGENER ZUSTAND. Die Folly schiesst nicht aus der Hueffe: LAMBDA_CAN_FIRE
 * verlangt, dass gezielt wird, dass die letzte Bewegung SPINUP war und dass sie mindestens
 * hundert Zuege laeuft. Vorher passiert gar nichts.
 *
 * RUNDE 190: bis hierher hiess es, diese Fabrik sei blockiert, weil EntityNukeExplosionMK5
 * und EntityNukeTorex im Port fehlten. NACHGEMESSEN: beide sind da -- NukeExplosionMK5 mit
 * statFac und NukeTorexCreator mit statFacStandard. Es fehlte nichts ausser der Fabrik selbst.
 */
public class XFactoryFolly {

    public static BulletConfig folly_sm;
    public static BulletConfig folly_nuke;

    /**
     * Der Weg des SM-Strahls. Zwei Dinge geschehen hier, und sie sind streng getrennt:
     *
     * Bis zum fuenfzigsten Zug wandert ein Schockfaecher die Strecke entlang und wird dabei
     * immer groesser -- das ist reiner Schauwert und laeuft auf jedem Rechner.
     *
     * Im ZWEITEN Zug, und nur dort, wird abgerechnet: der Schuetze bekommt eine
     * Strahlendosis, und ueber die volle Strahllaenge wird in Zweierschritten eine Roehre
     * ausgeloescht.
     */
    public static Consumer<Entity> LAMBDA_SM_UPDATE = (entity) -> {

        if(!(entity instanceof BulletBeamBase strahl)) return;
        Level level = strahl.level;

        Vec3 richtung = new Vec3(strahl.headingX, strahl.headingY, strahl.headingZ).normalize();

        if(strahl.tickCount < 50) {
            double abstand = 10D;
            double weg = strahl.tickCount * abstand;
            PlasmaBlastCreator.composeEffect(level,
                    strahl.getX() + richtung.x * weg, strahl.getY() + richtung.y * weg, strahl.getZ() + richtung.z * weg,
                    0.75F, 0.75F, 0.75F,
                    strahl.xRot + 90F, -strahl.yRot,
                    2F + strahl.tickCount / (float) (strahl.beamLength / abstand) * 3F);
        }

        if(level.isClientSide) return;
        if(strahl.tickCount != 2) return;

        if(strahl.thrower != null) ContaminationUtil.contaminate(strahl.thrower, HazardType.RADIATION, ContaminationType.CREATIVE, 150F);

        List<Entity> getroffene = level.getEntities(strahl,
                strahl.getBoundingBox().expandTowards(strahl.headingX, strahl.headingY, strahl.headingZ).inflate(1.0D));

        DamageSource quelle = BulletConfig.getDamage(level, strahl, strahl.thrower, strahl.config.dmgClass);

        for(int i = 1; i < strahl.beamLength; i += 2) {

            int x = (int) Math.floor(strahl.getX() + richtung.x * i);
            int y = (int) Math.floor(strahl.getY() + richtung.y * i);
            int z = (int) Math.floor(strahl.getZ() + richtung.z * i);

            for(int ix = x - 1; ix <= x + 1; ix++) for(int iy = y - 1; iy <= y + 1; iy++) for(int iz = z - 1; iz <= z + 1; iz++) {

                if(iy > level.getMinBuildHeight() && iy < level.getMaxBuildHeight()) {
                    level.removeBlock(new BlockPos(ix, iy, iz), false);
                }

                AABB kasten = new AABB(ix - 1, iy - 1, iz - 1, ix + 2, iy + 2, iz + 2);

                for(Entity e : getroffene) {
                    if(e == strahl.thrower) continue;
                    if(!e.getBoundingBox().intersects(kasten)) continue;

                    if(e instanceof LivingEntity lebendig) {
                        EntityDamageUtil.hurtNT(lebendig, quelle, strahl.damage, true, false, 0D, 100F, 0.99F);
                    } else {
                        EntityDamageUtil.hurtIgnoreIFrame(e, quelle, strahl.damage);
                    }
                }
            }
        }
    };

    /** Der Atomkopf: eine Kernexplosion der Staerke 100 mit Pilz, und das Geschoss ist weg. */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_IMPACT = (geschoss, treffer) -> {

        if(treffer instanceof EntityHitResult && geschoss.tickCount < 2) return;
        if(!geschoss.isAlive()) return;
        geschoss.discard();

        Vec3 ort = treffer.getLocation();
        Level level = geschoss.level;

        level.addFreshEntity(NukeExplosionMK5.statFac(level, 100, ort.x, ort.y, ort.z));
        NukeTorexCreator.statFacStandard(level, ort.x, ort.y, ort.z, 100F);
    };

    public static void init(DeferredRegister.Items registry) {

        /*
         * Der SM-Strahl ist spektral (er haelt an keinem Block an) und durchschlaegt; seine
         * Wirkung steht vollstaendig in LAMBDA_SM_UPDATE. Der Atomkopf laedt Chunks nach,
         * weil er zehn Minuten weit fliegen kann.
         */
        folly_sm = new BulletConfig().setItem(AmmoSecret.FOLLY_SM).setupDamageClass(DamageClass.SUBATOMIC)
                .setBeam().setLife(100).setVel(2F).setGrav(0.015D).setRenderRotations(false)
                .setSpectral(true).setDoesPenetrate(true)
                .setOnUpdate(LAMBDA_SM_UPDATE);
        folly_nuke = new BulletConfig().setItem(AmmoSecret.FOLLY_NUKE).setChunkloading()
                .setLife(600).setVel(4F).setGrav(0.015D)
                .setOnImpact(LAMBDA_NUKE_IMPACT);

        NtmItems.GUN_FOLLY = registry.register("gun_folly", () -> new GunBaseNTItem(WeaponQuality.SECRET, new GunConfig()
                .dura(0).draw(40).crosshair(Crosshair.NONE)
                .rec(new Receiver(0)
                        .dmg(1_000F).delay(26).dryfire(false).reload(160).jam(0)
                        .sound(NtmSoundEvents.GUN_FOLLY_FIRE, 100.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(folly_sm, folly_nuke))
                        .offset(0.75, -0.0625, -0.1875D).offsetScoped(0.75, -0.0625, -0.125D)
                        .canFire(LAMBDA_CAN_FIRE).fire(LAMBDA_FIRE).recoil(LAMBDA_RECOIL_FOLLY))
                .setupStandardConfiguration().pt(LAMBDA_TOGGLE_AIM)
                .anim(LAMBDA_FOLLY_ANIMS).orchestra(Orchestras.ORCHESTRA_FOLLY)
        ));
    }

    /**
     * Die mittlere Maustaste schaltet das Zielen um -- und nur beim Einschalten laeuft die
     * SPINUP-Bewegung an, die das Geraet hochfaehrt.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_TOGGLE_AIM = (stack, ctx) -> {
        if(GunBaseNTItem.getState(stack, ctx.configIndex) == GunState.IDLE) {
            boolean zielteVorher = GunBaseNTItem.getIsAiming(stack);
            GunBaseNTItem.setIsAiming(stack, !zielteVorher);
            if(!zielteVorher) GunBaseNTItem.playAnimation(ctx.getPlayer(), stack, GunAnimation.SPINUP, ctx.configIndex);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_FIRE = (stack, ctx) -> {
        Lego.doStandardFire(stack, ctx, GunAnimation.CYCLE, 0, false);
    };

    /**
     * Ein Spieler muss zielen, die Hochfahrbewegung muss die letzte gewesen sein, und sie muss
     * hundert Zuege laufen. Fuer alles andere (Geschuetze, Wesen) faellt die Pruefung weg --
     * daher die Abfrage auf Player.
     */
    public static BiFunction<ItemStack, LambdaContext, Boolean> LAMBDA_CAN_FIRE = (stack, ctx) -> {
        if(ctx.entity instanceof Player) {
            if(!GunBaseNTItem.getIsAiming(stack)) return false;
            if(GunBaseNTItem.getLastAnim(stack, ctx.configIndex) != GunAnimation.SPINUP) return false;
            if(GunBaseNTItem.getAnimTimer(stack, ctx.configIndex) < 100) return false;
        }
        return ctx.config.getReceivers(stack)[0].getMagazine(stack).getAmount(stack, ctx.container) > 0;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_FOLLY = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(25, (float) (ctx.getPlayer().getRandom().nextGaussian() * 1.5));
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_FOLLY_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-60, 0, 0, 0).addPos(5, 0, 0, 1500, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -4.5, 50).addPos(0, 0, -4.5, 500).addPos(0, 0, 0, 500, IType.SIN_UP))
                    .addBus("LOAD", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(-25, 0, 0, 250, IType.SIN_DOWN).addPos(0, 0, 0, 1000, IType.SIN_FULL));
            case RELOAD: return new BusAnimation()
                    .addBus("LOAD", new BusAnimationSequence().addPos(60, 0, 0, 1000, IType.SIN_FULL).addPos(60, 0, 0, 6000).addPos(0, 0, 0, 1000, IType.SIN_FULL))
                    .addBus("SCREW", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -135, 1000, IType.SIN_FULL).addPos(0, 0, -135, 4000).addPos(0, 0, 0, 1000, IType.SIN_FULL))
                    .addBus("BREECH", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -0.5, 1000, IType.SIN_FULL).addPos(0, -4, -0.5, 1000, IType.SIN_FULL).addPos(0, -4, -0.5, 2000).addPos(0, 0, -0.5, 1000, IType.SIN_FULL).addPos(0, 0, 0, 1000, IType.SIN_FULL))
                    .addBus("SHELL", new BusAnimationSequence().addPos(0, -4, -4.5, 0).addPos(0, -4, -4.5, 3000).addPos(0, 0, -4.5, 1000, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP));
            default: return null;
        }
    };
}
