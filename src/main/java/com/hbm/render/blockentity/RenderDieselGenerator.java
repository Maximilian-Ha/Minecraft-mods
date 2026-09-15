package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineDieselBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.MachineDieselBlock;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderDieselGen.
 */
public class RenderDieselGenerator extends BlockEntityRendererNT<MachineDieselBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineDieselBlockEntity> create(Context context) {
        return new RenderDieselGenerator();
    }

    @Override
    public void render(MachineDieselBlockEntity be, MultiBufferSource buffer, float partialTicks) {
        Direction facing = be.getBlockState().getValue(MachineDieselBlock.FACING);

        RenderContext.translate(0.5F, 0.0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            default -> { }
        }

        bindTexture(ResourceManager.DIESEL_GENERATOR_TEX);
        ResourceManager.diesel_generator.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_DIESEL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0.0F, -0.5F, 0.0F);
                RenderContext.scale(2.5F, 2.5F, 2.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.DIESEL_GENERATOR_TEX);
                ResourceManager.diesel_generator.renderAll();
            }
        };
    }
}
