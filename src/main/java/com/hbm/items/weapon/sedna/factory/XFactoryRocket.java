package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import com.hbm.lib.Library;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryRocket.
 *
 * Die Raketen und der Panzerschreck. Eine Rakete ist kein Geschoss: sie verlaesst das Rohr mit
 * Geschwindigkeit null und schiebt sich erst im Flug auf Fahrt -- vierzig Tausendstel je Tick,
 * bis das Siebenfache erreicht ist. Deshalb faellt sie auch nicht (setGrav(0)) und zerplatzt
 * nicht an Wesen (setOnEntityHit(null)), sondern nur beim Aufschlag.
 *
 * FUENF GEFECHTSKOEPFE, dieselben fuenf wie beim 40-mm-Kaliber, nur groesser: Sprengkopf,
 * Hohlladung mit dem Dreifachen auf das getroffene Wesen, Abbruchkopf als einziger mit
 * Blockschaden, Brand- und Phosphorkopf mit stehendem Feuer.
 *
 * ABWEICHUNG, der Selbstschutz: das Original setzt setSelfDamageDelay(10) -- die Rakete geht
 * zehn Ticks lang durch den Schuetzen hindurch, ohne ihn zu beruehren. Der Port hat diese
 * Vorlaufzeit im Geschoss nicht; sie steht deshalb hier als Wachposten vor jedem Aufschlag,
 * zusammen mit der Regel des Originals, dass in den ersten drei Ticks ueberhaupt kein Wesen
 * zaehlt.
 *
 * ABWEICHUNG, der Rueckstoss: das Original haengt ein leeres LAMBDA_RECOIL_ROCKET an, damit der
 * Standardrueckstoss ausfaellt. Der Port laesst den Haken schlicht leer -- das Feld ist
 * standardmaessig leer, und die Auswertung fragt vorher nach.
 *
 * NICHT UEBERNOMMEN: Stinger, Quadro und Raketenwerfer. Der Stinger braucht ItemGunStinger samt
 * Zielerfassung, Quadro und Raketenwerfer die lenkbaren Raketen (rocket_qd, rocket_ml) -- und
 * lenkbar heisst: die Rakete fragt jeden Tick, wohin ihr Schuetze gerade zielt. Beides fehlt im
 * Port, und eine lenkbare Rakete ohne Lenkung waere eine gewoehnliche.
 */
public class XFactoryRocket {

    public static BulletConfig[] rocket_template;
    public static BulletConfig[] rocket_rpzb;
    /** Die beiden Saetze der NCR-Ruestung: ungelenkt auf der linken, gelenkt auf der rechten Taste. */
    public static BulletConfig[] rocket_ncrpa;
    public static BulletConfig[] rocket_ncrpa_steer;

    /**
     * Der Antrieb. Eine Rakete startet mit Geschwindigkeit null und schiebt sich selbst auf
     * Fahrt -- das ist der ganze Unterschied zu einer Granate.
     */
    public static Consumer<Entity> LAMBDA_STANDARD_ACCELERATE = (entity) -> {
        BulletBaseMK4 bullet = (BulletBaseMK4) entity;
        if(bullet.accel < 7) bullet.accel += 0.4D;
    };

    /**
     * DIE GELENKTE RAKETE DER NCR-RUESTUNG. Sie beschleunigt langsamer als die uebrigen (bis
     * 4 statt bis 7) und fragt dafuer jeden Zug, wohin ihr Schuetze gerade zielt -- und zieht
     * dorthin. Zwei Grenzen stehen im Original und sind uebernommen:
     *
     *   * Ueber hundert Bloecke Abstand zum Schuetzen hoert die Lenkung auf. Die Rakete
     *     fliegt dann geradeaus weiter.
     *   * Naeher als drei Bloecke am Zielpunkt wird nicht mehr korrigiert, sonst taenzelt sie
     *     um den Punkt herum, statt ihn zu treffen.
     *
     * Die Geschwindigkeit bleibt dabei gleich; gedreht wird nur die Richtung.
     */
    public static Consumer<Entity> LAMBDA_NCR_ACCELERATE = (entity) -> lenkendBeschleunigen(entity, false);

    public static void lenkendBeschleunigen(Entity entity, boolean ohneLenkung) {

        if(!(entity instanceof BulletBaseMK4 geschoss)) return;
        if(geschoss.accel < 4) geschoss.accel += 0.4D;

        if(!(geschoss.getOwner() instanceof Player spieler)) return;
        if(geschoss.position().subtract(spieler.position()).length() > 100D) return;
        if(ohneLenkung) return;

        BlockHitResult ziel = Library.rayTrace(spieler, 200D, 1F);
        if(ziel == null) return;

        Vec3 nach = ziel.getLocation().subtract(geschoss.position());
        if(nach.length() < 3D) return;
        nach = nach.normalize();

        double tempo = geschoss.getDeltaMovement().length();
        geschoss.setDeltaMovement(nach.scale(tempo));
    }

    /**
     * Der Wachposten vor jedem Aufschlag. Im Original stecken hier zwei getrennte Regeln: die
     * ersten drei Ticks zaehlt kein Wesen (die Abfrage in jedem Aufschlaglambda), und die ersten
     * zehn Ticks zaehlt der Schuetze selbst nicht (setSelfDamageDelay(10)). Beide zusammen
     * ergeben das Verhalten, das der Port hier nachbildet.
     */
    private static boolean zuFrueh(BulletBaseMK4 bullet, HitResult hr) {
        if(!(hr instanceof EntityHitResult ehr)) return false;
        if(bullet.tickCount < 3) return true;
        return bullet.tickCount < 10 && ehr.getEntity() == bullet.getOwner();
    }

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE = (bullet, hr) -> {
        if(zuFrueh(bullet, hr)) return;
        Lego.standardExplode(bullet, hr, 5F);
        bullet.discard();
    };

    /**
     * Die Hohlladung. Sie reisst weniger weit als der Sprengkopf, setzt dem getroffenen Wesen
     * aber das Dreifache des Geschosschadens obendrauf -- der Strahl wirkt nur dort, wo er
     * auftrifft.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_HEAT = (bullet, hr) -> {

        if(zuFrueh(bullet, hr)) return;

        Lego.standardExplode(bullet, hr, 3.5F);
        bullet.discard();

        if(!(hr instanceof EntityHitResult ehr)) return;

        Entity hit = ehr.getEntity();
        DamageSource source = BulletConfig.getDamage(bullet.level, hit, bullet.getOwner(), DamageClass.EXPLOSION);

        if(hit instanceof LivingEntity living) {
            EntityDamageUtil.hurtNT(living, source, bullet.damage * 3F, true, true, 0.5D, 5F, 0.2F);
        } else {
            EntityDamageUtil.hurtIgnoreIFrame(hit, source, bullet.damage * 3F);
        }
    };

    /**
     * Der Abbruchkopf. Er ist der einzige der fuenf, der wirklich Bloecke herausreisst.
     *
     * ABWEICHUNG: das Original setzt hier einen PlayerProcessorStandard, der dem Spieler eigene
     * Regeln fuer Schaden und Rueckstoss gibt. Der fehlt im Port; ohne ihn behandelt die
     * Explosion den Spieler wie jedes andere Wesen.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_DEMO = (bullet, hr) -> {

        if(zuFrueh(bullet, hr)) return;

        Vec3 pos = hr.getLocation();
        new ExplosionVNT(bullet.level, pos.x, pos.y, pos.z, 5F, bullet.getOwner())
                .setBlockAllocator(new BlockAllocatorStandard())
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                .explode();

        bullet.discard();
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_INC = (bullet, hr) -> {
        spawnFire(bullet, hr, false, 300);
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_PHOSPHORUS = (bullet, hr) -> {
        spawnFire(bullet, hr, true, 600);
    };

    /**
     * Brand- und Phosphorkopf. Sie reissen drei Bloecke weit, stellen ein stehendes Feuer hin und
     * zuenden ausserdem alles an, was in den umliegenden hundertfuenfundzwanzig Bloecken brennbar
     * ist -- ein Luftblock bekommt Feuer, sobald einer seiner sechs Nachbarn brennen kann.
     *
     * Das ist nicht dasselbe wie XFactory40mm.spawnFire: die Rakete stellt ein groesseres Feuer
     * hin (sechs statt fuenf Bloecke breit) und zuendet fuenf statt drei Bloecke weit an.
     */
    public static void spawnFire(BulletBaseMK4 bullet, HitResult hr, boolean phosphorus, int duration) {

        if(zuFrueh(bullet, hr)) return;

        Level level = bullet.level;
        Vec3 pos = hr.getLocation();

        Lego.standardExplode(bullet, hr, 3F);

        FireLingering fire = new FireLingering(level)
                .setArea(6, 2).setDuration(duration)
                .setFireType(phosphorus ? FireLingering.TYPE_PHOSPHORUS : FireLingering.TYPE_DIESEL);
        fire.at(pos);
        level.addFreshEntity(fire);

        bullet.discard();

        BlockPos center = BlockPos.containing(pos);

        for(int dx = -2; dx <= 2; dx++) for(int dy = -2; dy <= 2; dy++) for(int dz = -2; dz <= 2; dz++) {

            BlockPos here = center.offset(dx, dy, dz);
            if(!level.getBlockState(here).isAir()) continue;

            for(Direction dir : Direction.values()) {
                BlockPos neighbour = here.relative(dir);
                if(level.getBlockState(neighbour).isFlammable(level, neighbour, dir.getOpposite())) {
                    level.setBlockAndUpdate(here, Blocks.FIRE.defaultBlockState());
                    break;
                }
            }
        }
    }

    public static void init(DeferredRegister.Items registry) {

        initAmmo();

        /*
         * Der Panzerschreck. Ein Rohr, eine Rakete, fuenfzig Ticks zum Nachladen -- und das
         * Schutzschild vor dem Gesicht, das sich im Original per Aufsatz abnehmen laesst.
         */
        NtmItems.GUN_PANZERSCHRECK = registry.register("gun_panzerschreck", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(300).draw(7).inspect(40).crosshair(Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(25F).delay(5).reload(50).jam(40).sound(NtmSoundEvents.GUN_ROCKET_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(rocket_rpzb))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_PANZERSCHRECK_ANIMS).orchestra(Orchestras.ORCHESTRA_PANZERSCHRECK)
        ).setDefaultAmmo(Ammo.ROCKET_HE, 3));
    }

    /**
     * Die Raketen. Das Original legt sie als Vorlage an und zieht daraus je einen Satz fuer
     * Panzerschreck, Quadro, Raketenwerfer und die beiden NCRPA-Werfer; nur der erste Satz ist
     * portiert, und er ist eine unveraenderte Kopie der Vorlage (makeRPZB = clone). Die Kopie
     * bleibt trotzdem stehen: eine BulletConfig traegt eine eigene Kennung, und zwei Werfer
     * duerfen sich keine teilen.
     */
    public static void initAmmo() {

        rocket_template = new BulletConfig[5];

        BulletConfig baseRocket = new BulletConfig().setLife(300).setVel(0F).setGrav(0D)
                .setOnEntityHit(null).setOnRicochet(null).setOnUpdate(LAMBDA_STANDARD_ACCELERATE);

        rocket_template[0] = baseRocket.clone().setItem(Ammo.ROCKET_HE).setOnImpact(LAMBDA_STANDARD_EXPLODE);
        rocket_template[1] = baseRocket.clone().setItem(Ammo.ROCKET_HEAT).setDamage(0.5F).setOnImpact(LAMBDA_STANDARD_EXPLODE_HEAT);
        rocket_template[2] = baseRocket.clone().setItem(Ammo.ROCKET_DEMO).setDamage(0.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE_DEMO);
        rocket_template[3] = baseRocket.clone().setItem(Ammo.ROCKET_INC).setDamage(0.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE_INC);
        rocket_template[4] = baseRocket.clone().setItem(Ammo.ROCKET_PHOSPHORUS).setDamage(0.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE_PHOSPHORUS);

        rocket_rpzb = new BulletConfig[rocket_template.length];
        rocket_ncrpa = new BulletConfig[rocket_template.length];
        rocket_ncrpa_steer = new BulletConfig[rocket_template.length];

        for(int i = 0; i < rocket_template.length; i++) {
            rocket_rpzb[i] = rocket_template[i].clone();
            /* makeRPZB im Original: eine unveraenderte Kopie. */
            rocket_ncrpa[i] = rocket_template[i].clone();
            /* makeNCR im Original: laengere Lebensdauer und die Lenkung. */
            rocket_ncrpa_steer[i] = rocket_template[i].clone().setLife(400).setOnUpdate(LAMBDA_NCR_ACCELERATE);
        }
    }

    /**
     * Der Panzerschreck. RELOAD kippt das Rohr um neunzig Grad zur Seite, damit die Rakete von
     * hinten hineingeht; ROCKET fuehrt sie dabei ins Rohr. Beim Nachsehen wird nicht geladen --
     * dort sagt ROCKET nur, ob eine Rakete drinsteckt: sie steht entweder an ihrem Platz oder
     * drei Einheiten tiefer, also gar nicht da.
     *
     * Bei JAMMED zeigt sie immer, obwohl das Rohr in dem Moment leer ist -- das Original setzt
     * dafuer eigens empty zurueck, bevor es in den Zweig von INSPECT faellt. Sinn: eine
     * verklemmte Waffe hat eine Rakete drin, die nicht herauswill.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_PANZERSCHRECK_ANIMS = (stack, type) -> switch(type) {
        case EQUIP -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        case RELOAD -> new BusAnimation()
                .addBus("RELOAD", new BusAnimationSequence().addPos(90, 0, 0, 750, IType.SIN_FULL).addPos(90, 0, 0, 1000).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("ROCKET", new BusAnimationSequence().addPos(0, -3, -6, 0).addPos(0, -3, -6, 750).addPos(0, 0, -6.5, 500, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_UP));
        case JAMMED -> panzerschreckSchau(false);
        case INSPECT -> panzerschreckSchau(((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0]
                .getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory) <= 0);
        default -> null;
    };

    private static BusAnimation panzerschreckSchau(boolean empty) {
        return new BusAnimation()
                .addBus("RELOAD", new BusAnimationSequence().addPos(90, 0, 0, 750, IType.SIN_FULL).addPos(90, 0, 0, 500).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("ROCKET", new BusAnimationSequence().addPos(0, empty ? -3 : 0, 0, 0));
    }
}
