package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.Torpedo;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Portiert aus 1.7.10: der Torpedozweig von com.hbm.render.entity.projectile.RenderBoxcar.
 *
 * Anders als Gueterwagen und Luftschiff kippt der Torpedo im Fall nach vorn: drei Grad je Tick,
 * bis er bei fuenfundachtzig Grad steht. Danach faellt er in dieser Lage weiter.
 */
public class RenderTorpedo extends EntityRenderer<Torpedo> {

    public RenderTorpedo(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Torpedo torpedo, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        float alter = torpedo.tickCount + partialTicks;
        RenderContext.mulPose(Axis.XP.rotationDegrees(Math.min(85F, alter * 3F)));

        RenderSystem.setShaderTexture(0, ResourceManager.TORPEDO_TEX);
        ResourceManager.torpedo.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(Torpedo torpedo) {
        return ResourceManager.TORPEDO_TEX;
    }
}
