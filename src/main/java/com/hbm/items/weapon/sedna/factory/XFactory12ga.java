package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.entity.projectile.DuchessGambit;
import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.*;
import com.hbm.items.weapon.sedna.GunBaseNTItem.GunState;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.factory.GunFactory.AmmoSecret;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.SoundUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class XFactory12ga {

    public static BulletConfig g12_bp;
    public static BulletConfig g12_bp_magnum;
    public static BulletConfig g12_bp_slug;
    public static BulletConfig g12;
    public static BulletConfig g12_slug;
    public static BulletConfig g12_flechette;
    public static BulletConfig g12_magnum;
    public static BulletConfig g12_explosive;
    public static BulletConfig g12_phosphorus;
    public static BulletConfig g12_equestrian_tkr;
    /** Die Signaturpatrone der schoenen Autoschrotflinte. Kein Schaden -- sie ruft ein Luftschiff. */
    public static BulletConfig g12_equestrian_bj;

    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_STANDARD_EXPLODE = (bullet, hr) -> {
        Lego.standardExplode(bullet, hr, 2F); bullet.discard();
    };

    /**
     * Was die Signaturpatrone der schoenen Autoschrotflinte anrichtet: fuenfzig Bloecke ueber
     * dem Getroffenen erscheint die Duchess Gambit, und ein Hornstoss kuendigt sie an. Den Rest
     * erledigt die Schwerkraft -- dasselbe Spiel wie beim Gueterwagen des Lilmac.
     *
     * ABWEICHUNG, die im Original ein Versehen sein duerfte: es spielt den Klang nicht dort, wo
     * das Schiff erscheint, sondern noch einmal fuenfzig Bloecke darueber. Hier steht er am Ort
     * des Schiffs.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_BOAT = (geschoss, treffer) -> {

        Vec3 stelle = treffer.getLocation();
        Level level = geschoss.level;

        DuchessGambit schiff = new DuchessGambit(NtmEntityTypes.DUCHESS_GAMBIT.get(), level);
        schiff.setPos(stelle.x, stelle.y + 50, stelle.z);
        schiff.setOwner(geschoss.getOwner());
        level.addFreshEntity(schiff);

        SoundUtils.playAtVec3(level, schiff.position(), NtmSoundEvents.GUN_BOAT.get(), SoundSource.HOSTILE, 100F, 1F);
        geschoss.discard();
    };

    public static void init(DeferredRegister.Items registry) {

        float buckshotSpread = 0.035F;
        float magnumSpread = 0.015F;
        g12_bp = new BulletConfig().setItem(Ammo.G12_BP).setCasing(CasingType.SHOTSHELL, 12).setBlackPowder(true).setProjectiles(8).setDamage(0.75F/8F).setSpread(buckshotSpread).setRicochetAngle(15).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_BRASS, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_BP"));
        g12_bp_magnum = new BulletConfig().setItem(Ammo.G12_BP_MAGNUM).setCasing(CasingType.SHOTSHELL, 12).setBlackPowder(true).setProjectiles(4).setDamage(0.75F/4F).setSpread(buckshotSpread).setRicochetAngle(25).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_BRASS, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_BP_MAGNUM"));
        g12_bp_slug = new BulletConfig().setItem(Ammo.G12_BP_SLUG).setCasing(CasingType.SHOTSHELL, 12).setBlackPowder(true).setDamage(0.75F).setSpread(0.01F).setRicochetAngle(5).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_BRASS, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_BP_SLUG"));
        g12 = new BulletConfig().setItem(Ammo.G12).setCasing(CasingType.BUCKSHOT, 6).setProjectiles(8).setDamage(1F/8F).setSpread(buckshotSpread).setRicochetAngle(15).setThresholdNegation(2F).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xB52B2B, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA"));
        g12_slug = new BulletConfig().setItem(Ammo.G12_SLUG).setCasing(CasingType.BUCKSHOT, 6).setHeadshot(1.5F).setSpread(0.0F).setRicochetAngle(25).setThresholdNegation(4F).setArmorPiercing(0.15F).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0x393939, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_SLUG"));
        g12_flechette = new BulletConfig().setItem(Ammo.G12_FLECHETTE).setCasing(CasingType.BUCKSHOT, 6).setProjectiles(8).setDamage(1F/8F).setThresholdNegation(5F).setArmorPiercing(0.2F).setSpread(0.025F).setRicochetAngle(5).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0x3C80F0, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_FLECHETTE"));
        g12_magnum = new BulletConfig().setItem(Ammo.G12_MAGNUM).setCasing(CasingType.BUCKSHOT_ADVANCED, 6).setProjectiles(4).setDamage(2F/4F).setSpread(magnumSpread).setRicochetAngle(15).setThresholdNegation(4F).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0x278400, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_MAGNUM"));
        g12_explosive = new BulletConfig().setItem(Ammo.G12_EXPLOSIVE).setCasing(CasingType.BUCKSHOT_ADVANCED, 6).setDamage(2.5F).setOnImpact(LAMBDA_STANDARD_EXPLODE).setSpread(0F).setRicochetAngle(15).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xDA4127, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_EXPLOSIVE"));
        g12_phosphorus = new BulletConfig().setItem(Ammo.G12_PHOSPHORUS).setCasing(CasingType.BUCKSHOT_ADVANCED, 6).setProjectiles(8).setDamage(1F/8F).setSpread(magnumSpread).setRicochetAngle(15).setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0x910001, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_PHOSPHORUS"))
                .setOnImpact((bullet, hr) -> { if(hr.getType() == HitResult.Type.MISS) { EntityHitResult ehr = (EntityHitResult) hr; if(ehr.getEntity() instanceof LivingEntity livingEntity) { HbmLivingAttachments data = HbmLivingAttachments.getData(livingEntity); if(data.phosphorus < 300) data.phosphorus = 300; } } });

        /*
         * Die Geheimpatrone der zerschossenen Mare's Leg. Sie macht selbst keinen Schaden -- der
         * Schaden der Waffe steckt im Empfaenger. Im Original gibt es dazu eine zweite Ausfuehrung
         * mit einem Boot als Aufschlag; die ist NICHT UEBERNOMMEN, weil ihr Gegenstueck im Port
         * fehlt.
         */
        g12_equestrian_tkr = new BulletConfig().setItem(AmmoSecret.G12_EQUESTRIAN).setDamage(0F)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xB52B2B, SpentCasing.COLOR_CASE_EQUESTRIAN).setScale(0.75F).register("12GA_EQUESTRIAN_TKR"));

        g12_equestrian_bj = new BulletConfig().setItem(AmmoSecret.G12_EQUESTRIAN).setDamage(0F).setOnImpact(LAMBDA_BOAT)
                .setCasing(new SpentCasing(SpentCasingType.SHOTGUN).setColor(0xB52B2B, SpentCasing.COLOR_CASE_EQUESTRIAN).setScale(0.75F).register("12gaEquestrianBJ"));

        BulletConfig[] all = new BulletConfig[] {g12_bp, g12_bp_magnum, g12_bp_slug, g12, g12_slug, g12_flechette, g12_magnum, g12_explosive, g12_phosphorus};

        NtmItems.GUN_MARESLEG = registry.register("gun_maresleg", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(600).draw(10).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(16F).delay(20).reload(22, 10, 13, 0).jam(24).sound(NtmSoundEvents.GUN_SHOTGUN_FIRE, 1F, 1F)
                        .mag(new MagazineSingleReload(0, 6).addConfigs(all))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                .setupStandardConfiguration()
                .anim(LAMBDA_MARESLEG_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG)
        ).setDefaultAmmo(Ammo.G12, 12).setNameMutator(LAMBDA_NAME_MARESLEG));
        /* Die Liberator, XFactory12ga Z. 335 des Originals: vier Laeufe, einzeln geladen. */
        NtmItems.GUN_LIBERATOR = registry.register("gun_liberator", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(200).draw(20).inspect(21).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(16F).delay(20).rounds(4).reload(25, 15, 7, 0).jam(45).sound(NtmSoundEvents.GUN_LIBERATOR_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 4).addConfigs(all))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_LIBERATOR))
                .setupStandardConfiguration()
                .anim(LAMBDA_LIBERATOR_ANIMS).orchestra(Orchestras.ORCHESTRA_LIBERATOR)
        ).setDefaultAmmo(Ammo.G12, 12));

        NtmItems.GUN_SPAS12 = registry.register("gun_spas12", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(600).draw(20).inspect(39).reloadSequential(true).reloadChangeType(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(32F).spreadHipfire(0F).delay(20).reload(5, 10, 10, 10, 0).jam(36).sound(NtmSoundEvents.GUN_SPAS_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 8).addConfigs(all))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                .setupStandardConfiguration().ps(LAMBDA_SPAS_SECONDARY).pt(null)
                .anim(LAMBDA_SPAS_ANIMS).orchestra(Orchestras.ORCHESTRA_SPAS)
        ).setDefaultAmmo(Ammo.G12, 16));

        /*
         * Die beidhaendige Mare's Leg. Zwei Empfaenger, zwei Magazine, zwei Waffen im Bild: der
         * erste haengt an der linken Maustaste (pp), der zweite an der rechten (ps). Beide sind
         * baugleich bis auf zwei Dinge -- die Hueftstreuung faellt weg, dafuer streut die Munition
         * selbst ein gutes Drittel mehr, und der Auswurf steht spiegelverkehrt.
         */
        NtmItems.GUN_MARESLEG_AKIMBO = registry.register("gun_maresleg_akimbo", () -> new GunBaseNTItem(WeaponQuality.B_SIDE,
                new GunConfig()
                        .dura(600).draw(5).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(16F).spreadHipfire(0F).spreadAmmo(1.35F).delay(20).reload(22, 10, 13, 0).jam(24).sound(NtmSoundEvents.GUN_SHOTGUN_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineSingleReload(0, 6).addConfigs(all))
                                .offset(0.75, -0.0625, 0.1875D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                        .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_MARESLEG_SHORT_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG_AKIMBO),
                new GunConfig()
                        .dura(600).draw(5).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(16F).spreadHipfire(0F).spreadAmmo(1.35F).delay(20).reload(22, 10, 13, 0).jam(24).sound(NtmSoundEvents.GUN_SHOTGUN_FIRE, 1.0F, 1.0F)
                                .mag(new MagazineSingleReload(1, 6).addConfigs(all))
                                .offset(0.75, -0.0625, -0.1875)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                        .ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_MARESLEG_SHORT_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG_AKIMBO)
        ).setDefaultAmmo(Ammo.G12, 24));

        /*
         * Die zerschossene Mare's Leg: abgesaegt, dreifacher Schaden, und sie nutzt sich nicht ab --
         * dura(0) und LAMBDA_NOWEAR_FIRE zusammen heissen, dass die Waffe keinen Verschleiss kennt.
         * Ihr erstes Magazin ist die Geheimpatrone; wer sie hat, schiesst damit ohne eigenen
         * Patronenschaden.
         */
        NtmItems.GUN_MARESLEG_BROKEN = registry.register("gun_maresleg_broken", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(0).draw(5).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(48F).spreadAmmo(1.15F).delay(20).reload(22, 10, 13, 0).jam(24).sound(NtmSoundEvents.GUN_SHOTGUN_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 6).addConfigs(g12_equestrian_tkr, g12_bp, g12_bp_magnum, g12_bp_slug, g12, g12_slug, g12_flechette, g12_magnum, g12_explosive, g12_phosphorus))
                        .offset(0.75, -0.0625, -0.1875)
                        .canFire(Lego.LAMBDA_STANDARD_CAN_FIRE).fire(Lego.LAMBDA_NOWEAR_FIRE).recoil(LAMBDA_RECOIL_MARESLEG))
                .setupStandardConfiguration()
                .anim(LAMBDA_MARESLEG_SHORT_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG_SHORT)
        ).setDefaultAmmo(Ammo.G12_MAGNUM, 24));

        /*
         * Die Autoschrotflinte, XFactory12ga Z. 357 des Originals. Zwanzig Schuss im Kasten,
         * vollautomatisch, und sie feuert auch nach dem letzten Schuss weiter (autoAfterDry),
         * bis der Abzug losgelassen wird.
         */
        NtmItems.GUN_AUTOSHOTGUN = registry.register("gun_autoshotgun", () -> new GunBaseNTItem(WeaponQuality.A_SIDE, new GunConfig()
                .dura(2_000).draw(10).inspect(33).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(48F).delay(10).auto(true).autoAfterDry(true).dryfireAfterAuto(true).reload(44).jam(19).sound(NtmSoundEvents.GUN_SHREDDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 20).addConfigs(all))
                        .offset(0.75, -0.125, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AUTOSHOTGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_SHREDDER_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER)
        ).setDefaultAmmo(Ammo.G12, 20));

        /*
         * Die schoene Autoschrotflinte, XFactory12ga Z. 378 des Originals. Hundert Schuss im
         * Gurt, vier Ticks zwischen zwei Schuessen, und ihr erstes Magazin ist die
         * Signaturpatrone -- wer sie hat, wirft Luftschiffe.
         *
         * NICHT UEBERNOMMEN: gun_autoshotgun_shredder, die dritte der Familie. Ihre Munition
         * zerfaellt beim Aufschlag in Strahlen, die weiterspringen (makeShredderConfig mit
         * setOnBeamImpact und setOnRicochet); das Geschossteilsystem des Ports kennt diese
         * Aufspaltung noch nicht.
         */
        NtmItems.GUN_AUTOSHOTGUN_SEXY = registry.register("gun_autoshotgun_sexy", () -> new GunBaseNTItem(WeaponQuality.LEGENDARY, new GunConfig()
                .dura(5_000).draw(20).inspect(65).reloadSequential(true).inspectCancel(false).crosshair(Crosshair.L_CIRCLE).hideCrosshair(false).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(64F).delay(4).auto(true).dryfireAfterAuto(true).reload(110).jam(19).sound(NtmSoundEvents.GUN_SHREDDER_FIRE, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 100).addConfigs(g12_equestrian_bj, g12_bp, g12_bp_magnum, g12_bp_slug, g12, g12_slug, g12_flechette, g12_magnum, g12_explosive, g12_phosphorus))
                        .offset(0.75, -0.125, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_SEXY))
                .setupStandardConfiguration()
                .anim(LAMBDA_SEXY_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER_SEXY)
        ).setDefaultAmmo(Ammo.G12_MAGNUM, 50));
    }

    /** Mit der Saege ist sie keine Flinte mehr, sondern eine Mare's Leg. */
    public static Function<ItemStack, Component> LAMBDA_NAME_MARESLEG = (stack) -> {
        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SAWED_OFF)) {
            return Component.translatable(stack.getItem().getDescriptionId() + ".short");
        }
        return null;
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_LIBERATOR = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(5, (float) (ctx.getPlayer().random.nextGaussian() * 1.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_MARESLEG = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil(10, (float) (ctx.getPlayer().getRandom().nextGaussian() * 1.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_AUTOSHOTGUN = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().getRandom().nextGaussian() * 1.5) + 1.5F,
                (float) (ctx.getPlayer().getRandom().nextGaussian() * 0.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_SEXY = (stack, ctx) -> {
        GunBaseNTItem.setupRecoil((float) (ctx.getPlayer().getRandom().nextGaussian() * 0.5),
                (float) (ctx.getPlayer().getRandom().nextGaussian() * 0.5));
    };

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_SPAS_SECONDARY = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Player player = ctx.getPlayer();
        Receiver rec = ctx.config.getReceivers(stack)[0];
        int index = ctx.configIndex;
        GunState state = GunBaseNTItem.getState(stack, index);
        if(state == GunState.IDLE) {
            if(rec.getCanFire(stack).apply(stack, ctx)) {
                rec.getOnFire(stack).accept(stack, ctx);
                int remaining = rec.getRoundsPerCycle(stack);
                int timeFired = 1;
                for(int i = 0; i < remaining; i++) {
                    if(rec.getCanFire(stack).apply(stack, ctx)) {
                        rec.getOnFire(stack).accept(stack, ctx);
                        timeFired++;
                    }
                }
                if(rec.getFireSound(stack) != null) SoundUtils.playAtVec3(entity.level, entity.position(), rec.getFireSound(stack).value(), entity.getSoundSource(), rec.getFireVolume(stack), rec.getFirePitch(stack) * (timeFired > 1 ? 0.9F : 1F));
                GunBaseNTItem.setState(stack, index, GunState.COOLDOWN);
                GunBaseNTItem.setTimer(stack, index, 20);
            } else {
                if(rec.getDoesDryFire(stack)) {
                    GunBaseNTItem.playAnimation(player, stack, GunAnimation.CYCLE_DRY, index);
                    GunBaseNTItem.setState(stack, index, GunState.DRAWING);
                    GunBaseNTItem.setTimer(stack, index, rec.getDelayAfterDryFire(stack));
                }
            }
        }
        if(state == GunState.RELOADING) {
            GunBaseNTItem.setReloadCancel(stack, true);
        }
    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_SPAS_ANIMS = (stack, type) -> {
        return switch (type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, BusAnimationKeyframe.IType.SIN_DOWN));
            case CYCLE -> ResourceManager.spas_12_anim.get("Fire");
            case CYCLE_DRY -> ResourceManager.spas_12_anim.get("FireDry");
            case ALT_CYCLE -> ResourceManager.spas_12_anim.get("FireAlt");
            case RELOAD -> {
                boolean empty = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory) <= 0;
                yield ResourceManager.spas_12_anim.get(empty ? "ReloadEmptyStart" : "ReloadStart");
            }
            case RELOAD_CYCLE -> ResourceManager.spas_12_anim.get("Reload");
            case RELOAD_END -> ResourceManager.spas_12_anim.get("ReloadEnd");
            case JAMMED -> ResourceManager.spas_12_anim.get("Jammed");
            case INSPECT -> ResourceManager.spas_12_anim.get("Inspect");
            default -> null;
        };

    };

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MARESLEG_ANIMS = (stack, type) -> {
        return switch (type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                    .addBus("SIGHT", new BusAnimationSequence().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200));
            case RELOAD -> {
                boolean empty = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory) <= 0;
                yield new BusAnimation()
                        .addBus("LIFT", new BusAnimationSequence().addPos(30, 0, 0, 400, IType.SIN_FULL))
                        .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 0, 400).addPos(-85, 0, 0, 200))
                        .addBus("SHELL", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(0, 0.25F, -3, 0).addPos(0, empty ? 0.25F : 0.125F, -1.5F, 150, IType.SIN_UP).addPos(0, empty ? 0.25F : -0.25F, 0, 150, IType.SIN_DOWN))
                        .addBus("FLAG", new BusAnimationSequence().addPos(0, 0, 0, empty ? 900 : 0).addPos(1, 1, 1, 0));
            }
            case RELOAD_CYCLE -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(30, 0, 0, 0))
                    .addBus("LEVER", new BusAnimationSequence().addPos(-85, 0, 0, 0))
                    .addBus("SHELL", new BusAnimationSequence().addPos(0, 0.25F, -3, 0).addPos(0, 0.125F, -1.5F, 150, IType.SIN_UP).addPos(0, -0.125F, 0, 150, IType.SIN_DOWN))
                    .addBus("FLAG", new BusAnimationSequence().addPos(1, 1, 1, 0));
            case RELOAD_END -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(30, 0, 0, 0).addPos(30, 0, 0, 250).addPos(0, 0, 0, 400, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(-85, 0, 0, 0).addPos(0, 0, 0, 200))
                    .addBus("FLAG", new BusAnimationSequence().addPos(1, 1, 1, 0));
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(30, 0, 0, 0).addPos(30, 0, 0, 250).addPos(0, 0, 0, 400, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(-85, 0, 0, 0).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 650).addPos(-85, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 850).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 45, 800).addPos(0, 0, 0, 200, IType.SIN_UP))
                    .addBus("FLAG", new BusAnimationSequence().addPos(1, 1, 1, 0));
            case INSPECT -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(-35, 0, 0, 300, IType.SIN_FULL).addPos(-35, 0, 0, 1150).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("TURN", new BusAnimationSequence().addPos(0, 0, 0, 450).addPos(0, 0, -90, 500, IType.SIN_FULL).addPos(0, 0, -90, 500).addPos(0, 0, 0, 500, IType.SIN_FULL));
            default -> null;
        };

    };

    /**
     * Der Satz der abgesaegten Ausfuehrungen -- Akimbo, zerschossen, und die Waffe mit dem
     * Saegeaufsatz. Ohne Lauf und Schaft laesst sie sich beim Durchladen um die eigene Achse
     * werfen (FLIP); die Huelse im Lauf wird dabei aus dem Bild geschoben, weil sie sonst
     * mitfliegen wuerde. Alles, was hier fehlt, kommt aus dem langen Satz.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MARESLEG_SHORT_ANIMS = (stack, type) -> {
        return switch (type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 250, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                    .addBus("SIGHT", new BusAnimationSequence().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200))
                    .addBus("FLIP", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(360, 0, 0, 400))
                    .addBus("SHELL", new BusAnimationSequence().addPos(-20, 0, 0, 0));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("HAMMER", new BusAnimationSequence().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200))
                    .addBus("FLIP", new BusAnimationSequence().addPos(0, 0, 0, 600).addPos(360, 0, 0, 400))
                    .addBus("SHELL", new BusAnimationSequence().addPos(-20, 0, 0, 0));
            case JAMMED -> new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(30, 0, 0, 0).addPos(30, 0, 0, 250).addPos(0, 0, 0, 400, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(-85, 0, 0, 0).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 650).addPos(-85, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("FLAG", new BusAnimationSequence().addPos(1, 1, 1, 0));
            default -> LAMBDA_MARESLEG_ANIMS.apply(stack, type);
        };
    };

    /*
     * Die vier Huelsen der Liberator waehrend des Ladens. Das Original schreibt fuer jeden
     * Fuellstand einen eigenen Zweig aus -- vier bis zum Verwechseln aehnliche Bloecke, in denen
     * nur die Nummer der bewegten Huelse wandert. Hier steht dieselbe Regel als Schleife:
     * alles unter "inBewegung" steckt schon im Lauf, "inBewegung" fliegt gerade herein, alles
     * darueber liegt noch draussen.
     *
     * Das Original haengt fuer die jeweils nicht gemeinten Huelsen einen Bus namens "NULL" an,
     * den niemand liest. Den braucht es hier nicht: jede Huelse kommt genau einmal vor.
     */
    private static BusAnimation liberatorHuelsen(BusAnimation anim, int inBewegung, boolean langerWeg) {

        for(int i = 0; i < 4; i++) {
            String bus = "SHELL" + (i + 1);

            if(i < inBewegung) {
                anim.addBus(bus, new BusAnimationSequence().addPos(0, 0, 0, 0));
            } else if(i == inBewegung) {
                anim.addBus(bus, langerWeg
                        ? new BusAnimationSequence().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP)
                        : new BusAnimationSequence().addPos(2, -4, -2, 0).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
            } else {
                anim.addBus(bus, new BusAnimationSequence().addPos(2, -4, -2, 0));
            }
        }

        return anim;
    }

    /** Dieselben vier Huelsen in Ruhe: die ersten "vorhanden" stecken im Lauf, der Rest liegt daneben. */
    private static BusAnimation liberatorHuelsenRuhe(BusAnimation anim, int vorhanden) {

        for(int i = 0; i < 4; i++) {
            anim.addBus("SHELL" + (i + 1), i < vorhanden
                    ? new BusAnimationSequence().addPos(0, 0, 0, 0)
                    : new BusAnimationSequence().addPos(2, -8, -2, 0));
        }

        return anim;
    }

    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_LIBERATOR_ANIMS = (stack, type) -> {

        int ammo = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, NuclearTechMod.proxy.me().inventory);

        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -2.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL));
            case CYCLE_DRY -> new BusAnimation();
            case RELOAD -> ammo > 3 ? null : liberatorHuelsen(new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 100))
                    .addBus("BREAK", new BusAnimationSequence().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN)), ammo, true);
            case RELOAD_CYCLE -> ammo > 2 ? null : liberatorHuelsen(new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 0))
                    .addBus("BREAK", new BusAnimationSequence().addPos(60, 0, 0, 0)), ammo + 1, false);
            case RELOAD_END -> liberatorHuelsenRuhe(new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 0).addPos(15, 0, 0, 250).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP)), ammo + 1);
            case JAMMED -> liberatorHuelsenRuhe(new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 0).addPos(15, 0, 0, 250).addPos(0, 0, 0, 50).addPos(0, 0, 0, 550).addPos(15, 0, 0, 100).addPos(15, 0, 0, 600).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP).addPos(0, 0, 0, 600).addPos(45, 0, 0, 250, IType.SIN_DOWN).addPos(45, 0, 0, 300).addPos(0, 0, 0, 150, IType.SIN_UP)), ammo + 1);
            /* Beim Nachsehen zaehlt das Original eine Huelse weniger als beim Nachladen --
             * die gerade gekammerte ist da schon verbucht. */
            case INSPECT -> liberatorHuelsenRuhe(new BusAnimation()
                    .addBus("LATCH", new BusAnimationSequence().addPos(15, 0, 0, 100).addPos(15, 0, 0, 1100).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequence().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN).addPos(60, 0, 0, 500).addPos(0, 0, 0, 250, IType.SIN_UP)), ammo);
            default -> null;
        };
    };

    /** Die Autoschrotflinte, XFactory12ga Z. 629 des Originals. */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_SHREDDER_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE -> new BusAnimation()
                    .addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("CYCLE", new BusAnimationSequence().addPos(0, 0, 0, 150).addPos(0, 0, 18, 100));
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("CYCLE", new BusAnimationSequence().addPos(0, 0, 0, 150).addPos(0, 0, 18, 100));
            case RELOAD -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence().addPos(0, -8, 0, 250, IType.SIN_UP).addPos(0, -8, 0, 1000).addPos(0, 0, 0, 300))
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 750).addPos(-25, 0, 0, 300, IType.SIN_FULL).addPos(-25, 0, 0, 500).addPos(-27, 0, 0, 100, IType.SIN_DOWN).addPos(-25, 0, 0, 100, IType.SIN_FULL).addPos(-25, 0, 0, 150).addPos(0, 0, 0, 300, IType.SIN_FULL));
            case JAMMED -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(0, -2, 0, 150, IType.SIN_UP).addPos(0, 0, 0, 100))
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 750).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case INSPECT -> new BusAnimation()
                    .addBus("MAG", new BusAnimationSequence()
                            .addPos(0, -1, 0, 150).addPos(6, -1, 0, 150).addPos(6, 12, 0, 350, IType.SIN_DOWN).addPos(6, -2, 0, 350, IType.SIN_UP).addPos(6, -1, 0, 50)
                            .addPos(6, -1, 0, 100).addPos(0, -1, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 150, IType.SIN_UP))
                    .addBus("SPEEN", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(360, 0, 0, 700))
                    .addBus("LIFT", new BusAnimationSequence().addPos(0, 0, 0, 1450).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            default -> null;
        };
    };

    /**
     * Die schoene Autoschrotflinte, XFactory12ga Z. 655 des Originals.
     *
     * Beim Schuss verschiebt der Bus SHELLS den Gurt um einen Platz -- deshalb steht dort der
     * aktuelle Fuellstand des Magazins und keine feste Zahl. Beim Pruefen nimmt sie einen
     * Schluck aus der Flasche; das ist im Original so, und es bleibt so.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_SEXY_ANIMS = (stack, type) -> {
        return switch(type) {
            case EQUIP -> new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().addPos(45, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            case CYCLE -> {
                int menge = ((GunBaseNTItem) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null);
                yield new BusAnimation()
                        .addBus("RECOIL", new BusAnimationSequence().hold(50).addPos(0, 0, -0.25, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                        .addBus("BARREL", new BusAnimationSequence().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150))
                        .addBus("CYCLE", new BusAnimationSequence().addPos(1, 0, 0, 150))
                        .addBus("HOOD", new BusAnimationSequence().hold(50).addPos(3, 0, 0, 50, IType.SIN_DOWN).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("SHELLS", new BusAnimationSequence().setPos(menge - 1, 0, 0));
            }
            case CYCLE_DRY -> new BusAnimation()
                    .addBus("CYCLE", new BusAnimationSequence().addPos(0, 0, 18, 50));
            case RELOAD -> new BusAnimation()
                    .addBus("LOWER", new BusAnimationSequence().addPos(15, 0, 0, 500, IType.SIN_FULL).hold(2750).addPos(12, 0, 0, 100, IType.SIN_DOWN).addPos(15, 0, 0, 100, IType.SIN_FULL).hold(1050).addPos(18, 0, 0, 100, IType.SIN_DOWN).addPos(15, 0, 0, 100, IType.SIN_FULL).hold(300).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("LEVER", new BusAnimationSequence().addPos(0, 0, 1, 150).hold(4700).addPos(0, 0, 0, 150))
                    .addBus("HOOD", new BusAnimationSequence().hold(250).addPos(60, 0, 0, 500, IType.SIN_FULL).hold(3250).addPos(0, 0, 0, 500, IType.SIN_UP))
                    .addBus("BELT", new BusAnimationSequence().setPos(1, 0, 0).hold(750).addPos(0, 0, 0, 500, IType.SIN_UP).hold(2000).addPos(1, 0, 0, 500, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequence().hold(1500).addPos(0, -1, 0, 250, IType.SIN_UP).addPos(2, -1, 0, 500, IType.SIN_UP).addPos(7, 1, 0, 250, IType.SIN_UP).addPos(15, 2, 0, 250).setPos(0, -2, 0).addPos(0, 0, 0, 500, IType.SIN_UP))
                    .addBus("MAGROT", new BusAnimationSequence().hold(2250).addPos(0, 0, -180, 500, IType.SIN_FULL).setPos(0, 0, 0));
            case INSPECT -> new BusAnimation()
                    .addBus("BOTTLE", new BusAnimationSequence().setPos(8, -8, -2).addPos(6, -4, -2, 500, IType.SIN_DOWN).addPos(3, -3, -5, 500, IType.SIN_FULL).addPos(3, -2, -5, 1000).addPos(4, -6, -2, 750, IType.SIN_FULL).addPos(6, -8, -2, 500, IType.SIN_UP))
                    .addBus("SIP", new BusAnimationSequence().setPos(25, 0, 0).hold(500).addPos(-90, 0, 0, 500, IType.SIN_FULL).addPos(-110, 0, 0, 1000).addPos(25, 0, 0, 750, IType.SIN_FULL));
            default -> null;
        };
    };
}
