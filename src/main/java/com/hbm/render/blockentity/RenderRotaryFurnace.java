package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineRotaryFurnaceBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRotaryFurnace.
 *
 * Der Ofen selbst und der Kolben, der ihn antreibt: er laeuft umso schneller, je besser der
 * Brennstoff ist.
 */
public class RenderRotaryFurnace extends BlockEntityRendererNT<MachineRotaryFurnaceBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineRotaryFurnaceBlockEntity> create(Context context) {
        return new RenderRotaryFurnace();
    }

    @Override
    public void render(MachineRotaryFurnaceBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
        }

        RenderSystem.enableCull();
        bindTexture(ResourceManager.ROTARY_FURNACE_TEX);
        ResourceManager.rotary_furnace.renderPart("Furnace");

        float anim = BobMathUtil.interp(be.lastAnim, be.anim, partialTicks);

        RenderContext.pushPose();
        RenderContext.translate(0F, (float) (BobMathUtil.sps(anim * 0.75D * 0.125D) * 0.5D - 0.5D), 0F);
        ResourceManager.rotary_furnace.renderPart("Piston");
        RenderContext.popPose();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles ausserhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block gross.
     */
    @Override
    public AABB getRenderBoundingBox(MachineRotaryFurnaceBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_ROTARY_FURNACE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.625F, 0.625F, 0.625F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

                bindTexture(ResourceManager.ROTARY_FURNACE_TEX);
                ResourceManager.rotary_furnace.renderAll();
            }
        };
    }
}
