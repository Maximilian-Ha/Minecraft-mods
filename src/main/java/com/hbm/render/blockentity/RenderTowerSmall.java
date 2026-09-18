package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.TowerSmallBlockEntity;
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
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSmallTower.
 *
 * Der Turm steht rund; das Original dreht ihn nicht nach der Aufstellrichtung.
 */
public class RenderTowerSmall extends BlockEntityRendererNT<TowerSmallBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<TowerSmallBlockEntity> create(Context context) { return new RenderTowerSmall(); }

    @Override
    public void render(TowerSmallBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderSystem.disableCull();
        bindTexture(ResourceManager.TOWER_SMALL_TEX);
        ResourceManager.tower_small.renderAll();
        RenderSystem.enableCull();
    }

    @Override
    public AABB getRenderBoundingBox(TowerSmallBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_TOWER_SMALL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(3F, 3F, 3F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.25F, 0.25F, 0.25F);
                bindTexture(ResourceManager.TOWER_SMALL_TEX);
                ResourceManager.tower_small.renderAll();
            }
        };
    }
}
