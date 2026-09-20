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
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
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
import com.hbm.util.Vec3NT;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
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
 * DREI WEITERE WAFFEN schiessen diese Raketen, und alle drei lenken auf ihre eigene Weise. Der
 * Quadro verschiesst vier gelenkte Raketen am Stueck; der Raketenwerfer eine einzige, die gar
 * nicht selbst lenkt, sondern beim Abschuss ein Ziel zugewiesen bekommt. Die Schulterrakete der
 * NCR-Ruestung steht in ArmorNCRPARanged und lenkt ohne Bedingung.
 *
 * BERICHTIGUNG: hier stand bis Runde 197, Quadro und Raketenwerfer seien nicht uebernommen,
 * weil ihnen die lenkbaren Raketen fehlten und Lenkung im Port nicht vorhanden sei. Beides
 * nachgemessen und falsch. makeML ist im Original eine reine Kopie ohne jede Lenkung -- der
 * Raketenwerfer lenkt ueber die Zielerfassung an der Waffe, und die liest BulletBaseMK4 laengst
 * aus (lockonTarget). Und Library.rayTrace(Player, double, float), das die Lenkung des Quadro
 * braucht, steht im Port seit jeher.
 *
 * NICHT UEBERNOMMEN bleibt allein der Stinger: er braucht ItemGunStinger als eigene Klasse mit
 * einem zweiten Tastenpaar zum Aufschalten. Seine Zielsuche ist hier als sucheZiel uebernommen,
 * weil der Raketenwerfer sie ebenfalls braucht.
 */
public class XFactoryRocket {

    public static BulletConfig[] rocket_template;
    public static BulletConfig[] rocket_rpzb;
    /** Die Saetze des Quadro und des Raketenwerfers. */
    public static BulletConfig[] rocket_qd;
    public static BulletConfig[] rocket_ml;
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

    /**
     * DIE GELENKTE RAKETE DES QUADRO. Dieselbe Rechnung, aber mit EINER BEDINGUNG: sie folgt
     * dem Blick nur, solange der Schuetze noch zielt. Laesst er die Zieltaste los oder wechselt
     * er die Waffe, fliegt die Rakete geradeaus weiter.
     *
     * Das ist der ganze Unterschied zur NCR-Rakete, die ohne Bedingung lenkt -- die Ruestung
     * hat keine Zieltaste, die man loslassen koennte.
     */
    public static Consumer<Entity> LAMBDA_STEERING_ACCELERATE = (entity) -> {

        if(!(entity instanceof BulletBaseMK4 geschoss)) return;

        if(!(geschoss.getOwner() instanceof Player spieler)) {
            /* Ohne Schuetzen gibt es nichts zu lenken -- und dann gilt die schnellere
             * Beschleunigung der ungelenkten Rakete, wie im Original. */
            if(geschoss.accel < 7) geschoss.accel += 0.4D;
            return;
        }

        ItemStack hand = spieler.getMainHandItem();
        boolean zielt = !hand.isEmpty() && hand.getItem() instanceof GunBaseNTItem && GunBaseNTItem.getIsAiming(hand);
        lenkendBeschleunigen(entity, !zielt);
    };

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
     * DIE ZIELSUCHE. Sie steht im Original in ItemGunStinger; der Stinger selbst ist nicht
     * portiert, diese eine Methode aber braucht auch der Raketenwerfer -- und deshalb steht
     * sie hier, wo beide Waffen sie finden.
     *
     * WIE SIE ARBEITET: vom Auge des Schuetzen geht ein Strahl in Blickrichtung. Um ihn wird
     * ein Kasten gespannt, der den Suchkegel grob umschliesst (der Strahl selbst, zweimal um
     * den Grenzwinkel gedreht und je zehn Bloecke nach oben und unten versetzt). In diesem
     * Kasten wird jedes Wesen einzeln geprueft: der Winkel zwischen Blickrichtung und
     * Richtung zum Wesen muss unter dem Grenzwinkel liegen. Der kleinste Winkel gewinnt.
     *
     * Der Kasten ist nur die Vorauswahl -- die eigentliche Entscheidung faellt der Winkel.
     * Wesen unter einem halben Block Hoehe und solche, gegen die man nicht stossen kann,
     * zaehlen nicht.
     *
     * Rueckgabe: die Nummer des Wesens, oder -1.
     */
    public static int sucheZiel(Player spieler, double reichweite, double grenzwinkel) {

        if(spieler == null) return -1;

        double x = spieler.getX();
        double y = spieler.getY() + spieler.getEyeHeight();
        double z = spieler.getZ();

        Vec3NT blick = new Vec3NT(spieler.getViewVector(1F)).multiply(reichweite);
        Vec3NT vorn = new Vec3NT(blick).add(x, y, z);
        Vec3NT links = new Vec3NT(blick).add(x, y, z).rotateAroundYDeg(-grenzwinkel).add(0, 10, 0);
        Vec3NT rechts = new Vec3NT(blick).add(x, y, z).rotateAroundYDeg(grenzwinkel).add(0, -10, 0);
        Vec3NT auge = new Vec3NT(x, y, z);

        AABB kasten = new AABB(
                Vec3NT.getMinX(vorn, links, rechts, auge), Vec3NT.getMinY(vorn, links, rechts, auge), Vec3NT.getMinZ(vorn, links, rechts, auge),
                Vec3NT.getMaxX(vorn, links, rechts, auge), Vec3NT.getMaxY(vorn, links, rechts, auge), Vec3NT.getMaxZ(vorn, links, rechts, auge));

        Entity bestes = null;
        double besterWinkel = 360D;
        Vec3NT zumWesen = new Vec3NT(0, 0, 0);

        for(Entity wesen : spieler.level.getEntities(spieler, kasten)) {

            if(wesen.getBbHeight() < 0.5F || !wesen.canBeCollidedWith()) continue;

            zumWesen.setComponents(wesen.getX() - x, wesen.getY() + wesen.getBbHeight() / 2D - y, wesen.getZ() - z);

            double skalar = zumWesen.xCoord * blick.xCoord + zumWesen.yCoord * blick.yCoord + zumWesen.zCoord * blick.zCoord;
            double nenner = zumWesen.length() * blick.length();
            double winkel = Math.abs(Math.acos(skalar / nenner) * 180D / Math.PI);

            if(winkel < besterWinkel && winkel < grenzwinkel) {
                besterWinkel = winkel;
                bestes = wesen;
            }
        }

        return bestes == null ? -1 : bestes.getId();
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

        /*
         * Der Quadro. Vier Rohre in einem Block, der beim Nachladen als Ganzes getauscht
         * wird -- daher MagazineFullReload statt MagazineSingleReload.
         */
        NtmItems.GUN_QUADRO = registry.register("gun_quadro", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(400).draw(7).inspect(40).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(40F).spreadHipfire(0F).delay(10).reload(55).jam(40)
                        .sound(NtmSoundEvents.GUN_ROCKET_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 4).addConfigs(rocket_qd))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_QUADRO_ANIMS).orchestra(Orchestras.ORCHESTRA_QUADRO)
        ).setDefaultAmmo(Ammo.ROCKET_HE, 4));

        /*
         * Der Raketenwerfer. Ein Rohr wie beim Panzerschreck, aber mit Zielerfassung: wer
         * zielt und klickt, sperrt ein Ziel fuer diesen einen Schuss auf.
         */
        NtmItems.GUN_MISSILE_LAUNCHER = registry.register("gun_missile_launcher", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(500).draw(20).inspect(40).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(50F).spreadHipfire(0F).delay(5).reload(48).jam(33)
                        .sound(NtmSoundEvents.GUN_ROCKET_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(rocket_ml))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire())
                .setupStandardConfiguration().pp(LAMBDA_MISSILE_LAUNCHER_PRIMARY_PRESS)
                .anim(LAMBDA_MISSILE_LAUNCHER_ANIMS).orchestra(Orchestras.ORCHESTRA_MISSILE_LAUNCHER)
        ).setDefaultAmmo(Ammo.ROCKET_HEAT, 5));
    }

    /**
     * DER QUADRO. Vier Rohre, vier gelenkte Raketen, und dann muss alles auf einmal neu
     * geladen werden. Der Schaden liegt hoeher als beim Panzerschreck, die Raketen fliegen
     * langsamer (Beschleunigung bis 4 statt 7) und ziehen dafuer dorthin, wohin der Schuetze
     * schaut.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_QUADRO_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -0.5, 50).addPos(0, 0, 0, 50));
            case RELOAD: return new BusAnimation()
                    .addBus("RELOAD_ROTATE", new BusAnimationSequence().addPos(0, 0, 60, 500, IType.SIN_FULL).addPos(0, 0, 60, 1500).addPos(0, 0, 0, 750, IType.SIN_FULL))
                    .addBus("RELOAD_PUSH", new BusAnimationSequence().addPos(-1, -1, 0, 0).addPos(-1, -1, 0, 500).addPos(-1, 0, 0, 350).addPos(0, 0, 0, 1000));
            case JAMMED:
            case INSPECT: return new BusAnimation()
                    .addBus("RELOAD_ROTATE", new BusAnimationSequence().addPos(0, 0, 60, 750, IType.SIN_FULL).addPos(0, 0, 60, 500).addPos(0, 0, 0, 750, IType.SIN_FULL));
            default: return null;
        }
    };

    /**
     * DER RAKETENWERFER. Ein Rohr, und was daraus kommt, fliegt geradeaus -- seine Raketen
     * sind eine unveraenderte Kopie der Vorlage. Die Lenkung sitzt woanders: WER ZIELT UND
     * DABEI KLICKT, ERFASST EIN ZIEL, und die Waffe merkt es sich fuer diesen einen Schuss.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MISSILE_LAUNCHER_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            case RELOAD: return new BusAnimation()
                    .addBus("BARREL", new BusAnimationSequence().addPos(0, 0, 1.5, 150).addPos(0, 0, 1.5, 2100).addPos(0, 0, 0, 150))
                    .addBus("OPEN", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(90, 0, 0, 500, IType.SIN_FULL).addPos(90, 0, 0, 1000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 2250).addPos(-1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_UP))
                    .addBus("MISSILE", new BusAnimationSequence().addPos(-10, 0, 0, 0).addPos(-10, 0, 0, 750).addPos(3, 0, 2, 0).addPos(0, 0, -6, 350, IType.SIN_FULL).addPos(0, 0, 0, 350, IType.SIN_UP));
            case JAMMED:
            case INSPECT: return new BusAnimation()
                    .addBus("BARREL", new BusAnimationSequence().addPos(0, 0, 1.5, 150).addPos(0, 0, 1.5, 1350).addPos(0, 0, 0, 150))
                    .addBus("OPEN", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(90, 0, 0, 500, IType.SIN_FULL).addPos(90, 0, 0, 250).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 1500).addPos(-1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_UP));
            default: return null;
        }
    };

    /**
     * Der Zielgriff des Raketenwerfers. Er sucht NUR, wenn der Schuetze zielt, und die Sperre
     * gilt nur fuer den Schuss, der unmittelbar folgt: nach dem Klick wird sie sofort wieder
     * geloest. Das ist die Regel des Originals, und sie ist der ganze Unterschied zum
     * Stinger, der die Sperre haelt.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_MISSILE_LAUNCHER_PRIMARY_PRESS = (stack, ctx) -> {

        if(GunBaseNTItem.getIsAiming(stack)) {
            int ziel = sucheZiel(ctx.getPlayer(), 150D, 20D);
            if(ziel != -1) {
                GunBaseNTItem.setLockonTarget(stack, ziel);
                GunBaseNTItem.setIsLockedOn(stack, true);
            }
        }

        Lego.LAMBDA_STANDARD_CLICK_PRIMARY.accept(stack, ctx);
        GunBaseNTItem.setIsLockedOn(stack, false);
    };


    /**
     * Die Raketen. Das Original legt sie als Vorlage an und zieht daraus je einen Satz fuer
     * Panzerschreck, Quadro, Raketenwerfer und die beiden NCRPA-Werfer; alle fuenf Saetze sind
     * portiert. Drei davon sind unveraenderte Kopien der Vorlage (makeRPZB, makeML, makeNCR
     * ohne Lenkung). Die Kopie bleibt trotzdem stehen: eine BulletConfig traegt eine eigene
     * Kennung, und zwei Werfer duerfen sich keine teilen.
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
        rocket_qd = new BulletConfig[rocket_template.length];
        rocket_ml = new BulletConfig[rocket_template.length];
        rocket_ncrpa = new BulletConfig[rocket_template.length];
        rocket_ncrpa_steer = new BulletConfig[rocket_template.length];

        for(int i = 0; i < rocket_template.length; i++) {
            rocket_rpzb[i] = rocket_template[i].clone();
            /* makeQD im Original: laengere Lebensdauer und die Lenkung, die am Zielen haengt. */
            rocket_qd[i] = rocket_template[i].clone().setLife(400).setOnUpdate(LAMBDA_STEERING_ACCELERATE);
            /* makeML im Original: eine unveraenderte Kopie -- der Raketenwerfer lenkt nicht
             * ueber die Rakete, sondern ueber die Zielerfassung an der Waffe. */
            rocket_ml[i] = rocket_template[i].clone();
            /* makeRPZB im Original: ebenfalls eine unveraenderte Kopie. */
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
