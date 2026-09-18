package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.ChimneyIndustrialBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderChimneyIndustrial.
 *
 * Der Schornstein steht immer gleich herum, das Original dreht ihn nur um 180 Grad und
 * richtet ihn nicht nach der Blickrichtung aus.
 */
public class RenderChimneyIndustrial extends BlockEntityRendererNT<ChimneyIndustrialBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<ChimneyIndustrialBlockEntity> create(Context context) {
        return new RenderChimneyIndustrial();
    }

    @Override
    public void render(ChimneyIndustrialBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        bindTexture(ResourceManager.CHIMNEY_INDUSTRIAL_TEX);
        ResourceManager.chimney_industrial.renderAll();
    }

    /** Ohne diese Box cullt Minecraft alles oberhalb des Kernblocks weg. */
    @Override
    public AABB getRenderBoundingBox(ChimneyIndustrialBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.CHIMNEY_INDUSTRIAL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.CHIMNEY_INDUSTRIAL_TEX);
                ResourceManager.chimney_industrial.renderAll();
            }
        };
    }
}
