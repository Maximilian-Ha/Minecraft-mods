package com.hbm.render.entity.rocket;

import com.hbm.entity.missile.SoyuzCapsule;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.rocket.RenderSoyuzCapsule.
 *
 * Die Kapsel haengt am Fallschirm und pendelt. Das Original nimmt dafuer zwei Sinus,
 * gegeneinander um eine Viertelumdrehung versetzt, und dreht um beide waagerechten Achsen --
 * so schwingt sie nicht in einer Ebene, sondern kreist. Der Drehpunkt liegt sieben Bloecke
 * ueber ihr: dort haengt die Leine.
 */
public class RenderSoyuzCapsule extends EntityRenderer<SoyuzCapsule> {

    /** Wo die Leine haengt, in Bloecken ueber der Kapsel. */
    private static final double AUFHAENGUNG = 7D;

    public RenderSoyuzCapsule(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SoyuzCapsule entity, float yaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {

        double zeit = (entity.level().getGameTime() * 0.05D) % (Math.PI * 2) + partialTicks * 0.05D;
        double pendelZ = Math.sin(zeit) * 5D;
        double pendelX = Math.sin(zeit + Math.PI * 0.5D) * 5D;

        RenderContext.setup(poseStack, packedLight, 0);
        RenderContext.translate(0F, (float) AUFHAENGUNG, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees((float) pendelZ));
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) pendelX));
        RenderContext.translate(0F, (float) -AUFHAENGUNG, 0F);

        RenderSystem.setShaderTexture(0, ResourceManager.SOYUZ_LANDER_TEX);
        ResourceManager.soyuz_lander.renderPart("Capsule");

        RenderSystem.setShaderTexture(0, ResourceManager.SOYUZ_CHUTE_TEX);
        ResourceManager.soyuz_lander.renderPart("Chute");

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(SoyuzCapsule entity) {
        return ResourceManager.SOYUZ_LANDER_TEX;
    }
}
