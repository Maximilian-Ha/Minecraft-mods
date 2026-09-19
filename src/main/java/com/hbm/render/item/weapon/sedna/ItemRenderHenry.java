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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderHenry.
 *
 * Der Unterhebelrepetierer. Sechs Teile bewegen sich einzeln: Kimme, Hahn, Unterhebel,
 * der Vorderschaft (beim Nachladen weggedreht), die einzelne Patrone und der Rest.
 *
 * DIESELBE KLASSE TRAEGT BEIDE WAFFEN, den Henry und den Lincoln Repeater -- sie
 * unterscheiden sich nur in der Textur, die der Konstruktor bekommt. Genauso macht es das
 * Original, das die Klasse zweimal mit verschiedenen Texturen anmeldet.
 */
public class ItemRenderHenry extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderHenry(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

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
                -1.25F * offset, -1F * offset, 1.75F * offset,
                0, -5F / 8F, 1);

        /* Das Original kippt die Waffe beim Zielen um zweieinhalb Grad nach vorn -- ohne das
         * sitzt die Kimme zu hoch. */
        float aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-2.5F * aimingProgress));
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.25F, 3F);
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
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, texture);
        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] sight = HbmAnimations.getRelevantTransformation("SIGHT");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] hammer = HbmAnimations.getRelevantTransformation("HAMMER");
        float[] lever = HbmAnimations.getRelevantTransformation("LEVER");
        float[] turn = HbmAnimations.getRelevantTransformation("TURN");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] twist = HbmAnimations.getRelevantTransformation("TWIST");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");
        float[] yeet = HbmAnimations.getRelevantTransformation("YEET");
        float[] roll = HbmAnimations.getRelevantTransformation("ROLL");

        RenderContext.translate(recoil[0] * 2, recoil[1], recoil[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 5));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(turn[2]));

        RenderContext.translate(yeet[0], yeet[1], yeet[2]);

        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(roll[2]));
        RenderContext.translate(0F, -1F, 0F);

        RenderContext.translate(0F, -4F, 4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 4F, -4F);

        RenderContext.translate(0F, 2F, -4F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, -2F, 4F);

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 8F);
        RenderContext.mulPose(Axis.ZN.rotationDegrees(turn[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.25F);
        RenderContext.popPose();

        ResourceManager.henry.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.25F, -0.1875F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(sight[0]));
        RenderContext.translate(0F, -1.25F, 0.1875F);
        ResourceManager.henry.renderPart("Sight");
        RenderContext.popPose();

        /* Der Hahn steht in der Ruhelage dreissig Grad zurueck, nicht bei null. */
        RenderContext.pushPose();
        RenderContext.translate(0F, 0.625F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-30F + hammer[0]));
        RenderContext.translate(0F, -0.625F, 3F);
        ResourceManager.henry.renderPart("Hammer");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.25F, -2.3125F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lever[0]));
        RenderContext.translate(0F, -0.25F, 2.3125F);
        ResourceManager.henry.renderPart("Lever");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(twist[2]));
        RenderContext.translate(0F, -1F, 0F);
        ResourceManager.henry.renderPart("Front");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(bullet[0], bullet[1], bullet[2] - 1F);
        ResourceManager.henry.renderPart("Bullet");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90 * gun.shotRand));
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 5);
        RenderContext.popPose();
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, texture);
        ResourceManager.henry.renderAll();

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
            RenderContext.translate(0F, 1F, 8F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90 * shotRand));
            renderMuzzleFlash(buffer, shot, 75, 5);
            RenderContext.popPose();
        }
    }
}
