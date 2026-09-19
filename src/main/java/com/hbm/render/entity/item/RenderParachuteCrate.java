package com.hbm.render.entity.item;

import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.item.ParachuteCrate;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.item.RenderParachuteCrate.
 *
 * Die Kiste pendelt unter ihrem Schirm. Zwei Sinuskurven, um eine Viertelwelle gegeneinander
 * versetzt, kippen sie um bis zu fuenf Grad in beide Achsen; der Drehpunkt liegt sieben Bloecke
 * ueber der Kiste, also dort, wo der Schirm haengt.
 *
 * ABWEICHUNG, woraus die Kiste gezeichnet wird: das Original nimmt dafuer das OBJ-Modell
 * conservecrate.obj. Der Port hat die Dosenkiste als gewoehnliches Blockmodell nachgebaut
 * (block/crate_can.json), und die Nachschubkiste teilt es sich mit ihr -- also wird hier
 * dasselbe Blockmodell gezeichnet statt eines zweiten Modells in einem anderen Format.
 */
public class RenderParachuteCrate extends EntityRenderer<ParachuteCrate> {

    /** Wo der Schirm haengt -- um diesen Punkt pendelt die Kiste. */
    private static final float AUFHAENGUNG = 7F;

    private final BlockRenderDispatcher blockRenderer;

    public RenderParachuteCrate(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(ParachuteCrate kiste, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        double zeit = kiste.level().getGameTime();
        float kippZ = (float) (Math.sin(zeit * 0.05) * 5);
        float kippX = (float) (Math.sin(zeit * 0.05 + Math.PI * 0.5) * 5);

        poseStack.pushPose();

        poseStack.translate(0F, AUFHAENGUNG, 0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(kippZ));
        poseStack.mulPose(Axis.XP.rotationDegrees(kippX));
        poseStack.translate(0F, -AUFHAENGUNG, 0F);

        poseStack.pushPose();
        poseStack.translate(-0.5F, 0F, -0.5F);
        this.blockRenderer.renderSingleBlock(NtmBlocks.CRATE_SUPPLY.get().defaultBlockState(),
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        /* Der Schirm sitzt einen Block ueber der Kiste -- im Original eine Verschiebung um -1
         * VOR dem Zeichnen, weil sein Modell nach unten haengt. */
        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
        RenderContext.translate(0F, -1F, 0F);
        RenderSystem.setShaderTexture(0, ResourceManager.SOYUZ_CHUTE_TEX);
        ResourceManager.soyuz_lander.renderPart("Chute");
        RenderContext.end();

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ParachuteCrate kiste) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
