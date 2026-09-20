package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.impl.GunChargeThrowerItem;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.network.toclient.MuzzleFlashPacket;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.BiConsumer;

/** Orchestras are server-side components that run along client-side animations.
 * The orchestra only knows what animation is or was playing and how long it started, but not if it is still active.
 * Orchestras are useful for things like playing server-side sound, spawning casings or sending particle packets.*/
public class Orchestras {

    public static BiConsumer<ItemStack, LambdaContext> DEBUG_ORCHESTRA = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 3) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 34) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());

           if(timer == 16) {
               Receiver rec = ctx.config.getReceivers(stack)[0];
               IMagazine<?> mag = rec.getMagazine(stack);
               SpentCasing casing = mag.getCasing(stack, ctx.container);
               if(casing != null) for(int i = 0; i < mag.getCapacity(stack); i++) CasingCreator.composeEffect(entity.level, entity, 0.25, -0.125, -0.125, -0.05, 0, 0, 0.01, casing.getName());
           }
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 3) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Henry. Ein Unterhebelrepetierer laedt EINZELN nach, deshalb die drei
     * Nachladezustaende: RELOAD einmal am Anfang, RELOAD_CYCLE je Patrone, RELOAD_END zum
     * Schluss. Der Hebel schnappt dabei nur, wenn die Waffe vorher leer war -- war noch etwas
     * drin, sitzt die naechste Patrone schon im Lauf.
     */
    /**
     * Die Spulenkanone und die NI4NI teilen sich diese Orchestrierung. Sie ist die kuerzeste
     * im ganzen Bausatz: ein Muendungsblitz beim Schuss, ein Ton beim Nachladen. Keine Huelse
     * -- ein Lichtbogen wirft nichts aus.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_COILGUN = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_COIL_RELOAD.get(), entity.getSoundSource(), 1F, 1F);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_HENRY = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.RELOAD) {
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.RELOAD_CYCLE) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.RELOAD_END) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 12 && ctx.config.getReceivers(stack)[0].getMagazine(stack).getAmountBeforeReload(stack) <= 0)
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 44) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 14) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(entity.level, entity, 0.5, -0.125, aiming ? -0.125 : -0.375D, 0, 0.12, -0.12, 0.01, -7.5F + (float)entity.random.nextGaussian() * 5F, (float)entity.random.nextGaussian() * 1.5F, casing.getName(), true, 60, 0.5D, 20);
            }
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MARESLEG = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.RELOAD) {
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_LOAD.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.RELOAD_CYCLE) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_LOAD.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.RELOAD_END) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.7F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.7F);
            if(timer == 17) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 29) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 14) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(entity.level, entity, 0.3125, -0.125, aiming ? -0.125 : -0.375D, 0, 0.18, -0.12, 0.01, -10F + (float)entity.random.nextGaussian() * 5F, (float)entity.random.nextGaussian() * 2.5F, casing.getName(), true, 60, 0.5D, 20);
            }
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_SPAS = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE || type == GunAnimation.ALT_CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_COCK.get(), entity.getSoundSource());
            if(timer == 10) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container); //turns out there's a reason why stovepipes look like that
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.375, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, 0.18, -0.12, 0.01, -3F + (float)entity.random.nextGaussian() * 2.5F, -15F + entity.random.nextFloat() * -5F, casing.getName());
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD) {
            IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
            if(mag.getAmount(stack, ctx.container) == 0) {
                if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
                if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            }
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_LOAD.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD_CYCLE) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_LOAD.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_OPEN.get(), entity.getSoundSource());
            if(timer == 18) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 18) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_WHACK.get(), entity.getSoundSource());
            if(timer == 25) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_WHACK.get(), entity.getSoundSource());
            if(timer == 29) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_CLOSE.get(), entity.getSoundSource());
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_HANGMAN = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.RELOAD) {

            //if(timer == 0) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_REVOLVER_COCK, 1F, 0.8F);
            //if(timer == 5) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_MAG_SMALL_REMOVE, 1F, 0.8F);
            //if(timer == 25) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_REVOLVER_CLOSE, 1F, 1F);
            //if(timer == 35) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_REVOLVER_COCK, 1F, 0.75F);

            if(timer == 10) {
                Receiver rec = ctx.config.getReceivers(stack)[0];
                IMagazine mag = rec.getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) for(int i = 0; i < mag.getCapacity(stack); i++) CasingCreator.composeEffect(level, entity, 0.25, -0.25, -0.125, -0.05, 0, 0, 0.01, -6.5F + (float)entity.random.nextGaussian() * 3F, (float)entity.random.nextGaussian() * 5F, casing.getName());
            }
        }

        if(type == GunAnimation.INSPECT) {
            if(timer == 16 && ctx.getPlayer() != null) {
                HitResult hr = EntityDamageUtil.getMouseOver(ctx.getPlayer(), 3.0D);
                if(hr != null) {
                    if(hr.getType() == Type.ENTITY) {
                        EntityHitResult ehr = (EntityHitResult) hr;
                        Entity hitEntity = ehr.getEntity();
                        float damage = 10F;
                        hitEntity.hurt(entity.level.damageSources().playerAttack(ctx.getPlayer()), damage);
                        Vec3 motion = hitEntity.getDeltaMovement();
                        hitEntity.setDeltaMovement(motion.x * 2, motion.y, motion.z * 2);
                        //entity.worldObj.playSoundAtEntity(mop.entityHit, NTMSounds.GUN_SMACK, 1F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
                    }
                    if(hr.getType() == Type.BLOCK) {
                        BlockHitResult bhr = (BlockHitResult) hr;
                        BlockPos pos = bhr.getBlockPos();
                        BlockState state = level.getBlockState(bhr.getBlockPos());
                        Block block = state.getBlock();
                        SoundUtils.playAtBlockPos(level, pos, block.getSoundType(state, level, pos, ctx.getPlayer()).getStepSound(), entity.getSoundSource(), 2F, 0.9F + entity.random.nextFloat() * 0.2F);
                    }
                }
            }
        }

        if(type == GunAnimation.JAMMED) {
            //if(timer == 10) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_REVOLVER_COCK, 1F, 0.8F);
            //if(timer == 15) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_MAG_SMALL_REMOVE, 1F, 0.8F);
            //if(timer == 20) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_REVOLVER_CLOSE, 1F, 1F);
            //if(timer == 25) entity.worldObj.playSoundAtEntity(entity, NTMSounds.GUN_REVOLVER_COCK, 1F, 0.75F);
        }
    };

    /**
     * Der schwere Revolver. Beim Nachladen kippt die Trommel heraus, die Huelsen fallen, und
     * am Ende wird der Hahn gespannt -- die vier Toene sitzen auf genau den Zaehlerstaenden
     * des Originals.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_NOPIP = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 3) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 34) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());

            if(timer == 16) {
                Receiver rec = ctx.config.getReceivers(stack)[0];
                IMagazine mag = rec.getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) for(int i = 0; i < mag.getCapacity(stack); i++) CasingCreator.composeEffect(level, entity, 0.25, -0.125, -0.125, -0.05, 0, 0, 0.01, -6.5F + (float) entity.random.nextGaussian() * 3F, (float) entity.random.nextGaussian() * 5F, casing.getName());
            }
        }

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.INSPECT) {
            if(timer == 3) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Panzerschreck. Beim Schuss das Muendungsfeuer, beim Nachladen ein einziger Ton -- das
     * Einschieben der Rakete, dreissig Ticks nach Beginn, kurz bevor das Rohr wieder herumkommt.
     *
     * Das Original schreibt den Namen ORCHESTRA_PANERSCHRECK; das fehlende Z ist ein Vertipper
     * und steht hier richtig, weil der Name nirgends als Zeichenkette auftaucht.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_PANZERSCHRECK = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_CANISTER_INSERT.get(), entity.getSoundSource());
        }
    };

    /**
     * Die Liberator. Beim Nachladen fliegen so viele Huelsen heraus, wie seit dem letzten
     * Nachladen verschossen wurden -- daher die Rechnung getAmountAfterReload minus getAmount.
     * Beim Nachsehen (INSPECT) tut sie dasselbe und setzt den Zaehler danach auf null, damit die
     * Huelsen nicht beim naechsten Nachladen ein zweites Mal kommen.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_LIBERATOR = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 4) {
                IMagazine mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                int toEject = mag.getAmountAfterReload(stack) - mag.getAmount(stack, ctx.container);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) for(int i = 0; i < toEject; i++) CasingCreator.composeEffect(level, entity, 0.625, -0.1875, -0.375D, -0.12, 0.18, 0, 0.01, -15F + (float) entity.random.nextGaussian() * 7.5F, (float) entity.random.nextGaussian() * 5F, casing.getName(), true, 60, 0.5D, 20);
            }
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.RELOAD_CYCLE) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.RELOAD_END) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
        }

        if(type == GunAnimation.JAMMED) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
        }

        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.INSPECT) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.75F);

            IMagazine mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
            int toEject = mag.getAmountAfterReload(stack) - mag.getAmount(stack, ctx.container);

            if(timer == 4 && toEject > 0) {
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                /* Das Original multipliziert hier statt zu addieren -- ein Tippfehler, der die
                 * Huelsen mal in die eine, mal in die andere Richtung wirft. Uebernommen, weil
                 * er nur den Wurfwinkel betrifft und nichts kaputtmacht. */
                if(casing != null) for(int i = 0; i < toEject; i++) CasingCreator.composeEffect(level, entity, 0.625, -0.1875, -0.375D, -0.12, 0.18, 0, 0.01, -15F * (float) entity.random.nextGaussian() * 7.5F, (float) entity.random.nextGaussian() * 5F, casing.getName(), true, 60, 0.5D, 20);
                mag.setAmountAfterReload(stack, 0);
            }

            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_GREASEGUN = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.EQUIP) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 2) {
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.55, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, 0.18, -0.12, 0.01,
                        -7.5F + (float) entity.random.nextGaussian() * 5F, 12F + (float) entity.random.nextGaussian() * 5F, casing.getName());
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 24) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 1.25F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
    };

    /**
     * Die Pfefferbuechse. Der Lauf dreht beim Nachladen einmal ganz durch -- das ist das
     * Revolvertrommel-Geraeusch bei Bild 55.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_PEPPERBOX = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 24) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
            if(timer == 55) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 21) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.6F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.6F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 3) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 28) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 45) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.6F);
        }
    };

    /** Der leichte Revolver samt seiner Atlas-Ausfuehrung: Trommel auf, Patronen rein, Trommel zu. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_ATLAS = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
            if(timer == 44) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 24) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 34) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Die AM180. Beim Nachladen faellt das Trommelmagazin zu Boden -- das ist der Aufprall bei
     * Bild 26.
     *
     * ABWEICHUNG: das Original hat hier zwei Saetze, einen fuer die alten und einen fuer die neuen
     * Animationen, umgeschaltet ueber ClientConfig.GUN_ANIMS_LEGACY. Den Schalter gibt es im Port
     * nicht; hier steht der neue Satz, passend zur Animationsdatei am180.json.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_AM180 = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.4375, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, -0.06, 0, 0.01,
                        (float) entity.random.nextGaussian() * 10F, (float) entity.random.nextGaussian() * 10F, casing.getName());
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 6) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 6) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.25F, 1F);
            if(timer == 48) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 54) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 6) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 6) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 53) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
        }
    };

    /** Die Star-F. Die Huelse fliegt nach oben und rechts weg, das Schloss klackt zweimal. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_STAR_F = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.3125, aiming ? 0 : -0.125, aiming ? 0 : -0.1875D, 0, 0.18, -0.12, 0.01,
                        (float) entity.random.nextGaussian() * 5F, 12.5F + entity.random.nextFloat() * 5F, casing.getName());
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 1.1F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 5) {
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            }
            if(timer == 22) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 19) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
            if(timer == 23) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 27) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
        }
    };

    /**
     * Die abgesaegten Ausfuehrungen der Mare's Leg. Sie werfen die Huelse gerader aus als die
     * lange -- der Winkel ist -15 statt -10 Grad -- und werfen sie weiter, weil beim Durchladen
     * die ganze Waffe herumgewirbelt wird.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MARESLEG_SHORT = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.RELOAD) {
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_LOAD.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD_CYCLE) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHOTGUN_LOAD.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD_END) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.7F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.7F);
            if(timer == 17) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 29) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 14) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.3125, -0.125, aiming ? -0.125 : -0.375D, 0, -0.08, 0, 0.01,
                        -15F + (float) entity.random.nextGaussian() * 5F, (float) entity.random.nextGaussian() * 2.5F, casing.getName(), true, 60, 0.5D, 20);
            }
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
    };

    /**
     * Die beidhaendige Mare's Leg. Bis auf den Schuss ist alles wie bei der einzelnen kurzen; nur
     * die Huelse fliegt zur richtigen Seite -- der linke Lauf wirft nach links, der rechte nach
     * rechts, sonst kaemen beide Huelsen aus derselben Waffe.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MARESLEG_AKIMBO = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 14) {
                int side = ctx.configIndex == 0 ? -1 : 1;
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.3125, -0.125, (aiming ? -0.125 : -0.375D) * side, 0, -0.08, 0, 0.01,
                        -15F + (float) entity.random.nextGaussian() * 5F, (float) entity.random.nextGaussian() * 2.5F, casing.getName(), true, 60, 0.5D, 20);
            }
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LEVER_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            return;
        }

        ORCHESTRA_MARESLEG_SHORT.accept(stack, ctx);
    };

    /** Die beidhaendige Star-F: wie die einzelne, nur wirft jede Haelfte zu ihrer eigenen Seite. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_STAR_F_AKIMBO = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                int side = ctx.configIndex == 0 ? -1 : 1;
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.3125, aiming ? 0 : -0.125, aiming ? 0 : -0.1875D * side, 0, 0.18, -0.12 * side, 0.01,
                        (float) entity.random.nextGaussian() * 5F, 12.5F + entity.random.nextFloat() * 5F, casing.getName());
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 1.1F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 5) {
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            }
            if(timer == 22) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 19) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
            if(timer == 23) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 27) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.1F);
        }
    };

    /** Die DANI. Dieselben Zeitpunkte wie der Atlas, nur der Hahn klackt frueher. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_DANI = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
            if(timer == 44) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 24) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 34) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /** Die Uzi. Beim Ziehen schnappt der Schulterstuetzenbuegel auf. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_UZI = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.EQUIP) {
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource(), 1F, 1.25F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 1) {
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.375, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, 0.18, -0.12, 0.01,
                        -2.5F + (float) entity.random.nextGaussian() * 5F, 10F + entity.random.nextFloat() * 15F, casing.getName());
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 4) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 17) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
            if(timer == 31) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
    };

    /**
     * Die beidhaendige Uzi. Sie zielt nie -- darum faellt der Zielfall beim Huelsenauswurf weg --
     * und beide Laeufe werfen zu ihrer eigenen Seite, samt gespiegeltem Drall.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_UZI_AKIMBO = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.EQUIP) {
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource(), 1F, 1.25F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 1) {
                int side = ctx.configIndex == 0 ? -1 : 1;
                IMagazine<?> mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.375, -0.125, -0.375D * side, 0, 0.18, -0.12 * side, 0.01,
                        -2.5F + (float) entity.random.nextGaussian() * 5F, (10F + entity.random.nextFloat() * 15F) * side, casing.getName());
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 4) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 17) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
            if(timer == 31) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource());
        }
    };

    /**
     * Das Antimateriegewehr. Der Verschluss klackt zweimal je Schuss -- einmal beim Aufdrehen,
     * einmal beim Schliessen. Die Huelse fliegt nach vorn oben weg, weil der Verschluss sie beim
     * Zurueckziehen auswirft, nicht beim Schuss.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_AMAT = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.EQUIP) {
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 0.5F, 1.25F);
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 0.5F, 1.25F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 12) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.375, aiming ? 0 : -0.125, -0.25D, -0.05, 0.2, -0.025, 0.01,
                        -10F + (float) entity.random.nextGaussian() * 10F, (float) entity.random.nextGaussian() * 12.5F, casing.getName(), true, 60, 0.5D, 10);
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 32) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 41) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
        }
    };

    /** Die M2. Sie hat nichts als den Schuss: kein Nachladen, kein Durchladen, keinen Hahn. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_M2 = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.EQUIP) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.TURRET_HOWARD_RELOAD.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.375, aiming ? 0 : -0.125, aiming ? 0 : -0.3125D, 0, 0.06, -0.18, 0.01,
                        (float) entity.random.nextGaussian() * 20F, 12.5F + (float) entity.random.nextGaussian() * 7.5F, casing.getName());
            }
        }
    };

    /**
     * Die G3. Die Huelse fliegt beim Schuss nach rechts weg; angelegt liegt die Waffe hoeher,
     * deshalb sitzt der Auswurf dann anders. Die Zebra und jede G3 mit Optik gelten als angelegt,
     * auch wenn der Schuetze aus der Hueften schiesst -- durch das Rohr sieht man ohnehin nur
     * angelegt etwas, und ihr Auswurf soll nicht springen.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_G3 = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean scoped = stack.getItem() == NtmItems.GUN_G3_ZEBRA.get() || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
        boolean aiming = GunBaseNTItem.getIsAiming(stack) && !scoped;

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.5, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, 0.18, -0.12, 0.01,
                        (float) entity.random.nextGaussian() * 5F, 12.5F + entity.random.nextFloat() * 5F, casing.getName());
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 4) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 32) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 28) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Die StG 77. Zwei Sekunden nach jedem Schuss klackt die Sicherung zurueck -- das ist der
     * leise Schlag bei timer 40, den der Renderer als SAFETY mitfuehrt.
     *
     * ABWEICHUNG: das Original haelt hier zwei Zeitplaene und schaltet mit
     * ClientConfig.GUN_ANIMS_LEGACY um. Den Schalter gibt es im Port nicht; hier stehen die
     * Zeiten des neuen Satzes, die zu den Animationen aus der Datei passen.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_STG77 = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, aiming ? 0.125 : 0.25, aiming ? -0.125 : -0.25, aiming ? -0.125 : -0.25D, 0, 0.18, -0.12, 0.01,
                        (float) entity.random.nextGaussian() * 5F, 7.5F + entity.random.nextFloat() * 5F, casing.getName());
            }
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 0.25F, 1.25F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 0.25F, 1.25F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 32) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.25F, 1.25F);
            if(timer == 38) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 43) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 11) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 72) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
            if(timer == 84) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Karabiner. Die Huelse fliegt qualmend nach rechts vorn weg -- er ist die einzige
     * Handwaffe des Ports, deren Huelsen noch rauchen, wenn sie liegen.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_CARBINE = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 1) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.3125, aiming ? 0 : -0.125, aiming ? 0 : -0.25D, 0, 0.21, -0.06, 0.01,
                        -10F + (float) entity.random.nextGaussian() * 2.5F, 2.5F + (float) entity.random.nextGaussian() * 2F, casing.getName(), true, 60, 0.5D, 20);
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 8) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.RELOAD_END) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 31) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 6) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.9F);
        }
    };

    /**
     * Die MAS-36. Der Verschluss klackt zweimal je Schuss, beim Aufdrehen und beim Schliessen;
     * beim Ziehen klappt der Schaft hoerbar aus.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MAS36 = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack) && !XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);

        if(type == GunAnimation.EQUIP) {
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource());
            if(timer == 18) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 12) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity,
                        0.375, aiming ? 0 : -0.125, aiming ? 0 : -0.25D,
                        -0.05, 0.2, -0.025,
                        0.01, -10F + (float) entity.random.nextGaussian() * 10F, (float) entity.random.nextGaussian() * 12.5F, casing.getName(), true, 60, 0.5D, 10);
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 7) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource());
            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_RIFLE_COCK.get(), entity.getSoundSource());
            if(timer == 36) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 23) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_OPEN.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 17) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource(), 0.5F, 1F);
        }
    };

    /**
     * Die Minigun. Der Laufkranz schnarrt bei jedem Schuss -- dasselbe Geraeusch, das die
     * Revolvertrommel dreht, nur tiefer. Mit dem Geschwindigkeitsaufsatz fallen drei Huelsen je
     * Schuss statt einer, und das Schnarren kommt entsprechend spaeter.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MINIGUN = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
                int rounds = XWeaponModManager.hasUpgrade(stack, ctx.configIndex, XWeaponModManager.ID_MINIGUN_SPEED) ? 3 : 1;
                for(int i = 0; i < rounds; i++) {
                    SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                    if(casing != null) CasingCreator.composeEffect(level, entity, aiming ? 0.125 : 0.5, aiming ? -0.125 : -0.25, aiming ? -0.25 : -0.5D, 0, 0.18, -0.12, 0.01,
                            (float) entity.random.nextGaussian() * 15F, (float) entity.random.nextGaussian() * 15F, casing.getName());
                }
            }
            if(timer == (XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_MINIGUN_SPEED) ? 3 : 1)) {
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource(), 1F, 0.75F);
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 1) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource(), 1F, 0.75F);
        }
        if(type == GunAnimation.RELOAD || type == GunAnimation.INSPECT) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource(), 1F, 0.75F);
        }
    };

    /**
     * Die Doppel-Minigun. Wie die einzelne, nur dass die Huelsen je nach Haelfte nach links oder
     * nach rechts wegfliegen -- die linke Waffe wirft nach links aus, die rechte nach rechts.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MINIGUN_DUAL = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
                int side = ctx.configIndex == 0 ? -1 : 1;
                int rounds = XWeaponModManager.hasUpgrade(stack, ctx.configIndex, XWeaponModManager.ID_MINIGUN_SPEED) ? 3 : 1;
                for(int i = 0; i < rounds; i++) {
                    SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                    if(casing != null) CasingCreator.composeEffect(level, entity, 0.25, -0.25, -0.5D * side, 0, 0.18, -0.12 * side, 0.01,
                            (float) entity.random.nextGaussian() * 15F, (float) entity.random.nextGaussian() * 15F, casing.getName());
                }
            }
            if(timer == (XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_MINIGUN_SPEED) ? 3 : 1)) {
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource(), 1F, 0.75F);
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 1) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource(), 1F, 0.75F);
        }
        if(type == GunAnimation.RELOAD || type == GunAnimation.INSPECT) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_SPIN.get(), entity.getSoundSource(), 1F, 0.75F);
        }
    };

    /**
     * Die Doppelflinte. Beim Nachladen fliegen so viele Huelsen heraus, wie verschossen wurden --
     * die Zahl steht in der Luecke zwischen dem Stand nach dem Nachladen und dem davor. Sie
     * fliegen nach hinten unten weg, weil die Laeufe dabei nach oben geklappt sind.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_DOUBLE_BARREL = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 19) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 0.9F);
            if(timer == 29) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.8F);

            if(timer == 12) {
                IMagazine mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                int toEject = mag.getAmountAfterReload(stack) - mag.getAmount(stack, ctx.container);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) for(int i = 0; i < toEject; i++) CasingCreator.composeEffect(level, entity, 0, -0.1875, -0.375D, -0.24, 0.18, 0, 0.01,
                        -20F + (float) entity.random.nextGaussian() * 5F, (float) entity.random.nextGaussian() * 2.5F, casing.getName(), true, 60, 0.5D, 20);
            }
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 19) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.8F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Bolter. Er hat keinen Schlagbolzen und keinen Hahn -- nur das Magazin, das beim
     * Nachladen heraus- und wieder hereinschwenkt, und die Huelsen, die im Dauerfeuer wegfliegen.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_BOLTER = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 1) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.5, aiming ? 0 : -0.125, aiming ? -0.0625 : -0.25D, 0, 0.18, -0.12, 0.01,
                        -10F + (float) entity.random.nextGaussian() * 5F, 10F + entity.random.nextFloat() * 10F, casing.getName());
            }
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource());
            if(timer == 26) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Aberrator. Die Huelse fliegt nach rechts weg -- ausser bei der linken Haelfte der EOTT,
     * die spiegelverkehrt ist und deshalb nach links auswirft.
     *
     * ANGELEGT faellt die Spiegelung weg: dann wirft auch die linke Haelfte nach rechts aus. Das
     * ist im Original so und sieht nach einem Versehen aus -- die Klammersetzung dort laesst die
     * Seite nur im Hueftschuss durchschlagen. Uebernommen, weil es das Verhalten des Originals
     * ist und niemand zwei Waffen zugleich angelegt fuehrt.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_ABERRATOR = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.RELOAD) {
            if(timer == 5) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 32) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 42) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.75F);
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 1) {
                int side = (stack.getItem() == NtmItems.GUN_ABERRATOR_EOTT.get() && ctx.configIndex == 0) ? -1 : 1;
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.5, aiming ? 0 : -0.125, aiming ? -0.0625 : -0.25D * side, -0.05, 0.25, -0.05 * side, 0.01,
                        -10F + (float) entity.random.nextGaussian() * 10F, (float) entity.random.nextGaussian() * 12.5F, casing.getName());
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 1) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 9) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_PISTOL_COCK.get(), entity.getSoundSource(), 1F, 0.75F);
        }
    };

    /**
     * Die Leuchtpistole. Die leere Huelse fliegt beim Nachladen heraus -- aber nur, wenn
     * ueberhaupt eine drin war; setAmountBeforeReload(0) sorgt dafuer, dass sie nur einmal
     * ausgeworfen wird und nicht bei jedem Durchlauf der Nachladeschleife erneut.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_FLAREGUN = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 4) {
                IMagazine mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                if(mag.getAmountAfterReload(stack) > 0) {
                    SpentCasing casing = mag.getCasing(stack, ctx.container);
                    if(casing != null) CasingCreator.composeEffect(level, entity, 0.625, -0.125, aiming ? -0.125 : -0.375D, -0.12, 0.18, 0, 0.01,
                            -15F + (float) entity.random.nextGaussian() * 7.5F, (float) entity.random.nextGaussian() * 5F, casing.getName(), true, 60, 0.5D, 20);
                    mag.setAmountBeforeReload(stack, 0);
                }
            }
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_CANISTER_INSERT.get(), entity.getSoundSource());
            if(timer == 24) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.JAMMED) {
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 0.8F);
            if(timer == 29) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE) {
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Congo Lake. Die Huelse fliegt nicht beim Schuss heraus, sondern fuenfzehn Ticks
     * spaeter -- erst muss die Pumpe zurueck. Beim Nachladen klackt es fuer jede einzelne
     * Granate, weil RELOAD_CYCLE je Schuss einmal laeuft.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_CONGOLAKE = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 15) {
                IMagazine mag = ctx.config.getReceivers(stack)[0].getMagazine(stack);
                SpentCasing casing = mag.getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.625, aiming ? -0.0625 : -0.25, aiming ? 0 : -0.375D, 0, 0.18, 0.12, 0.01,
                        -5F + (float) entity.random.nextGaussian() * 3.5F, -10F + entity.random.nextFloat() * 5F, casing.getName(), true, 60, 0.5D, 20);
            }
        }
        if(type == GunAnimation.RELOAD || type == GunAnimation.RELOAD_CYCLE) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_GRENADE_RELOAD.get(), entity.getSoundSource());
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 9) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_GRENADE_OPEN.get(), entity.getSoundSource());
            if(timer == 27) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_GRENADE_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Die MK 108. Beim Nachladen klackt es sechsmal -- Deckel auf, alter Gurt heraus, Trommel
     * heraus, Trommel aufgesetzt, neuer Gurt eingelegt, Deckel zu. Auf der Haelfte des Weges
     * traegt das Magazin die neue Munition ein (reloadAction), damit der Renderer den Gurt schon
     * voll zeichnet, waehrend der Deckel noch offen steht.
     *
     * BEIM BETRACHTEN klatscht jede der drei geworfenen Granaten einmal in die Hand zurueck --
     * dreimal derselbe Ton, um jeweils eine Viertelsekunde versetzt.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_MK108 = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 2) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.5, aiming ? -0.125 : -0.3125, aiming ? -0.375 : -0.3125D, 0, 0.18, -0.12, 0.01,
                        -10F + (float) entity.random.nextGaussian() * 2.5F, (float) entity.random.nextGaussian() * -20F + 15F, casing.getName(), true, 60, 0.5D, 10);
            }
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.65F);
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 60) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 90) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 100) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 125) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.65F);

            if(timer == 60) ctx.config.getReceivers(stack)[0].getMagazine(stack).reloadAction(stack, ctx.container);
        }
        if(type == GunAnimation.INSPECT) {

            int yeetHorizontal = 750;
            int untilImpact = yeetHorizontal * 9 / 15;
            int delay = 250;

            for(int i = 0; i < 3; i++) {
                if(timer == (untilImpact + delay * i) / 50) {
                    SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.5F, 1.5F);
                }
            }
        }
    };

    /** Die Autoschrotflinte, Orchestras Z. 1064 des Originals. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_SHREDDER = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHREDDER_CYCLE.get(), entity.getSoundSource(), 0.25F, 1.5F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHREDDER_CYCLE.get(), entity.getSoundSource(), 0.25F, 1.5F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 32) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.INSPECT) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 28) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource(), 1F, 1F);
        }
    };

    /**
     * Die schoene Autoschrotflinte, Orchestras Z. 1088 des Originals.
     *
     * WAS DIE SIGNATURPATRONE KOSTET: schiesst sie, wird der Zeitgeber auf zwanzig Ticks
     * gesetzt. Das ist die Pause, die das Luftschiff braucht -- eine Sekunde Stillstand fuer
     * eine Sekunde Schauspiel.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_SHREDDER_SEXY = (stack, ctx) -> {
        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);
        boolean aiming = GunBaseNTItem.getIsAiming(stack);

        if(type == GunAnimation.CYCLE) {
            if(timer == 0) {
                PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
                if(ctx.config.getReceivers(stack)[0].getMagazine(stack).getType(stack, null) == XFactory12ga.g12_equestrian_bj) {
                    GunBaseNTItem.setTimer(stack, 0, 20);
                }
            }

            if(timer == 2) {
                SpentCasing casing = ctx.config.getReceivers(stack)[0].getMagazine(stack).getCasing(stack, ctx.container);
                if(casing != null) CasingCreator.composeEffect(level, entity, 0.375, aiming ? -0.0625 : -0.125, aiming ? -0.125 : -0.25D, 0, 0.18, -0.12, 0.01,
                        -10F + (float) entity.random.nextGaussian() * 2.5F, (float) entity.random.nextGaussian() * -20F + 15F, casing.getName(), false, 60, 0.5D, 20);
            }
        }

        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 1F);
        }
        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 4) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 16) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_REMOVE.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 55) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 65) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 74) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 1F);
            if(timer == 88) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 100) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource(), 1F, 1F);

            /* Der Gurt wird in der Mitte der Bewegung nachgelegt, nicht am Ende. */
            if(timer == 55) ctx.config.getReceivers(stack)[0].getMagazine(stack).reloadAction(stack, ctx.container);
        }
    };

    /**
     * Der Flammenwerfer. Anders als alle uebrigen Orchester hier macht dieses das Meiste auf
     * der Seite des Zuschauers: ein Flammenwerfer feuert jeden Tick, ein Schussgeraeusch je
     * Schuss waere ein Presslufthammer. Stattdessen laeuft ein Dauerton, solange der
     * Abschussvorgang laeuft, und verstummt, sobald er stockt.
     *
     * DER TON HAELT SICH SELBST AM LEBEN. keepAlive(10) heisst: er verstummt von allein zehn
     * Ticks nach dem letzten Lebenszeichen. Solange der Abschussvorgang laeuft, kommt alle
     * paar Ticks eines; hoert der Spieler auf zu schiessen, hoert auch der Ton auf -- ohne
     * dass irgendwer ihn ausdruecklich abstellen muesste.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_FLAMER = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(level.isClientSide) {

            AudioWrapper laufend = GunBaseNTItem.loopedSounds.get(entity);

            if(type == GunAnimation.CYCLE && timer < 5) {

                if(laufend == null || !laufend.isPlaying()) {
                    AudioWrapper ton = AudioWrapper.getLoopedSound(NtmSoundEvents.GUN_FLAMER_LOOP.get(), entity.getSoundSource(),
                            (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), 1F, 15F, 1F, 10);
                    GunBaseNTItem.loopedSounds.put(entity, ton);
                    ton.startSound();
                    ton.attachTo(entity);
                } else {
                    laufend.keepAlive();
                }

            } else if(laufend != null && laufend.isPlaying()) {
                laufend.stopSound();
            }

            return;
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource());
            if(timer == 35) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 60) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 70) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_CANISTER_INSERT.get(), entity.getSoundSource());
            if(timer == 85) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_VALVE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Daybreaker. Er wirft Brandbomben statt eines Strahls und hat deshalb keinen
     * Dauerton -- nur die fuenf Griffe des Nachladens.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_FLAMER_DAYBREAKER = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource());
            if(timer == 35) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 60) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 70) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_CANISTER_INSERT.get(), entity.getSoundSource());
            if(timer == 85) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_VALVE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Bohrer. Sein Klang haengt nicht an einer Animationsstufe, sondern am Kanal SPEED der
     * laufenden Bewegung: solange der Bohrer dreht, laeuft der Motor, und Lautstaerke wie
     * Tonhoehe folgen der Drehzahl.
     *
     * DAS ABSTELLEN IST BEWUSST NICHT IMPLEMENTIERT, wie im Original. Der Ton haelt sich ueber
     * keepAlive(25) selbst am Leben und verstummt von allein; ein ausdrueckliches stopSound an
     * dieser Stelle liess ihn im Original stottern.
     *
     * NICHT UEBERNOMMEN: die Umschaltung auf den Turbinenklang, wenn ein Elektromotor
     * eingebaut ist. Den Aufsatz ENGINE_ELECTRIC gibt es im Port nicht.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_DRILL = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(level.isClientSide) {

            float speed = HbmAnimations.getRelevantTransformation("SPEED")[0];
            AudioWrapper laufend = GunBaseNTItem.loopedSounds.get(entity);

            if(speed > 0F) {
                if(laufend == null || !laufend.isPlaying()) {
                    AudioWrapper ton = AudioWrapper.getLoopedSound(NtmSoundEvents.ENGINE_LOOP.get(), entity.getSoundSource(),
                            (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), speed, 15F, speed, 25);
                    GunBaseNTItem.loopedSounds.put(entity, ton);
                    ton.startSound();
                    ton.attachTo(entity);
                } else {
                    laufend.keepAlive();
                    laufend.updateVolume(speed);
                    laufend.updatePitch(speed);
                }
            }

            if(type != GunAnimation.CYCLE && type != GunAnimation.CYCLE_DRY && laufend != null && laufend.isPlaying()) {
                laufend.stopSound();
            }

            return;
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_LATCH_OPEN.get(), entity.getSoundSource());
            if(timer == 35) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.5F, 1F);
            if(timer == 60) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 0.75F);
            if(timer == 70) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_CANISTER_INSERT.get(), entity.getSoundSource());
            if(timer == 85) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_VALVE.get(), entity.getSoundSource());
        }
    };

    /**
     * Die drei Laserpistolen. Der Laser hat keinen Auswurf und keine Huelse -- was hier
     * passiert, ist die Muendungswolke beim Schuss und die vier Griffe des Magazinwechsels.
     */
    /**
     * Der Ladungswerfer. Zwei Toene beim Nachladen -- Ladung hinein, Verschluss zu -- und ein
     * dritter, der nur dann kommt, wenn er etwas zu sagen hat: der Leerschlag.
     *
     * DER LEERSCHLAG SCHWEIGT, SOLANGE EIN HAKEN HAENGT. Wer am Seil haengt und die Taste
     * gedrueckt haelt, zieht sich heran -- das ist kein Fehlschuss, und es soll nicht klicken.
     */
    /**
     * Der Feuerloescher hat genau einen Klang ausserhalb des Schiessens: das Ventil, wenn man
     * den Tank wechselt. Mehr macht das Original auch nicht.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_FIREEXT = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD && timer == 0) {
            SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_VALVE.get(), entity.getSoundSource());
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_CHARGE_THROWER = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE_DRY && timer == 0) {
            if(level.getEntity(GunChargeThrowerItem.getLastHook(stack)) == null) {
                SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 0.75F);
            }
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_ROCKET_INSERT.get(), entity.getSoundSource());
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_BOLT_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Die Folly laedt acht Sekunden lang nach, und in dieser Zeit fallen genau drei Toene:
     * der Verschluss auf, die Granate hinein, der Verschluss zu.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_FOLLY = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.RELOAD) {
            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SCREW.get(), entity.getSoundSource());
            if(timer == 80) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_ROCKET_INSERT.get(), entity.getSoundSource());
            if(timer == 120) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SCREW.get(), entity.getSoundSource());
        }
    };

    /**
     * Die Teslakanone. Sie laedt nie nach, also hat sie nur drei Toene: das Weiterdrehen des
     * Zahnrads beim Schuss, den Leerschlag, und -- beim Begutachten -- das Quietschtier.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_TESLA = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE && timer == 2) {
            SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHREDDER_CYCLE.get(), entity.getSoundSource(), 0.25F, 1.25F);
        }
        if(type == GunAnimation.CYCLE_DRY) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource());
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_SHREDDER_CYCLE.get(), entity.getSoundSource(), 0.25F, 1.25F);
        }
        if(type == GunAnimation.INSPECT && timer == 12) {
            SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.SQUEAKY_TOY.get(), entity.getSoundSource(), 0.25F, 1F);
        }
    };

    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_LASER_PISTOL = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE && timer == 0) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }
        if(type == GunAnimation.CYCLE_DRY && timer == 0) {
            SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 1.5F);
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 0) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource(), 1F, 1.25F);
            if(timer == 34) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_INSERT.get(), entity.getSoundSource(), 1F, 1.25F);
            if(timer == 40) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.25F);
        }

        /* RUNDE 189 NACHGETRAGEN: das Klemmen. Die Bewegung dazu gab es seit Runde 187 nicht,
         * weil die Bewegungsvorschrift der Waffe erfunden statt uebertragen war -- und mit ihr
         * fehlte hier der Ton. Beides steht jetzt so da wie im Original. */
        if(type == GunAnimation.JAMMED) {
            if(timer == 10) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_COCK.get(), entity.getSoundSource());
            if(timer == 15) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource(), 1F, 1.25F);
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.25F, 1.5F);
        }
    };

    /** Das Lasergewehr. Dasselbe Muster, aber mit dem grossen Magazin und dem Hebel. */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_LASRIFLE = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(!(level instanceof ServerLevel serverLevel)) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE && timer == 0) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), 100, new MuzzleFlashPacket(entity.getId()));
        }
        if(type == GunAnimation.CYCLE_DRY && timer == 0) {
            SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_DRY_FIRE.get(), entity.getSoundSource(), 1F, 1.5F);
        }

        if(type == GunAnimation.RELOAD) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 18) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_IMPACT.get(), entity.getSoundSource(), 0.25F, 1F);
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 38) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }

        /* RUNDE 189 NACHGETRAGEN, aus demselben Grund wie bei der Laserpistole. */
        if(type == GunAnimation.INSPECT) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 12) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 20) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }

        if(type == GunAnimation.JAMMED) {
            if(timer == 2) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_SMALL_REMOVE.get(), entity.getSoundSource());
            if(timer == 22) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_MAG_INSERT.get(), entity.getSoundSource());
            if(timer == 30) SoundUtils.playAtVec3(level, entity.position(), NtmSoundEvents.GUN_REVOLVER_CLOSE.get(), entity.getSoundSource());
        }
    };

    /**
     * Der Chemiewerfer. Wie der Flammenwerfer ein Dauerton, solange gefeuert wird, und sonst
     * nichts -- er hat kein Nachladen.
     */
    public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA_CHEMTHROWER = (stack, ctx) -> {

        LivingEntity entity = ctx.entity;
        if(!entity.level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        AudioWrapper laufend = GunBaseNTItem.loopedSounds.get(entity);

        if(type == GunAnimation.CYCLE && timer < 5) {

            if(laufend == null || !laufend.isPlaying()) {
                AudioWrapper ton = AudioWrapper.getLoopedSound(NtmSoundEvents.GUN_FLAMER_LOOP.get(), entity.getSoundSource(),
                        (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), 1F, 15F, 1F, 10);
                GunBaseNTItem.loopedSounds.put(entity, ton);
                ton.startSound();
                ton.attachTo(entity);
            } else {
                laufend.keepAlive();
                laufend.attachTo(entity);
            }

        } else if(laufend != null && laufend.isPlaying()) {
            laufend.stopSound();
        }
    };
}
