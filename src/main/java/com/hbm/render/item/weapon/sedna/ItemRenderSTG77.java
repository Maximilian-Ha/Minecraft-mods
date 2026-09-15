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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderSTG77.
 *
 * Die StG 77. Sie hat als einzige Waffe des Ports Bewegungen, die nicht von Hand geschrieben,
 * sondern aus einer Animationsdatei geladen sind -- deshalb haengen fast alle Teile an
 * applyRelevantTransformation und nicht an ausgerechneten Verschiebungen. Von Hand kommen nur
 * der Ruecklauf (RECOIL), die Sicherung (SAFETY) und der Verschluss (BOLT).
 *
 * BEIM BETRACHTEN zerlegt der Schuetze die Waffe halb: INSPECT_GUN dreht den Koerper weg,
 * INSPECT_BARREL und INSPECT_MOVE fuehren das Rohr getrennt davon.
 *
 * ABWEICHUNG: das Original zieht beim Anlegen zusaetzlich das Blickfeld der Hand von 70 auf 5
 * Grad zusammen (getBaseFOV) -- das ist die eigentliche Zoomwirkung des Rohrs. Der Port hat
 * keinen Haken fuer das Blickfeld der Handdarstellung; es bleibt beim Weltblickfeld, das
 * getViewFOV auf ein Drittel zieht.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderSTG77 extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 0.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.66);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1F * offset, 2.5F * offset,
                0F, -5.75F / 8F, 2F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        if(GunBaseNTItem.prevAimingProgress == 1 && GunBaseNTItem.aimingProgress == 1) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.STG77_TEX);

        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] bolt = HbmAnimations.getRelevantTransformation("BOLT");
        float[] handle = HbmAnimations.getRelevantTransformation("HANDLE");
        float[] safety = HbmAnimations.getRelevantTransformation("SAFETY");

        float[] inspectGun = HbmAnimations.getRelevantTransformation("INSPECT_GUN");
        float[] inspectBarrel = HbmAnimations.getRelevantTransformation("INSPECT_BARREL");
        float[] inspectMove = HbmAnimations.getRelevantTransformation("INSPECT_MOVE");
        float[] inspectLever = HbmAnimations.getRelevantTransformation("INSPECT_LEVER");

        RenderContext.translate(0F, -1F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 4F);

        RenderContext.translate(0F, 0F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, 4F);

        RenderContext.translate(0F, 0F, recoil[2]);

        /* Der Koerper der Waffe -- beim Betrachten dreht ihn INSPECT_GUN aus dem Bild. */
        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZP.rotationDegrees(inspectGun[2]));
        RenderContext.mulPose(Axis.XP.rotationDegrees(inspectGun[0]));

        HbmAnimations.applyRelevantTransformation("Gun");
        ResourceManager.stg77.renderPart("Gun");

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Magazine");
        ResourceManager.stg77.renderPart("Magazine");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZP.rotationDegrees(inspectLever[2]));
        HbmAnimations.applyRelevantTransformation("Lever");
        ResourceManager.stg77.renderPart("Lever");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, bolt[2]);
        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Breech");
        ResourceManager.stg77.renderPart("Breech");
        RenderContext.popPose();
        RenderContext.translate(0.125F, 0F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(handle[2]));
        RenderContext.translate(-0.125F, 0F, 0F);
        HbmAnimations.applyRelevantTransformation("Handle");
        ResourceManager.stg77.renderPart("Handle");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(safety[0], 0F, 0F);
        HbmAnimations.applyRelevantTransformation("Safety");
        ResourceManager.stg77.renderPart("Safety");
        RenderContext.popPose();

        RenderContext.popPose();

        /* Das Rohr haengt nicht am Koerper: beim Betrachten wird es getrennt herausgezogen. */
        RenderContext.pushPose();
        RenderContext.translate(inspectMove[0], inspectMove[1], inspectMove[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(inspectBarrel[0]));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(inspectBarrel[2]));
        HbmAnimations.applyRelevantTransformation("Gun");
        HbmAnimations.applyRelevantTransformation("Barrel");
        ResourceManager.stg77.renderPart("Barrel");
        RenderContext.popPose();

        float smokeScale = 0.75F;

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 7.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(0.25F, 0.25F, 0.25F);
        renderGapFlash(buffer, gun.lastShot[0]);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 1F, 2F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.STG77_TEX);
        ResourceManager.stg77.renderPart("Gun");
        ResourceManager.stg77.renderPart("Barrel");
        ResourceManager.stg77.renderPart("Lever");
        ResourceManager.stg77.renderPart("Magazine");
        ResourceManager.stg77.renderPart("Safety");
        ResourceManager.stg77.renderPart("Handle");
        ResourceManager.stg77.renderPart("Breech");

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 7.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(0.25F, 0.25F, 0.25F);
            renderGapFlash(buffer, shot);
            RenderContext.popPose();
        }
    }
}
