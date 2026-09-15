package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderGreasegun.
 *
 * ABWEICHUNG: das Original schaltet auf eine zweite Textur um, wenn die Waffe den Aufsatz
 * "gereinigt" traegt. Die Waffenaufsaetze sind noch nicht portiert, also bleibt es bei der einen.
 */
public class ItemRenderGreasegun extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1F * offset, 1.75F * offset,
                0, -2.625F / 8F, 1.125F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, 1F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 2F, 0F);
    }

    @Override
    public void setupEntity(ItemStack stack) {
        float scale = 0.0625F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.GREASEGUN_TEX);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] stock = HbmAnimations.getRelevantTransformation("STOCK");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] flap = HbmAnimations.getRelevantTransformation("FLAP");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] handle = HbmAnimations.getRelevantTransformation("HANDLE");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] turn = HbmAnimations.getRelevantTransformation("TURN");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");

        RenderContext.translate(0F, -3F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 3F, 3F);

        RenderContext.translate(0F, -3F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 3F, 3F);

        if(GunBaseNTItem.aimingProgress < 1F) RenderContext.mulPose(Axis.ZP.rotationDegrees(turn[2]));

        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.greasegun.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, -4F - stock[2]);
        ResourceManager.greasegun.renderPart("Stock");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        ResourceManager.greasegun.renderPart("Magazine");
        if(bullet[0] != 1) ResourceManager.greasegun.renderPart("Bullet");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, -1.4375F, -0.125F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(handle[0]));
        RenderContext.translate(0F, 1.4375F, 0.125F);
        ResourceManager.greasegun.renderPart("Handle");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.53125F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(flap[2]));
        RenderContext.translate(0F, -0.5125F, 0F);
        ResourceManager.greasegun.renderPart("Flap");
        RenderContext.popPose();

        float smokeScale = 0.25F;

        RenderContext.pushPose();
        RenderContext.translate(-0.25F, 0F, 1.5F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-turn[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 1F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 8F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-turn[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 1F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.GREASEGUN_TEX);
        ResourceManager.greasegun.renderAll();

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;
            float shotRand = 0;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 8F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            RenderContext.scale(0.5F, 0.5F, 0.5F);
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();
        }
    }
}
