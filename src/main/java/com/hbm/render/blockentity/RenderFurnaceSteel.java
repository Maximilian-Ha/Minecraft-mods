package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.FurnaceSteelBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFurnaceSteel.
 *
 * Weggelassen: die additiv gezeichneten Glutscheiben vor der Ofenklappe. Das Original baut
 * sie von Hand aus Tessellator-Quads; im Port gibt es dafuer bislang kein Vorbild, darum
 * bleibt der Ofen ohne diesen Effekt.
 */
public class RenderFurnaceSteel extends BlockEntityRendererNT<FurnaceSteelBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<FurnaceSteelBlockEntity> create(Context context) {
        return new RenderFurnaceSteel();
    }

    @Override
    public void render(FurnaceSteelBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
        }

        RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));

        bindTexture(ResourceManager.FURNACE_STEEL_TEX);
        ResourceManager.furnace_steel.renderAll();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch.
     */
    @Override
    public AABB getRenderBoundingBox(FurnaceSteelBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FURNACE_STEEL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(3.25F, 3.25F, 3.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.FURNACE_STEEL_TEX);
                ResourceManager.furnace_steel.renderAll();
            }
        };
    }
}
