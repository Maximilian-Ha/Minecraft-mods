package com.hbm.render.blockentity;

import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Kleiner Mast, Holz und Stahl teilen sich das Modell */
public class RenderPylon extends RenderPylonBase<PylonBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<PylonBlockEntity> create(Context context) {
        return new RenderPylon();
    }

    @Override
    public void render(PylonBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        boolean steel = be.getBlockState().getBlock() == NtmBlocks.RED_PYLON_STEEL.get();

        RenderContext.pushPose();
        RenderContext.translate(0.5F, 0F, 0.5F);

        // Das Original schaltet fuer den kleinen Mast das Backface-Culling ab.
        RenderSystem.disableCull();
        bindTexture(steel ? ResourceManager.PYLON_STEEL_TEX : ResourceManager.PYLON_TEX);
        ResourceManager.pylon.renderPart(steel ? "Pylon_steel" : "Pylon");
        RenderSystem.enableCull();

        RenderContext.popPose();

        this.renderLinesGeneric(be, buffer);
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] {
                NtmBlocks.RED_PYLON.asItem(),
                NtmBlocks.RED_PYLON_STEEL.asItem()
        };
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RED_PYLON.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(2.9F, 2.9F, 2.9F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                boolean steel = stack.getItem() == NtmBlocks.RED_PYLON_STEEL.asItem();

                bindTexture(steel ? ResourceManager.PYLON_STEEL_TEX : ResourceManager.PYLON_TEX);
                ResourceManager.pylon.renderPart(steel ? "Pylon_steel" : "Pylon");
            }
        };
    }
}
