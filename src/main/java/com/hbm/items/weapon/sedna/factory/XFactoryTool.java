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
import com.hbm.blockentity.IRepairable;
import com.hbm.blockentity.IRepairable.EnumExtinguishType;
import com.hbm.blocks.generic.LayeringBlock;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.factory.GunFactory.AmmoFireExt;
import com.hbm.util.CompatExternal;
import com.hbm.util.particle.ParticleUtil;
import com.hbm.items.weapon.sedna.impl.GunChargeThrowerItem;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryTool.
 *
 * ZWEI WERKZEUGE, KEINE WAFFEN: der Feuerloescher und der Ladungswerfer. Beide tragen die
 * Guete UTILITY, beide machen kaum Schaden, und beide sind nur so gut wie das, was sie in der
 * Welt hinterlassen.
 *
 * DER FEUERLOESCHER verschiesst Wasser, Schaum und Borsand. Wasser loescht im Umkreis und
 * spuelt Schaum weg; Schaum und Sand bleiben liegen und wachsen Lage um Lage, bis aus der
 * Schicht der volle Block wird. Trifft einer der drei eine Maschine, die sich loeschen laesst,
 * meldet er ihr seine Sorte -- das ist die einzige Stelle im Port, die IRepairable.tryExtinguish
 * je ausloest.
 *
 * DER LADUNGSWERFER IST DREI WAFFEN IN EINER, je nachdem was geladen ist: ein Enterhaken zum
 * Fortbewegen, eine kleine Moerserladung und eine grosse. Die Munition wechselt man nur ueber
 * das Nachladen (reloadChangeType), nicht im Flug.
 *
 * Der Enterhaken bleibt in der Wand stecken -- dafuer hat BulletBaseMK4 seit dieser Runde
 * einen Steckzustand. Was daran haengt, steht in GunChargeThrowerItem.
 */
public class XFactoryTool {

    public static BulletConfig fext_water;
    public static BulletConfig fext_foam;
    public static BulletConfig fext_sand;

    /** Alle drei Fuellungen loeschen ein brennendes Lebewesen, das sie treffen. */
    public static BiConsumer<BulletBaseMK4, EntityHitResult> LAMBDA_EXT_ENTITY = (geschoss, treffer) -> {
        if(treffer.getEntity() != null) treffer.getEntity().clearFire();
    };

    /**
     * Wasser. Es loescht in einem Wuerfel von drei Bloecken Kantenlaenge alles Feuer und
     * spuelt dabei auch Schaum weg -- beides, die Schicht und den vollen Block.
     *
     * Trifft es eine Maschine, die sich loeschen laesst, meldet es ihr das. Das ist die
     * einzige Stelle im Port, die EnumExtinguishType.WATER je ausloest.
     */
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_WATER_HIT = (geschoss, treffer) -> {

        if(geschoss.level.isClientSide) return;

        BlockPos ort = treffer.getBlockPos();
        boolean zischt = false;

        for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) for(int k = -1; k <= 1; k++) {
            BlockPos nachbar = ort.offset(i, j, k);
            Block block = geschoss.level.getBlockState(nachbar).getBlock();
            if(block instanceof BaseFireBlock || block == NtmBlocks.FOAM_LAYER.get() || block == NtmBlocks.BLOCK_FOAM.get()) {
                geschoss.level.setBlock(nachbar, Blocks.AIR.defaultBlockState(), 3);
                zischt = true;
            }
        }

        BlockEntity kern = CompatExternal.getCoreFromPos(geschoss.level, ort);
        if(kern instanceof IRepairable reparierbar) reparierbar.tryExtinguish(geschoss.level, ort, EnumExtinguishType.WATER);

        if(zischt) zischen(geschoss);
        geschoss.discard();
    };

    /**
     * Schaum. Er loescht nur, was er unmittelbar trifft, bleibt dafuer aber liegen: jeder
     * Schuss haeuft eine Lage auf, und auf die siebte folgt der volle Schaumblock.
     *
     * Die Muenzwurf-Zeile ist aus dem Original uebernommen: in der Haelfte der Faelle legt
     * sich der Schaum nicht in den getroffenen Block, sondern davor. Ohne sie bliebe an einer
     * Wand nie etwas haengen.
     */
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_FOAM_HIT = (geschoss, treffer) -> {

        if(geschoss.level.isClientSide) return;

        BlockPos ort = treffer.getBlockPos();
        boolean zischt = false;

        for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) for(int k = -1; k <= 1; k++) {
            BlockPos nachbar = ort.offset(i, j, k);
            if(geschoss.level.getBlockState(nachbar).getBlock() instanceof BaseFireBlock) {
                geschoss.level.setBlock(nachbar, Blocks.AIR.defaultBlockState(), 3);
                zischt = true;
            }
        }

        BlockEntity kern = CompatExternal.getCoreFromPos(geschoss.level, ort);
        if(kern instanceof IRepairable reparierbar) {
            reparierbar.tryExtinguish(geschoss.level, ort, EnumExtinguishType.FOAM);
            return;
        }

        if(geschoss.level.random.nextBoolean()) ort = ort.relative(treffer.getDirection());

        schichten(geschoss.level, ort, NtmBlocks.FOAM_LAYER.get(), NtmBlocks.BLOCK_FOAM.get());
        if(zischt) zischen(geschoss);
    };

    /**
     * Borsand. Wie der Schaum, nur dass er kein Feuer im Umkreis loescht -- er erstickt es
     * dort, wo er liegenbleibt. Aus der vollen Schicht wird SAND_BORON.
     */
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_SAND_HIT = (geschoss, treffer) -> {

        if(geschoss.level.isClientSide) return;

        BlockPos ort = treffer.getBlockPos();

        BlockEntity kern = CompatExternal.getCoreFromPos(geschoss.level, ort);
        if(kern instanceof IRepairable reparierbar) {
            reparierbar.tryExtinguish(geschoss.level, ort, EnumExtinguishType.SAND);
            return;
        }

        if(geschoss.level.random.nextBoolean()) ort = ort.relative(treffer.getDirection());

        boolean warFeuer = geschoss.level.getBlockState(ort).getBlock() instanceof BaseFireBlock;
        if(schichten(geschoss.level, ort, NtmBlocks.SAND_BORON_LAYER.get(), NtmBlocks.SAND_BORON.get()) && warFeuer) zischen(geschoss);
    };

    /**
     * Eine Lage auflegen. Liegt dort noch nichts von dieser Sorte, faengt die Schicht bei
     * eins an; sonst waechst sie. DIE SIEBTE LAGE IST DIE LETZTE -- der naechste Schuss
     * macht daraus den vollen Block. Das entspricht dem Original, wo die Metadaten von 0
     * bis 6 laufen und der Block bei 6 umschlaegt.
     *
     * Die achte Lage, die LayeringBlock zulaesst, erreicht der Loescher also nie; von Hand
     * gesetzt gibt es sie sehr wohl, und dann schlaegt der naechste Schuss sie ebenfalls um.
     *
     * Rueckgabe: ob ueberhaupt etwas gesetzt wurde.
     */
    private static boolean schichten(Level welt, BlockPos ort, Block schicht, Block voll) {

        BlockState zustand = welt.getBlockState(ort);

        if(zustand.is(schicht)) {
            int lagen = zustand.getValue(LayeringBlock.LAYERS);
            if(lagen < 7) welt.setBlock(ort, zustand.setValue(LayeringBlock.LAYERS, lagen + 1), 3);
            else welt.setBlock(ort, voll.defaultBlockState(), 3);
            return true;
        }

        if(!zustand.canBeReplaced()) return false;

        BlockState neu = schicht.defaultBlockState();
        if(!neu.canSurvive(welt, ort)) return false;

        welt.setBlock(ort, neu, 3);
        return true;
    }

    /** Das Zischen, wenn Feuer ausgeht. Im Original random.fizz. */
    private static void zischen(BulletBaseMK4 geschoss) {
        geschoss.level.playSound(null, geschoss.getX(), geschoss.getY(), geschoss.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.5F + geschoss.level.random.nextFloat() * 0.5F);
    }

    /**
     * Die Spur des Strahls. Das Original schickt dafuer ein Partikelpaket vom Server; hier
     * reicht der Client sich selbst, weil onUpdate auf beiden Seiten laeuft.
     *
     * ABWEICHUNG: das Original verwandelt vulkanische Lava, die der Wasserstrahl trifft, in
     * Obsidian. Das ist hier weggelassen -- der Zweig braucht den Metadatenwert 0 des
     * Blocks, und volcanic_lava ist im Port ein Fluessigkeitsblock ohne diese Unterscheidung.
     * Eine Zeile zu schreiben, die auf einen Zustand prueft, den es nicht gibt, waere ein
     * Ast, den nichts erreicht.
     */
    private static Consumer<Entity> spur(Supplier<BlockState> zustand) {
        return (geschoss) -> {
            if(!geschoss.level.isClientSide) return;
            RandomSource zufall = geschoss.level.random;
            Vec3 fahrt = geschoss.getDeltaMovement();
            ParticleUtil.addParticle(geschoss.level, new BlockParticleOption(ParticleTypes.BLOCK, zustand.get()),
                    geschoss.getX(), geschoss.getY(), geschoss.getZ(),
                    (float) (fahrt.x + zufall.nextGaussian() * 0.1),
                    (float) (fahrt.y - 0.2 + zufall.nextGaussian() * 0.1),
                    (float) (fahrt.z + zufall.nextGaussian() * 0.1));
        };
    }

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
        /*
         * 300 Schuss im Tank, und keiner davon macht Schaden. Die Reichweite ist kurz
         * (100 Zuege Lebensdauer bei 0,75 Geschwindigkeit und deutlicher Schwerkraft) --
         * ein Loescher ist kein Gewehr.
         */
        fext_water = new BulletConfig().setItem(AmmoFireExt.WATER).setReloadCount(300)
                .setLife(100).setVel(0.75F).setGrav(0.04D).setSpread(0.025F)
                .setOnUpdate(spur(() -> Blocks.WATER.defaultBlockState()))
                .setOnEntityHit(LAMBDA_EXT_ENTITY).setOnRicochet(LAMBDA_WATER_HIT);
        fext_foam = new BulletConfig().setItem(AmmoFireExt.FOAM).setReloadCount(300)
                .setLife(100).setVel(0.75F).setGrav(0.04D).setSpread(0.05F)
                .setOnUpdate(spur(() -> NtmBlocks.BLOCK_FOAM.get().defaultBlockState()))
                .setOnEntityHit(LAMBDA_EXT_ENTITY).setOnRicochet(LAMBDA_FOAM_HIT);
        fext_sand = new BulletConfig().setItem(AmmoFireExt.SAND).setReloadCount(300)
                .setLife(100).setVel(0.75F).setGrav(0.04D).setSpread(0.05F)
                .setOnUpdate(spur(() -> NtmBlocks.SAND_BORON.get().defaultBlockState()))
                .setOnEntityHit(LAMBDA_EXT_ENTITY).setOnRicochet(LAMBDA_SAND_HIT);

        ct_hook = new BulletConfig().setItem(Ammo.CT_HOOK).setRenderRotations(false)
                .setLife(6_000).setVel(3F).setGrav(0.035D).setDoesPenetrate(true).setDamageFalloffByPen(false)
                .setOnUpdate(LAMBDA_SET_HOOK).setOnImpact(LAMBDA_HOOK);
        ct_mortar = new BulletConfig().setItem(Ammo.CT_MORTAR).setDamage(2.5F).setLife(200).setVel(3F).setGrav(0.035D)
                .setOnImpact(LAMBDA_MORTAR);
        ct_mortar_charge = new BulletConfig().setItem(Ammo.CT_MORTAR_CHARGE).setDamage(5F).setLife(200).setVel(3F).setGrav(0.035D)
                .setOnImpact(LAMBDA_MORTAR_CHARGE);

        /*
         * Kein eigener Bewegungssatz: das Original gibt dem Loescher keinen, und er braucht
         * auch keinen -- der Tank bewegt sich beim Schiessen nicht. Was man hoert, macht die
         * ORCHESTRA_FIREEXT: ein Ventil beim Wechsel des Tanks.
         */
        NtmItems.GUN_FIREEXT = registry.register("gun_fireext", () -> new GunBaseNTItem(WeaponQuality.UTILITY, new GunConfig()
                .dura(5_000).draw(10).inspect(55).reloadChangeType(true).hideCrosshair(false).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(0F).delay(1).dry(0).auto(true).spread(0F).spreadHipfire(0F).reload(20).jam(0)
                        .sound(NtmSoundEvents.GUN_EXTINGUISHER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 300).addConfigs(fext_water, fext_foam, fext_sand))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .orchestra(Orchestras.ORCHESTRA_FIREEXT)
        ));

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
