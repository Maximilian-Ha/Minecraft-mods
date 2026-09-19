package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.CoinEntity;
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
import net.minecraft.util.Mth;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderCoin.
 *
 * Die Muenze dreht sich um fuenfundvierzig Grad JE TICK -- nicht je Sekunde. Sie wirbelt
 * also sichtbar, und das ist Absicht: man soll sie im Flug finden koennen. Die Drehung
 * haengt an ticksExisted und laeuft deshalb mit der Spielzeit, anders als bei den
 * Propellern der C-130.
 */
public class RenderCoin extends EntityRenderer<CoinEntity> {

    public RenderCoin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CoinEntity muenze, float yRot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
        RenderSystem.setShaderTexture(0, ResourceManager.CHIP_GOLD_TEX);

        RenderContext.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTick, muenze.yRotO, muenze.yRot) - 90.0F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees((muenze.tickCount + partialTick) * 45F));

        float scale = 0.125F;
        RenderContext.scale(scale, scale, scale);

        ResourceManager.chip.renderAll();

        RenderContext.end();
    }

    @Override public ResourceLocation getTextureLocation(CoinEntity muenze) { return ResourceManager.CHIP_GOLD_TEX; }
}
