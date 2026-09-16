package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachinePyroOvenBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPyroOven.
 *
 * Drei Teile: der Ofen selbst, der Schieber, der davor hin und her faehrt, und der Luefter,
 * der sich dreht. Beide bewegen sich nach demselben Zaehler.
 */
public class RenderPyroOven extends BlockEntityRendererNT<MachinePyroOvenBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachinePyroOvenBlockEntity> create(Context context) {
        return new RenderPyroOven();
    }

    @Override
    public void render(MachinePyroOvenBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            default -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        float anim = be.prevAnim + (be.anim - be.prevAnim) * partialTicks;

        bindTexture(ResourceManager.PYRO_OVEN_TEX);
        ResourceManager.pyroOven.renderPart("Oven");

        RenderContext.pushPose();
        RenderContext.translate((float) (BobMathUtil.sps(anim * 0.125D) / 2D - 0.5D), 0F, 0F);
        ResourceManager.pyroOven.renderPart("Slider");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(1.5F, 0F, 1.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) (anim * 45D % 360D)));
        RenderContext.translate(-1.5F, 0F, -1.5F);
        ResourceManager.pyroOven.renderPart("Fan");
        RenderContext.popPose();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles ausserhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block gross.
     */
    @Override
    public AABB getRenderBoundingBox(MachinePyroOvenBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_PYRO_OVEN.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.PYRO_OVEN_TEX);
                ResourceManager.pyroOven.renderAll();
            }
        };
    }
}
