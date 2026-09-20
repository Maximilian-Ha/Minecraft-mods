package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.logic.NukeExplosionMK5;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorBalefire;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.NtmItems;
import com.hbm.items.special.PolaroidItem;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.saveddata.satellite.SatelliteDetector;
import com.hbm.saveddata.satellite.SatelliteDetector.BurstIntensity;
import com.hbm.util.SoundUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryCatapult.
 *
 * DER FATMAN, der Schulterwerfer fuer Minikernwaffen, und seine sechs Sprengkoepfe. Bis
 * Runde 203 stand hier nur cluster_submunition -- das Geschoss, das die Streumunition des
 * Granatwerfers ausspuckt; der Werfer selbst fehlte ohne jede Begruendung.
 *
 * DIE SECHS KOEPFE unterscheiden sich nur im Aufschlag, nicht im Flug (alle 300 Ticks Leben,
 * Tempo 3, Fallbeschleunigung 0,025):
 *   NUKE_STANDARD  Kugelblitz ohne Blockschaden, Strahlung
 *   NUKE_DEMO      groesserer Blitz, setzt Bloecke in Brand, mehr Strahlung
 *   NUKE_HIGH      eine wirkliche Kernexplosion (NukeExplosionMK5 mit Staerke 35)
 *   NUKE_TOTS      acht kleine Koepfe auf einmal -- "tiny tots"
 *   NUKE_HIVE      zwoelf noch kleinere, ohne Strahlung und ohne Pilz
 *   NUKE_BALEFIRE  Blauflamme: verwandelt Bloecke statt sie wegzureissen
 *
 * DIE MUNITION GAB ES LAENGST. NtmItems.AMMO_STANDARD ist ein EnumMultiItem ueber
 * GunFactory.Ammo; alle sechs NUKE_-Werte stehen dort samt Platz in ORDER und damit im
 * Kreativreiter. Es fehlte nur das Sprengverhalten.
 *
 * ABWEICHUNGEN VOM ORIGINAL:
 *  - Der Pilz kommt im Port ueber das AuxParticle-Paket mit "type"="muke" beziehungsweise
 *    "tinytot", genau wie bei der Granate und den Bomben. NukeTorexCreator bleibt den
 *    grossen Sprengkoepfen vorbehalten; der Fatman benutzt ihn auch im Original nicht.
 *  - Der Ton heisst im Port weapon.muke_explosion statt "hbm:weapon.mukeExplosion".
 */
public class XFactoryCatapult {

    public static BulletConfig nuke_standard;
    public static BulletConfig nuke_demo;
    public static BulletConfig nuke_high;
    public static BulletConfig nuke_tots;
    public static BulletConfig nuke_hive;
    public static BulletConfig nuke_balefire;

    public static BulletConfig cluster_submunition;

    /**
     * Der Aufschlag eines Sprengkopfes. Die ersten drei Ticks trifft er den Schuetzen nicht --
     * sonst reisst der Werfer sich beim Abschuss selbst in Stuecke.
     *
     * @return wahr, wenn der Aufschlag zaehlt und das Geschoss eben verschwunden ist.
     */
    private static boolean aufschlag(BulletBaseMK4 geschoss, HitResult hr) {
        if(hr.getType() == HitResult.Type.ENTITY && geschoss.tickCount < 3
                && ((EntityHitResult) hr).getEntity() == geschoss.getOwner()) return false;
        if(!geschoss.isAlive()) return false;
        geschoss.discard();
        return true;
    }

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_STANDARD = (geschoss, hr) -> {
        if(!aufschlag(geschoss, hr)) return;
        Vec3 ort = hr.getLocation();

        ExplosionVNT vnt = new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 10);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, geschoss.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(geschoss.level, ort.x, ort.y, ort.z, 1F);
        spawnMush(geschoss, ort);
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_DEMO = (geschoss, hr) -> {
        if(!aufschlag(geschoss, hr)) return;
        Vec3 ort = hr.getLocation();

        ExplosionVNT vnt = new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 15);
        vnt.setBlockAllocator(new BlockAllocatorStandard(64));
        vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, geschoss.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(geschoss.level, ort.x, ort.y, ort.z, 1.5F);
        spawnMush(geschoss, ort);
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_HIGH = (geschoss, hr) -> {
        if(!aufschlag(geschoss, hr)) return;
        Vec3 ort = hr.getLocation();

        geschoss.level.addFreshEntity(NukeExplosionMK5.statFac(geschoss.level, 35, ort.x, ort.y, ort.z));
        spawnMush(geschoss, ort);
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_BALEFIRE = (geschoss, hr) -> {
        if(!aufschlag(geschoss, hr)) return;
        Vec3 ort = hr.getLocation();

        ExplosionVNT vnt = new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 10);
        vnt.setBlockAllocator(new BlockAllocatorStandard(64));
        vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBalefire()));
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, geschoss.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(geschoss.level, ort.x, ort.y, ort.z, 1.5F);
        /* Der einzige Kopf, dessen Pilz IMMER blau brennt -- die anderen wuerfeln darum. */
        pilz(geschoss, ort, "muke", true);
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_TINYTOT = (geschoss, hr) -> {
        if(!aufschlag(geschoss, hr)) return;
        Vec3 ort = hr.getLocation();

        ExplosionVNT vnt = new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 5);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, geschoss.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(geschoss.level, ort.x, ort.y, ort.z, 0.25F);
        pilz(geschoss, ort, "tinytot", null);
    };

    /**
     * Der Bienenstock. Zwoelf winzige Koepfe, jeder nur ein Viertel Schaden, und als einziger
     * Kopf ohne Strahlung und ohne Pilz -- zwoelf Pilze auf einmal waeren weder zu sehen noch
     * zu ertragen.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_HIVE = (geschoss, hr) -> {
        if(!aufschlag(geschoss, hr)) return;
        Vec3 ort = hr.getLocation();

        ExplosionVNT vnt = new ExplosionVNT(geschoss.level, ort.x, ort.y, ort.z, 5);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, geschoss.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_SUBMUNITION = (bullet, hr) -> {
        Vec3 position = hr.getLocation();
        ExplosionVNT vnt = new ExplosionVNT(bullet.level, position.x, position.y, position.z, 7.5F, bullet.getOwner());
        vnt.setBlockAllocator(new BlockAllocatorStandard());
        vnt.setBlockProcessor(new BlockProcessorStandard());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
        bullet.discard();
    };

    /**
     * Verstrahlt fuenf mal fuenf Chunks um den Einschlag, die vier Ecken ausgenommen (die
     * Bedingung |i| + |j| < 4 schneidet sie weg). Je weiter weg, desto weniger: 50 geteilt
     * durch den Chunk-Abstand plus eins.
     */
    public static void incrementRad(Level level, double posX, double posY, double posZ, float mult) {
        for(int i = -2; i <= 2; i++) {
            for(int j = -2; j <= 2; j++) {
                if(Math.abs(i) + Math.abs(j) >= 4) continue;
                BlockPos pos = BlockPos.containing(posX + i * 16, posY, posZ + j * 16);
                ChunkRadiationManager.proxy.incrementRad(level, pos, 50F / (Math.abs(i) + Math.abs(j) + 1) * mult);
            }
        }
    }

    /** Der gewoehnliche Pilz: blau nur an Polaroid 11 oder mit einem Prozent Glueck. */
    public static void spawnMush(BulletBaseMK4 geschoss, Vec3 ort) {
        pilz(geschoss, ort, "muke", null);
    }

    /**
     * Meldet den Blitz an die Satelliten, spielt den Knall und schickt die Pilzpartikel an
     * jeden Spieler im Umkreis von 250 Bloecken.
     *
     * @param balefire null laesst den Zufall entscheiden, sonst wird der Wert erzwungen.
     */
    private static void pilz(BulletBaseMK4 geschoss, Vec3 ort, String art, Boolean balefire) {

        Level level = geschoss.level;
        SatelliteDetector.reportEvent(level, SatelliteDetector.DURATION_LOW, BurstIntensity.LOW, geschoss.getX(), geschoss.getZ());
        SoundUtils.playAtVec3(level, new Vec3(ort.x, ort.y + 0.5, ort.z), NtmSoundEvents.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

        if(!(level instanceof ServerLevel serverLevel)) return;

        CompoundTag tag = new CompoundTag();
        tag.putString("type", art);
        tag.putBoolean("balefire", balefire != null ? balefire
                : PolaroidItem.polaroidID == 11 || serverLevel.random.nextInt(100) == 0);
        PacketDistributor.sendToPlayersNear(serverLevel, null, ort.x, ort.y + 0.5, ort.z, 250,
                new AuxParticle(tag, ort.x, ort.y + 0.5, ort.z));
    }

    public static void init(DeferredRegister.Items registry) {

        nuke_standard = new BulletConfig().setItem(Ammo.NUKE_STANDARD).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_STANDARD);
        nuke_demo = new BulletConfig().setItem(Ammo.NUKE_DEMO).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_DEMO);
        nuke_high = new BulletConfig().setItem(Ammo.NUKE_HIGH).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_HIGH);
        nuke_tots = new BulletConfig().setItem(Ammo.NUKE_TOTS).setProjectiles(8).setLife(300).setVel(3F).setGrav(0.025F).setSpread(0.1F).setDamage(0.35F).setOnImpact(LAMBDA_NUKE_TINYTOT);
        nuke_hive = new BulletConfig().setItem(Ammo.NUKE_HIVE).setProjectiles(12).setLife(300).setVel(1F).setGrav(0.025F).setSpread(0.15F).setDamage(0.25F).setOnImpact(LAMBDA_NUKE_HIVE);
        nuke_balefire = new BulletConfig().setItem(Ammo.NUKE_BALEFIRE).setDamage(2.5F).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_BALEFIRE);

        cluster_submunition = new BulletConfig().setLife(1_200).setGrav(0.025F).setOnImpact(LAMBDA_SUBMUNITION);

        /*
         * DER FATMAN. Ein Kopf im Rohr, 57 Zuege Nachladen, und er klemmt in zwei von fuenf
         * Faellen -- dafuer traegt jeder Treffer eine Kernwaffe. Das Fadenkreuz bleibt
         * sichtbar, auch im Visier: wer damit zielt, will wissen, wohin.
         */
        NtmItems.GUN_FATMAN = registry.register("gun_fatman", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(300).draw(20).inspect(30).reloadChangeType(true).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(100F).spreadHipfire(0F).delay(10).reload(57).jam(40).sound(NtmSoundEvents.GUN_FATMAN_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(nuke_standard, nuke_demo, nuke_high, nuke_tots, nuke_hive, nuke_balefire))
                        .offset(1, -0.0625 * 1.5, -0.1875D).offsetScoped(1, -0.0625 * 1.5, -0.125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_FATMAN))
                .setupStandardConfiguration()
                .anim(LAMBDA_FATMAN_ANIMS).orchestra(Orchestras.ORCHESTRA_FATMAN)
        ).setDefaultAmmoExpensive(Ammo.NUKE_STANDARD, 1));
    }

    /**
     * KEIN RUECKSTOSS. Der Fatman ist ein rueckstossfreies Rohr -- der Treibsatz blaest nach
     * hinten aus. Das Original setzt dafuer ebenfalls eine leere Lambda ein, statt den
     * Rueckstoss wegzulassen: das Geruest verlangt eine.
     */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_FATMAN = (stack, ctx) -> { };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_FATMAN_ANIMS = (stack, type) -> {
        switch(type) {
        case EQUIP: return new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        case CYCLE:
            /* Der Zeiger schnellt um 135 bis 270 Grad hoch und faellt wieder zurueck -- wie
             * weit, entscheidet der Zufall. Deshalb sieht kein Schuss aus wie der vorige. */
            return new BusAnimation()
                .addBus("GAUGE", new BusAnimationSequence().addPos(0, 0, 135 + (float) (Math.random() * 136), 100, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_DOWN))
                .addBus("PISTON", new BusAnimationSequence().addPos(0, 0, 3, 100, IType.SIN_UP))
                .addBus("NUKE", new BusAnimationSequence().addPos(0, 0, 3, 100, IType.SIN_UP).addPos(0, 0, 0, 0));
        case RELOAD: return new BusAnimation()
                .addBus("LID", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, -45, 250, IType.SIN_UP).addPos(0, 0, -45, 1200).addPos(0, 0, 0, 250, IType.SIN_UP))
                .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, -2, 500, IType.SIN_FULL).addPos(0, 0, -2, 1700).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("NUKE", new BusAnimationSequence().addPos(5, -4, 3, 0).addPos(5, -4, 3, 750).addPos(2, 0.5F, 3, 500, IType.SIN_UP).addPos(1, 0.5F, 3, 100).addPos(0, 0, 3, 100).addPos(0, 0, 3, 750).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("PISTON", new BusAnimationSequence().addPos(0, 0, 3, 0).addPos(0, 0, 3, 2200).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("EQUIP", new BusAnimationSequence().addPos(5, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 450).addPos(3, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL).addPos(0, 0, 0, 500).addPos(-10, 0, 0, 375, IType.SIN_DOWN).addPos(0, 0, 0, 375, IType.SIN_UP));
        case JAMMED: return new BusAnimation()
                .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, 0, 750).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-15, 0, 0, 250, IType.SIN_FULL).addPos(-15, 0, 0, 1000).addPos(0, 0, 0, 250, IType.SIN_FULL));
        case INSPECT: return new BusAnimation()
                .addBus("HANDLE", new BusAnimationSequence().addPos(0, 0, 0, 250).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("EQUIP", new BusAnimationSequence().addPos(-15, 0, 0, 250, IType.SIN_FULL).addPos(-15, 0, 0, 1000).addPos(0, 0, 0, 250, IType.SIN_FULL));
        default: return null;
        }
    };
}
