package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderAtlas.
 *
 * Der leichte Revolver. Beide Ausfuehrungen teilen sich das Modell und unterscheiden sich nur in
 * der Textur -- die bekommt der Renderer deshalb im Konstruktor.
 *
 * Beim Nachladen klappt der vordere Teil nach unten (FRONT), die Sperre gibt ihn frei (LATCH), die
 * Trommel dreht sich (DRUM) und wird am Ende zurueckgeschoben (DRUM_PUSH).
 *
 * ABWEICHUNG: setupModTable des Originals entfaellt, solange das Aufsatzsystem fehlt.
 */
public class ItemRenderAtlas extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderAtlas(ResourceLocation texture) {
        this.texture = texture;
    }

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

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.0F * offset, -0.75F * offset, 1F * offset,
                0F, -3.125F / 8F, 0.25F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 1F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.125F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 1.5F, 0F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.125F;
        RenderContext.scale(scale, scale, scale);

        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] reloadMove = HbmAnimations.getRelevantTransformation("RELOAD_MOVE");
        float[] reloadRot = HbmAnimations.getRelevantTransformation("RELOAD_ROT");
        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");

        RenderContext.translate(recoil[0], recoil[1], recoil[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 10F));

        RenderContext.translate(0F, 0F, -7F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-equip[0]));
        RenderContext.translate(0F, 0F, 7F);

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.5F, 9.25F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-recoil[2] * 10F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderContext.translate(reloadMove[0], reloadMove[1], reloadMove[2]);

        RenderContext.mulPose(Axis.XP.rotationDegrees(reloadRot[0]));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(reloadRot[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(reloadRot[1]));

        ResourceManager.bio_revolver.renderPart("Grip");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.XP.rotationDegrees(HbmAnimations.getRelevantTransformation("FRONT")[2]));
        ResourceManager.bio_revolver.renderPart("Barrel");

        RenderContext.pushPose();
        RenderContext.translate(0F, 2.3125F, -0.875F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(HbmAnimations.getRelevantTransformation("LATCH")[2]));
        RenderContext.translate(0F, -2.3125F, 0.875F);
        ResourceManager.bio_revolver.renderPart("Latch");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(HbmAnimations.getRelevantTransformation("DRUM")[2] * 60F));
        RenderContext.translate(0F, -1F, 0F);
        RenderContext.translate(0F, 0F, HbmAnimations.getRelevantTransformation("DRUM_PUSH")[2]);
        ResourceManager.bio_revolver.renderPart("Drum");
        RenderContext.popPose();

        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, -4.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-45F + 45F * HbmAnimations.getRelevantTransformation("HAMMER")[2]));
        RenderContext.translate(0F, 0F, 4.5F);
        ResourceManager.bio_revolver.renderPart("Hammer");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.5F, 9.25F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.bio_revolver.renderAll();

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;

            if(living == Minecraft.getInstance().player) {
                shot = ((GunBaseNTItem) stack.getItem()).lastShot[0];
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 1.5F, 9.25F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();
        }
    }
}
