package com.hbm.render.item.weapon.sedna;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderAmat.
 *
 * Das Antimateriegewehr. Beweglich sind der Verschluss (BOLT_TURN und BOLT_PULL), das Magazin,
 * das Zweibein und das Zielfernrohr, das der Schuetze beim Betrachten in die Luft wirft.
 *
 * DAS ZWEIBEIN steht ausgeklappt, sobald die laufende Animation keinen BIPOD-Kanal hat -- also
 * immer ausser beim Ziehen. So klappt es beim Ziehen aus und bleibt danach stehen, ohne dass
 * irgendwo ein Zustand dafuer mitgefuehrt werden muesste.
 *
 * IST GANZ ANGELEGT, wird die Waffe gar nicht gezeichnet: dann liegt das Bild der Zieloptik ueber
 * dem ganzen Schirm.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderAmat extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderAmat(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.8);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1F * offset, -1F * offset, 3.25F * offset,
                0F, -4.875F / 8F, 1.875F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        /* Ganz angelegt liegt das Bild der Optik ueber dem Schirm -- die Waffe waere nur im Weg. */
        if(GunBaseNTItem.prevAimingProgress == 1 && GunBaseNTItem.aimingProgress == 1) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        /* Ohne BIPOD-Kanal in der laufenden Animation steht das Zweibein fest ausgeklappt. */
        boolean deployed = HbmAnimations.getRelevantAnim(0) == null || HbmAnimations.getRelevantAnim(0).animation.getBus("BIPOD") == null;

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] bipod = HbmAnimations.getRelevantTransformation("BIPOD");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] boltTurn = HbmAnimations.getRelevantTransformation("BOLT_TURN");
        float[] boltPull = HbmAnimations.getRelevantTransformation("BOLT_PULL");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] scopeThrow = HbmAnimations.getRelevantTransformation("SCOPE_THROW");
        float[] scopeSpin = HbmAnimations.getRelevantTransformation("SCOPE_SPIN");

        RenderContext.translate(0F, 0F, recoil[2]);

        RenderContext.translate(0F, -3F, -8F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 3F, 8F);

        ResourceManager.amat.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(scopeThrow[0], scopeThrow[1], scopeThrow[2]);
        RenderContext.translate(0F, 1.5F, -4.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(scopeSpin[0]));
        RenderContext.translate(0F, -1.5F, 4.5F);
        ResourceManager.amat.renderPart("Scope");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.625F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(boltTurn[2]));
        RenderContext.translate(0F, -0.625F, 0F);
        RenderContext.translate(0F, 0F, boltPull[2]);
        ResourceManager.amat.renderPart("Bolt");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        ResourceManager.amat.renderPart("Magazine");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0.3125F, -0.625F, -1F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(deployed ? 25F : bipod[1]));
        RenderContext.translate(-0.3125F, 0.625F, 1F);
        ResourceManager.amat.renderPart("BipodHingeLeft");
        RenderContext.translate(0.3125F, -0.625F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(deployed ? 80F : bipod[0]));
        RenderContext.translate(-0.3125F, 0.625F, 1F);
        ResourceManager.amat.renderPart("BipodLeft");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(-0.3125F, -0.625F, -1F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(deployed ? -25F : -bipod[1]));
        RenderContext.translate(0.3125F, 0.625F, 1F);
        ResourceManager.amat.renderPart("BipodHingeRight");
        RenderContext.translate(-0.3125F, -0.625F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(deployed ? 80F : bipod[0]));
        RenderContext.translate(0.3125F, 0.625F, 1F);
        ResourceManager.amat.renderPart("BipodRight");
        RenderContext.popPose();

        if(isSilenced(stack)) {

            RenderContext.translate(0F, 0.625F, -4.3125F);
            RenderContext.scale(1.25F, 1.25F, 1.25F);
            RenderSystem.setShaderTexture(0, ResourceManager.G3_ATTACHMENTS_TEX);
            ResourceManager.g3.renderPart("Silencer");

        } else {

            ResourceManager.amat.renderPart("MuzzleBrake");

            float smokeScale = 0.5F;

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.625F, 12F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(smokeScale, smokeScale, smokeScale);
            renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 1F);
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.5F, 11F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(0.75F, 0.75F, 0.75F);
            renderGapFlash(buffer, gun.lastShot[0]);
            RenderContext.popPose();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.5F, 6.75F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = isSilenced(stack) ? 0.8175F : 0.9375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0.5F, isSilenced(stack) ? -1F : 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.amat.renderPart("Gun");
        ResourceManager.amat.renderPart("Bolt");
        ResourceManager.amat.renderPart("Magazine");
        ResourceManager.amat.renderPart("BipodLeft");
        ResourceManager.amat.renderPart("BipodHingeLeft");
        ResourceManager.amat.renderPart("BipodRight");
        ResourceManager.amat.renderPart("BipodHingeRight");
        ResourceManager.amat.renderPart("Scope");

        boolean silenced = isSilenced(stack);

        if(silenced) {
            RenderContext.translate(0F, 0.625F, -4.3125F);
            RenderContext.scale(1.25F, 1.25F, 1.25F);
            RenderSystem.setShaderTexture(0, ResourceManager.G3_ATTACHMENTS_TEX);
            ResourceManager.g3.renderPart("Silencer");
            return;
        }

        ResourceManager.amat.renderPart("MuzzleBrake");

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
            RenderContext.translate(0F, 0.5F, 11F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(0.75F, 0.75F, 0.75F);
            renderGapFlash(buffer, shot);
            RenderContext.popPose();
        }
    }

    /** Die Penance traegt ihren Schalldaempfer von Haus aus; an den beiden anderen ist er Aufsatz. */
    public boolean isSilenced(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_AMAT_PENANCE.get()
                || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
    }
}
