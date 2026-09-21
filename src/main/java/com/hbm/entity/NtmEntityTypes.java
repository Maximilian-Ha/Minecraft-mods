package com.hbm.entity;

import com.hbm.entity.effect.DigammaSpear;
import com.hbm.entity.effect.BlackHole;
import com.hbm.entity.grenade.Dynamite;
import com.hbm.entity.grenade.DynamiteFishing;
import com.hbm.entity.item.BuoyantItemEntity;
import com.hbm.entity.item.WasteItemEntity;
import com.hbm.entity.mob.FbiAgent;
import com.hbm.entity.mob.FbiDrone;
import com.hbm.entity.mob.Pigeon;
import com.hbm.entity.grenade.GrenadeUniversal;
import com.hbm.entity.projectile.Boxcar;
import com.hbm.entity.item.ParachuteCrate;
import com.hbm.entity.projectile.DuchessGambit;
import com.hbm.entity.projectile.Chemical;
import com.hbm.entity.projectile.CoinEntity;
import com.hbm.entity.projectile.Torpedo;
import com.hbm.entity.projectile.Sawblade;
import com.hbm.entity.effect.FalloutRain;
import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.effect.Mist;
import com.hbm.entity.effect.RagingVortex;
import com.hbm.entity.effect.Vortex;
import com.hbm.entity.item.FallingBlockEntityNT;
import com.hbm.entity.item.MovingItem;
import com.hbm.entity.item.MovingPackage;
import com.hbm.entity.item.TNTPrimedBase;
import com.hbm.entity.logic.*;
import com.hbm.entity.missile.MissileAntiBallistic;
import com.hbm.entity.missile.MissileShuttle;
import com.hbm.entity.missile.MissileStealth;
import com.hbm.entity.missile.MissileTier0.*;
import com.hbm.entity.missile.MissileTier1.*;
import com.hbm.entity.missile.MissileTier2.*;
import com.hbm.entity.missile.MissileTier3.MissileBurst;
import com.hbm.entity.missile.MissileTier3.MissileDrill;
import com.hbm.entity.missile.MissileTier3.MissileInferno;
import com.hbm.entity.missile.MissileTier3.MissileRain;
import com.hbm.entity.missile.MissileTier4.*;
import com.hbm.entity.missile.MissileCustom;
import com.hbm.entity.missile.SatellitePod;
import com.hbm.entity.missile.Soyuz;
import com.hbm.entity.missile.SoyuzCapsule;
import com.hbm.entity.mob.CreeperNuclear;
import com.hbm.entity.mob.Ghost;
import com.hbm.entity.mob.Quackos;
import com.hbm.entity.mob.CreeperGold;
import com.hbm.entity.mob.CreeperPhosgene;
import com.hbm.entity.mob.CreeperTainted;
import com.hbm.entity.mob.MaskMan;
import com.hbm.entity.mob.RadBeast;
import com.hbm.entity.mob.Ufo;
import com.hbm.entity.mob.botprime.BotPrimeBody;
import com.hbm.entity.mob.botprime.BotPrimeHead;
import com.hbm.entity.mob.CreeperVolatile;
import com.hbm.entity.mob.CyberCrab;
import com.hbm.entity.mob.TaintCrab;
import com.hbm.entity.mob.TeslaCrab;
import com.hbm.entity.mob.Duck;
import com.hbm.entity.mob.UndeadSoldier;
import com.hbm.entity.projectile.*;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NtmEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, NuclearTechMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BulletBeamBase>> BULLET_BEAM = ENTITY_TYPES.register(
            "bullet_beam",
            () -> EntityType.Builder.<BulletBeamBase>of(BulletBeamBase::new, MobCategory.MISC)
                    .noSummon()
                    .fireImmune()
                    .setTrackingRange(250)
                    .sized(0.5F, 0.5F)
                    .build("bullet_beam")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<BulletBaseMK4>> BULLET_MK4 = ENTITY_TYPES.register(
            "bullet_mk4",
            () -> EntityType.Builder.<BulletBaseMK4>of(BulletBaseMK4::new, MobCategory.MISC)
                    .noSummon()
                    .fireImmune()
                    .setTrackingRange(250)
                    .sized(0.5F, 0.5F)
                    .build("bullet_mk4")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<NukeExplosionMK5>> NUKE_MK5 = ENTITY_TYPES.register("nuke_mk5", () -> EntityType.Builder.of(NukeExplosionMK5::new, MobCategory.MISC).sized(1.0F, 1.0F).build("nuke_mk5"));
    /* Runde 42: die Truemmer einer Kernschmelze. Die Masse setzt jede Entitaet selbst nach ihrem Typ. */
    public static final DeferredHolder<EntityType<?>, EntityType<RBMKDebris>> RBMK_DEBRIS = ENTITY_TYPES.register("rbmk_debris", () -> EntityType.Builder.<RBMKDebris>of(RBMKDebris::new, MobCategory.MISC).noSummon().setTrackingRange(128).sized(1.0F, 1.0F).build("rbmk_debris"));
    public static final DeferredHolder<EntityType<?>, EntityType<ZirnoxDebris>> ZIRNOX_DEBRIS = ENTITY_TYPES.register("zirnox_debris", () -> EntityType.Builder.<ZirnoxDebris>of(ZirnoxDebris::new, MobCategory.MISC).noSummon().setTrackingRange(128).sized(1.0F, 1.0F).build("zirnox_debris"));
    /* Der Digamma-Speer, Runde 276. Sichtweite wie im Original weit ueber dem Ueblichen --
     * er ist zehn Bloecke hoch und soll von weitem zu sehen sein. */
    public static final DeferredHolder<EntityType<?>, EntityType<DigammaSpear>> DIGAMMA_SPEAR = ENTITY_TYPES.register("digamma_spear", () -> EntityType.Builder.<DigammaSpear>of(DigammaSpear::new, MobCategory.MISC).noSummon().setTrackingRange(512).sized(2.0F, 10.0F).build("digamma_spear"));
    public static final DeferredHolder<EntityType<?>, EntityType<Sawblade>> SAWBLADE = ENTITY_TYPES.register("sawblade", () -> EntityType.Builder.<Sawblade>of(Sawblade::new, MobCategory.MISC).noSummon().setTrackingRange(250).sized(1.0F, 1.0F).build("sawblade"));
    public static final DeferredHolder<EntityType<?>, EntityType<Cog>> COG = ENTITY_TYPES.register("cog", () -> EntityType.Builder.<Cog>of(Cog::new, MobCategory.MISC).noSummon().setTrackingRange(250).sized(1.0F, 1.0F).build("cog"));
    public static final DeferredHolder<EntityType<?>, EntityType<Mist>> MIST = ENTITY_TYPES.register("mist", () -> EntityType.Builder.<Mist>of(Mist::new, MobCategory.MISC).noSummon().fireImmune().setTrackingRange(250).sized(1.0F, 1.0F).build("mist"));
    public static final DeferredHolder<EntityType<?>, EntityType<FireLingering>> FIRE_LINGERING = ENTITY_TYPES.register("fire_lingering", () -> EntityType.Builder.<FireLingering>of(FireLingering::new, MobCategory.MISC).noSummon().fireImmune().setTrackingRange(250).sized(1.0F, 1.0F).build("fire_lingering"));
    public static final DeferredHolder<EntityType<?>, EntityType<NukeExplosionMK3>> NUKE_MK3 = ENTITY_TYPES.register("nuke_mk3", () -> EntityType.Builder.of(NukeExplosionMK3::new, MobCategory.MISC).sized(1.0F, 1.0F).build("nuke_mk3"));
    public static final DeferredHolder<EntityType<?>, EntityType<NukeExplosionBalefire>> NUKE_BALEFIRE = ENTITY_TYPES.register("nuke_explosion_balefire", () -> EntityType.Builder.<NukeExplosionBalefire>of(NukeExplosionBalefire::new, MobCategory.MISC).sized(1.0F, 1.0F).build("nuke_explosion_balefire"));

    public static final DeferredHolder<EntityType<?>, EntityType<FalloutRain>> FALLOUT_RAIN = ENTITY_TYPES.register(
            "fallout_rain",
            () -> EntityType.Builder.<FalloutRain>of(FalloutRain::new, MobCategory.MISC)
                    .setTrackingRange(1000)
                    .sized(4F, 20F)
                    .fireImmune()
                    .build("fallout_rain"));

    public static final DeferredHolder<EntityType<?>, EntityType<FallingBlockEntityNT>> FALLING_BLOCK = ENTITY_TYPES.register(
            "falling_block",
            () -> EntityType.Builder.of(FallingBlockEntityNT::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20)
                    .build("falling_block"));

    /*
     * Der Gegenstand auf dem Foerderband. Er wird nicht vom Band bewegt, sondern bewegt sich
     * selbst: bei jedem Takt fragt er den Block unter sich, wohin es geht. Kurze Taktweite,
     * damit die Fahrt auch beim Zuschauer fluessig aussieht.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<MovingItem>> MOVING_ITEM = ENTITY_TYPES.register(
            "moving_item",
            () -> EntityType.Builder.of(MovingItem::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(2)
                    .build("moving_item"));

    /**
     * Das Paket. Groesser als ein einzelner Gegenstand -- es traegt bis zu einundzwanzig Stapel
     * und sieht auch danach aus.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<MovingPackage>> MOVING_PACKAGE = ENTITY_TYPES.register(
            "moving_package",
            () -> EntityType.Builder.of(MovingPackage::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(2)
                    .build("moving_package"));

    public static final DeferredHolder<EntityType<?>, EntityType<Duck>> DUCK = ENTITY_TYPES.register(
            "duck",
            () -> EntityType.Builder.of(Duck::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.7F)
                    .eyeHeight(0.644F)
                    .clientTrackingRange(10)
                    .build("duck"));

    /* Fuenfundzwanzigmal die Ente: 0,3 x 0,7 mal 25, wie im Original. */
    public static final DeferredHolder<EntityType<?>, EntityType<Quackos>> QUACKOS = ENTITY_TYPES.register(
            "quackos",
            () -> EntityType.Builder.of(Quackos::new, MobCategory.CREATURE)
                    .sized(7.5F, 17.5F)
                    .clientTrackingRange(10)
                    .build("quackos"));

    /* Runde 291: der FBI-Beamte. Menschenmass, feuerfest -- im Original setzt er
     * isImmuneToFire im Konstruktor. */
    public static final DeferredHolder<EntityType<?>, EntityType<FbiAgent>> FBI_AGENT = ENTITY_TYPES.register(
            "fbi_agent",
            () -> EntityType.Builder.of(FbiAgent::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .build("fbi_agent"));

    /* Runde 292: der Quadrokopter der Razzia. Ein knapper Block breit, flach, und er
     * haengt in der Luft -- Schwerkraft hat er keine. */
    public static final DeferredHolder<EntityType<?>, EntityType<FbiDrone>> FBI_DRONE = ENTITY_TYPES.register(
            "fbi_drone",
            () -> EntityType.Builder.<FbiDrone>of(FbiDrone::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.4F)
                    .clientTrackingRange(16)
                    .build("fbi_drone"));

    /* Runde 295: die Taube. Ein halber Block breit, einer hoch -- die Masse des Originals. */
    public static final DeferredHolder<EntityType<?>, EntityType<Pigeon>> PIGEON = ENTITY_TYPES.register(
            "pigeon",
            () -> EntityType.Builder.of(Pigeon::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.0F)
                    .clientTrackingRange(8)
                    .build("pigeon"));

    /* Das Gespenst: Menschenmass, und es verschwindet, sobald jemand hinsieht. */
    public static final DeferredHolder<EntityType<?>, EntityType<Ghost>> GHOST = ENTITY_TYPES.register(
            "ghost",
            () -> EntityType.Builder.of(Ghost::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(16)
                    .build("ghost"));

    /* Runde 290: der fallengelassene Atommuell. Unzerstoerbar, sonst ein gewoehnlicher
     * liegender Gegenstand -- ein Viertelblock gross, kurze Verfolgungsweite, traege
     * Nachfuehrung. */
    public static final DeferredHolder<EntityType<?>, EntityType<WasteItemEntity>> WASTE_ITEM = ENTITY_TYPES.register(
            "waste_item",
            () -> EntityType.Builder.<WasteItemEntity>of(WasteItemEntity::new, MobCategory.MISC)
                    .noSummon()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("waste_item"));

    public static final DeferredHolder<EntityType<?>, EntityType<CreeperNuclear>> CREEPER_NUCLEAR =
            ENTITY_TYPES.register("creeper_nuclear",
                    () -> EntityType.Builder.of(CreeperNuclear::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.7F)
                            .build("creeper_nuclear"));

    /**
     * Der Untote Soldat, Runde 228. Masse wie ein Spieler -- das Original setzt fuer ihn kein
     * eigenes setSize, EntityMob behaelt also die 0,6 mal 1,8 aus Entity.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<UndeadSoldier>> UNDEAD_SOLDIER =
            ENTITY_TYPES.register("undead_soldier",
                    () -> EntityType.Builder.of(UndeadSoldier::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .build("undead_soldier"));

    /**
     * Die drei uebrigen Creeper, Runde 240. Masse wie jeder Creeper -- das Original setzt
     * fuer keinen von ihnen ein eigenes setSize. Anders als der nukleare und der verseuchte
     * erscheinen diese drei von selbst in der Welt; wo, steht im Biom-Aenderer.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<CreeperGold>> CREEPER_GOLD =
            ENTITY_TYPES.register("creeper_gold",
                    () -> EntityType.Builder.<CreeperGold>of(CreeperGold::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.7F)
                            .build("creeper_gold"));

    public static final DeferredHolder<EntityType<?>, EntityType<CreeperVolatile>> CREEPER_VOLATILE =
            ENTITY_TYPES.register("creeper_volatile",
                    () -> EntityType.Builder.<CreeperVolatile>of(CreeperVolatile::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.7F)
                            .build("creeper_volatile"));

    public static final DeferredHolder<EntityType<?>, EntityType<CreeperPhosgene>> CREEPER_PHOSGENE =
            ENTITY_TYPES.register("creeper_phosgene",
                    () -> EntityType.Builder.<CreeperPhosgene>of(CreeperPhosgene::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.7F)
                            .build("creeper_phosgene"));

    /**
     * Der verseuchte Creeper, Runde 238. Masse wie jeder Creeper -- das Original setzt
     * fuer ihn kein eigenes setSize.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<CreeperTainted>> CREEPER_TAINTED =
            ENTITY_TYPES.register("creeper_tainted",
                    () -> EntityType.Builder.<CreeperTainted>of(CreeperTainted::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.7F)
                            .build("creeper_tainted"));

    /**
     * Der Maskenmann, Runde 280. Zwei Bloecke breit und fuenf hoch -- setSize(2F, 5F) des
     * Originals. Er erscheint nicht von selbst und ist feuerfest.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<MaskMan>> MASKMAN =
            ENTITY_TYPES.register("maskman",
                    () -> EntityType.Builder.<MaskMan>of(MaskMan::new, MobCategory.MONSTER)
                            .sized(2.0F, 5.0F)
                            .fireImmune()
                            .noSummon()
                            .build("maskman"));

    /**
     * Das Strahlenbiest, Runde 281. Masse der Vanilla-Lohe, die es auch darstellt. Es faellt
     * nicht von selbst vom Himmel -- das Original setzt es ueber den Bosszaehler.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<RadBeast>> RAD_BEAST =
            ENTITY_TYPES.register("rad_beast",
                    () -> EntityType.Builder.<RadBeast>of(RadBeast::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .fireImmune()
                            .noSummon()
                            .build("rad_beast"));

    /**
     * Der Wurm, Runde 282. Kopf drei Bloecke, Glieder zwei -- die Masse des Originals
     * (setSize(3F, 3F) beim Kopf, (2F, 2F) in der Basis). Beide fliegen durch Bloecke
     * hindurch und erscheinen nicht von selbst.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<BotPrimeHead>> BOT_PRIME_HEAD =
            ENTITY_TYPES.register("bot_prime_head",
                    () -> EntityType.Builder.<BotPrimeHead>of(BotPrimeHead::new, MobCategory.MONSTER)
                            .sized(3.0F, 3.0F)
                            .fireImmune()
                            .noSummon()
                            .build("bot_prime_head"));

    public static final DeferredHolder<EntityType<?>, EntityType<BotPrimeBody>> BOT_PRIME_BODY =
            ENTITY_TYPES.register("bot_prime_body",
                    () -> EntityType.Builder.<BotPrimeBody>of(BotPrimeBody::new, MobCategory.MONSTER)
                            .sized(2.0F, 2.0F)
                            .fireImmune()
                            .noSummon()
                            .build("bot_prime_body"));

    /**
     * Das UFO, Runde 283. Fuenfzehn Bloecke breit und vier hoch -- setSize(15F, 4F) des
     * Originals. Es fliegt durch Bloecke und erscheint nicht von selbst.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<Ufo>> UFO =
            ENTITY_TYPES.register("ufo",
                    () -> EntityType.Builder.<Ufo>of(Ufo::new, MobCategory.MONSTER)
                            .sized(15.0F, 4.0F)
                            .fireImmune()
                            .noSummon()
                            .build("ufo"));

    /**
     * Die Kybernetische Krabbe, Runde 236. Masse wie im Original: drei Viertel breit,
     * gut ein Drittel hoch -- sie passt unter jeden Ueberhang.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<CyberCrab>> CYBER_CRAB =
            ENTITY_TYPES.register("cyber_crab",
                    () -> EntityType.Builder.<CyberCrab>of(CyberCrab::new, MobCategory.MONSTER)
                            .sized(0.75F, 0.35F)
                            .build("cyber_crab"));

    /** Die Teslakrabbe. Ebenso breit, aber deutlich hoeher -- die Spule steht auf ihr. */
    public static final DeferredHolder<EntityType<?>, EntityType<TeslaCrab>> TESLA_CRAB =
            ENTITY_TYPES.register("tesla_crab",
                    () -> EntityType.Builder.<TeslaCrab>of(TeslaCrab::new, MobCategory.MONSTER)
                            .sized(0.75F, 1.25F)
                            .build("tesla_crab"));

    /**
     * Die Taint-Krabbe. Anderthalb Bloecke in jede Richtung -- die groesste der drei.
     * Gerufen wird sie nicht vom Nest, sondern vom Taint: wer als Teslakrabbe hineinlaeuft,
     * kommt als diese wieder heraus.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<TaintCrab>> TAINT_CRAB =
            ENTITY_TYPES.register("taint_crab",
                    () -> EntityType.Builder.<TaintCrab>of(TaintCrab::new, MobCategory.MONSTER)
                            .sized(1.25F, 1.25F)
                            .build("taint_crab"));

    /** Der Tau-Bolzen der Krabbe. Einen halben Block gross, wie jedes Geschoss des Originals. */
    public static final DeferredHolder<EntityType<?>, EntityType<TauShot>> TAU_SHOT =
            ENTITY_TYPES.register("tau_shot",
                    () -> EntityType.Builder.<TauShot>of(TauShot::new, MobCategory.MISC)
                            .noSummon()
                            .setTrackingRange(250)
                            .sized(0.5F, 0.5F)
                            .build("tau_shot"));

    public static final DeferredHolder<EntityType<?>, EntityType<TNTPrimedBase>> TNT_PRIMED_BASE = ENTITY_TYPES.register(
            "tnt_primed_base",
            () -> EntityType.Builder.<TNTPrimedBase>of(TNTPrimedBase::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.98F, 0.98F)
                    .eyeHeight(0.15F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("tnt_primed_base"));

    public static final DeferredHolder<EntityType<?>, EntityType<Shrapnel>> SHRAPNEL = ENTITY_TYPES.register("shrapnel", () -> EntityType.Builder.<Shrapnel>of(Shrapnel::new, MobCategory.MISC).sized(0.25F, 0.25F).fireImmune().build("shrapnel"));
    public static final DeferredHolder<EntityType<?>, EntityType<Rubble>> RUBBLE = ENTITY_TYPES.register("rubble", () -> EntityType.Builder.of(Rubble::new, MobCategory.MISC).sized(0.25F, 0.25F).build("rubble"));

    public static final DeferredHolder<EntityType<?>, EntityType<Rocket>> ROCKET = ENTITY_TYPES.register("rocket", () -> EntityType.Builder.of(Rocket::new, MobCategory.MISC).sized(0.5F, 0.5F).build("rocket"));

    /* Der Gueterwagen aus der Luft. Drei Bloecke breit, drei hoch -- ungefaehr sein Modell. */
    public static final DeferredHolder<EntityType<?>, EntityType<Boxcar>> BOXCAR = ENTITY_TYPES.register("boxcar",
            () -> EntityType.Builder.<Boxcar>of(Boxcar::new, MobCategory.MISC).noSummon().setTrackingRange(250).sized(3.0F, 3.0F).fireImmune().build("boxcar"));

    /* Das Transportflugzeug. Acht Bloecke breit, vier hoch -- dieselben Masse wie der Bomber. */
    public static final DeferredHolder<EntityType<?>, EntityType<C130>> C130 = ENTITY_TYPES.register("c130",
            () -> EntityType.Builder.<C130>of(C130::new, MobCategory.MISC).noSummon().sized(8F, 4F).setTrackingRange(1000).build("c130"));

    /* Die Kiste am Fallschirm. Einen Block gross, wie das Modell, das sie zeigt. */
    public static final DeferredHolder<EntityType<?>, EntityType<ParachuteCrate>> PARACHUTE_CRATE = ENTITY_TYPES.register("parachute_crate",
            () -> EntityType.Builder.<ParachuteCrate>of(ParachuteCrate::new, MobCategory.MISC).noSummon().setTrackingRange(250).sized(1.0F, 1.0F).fireImmune().build("parachute_crate"));

    /* Der Torpedo aus der Luft. Ein Block breit, drei hoch -- ungefaehr sein Modell. */
    public static final DeferredHolder<EntityType<?>, EntityType<Torpedo>> TORPEDO = ENTITY_TYPES.register("torpedo",
            () -> EntityType.Builder.<Torpedo>of(Torpedo::new, MobCategory.MISC).noSummon().setTrackingRange(250).sized(1.0F, 3.0F).fireImmune().build("torpedo"));

    /* Die Muenze der NI4NI. Einen Block gross, obwohl das Modell viel kleiner ist -- der
     * Strahl muss sie im Flug treffen koennen, und die Trefferflaeche ist es, die zaehlt. */
    /* Die Chemikalienwolke des Chemiewerfers. Winzig, aber sehr zahlreich -- ein Viertel
     * Block gross, wie die Granate, und feuerfest, weil sie selbst brennen kann. */
    public static final DeferredHolder<EntityType<?>, EntityType<Chemical>> CHEMICAL = ENTITY_TYPES.register("chemical",
            () -> EntityType.Builder.<Chemical>of(Chemical::new, MobCategory.MISC).noSummon().setTrackingRange(100).sized(0.25F, 0.25F).fireImmune().build("chemical"));

    public static final DeferredHolder<EntityType<?>, EntityType<CoinEntity>> COIN = ENTITY_TYPES.register("coin",
            () -> EntityType.Builder.<CoinEntity>of(CoinEntity::new, MobCategory.MISC).noSummon().setTrackingRange(100).sized(1.0F, 1.0F).build("coin"));

    /* Das Luftschiff aus der Luft. Zehn Bloecke breit, acht hoch -- grob sein Modell. */
    public static final DeferredHolder<EntityType<?>, EntityType<DuchessGambit>> DUCHESS_GAMBIT = ENTITY_TYPES.register("duchess_gambit",
            () -> EntityType.Builder.<DuchessGambit>of(DuchessGambit::new, MobCategory.MISC).noSummon().setTrackingRange(250).sized(10.0F, 8.0F).fireImmune().build("duchess_gambit"));

    /* Die geworfene Granate. Ein Viertelblock gross, wie im Original (setSize(0.25F, 0.25F)). */
    /* Die Dynamitstange, Runde 234. Dieselben Masse wie die Granate -- ein Viertelblock. */
    public static final DeferredHolder<EntityType<?>, EntityType<Dynamite>> DYNAMITE = ENTITY_TYPES.register("dynamite",
            () -> EntityType.Builder.<Dynamite>of(Dynamite::new, MobCategory.MISC).sized(0.25F, 0.25F).build("dynamite"));

    /* Runde 293: der Fischerdynamit -- dieselbe Stange, anderer Knall. */
    public static final DeferredHolder<EntityType<?>, EntityType<DynamiteFishing>> DYNAMITE_FISHING = ENTITY_TYPES.register("dynamite_fishing",
            () -> EntityType.Builder.<DynamiteFishing>of(DynamiteFishing::new, MobCategory.MISC).sized(0.25F, 0.25F).build("dynamite_fishing"));

    /* Runde 293: der treibende Gegenstand. Wie der gewoehnliche, nur dass er im Wasser
     * nicht sinkt. */
    public static final DeferredHolder<EntityType<?>, EntityType<BuoyantItemEntity>> BUOYANT_ITEM = ENTITY_TYPES.register(
            "buoyant_item",
            () -> EntityType.Builder.<BuoyantItemEntity>of(BuoyantItemEntity::new, MobCategory.MISC)
                    .noSummon()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("buoyant_item"));

    public static final DeferredHolder<EntityType<?>, EntityType<GrenadeUniversal>> GRENADE_UNIVERSAL = ENTITY_TYPES.register("grenade_universal",
            () -> EntityType.Builder.<GrenadeUniversal>of(GrenadeUniversal::new, MobCategory.MISC).sized(0.25F, 0.25F).build("grenade_universal"));

    public static final DeferredHolder<EntityType<?>, EntityType<Tom>> TOM = ENTITY_TYPES.register("tom", () -> EntityType.Builder.<Tom>of(Tom::new, MobCategory.MISC).setTrackingRange(1000).build("tom"));

    public static final DeferredHolder<EntityType<?>, EntityType<DeathBlast>> DEATH_BLAST = ENTITY_TYPES.register("death_blast", () -> EntityType.Builder.<DeathBlast>of(DeathBlast::new, MobCategory.MISC).setTrackingRange(1000).build("death_blast"));
    public static final DeferredHolder<EntityType<?>, EntityType<OrbitalLaser>> ORBITAL_LASER = ENTITY_TYPES.register("orbital_laser", () -> EntityType.Builder.<OrbitalLaser>of(OrbitalLaser::new, MobCategory.MISC).setTrackingRange(1000).build("orbital_laser"));

    public static final DeferredHolder<EntityType<?>, EntityType<Bomber>> BOMBER = ENTITY_TYPES.register(
            "bomber",
            () -> EntityType.Builder.<Bomber>of(Bomber::new, MobCategory.MISC)
                    .sized(8F, 4F)
                    .setTrackingRange(250)
                    .build("bomber"));

    public static final DeferredHolder<EntityType<?>, EntityType<MissileMicro>>       MISSILE_MICRO =       ENTITY_TYPES.register("missile_micro",       () -> EntityType.Builder.of(MissileMicro::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_micro"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileSchrabidium>> MISSILE_SCHRABIDIUM = ENTITY_TYPES.register("missile_schrabidium", () -> EntityType.Builder.of(MissileSchrabidium::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_schrabidium"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileBHole>>       MISSILE_BHOLE =       ENTITY_TYPES.register("missile_bhole",       () -> EntityType.Builder.of(MissileBHole::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_bhole"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileTaint>>       MISSILE_TAINT =       ENTITY_TYPES.register("missile_taint",       () -> EntityType.Builder.of(MissileTaint::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_taint"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileEMP>>         MISSILE_EMP =         ENTITY_TYPES.register("missile_emp",         () -> EntityType.Builder.of(MissileEMP::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_emp"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileGeneric>>      MISSILE_GENERIC =    ENTITY_TYPES.register("missile_generic",    () -> EntityType.Builder.of(MissileGeneric::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_generic"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileIncendiary>>   MISSILE_INCENDIARY = ENTITY_TYPES.register("missile_incendiary", () -> EntityType.Builder.of(MissileIncendiary::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_incendiary"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileCluster>>      MISSILE_CLUSTER =    ENTITY_TYPES.register("missile_cluster",    () -> EntityType.Builder.of(MissileCluster::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_cluster"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileBunkerBuster>> MISSILE_BUSTER =     ENTITY_TYPES.register("missile_buster",     () -> EntityType.Builder.of(MissileBunkerBuster::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_bunker_buster"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileDecoy>>        MISSILE_DECOY =      ENTITY_TYPES.register("missile_decoy",      () -> EntityType.Builder.of(MissileDecoy::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_decoy"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileStrong>>           MISSILE_STRONG =            ENTITY_TYPES.register("missile_strong",            () -> EntityType.Builder.of(MissileStrong::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_strong"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileIncendiaryStrong>> MISSILE_INCENDIARY_STRONG = ENTITY_TYPES.register("missile_incendiary_strong", () -> EntityType.Builder.of(MissileIncendiaryStrong::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_incendiary_strong"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileClusterStrong>>    MISSILE_CLUSTER_STRONG =    ENTITY_TYPES.register("missile_cluster_strong",    () -> EntityType.Builder.of(MissileClusterStrong::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_cluster_strong"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileBusterStrong>>     MISSILE_BUSTER_STRONG =     ENTITY_TYPES.register("missile_buster_strong",     () -> EntityType.Builder.of(MissileBusterStrong::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_buster_strong"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileEMPStrong>>        MISSILE_EMP_STRONG =        ENTITY_TYPES.register("missile_emp_strong",        () -> EntityType.Builder.of(MissileEMPStrong::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_emp_strong"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileStealth>>          MISSILE_STEALTH =           ENTITY_TYPES.register("missile_stealth",           () -> EntityType.Builder.of(MissileStealth::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_stealth"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileAntiBallistic>>    MISSILE_ANTI_BALLISTIC =    ENTITY_TYPES.register("missile_anti_ballistic",    () -> EntityType.Builder.of(MissileAntiBallistic::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_anti_ballistic"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileBurst>>   MISSILE_BURST =   ENTITY_TYPES.register("missile_burst",   () -> EntityType.Builder.of(MissileBurst::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_burst"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileInferno>> MISSILE_INFERNO = ENTITY_TYPES.register("missile_inferno", () -> EntityType.Builder.of(MissileInferno::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_inferno"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileRain>>    MISSILE_RAIN =    ENTITY_TYPES.register("missile_rain",    () -> EntityType.Builder.of(MissileRain::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_rain"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileDrill>>   MISSILE_DRILL =   ENTITY_TYPES.register("missile_drill",   () -> EntityType.Builder.of(MissileDrill::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_drill"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileShuttle>> MISSILE_SHUTTLE = ENTITY_TYPES.register("missile_shuttle", () -> EntityType.Builder.of(MissileShuttle::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_shuttle"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileNuclear>>        MISSILE_NUCLEAR =         ENTITY_TYPES.register("missile_nuclear",         () -> EntityType.Builder.of(MissileNuclear::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_nuclear"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileMirv>>           MISSILE_NUCLEAR_CLUSTER = ENTITY_TYPES.register("missile_nulcear_cluster", () -> EntityType.Builder.of(MissileMirv::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_nulcear_cluster"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileVolcano>>        MISSILE_VOLCANO =         ENTITY_TYPES.register("missile_volcano",         () -> EntityType.Builder.of(MissileVolcano::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_volcano"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileDoomsday>>       MISSILE_DOOMSDAY =        ENTITY_TYPES.register("missile_doomsday",        () -> EntityType.Builder.of(MissileDoomsday::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_doomsday"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileDoomsdayRusted>> MISSILE_DOOMSDAY_RUSTED = ENTITY_TYPES.register("missile_doomsday_rusted", () -> EntityType.Builder.of(MissileDoomsdayRusted::new, MobCategory.MISC).sized(1.5F, 1.5F).setTrackingRange(500).build("missile_doomsday_rusted"));

    public static final DeferredHolder<EntityType<?>, EntityType<BombletZeta>> BOMBLET_ZETA = ENTITY_TYPES.register(
            "bomblet_zeta",
            () -> EntityType.Builder.of(BombletZeta::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .setTrackingRange(250)
                    .build("bomblet_zeta"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlackHole>> BLACK_HOLE = ENTITY_TYPES.register(
            "black_hole",
            () -> EntityType.Builder.of(BlackHole::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .setTrackingRange(250)
                    .fireImmune()
                    .build("black_hole"));
    public static final DeferredHolder<EntityType<?>, EntityType<Vortex>> VORTEX = ENTITY_TYPES.register(
            "vortex",
            () -> EntityType.Builder.of(Vortex::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .setTrackingRange(250)
                    .fireImmune()
                    .build("vortex"));
    public static final DeferredHolder<EntityType<?>, EntityType<RagingVortex>> RAGING_VORTEX = ENTITY_TYPES.register(
            "raging_vortex",
            () -> EntityType.Builder.of(RagingVortex::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .setTrackingRange(250)
                    .fireImmune()
                    .build("raging_vortex"));
    public static final DeferredHolder<EntityType<?>, EntityType<BlackHole>> DIGAMMA_QUASAR = ENTITY_TYPES.register(
            "digamma_quasar",
            () -> EntityType.Builder.of(BlackHole::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .setTrackingRange(250)
                    .fireImmune()
                    .build("digamma_quasar"));

    public static final DeferredHolder<EntityType<?>, EntityType<Meteor>> METEOR = ENTITY_TYPES.register(
            "meteor",
            () -> EntityType.Builder.of(Meteor::new, MobCategory.MISC)
                    .sized(4F, 4F)
                    .build("meteor"));

    public static final DeferredHolder<EntityType<?>, EntityType<EMP>> EMP = ENTITY_TYPES.register("emp", () -> EntityType.Builder.of(EMP::new, MobCategory.MISC).build("emp"));

    public static final DeferredHolder<EntityType<?>, EntityType<Soyuz>> SOYUZ_MISSILE = ENTITY_TYPES.register("soyuz", () -> EntityType.Builder.<Soyuz>of(Soyuz::new, MobCategory.MISC).noSummon().sized(5.0F, 50.0F).build("soyuz"));

    /**
     * Die Landekapsel, Runde 278. Sie kommt am Fallschirm herunter und setzt sich als Block
     * ab. Feuerfest wie im Original (isImmuneToFire), und so gross wie das Modell, das sie
     * zeigt.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<SoyuzCapsule>> SOYUZ_CAPSULE = ENTITY_TYPES.register("soyuz_capsule",
            () -> EntityType.Builder.<SoyuzCapsule>of(SoyuzCapsule::new, MobCategory.MISC).noSummon().fireImmune().sized(2.0F, 2.0F).build("soyuz_capsule"));

    /**
     * Die Eigenbau-Rakete, Runde 132. Sie traegt ihre vier Bauteile mit sich; wie gross sie
     * wirklich ist, haengt am Rumpf. Der Sichtbereich ist der der anderen Raketen.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<MissileCustom>> MISSILE_CUSTOM = ENTITY_TYPES.register(
            "missile_custom",
            () -> EntityType.Builder.<MissileCustom>of(MissileCustom::new, MobCategory.MISC)
                    .noSummon()
                    .sized(1.5F, 1.5F)
                    .setTrackingRange(250)
                    .fireImmune()
                    .build("missile_custom"));

    /** Die Abwurfkapsel der Satellitenstation. Sie faellt aus dreihundert Bloecken -- entsprechend weit muss sie verfolgt werden. */
    public static final DeferredHolder<EntityType<?>, EntityType<SatellitePod>> SATELLITE_POD = ENTITY_TYPES.register(
            "satellite_pod",
            () -> EntityType.Builder.<SatellitePod>of(SatellitePod::new, MobCategory.MISC)
                    .noSummon()
                    .sized(2.0F, 3.0F)
                    .setTrackingRange(250)
                    .fireImmune()
                    .build("satellite_pod"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
