package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.albion.MachinePAQuadrupoleBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPAQuadrupole.
 *
 * Runde 168 nachgereicht: der Quadrupolmagnet steht seit Runde 133, hatte aber keinen Darsteller und war
 * daher in der Welt wie im Inventar unsichtbar. Damit ist die Schuldenliste aus Runde 160 leer.
 */
public class RenderPAQuadrupole extends BlockEntityRendererNT<MachinePAQuadrupoleBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachinePAQuadrupoleBlockEntity> create(Context context) { return new RenderPAQuadrupole(); }

    @Override
    public void render(MachinePAQuadrupoleBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, -1F, 0.5F);

        /* Die Zuordnung stammt woertlich aus dem Original: 4=WEST 180, 3=SUED 270, 5=OST 0, 2=NORD 90. */
        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            default -> { }
        }

        RenderSystem.enableCull();
        bindTexture(ResourceManager.PA_QUADRUPOLE_TEX);
        ResourceManager.pa_quadrupole.renderAll();
    }

    @Override
    public AABB getRenderBoundingBox(MachinePAQuadrupoleBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_PA_QUADRUPOLE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3.5F, 0F);
                RenderContext.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.PA_QUADRUPOLE_TEX);
                ResourceManager.pa_quadrupole.renderAll();
            }
        };
    }
}
