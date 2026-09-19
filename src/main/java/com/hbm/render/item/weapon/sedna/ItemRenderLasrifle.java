package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderLasrifle.
 *
 * Das Lasergewehr. Der Hebel schnappt bei jedem Schuss, beim Nachladen faellt die Batterie
 * heraus, und wer ein Zielfernrohr angebaut hat, sieht beim Anlegen nur noch durch das Glas --
 * die Waffe selbst wird dann gar nicht gezeichnet.
 *
 * NICHT UEBERNOMMEN sind die beiden Aufsatzteile aus lasrifle_mods.obj (Schrotlauf und
 * Unterlaufkondensator). Die zugehoerigen Aufsaetze LAS_SHOTGUN und LAS_CAPACITOR sind im
 * Port nicht angemeldet; ein Modellteil, das nie erscheint, waere totes Gewicht.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderLasrifle extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        if(hasScope(stack)) {
            standardAimingTransform(stack,
                    -1.5F * offset, -1.5F * offset, 2.5F * offset,
                    0F, -7.375F / 8F, 0.75F);
        } else {
            standardAimingTransform(stack,
                    -1.5F * offset, -1.5F * offset, 2.5F * offset,
                    0F, -5.25F / 8F, 1F);
        }
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        /* Voll angelegt mit Zielfernrohr: nur noch das Glas, die Waffe faellt weg. */
        if(hasScope(stack) && GunBaseNTItem.prevAimingProgress == 1 && GunBaseNTItem.aimingProgress == 1) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.LASRIFLE_TEX);

        float scale = 0.3125F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] lever = HbmAnimations.getRelevantTransformation("LEVER");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");

        RenderContext.translate(0F, -1F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 6F);

        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.lasrifle.renderPart("Gun");
        ResourceManager.lasrifle.renderPart("Stock");
        ResourceManager.lasrifle.renderPart("Barrel");
        if(hasScope(stack)) ResourceManager.lasrifle.renderPart("Scope");

        RenderContext.pushPose();
        RenderContext.translate(0F, -0.375F, 2.375F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lever[0]));
        RenderContext.translate(0F, 0.375F, -2.375F);
        ResourceManager.lasrifle.renderPart("Lever");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        ResourceManager.lasrifle.renderPart("Battery");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.5F, 12F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderLaserFlash(buffer, gun.lastShot[0], 150, 1.5F, 0xFF0000);
        RenderContext.translate(0F, 0F, -0.25F);
        renderLaserFlash(buffer, gun.lastShot[0], 150, 0.75F, 0xFF8000);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.03125F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0.75F, 0F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.LASRIFLE_TEX);
        ResourceManager.lasrifle.renderPart("Gun");
        ResourceManager.lasrifle.renderPart("Stock");
        ResourceManager.lasrifle.renderPart("Barrel");
        ResourceManager.lasrifle.renderPart("Lever");
        ResourceManager.lasrifle.renderPart("Battery");
        if(hasScope(stack)) ResourceManager.lasrifle.renderPart("Scope");
    }

    public boolean hasScope(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
    }
}
