package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.WatzBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderWatz.
 *
 * Ein Segment ist rund; es wird nicht gedreht.
 */
public class RenderWatz extends BlockEntityRendererNT<WatzBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<WatzBlockEntity> create(Context context) { return new RenderWatz(); }

    @Override
    public void render(WatzBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        RenderSystem.disableCull();
        bindTexture(ResourceManager.WATZ_TEX); ResourceManager.watz.renderAll();
        RenderSystem.enableCull();
    }

    @Override
    public int getPacketLight(int packedLight, WatzBlockEntity be) {
        if(be.getLevel() != null && be.getBlockState().getBlock() instanceof DummyableBlock dummy) {
            return LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above(dummy.getDimensions()[0]));
        }
        return packedLight;
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.WATZ.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2F, 2F, 2F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.6F, 0.6F, 0.6F);
                bindTexture(ResourceManager.WATZ_TEX); ResourceManager.watz.renderAll();
            }
        };
    }
}
