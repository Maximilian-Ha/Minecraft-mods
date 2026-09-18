package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineCompressorCompactBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCompressorCompact.
 * Nutzt dasselbe OBJ wie der Kondensator, nur mit eigener Textur.
 */
public class RenderCompressorCompact extends BlockEntityRendererNT<MachineCompressorCompactBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineCompressorCompactBlockEntity> create(Context context) {
        return new RenderCompressorCompact();
    }

    @Override
    public void render(MachineCompressorCompactBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> { }
        }

        bindTexture(ResourceManager.COMPRESSOR_COMPACT_TEX);
        ResourceManager.condenser.renderPart("Condenser");

        float rot = be.prevFanSpin + (be.fanSpin - be.prevFanSpin) * partialTicks;

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.5F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
        RenderContext.translate(0F, -1.5F, 0F);
        ResourceManager.condenser.renderPart("Fan1");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.5F, 0F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(rot));
        RenderContext.translate(0F, -1.5F, 0F);
        ResourceManager.condenser.renderPart("Fan2");
        RenderContext.popPose();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch. Die Maschine liefert
     * ihre tatsaechliche Ausdehnung selbst.
     */
    @Override
    public AABB getRenderBoundingBox(MachineCompressorCompactBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_COMPRESSOR_COMPACT.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                // Original: translate(-1, -1, 0); der X-Versatz stammt aus dem alten GUI-Bezugssystem und entfaellt
                RenderContext.translate(-1F, -1F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.75F, 0.75F, 0.75F);
                RenderContext.translate(0.5F, 0F, 0F);

                bindTexture(ResourceManager.COMPRESSOR_COMPACT_TEX);
                ResourceManager.condenser.renderAll();
            }
        };
    }
}
