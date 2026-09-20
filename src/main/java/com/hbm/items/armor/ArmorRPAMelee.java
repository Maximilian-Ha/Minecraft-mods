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

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorRPAMelee.
 *
 * DIE FAEUSTE DER REMNANT-RUESTUNG. Linke Taste ist ein Doppelschlag, rechte Taste eine
 * einzelne Ohrfeige -- die trifft haerter, schlaegt zurueck und durchdringt mehr Panzerung.
 *
 * DER SCHADEN ENTSTEHT NICHT BEIM KLICKEN, SONDERN IM TAKT. Die Orchestra laeuft jeden Zug
 * mit und prueft, ob die Bewegung gerade an der Stelle ist, an der die Faust ankommt: beim
 * Doppelschlag in Zug 3 und 9, bei der Ohrfeige in Zug 8. Erst dann wird geschaut, was vor
 * der Faust steht. Deshalb trifft man, was beim Aufschlag davorsteht, nicht was beim Klicken
 * davorstand.
 *
 * WER GROSS IST, BEKOMMT MEHR AB: ab hundert Lebenspunkten das Zweieinhalbfache. Das ist die
 * Regel des Originals und der Grund, warum diese Faeuste gegen Bosse etwas taugen.
 *
 * NACHSCHLAGEN: haelt man die linke Taste gedrueckt, setzt die Orchestra in Zug 14 den
 * naechsten Doppelschlag selbst an. Ohne diese Zeile muesste man fuer jeden Schlag neu
 * klicken.
 */
public class ArmorRPAMelee implements IPAMelee {

    @Override public void clickPrimary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, GunAnimation.CYCLE, 14); }
    @Override public void clickSecondary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, GunAnimation.ALT_CYCLE, 20); }

    @Override
    public void orchestra(ItemStack stack, LambdaContext ctx) {

        LivingEntity entity = ctx.entity;
        Level level = entity.level;
        if(level.isClientSide) return;

        GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
        int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

        if(type == GunAnimation.CYCLE && timer == 14 && GunBaseNTItem.getPrimary(stack, 0)) {
            XFactoryPA.doSwing(stack, ctx, GunAnimation.CYCLE, 14);
        }

        boolean schlag = type == GunAnimation.CYCLE && (timer == 3 || timer == 9);
        boolean ohrfeige = type == GunAnimation.ALT_CYCLE && timer == 8;

        Player spieler = ctx.getPlayer();
        if((!schlag && !ohrfeige) || spieler == null) return;

        HitResult treffer = EntityDamageUtil.getMouseOver(spieler, 3.0D, 0.5D);
        if(treffer == null) return;

        if(treffer instanceof EntityHitResult ehr) {

            float schaden = schlag ? 15F : 35F;
            float rueckstoss = schlag ? 0F : 1.5F;
            float dt = schlag ? 5F : 15F;
            float durchschlag = schlag ? 0.1F : 0.25F;

            Entity ziel = ehr.getEntity();

            if(ziel instanceof LivingEntity lebewesen) {
                if(lebewesen.getMaxHealth() >= 100F) schaden *= 2.5F;
                EntityDamageUtil.hurtNT(lebewesen, level.damageSources().playerAttack(spieler), schaden,
                        true, false, rueckstoss, dt, durchschlag);
                /* Wer durch die Faust stirbt, zerfaellt mit einer gewissen Wahrscheinlichkeit. */
                if(lebewesen.getRandom().nextInt(ohrfeige ? 3 : 10) == 0 && !lebewesen.isAlive()) ConfettiUtil.gib(lebewesen);
            } else {
                ziel.hurt(level.damageSources().playerAttack(spieler), schaden);
            }

            SoundUtils.playAtVec3(level, ziel.position(), NtmSoundEvents.GUN_SMACK.get(), SoundSource.PLAYERS,
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
                .addBus("EQUIP", new BusAnimationSequence().setPos(-1, 0, 0).addPos(0, 0, 0, 250, IType.SIN_DOWN));

        if(type == GunAnimation.CYCLE) return new BusAnimation()
                .addBus("SWINGRIGHT", new BusAnimationSequence().addPos(1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SWINGLEFT", new BusAnimationSequence().addPos(0, 0, 0, 300).addPos(1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));

        if(type == GunAnimation.ALT_CYCLE) return new BusAnimation()
                .addBus("SLAPTURN", new BusAnimationSequence().addPos(1, 0, 0, 250, IType.LINEAR).hold(150).addPos(0, 0, 0, 350, IType.LINEAR))
                .addBus("SLAP", new BusAnimationSequence().hold(250).addPos(1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL));

        return null;
    }

    @Override @OnlyIn(Dist.CLIENT) public void setupFirstPerson(ItemStack stack) { }

    /**
     * Gezeichnet werden zwei Arme, und zwar die des Ruestungsmodells -- nicht die des
     * Spielers. Jeder Arm bekommt dieselbe Rechnung spiegelbildlich: erst die Verschiebung
     * durch die laufende Bewegung, dann die Vorneigung, dann das Ausstellen nach aussen.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        RenderSystem.setShaderTexture(0, ResourceManager.RPA_ARM);

        RenderContext.translate(0F, -1.5F, 0.5F);
        float scale = 0.125F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float swingRight = HbmAnimations.getRelevantTransformation("SWINGRIGHT")[0];
        float swingLeft = HbmAnimations.getRelevantTransformation("SWINGLEFT")[0];
        float slapTurn = HbmAnimations.getRelevantTransformation("SLAPTURN")[0];
        float slap = HbmAnimations.getRelevantTransformation("SLAP")[0];

        float vorneigung = 60F - 60F * equip[0];
        float nachAussen = 3F;
        float rollen = 60F;

        RenderContext.pushPose();
        RenderContext.translate(-12F * swingLeft + 2F * slapTurn - 5F * slap, 6F * slap, 5F * swingLeft + 8F * slap);
        RenderContext.mulPose(Axis.XP.rotationDegrees(vorneigung - swingRight * 20F));
        RenderContext.translate(nachAussen, 0F, 0F);
        RenderContext.translate(6F, 8F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(60F * swingLeft + 45F * slap));
        RenderContext.mulPose(Axis.YP.rotationDegrees(rollen + 15F * swingLeft + 45F * slapTurn));
        RenderContext.translate(-6F, -8F, 0F);
        ResourceManager.armor_remnant.renderPart("LeftArm");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(12F * swingRight - 2F * slapTurn + 5F * slap, 6F * slap, 5F * swingRight + 8F * slap);
        RenderContext.mulPose(Axis.XP.rotationDegrees(vorneigung - swingLeft * 20F));
        RenderContext.translate(-nachAussen, 0F, 0F);
        RenderContext.translate(-6F, 8F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-60F * swingRight - 45F * slap));
        RenderContext.mulPose(Axis.YP.rotationDegrees(-rollen - 15F * swingRight - 45F * slapTurn));
        RenderContext.translate(6F, -8F, 0F);
        ResourceManager.armor_remnant.renderPart("RightArm");
        RenderContext.popPose();
    }
}
