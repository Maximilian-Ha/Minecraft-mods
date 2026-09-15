package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderFlaregun.
 *
 * Die Leuchtpistole. OPEN klappt den Lauf auf, SHELL schiebt die Leuchtkugel heraus, FLIP dreht
 * die ganze Waffe -- beim Betrachten dreimal um sich selbst.
 *
 * DER RAUCH wird zweimal gezeichnet, einmal gross und einmal etwas kleiner dahinter. Das ist im
 * Original so: eine Leuchtkugel qualmt dichter als ein Geschoss.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderFlaregun extends ItemRenderWeaponBase {

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
                -1.25F * offset, -1.5F * offset, 2F * offset,
                0F, -5.5F / 8F, 0.5F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.FLAREGUN_TEX);

        float scale = 0.125F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] hammer = HbmAnimations.getRelevantTransformation("HAMMER");
        float[] open = HbmAnimations.getRelevantTransformation("OPEN");
        float[] shell = HbmAnimations.getRelevantTransformation("SHELL");
        float[] flip = HbmAnimations.getRelevantTransformation("FLIP");

        RenderContext.translate(recoil[0], recoil[1], recoil[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 10F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(flip[0]));

        RenderContext.translate(0F, 0F, -8F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 0F, 8F);

        ResourceManager.flaregun.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.8125F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(hammer[0] - 15F));
        RenderContext.translate(0F, -1.8125F, 4F);
        ResourceManager.flaregun.renderPart("Hammer");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 2.156F, 1.78F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(open[0]));
        RenderContext.translate(0F, -2.156F, -1.78F);
        ResourceManager.flaregun.renderPart("Barrel");
        RenderContext.translate(shell[0], shell[1], shell[2]);
        ResourceManager.flaregun.renderPart("Flare");
        RenderContext.popPose();

        float smokeScale = 0.5F;

        RenderContext.pushPose();
        RenderContext.translate(0F, 4F, 9F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 2.5F);
        RenderContext.translate(0F, 0F, 0.1F);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 2F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.25F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0F, 0F);
    }

    @Override
    public void setupEntity(ItemStack stack) {
        super.setupEntity(stack);
        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.FLAREGUN_TEX);
        ResourceManager.flaregun.renderAll();
    }
}
