package com.hbm.render.blockentity;

import com.hbm.blockentity.network.ConnectorSuperBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.network.ConnectorRedWireBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Portiert aus 1.7.10: com.hbm.render.tileentity.RenderConnectorSuper. */
public class RenderConnectorSuper extends RenderPylonBase<ConnectorSuperBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<ConnectorSuperBlockEntity> create(Context context) {
        return new RenderConnectorSuper();
    }

    @Override
    public void render(ConnectorSuperBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.pushPose();
        RenderContext.translate(0.5F, 0.5F, 0.5F);

        RenderConnector.applyFacing(be.getBlockState().hasProperty(ConnectorRedWireBlock.FACING) ? be.getBlockState().getValue(ConnectorRedWireBlock.FACING) : Direction.UP);

        RenderContext.translate(0F, -0.5F, 0F);

        bindTexture(ResourceManager.CONNECTOR_SUPER_TEX);
        ResourceManager.connector_super.renderAll();
        RenderContext.popPose();

        this.renderLinesGeneric(be, buffer);
    }

    @Override
    public AABB getRenderBoundingBox(ConnectorSuperBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RED_CONNECTOR_SUPER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(7F, 7F, 7F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2F, 2F, 2F);
                bindTexture(ResourceManager.CONNECTOR_SUPER_TEX);
                ResourceManager.connector_super.renderAll();
            }
        };
    }
}
