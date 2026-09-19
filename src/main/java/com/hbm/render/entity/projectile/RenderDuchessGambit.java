package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.DuchessGambit;
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
 * Portiert aus 1.7.10: der Zeichner der Duchess Gambit.
 * Wie der Gueterwagen faellt sie ohne Drehung -- es gibt nichts auszurichten.
 */
public class RenderDuchessGambit extends EntityRenderer<DuchessGambit> {

    public RenderDuchessGambit(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DuchessGambit schiff, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        RenderSystem.setShaderTexture(0, ResourceManager.DUCHESSGAMBIT_TEX);
        ResourceManager.duchessgambit.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(DuchessGambit schiff) {
        return ResourceManager.DUCHESSGAMBIT_TEX;
    }
}
