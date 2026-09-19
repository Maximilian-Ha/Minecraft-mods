package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunConfig;
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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderLiberator.
 *
 * Die Liberator, eine vierlaeufige Kipplaufwaffe. Zum Nachladen wird der Riegel (LATCH)
 * umgelegt, der Laufblock klappt nach unten (BREAK), und die vier Huelsen (SHELL1 bis SHELL4)
 * fallen einzeln heraus -- welche davon sich bewegen, entscheidet die Bewegungsvorschrift
 * anhand der noch geladenen Patronen.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so (siehe ItemRenderDoubleBarrel).
 */
public class ItemRenderLiberator extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 1.25F * offset,
                0F, -4.625F / 8F, 0.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.LIBERATOR_TEX);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] latch = HbmAnimations.getRelevantTransformation("LATCH");
        float[] brk = HbmAnimations.getRelevantTransformation("BREAK");
        float[] shell1 = HbmAnimations.getRelevantTransformation("SHELL1");
        float[] shell2 = HbmAnimations.getRelevantTransformation("SHELL2");
        float[] shell3 = HbmAnimations.getRelevantTransformation("SHELL3");
        float[] shell4 = HbmAnimations.getRelevantTransformation("SHELL4");

        RenderContext.translate(0F, -1F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 3F);

        RenderContext.translate(0F, -3F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 3F, 3F);

        RenderContext.translate(recoil[0] * 2F, recoil[1], recoil[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 10F));

        ResourceManager.liberator.renderPart("Gun");

        RenderContext.pushPose();

        RenderContext.translate(0F, -0.5F, 0.75F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(brk[0]));
        RenderContext.translate(0F, 0.5F, -0.75F);
        ResourceManager.liberator.renderPart("Barrel");

        this.huelse(shell1, "Shell1");
        this.huelse(shell2, "Shell2");
        this.huelse(shell3, "Shell3");
        this.huelse(shell4, "Shell4");

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.15625F, 0.75F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(latch[0]));
        RenderContext.translate(0F, -1.15625F, -0.75F);
        ResourceManager.liberator.renderPart("Latch");
        RenderContext.popPose();

        RenderContext.popPose();

        /*
         * Vier Muendungen, vier Rauchfahnen: das Original zeichnet dieselben Knoten viermal,
         * jedes Mal um einen halben Schritt versetzt. Die Verschiebungen sind durch die
         * Skalierung geteilt, damit sie in Weltmass gelten und nicht im Modellmass.
         */
        float rauch = 0.375F;
        GunConfig cfg = gun.getConfig(stack, 0);

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.25F, 7.25F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(rauch, rauch, rauch);
        RenderContext.translate(0F, 0F, 0.25F / rauch);
        renderSmokeNodes(buffer, cfg.smokeNodes, 1F);
        RenderContext.translate(0F, 0F, -0.5F / rauch);
        renderSmokeNodes(buffer, cfg.smokeNodes, 1F);
        RenderContext.translate(0F, 0.5F / rauch, 0F);
        renderSmokeNodes(buffer, cfg.smokeNodes, 1F);
        RenderContext.translate(0F, 0F, 0.5F / rauch);
        renderSmokeNodes(buffer, cfg.smokeNodes, 1F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(1.5F, 1.5F, 1.5F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 5F);
        RenderContext.popPose();
    }

    /** Eine einzelne Huelse an ihrer Stelle. */
    private void huelse(float[] versatz, String teil) {
        RenderContext.pushPose();
        RenderContext.translate(versatz[0], versatz[1], versatz[2]);
        ResourceManager.liberator.renderPart(teil);
        RenderContext.popPose();
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
        RenderContext.translate(-0.5F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.LIBERATOR_TEX);
        ResourceManager.liberator.renderAll();

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
            RenderContext.translate(0F, 0.5F, 8F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            RenderContext.scale(1.5F, 1.5F, 1.5F);
            renderMuzzleFlash(buffer, shot, 75, 5F);
            RenderContext.popPose();
        }
    }
}
