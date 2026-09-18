package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionCollectorBlockEntity;
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

/** Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionCollector. */
public class RenderFusionCollector extends BlockEntityRendererNT<FusionCollectorBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionCollectorBlockEntity> create(Context context) { return new RenderFusionCollector(); }

    @Override
    public void render(FusionCollectorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        }));

        bindTexture(ResourceManager.FUSION_COLLECTOR_TEX);
        ResourceManager.fusion_collector.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_COLLECTOR.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(5F, 5F, 5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.FUSION_COLLECTOR_TEX);
                ResourceManager.fusion_collector.renderAll();
            }
        };
    }
}
