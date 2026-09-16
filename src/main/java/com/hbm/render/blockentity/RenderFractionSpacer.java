package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.SpacerBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFractionSpacer.
 *
 * Das Zwischenstueck zwischen zwei Fraktioniertuermen. Wie der Turm ohne Vorderseite.
 */
public class RenderFractionSpacer extends BlockEntityRendererNT<SpacerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<SpacerBlockEntity> create(Context context) {
        return new RenderFractionSpacer();
    }

    @Override
    public void render(SpacerBlockEntity be, MultiBufferSource buffer, float partialTicks) {
        RenderContext.translate(0.5F, 0F, 0.5F);
        bindTexture(ResourceManager.FRACTION_SPACER_TEX);
        ResourceManager.fractionSpacer.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FRACTION_SPACER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2.5F, 2.5F, 2.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(0.4F, 0.4F, 0.4F);
                bindTexture(ResourceManager.FRACTION_SPACER_TEX);
                ResourceManager.fractionSpacer.renderAll();
            }
        };
    }
}
