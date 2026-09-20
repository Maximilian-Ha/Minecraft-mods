package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.CoinEntity;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.NI4NIGunItem;
import com.hbm.items.weapon.sedna.mags.MagazineInfinite;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.SoundUtils;
import com.hbm.util.particle.ParticleUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryAccelerator.
 *
 * Im Original stehen hier drei Waffen: die Tau-Kanone, die Spulenkanone und die NI4NI.
 *
 * BERICHTIGUNG: hier stand bis Runde 202, die ersten beiden brauchten "Munition, die der Port
 * nicht hat". Nachgemessen falsch -- NtmItems.AMMO_STANDARD ist ein EnumMultiItem ueber
 * GunFactory.Ammo, und TAU_URANIUM, COIL_TUNGSTEN und COIL_FERROURANIUM stehen dort seit jeher,
 * samt Eintrag in ORDER und damit im Kreativreiter. Es fehlten nur die BulletConfigs, die sie
 * an ein Verhalten binden.
 *
 * DIE SPULENKANONE kam in Runde 202 dazu. OFFEN BLEIBT DIE TAU-KANONE: sie hat ein Aufladewerk
 * -- der Zweitdruck laedt auf, der Erstdruck feuert, und ein aufgeladener Schuss kommt aus
 * einem zweiten, eigenen Magazin. Das ist eine eigene Runde.
 *
 * DIE NI4NI IST EINE MUENZWAFFE. Sie hat unendlich Munition und keine Haltbarkeit; ihr Witz
 * liegt darin, dass der Zweitdruck eine Muenze in die Luft wirft und der Strahl an ihr
 * abknickt, um sich das naechste Ziel zu suchen. Ohne Muenze ist sie eine mittelmaessige
 * Pistole, mit Muenzen schiesst sie um die Ecke.
 */
public class XFactoryAccelerator {

    public static BulletConfig coil_tungsten;
    public static BulletConfig coil_ferrouranium;
    public static BulletConfig ni4ni_arc;

    public static void init(DeferredRegister.Items registry) {

        /* Der Lichtbogen. Er durchschlaegt zehn Punkte Panzerung, laesst ein Fuenftel des
         * Rests weg und bleibt am ersten Ziel stehen -- durchschlagen tut er NICHT, sonst
         * waere der Muenzknick sinnlos. */
        ni4ni_arc = new BulletConfig().setupDamageClass(DamageClass.PHYSICAL).setBeam().setLife(5)
                .setThresholdNegation(10F).setArmorPiercing(0.2F).setRenderRotations(false).setDoesPenetrate(false)
                .setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);

        /*
         * DIE BEIDEN SPULENGESCHOSSE. Sie fliegen sehr schnell (7,5 Bloecke je Tick),
         * durchschlagen ohne Schadensverlust und sind SPEKTRAL -- sie gehen durch Bloecke
         * hindurch, statt an ihnen zu zerplatzen. Was sie auf dem Weg durchschlagen,
         * zerbricht: Wolfram raeumt alles bis Haerte 1,25 weg, Ferrouran bis 2,5.
         */
        coil_tungsten = new BulletConfig().setItem(Ammo.COIL_TUNGSTEN).setVel(7.5F).setLife(50)
                .setDoesPenetrate(true).setDamageFalloffByPen(false).setSpectral(true)
                .setOnUpdate(LAMBDA_UPDATE_TUNGSTEN);
        coil_ferrouranium = new BulletConfig().setItem(Ammo.COIL_FERROURANIUM).setVel(7.5F).setLife(50)
                .setDoesPenetrate(true).setDamageFalloffByPen(false).setSpectral(true)
                .setOnUpdate(LAMBDA_UPDATE_FERRO);

        /*
         * DIE SPULENKANONE. Ein Schuss im Rohr, zwanzig Zuege Nachladen, und sie klemmt oft
         * (jeder dritte Nachladevorgang). Sie hat kein Muendungsfeuer -- sie schiesst mit
         * Magnetfeldern, da brennt nichts.
         */
        NtmItems.GUN_COILGUN = registry.register("gun_coilgun", () -> new GunBaseNTItem(WeaponQuality.SPECIAL, new GunConfig()
                .dura(400).draw(5).inspect(39).crosshair(Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(35F).delay(5).reload(20).jam(33).sound(NtmSoundEvents.GUN_COIL_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(coil_tungsten, coil_ferrouranium))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_COILGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_COILGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_COILGUN)
        ).setDefaultAmmo(Ammo.COIL_TUNGSTEN, 5));

        NtmItems.GUN_N_I_4_N_I = registry.register("gun_n_i_4_n_i", () -> new NI4NIGunItem(WeaponQuality.SPECIAL, new GunConfig()
                .dura(0).draw(5).inspect(39).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(35F).delay(10).sound(NtmSoundEvents.GUN_COIL_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineInfinite(ni4ni_arc))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().fire(Lego.LAMBDA_NOWEAR_FIRE))
                .setupStandardConfiguration()
                .ps(LAMBDA_NI4NI_SECONDARY_PRESS)
                .anim(LAMBDA_NI4NI_ANIMS).orchestra(Orchestras.ORCHESTRA_COILGUN)
        ));
    }

    /**
     * Der Zweitdruck wirft eine Muenze. Sie fliegt aus Augenhoehe mit vier Fuenfteln der
     * Blickrichtung los und bekommt einen halben Block Auftrieb -- sie steigt also erst, bevor
     * sie faellt, und steht dadurch einen Moment lang still genug, um sie zu treffen.
     *
     * OHNE MUENZE PASSIERT NICHTS. Der Zaehler laeuft in NI4NIGunItem weiter und fuellt ihn
     * nach.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_COILGUN = (stack, ctx) ->
            GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));

    /**
     * DAS GESCHOSS RAEUMT SICH DEN WEG FREI. Anders als jedes andere Geschoss des Ports
     * zerbricht es die Bloecke, durch die es fliegt -- alle halbe Blocklaenge wird geprueft,
     * und was weicher ist als der Grenzwert, faellt.
     *
     * NUR WAS LUFT IST, wird geprueft: der Port fragt blockstate.isAir() ab, und das Original
     * tut dasselbe (b.isAir(...) && hardness < threshold). Das sieht nach einem Widerspruch
     * aus -- Luft hat keine Haerte --, ist aber der Wortlaut des Originals, und ohne eine
     * Messung, die etwas anderes belegt, bleibt er stehen.
     */
    public static Consumer<Entity> LAMBDA_UPDATE_TUNGSTEN = (entity) -> breakInPath(entity, 1.25F);
    public static Consumer<Entity> LAMBDA_UPDATE_FERRO = (entity) -> breakInPath(entity, 2.5F);

    public static void breakInPath(Entity entity, float threshold) {

        Level level = entity.level;
        Vec3 vec = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        double motion = Math.max(vec.length(), 0.1D);
        vec = vec.normalize();

        for(double d = 0; d < motion; d += 0.5D) {

            double dX = entity.getX() - vec.x * d;
            double dY = entity.getY() - vec.y * d;
            double dZ = entity.getZ() - vec.z * d;

            if(level.isClientSide) {
                ParticleUtil.addParticle(level, ParticleTypes.FIREWORK, dX, dY, dZ, 0F, 0F, 0F);
                continue;
            }

            BlockPos pos = BlockPos.containing(dX, dY, dZ);
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            if(state.isAir() && hardness >= 0F && hardness < threshold) {
                level.destroyBlock(pos, false);
            }
        }
    }

    /**
     * Die Spulenkanone kippt beim Schuss nach hinten und beim Nachladen zur Seite -- beides
     * derselbe Bus RELOAD beziehungsweise RECOIL, den der Renderer als Drehung auswertet.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_COILGUN_ANIMS = (stack, type) -> {
        if(type == GunAnimation.EQUIP) return new BusAnimation()
                .addBus("RELOAD", new BusAnimationSequence().addPos(1, 0, 0, 0).addPos(0, 0, 0, 250));
        if(type == GunAnimation.CYCLE) return new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence().addPos(GunBaseNTItem.getIsAiming(stack) ? 0.5 : 1, 0, 0, 100).addPos(0, 0, 0, 200));
        if(type == GunAnimation.RELOAD) return new BusAnimation()
                .addBus("RELOAD", new BusAnimationSequence().addPos(1, 0, 0, 250).addPos(1, 0, 0, 500).addPos(0, 0, 0, 250));
        return null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_NI4NI_SECONDARY_PRESS = (stack, ctx) -> {

        Player spieler = ctx.getPlayer();
        if(spieler == null) return;
        if(spieler.level.isClientSide) return;
        if(NI4NIGunItem.getCoinCount(stack) <= 0) return;

        Vec3 blick = spieler.getLookAngle().scale(0.8D);

        CoinEntity muenze = new CoinEntity(spieler.level, spieler);
        muenze.setPos(spieler.getX(), spieler.getEyeY() - 0.125D, spieler.getZ());
        muenze.setDeltaMovement(blick.x, blick.y + 0.5D, blick.z);
        muenze.setYRot(spieler.getYRot());
        spieler.level.addFreshEntity(muenze);

        SoundUtils.playAtVec3(spieler.level, spieler.position(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                spieler.getSoundSource(), 1.0F, 1F + spieler.getRandom().nextFloat() * 0.25F);

        NI4NIGunItem.setCoinCount(stack, NI4NIGunItem.getCoinCount(stack) - 1);
    };

    /**
     * Drei Bewegungen. Beim Ziehen und beim Betrachten wirbelt die Waffe um die eigene Achse
     * -- zweimal herum beim Ziehen, dreimal beim Betrachten.
     *
     * DER RUECKSTOSS HAENGT AM ZIELEN: aus der Huefte kippt sie dreissig Grad hoch, ueber Kimme
     * und Korn nur fuenf. Die Trommel dreht sich dabei um hundertzwanzig Grad weiter, also um
     * ein Drittel.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_NI4NI_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-360 * 2, 0, 0, 500));
            case CYCLE -> {
                boolean zielt = GunBaseNTItem.getIsAiming(stack);
                yield new BusAnimation()
                        .addBus("RECOIL", new BusAnimationSequence().addPos(zielt ? -5 : -30, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                        .addBus("DRUM", new BusAnimationSequence().hold(50).addPos(0, 0, 120, 300, IType.SIN_FULL));
            }
            case INSPECT -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-360 * 3, 0, 0, 750).hold(100).addPos(0, 0, 0, 750));
            default -> null;
        };
    };
}
