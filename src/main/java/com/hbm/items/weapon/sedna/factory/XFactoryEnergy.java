package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.projectile.BulletBeamBase;
import com.hbm.extprop.HbmLivingAttachments;
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
import com.hbm.main.ResourceManager;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryEnergy.
 *
 * Die Laserwaffen. Alle vier schiessen Strahlen statt Geschossen -- dasselbe Hitscan-System,
 * das die 35800 und die NI4NI benutzen und das seit Runde 183 ueberhaupt erst funktioniert.
 *
 * DIE DREI KONDENSATOREN SIND DIE MUNITION, und sie lagen wie die Flammenwerferbrennstoffe
 * schon im Port, ohne dass eine Waffe sie verschiessen konnte.
 *
 * NICHT IN DIESER RUNDE: die Teslakanone aus derselben Fabrik des Originals. Ihr Einschlag ist
 * eine Explosion mit eigenem Spielerverarbeiter (PlayerProcessorStandard) und einem
 * Partikelpaket, die beide im Port fehlen; dazu kommt ihr Gurtmagazin. Das ist eine Stufe fuer
 * sich, und die drei Kondensatorarten stehen hier schon bereit.
 */
public class XFactoryEnergy {

    public static BulletConfig energy_las;
    public static BulletConfig energy_las_overcharge;
    public static BulletConfig energy_las_ir;
    public static BulletConfig energy_emerald;
    public static BulletConfig energy_emerald_overcharge;
    public static BulletConfig energy_emerald_ir;

    /**
     * Der Brandstrahl. Er macht denselben Schaden wie der gewoehnliche und zuendet zusaetzlich
     * an, was er trifft: ein Wesen brennt fuenf Sekunden, ein brennbarer Block faengt Feuer,
     * und wo beides nicht geht, bleibt eine Lache stehenden Feuers.
     */
    public static BiConsumer<BulletBeamBase, HitResult> LAMBDA_IR_HIT = (strahl, treffer) -> {

        BulletConfig.LAMBDA_STANDARD_BEAM_HIT.accept(strahl, treffer);

        if(treffer instanceof EntityHitResult wesenTreffer && wesenTreffer.getEntity() instanceof LivingEntity lebendig) {
            HbmLivingAttachments props = HbmLivingAttachments.getData(lebendig);
            if(props.fire < 100) props.fire = 100;
            return;
        }

        if(!(treffer instanceof BlockHitResult blockTreffer)) return;

        Level level = strahl.level;
        BlockPos stelle = blockTreffer.getBlockPos();
        Direction seite = blockTreffer.getDirection();
        BlockState zustand = level.getBlockState(stelle);

        if(zustand.isFlammable(level, stelle, seite.getOpposite())) {
            BlockPos davor = stelle.relative(seite);
            if(level.getBlockState(davor).isAir()) {
                level.setBlockAndUpdate(davor, Blocks.FIRE.defaultBlockState());
                return;
            }
        }

        Vec3 ort = blockTreffer.getLocation();
        FireLingering feuer = new FireLingering(level).setArea(2F, 1F).setDuration(100).setFireType(FireLingering.TYPE_DIESEL);
        feuer.setPos(ort);
        level.addFreshEntity(feuer);
    };

    public static void init(DeferredRegister.Items registry) {

        /*
         * Die drei Strahlarten. Der ueberladene durchschlaegt, der Brandstrahl zaehlt als
         * Feuerschaden statt als Laserschaden -- deshalb der andere DamageClass.
         */
        energy_las = new BulletConfig().setItem(Ammo.CAPACITOR).setCasing(() -> new ItemStack(NtmItems.INGOT_POLYMER.get(), 2), 4)
                .setupDamageClass(DamageClass.LASER).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false)
                .setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);
        energy_las_overcharge = new BulletConfig().setItem(Ammo.CAPACITOR_OVERCHARGE).setCasing(() -> new ItemStack(NtmItems.INGOT_POLYMER.get(), 2), 4)
                .setupDamageClass(DamageClass.LASER).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setDoesPenetrate(true)
                .setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);
        energy_las_ir = new BulletConfig().setItem(Ammo.CAPACITOR_IR).setCasing(() -> new ItemStack(NtmItems.INGOT_POLYMER.get(), 2), 4)
                .setupDamageClass(DamageClass.FIRE).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false)
                .setOnBeamImpact(LAMBDA_IR_HIT);

        /* Dieselben drei mit dem Smaragd der Morning Glory: halber Ruestungswert, zehn Punkte weniger Schwelle. */
        energy_emerald = energy_las.clone().setArmorPiercing(0.5F).setThresholdNegation(10F);
        energy_emerald_overcharge = energy_las_overcharge.clone().setArmorPiercing(0.5F).setThresholdNegation(15F);
        energy_emerald_ir = energy_las_ir.clone().setArmorPiercing(0.5F).setThresholdNegation(10F);

        NtmItems.GUN_LASER_PISTOL = registry.register("gun_laser_pistol", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(500).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(25F).delay(5).spread(1F).spreadHipfire(1F).reload(45).jam(37).sound(NtmSoundEvents.GUN_LASER_PISTOL_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(energy_las, energy_las_overcharge, energy_las_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASER_PISTOL).orchestra(Orchestras.ORCHESTRA_LASER_PISTOL)
        ).setDefaultAmmo(Ammo.CAPACITOR, 15));

        NtmItems.GUN_LASER_PISTOL_PEW_PEW = registry.register("gun_laser_pistol_pew_pew", () -> new GunBaseNTItem(WeaponQuality.B_SIDE, new GunConfig()
                .dura(500).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(30F).rounds(5).delay(10).spread(0.25F).spreadHipfire(1F).reload(45).jam(37).sound(NtmSoundEvents.GUN_LASER_PISTOL_FIRE, 1.0F, 0.8F)
                        .mag(new MagazineFullReload(0, 10).addConfigs(energy_las, energy_las_overcharge, energy_las_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASER_PISTOL).orchestra(Orchestras.ORCHESTRA_LASER_PISTOL)
        ).setDefaultAmmo(Ammo.CAPACITOR_OVERCHARGE, 10));

        NtmItems.GUN_LASER_PISTOL_MORNING_GLORY = registry.register("gun_laser_pistol_morning_glory", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(1_500).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(20F).delay(7).spread(0F).spreadHipfire(0.5F).reload(45).jam(37).sound(NtmSoundEvents.GUN_LASER_PISTOL_FIRE, 1.0F, 1.1F)
                        .mag(new MagazineFullReload(0, 20).addConfigs(energy_emerald, energy_emerald_overcharge, energy_emerald_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASER_PISTOL).orchestra(Orchestras.ORCHESTRA_LASER_PISTOL)
        ).setDefaultAmmo(Ammo.CAPACITOR_OVERCHARGE, 20));

        NtmItems.GUN_LASRIFLE = registry.register("gun_lasrifle", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(2_000).draw(10).inspect(26).crosshair(Crosshair.CIRCLE).scopeTexture(ResourceManager.SCOPE_LUNA_TEX)
                .rec(new Receiver(0)
                        .dmg(50F).delay(8).spreadHipfire(1F).reload(44).jam(36).sound(NtmSoundEvents.GUN_LASER_RIFLE_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 24).addConfigs(energy_las, energy_las_overcharge, energy_las_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASRIFLE).orchestra(Orchestras.ORCHESTRA_LASRIFLE)
        ).setDefaultAmmo(Ammo.CAPACITOR, 24));
    }

    /** Ein Laser hat keinen Rueckstoss -- die Waffe bleibt, wo sie ist. */
    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_ENERGY = (stack, ctx) -> { };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LASER_PISTOL = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 1, 50, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case RELOAD: return new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(-30, 0, 0, 250, IType.SIN_FULL).hold(1750).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("LATCH", new BusAnimationSequence().hold(250).addPos(0, 90, 0, 250, IType.SIN_FULL).hold(1250).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("BATTERY", new BusAnimationSequence().hold(500).addPos(0, -8, 0, 250, IType.SIN_UP).hold(500).addPos(0, 0, 0, 250, IType.SIN_DOWN));
            case JAMMED: return new BusAnimation()
                    .addBus("SWIRL", new BusAnimationSequence().addPos(-15, 0, 0, 250, IType.SIN_FULL).hold(250).addPos(0, 0, 0, 250, IType.SIN_FULL));
            default: return null;
        }
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LASRIFLE = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0.5, 50, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(-15, 0, 0, 50, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case RELOAD: return new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence().hold(100).addPos(0, -8, 0, 300, IType.SIN_UP).hold(900).addPos(0, 0, 0, 300, IType.SIN_DOWN))
                    .addBus("LEVER", new BusAnimationSequence().hold(1700).addPos(-30, 0, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 150, IType.SIN_FULL));
            default: return null;
        }
    };
}
