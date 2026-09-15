package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.ZirnoxDebris;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
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
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderZirnoxDebris.
 * Das Graphit teilt sich Modell und Textur mit dem RBMK-Schutt, wie im Original.
 */
public class RenderZirnoxDebris extends EntityRenderer<ZirnoxDebris> {

    private static final ResourceLocation ZIRNOX_ELEMENT_TEX = NuclearTechMod.withDefaultNamespace("textures/models/machines/zirnox_deb_element.png");

    public RenderZirnoxDebris(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ZirnoxDebris debris, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        RenderContext.translate(0F, 0.125F, 0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(debris.getId() % 360));

        float rot = debris.lastRot + (debris.rot - debris.lastRot) * partialTicks;
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
        RenderContext.mulPose(Axis.YP.rotationDegrees(rot));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rot));

        ResourceLocation texture;
        IModelCustom model;

        switch(debris.getDebrisType()) {
            case ELEMENT -> { texture = ZIRNOX_ELEMENT_TEX; model = ResourceManager.deb_zirnox_element; }
            case SHRAPNEL -> { texture = ResourceManager.ZIRNOX_TEX; model = ResourceManager.deb_zirnox_shrapnel; }
            case GRAPHITE -> { texture = RenderRBMKDebris.TEX_GRAPHITE; model = ResourceManager.deb_graphite; }
            case CONCRETE -> { texture = ResourceManager.ZIRNOX_DESTROYED_TEX; model = ResourceManager.deb_zirnox_concrete; }
            case EXCHANGER -> { texture = ResourceManager.ZIRNOX_TEX; model = ResourceManager.deb_zirnox_exchanger; }
            default -> { texture = ResourceManager.ZIRNOX_TEX; model = ResourceManager.deb_zirnox_blank; }
        }

        RenderSystem.setShaderTexture(0, texture);
        model.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(ZirnoxDebris debris) {
        return ResourceManager.ZIRNOX_TEX;
    }
}
