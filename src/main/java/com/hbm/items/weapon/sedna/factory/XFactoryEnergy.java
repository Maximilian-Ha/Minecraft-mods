package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.effect.FireLingering;
import com.hbm.entity.projectile.BulletBeamBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
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
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.main.ResourceManager;
import com.hbm.particle.helper.PlasmaBlastCreator;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.SoundUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryEnergy.
 *
 * Die Energiewaffen. Alle fuenf schiessen Strahlen statt Geschossen -- dasselbe Hitscan-System,
 * das die 35800 und die NI4NI benutzen und das seit Runde 183 ueberhaupt erst funktioniert.
 *
 * DIE DREI KONDENSATOREN SIND DIE MUNITION, und sie lagen wie die Flammenwerferbrennstoffe
 * schon im Port, ohne dass eine Waffe sie verschiessen konnte.
 *
 * Die Teslakanone ist seit Runde 189 dabei und schliesst die Fabrik ab. Sie unterscheidet sich
 * von den Laserwaffen in drei Punkten: ihr Einschlag ist eine Explosion mit eigenem
 * Spielerverarbeiter, ihr Brandstrahl springt auf bis zu zwanzig Bloecke weiter, und sie
 * frisst aus dem Rucksack statt aus einem Magazin (MagazineBelt).
 */
public class XFactoryEnergy {

    public static BulletConfig energy_tesla;
    public static BulletConfig energy_tesla_overcharge;
    public static BulletConfig energy_tesla_ir;
    public static BulletConfig energy_tesla_ir_sub;

    public static BulletConfig energy_las;
    public static BulletConfig energy_las_overcharge;
    public static BulletConfig energy_las_ir;
    public static BulletConfig energy_emerald;
    public static BulletConfig energy_emerald_overcharge;
    public static BulletConfig energy_emerald_ir;

    /**
     * Der Einschlag der Teslakanone: eine kleine Explosion (Radius 2) mit gerichteten
     * Schockfaechern, zwei Toenen und -- bei einem getroffenen Wesen -- drei Sekunden
     * Laehmung.
     *
     * DER SPIELERVERARBEITER IST HIER DER PUNKT. Ohne ihn wirft die Explosion zwar Monster
     * zurueck, aber keinen Spieler: ein Spieler rechnet seine Bewegung selbst und verwirft,
     * was der Server ihm setzt, wenn es ihm niemand schickt. Die Schnittstelle lag seit jeher
     * im Baum, aber ExplosionVNT hat sie bis Runde 189 nie aufgerufen.
     *
     * Bei einem Blocktreffer rueckt die Aufschlagstelle einen halben Block von der Wand weg,
     * damit die Explosion nicht in der Wand sitzt.
     */
    public static BiConsumer<BulletBeamBase, HitResult> LAMBDA_LIGHTNING_HIT = (strahl, treffer) -> {

        Level level = strahl.level;
        Vec3 ort = treffer.getLocation();

        if(treffer instanceof BlockHitResult blockTreffer) {
            Direction seite = blockTreffer.getDirection();
            ort = ort.add(seite.getStepX() * 0.5D, seite.getStepY() * 0.5D, seite.getStepZ() * 0.5D);
        }

        ExplosionVNT vnt = new ExplosionVNT(level, ort.x, ort.y, ort.z, 2F, strahl.getThrower());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, strahl.damage).setDamageClass(strahl.config.dmgClass));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        SoundUtils.playAtVec3(level, ort, NtmSoundEvents.UFO_BLAST.get(), SoundSource.BLOCKS,
                5.0F, 0.9F + level.random.nextFloat() * 0.2F);
        /* Der Feuerwerksknall geht unmittelbar ueber level.playSound, nicht ueber SoundUtils:
         * genau diese Form steht schon an der Energiegranate und uebersetzt. */
        level.playSound(null, ort.x, ort.y, ort.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 5.0F, 0.5F);

        PlasmaBlastCreator.composeEffectTriple(level, ort.x, ort.y, ort.z, 0.5F, 0.5F, 1.0F, 2F);

        if(treffer instanceof EntityHitResult wesenTreffer && wesenTreffer.getEntity() instanceof LivingEntity lebendig) {
            lebendig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
            lebendig.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 9));
        }
    };

    /**
     * Der Brandkondensator der Teslakanone. Er schlaegt wie der gewoehnliche ein UND springt
     * von dort aus auf JEDES Wesen im Umkreis von zwanzig Bloecken weiter -- jeder Sprung ist
     * ein eigener Strahl mit halbem Schaden, der wiederum trifft.
     *
     * Das Weiterspringen setzt einen Wesentreffer voraus: an einer Wand endet der Blitz.
     *
     * Die Liste wird gemischt, weil die Reihenfolge der Funde sonst die Reihenfolge der
     * Sprungstrahlen bestimmt und der Faecher dadurch immer gleich aussieht.
     */
    public static BiConsumer<BulletBeamBase, HitResult> LAMBDA_LIGHTNING_SPLIT = (strahl, treffer) -> {

        LAMBDA_LIGHTNING_HIT.accept(strahl, treffer);
        if(!(treffer instanceof EntityHitResult wesenTreffer)) return;

        Level level = strahl.level;
        Vec3 ort = treffer.getLocation();
        double reichweite = 20D;

        List<LivingEntity> ziele = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                new AABB(ort, ort).inflate(reichweite)));
        Collections.shuffle(ziele);

        for(LivingEntity ziel : ziele) {
            if(ziel == strahl.thrower) continue;
            if(ziel == wesenTreffer.getEntity()) continue;

            Vec3 delta = new Vec3(ziel.getX() - ort.x, ziel.getY() + ziel.getBbHeight() / 2D - ort.y, ziel.getZ() - ort.z);
            if(delta.length() > reichweite) continue;

            BulletBeamBase sprung = new BulletBeamBase(strahl.thrower, energy_tesla_ir_sub, strahl.damage);
            sprung.setPos(ort);
            sprung.setRotationsFromVector(delta);
            sprung.schussweg(delta.length());
            level.addFreshEntity(sprung);
        }
    };

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
         * Die vier Blitzarten. Der gewoehnliche und der ueberladene durchschlagen, der
         * Brandkondensator nicht -- er muss im ersten Wesen stehenbleiben, weil von dort aus
         * weitergesprungen wird. Der Sprungstrahl selbst durchschlaegt wieder, traegt einen
         * Abnutzungsfaktor von 3 und macht nur einen halben Punkt.
         */
        energy_tesla = new BulletConfig().setItem(Ammo.CAPACITOR).setCasing(() -> new ItemStack(NtmItems.INGOT_POLYMER.get(), 2), 4)
                .setupDamageClass(DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setDoesPenetrate(true)
                .setOnBeamImpact(LAMBDA_LIGHTNING_HIT);
        energy_tesla_overcharge = new BulletConfig().setItem(Ammo.CAPACITOR_OVERCHARGE).setCasing(() -> new ItemStack(NtmItems.INGOT_POLYMER.get(), 2), 4)
                .setupDamageClass(DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setDoesPenetrate(true)
                .setDamage(1.5F).setOnBeamImpact(LAMBDA_LIGHTNING_HIT);
        energy_tesla_ir = new BulletConfig().setItem(Ammo.CAPACITOR_IR).setCasing(() -> new ItemStack(NtmItems.INGOT_POLYMER.get(), 2), 4)
                .setupDamageClass(DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false)
                .setDamage(0.8F).setOnBeamImpact(LAMBDA_LIGHTNING_SPLIT);
        energy_tesla_ir_sub = new BulletConfig().setItem(Ammo.CAPACITOR_IR)
                .setupDamageClass(DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(3).setWear(3F).setRenderRotations(false).setDoesPenetrate(true)
                .setDamage(0.5F).setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);

        /*
         * Die drei Strahlarten der Laserwaffen. Der ueberladene durchschlaegt, der Brandstrahl
         * zaehlt als Feuerschaden statt als Laserschaden -- deshalb der andere DamageClass.
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

        /*
         * Die Teslakanone. Sie hat kein Magazin: MagazineBelt frisst unmittelbar aus dem
         * Rucksack, und welche der drei Kondensatorarten laeuft, entscheidet sich Schuss fuer
         * Schuss neu. Nachgeladen wird nie -- reload(44) und jam(19) sind trotzdem gesetzt,
         * wie im Original, weil sie die Laengen der Klemmbehebung bestimmen.
         */
        NtmItems.GUN_TESLA_CANNON = registry.register("gun_tesla_cannon", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(1_000).draw(10).inspect(33).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(35F).delay(20).spreadHipfire(1.5F).reload(44).jam(19).sound(NtmSoundEvents.GUN_TESLA_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineBelt().addConfigs(energy_tesla, energy_tesla_overcharge, energy_tesla_ir))
                        .offset(0.75, 0, -0.375).offsetScoped(0.75, 0, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_TESLA_ANIMS).orchestra(Orchestras.ORCHESTRA_TESLA)
        ).setDefaultAmmo(Ammo.CAPACITOR, 15));

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

    /**
     * Die Teslakanone. Der Bus COUNT traegt keine Bewegung, sondern eine ZAHL: wie viele
     * Kondensatoren im Rucksack liegen. Der Zeichner liest sie aus und steckt so viele auf das
     * Zahnrad -- damit sieht man der Waffe die Munition an, obwohl sie gar kein Magazin hat.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_TESLA_ANIMS = (stack, type) -> {

        int amount = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                .getAmount(stack, Minecraft.getInstance().player.getInventory());

        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, GunBaseNTItem.getIsAiming(stack) ? -0.5 : -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("CYCLE", new BusAnimationSequence().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350))
                    .addBus("COUNT", new BusAnimationSequence().addPos(amount, 0, 0, 0));
            case CYCLE_DRY: return new BusAnimation()
                    .addBus("CYCLE", new BusAnimationSequence().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350));
            case INSPECT: return new BusAnimation()
                    .addBus("YOMI", new BusAnimationSequence().addPos(8, -4, 0, 0).addPos(4, -1, 0, 500, IType.SIN_DOWN).addPos(4, -1, 0, 1000).addPos(6, -6, 0, 500, IType.SIN_UP))
                    .addBus("SQUEEZE", new BusAnimationSequence().addPos(1, 1, 1, 0).addPos(1, 1, 1, 750).addPos(1, 1, 0.5, 125).addPos(1, 1, 1, 125));
            default: return null;
        }
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LASER_PISTOL = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL));
            case RELOAD: return new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(0, -20, 0, 100).hold(1900).addPos(0, 0, 0, 100))
                    .addBus("LIFT", new BusAnimationSequence().hold(100).addPos(-45, 0, 0, 250, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("JOLT", new BusAnimationSequence().hold(350).addPos(0, 0, 0.5, 100, IType.SIN_FULL).addPos(0, 0, -1.5, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL).holdUntil(2100).addPos(-0.0625, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BATTERY", new BusAnimationSequence().hold(550).addPos(0, 0, 5, 250).hold(550).setPos(0, -2, -2).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_UP));
            case JAMMED: return new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().hold(500).addPos(0, -20, 0, 100).hold(250).addPos(0, 0, 0, 100))
                    .addBus("JOLT", new BusAnimationSequence().hold(950).addPos(-0.0625, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("EQUIP", new BusAnimationSequence().hold(1500).addPos(7.5, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case INSPECT: return new BusAnimation()
                    .addBus("SWIRL", new BusAnimationSequence().addPos(-720, 0, 0, 750, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 750, IType.SIN_FULL));
            default: return null;
        }
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LASRIFLE = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL));
            case RELOAD: return new BusAnimation()
                    .addBus("LEVER", new BusAnimationSequence().addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 1500).addPos(0, 0, 0, 350, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 350).addPos(0, -5, 0, 350, IType.SIN_UP).addPos(0, -5, 0, 500).addPos(0, -0.25, 0, 500, IType.SIN_FULL).addPos(0, -0.25, 0, 150).addPos(0, 0, 0, 350))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 1700).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case JAMMED: return new BusAnimation()
                    .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 600).addPos(0, 0, 0, 350, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 350).addPos(0, -2, 0, 200, IType.SIN_UP).addPos(0, -0.25, 0, 250, IType.SIN_FULL).addPos(0, -0.25, 0, 150).addPos(0, 0, 0, 350))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, 0, 0, 800).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case INSPECT: return new BusAnimation()
                    .addBus("LEVER", new BusAnimationSequence().addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 600).addPos(0, 0, 0, 350, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 350).addPos(0, -2, 0, 200, IType.SIN_UP).addPos(0, -0.25, 0, 250, IType.SIN_FULL).addPos(0, -0.25, 0, 150).addPos(0, 0, 0, 350))
                    .addBus("EQUIP", new BusAnimationSequence().addPos(0, 0, 0, 800).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            default: return null;
        }
    };
}
