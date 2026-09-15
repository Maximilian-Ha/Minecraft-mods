package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionBreederBlockEntity;
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

/** Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionBreeder. */
public class RenderFusionBreeder extends BlockEntityRendererNT<FusionBreederBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionBreederBlockEntity> create(Context context) { return new RenderFusionBreeder(); }

    @Override
    public void render(FusionBreederBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        }));

        bindTexture(ResourceManager.FUSION_BREEDER_TEX);
        ResourceManager.fusion_breeder.renderPart("Breeder");
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_BREEDER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(2.5F, 2.5F, 2.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.FUSION_BREEDER_TEX);
                ResourceManager.fusion_breeder.renderPart("Breeder");
            }
        };
    }
}
