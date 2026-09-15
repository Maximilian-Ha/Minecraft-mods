package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.FurnaceIronBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFurnaceIron. */
public class RenderFurnaceIron extends BlockEntityRendererNT<FurnaceIronBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<FurnaceIronBlockEntity> create(Context context) {
        return new RenderFurnaceIron();
    }

    @Override
    public void render(FurnaceIronBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
        }

        RenderContext.translate(-0.5F, 0F, -0.5F);

        bindTexture(ResourceManager.FURNACE_IRON_TEX);
        ResourceManager.furnace_iron.renderPart("Main");

        if(be.wasOn) {
            FullBright.enable();
            RenderContext.setLightning(false);
            ResourceManager.furnace_iron.renderPart("On");
            RenderContext.setLightning(true);
            FullBright.disable();
        } else {
            ResourceManager.furnace_iron.renderPart("Off");
        }
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch.
     */
    @Override
    public AABB getRenderBoundingBox(FurnaceIronBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FURNACE_IRON.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.FURNACE_IRON_TEX);
                ResourceManager.furnace_iron.renderPart("Main");
                ResourceManager.furnace_iron.renderPart("Off");
            }
        };
    }
}
