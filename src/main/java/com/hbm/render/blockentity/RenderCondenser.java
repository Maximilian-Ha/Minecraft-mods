package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.CondenserPoweredBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCondenser.
 * Nutzt dasselbe OBJ wie der kompakte Verdichter, nur mit eigener Textur.
 */
public class RenderCondenser extends BlockEntityRendererNT<CondenserPoweredBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<CondenserPoweredBlockEntity> create(Context context) {
        return new RenderCondenser();
    }

    @Override
    public void render(CondenserPoweredBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        // Das Original schaltet vor dem Zeichnen GL_CULL_FACE ab
        RenderSystem.disableCull();

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> { }
        }

        bindTexture(ResourceManager.CONDENSER_TEX);
        ResourceManager.condenser.renderPart("Condenser");

        float rot = be.lastSpin + (be.spin - be.lastSpin) * partialTicks;

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

        RenderSystem.enableCull();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch. Die Maschine liefert
     * ihre tatsaechliche Ausdehnung selbst. Das Ergebnis wird bewusst nicht im Renderer
     * zwischengespeichert -- es gibt nur einen Renderer fuer alle Kondensatoren.
     */
    @Override
    public AABB getRenderBoundingBox(CondenserPoweredBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_CONDENSER_POWERED.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                // Original: translate(-1, -1, 0); der X-Versatz stammt aus dem alten GUI-Bezugssystem und entfaellt
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.75F, 0.75F, 0.75F);
                RenderContext.translate(0.5F, 0F, 0F);

                bindTexture(ResourceManager.CONDENSER_TEX);
                ResourceManager.condenser.renderAll();
            }
        };
    }
}
