package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.Cog;
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
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderCog.
 *
 * Der Orientierungswert ist weiterhin die Seitenkennziffer aus 1.7.10
 * (NORTH 2, SOUTH 3, WEST 4, EAST 5), plus 6, wenn das Zahnrad flach liegt.
 * Das Original schaltet kein Culling ab, also bleibt es auch hier an.
 *
 * Das Original waehlt anhand der Metadaten zwischen stirling_tex und
 * stirling_steel_tex. Der Port kennt nur STIRLING_TEX, weil es weder
 * machine_stirling_steel noch dessen Textur gibt -- nachgemalt wird nichts,
 * also bekommen alle Untertypen vorerst die eine vorhandene Textur.
 */
public class RenderCog extends EntityRenderer<Cog> {

    public RenderCog(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Cog cog, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        int orientation = cog.getOrientation();

        switch(orientation % 6) {
            case 3 -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case 5 -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case 2 -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case 4 -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            default -> { }
        }

        RenderContext.translate(0F, 0F, -1F);

        if(orientation < 6) {
            RenderContext.mulPose(Axis.ZN.rotationDegrees((float) (System.currentTimeMillis() % (360 * 3) / 3D)));
        }

        RenderContext.translate(0F, -1.375F, 0F);

        RenderSystem.setShaderTexture(0, this.getTextureLocation(cog));
        ResourceManager.stirling.renderPart("Cog");

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(Cog cog) {
        return ResourceManager.STIRLING_TEX;
    }
}
