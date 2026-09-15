package com.hbm.render.entity.rocket;

import com.hbm.entity.missile.SatellitePod;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.rocket.RenderDropship.
 *
 * Die Kapsel besteht aus zwei Teilen desselben Modells: dem Rumpf und einem Bein, das viermal
 * gezeichnet und dabei je um neunzig Grad weitergedreht wird. Eingeklappt steht es um
 * hundertfuenfzig Grad abgewinkelt; ausgefahren gerade.
 */
public class RenderSatellitePod extends EntityRenderer<SatellitePod> {

    public RenderSatellitePod(Context context) {
        super(context);
    }

    @Override
    public void render(SatellitePod pod, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
        RenderSystem.setShaderTexture(0, this.getTextureLocation(pod));

        ResourceManager.dropship.renderPart("Pod");

        float legs = Mth.lerp(partialTicks, pod.prevLegs, pod.legs);

        for(int i = 0; i < 4; i++) {
            RenderContext.pushPose();
            RenderContext.mulPose(Axis.YP.rotationDegrees(45F + 90F * i));
            RenderContext.translate(0.5F, 1.75F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(150F * (1F - legs)));
            RenderContext.translate(-0.5F, -1.75F, 0F);
            ResourceManager.dropship.renderPart("Leg");
            RenderContext.popPose();
        }

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(SatellitePod pod) {
        return ResourceManager.DROPSHIP_TEX;
    }
}
