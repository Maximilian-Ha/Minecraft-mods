package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderMareslegAkimbo.
 *
 * Zwei abgesaegte Mare's Legs nebeneinander. Beide sind kurz -- ohne Lauf und ohne Schaft --,
 * denn nur so laesst sich jede einzeln mit einer Hand durchladen; im Bild wirbelt sie dabei um
 * die eigene Achse (FLIP).
 *
 * ABWEICHUNG: in dritter Person zeichnet der Port nur die Waffe in der Haupthand. Die zweite
 * haengt im Original an RenderPlayerEvent.Specials, einer Ereignisreihe, die es in 1.21 nicht
 * mehr gibt; der Port hat die zugehoerige Schicht noch gar nicht.
 */
public class ItemRenderMareslegAkimbo extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);
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
        float scale = 1.875F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.MARESLEG_TEX);

        float offset = 0.8F;

        for(int side = -1; side <= 1; side += 2) {

            int index = side == -1 ? 0 : 1;

            RenderContext.pushPose();

            standardAimingTransform(stack,
                    -1.5F * offset * side, -1F * offset, 2F * offset,
                    0F, -3.875F / 8F, 1F);

            float scale = 0.375F;
            RenderContext.scale(scale, scale, scale);

            float[] equip = HbmAnimations.getRelevantTransformation("EQUIP", index);
            float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL", index);
            float[] lever = HbmAnimations.getRelevantTransformation("LEVER", index);
            float[] turn = HbmAnimations.getRelevantTransformation("TURN", index);
            float[] flip = HbmAnimations.getRelevantTransformation("FLIP", index);
            float[] lift = HbmAnimations.getRelevantTransformation("LIFT", index);
            float[] shell = HbmAnimations.getRelevantTransformation("SHELL", index);
            float[] flag = HbmAnimations.getRelevantTransformation("FLAG", index);

            RenderContext.translate(recoil[0] * 2, recoil[1], recoil[2]);
            RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 5));
            RenderContext.mulPose(Axis.ZP.rotationDegrees(turn[2]));

            RenderContext.translate(0F, 0F, -4F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
            RenderContext.translate(0F, 0F, 4F);

            RenderContext.translate(0F, 0F, -4F);
            RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
            RenderContext.translate(0F, 0F, 4F);

            RenderContext.translate(0F, 0F, -2F);
            RenderContext.mulPose(Axis.XN.rotationDegrees(flip[0]));
            RenderContext.translate(0F, 0F, 2F);

            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 3.75F);
            RenderContext.mulPose(Axis.ZN.rotationDegrees(turn[2]));
            RenderContext.mulPose(Axis.XP.rotationDegrees(flip[0]));
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            renderSmokeNodes(buffer, gun.getConfig(stack, index).smokeNodes, 0.25F);
            RenderContext.popPose();

            ResourceManager.maresleg.renderPart("Gun");

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.125F, -2.875F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(lever[0]));
            RenderContext.translate(0F, -0.125F, 2.875F);
            ResourceManager.maresleg.renderPart("Lever");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(shell[0], shell[1] - 0.75F, shell[2]);
            ResourceManager.maresleg.renderPart("Shell");
            RenderContext.popPose();

            if(flag[0] != 0) {
                RenderContext.pushPose();
                RenderContext.translate(0F, -0.5F, 0F);
                ResourceManager.maresleg.renderPart("Shell");
                RenderContext.popPose();
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 3.75F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
            renderMuzzleFlash(buffer, gun.lastShot[index], 75, 5);
            RenderContext.popPose();

            RenderContext.popPose();
        }
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.MARESLEG_TEX);

        if(displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND) {

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, -2.5F);
            ResourceManager.maresleg.renderPart("Gun");
            ResourceManager.maresleg.renderPart("Lever");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 2.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            ResourceManager.maresleg.renderPart("Gun");
            ResourceManager.maresleg.renderPart("Lever");
            RenderContext.popPose();
            return;
        }

        ResourceManager.maresleg.renderPart("Gun");
        ResourceManager.maresleg.renderPart("Lever");

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;
            float shotRand = 0;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[1];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 3.75F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            renderMuzzleFlash(buffer, shot, 75, 5);
            RenderContext.popPose();
        }
    }
}
