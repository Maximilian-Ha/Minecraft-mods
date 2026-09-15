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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderStarF.
 *
 * Die Star-F. Beweglich sind das Schloss (SLIDE), der Hahn (HAMMER), das Magazin (MAG) und die
 * Patrone im Lauf (BULLET) -- letztere wird beim Schuss nach hinten weggeschoben, wenn noch eine
 * da ist, und sonst ganz aus dem Bild.
 *
 * Der Schalldaempfer borgt sich sein Modellteil von der Uzi -- so steht es schon im Original.
 * Steckt einer, entfallen Rauch und Muendungsfeuer.
 *
 * ABWEICHUNG: setupModTable und renderModTable sind NICHT UEBERNOMMEN. Sie stellen die Waffe im
 * Waffentisch dar; der Tisch des Ports zeigt statt der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderStarF extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderStarF(ResourceLocation texture) {
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
                -1.75F * offset, -1.75F * offset, 2.5F * offset,
                0F, -7.625F / 8F, 1F);
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
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-1F, -0.5F, 0F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.25F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] hammer = HbmAnimations.getRelevantTransformation("HAMMER");
        float[] tilt = HbmAnimations.getRelevantTransformation("TILT");
        float[] turn = HbmAnimations.getRelevantTransformation("TURN");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");
        float[] slide = HbmAnimations.getRelevantTransformation("SLIDE");

        RenderContext.translate(0F, -2F, -8F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 8F);

        RenderContext.translate(0F, 1F, -3F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(turn[2]));
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

        if(hasSilencer(stack)) {
            RenderContext.pushPose();
            RenderContext.translate(0F, 2.375F, -0.25F);
            RenderSystem.setShaderTexture(0, ResourceManager.UZI_TEX);
            ResourceManager.uzi.renderPart("Silencer");
            RenderContext.popPose();
            return;
        }

        float smokeScale = 0.5F;

        RenderContext.pushPose();
        RenderContext.translate(0F, 3F, 6.125F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.75F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 3F, 6.125F);
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        boolean silenced = hasSilencer(stack);

        if(silenced && displayContext == ItemDisplayContext.GUI) {
            float scale = 0.625F;
            RenderContext.scale(scale, scale, scale);
            RenderContext.translate(0F, 0F, -6F);
        }

        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.star_f.renderPart("Gun");
        ResourceManager.star_f.renderPart("Slide");
        ResourceManager.star_f.renderPart("Mag");
        ResourceManager.star_f.renderPart("Hammer");

        if(silenced) {
            RenderContext.pushPose();
            RenderContext.translate(0F, 2.375F, -0.25F);
            RenderSystem.setShaderTexture(0, ResourceManager.UZI_TEX);
            ResourceManager.uzi.renderPart("Silencer");
            RenderContext.popPose();
            return;
        }

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
            RenderContext.translate(0F, 3F, 6.25F);
            RenderContext.scale(0.75F, 0.75F, 0.75F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();
        }
    }

    public boolean hasSilencer(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
    }
}
