package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.DemonLampBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.DemonLampBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderDemonLamp.
 *
 * Erst die Lampe selbst, dann der Lichtkegel: zwei Ringe zu je sechzehn Segmenten, die vom
 * Lampenrand nach aussen laufen und dabei durchsichtig werden -- einer nach oben, einer nach
 * unten. Additiv gemischt, damit sich die Segmente aufhellen statt einander zu verdecken.
 */
public class RenderDemonLamp extends BlockEntityRendererNT<DemonLampBlockEntity> implements IBEWLRProvider {

    /** Von hier laeuft der Kegel los. */
    private static final double NAH = 0.375D;

    /** Bis hierhin, dann ist er verschwunden. */
    private static final double FERN = 15D;

    private static final int SEGMENTE = 16;

    @Override
    public BlockEntityRenderer<DemonLampBlockEntity> create(Context context) {
        return new RenderDemonLamp();
    }

    @Override
    public void render(DemonLampBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0.5F, 0.5F);

        // Die Tabelle des Originals, eins zu eins uebernommen.
        switch(be.getBlockState().getValue(DemonLampBlock.FACING)) {
            case DOWN -> RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
            case UP -> { }
            case NORTH -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(180F)); }
            case SOUTH -> RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
            case WEST -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(90F)); }
            case EAST -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(270F)); }
        }

        RenderContext.translate(0F, -0.5F, 0F);

        bindTexture(ResourceManager.DEMON_LAMP_TEX);
        ResourceManager.demon_lamp.renderAll();

        this.zeichneKegel(buffer);
    }

    /** Der Lichtkegel. Zwei Ringe, der eine nach oben, der andere nach unten geneigt. */
    private void zeichneKegel(MultiBufferSource buffer) {

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.GLOW);
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        for(int ring = 0; ring < 2; ring++) {

            double neigung = ring == 0 ? -0.5D : 0.5D;
            double hoehe = 0.5D + ring * 0.125D;

            double winkel = 0;

            for(int i = 0; i < SEGMENTE; i++) {

                double x1 = Math.cos(winkel);
                double z1 = Math.sin(winkel);
                winkel += Math.PI * 2D / SEGMENTE;
                double x2 = Math.cos(winkel);
                double z2 = Math.sin(winkel);

                // Innen voll, aussen ausgeblendet -- 0x40 ist das Viertel des Originals.
                consumer.addVertex(matrix, (float) (x1 * NAH), (float) hoehe, (float) (z1 * NAH))
                        .setColor(0, 191, 255, 64).setNormal(0, 1, 0);
                consumer.addVertex(matrix, (float) (x1 * FERN), (float) (hoehe + neigung), (float) (z1 * FERN))
                        .setColor(0, 191, 255, 0).setNormal(0, 1, 0);
                consumer.addVertex(matrix, (float) (x2 * FERN), (float) (hoehe + neigung), (float) (z2 * FERN))
                        .setColor(0, 191, 255, 0).setNormal(0, 1, 0);
                consumer.addVertex(matrix, (float) (x2 * NAH), (float) hoehe, (float) (z2 * NAH))
                        .setColor(0, 191, 255, 64).setNormal(0, 1, 0);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(DemonLampBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    /* Der Kegel reicht fuenfzehn Bloecke weit -- er soll nicht verschwinden, sobald die
     * Lampe selbst aus dem Blickfeld geraet. Im Original steht dafuer INFINITE_EXTENT_AABB. */
    @Override
    public boolean shouldRenderOffScreen(DemonLampBlockEntity be) { return true; }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.LAMP_DEMON.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(8F, 8F, 8F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.DEMON_LAMP_TEX);
                ResourceManager.demon_lamp.renderAll();
            }
        };
    }
}
