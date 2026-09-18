package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineCatalyticReformerBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCatalyticReformer.
 */
public class RenderCatalyticReformer extends BlockEntityRendererNT<MachineCatalyticReformerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineCatalyticReformerBlockEntity> create(Context context) {
        return new RenderCatalyticReformer();
    }

    @Override
    public void render(MachineCatalyticReformerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        bindTexture(ResourceManager.CATALYTIC_REFORMER_TEX);
        ResourceManager.catalyticReformer.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_CATALYTIC_REFORMER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(0.4F, 0.4F, 0.4F);
                bindTexture(ResourceManager.CATALYTIC_REFORMER_TEX);
                ResourceManager.catalyticReformer.renderAll();
            }
        };
    }
}
