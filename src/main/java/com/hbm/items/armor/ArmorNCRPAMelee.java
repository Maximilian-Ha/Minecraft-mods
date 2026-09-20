package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.factory.ConfettiUtil;
import com.hbm.items.weapon.sedna.factory.XFactoryPA;
import com.hbm.main.ResourceManager;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.SoundUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorNCRPAMelee.
 *
 * DIE KLINGEN DER NCR-RUESTUNG. Dieselbe Bauart wie die Faeuste der Remnant, aber langsamer
 * und mit zwei Unterschieden, die beide im Original stehen:
 *
 *   * KEIN NACHSCHLAGEN. Wer die Taste gedrueckt haelt, schlaegt trotzdem nur einmal -- die
 *     Zeile, die bei der Remnant den naechsten Doppelschlag selbst ansetzt, fehlt hier.
 *   * WER DARAN STIRBT, ZERFAELLT IMMER. Bei den Faeusten ist es ein Wuerfelwurf; hier nicht.
 *     Klingen sind Klingen.
 *
 * Ausserdem trifft die Klinge nur, was noch lebt: das Original fragt vor dem Schaden
 * ausdruecklich isEntityAlive ab. Bei den Faeusten steht diese Abfrage nicht.
 */
public class ArmorNCRPAMelee implements IPAMelee {

    @Override public void clickPrimary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, GunAnimation.CYCLE, 25); }
    @Override public void clickSecondary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, GunAnimation.ALT_CYCLE, 30); }

    @Override
    public void orchestra(ItemStack stack, LambdaContext ctx) {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        boolean schnitt = type == GunAnimation.CYCLE && (timer == 5 || timer == 15);
        boolean streich = type == GunAnimation.ALT_CYCLE && timer == 5;

        Player spieler = ctx.getPlayer();
        if((!schnitt && !streich) || spieler == null) return;

        HitResult treffer = EntityDamageUtil.getMouseOver(spieler, 3.0D, 0.5D);
        if(treffer == null) return;

        if(treffer instanceof EntityHitResult ehr && ehr.getEntity().isAlive()) {

            float schaden = schnitt ? 15F : 35F;
            float rueckstoss = schnitt ? 0F : 1.5F;
            float dt = schnitt ? 5F : 15F;
            float durchschlag = schnitt ? 0.1F : 0.25F;

            Entity ziel = ehr.getEntity();

            if(ziel instanceof LivingEntity lebewesen) {
                if(lebewesen.getMaxHealth() >= 100F) schaden *= 2.5F;
                EntityDamageUtil.hurtNT(lebewesen, level.damageSources().playerAttack(spieler), schaden,
                        true, false, rueckstoss, dt, durchschlag);
                if(!lebewesen.isAlive()) ConfettiUtil.gib(lebewesen);
            } else {
                ziel.hurt(level.damageSources().playerAttack(spieler), schaden);
            }

            SoundUtils.playAtVec3(level, ziel.position(), NtmSoundEvents.GUN_STAB.get(), SoundSource.PLAYERS,
                    1F, 0.9F + level.random.nextFloat() * 0.2F);
        }

        if(treffer instanceof BlockHitResult bhr) {
            BlockPos ort = bhr.getBlockPos();
            BlockState zustand = level.getBlockState(ort);
            SoundUtils.playAtVec3(level, bhr.getLocation(),
                    zustand.getBlock().getSoundType(zustand, level, ort, spieler).getStepSound(), SoundSource.BLOCKS,
                    2F, 0.9F + level.random.nextFloat() * 0.2F);
        }
    }

    @Override
    public BusAnimation playAnim(ItemStack stack, GunAnimation type) {

        if(type == GunAnimation.EQUIP) return new BusAnimation()
                .addBus("EQUIP", new BusAnimationSequence().setPos(-1, 0, 0).addPos(0, 0, 0, 750, IType.SIN_DOWN));

        if(type == GunAnimation.CYCLE) return new BusAnimation()
                .addBus("SWINGRIGHT", new BusAnimationSequence().addPos(1, 0, 0, 250, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SWINGLEFT", new BusAnimationSequence().addPos(0, 0, 0, 500).addPos(1, 0, 0, 250, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL));

        if(type == GunAnimation.ALT_CYCLE) return new BusAnimation()
                .addBus("SWEEPTURN", new BusAnimationSequence().addPos(1, 0, 0, 100, IType.LINEAR).hold(350).addPos(0, 0, 0, 500, IType.LINEAR))
                .addBus("SWEEPCUT", new BusAnimationSequence().hold(100).addPos(1, 0, 0, 250, IType.SIN_DOWN).hold(100).addPos(0, 0, 0, 500, IType.SIN_FULL));

        return null;
    }

    @Override @OnlyIn(Dist.CLIENT) public void setupFirstPerson(ItemStack stack) { }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        RenderSystem.setShaderTexture(0, ResourceManager.NCRPA_ARM);

        RenderContext.translate(0F, -1.5F, 0.5F);
        float scale = 0.125F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float swingRight = HbmAnimations.getRelevantTransformation("SWINGRIGHT")[0];
        float swingLeft = HbmAnimations.getRelevantTransformation("SWINGLEFT")[0];
        float sweepTurn = HbmAnimations.getRelevantTransformation("SWEEPTURN")[0];
        float sweepCut = HbmAnimations.getRelevantTransformation("SWEEPCUT")[0];

        float vorneigung = 60F - 60F * equip[0];
        float nachAussen = 3F;
        float rollen = 60F;

        RenderContext.pushPose();
        RenderContext.translate(-14F * swingLeft - 4F * sweepTurn, 6F * sweepCut, 2F * swingLeft + 8F * sweepCut);
        RenderContext.mulPose(Axis.XP.rotationDegrees(vorneigung + swingRight * 40F - 60F * sweepCut));
        RenderContext.translate(nachAussen, 0F, 0F);
        RenderContext.translate(6F, 8F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(90F * swingLeft));
        RenderContext.mulPose(Axis.YP.rotationDegrees(rollen + 30F * swingLeft - 90F * sweepTurn));
        RenderContext.translate(-6F, -8F, 0F);
        ResourceManager.armor_ncrpa.renderPart("LeftArm");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(14F * swingRight + 4F * sweepTurn, 6F * sweepCut, 2F * swingRight + 8F * sweepCut);
        RenderContext.mulPose(Axis.XP.rotationDegrees(vorneigung + swingLeft * 40F - 60F * sweepCut));
        RenderContext.translate(-nachAussen, 0F, 0F);
        RenderContext.translate(-6F, 8F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-90F * swingRight));
        RenderContext.mulPose(Axis.YP.rotationDegrees(-rollen - 30F * swingRight + 90F * sweepTurn));
        RenderContext.translate(6F, -8F, 0F);
        ResourceManager.armor_ncrpa.renderPart("RightArm");
        RenderContext.popPose();
    }
}
