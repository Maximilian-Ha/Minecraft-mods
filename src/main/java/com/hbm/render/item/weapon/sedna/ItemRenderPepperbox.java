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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderPepperbox.
 *
 * Die Pfefferbuechse hat fuenf bewegliche Teile: den Laufblock, der sich dreht, den Hahn, den
 * Abzug, den Schnelllader beim Nachladen und die Patrone darin. Das Muendungsfeuer wird zweimal
 * gezeichnet, um 45 Grad versetzt -- sechs Laeufe, das sieht sonst zu duenn aus.
 *
 * ABWEICHUNG: setupModTable des Originals entfaellt, solange das Aufsatzsystem fehlt.
 */
public class ItemRenderPepperbox extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 1.5F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.25F * offset, -0.75F * offset, 1F * offset,
                0F, -2.5F / 8F, 0.5F);
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
        RenderContext.translate(0.5F, 0.5F, 0F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        float scale = 0.25F;
        RenderContext.scale(scale, scale, scale);

        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] cylinder = HbmAnimations.getRelevantTransformation("ROTATE");
        float[] hammer = HbmAnimations.getRelevantTransformation("HAMMER");
        float[] trigger = HbmAnimations.getRelevantTransformation("TRIGGER");
        float[] translate = HbmAnimations.getRelevantTransformation("TRANSLATE");
        float[] loader = HbmAnimations.getRelevantTransformation("LOADER");
        float[] shot = HbmAnimations.getRelevantTransformation("SHOT");

        RenderContext.translate(translate[0], translate[1], translate[2]);

        RenderContext.translate(0F, 0F, -5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-recoil[0]));
        RenderContext.translate(0F, 0F, 5F);

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 7F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderSystem.setShaderTexture(0, ResourceManager.PEPPERBOX_TEX);

        /* Der Schnelllader wird nur gezeichnet, solange er im Bild ist. */
        if(loader[0] != 0 || loader[1] != 0 || loader[2] != 0) {
            RenderContext.pushPose();
            RenderContext.translate(loader[0], loader[1], loader[2]);
            ResourceManager.pepperbox.renderPart("Speedloader");
            if(shot[0] != 0) ResourceManager.pepperbox.renderPart("Shot");
            RenderContext.popPose();
        }

        ResourceManager.pepperbox.renderPart("Grip");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZP.rotationDegrees(cylinder[0]));
        ResourceManager.pepperbox.renderPart("Cylinder");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.375F, -1.875F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(hammer[0]));
        RenderContext.translate(0F, -0.375F, 1.875F);
        ResourceManager.pepperbox.renderPart("Hammer");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, -trigger[0] * 0.5F);
        ResourceManager.pepperbox.renderPart("Trigger");
        RenderContext.popPose();

        this.muzzleFlash(buffer, gun.lastShot[0], gun.shotRand);
    }

    /** Zweimal, um 45 Grad versetzt -- so wirkt das Feuer aus sechs Laeufen breit genug. */
    private void muzzleFlash(MultiBufferSource buffer, long lastShot, float shotRand) {
        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 7F);
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        renderMuzzleFlash(buffer, lastShot);
        RenderContext.mulPose(Axis.XP.rotationDegrees(45F));
        renderMuzzleFlash(buffer, lastShot);
        RenderContext.popPose();
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.PEPPERBOX_TEX);
        ResourceManager.pepperbox.renderPart("Grip");
        ResourceManager.pepperbox.renderPart("Cylinder");
        ResourceManager.pepperbox.renderPart("Hammer");
        ResourceManager.pepperbox.renderPart("Trigger");

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

            this.muzzleFlash(buffer, shot, shotRand);
        }
    }
}
