package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.impl.GunChemthrowerItem;
import com.hbm.items.weapon.sedna.mags.MagazineFluid;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.main.ResourceManager;
import com.hbm.particle.helper.FlameCreator;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryFlamer.
 *
 * Die drei Flammenwerfer. Die vier Brennstoffe -- Diesel, Gas, Napalm, Bannfeuer -- lagen als
 * Munition laengst im Port, ohne dass eine einzige Waffe sie verschiessen konnte.
 *
 * WIE EIN FLAMMENWERFER IN DIESEM SYSTEM ARBEITET: er verschiesst gewoehnliche Geschosse, nur
 * sehr viele, sehr langsame und sehr kurzlebige. Jedes zieht auf seinem Weg eine Flamme hinter
 * sich her (onUpdate), zuendet an, was es trifft (onImpact), und laesst dort, wo es auf einen
 * Block schlaegt, eine Lache stehenden Feuers zurueck (onRicochet).
 *
 * DER CHEMIEWERFER gehoert im Original in dieselbe Fabrik und steht seit Runde 188 auch hier.
 * Er schiesst keine Geschosse, sondern den Inhalt seines Tanks; was dabei herauskommt,
 * entscheidet das Fluid.
 *
 * DER DAYBREAKER BLEIBT VORERST OHNE QUELLE. Im Original kommt er aus dem Sockel
 * (PedestalRecipes); den gibt es im Port nicht, und damit auch keinen Weg zu ihm ausser dem
 * Kreativreiter. Das ist kein Versehen dieser Runde, sondern derselbe Stand wie beim Lilmac
 * und beim Protege: die Waffe ist fertig, ihr Fundort fehlt.
 */
public class XFactoryFlamer {

    public static BulletConfig flame_diesel;
    public static BulletConfig flame_gas;
    public static BulletConfig flame_napalm;
    public static BulletConfig flame_balefire;

    public static BulletConfig flame_topaz_diesel;
    public static BulletConfig flame_topaz_gas;
    public static BulletConfig flame_topaz_napalm;
    public static BulletConfig flame_topaz_balefire;

    public static BulletConfig flame_daybreaker_diesel;
    public static BulletConfig flame_daybreaker_gas;
    public static BulletConfig flame_daybreaker_napalm;
    public static BulletConfig flame_daybreaker_balefire;

    /**
     * Die Flamme am Geschoss. Sie entsteht nur auf dem Rechner des Zuschauers.
     *
     * ABWEICHUNG: das Original prueft zusaetzlich, ob der Zuschauer naeher als hundert Bloecke
     * ist. Dafuer muesste hier die Minecraft-Klasse angefasst werden, und die gibt es auf dem
     * Server nicht. Der Port haelt es wie FireLingering, das denselben Aufruf ohne Abstands-
     * pruefung macht: die Partikelanlage wirft ohnehin weg, was ausser Sicht liegt.
     */
    public static Consumer<Entity> LAMBDA_FIRE = (geschoss) -> zeichneFlamme(geschoss, FlameCreator.META_FIRE);
    public static Consumer<Entity> LAMBDA_BALEFIRE = (geschoss) -> zeichneFlamme(geschoss, FlameCreator.META_BALEFIRE);

    private static void zeichneFlamme(Entity geschoss, int meta) {
        if(!geschoss.level.isClientSide) return;
        FlameCreator.composeEffectClient(geschoss.getX(), geschoss.getY() - 0.125D, geschoss.getZ(), meta);
    }

    /** Wen die Flamme trifft, der brennt -- fuenf Sekunden, oder laenger, wenn er schon brennt. */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_IGNITE_FIRE = (geschoss, treffer) -> {
        LivingEntity getroffen = getroffenesWesen(treffer);
        if(getroffen == null) return;
        HbmLivingAttachments props = HbmLivingAttachments.getData(getroffen);
        if(props.fire < 100) props.fire = 100;
    };

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_IGNITE_BALEFIRE = (geschoss, treffer) -> {
        LivingEntity getroffen = getroffenesWesen(treffer);
        if(getroffen == null) return;
        HbmLivingAttachments props = HbmLivingAttachments.getData(getroffen);
        if(props.balefire < 200) props.balefire = 200;
    };

    private static LivingEntity getroffenesWesen(HitResult treffer) {
        if(treffer instanceof EntityHitResult wesenTreffer && wesenTreffer.getEntity() instanceof LivingEntity lebendig) return lebendig;
        return null;
    }

    /**
     * Was am Block passiert. Der Reihe nach: erst versuchen, ihn anzuzuenden; klappt das nicht,
     * bleibt je nach Brennstoff eine Lache stehenden Feuers zurueck. Gas laesst nichts stehen,
     * Bannfeuer brennt immer.
     */
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_LINGER_DIESEL = (geschoss, treffer) -> {
        if(!zuendeAn(geschoss, treffer)) setzeFeuer(geschoss, treffer, 2F, 1F, 100, FireLingering.TYPE_DIESEL);
    };
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_LINGER_GAS = (geschoss, treffer) -> {
        zuendeAn(geschoss, treffer);
    };
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_LINGER_NAPALM = (geschoss, treffer) -> {
        if(!zuendeAn(geschoss, treffer)) setzeFeuer(geschoss, treffer, 2.5F, 1F, 200, FireLingering.TYPE_DIESEL);
    };
    public static BiConsumer<BulletBaseMK4, BlockHitResult> LAMBDA_LINGER_BALEFIRE = (geschoss, treffer) -> {
        setzeFeuer(geschoss, treffer, 3F, 1F, 300, FireLingering.TYPE_BALEFIRE);
    };

    /**
     * Zuendet den getroffenen Block an, wenn er brennbar ist und vor ihm Luft steht. Gelingt
     * das, ist das Geschoss verbraucht; gelingt es nicht, verschwindet es trotzdem -- es ist
     * an einer Wand zerplatzt.
     */
    public static boolean zuendeAn(BulletBaseMK4 geschoss, BlockHitResult treffer) {

        Level level = geschoss.level;
        BlockPos stelle = treffer.getBlockPos();
        Direction seite = treffer.getDirection();
        BlockState zustand = level.getBlockState(stelle);

        if(zustand.isFlammable(level, stelle, seite.getOpposite())) {
            BlockPos davor = stelle.relative(seite);
            if(level.getBlockState(davor).isAir()) {
                level.setBlockAndUpdate(davor, Blocks.FIRE.defaultBlockState());
                return true;
            }
        }

        geschoss.discard();
        return false;
    }

    /**
     * Stellt eine Lache stehenden Feuers an die Trefferstelle -- aber nur, wenn dort nicht
     * schon eine steht. Sonst legt ein Flammenwerfer in einer Sekunde zwanzig davon uebereinander.
     */
    public static void setzeFeuer(BulletBaseMK4 geschoss, BlockHitResult treffer, float breite, float hoehe, int dauer, int art) {

        Vec3 stelle = treffer.getLocation();
        Level level = geschoss.level;

        List<FireLingering> vorhanden = level.getEntitiesOfClass(FireLingering.class,
                new AABB(stelle, stelle).inflate(breite / 2 + 0.5, hoehe / 2 + 0.5, breite / 2 + 0.5));

        if(vorhanden.isEmpty()) {
            FireLingering feuer = new FireLingering(level).setArea(breite, hoehe).setDuration(dauer).setFireType(art);
            feuer.setPos(stelle);
            level.addFreshEntity(feuer);
        }

        geschoss.discard();
    }

    public static void init(DeferredRegister.Items registry) {

        /*
         * Die vier Brennstoffe. Alle vier haben dieselbe Bauart: kaum Schaden je Geschoss,
         * dafuer sehr viele; der Rueckstoss ist null, damit der Strahl steht.
         *
         * setSelfDamageDelay(20) ist hier keine Feinheit, sondern notwendig: die Geschosse
         * entstehen eine Handbreit vor dem Schuetzen und wuerden ihn ohne die Sperre sofort
         * selbst anzuenden.
         */
        flame_diesel = new BulletConfig().setItem(Ammo.FLAME_DIESEL).setCasing(() -> new ItemStack(NtmItems.PLATE_STEEL.get(), 2), 500)
                .setupDamageClass(DamageClass.FIRE).setLife(100).setVel(1F).setGrav(0.02D).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnImpact(LAMBDA_IGNITE_FIRE).setOnUpdate(LAMBDA_FIRE).setOnRicochet(LAMBDA_LINGER_DIESEL);
        flame_gas = new BulletConfig().setItem(Ammo.FLAME_GAS).setCasing(() -> new ItemStack(NtmItems.PLATE_STEEL.get(), 2), 500)
                .setupDamageClass(DamageClass.FIRE).setLife(10).setSpread(0.05F).setVel(1F).setGrav(0.0D).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnImpact(LAMBDA_IGNITE_FIRE).setOnUpdate(LAMBDA_FIRE).setOnRicochet(LAMBDA_LINGER_GAS);
        flame_napalm = new BulletConfig().setItem(Ammo.FLAME_NAPALM).setCasing(() -> new ItemStack(NtmItems.PLATE_STEEL.get(), 2), 500)
                .setupDamageClass(DamageClass.FIRE).setLife(200).setVel(1F).setGrav(0.02D).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnImpact(LAMBDA_IGNITE_FIRE).setOnUpdate(LAMBDA_FIRE).setOnRicochet(LAMBDA_LINGER_NAPALM);
        flame_balefire = new BulletConfig().setItem(Ammo.FLAME_BALEFIRE).setCasing(() -> new ItemStack(NtmItems.PLATE_STEEL.get(), 2), 500)
                .setupDamageClass(DamageClass.FIRE).setLife(200).setVel(1F).setGrav(0.02D).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnImpact(LAMBDA_IGNITE_BALEFIRE).setOnUpdate(LAMBDA_BALEFIRE).setOnRicochet(LAMBDA_LINGER_BALEFIRE);

        /*
         * NICHT UEBERNOMMEN: flame_nograv und flame_nograv_bf. Das Original legt die beiden
         * schwerelosen Spielarten hier an, benutzt sie in dieser Fabrik aber nicht -- sie
         * gehoeren zum Chemiewerfer und zum Bannfeuerwerfer der Panzerruestung. Beide sind
         * nicht portiert; die Konfigurationen kommen mit ihnen.
         */

        /* Der Topaz schiesst je Zug zwei Geschosse mit Streuung, dafuer kuerzer und gerade. */
        flame_topaz_diesel = flame_diesel.clone().setProjectiles(2).setSpread(0.05F).setLife(60).setGrav(0.0D);
        flame_topaz_gas = flame_gas.clone().setProjectiles(2).setSpread(0.05F);
        flame_topaz_napalm = flame_napalm.clone().setProjectiles(2).setSpread(0.05F).setLife(60).setGrav(0.0D);
        flame_topaz_balefire = flame_balefire.clone().setProjectiles(2).setSpread(0.05F).setLife(60).setGrav(0.0D);

        /*
         * Der Daybreaker wirft keine Flamme, er wirft Brandbomben: doppelt so schnell, weit
         * fliegend, und jede einzelne schlaegt mit einer Explosion und einer grossen Feuerlache
         * ein.
         */
        flame_daybreaker_diesel = flame_diesel.clone().setLife(200).setVel(2F).setGrav(0.035D)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 5F); setzeFeuerBeiBlock(geschoss, treffer, 6F, 2F, 200, FireLingering.TYPE_DIESEL); geschoss.discard(); });
        flame_daybreaker_gas = flame_gas.clone().setLife(200).setVel(2F).setGrav(0.035D)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 5F); geschoss.discard(); });
        flame_daybreaker_napalm = flame_napalm.clone().setLife(200).setVel(2F).setGrav(0.035D)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 7.5F); setzeFeuerBeiBlock(geschoss, treffer, 6F, 2F, 300, FireLingering.TYPE_DIESEL); geschoss.discard(); });
        flame_daybreaker_balefire = flame_balefire.clone().setLife(200).setVel(2F).setGrav(0.035D)
                .setOnImpact((geschoss, treffer) -> { Lego.standardExplode(geschoss, treffer, 5F); setzeFeuerBeiBlock(geschoss, treffer, 7.5F, 2.5F, 400, FireLingering.TYPE_BALEFIRE); geschoss.discard(); });

        NtmItems.GUN_FLAMER = registry.register("gun_flamer", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(20_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(1F).spreadHipfire(0F).delay(1).auto(true).reload(90).jam(17)
                        .mag(new MagazineFullReload(0, 300).addConfigs(flame_diesel, flame_gas, flame_napalm, flame_balefire))
                        .offset(0.75, -0.0625, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAMER_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAMER)
        ).setDefaultAmmo(Ammo.FLAME_DIESEL, 1));

        NtmItems.GUN_FLAMER_TOPAZ = registry.register("gun_flamer_topaz", () -> new GunBaseNTItem(WeaponQuality.B_SIDE, new GunConfig()
                .dura(20_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(1.5F).spreadHipfire(0F).delay(1).auto(true).reload(90).jam(17)
                        .mag(new MagazineFullReload(0, 500).addConfigs(flame_topaz_diesel, flame_topaz_gas, flame_topaz_napalm, flame_topaz_balefire))
                        .offset(0.75, -0.0625, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAMER_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAMER)
        ).setDefaultAmmo(Ammo.FLAME_DIESEL, 1));

        NtmItems.GUN_FLAMER_DAYBREAKER = registry.register("gun_flamer_daybreaker", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(20_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(25F).spreadHipfire(0F).delay(10).auto(true).reload(90).jam(17).sound(NtmSoundEvents.GUN_POWDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 50).addConfigs(flame_daybreaker_diesel, flame_daybreaker_gas, flame_daybreaker_napalm, flame_daybreaker_balefire))
                        .offset(0.75, -0.0625, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAMER_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAMER_DAYBREAKER)
        ).setDefaultAmmo(Ammo.FLAME_DIESEL, 1));

        /*
         * Der Chemiewerfer. Kein Magazin mit Patronen, sondern ein Tank mit drei Litern; was
         * er verschiesst, haengt daran, was drin ist. Er hat deshalb auch kein Nachladen --
         * pr und reload fehlen im Original ebenso.
         */
        NtmItems.GUN_CHEMTHROWER = registry.register("gun_chemthrower", () -> new GunChemthrowerItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(90_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .delay(1).spreadHipfire(0F).auto(true)
                        .mag(new MagazineFluid(0, 3_000))
                        .offset(0.75, -0.0625, -0.25D)
                        .canFire(GunChemthrowerItem.LAMBDA_CAN_FIRE).fire(GunChemthrowerItem.LAMBDA_FIRE))
                .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                .anim(LAMBDA_CHEMTHROWER_ANIMS).orchestra(Orchestras.ORCHESTRA_CHEMTHROWER)
        ));
    }

    /** Der Chemiewerfer hat nur eine einzige Bewegung: das Anlegen. */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_CHEMTHROWER_ANIMS = (stack, type) -> {
        if(type == GunAnimation.EQUIP) return new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        return null;
    };

    /** Dieselbe Lache, aber aus einem Treffer, der auch ein Wesen gewesen sein kann. */
    private static void setzeFeuerBeiBlock(BulletBaseMK4 geschoss, HitResult treffer, float breite, float hoehe, int dauer, int art) {
        if(treffer instanceof BlockHitResult blockTreffer) setzeFeuer(geschoss, blockTreffer, breite, hoehe, dauer, art);
    }

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_FLAMER_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case RELOAD: return ResourceManager.flamethrower_anim.get("Reload");
            case INSPECT:
            case JAMMED: return new BusAnimation()
                    .addBus("ROTATE", new BusAnimationSequence().addPos(0, 0, 45, 250, IType.SIN_FULL).addPos(0, 0, 45, 350).addPos(0, 0, -15, 150, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL));
            default: return null;
        }
    };
}
