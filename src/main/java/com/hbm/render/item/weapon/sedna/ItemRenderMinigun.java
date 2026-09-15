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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderMinigun.
 *
 * Die Minigun. Beweglich ist nur der Laufkranz (ROTATE); Gehaeuse und Griff stehen fest.
 *
 * ABWEICHUNG: das Original kennt hier auch das Lacunae-Lasergatling und zeichnet ihm statt des
 * Muendungsfeuers zwei ineinanderliegende Laserblitze (renderLaserFlash). Weder die Waffe noch
 * renderLaserFlash stehen im Port; beides kommt mit der Kondensatormunition.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderMinigun extends ItemRenderWeaponBase {

    protected final ResourceLocation texture;

    public ItemRenderMinigun(ResourceLocation texture) {
        this.texture = texture;
    }

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
        standardAimingTransform(stack,
                -1.75F * offset, -1.75F * offset, 3.5F * offset,
                0F, -6.25F / 8F, 1F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] rotate = HbmAnimations.getRelevantTransformation("ROTATE");

        RenderContext.translate(0F, 3F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, -3F, 6F);

        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.minigun.renderPart("Gun");
        ResourceManager.minigun.renderPart("Grip");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rotate[2]));
        ResourceManager.minigun.renderPart("Barrels");
        RenderContext.popPose();

        float smokeScale = 0.5F;

        RenderContext.pushPose();
        RenderContext.translate(-2F, 1.25F, -3.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 12F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.translate(0F, 0.5F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(gun.shotRand * 90F));
        RenderContext.scale(1.5F, 1.5F, 1.5F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 50, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(1F, -3.5F, 8F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 0.875F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.25F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.minigun.renderPart("Gun");
        ResourceManager.minigun.renderPart("Grip");
        ResourceManager.minigun.renderPart("Barrels");

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
            RenderContext.translate(0F, 0F, 12.25F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.translate(0F, 0.5F, 0F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(shotRand * 90F));
            RenderContext.scale(1.5F, 1.5F, 1.5F);
            renderMuzzleFlash(buffer, shot, 50, 7.5F);
            RenderContext.popPose();
        }
    }
}
