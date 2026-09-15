package com.hbm.render.entity.item;

import com.hbm.entity.item.MovingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
 * Zeichnet den Gegenstand, der auf einem Foerderband faehrt.
 *
 * Das Original laesst dafuer eine unsichtbare EntityItem mitlaufen und reicht sie an den
 * Gegenstandsrenderer weiter. Auf 1.21 braucht es diesen Umweg nicht: der Gegenstandsstapel
 * wird unmittelbar gezeichnet.
 *
 * ABWEICHUNG: das Original laesst den Gegenstand wie eine abgelegte Beute schweben und sich
 * drehen. Auf dem Band liegt er hier still -- die Fahrt ist die Bewegung, ein zusaetzliches
 * Kreiseln macht auf einer langen Strecke nur unruhig.
 */
@OnlyIn(Dist.CLIENT)
public class RenderMovingItem extends EntityRenderer<MovingItem> {

    private final ItemRenderer itemRenderer;

    public RenderMovingItem(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public void render(MovingItem entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        ItemStack stack = entity.getItemStack();
        if(stack.isEmpty()) return;

        poseStack.pushPose();

        /* Flach hingelegt, ein Stueck kleiner als in der Hand -- sonst ragt er ueber das Band. */
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90F));

        BakedModel model = this.itemRenderer.getModel(stack, entity.level(), null, entity.getId());
        this.itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingItem entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
