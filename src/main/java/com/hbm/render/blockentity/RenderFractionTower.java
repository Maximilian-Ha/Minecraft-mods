package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineFractionTowerBlockEntity;
import com.hbm.blocks.NtmBlocks;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFractionTower.
 *
 * Der Turm steht immer gleich -- er hat keine Vorderseite, also wird auch nicht gedreht.
 */
public class RenderFractionTower extends BlockEntityRendererNT<MachineFractionTowerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineFractionTowerBlockEntity> create(Context context) {
        return new RenderFractionTower();
    }

    @Override
    public void render(MachineFractionTowerBlockEntity be, MultiBufferSource buffer, float partialTicks) {
        RenderContext.translate(0.5F, 0F, 0.5F);
        bindTexture(ResourceManager.FRACTION_TOWER_TEX);
        ResourceManager.fractionTower.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_FRACTION_TOWER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2.5F, 0F);
                RenderContext.scale(3.25F, 3.25F, 3.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(0.4F, 0.4F, 0.4F);
                bindTexture(ResourceManager.FRACTION_TOWER_TEX);
                ResourceManager.fractionTower.renderAll();
            }
        };
    }
}
