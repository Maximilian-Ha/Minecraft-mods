package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderCarbine.
 *
 * Der Karabiner. Beweglich sind der Verschluss (SLIDE), das Roehrenmagazin (MAG) und die Patrone
 * am Auswerfer (REL); STAB ist der Stoss des Bajonetts, LIFT das Anheben beim Nachladen.
 *
 * DIE VISIERUNG haengt am Aufsatz: ohne Zielfernrohr steht die Kimme, mit Rohr steht das Rohr.
 * Ganz durch das Rohr gesehen wird die Waffe gar nicht gezeichnet.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderCarbine extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * (isScoped(stack) ? 0.66 : 0.33));
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1.5F * offset, 0.875F * offset,
                0F, isScoped(stack) ? (-8F / 8F) : (-6.25F / 8F), 0.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        boolean isScoped = isScoped(stack);
        if(isScoped && GunBaseNTItem.prevAimingProgress == 1 && GunBaseNTItem.aimingProgress == 1) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.CARBINE_TEX);

        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] slide = HbmAnimations.getRelevantTransformation("SLIDE");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");
        float[] rel = HbmAnimations.getRelevantTransformation("REL");
        float[] stab = HbmAnimations.getRelevantTransformation("STAB");

        RenderContext.translate(0F, -1F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 2F);

        RenderContext.translate(0F, 0F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, 2F);

        RenderContext.translate(stab[0], stab[1], stab[2]);
        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.carbine.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, slide[2]);
        ResourceManager.carbine.renderPart("Slide");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        ResourceManager.carbine.renderPart("Magazine");
        RenderContext.translate(rel[0], rel[1], rel[2]);
        if(bullet[0] != 1) ResourceManager.carbine.renderPart("Bullet");
        RenderContext.popPose();

        if(!isScoped) {
            ResourceManager.carbine.renderPart("IronSight");
        } else {
            RenderSystem.setShaderTexture(0, ResourceManager.CARBINE_SCOPE_TEX);
            ResourceManager.carbine.renderPart("Scope");
        }

        if(hasBayonet(stack)) {
            RenderSystem.setShaderTexture(0, ResourceManager.CARBINE_BAYONET_TEX);
            ResourceManager.carbine.renderPart("Bayonet");
        }

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.25F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0F, 2F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);

        /* Mit Bajonett ist die Waffe laenger und muss deshalb kleiner gezeichnet werden. */
        boolean bayonet = hasBayonet(stack);
        float scale = bayonet ? 1.1875F : 1.375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(bayonet ? 1.5F : -0.5F, 0F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.CARBINE_TEX);
        ResourceManager.carbine.renderPart("Gun");
        ResourceManager.carbine.renderPart("Slide");
        ResourceManager.carbine.renderPart("Magazine");

        if(!isScoped(stack)) {
            ResourceManager.carbine.renderPart("IronSight");
        } else {
            RenderSystem.setShaderTexture(0, ResourceManager.CARBINE_SCOPE_TEX);
            ResourceManager.carbine.renderPart("Scope");
        }

        if(hasBayonet(stack)) {
            RenderSystem.setShaderTexture(0, ResourceManager.CARBINE_BAYONET_TEX);
            ResourceManager.carbine.renderPart("Bayonet");
        }

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;
            float shotRand = 0F;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 8F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            RenderContext.scale(0.5F, 0.5F, 0.5F);
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();
        }
    }

    public boolean isScoped(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
    }

    public boolean hasBayonet(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_CARBINE_BAYONET);
    }
}
