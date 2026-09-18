package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineVacuumDistillBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderVacuumDistill.
 */
public class RenderVacuumDistill extends BlockEntityRendererNT<MachineVacuumDistillBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineVacuumDistillBlockEntity> create(Context context) {
        return new RenderVacuumDistill();
    }

    @Override
    public void render(MachineVacuumDistillBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        bindTexture(ResourceManager.VACUUM_DISTILL_TEX);
        ResourceManager.vacuumDistill.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_VACUUM_DISTILL.asItem();
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
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(0.4F, 0.4F, 0.4F);
                bindTexture(ResourceManager.VACUUM_DISTILL_TEX);
                ResourceManager.vacuumDistill.renderAll();
            }
        };
    }
}
