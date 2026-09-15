package com.hbm.render.blockentity;

import com.hbm.blockentity.network.ConnectorBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.network.ConnectorRedWireBlock;
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
import net.minecraft.world.phys.AABB;

/** Portiert aus 1.7.10: com.hbm.render.tileentity.RenderConnector. */
public class RenderConnector extends RenderPylonBase<ConnectorBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<ConnectorBlockEntity> create(Context context) {
        return new RenderConnector();
    }

    @Override
    public void render(ConnectorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.pushPose();
        RenderContext.translate(0.5F, 0.5F, 0.5F);

        applyFacing(be.getBlockState().hasProperty(ConnectorRedWireBlock.FACING) ? be.getBlockState().getValue(ConnectorRedWireBlock.FACING) : Direction.UP);

        RenderContext.translate(0F, -0.5F, 0F);

        bindTexture(ResourceManager.CONNECTOR_TEX);
        ResourceManager.connector.renderAll();
        RenderContext.popPose();

        this.renderLinesGeneric(be, buffer);
    }

    /** Entspricht dem Metadaten-Switch des Originals; die Seitenreihenfolge ist dieselbe. */
    protected static void applyFacing(Direction facing) {
        switch(facing) {
            case DOWN -> RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
            case UP -> { }
            case NORTH -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(180F)); }
            case SOUTH -> RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
            case WEST -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(90F)); }
            case EAST -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(270F)); }
        }
    }

    /** Ohne das wuerde alles ausserhalb des Kernblocks weggecullt -- die Leitungen reichen weit hinaus. */
    @Override
    public AABB getRenderBoundingBox(ConnectorBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RED_CONNECTOR.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3.5F, 0F);
                RenderContext.scale(7F, 7F, 7F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2F, 2F, 2F);
                bindTexture(ResourceManager.CONNECTOR_TEX);
                ResourceManager.connector.renderAll();
            }
        };
    }
}
