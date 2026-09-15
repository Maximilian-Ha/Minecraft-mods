package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineTurbineGasBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTurbineGas.
 * Drehwinkel je Blickrichtung unveraendert uebernommen.
 */
public class RenderTurbineGas extends BlockEntityRendererNT<MachineTurbineGasBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineTurbineGasBlockEntity> create(Context context) { return new RenderTurbineGas(); }

    @Override
    public void render(MachineTurbineGasBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        // Das Original schaltet fuer die Gasturbine das Backface-Culling ab
        // (RenderTurbineGas.java:33), sonst fehlen Rueckseiten am Modell.
        RenderSystem.disableCull();
        bindTexture(ResourceManager.TURBINEGAS_TEX);
        ResourceManager.turbinegas.renderAll();
        RenderSystem.enableCull();
    }

    @Override
    public int getPacketLight(int packedLight, MachineTurbineGasBlockEntity be) {
        if(be.getLevel() != null && be.getBlockState().getBlock() instanceof DummyableBlock dummy) {
            return LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above(dummy.getDimensions()[0]));
        }
        return packedLight;
    }

    @Override
    public AABB getRenderBoundingBox(MachineTurbineGasBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_TURBINEGAS.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 1.5F);
                RenderContext.scale(2.5F, 2.5F, 2.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.75F, 0.75F, 0.75F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                // Auch im Gegenstandsrenderer ohne Culling (Original Z. 56).
                RenderSystem.disableCull();
                bindTexture(ResourceManager.TURBINEGAS_TEX);
                ResourceManager.turbinegas.renderAll();
                RenderSystem.enableCull();
            }
        };
    }
}
