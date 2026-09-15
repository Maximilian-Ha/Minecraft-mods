package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineRockMillBlockEntity;
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

public class RenderRockMill extends BlockEntityRendererNT<MachineRockMillBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineRockMillBlockEntity> create(Context context) {
        return new RenderRockMill();
    }

    @Override
    public void render(MachineRockMillBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> { }
        }

        bindTexture(ResourceManager.ROCK_MILL_TEX);
        ResourceManager.rock_mill.renderPart("Base");
        if(be.frame) ResourceManager.rock_mill.renderPart("Frame");

        float rot = be.prevRotation + (be.rotation - be.prevRotation) * partialTicks;
        RenderContext.mulPose(Axis.YN.rotationDegrees(rot));
        ResourceManager.rock_mill.renderPart("Wheel");
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch. Die Maschine liefert
     * ihre tatsaechliche Ausdehnung selbst.
     */
    @Override
    public AABB getRenderBoundingBox(MachineRockMillBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_ROCK_MILL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(3F, 3F, 3F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.75F, 0.75F, 0.75F);

                bindTexture(ResourceManager.ROCK_MILL_TEX);
                ResourceManager.rock_mill.renderAll();
            }
        };
    }
}
