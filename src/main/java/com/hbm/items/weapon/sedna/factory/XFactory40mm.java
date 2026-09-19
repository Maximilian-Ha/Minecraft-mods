package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.logic.C130;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.items.ItemEnums.CasingType;
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
import com.hbm.util.SoundUtils;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactory40mm.
 *
 * Die 40-mm-Granaten und die 26-mm-Leuchtmunition. Beide fliegen langsam und im Bogen -- sie
 * sind keine Geschosse, sondern Wurfkoerper mit Zuender.
 *
 * FUENF GRANATEN: die Sprenggranate reisst fuenf Bloecke weit; die Hohlladung nur dreieinhalb,
 * setzt dem getroffenen Wesen aber das Dreifache obendrauf, weil sie fuer Panzerung gedacht ist;
 * die Abbruchgranate reisst als einzige wirklich Bloecke heraus; Brand- und Phosphorgranate
 * stellen ein stehendes Feuer hin und zuenden alles Brennbare ringsum an.
 *
 * DIE LEUCHTPISTOLE ist ein Einzellader mit hundert Schuss Haltbarkeit -- sie ist zum Leuchten
 * da, nicht zum Kaempfen, auch wenn die Leuchtkugel Ziele in Brand setzt.
 *
 * DER CONGO LAKE ist der Granatwerfer: vier Granaten im Roehrenmagazin, einzeln nachgeladen. Er
 * ist die zweite Waffe des Ports mit Animationen aus einer Datei.
 *
 * NICHT UEBERNOMMEN: die beiden Signalkugeln (Nachschub und Bewaffnung). Sie rufen eine C-130
 * herbei, die den Nachschub abwirft -- das Flugzeug fehlt im Port, und eine Signalkugel ohne
 * Flugzeug waere eine gewoehnliche Leuchtkugel mit irrefuehrendem Namen.
 *
 * DIE MK 108 ist die schwerste Waffe des Kalibers: dreissig Granaten im Gurt, alle zehn Ticks
 * eine. Ihr Gurt ist das aufwendigste Stueck Darstellung im ganzen Port.
 */
public class XFactory40mm {

    public static BulletConfig g26_flare;
    /** Die beiden Signalpatronen: blau ruft Nachschub, gruen Waffen und Munition. */
    public static BulletConfig g26_flare_supply;
    public static BulletConfig g26_flare_weapon;

    public static BulletConfig g40_he;
    public static BulletConfig g40_heat;
    public static BulletConfig g40_demo;
    public static BulletConfig g40_inc;
    public static BulletConfig g40_phosphorus;

    /** Die Leuchtkugel brennt, wo sie auftrifft -- zehn Sekunden lang. */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_IGNITE = (bullet, hr) -> {
        if(hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
            HbmLivingAttachments.getData(living).fire += 200;
        }
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE = (bullet, hr) -> {
        Lego.standardExplode(bullet, hr, 5F); bullet.discard();
    };

    /**
     * Die Hohlladung. Sie reisst weniger weit als die Sprenggranate, setzt dem getroffenen Wesen
     * aber das Dreifache des Geschosschadens obendrauf -- der Strahl wirkt nur dort, wo er
     * auftrifft, nicht ringsum.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_HEAT = (bullet, hr) -> {

        if(hr instanceof EntityHitResult self && bullet.tickCount < 3 && self.getEntity() == bullet.getOwner()) return;

        Lego.standardExplode(bullet, hr, 3.5F);
        bullet.discard();

        if(!(hr instanceof EntityHitResult ehr)) return;

        Entity hit = ehr.getEntity();
        DamageSource source = BulletConfig.getDamage(bullet.level, hit, bullet.getOwner(), DamageClass.EXPLOSION);

        if(hit instanceof LivingEntity living) {
            EntityDamageUtil.hurtNT(living, source, bullet.damage * 3F, true, true, 0.5D, 3F, 0.15F);
        } else {
            EntityDamageUtil.hurtIgnoreIFrame(hit, source, bullet.damage * 3F);
        }
    };

    /**
     * Die Abbruchgranate. Sie ist die einzige der fuenf, die wirklich Bloecke herausreisst --
     * die uebrigen lassen das Gelaende stehen.
     *
     * ABWEICHUNG: das Original setzt hier einen PlayerProcessorStandard, der dem Spieler eigene
     * Regeln fuer Schaden und Rueckstoss gibt. Der fehlt im Port; ohne ihn behandelt die
     * Explosion den Spieler wie jedes andere Wesen.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_DEMO = (bullet, hr) -> {

        if(hr instanceof EntityHitResult self && bullet.tickCount < 3 && self.getEntity() == bullet.getOwner()) return;

        Vec3 pos = hr.getLocation();
        new ExplosionVNT(bullet.level, pos.x, pos.y, pos.z, 5F, bullet.getOwner())
                .setBlockAllocator(new BlockAllocatorStandard())
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage))
                .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                .explode();

        bullet.discard();
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_INC = (bullet, hr) -> {
        spawnFire(bullet, hr, false, 200);
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE_PHOSPHORUS = (bullet, hr) -> {
        spawnFire(bullet, hr, true, 400);
    };

    /**
     * Brand- und Phosphorgranate. Sie reissen drei Bloecke weit, stellen ein stehendes Feuer hin
     * und zuenden ausserdem alles an, was in den umliegenden siebenundzwanzig Bloecken brennbar
     * ist -- ein Luftblock bekommt Feuer, sobald einer seiner sechs Nachbarn brennen kann.
     */
    public static void spawnFire(BulletBaseMK4 bullet, HitResult hr, boolean phosphorus, int duration) {

        if(hr instanceof EntityHitResult self && bullet.tickCount < 3 && self.getEntity() == bullet.getOwner()) return;

        Level level = bullet.level;
        Vec3 pos = hr.getLocation();

        Lego.standardExplode(bullet, hr, 3F);

        FireLingering fire = new FireLingering(level)
                .setArea(5, 2).setDuration(duration)
                .setFireType(phosphorus ? FireLingering.TYPE_PHOSPHORUS : FireLingering.TYPE_DIESEL);
        fire.at(pos);
        level.addFreshEntity(fire);

        bullet.discard();

        BlockPos center = BlockPos.containing(pos);

        for(int dx = -1; dx <= 1; dx++) for(int dy = -1; dy <= 1; dy++) for(int dz = -1; dz <= 1; dz++) {

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

        /* Die Leuchtpistole. Ein Schuss, dann muss die Huelse heraus. */
        NtmItems.GUN_FLAREGUN = registry.register("gun_flaregun", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(100).draw(7).inspect(39).crosshair(Crosshair.L_CIRCUMFLEX).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(15F).delay(20).reload(28).jam(33).sound(NtmSoundEvents.GUN_UNDERBARREL_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(g26_flare, g26_flare_supply, g26_flare_weapon))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_GL))
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAREGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAREGUN)
        ).setDefaultAmmo(Ammo.G26_FLARE, 3));

        /*
         * Der Congo Lake. Vier Granaten im Roehrenmagazin, einzeln nachgeladen, und beim
         * Nachladen laesst sich die Granatenart wechseln (reloadChangeType).
         */
        NtmItems.GUN_CONGOLAKE = registry.register("gun_congolake", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(400).draw(7).inspect(39).reloadSequential(true).reloadChangeType(true).crosshair(Crosshair.L_CIRCUMFLEX).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(20F).delay(24).reload(16, 16, 16, 0).jam(0).sound(NtmSoundEvents.GUN_CONGO_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 4).addConfigs(g40_he, g40_heat, g40_demo, g40_inc, g40_phosphorus))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_GL))
                .setupStandardConfiguration()
                .anim(LAMBDA_CONGOLAKE_ANIMS).orchestra(Orchestras.ORCHESTRA_CONGOLAKE)
        ).setDefaultAmmo(Ammo.G40_HE, 8));

        /*
         * Die MK 108. Sie hat keinen Ruecklauf im gewoehnlichen Sinn -- der Lauf selbst faehrt
         * zurueck (BARREL), nicht die ganze Waffe. Nach dem letzten Schuss klickt sie trocken
         * weiter (dryfireAfterAuto), damit der Schuetze merkt, dass der Gurt leer ist.
         */
        NtmItems.GUN_MK108 = registry.register("gun_mk108", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(5_000).draw(20).inspect(65).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(25F).delay(10).auto(true).dryfireAfterAuto(true).reload(135).jam(25).sound(NtmSoundEvents.GUN_MK108_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(g40_he, g40_heat, g40_demo, g40_inc, g40_phosphorus))
                        .offset(0.75, -0.125, -0.125)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_MK108))
                .setupStandardConfiguration()
                .anim(LAMBDA_MK108_ANIMS).orchestra(Orchestras.ORCHESTRA_MK108)
        ).setDefaultAmmo(Ammo.G40_HE, 50));
    }

    /** Die Munition. Getrennt, weil die MK 108 dieselben Granaten verschiesst. */
    public static void initAmmo() {

        g26_flare = new BulletConfig().setItem(Ammo.G26_FLARE).setCasing(CasingType.LARGE, 4).setLife(100).setVel(2F).setGrav(0.015D).setRenderRotations(false).setOnImpact(LAMBDA_STANDARD_IGNITE)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0x9E1616).setScale(2F).register("g26Flare"));
        g26_flare_supply = new BulletConfig().setItem(Ammo.G26_FLARE_SUPPLY).setCasing(CasingType.LARGE, 4).setLife(100).setVel(2F).setGrav(0.015D).setRenderRotations(false).setOnImpact(LAMBDA_STANDARD_IGNITE).setOnUpdate(LAMBDA_SPAWN_C130_SUPPLIES)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0x3C80F0).setScale(2F).register("g26FlareSupply"));
        g26_flare_weapon = new BulletConfig().setItem(Ammo.G26_FLARE_WEAPON).setCasing(CasingType.LARGE, 4).setLife(100).setVel(2F).setGrav(0.015D).setRenderRotations(false).setOnImpact(LAMBDA_STANDARD_IGNITE).setOnUpdate(LAMBDA_SPAWN_C130_WEAPONS)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0x278400).setScale(2F).register("g26FlareWeapon"));

        /* Alle fuenf Granaten teilen Flugzeit, Geschwindigkeit und Fall -- nur die Wirkung trennt sie. */
        BulletConfig g40_base = new BulletConfig().setLife(200).setVel(2F).setGrav(0.035D);

        g40_he = g40_base.clone().setItem(Ammo.G40_HE).setCasing(CasingType.LARGE, 4).setOnImpact(LAMBDA_STANDARD_EXPLODE)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0x777777).setScale(2F, 2F, 1.5F).register("g40"));
        g40_heat = g40_base.clone().setItem(Ammo.G40_HEAT).setCasing(CasingType.LARGE, 4).setOnImpact(LAMBDA_STANDARD_EXPLODE_HEAT).setDamage(0.5F)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0x5E6854).setScale(2F, 2F, 1.5F).register("g40heat"));
        g40_demo = g40_base.clone().setItem(Ammo.G40_DEMO).setCasing(CasingType.LARGE, 4).setOnImpact(LAMBDA_STANDARD_EXPLODE_DEMO).setDamage(0.75F)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0xE30000).setScale(2F, 2F, 1.5F).register("g40demo"));
        g40_inc = g40_base.clone().setItem(Ammo.G40_INC).setCasing(CasingType.LARGE, 4).setOnImpact(LAMBDA_STANDARD_EXPLODE_INC).setDamage(0.75F)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0xE86F20).setScale(2F, 2F, 1.5F).register("g40inc"));
        g40_phosphorus = g40_base.clone().setItem(Ammo.G40_PHOSPHORUS).setCasing(CasingType.LARGE, 4).setOnImpact(LAMBDA_STANDARD_EXPLODE_PHOSPHORUS).setDamage(0.75F)
                .setCasing(new SpentCasing(SpentCasingType.STRAIGHT).setColor(0xC8C8C8).setScale(2F, 2F, 1.5F).register("g40phos"));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> {
        Lego.handleStandardSmoke(ctx.entity, stack, 1500, 0.025D, 1.05D, 0);
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_GL = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_MK108 = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().random.nextGaussian() * 1.0) + 1F, (float) ctx.getPlayer().random.nextGaussian());
    };

    /**
     * Die Leuchtpistole. OPEN klappt den Lauf auf, SHELL wirft die Huelse aus, FLIP dreht die
     * ganze Waffe -- beim Betrachten dreimal um sich selbst.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_FLAREGUN_ANIMS = (stack, type) -> switch(type) {
        case EQUIP -> new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(-90, 0, 0, 0).addPos(0, 0, 0, 350, IType.SIN_DOWN));
        case CYCLE -> new BusAnimation()
                .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", new BusAnimationSequence().addPos(15, 0, 0, 50).addPos(15, 0, 0, 550).addPos(0, 0, 0, 100));
        case CYCLE_DRY -> new BusAnimation()
                .addBus("HAMMER", new BusAnimationSequence().addPos(15, 0, 0, 50).addPos(15, 0, 0, 550).addPos(0, 0, 0, 100));
        case RELOAD -> new BusAnimation()
                .addBus("OPEN", new BusAnimationSequence().addPos(45, 0, 0, 200, IType.SIN_FULL).addPos(45, 0, 0, 750).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("SHELL", new BusAnimationSequence().addPos(4, -8, -4, 0).addPos(4, -8, -4, 200).addPos(0, 0, -5, 500, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("FLIP", new BusAnimationSequence().addPos(0, 0, 0, 200).addPos(25, 0, 0, 200, IType.SIN_DOWN).addPos(25, 0, 0, 800).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        case JAMMED -> new BusAnimation()
                .addBus("OPEN", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(45, 0, 0, 200, IType.SIN_FULL).addPos(45, 0, 0, 500).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("FLIP", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 200).addPos(25, 0, 0, 200, IType.SIN_DOWN).addPos(25, 0, 0, 550).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        case INSPECT -> new BusAnimation()
                .addBus("FLIP", new BusAnimationSequence().addPos(-360 * 3, 0, 0, 1500, IType.SIN_FULL));
        default -> null;
    };

    /**
     * Der Congo Lake. Alle Bewegungen kommen aus der Animationsdatei; welche gespielt wird,
     * haengt am Magazinstand: der letzte Schuss und das Nachladen aus leerem Rohr sehen anders
     * aus als der Regelfall.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_CONGOLAKE_ANIMS = (stack, type) -> {

        int ammo = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                .getAmount(stack, NuclearTechMod.proxy.me().inventory);

        return switch(type) {
            case EQUIP -> ResourceManager.congolake_anim.get("Equip");
            case CYCLE -> ResourceManager.congolake_anim.get(ammo <= 1 ? "FireEmpty" : "Fire");
            case RELOAD -> ResourceManager.congolake_anim.get(ammo == 0 ? "ReloadEmpty" : "ReloadStart");
            case RELOAD_CYCLE -> ResourceManager.congolake_anim.get("Reload");
            case RELOAD_END -> ResourceManager.congolake_anim.get("ReloadEnd");
            case JAMMED -> ResourceManager.congolake_anim.get("Jammed");
            case INSPECT -> ResourceManager.congolake_anim.get("Inspect");
            default -> null;
        };
    };

    /**
     * Die MK 108. RECOIL und BARREL trennen, was bei anderen Waffen eines ist: die Waffe kippt
     * nur leicht, der Lauf selbst faehrt einen ganzen Block zurueck. CYCLE treibt den Gurt einen
     * Schritt weiter, SHELLS sagt dem Renderer, wie viele Granaten noch darin haengen.
     *
     * BEIM NACHLADEN wird der Deckel aufgeklappt (LID), der alte Gurt herausgezogen (BELT) und
     * die Trommel gewechselt (DRUM). Die ganze Waffe wird dabei angehoben (LIFT).
     *
     * BEIM BETRACHTEN wirft der Schuetze drei Granaten nacheinander in die Luft und faengt sie
     * wieder auf: GRENH traegt sie waagerecht, GRENV im Bogen, GRENS dreht sie dabei. Die Zahlen
     * dahinter sind aus der Wurfdauer abgeleitet, damit alle drei im gleichen Takt fliegen.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MK108_ANIMS = (stack, type) -> {

        int yeetHorizontal = 750;
        int untilImpact = yeetHorizontal * 9 / 15;
        int delay = 250;
        int height = 6;
        int arcUp = untilImpact * 5 / 8;
        int arcDown = untilImpact * 3 / 8;

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().setPos(45, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            case CYCLE -> {
                int amount = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                        .getAmount(stack, NuclearTechMod.proxy.me().inventory);
                yield new BusAnimation()
                        .addBus("RECOIL", new BusAnimationSequence().hold(50).addPos(0, 0, -0.25, 100, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                        .addBus("BARREL", new BusAnimationSequence().addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                        .addBus("CYCLE", new BusAnimationSequence().addPos(0, 0, 0, 100).addPos(1, 0, 0, 150))
                        .addBus("SHELLS", new BusAnimationSequence().setPos(amount - 1, 0, 0));
            }
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("HAMMER", new BusAnimationSequence().addPos(15, 0, 0, 50).addPos(15, 0, 0, 550).addPos(0, 0, 0, 100));
            case RELOAD -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(10, 0, 0, 500, IType.SIN_FULL).holdUntil(1250).addPos(-50, 0, 0, 750, IType.SIN_FULL).holdUntil(5500).addPos(0, 0, 0, 500, IType.SIN_FULL).hold(500).addPos(1, 0, 0, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("LID", new BusAnimationSequence().addPos(60, 0, 0, 500, IType.SIN_FULL).holdUntil(6000).addPos(0, 0, 0, 500, IType.SIN_UP))
                    .addBus("BELT", new BusAnimationSequence().setPos(1, 0, 0).hold(500).addPos(0, 0, 0, 750, IType.SIN_UP).holdUntil(4500).addPos(1, 0, 0, 750, IType.SIN_UP))
                    .addBus("DRUM", new BusAnimationSequence().hold(2000).addPos(2.5, 0, 0, 500, IType.SIN_DOWN).addPos(2.5, -2, -8, 500, IType.SIN_UP).setPos(4, -3, -8).addPos(2.5, 0, 0, 1000, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP));
            case JAMMED -> new BusAnimation()
                    .addBus("LID", new BusAnimationSequence().hold(250).addPos(45, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_UP))
                    .addBus("LIFT", new BusAnimationSequence().hold(1000).addPos(1, 0, 0, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL));
            case INSPECT -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().hold(untilImpact).addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL).hold(delay - 150).addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL).hold(delay - 150).addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("GRENH1", new BusAnimationSequence().setPos(9, 0, 0).addPos(-6, 0, 0, yeetHorizontal))
                    .addBus("GRENV1", new BusAnimationSequence().setPos(0, -2, 0).addPos(0, height, 0, arcUp, IType.SIN_DOWN).addPos(0, 2, 0, arcDown, IType.SIN_UP).addPos(0, 3, 0, yeetHorizontal - untilImpact, IType.SIN_DOWN))
                    .addBus("GRENS1", new BusAnimationSequence().addPos(360 * 2, 0, 0, untilImpact).setPos(0, 0, 0).addPos(360, 0, 0, yeetHorizontal - untilImpact))
                    .addBus("GRENH2", new BusAnimationSequence().setPos(9, 0, 0).hold(delay).addPos(-6, 0, 0, yeetHorizontal))
                    .addBus("GRENV2", new BusAnimationSequence().setPos(0, -2, 0).hold(delay).addPos(0, height, 0, arcUp, IType.SIN_DOWN).addPos(0, 2, 0, arcDown, IType.SIN_UP).addPos(0, 3, 0, yeetHorizontal - untilImpact, IType.SIN_DOWN))
                    .addBus("GRENS2", new BusAnimationSequence().hold(delay).addPos(360 * 2, 0, 0, untilImpact).setPos(0, 0, 0).addPos(360, 0, 0, yeetHorizontal - untilImpact))
                    .addBus("GRENH3", new BusAnimationSequence().setPos(9, 0, 0).hold(delay * 2).addPos(-6, 0, 0, yeetHorizontal))
                    .addBus("GRENV3", new BusAnimationSequence().setPos(0, -2, 0).hold(delay * 2).addPos(0, height, 0, arcUp, IType.SIN_DOWN).addPos(0, 2, 0, arcDown, IType.SIN_UP).addPos(0, 3, 0, yeetHorizontal - untilImpact, IType.SIN_DOWN))
                    .addBus("GRENS3", new BusAnimationSequence().hold(delay * 2).addPos(360 * 2, 0, 0, untilImpact).setPos(0, 0, 0).addPos(360, 0, 0, yeetHorizontal - untilImpact));
            default -> null;
        };
    };

    public static Consumer<Entity> LAMBDA_SPAWN_C130_SUPPLIES = (geschoss) -> rufeFlugzeug(geschoss, C130.Nutzlast.SUPPLIES);
    public static Consumer<Entity> LAMBDA_SPAWN_C130_WEAPONS  = (geschoss) -> rufeFlugzeug(geschoss, C130.Nutzlast.WEAPONS);

    /**
     * Ruft die C-130. Das geschieht nicht beim Aufschlag, sondern VIERZIG TICKS NACH DEM
     * ABSCHUSS -- die Leuchtkugel steigt noch, und das Flugzeug ist schon unterwegs. Das ist
     * im Original genauso; wer die Patrone senkrecht nach oben schiesst, bekommt das Flugzeug
     * ueber seinem eigenen Kopf.
     *
     * Es wird ueber der Gelaendeoberkante eingesetzt, nicht auf Hoehe der Leuchtkugel.
     */
    public static void rufeFlugzeug(Entity geschoss, C130.Nutzlast nutzlast) {

        if(geschoss.level.isClientSide) return;
        if(geschoss.tickCount != 40) return;
        if(!(geschoss instanceof BulletBaseMK4 kugel)) return;

        if(kugel.getOwner() != null) {
            SoundUtils.playAtVec3(kugel.level, kugel.getOwner().position(), NtmSoundEvents.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        int x = Mth.floor(kugel.getX());
        int z = Mth.floor(kugel.getZ());
        int y = kugel.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        C130 flugzeug = new C130(NtmEntityTypes.C130.get(), kugel.level);
        flugzeug.fac(kugel.level, x, y, z, nutzlast);
        kugel.level.addFreshEntity(flugzeug);
    }
}
