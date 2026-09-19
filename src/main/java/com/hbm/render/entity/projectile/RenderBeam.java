package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.BulletBeamBase;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderBeam.
 *
 * Zeichnet gar nichts selbst. Was ein Strahl aussieht, haengt an seiner Munition, nicht an
 * seiner Entitaet -- deshalb reicht diese Klasse an config.rendererBeam weiter, genau wie
 * RenderBulletMK4 an config.renderer. Eine Konfiguration ohne Zeichner bleibt unsichtbar,
 * und das ist kein Fehler: der Strahl WIRKT trotzdem, sein Schussweg ist schon im
 * Konstruktor abgerechnet.
 *
 * NICHT UEBERNOMMEN: das Ab- und Anschalten des Nebels. Das Original muss GL_FOG von Hand
 * ausknipsen, damit der Strahl auf Entfernung nicht verblasst; auf 1.21 laeuft das ueber den
 * Renderzustand des Puffers, und der Leuchtzeichner in RenderContext bringt ihn schon mit.
 */
public class RenderBeam extends EntityRenderer<BulletBeamBase> {

    public RenderBeam(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BulletBeamBase strahl, float yRot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if(strahl.config == null) strahl.config = strahl.getBulletConfig();
        if(strahl.config == null) return;

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        if(strahl.config.renderRotations) {
            RenderContext.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, strahl.yRotO, strahl.yRot) - 90.0F));
            RenderContext.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, strahl.xRotO, strahl.xRot) + 180));
        }

        if(strahl.config.rendererBeam != null) {
            strahl.config.rendererBeam.accept(strahl, partialTick);
        }

        RenderContext.end();
    }

    @Override public ResourceLocation getTextureLocation(BulletBeamBase strahl) { return ResourceManager.EMPTY; }
}
