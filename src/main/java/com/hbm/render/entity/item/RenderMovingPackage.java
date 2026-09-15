package com.hbm.render.entity.item;

import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.item.MovingPackage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Zeichnet das Paket, das auf einem Foerderband faehrt: eine Kiste, halb so gross wie ein Block.
 *
 * Das Original nimmt dafuer die hoelzerne Kiste und zeichnet sie doppelt so gross wie einen
 * gewoehnlichen Gegenstand. Der Port hat keine hoelzerne Kiste -- seine Kisten sind aus Metall --,
 * also steht hier die eiserne. Gemeint ist in beiden Faellen dasselbe: man sieht eine Kiste
 * fahren und weiss, dass darin mehreres steckt.
 */
@OnlyIn(Dist.CLIENT)
public class RenderMovingPackage extends EntityRenderer<MovingPackage> {

    private final ItemRenderer itemRenderer;
    private ItemStack crate = ItemStack.EMPTY;

    public RenderMovingPackage(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.25F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public void render(MovingPackage entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if(this.crate.isEmpty()) this.crate = new ItemStack(NtmBlocks.CRATE_IRON.get());

        poseStack.pushPose();

        /* Auf halbe Blockgroesse und auf die Bandhoehe gesetzt. */
        poseStack.translate(0F, 0.25F, 0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);

        BakedModel model = this.itemRenderer.getModel(this.crate, entity.level(), null, entity.getId());
        this.itemRenderer.render(this.crate, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingPackage entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
