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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderDANI.
 *
 * Das beidhaendige Revolverpaar. Die beiden teilen sich das Modell des leichten Revolvers, aber
 * nicht die Textur: der erste traegt die Sonne, der zweite den Mond. Ihre Nachladebewegung
 * spiegelt sich, damit beide Trommeln nach aussen aufklappen.
 *
 * ABWEICHUNG: in dritter Person zeichnet der Port nur die Waffe in der Haupthand. Die zweite
 * haengt im Original an RenderPlayerEvent.Specials, einer Ereignisreihe, die es in 1.21 nicht
 * mehr gibt; der Port hat die zugehoerige Schicht noch gar nicht.
 */
public class ItemRenderDANI extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, 1F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 0.9375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
    }

    /** Der erste Lauf traegt die Sonne, der zweite den Mond. */
    private static ResourceLocation textureFor(int index) {
        return index == 0 ? ResourceManager.DANI_CELESTIAL_TEX : ResourceManager.DANI_LUNAR_TEX;
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        float offset = 0.8F;

        for(int side = -1; side <= 1; side += 2) {

            int index = side == -1 ? 0 : 1;

            RenderContext.pushPose();
            RenderSystem.setShaderTexture(0, textureFor(index));

            standardAimingTransform(stack,
                    -1.5F * offset * side, -0.75F * offset, 1F * offset,
                    0F, -3.125F / 8F, 0.25F);

            float scale = 0.125F;
            RenderContext.scale(scale, scale, scale);

            float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL", index);
            float[] reloadMove = HbmAnimations.getRelevantTransformation("RELOAD_MOVE", index);
            float[] reloadRot = HbmAnimations.getRelevantTransformation("RELOAD_ROT", index);
            float[] equip = HbmAnimations.getRelevantTransformation("EQUIP", index);

            RenderContext.translate(recoil[0], recoil[1], recoil[2]);
            RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 10F));

            RenderContext.translate(0F, -2F, -2F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(-equip[0]));
            RenderContext.translate(0F, 2F, 2F);

            RenderContext.pushPose();
            RenderContext.translate(0F, 1.5F, 9.25F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(-recoil[2] * 10F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            renderSmokeNodes(buffer, gun.getConfig(stack, index).smokeNodes, 0.5F);
            RenderContext.popPose();

            RenderContext.translate(reloadMove[0], reloadMove[1], reloadMove[2]);

            RenderContext.mulPose(Axis.XP.rotationDegrees(reloadRot[0]));
            RenderContext.mulPose(Axis.ZP.rotationDegrees(reloadRot[2] * side));
            RenderContext.mulPose(Axis.YP.rotationDegrees(reloadRot[1] * side));

            ResourceManager.bio_revolver.renderPart("Grip");

            RenderContext.pushPose();
            RenderContext.mulPose(Axis.XP.rotationDegrees(HbmAnimations.getRelevantTransformation("FRONT", index)[2]));
            ResourceManager.bio_revolver.renderPart("Barrel");

            RenderContext.pushPose();
            RenderContext.translate(0F, 2.3125F, -0.875F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(HbmAnimations.getRelevantTransformation("LATCH", index)[2]));
            RenderContext.translate(0F, -2.3125F, 0.875F);
            ResourceManager.bio_revolver.renderPart("Latch");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(HbmAnimations.getRelevantTransformation("DRUM", index)[2] * 60F));
            RenderContext.translate(0F, -1F, 0F);
            RenderContext.translate(0F, 0F, HbmAnimations.getRelevantTransformation("DRUM_PUSH", index)[2]);
            ResourceManager.bio_revolver.renderPart("Drum");
            RenderContext.popPose();

            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, -4.5F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(-45F + 45F * HbmAnimations.getRelevantTransformation("HAMMER", index)[2]));
            RenderContext.translate(0F, 0F, 4.5F);
            ResourceManager.bio_revolver.renderPart("Hammer");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 1.5F, 9.25F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            renderMuzzleFlash(buffer, gun.lastShot[index], 75, 7.5F);
            RenderContext.popPose();

            RenderContext.popPose();
        }
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        if(displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND) {

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, -2.5F);
            RenderSystem.setShaderTexture(0, textureFor(1));
            ResourceManager.bio_revolver.renderAll();
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 2.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            RenderSystem.setShaderTexture(0, textureFor(0));
            ResourceManager.bio_revolver.renderAll();
            RenderContext.popPose();
            return;
        }

        RenderSystem.setShaderTexture(0, textureFor(1));
        ResourceManager.bio_revolver.renderAll();

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[1];
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
