package com.hbm.render.blockentity;

import com.hbm.blockentity.network.PylonLargeBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Grosser Mast */
public class RenderPylonLarge extends RenderPylonBase<PylonLargeBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<PylonLargeBlockEntity> create(Context context) {
        return new RenderPylonLarge();
    }

    @Override
    public void render(PylonLargeBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.pushPose();
        RenderContext.translate(0.5F, 0F, 0.5F);

        switch(getFacing(be)) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(135F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
            default -> { }
        }

        RenderSystem.disableCull();
        bindTexture(ResourceManager.PYLON_LARGE_TEX);
        ResourceManager.pylon_large.renderAll();
        RenderSystem.enableCull();

        RenderContext.popPose();

        this.renderLinesGeneric(be, buffer);
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RED_PYLON_LARGE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                // Das Original hatte fuer den grossen Mast keine Itemdarstellung; im Port braucht
                // das builtin/entity-Itemmodell eine, daher diese an das Modell angepassten Werte.
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(1.1F, 1.1F, 1.1F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderSystem.disableCull();
                bindTexture(ResourceManager.PYLON_LARGE_TEX);
                ResourceManager.pylon_large.renderAll();
                RenderSystem.enableCull();
            }
        };
    }
}
