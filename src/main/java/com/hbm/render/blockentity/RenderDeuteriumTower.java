package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.DeuteriumTowerBlockEntity;
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

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderDeuteriumTower.
 *
 * Das Original dreht erst um 180 Grad und schiebt danach je Richtung anders -- weil sein Modell
 * ueber zwei Bloecke liegt und der Kern nicht in der Mitte sitzt. Die vier Faelle sind
 * unveraendert uebernommen, nur dass die Richtung aus dem Blockzustand kommt statt aus dem
 * Metadatum.
 */
public class RenderDeuteriumTower extends BlockEntityRendererNT<DeuteriumTowerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<DeuteriumTowerBlockEntity> create(Context context) {
        return new RenderDeuteriumTower();
    }

    @Override
    public void render(DeuteriumTowerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        switch(facing) {
            case NORTH -> RenderContext.translate(0F, 0F, -1F);
            case SOUTH -> { RenderContext.mulPose(Axis.YP.rotationDegrees(180F)); RenderContext.translate(1F, 0F, 0F); }
            case WEST -> { RenderContext.mulPose(Axis.YP.rotationDegrees(90F)); RenderContext.translate(1F, 0F, -1F); }
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            default -> { }
        }

        bindTexture(ResourceManager.DEUTERIUM_TOWER_TEX);
        ResourceManager.deuterium_tower.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_DEUTERIUM_TOWER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(3F, 3F, 3F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.DEUTERIUM_TOWER_TEX);
                ResourceManager.deuterium_tower.renderAll();
            }
        };
    }
}
