package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionCouplerBlockEntity;
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

/** Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionCoupler. */
public class RenderFusionCoupler extends BlockEntityRendererNT<FusionCouplerBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionCouplerBlockEntity> create(Context context) { return new RenderFusionCoupler(); }

    @Override
    public void render(FusionCouplerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        /* Die Werte enthalten die 90-Grad-Vordrehung, die im Original vor dem Schalter steht
         * (glRotatef(90, 0F, 1F, 0F)); so halten es auch RenderRockMill und RenderSolarBoiler. */
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 180F;
            case WEST -> 270F;
            case SOUTH -> 0F;
            default -> 90F;
        }));

        bindTexture(ResourceManager.FUSION_COUPLER_TEX);
        ResourceManager.fusion_coupler.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_COUPLER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(6F, 6F, 6F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.FUSION_COUPLER_TEX);
                ResourceManager.fusion_coupler.renderAll();
            }
        };
    }
}
