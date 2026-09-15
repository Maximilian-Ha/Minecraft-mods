package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.AshpitBlockEntity;
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
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderAshpit.
 *
 * Benutzt dasselbe Modell wie der Backofen, nur mit eigener Textur -- so steht es auch im
 * Original.
 */
public class RenderAshpit extends BlockEntityRendererNT<AshpitBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<AshpitBlockEntity> create(Context context) {
        return new RenderAshpit();
    }

    @Override
    public void render(AshpitBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0.0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
        }
        RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));

        bindTexture(ResourceManager.ASHPIT_TEX);
        ResourceManager.heater_oven.renderPart("Main");

        RenderContext.pushPose();
        float door = be.prevDoorAngle + (be.doorAngle - be.prevDoorAngle) * partialTicks;
        RenderContext.translate(0.0F, 0.0F, door * 0.75F / 135F);
        ResourceManager.heater_oven.renderPart("Door");
        RenderContext.popPose();

        if(be.isFull) {
            ResourceManager.heater_oven.renderPart("InnerBurning");
        } else {
            ResourceManager.heater_oven.renderPart("Inner");
        }
    }

    @Override
    public AABB getRenderBoundingBox(AshpitBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_ASHPIT.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0.0F, -1F, 0.0F);
                RenderContext.scale(3.25F, 3.25F, 3.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.ASHPIT_TEX);
                ResourceManager.heater_oven.renderPart("Main");
                ResourceManager.heater_oven.renderPart("Door");
            }
        };
    }
}
