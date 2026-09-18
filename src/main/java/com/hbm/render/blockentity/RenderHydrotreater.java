package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineHydrotreaterBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderHydrotreater.
 */
public class RenderHydrotreater extends BlockEntityRendererNT<MachineHydrotreaterBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineHydrotreaterBlockEntity> create(Context context) {
        return new RenderHydrotreater();
    }

    @Override
    public void render(MachineHydrotreaterBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        bindTexture(ResourceManager.HYDROTREATER_TEX);
        ResourceManager.hydrotreater.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_HYDROTREATER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(0.4F, 0.4F, 0.4F);
                bindTexture(ResourceManager.HYDROTREATER_TEX);
                ResourceManager.hydrotreater.renderAll();
            }
        };
    }
}
