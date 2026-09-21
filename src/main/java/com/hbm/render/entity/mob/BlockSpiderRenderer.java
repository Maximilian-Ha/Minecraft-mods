package com.hbm.render.entity.mob;

import com.hbm.entity.mob.BlockSpider;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderBlockSpider samt ModelBlockSpider.
 *
 * Acht Beine aus einem OBJ-Modell, und darueber der Block, den die Spinne traegt.
 *
 * DIE BEINE LAUFEN GEGENLAEUFIG: die ungeraden schwenken in die eine Richtung und heben sich
 * dabei, die geraden in die andere und senken sich -- fuenf Tausendstel je Grad, wie im
 * Original. Der Schwenk selbst ist der uebliche Schrittkosinus.
 *
 * DIE DREHUNGEN DES ORIGINALS sind uebernommen, wie sie dastehen: erst neunzig Grad um die
 * Hochachse, dann hundertachtzig um die Tiefenachse, dann anderthalb Bloecke nach unten. Das
 * Modell liegt im OBJ auf dem Kopf.
 */
@OnlyIn(Dist.CLIENT)
public class BlockSpiderRenderer extends EntityRenderer<BlockSpider> {

    private static final String[] UNGERADE = { "Leg1", "Leg3", "Leg5", "Leg7" };
    private static final String[] GERADE = { "Leg2", "Leg4", "Leg6", "Leg8" };

    public BlockSpiderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
    }

    @Override
    public void render(BlockSpider spinne, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        BlockState zustand = spinne.getBlockZustand();

        float schwung = -(Mth.cos(spinne.walkAnimation.position(partialTick) * 0.6662F * 2.0F) * 0.4F)
                * spinne.walkAnimation.speed(partialTick) * 57.3F;

        poseStack.pushPose();

        poseStack.mulPose(Axis.YN.rotationDegrees(90F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
        poseStack.translate(0, -1.5F, 0);

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
        RenderSystem.setShaderTexture(0, ResourceManager.BLOCKSPIDER_TEX);

        RenderContext.pushPose();
        RenderContext.translate(0F, (float) (schwung * 0.005), 0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(schwung));
        for(String bein : UNGERADE) ResourceManager.blockspider.renderPart(bein);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, (float) (schwung * -0.005), 0F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(schwung));
        for(String bein : GERADE) ResourceManager.blockspider.renderPart(bein);
        RenderContext.popPose();

        RenderContext.end();

        /* Der getragene Block. Das Original zeichnet ihn als Gegenstand; auf 1.21 zeichnet
         * der Blockzeichner ihn unmittelbar, und zwar um einen halben Block versetzt, damit
         * er wie dort mittig ueber den Beinen sitzt. */
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.25, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(zustand, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.popPose();

        super.render(spinne, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockSpider spinne) {
        return ResourceManager.BLOCKSPIDER_TEX;
    }
}
