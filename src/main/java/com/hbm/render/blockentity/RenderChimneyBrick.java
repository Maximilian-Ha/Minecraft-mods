package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.ChimneyBrickBlockEntity;
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
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderChimneyBrick.
 *
 * Der Schornstein steht immer gleich herum, das Original dreht ihn nur um 180 Grad und
 * richtet ihn nicht nach der Blickrichtung aus.
 */
public class RenderChimneyBrick extends BlockEntityRendererNT<ChimneyBrickBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<ChimneyBrickBlockEntity> create(Context context) {
        return new RenderChimneyBrick();
    }

    @Override
    public void render(ChimneyBrickBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        bindTexture(ResourceManager.CHIMNEY_BRICK_TEX);
        ResourceManager.chimney_brick.renderAll();
    }

    /** Ohne diese Box cullt Minecraft alles oberhalb des Kernblocks weg. */
    @Override
    public AABB getRenderBoundingBox(ChimneyBrickBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.CHIMNEY_BRICK.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(2.25F, 2.25F, 2.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.CHIMNEY_BRICK_TEX);
                ResourceManager.chimney_brick.renderAll();
            }
        };
    }
}
