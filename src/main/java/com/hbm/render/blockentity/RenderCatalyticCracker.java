package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineCatalyticCrackerBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCatalyticCracker.
 * Ein Teil, keine Bewegung -- nur die Drehung nach der Blickrichtung.
 */
public class RenderCatalyticCracker extends BlockEntityRendererNT<MachineCatalyticCrackerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineCatalyticCrackerBlockEntity> create(Context context) {
        return new RenderCatalyticCracker();
    }

    @Override
    public void render(MachineCatalyticCrackerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        RenderSystem.disableCull();
        bindTexture(ResourceManager.CATALYTIC_CRACKER_TEX);
        ResourceManager.catalyticCracker.renderAll();
        RenderSystem.enableCull();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles ausserhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block gross.
     */
    @Override
    public AABB getRenderBoundingBox(MachineCatalyticCrackerBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_CATALYTIC_CRACKER.asItem();
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
                RenderContext.scale(0.2F, 0.2F, 0.2F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderSystem.disableCull();
                bindTexture(ResourceManager.CATALYTIC_CRACKER_TEX);
                ResourceManager.catalyticCracker.renderAll();
                RenderSystem.enableCull();
            }
        };
    }
}
