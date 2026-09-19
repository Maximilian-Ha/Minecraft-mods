package com.hbm.items.weapon.grenade;

import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.grenade.GrenadeUniversal;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectTiny;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.items.EnumMultiItem;
import com.hbm.items.special.PolaroidItem;
import com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.Lego;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.saveddata.satellite.SatelliteDetector;
import com.hbm.saveddata.satellite.SatelliteDetector.BurstIntensity;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.SoundUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell.FRAG;
import static com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell.NUKE;
import static com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell.STICK;
import static com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell.TECH;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.grenade.ItemGrenadeFilling.
 *
 * Die Fuellung -- der Knall. Sie sagt, was beim Hochgehen passiert, und in welchen Koerper sie
 * ueberhaupt passt: Schwarzpulver und Sprengstoff in die Handgranate und den Stiel, EMP und
 * Plasma nur in den Elektronikkoerper, die schweren Sachen nur in den Nuka-Koerper.
 *
 * Die beiden Farben sind der Anstrich: bodyColor der Koerper, labelColor der Aufkleber.
 *
 * NICHT UEBERNOMMEN, zwei der dreizehn Fuellungen des Originals:
 *
 *   LASER  -- verschiesst beim Hochgehen Strahlen auf alles im Umkreis. Der Port hat mit
 *             BulletBeamBase zwar die Huelle einer Strahlenentitaet, aber keine Abtastung
 *             (performHitscan fehlt ganz). Eine Laserfuellung ohne Strahl waere eine
 *             gewoehnliche Sprengfuellung mit irrefuehrendem Namen.
 *   SCHRAB -- zuendet eine Fleija. Dafuer fehlt die Wolkenentitaet (EntityCloudFleija); sie
 *             gehoert zur Fleija-Bombe und kommt mit deren Runde.
 *
 * Beide stehen im Original am Ende der Aufzaehlung, ihr Fehlen verschiebt also keine
 * Ordnungszahl der uebrigen.
 */
public class GrenadeFillingItem extends EnumMultiItem {

    /** Die Splitter, die eine Splittergranate auswirft. Drei Ticks Flugzeit, sonst nichts. */
    public static BulletConfig fragmentation;
    /** Die Streukugeln der Streufuellung -- sie zerplatzen beim Auftreffen. */
    public static BulletConfig pellets;
    /** Dieselben, nur schwerer: die Streukugeln des Nuka-Koerpers. */
    public static BulletConfig pellets_heavy;

    public GrenadeFillingItem(Properties properties) {
        super(properties, GrenadeFilling.class, true, true);
        initAmmo();
    }

    /** Die drei Geschossarten, die eine Fuellung auswirft. */
    public static void initAmmo() {
        if(fragmentation != null) return;
        fragmentation = new BulletConfig().setLife(3).setThresholdNegation(5F).setRicochetAngle(90).setRicochetCount(2);
        pellets = new BulletConfig().setLife(100).setGrav(0.04).setVel(1.5F).setOnImpact(LAMBDA_TINY_EXPLODE);
        pellets_heavy = new BulletConfig().setLife(100).setGrav(0.04).setVel(1.5F).setOnImpact(LAMBDA_EXPLODE);
    }

    public enum GrenadeFilling {
        POWDER(EXPLODE_POWDER, 0x424242, 0x939176, FRAG, STICK),
        HE(EXPLODE_HE, 0x595533, 0xA49D62, FRAG, STICK),
        DEMO(EXPLODE_DEMO, 0x595533, 0xDD4029, FRAG, STICK),
        INC(EXPLODE_INC, 0x5A5A5A, 0xFF5F21, FRAG, STICK),
        WP(EXPLODE_WP, 0xDCDCDC, 0xFF5F21, FRAG, STICK),
        CLUSTER(EXPLODE_CLUSTER, 0x5A5A5A, 0xFFC711, FRAG, STICK),
        EMP(EXPLODE_EMP, 0x93A1AC, 0x00FFFF, TECH),
        PLASMA(EXPLODE_PLASMA, 0x655B2C, 0x4CFF00, TECH),
        CLUSTER_HEAVY(EXPLODE_CLUSTER_HEAVY, 0x5A5A5A, 0xFF5F21, NUKE),
        NUCLEAR(EXPLODE_NUKE, 0xDFD7A8, 0xA49D62, NUKE),
        NUCLEAR_DEMO(EXPLODE_NUKE_DEMO, 0xDFD7A8, 0xDD4029, NUKE);

        public final Consumer<GrenadeUniversal> explode;
        public final Set<GrenadeShell> compatibleShells;
        public final int bodyColor;
        public final int labelColor;

        GrenadeFilling(Consumer<GrenadeUniversal> explode, int bodyColor, int labelColor, GrenadeShell... compatibleShells) {
            this.explode = explode;
            this.compatibleShells = EnumSet.copyOf(java.util.List.of(compatibleShells));
            this.bodyColor = bodyColor;
            this.labelColor = labelColor;
        }
    }

    public static final Consumer<GrenadeUniversal> EXPLODE_POWDER = (granate) -> standardExplode(granate, 5F, 10F, 5F, 0F);
    public static final Consumer<GrenadeUniversal> EXPLODE_HE = (granate) -> standardExplode(granate, 7.5F, 25F, 10F, 0.1F);

    /** Streufuellung: dreissig zerplatzende Kugeln, im Splitterkoerper ein Viertel mehr. */
    public static final Consumer<GrenadeUniversal> EXPLODE_CLUSTER = (granate) -> {
        standardExplode(granate, 7.5F, 15F, 10F, 0.1F);
        int anzahl = granate.getShell() == GrenadeShell.FRAG ? (int) (30 * 1.25) : 30;
        streue(granate, pellets, anzahl, 15F, 0.5, 0.75);
    };

    /** Dieselbe Streuung, nur halb so viele und doppelt so schwere Kugeln. */
    public static final Consumer<GrenadeUniversal> EXPLODE_CLUSTER_HEAVY = (granate) ->  {
        standardExplode(granate, 7.5F, 15F, 10F, 0.1F);
        streue(granate, pellets_heavy, 15, 30F, 0.5, 1.25);
    };

    /**
     * Die Abbruchfuellung. Sie ist die einzige, die wirklich Bloecke herausreisst.
     *
     * ABWEICHUNG: das Original setzt hier einen PlayerProcessorStandard, der dem Spieler eigene
     * Regeln fuer Schaden und Rueckstoss gibt. Der fehlt im Port; ohne ihn behandelt die
     * Explosion den Spieler wie jedes andere Wesen. Das gilt fuer alle Fuellungen hier.
     */
    public static final Consumer<GrenadeUniversal> EXPLODE_DEMO = (granate) -> {
        new ExplosionVNT(granate.level, granate.getX(), granate.getY(), granate.getZ(), 5F, granate.getOwner())
                .setBlockAllocator(new BlockAllocatorStandard())
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, 10F))
                .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                .explode();
    };

    public static final Consumer<GrenadeUniversal> EXPLODE_INC = (granate) -> brand(granate, FireLingering.TYPE_DIESEL, 200, 2);
    public static final Consumer<GrenadeUniversal> EXPLODE_WP = (granate) -> brand(granate, FireLingering.TYPE_PHOSPHORUS, 600, 3);

    /**
     * Brand- und Phosphorfuellung. Beide reissen drei Bloecke weit, stellen ein stehendes Feuer
     * hin und zuenden ringsum alles Brennbare an -- die Phosphorfuellung brennt dreimal so
     * lange und greift einen Block weiter.
     *
     * ABWEICHUNG: das Original schickt bei der Phosphorfuellung noch drei "haze"-Partikel los.
     * Diese Partikelart hat der Port nicht; das Feuer selbst ist da, der Schleier fehlt.
     */
    private static void brand(GrenadeUniversal granate, int feuerart, int dauer, int reichweite) {

        Level level = granate.level;
        Vec3 pos = granate.position();

        standardExplode(granate, 3F, 10F);

        FireLingering feuer = new FireLingering(level).setArea(6, 2).setDuration(dauer).setFireType(feuerart);
        feuer.at(pos);
        level.addFreshEntity(feuer);

        BlockPos mitte = BlockPos.containing(pos);

        for(int dx = -reichweite; dx <= reichweite; dx++)
        for(int dy = -reichweite; dy <= reichweite; dy++)
        for(int dz = -reichweite; dz <= reichweite; dz++) {

            BlockPos hier = mitte.offset(dx, dy, dz);
            if(!level.getBlockState(hier).isAir()) continue;

            for(Direction richtung : Direction.values()) {
                BlockPos nachbar = hier.relative(richtung);
                if(level.getBlockState(nachbar).isFlammable(level, nachbar, richtung.getOpposite())) {
                    level.setBlockAndUpdate(hier, Blocks.FIRE.defaultBlockState());
                    break;
                }
            }
        }
    }

    public static final Consumer<GrenadeUniversal> EXPLODE_EMP = (granate) -> {
        energieExplosion(granate, 15F, 3F, DamageClass.ELECTRIC);
        ExplosionNukeGeneric.empBlast(granate.level, BlockPos.containing(granate.position()), 5);
    };

    public static final Consumer<GrenadeUniversal> EXPLODE_PLASMA = (granate) -> energieExplosion(granate, 50F, 5F, DamageClass.PLASMA);

    public static final Consumer<GrenadeUniversal> EXPLODE_NUKE = (granate) -> {
        new ExplosionVNT(granate.level, granate.getX(), granate.getY(), granate.getZ(), 10F, granate.getOwner())
                .setEntityProcessor(new EntityProcessorCrossSmooth(2, 100).withRangeMod(1.5F))
                .explode();
        ExplosionNukeGeneric.incrementRad(granate.level, granate.getX(), granate.getY(), granate.getZ(), 1F);
        pilz(granate);
    };

    public static final Consumer<GrenadeUniversal> EXPLODE_NUKE_DEMO = (granate) -> {
        new ExplosionVNT(granate.level, granate.getX(), granate.getY(), granate.getZ(), 10F, granate.getOwner())
                .setBlockAllocator(new BlockAllocatorStandard(64))
                .setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()))
                .setEntityProcessor(new EntityProcessorCrossSmooth(2, 50).withRangeMod(1.5F))
                .explode();
        ExplosionNukeGeneric.incrementRad(granate.level, granate.getX(), granate.getY(), granate.getZ(), 1.5F);
        pilz(granate);
    };

    /**
     * Der Pilz ueber einer Atomgranate: ein Ausschlag auf den Strahlungsmeldern der Satelliten,
     * ein schwerer Knall und die Wolke.
     */
    private static void pilz(GrenadeUniversal granate) {

        Level level = granate.level;
        Vec3 pos = granate.position();

        SatelliteDetector.reportEvent(level, SatelliteDetector.DURATION_LOW, BurstIntensity.LOW, pos.x, pos.z);
        SoundUtils.playAtVec3(level, pos, NtmSoundEvents.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

        if(!(level instanceof ServerLevel serverLevel)) return;

        CompoundTag tag = new CompoundTag();
        tag.putString("type", "muke");
        tag.putBoolean("balefire", PolaroidItem.polaroidID == 11 || serverLevel.random.nextInt(100) == 0);
        PacketDistributor.sendToPlayersNear(serverLevel, null, pos.x, pos.y + 0.5, pos.z, 250,
                new AuxParticle(tag, pos.x, pos.y + 0.5, pos.z));
    }

    /**
     * EMP- und Plasmafuellung. Sie reissen keine Bloecke heraus und machen keinen Rauch --
     * nur einen Schlag und zwei Toene.
     *
     * ABWEICHUNG: das Original zeichnet dazu drei "plasmablast"-Partikelfaecher. Diese
     * Partikelart hat der Port nicht.
     */
    private static void energieExplosion(GrenadeUniversal granate, float schaden, float reichweite, DamageClass art) {

        Level level = granate.level;
        Vec3 pos = granate.position();

        new ExplosionVNT(level, pos.x, pos.y, pos.z, reichweite, granate.getOwner())
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, schaden).setDamageClass(art))
                .explode();

        /* ABWEICHUNG: das Original legt hier zwei Toene uebereinander, den Ufo-Schlag und den
         * Feuerwerksknall. Den Ufo-Schlag hat der Port nicht; der Knall bleibt. */
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 5.0F, 0.5F);
    }

    public static final BiConsumer<BulletBaseMK4, HitResult> LAMBDA_TINY_EXPLODE = (geschoss, treffer) -> {
        if(geschoss.tickCount < 2) return;
        Lego.tinyExplode(geschoss, treffer, 1.5F);
        geschoss.discard();
    };

    public static final BiConsumer<BulletBaseMK4, HitResult> LAMBDA_EXPLODE = (geschoss, treffer) -> {
        if(geschoss.tickCount < 2) return;
        Lego.standardExplode(geschoss, treffer, 5F);
        geschoss.discard();
    };

    public static void standardExplode(GrenadeUniversal granate, float reichweite, float schaden) {
        standardExplode(granate, reichweite, schaden, 0F, 0F);
    }

    public static void standardExplode(GrenadeUniversal granate, float reichweite, float schaden, float dt, float dr) {
        new ExplosionVNT(granate.level, granate.getX(), granate.getY(), granate.getZ(), reichweite, granate.getOwner())
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, schaden).setupPiercing(dt, dr))
                .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F))
                .explode();
    }

    public static void tinyExplode(GrenadeUniversal granate, float reichweite, float schaden) {
        new ExplosionVNT(granate.level, granate.getX(), granate.getY(), granate.getZ(), reichweite, granate.getOwner())
                .setEntityProcessor(new EntityProcessorCrossSmooth(0.5, schaden).setKnockback(0.25D))
                .setSFX(new ExplosionEffectTiny())
                .explode();
    }

    /** Die Splitter einer Splittergranate: im Splitterkoerper anderthalbmal so viele. */
    public static void standardFragmentation(GrenadeUniversal granate, float anzahl) {
        if(granate.getShell() == GrenadeShell.FRAG) anzahl *= 1.5F;
        streue(granate, fragmentation, (int) anzahl, 10F, 1, 1);
    }

    /**
     * Wirft Geschosse in alle Richtungen aus. Der Aufwaerts- und der Seitwaertsfaktor
     * entscheiden, ob die Wolke flach ausschwaermt (Splitter) oder nach oben geht (Streukugeln).
     */
    private static void streue(GrenadeUniversal granate, BulletConfig art, int anzahl, float schaden, double seite, double hoch) {

        Level level = granate.level;

        for(int i = 0; i < anzahl; i++) {

            /* Der Konstruktor nimmt Bogenmass und rechnet selbst in Grad um -- die Werte sind
             * deshalb die des Originals, nicht in Grad umgeschrieben. Die Splitter schwaermen
             * in alle Richtungen (voller Kreis), die Streukugeln nur nach oben (halber). */
            float gierung = level.random.nextFloat() * 2F * (float) Math.PI;
            float neigung = seite == 1
                    ? (level.random.nextFloat() - 0.5F) * 2F * (float) Math.PI
                    : (level.random.nextFloat() * 0.5F + 0.5F) * (float) Math.PI;

            BulletBaseMK4 geschoss = new BulletBaseMK4(level, art, schaden, 0F, gierung, neigung);
            geschoss.setPos(granate.getX(), granate.getY() + 0.05, granate.getZ());
            geschoss.setDeltaMovement(geschoss.getDeltaMovement().multiply(seite, hoch, seite));
            level.addFreshEntity(geschoss);
        }
    }
}
