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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderStarFAkimbo.
 *
 * Zwei Star-F nebeneinander, beide in der Elite-Ausfuehrung -- das Paar ist nicht dieselbe Waffe
 * doppelt, sondern eine eigene. Der Schalldaempfer sitzt je Haelfte fuer sich; er borgt sich sein
 * Modellteil von der Uzi, so wie im Original.
 *
 * ABWEICHUNG: in dritter Person zeichnet der Port nur die Waffe in der Haupthand. Die zweite
 * haengt im Original an RenderPlayerEvent.Specials, einer Ereignisreihe, die es in 1.21 nicht
 * mehr gibt; der Port hat die zugehoerige Schicht noch gar nicht.
 */
public class ItemRenderStarFAkimbo extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, -0.25F, 1.75F);
        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.125F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        float offset = 0.8F;

        for(int side = -1; side <= 1; side += 2) {

            int index = side == -1 ? 0 : 1;

            RenderContext.pushPose();
            RenderSystem.setShaderTexture(0, ResourceManager.STAR_F_ELITE_TEX);

            standardAimingTransform(stack,
                    -2F * offset * side, -1.75F * offset, 2.5F * offset,
                    0F, -7.625F / 8F, 1F);

            float scale = 0.25F;
            RenderContext.scale(scale, scale, scale);

            float[] equip = HbmAnimations.getRelevantTransformation("EQUIP", index);
            float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL", index);
            float[] hammer = HbmAnimations.getRelevantTransformation("HAMMER", index);
            float[] tilt = HbmAnimations.getRelevantTransformation("TILT", index);
            float[] turn = HbmAnimations.getRelevantTransformation("TURN", index);
            float[] mag = HbmAnimations.getRelevantTransformation("MAG", index);
            float[] bullet = HbmAnimations.getRelevantTransformation("BULLET", index);
            float[] slide = HbmAnimations.getRelevantTransformation("SLIDE", index);

            RenderContext.translate(0F, -2F, -8F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
            RenderContext.translate(0F, 2F, 8F);

            RenderContext.translate(0F, 1F, -3F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(turn[2] * side));
            RenderContext.mulPose(Axis.XP.rotationDegrees(tilt[0]));
            RenderContext.translate(0F, -1F, 3F);

            RenderContext.translate(0F, 0F, recoil[2]);

            ResourceManager.star_f.renderPart("Gun");

            RenderContext.pushPose();
            RenderContext.translate(0F, 1.75F, -4.25F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(60F * (hammer[0] - 1F)));
            RenderContext.translate(0F, -1.75F, 4.25F);
            ResourceManager.star_f.renderPart("Hammer");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, slide[2] * 2.3125F);
            ResourceManager.star_f.renderPart("Slide");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(mag[0], mag[1], mag[2]);
            ResourceManager.star_f.renderPart("Mag");
            RenderContext.translate(bullet[0], bullet[1], bullet[2]);
            ResourceManager.star_f.renderPart("Bullet");
            RenderContext.popPose();

            if(hasSilencer(stack, index)) {
                RenderContext.pushPose();
                RenderContext.translate(0F, 2.375F, -0.25F);
                RenderSystem.setShaderTexture(0, ResourceManager.UZI_TEX);
                ResourceManager.uzi.renderPart("Silencer");
                RenderContext.popPose();
            } else {
                float smokeScale = 0.5F;

                RenderContext.pushPose();
                RenderContext.translate(0F, 3F, 6.125F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderContext.scale(smokeScale, smokeScale, smokeScale);
                renderSmokeNodes(buffer, gun.getConfig(stack, index).smokeNodes, 0.75F);
                RenderContext.popPose();

                RenderContext.pushPose();
                RenderContext.translate(0F, 3F, 6.125F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
                renderMuzzleFlash(buffer, gun.lastShot[index], 75, 7.5F);
                RenderContext.popPose();
            }

            RenderContext.popPose();
        }
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        if(displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND) {

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, -2.5F);
            renderStandardGun(stack, 1);
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 2.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            renderStandardGun(stack, 0);
            RenderContext.popPose();
            return;
        }

        renderStandardGun(stack, 1);

        if(living != null && !hasSilencer(stack, 1) && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;
            float shotRand = 0;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[1];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 3F, 6.125F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();
        }
    }

    private void renderStandardGun(ItemStack stack, int index) {

        RenderSystem.setShaderTexture(0, ResourceManager.STAR_F_ELITE_TEX);
        ResourceManager.star_f.renderPart("Gun");
        ResourceManager.star_f.renderPart("Slide");
        ResourceManager.star_f.renderPart("Mag");
        ResourceManager.star_f.renderPart("Hammer");

        if(hasSilencer(stack, index)) {
            RenderContext.pushPose();
            RenderContext.translate(0F, 2.375F, -0.25F);
            RenderSystem.setShaderTexture(0, ResourceManager.UZI_TEX);
            ResourceManager.uzi.renderPart("Silencer");
            RenderContext.popPose();
        }
    }

    public boolean hasSilencer(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_SILENCER);
    }
}
