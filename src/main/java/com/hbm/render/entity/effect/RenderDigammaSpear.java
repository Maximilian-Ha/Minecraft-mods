package com.hbm.render.entity.effect;

import com.hbm.entity.effect.DigammaSpear;
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
 * Portiert aus 1.7.10: com.hbm.render.entity.effect.RenderSpear.
 *
 * Der Speer steht auf dem Kopf -- das Original dreht ihn um 180 Grad um die X-Achse und
 * verschiebt ihn fuenfzehn Bloecke nach oben, weil das Modell von seiner Spitze aus gebaut ist.
 *
 * NICHT UEBERNOMMEN: der zweite, eingeblendete Durchgang. Das Original zeichnet den Speer ein
 * zweites Mal, ohne Textur und mit wachsender Deckkraft, sobald er steht -- ein weisses Leuchten,
 * das ueber hundert Ticks aufzieht. Das haengt dort an glBlendFunc und glShadeModel von Hand;
 * auf 1.21 waere das ein eigener RenderType. Der Zaehler dafuer steht bereit
 * (DigammaSpear.getTicksInGround), damit das nachgereicht werden kann.
 */
public class RenderDigammaSpear extends EntityRenderer<DigammaSpear> {

    public RenderDigammaSpear(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DigammaSpear entity, float yaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, 0);
        RenderContext.translate(0F, 15F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
        RenderContext.scale(2F, 2F, 2F);

        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, ResourceManager.LANCE_TEX);
        ResourceManager.lance.renderPart("Spear");
        RenderSystem.enableCull();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(DigammaSpear entity) {
        return ResourceManager.LANCE_TEX;
    }
}
