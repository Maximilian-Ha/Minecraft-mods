package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.Boxcar;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderBoxcar.
 *
 * Der fallende Gueterwagen. Er dreht sich nicht und richtet sich nicht aus -- er faellt einfach,
 * und das ist auch schon die ganze Darstellung.
 */
public class RenderBoxcar extends EntityRenderer<Boxcar> {

    public RenderBoxcar(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Boxcar boxcar, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        RenderSystem.setShaderTexture(0, ResourceManager.BOXCAR_TEX);
        ResourceManager.boxcar.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(Boxcar boxcar) {
        return ResourceManager.BOXCAR_TEX;
    }
}
