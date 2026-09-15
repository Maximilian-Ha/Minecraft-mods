package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.pile.PileControlBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.pile.PileDeviceBlock;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPileControl.
 */
public class RenderPileControl extends BlockEntityRendererNT<PileControlBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<PileControlBlockEntity> create(Context context) { return new RenderPileControl(); }

    @Override
    public void render(PileControlBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(RenderPileLoader.facingAngle(be.getBlockState().getValue(PileDeviceBlock.FACING))));

        bindTexture(ResourceManager.PILE_CONTROL_TEX);
        ResourceManager.pile_control.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.PILE_CONTROL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3.5F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2F, 2F, 2F);
                bindTexture(ResourceManager.PILE_CONTROL_TEX);
                ResourceManager.pile_control.renderAll();
            }
        };
    }
}
