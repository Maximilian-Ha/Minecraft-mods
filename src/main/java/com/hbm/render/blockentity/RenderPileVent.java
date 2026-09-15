package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.pile.PileVentBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPileVent.
 *
 * Das Modell hat kein eigenes Teil fuer das Luefterrad, also dreht sich auch nichts -- so wie
 * im Original. Den Winkel fuehrt die Blockentitaet trotzdem mit; er wird dort ebenfalls nicht
 * benutzt.
 */
public class RenderPileVent extends BlockEntityRendererNT<PileVentBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<PileVentBlockEntity> create(Context context) { return new RenderPileVent(); }

    @Override
    public void render(PileVentBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(RenderPileLoader.facingAngle(be.getBlockState().getValue(PileDeviceBlock.FACING))));

        bindTexture(ResourceManager.PILE_VENT_TEX);
        ResourceManager.pile_vent.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.PILE_VENT.asItem();
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
                bindTexture(ResourceManager.PILE_VENT_TEX);
                ResourceManager.pile_vent.renderAll();
            }
        };
    }
}
