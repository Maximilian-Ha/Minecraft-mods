package com.hbm.render.entity.rocket;

import com.hbm.entity.missile.MissileBase;
import com.hbm.entity.missile.MissileCustom;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.rocket.RenderMissileCustom.
 *
 * ABWEICHUNG, und eine grosse: das Original setzt die Eigenbau-Rakete aus einem Modell JE BAUTEIL
 * zusammen -- MissilePart ordnet jedem der hundertzweiundzwanzig Teile ein Modell und eine Haut
 * zu, MissileMultipart stapelt sie, MissilePronter zeichnet. Das sind gut dreihundert Zeilen plus
 * ein Modell je Teil.
 *
 * Bis die portiert sind, traegt die Eigenbau-Rakete das allgemeine Raketenmodell. SIE FLIEGT UND
 * SCHLAEGT RICHTIG EIN -- nur aussehen tut sie wie jede andere. Das ist sichtbar unfertig und
 * gehoert in eine eigene Runde.
 */
public class RenderMissileCustom extends EntityRenderer<MissileCustom> {

    public RenderMissileCustom(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MissileCustom missile, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        RenderContext.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, missile.yRotO, missile.yRot) - 90.0F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, missile.xRotO, missile.xRot)));
        RenderContext.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTicks, missile.yRotO, missile.yRot) - 90.0F));

        Direction facing = missile.getEntityData().get(MissileBase.ROT);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        RenderSystem.setShaderTexture(0, this.getTextureLocation(missile));
        ResourceManager.missileV2.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(MissileCustom missile) {
        return ResourceManager.MISSILE_V2_HE_TEX;
    }
}
