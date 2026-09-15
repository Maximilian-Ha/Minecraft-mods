package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.RBMKColumn;
import com.hbm.blockentity.machine.rbmk.RBMKDisplayBlockEntity;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKDisplay.
 *
 * Ein 7x7-Raster kleiner Quadrate auf der Tafel. Was ein Quadrat bedeutet, steht in
 * RBMKGridPainter -- das Reaktorpult zeichnet dasselbe Raster, nur groesser.
 *
 * ABWEICHUNG: das Original zeichnet ohne Textur und mit eigenem Vollhelligkeitsschalter. Hier
 * uebernimmt beides NtmRenderTypes.SOLID_COLOR.
 */
public class RenderRBMKDisplay extends BlockEntityRendererNT<RBMKDisplayBlockEntity> {

    /** Wie weit die Quadrate vor der Tafel schweben. Zahl aus dem Original. */
    private static final float PLATE_DEPTH = 0.28125F;
    private static final float CELL = 0.125F;

    @Override
    public BlockEntityRenderer<RBMKDisplayBlockEntity> create(Context context) {
        return new RenderRBMKDisplay();
    }

    @Override
    public void render(RBMKDisplayBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        /* Sieben Zeilen fuellen acht Sechzehntel -- das Raster wird passend gestreckt. */
        RenderContext.translate(0F, 0.5F, 0F);
        RenderContext.scale(1F, 8F / 7F, 8F / 7F);
        RenderContext.translate(0F, -0.5F, 0F);

        VertexConsumer consumer = RBMKGridPainter.consumer(buffer);
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        for(int i = 0; i < be.columns.length; i++) {

            RBMKColumn col = be.columns[i];
            if(col == null) continue;

            float y = -(i / RBMKDisplayBlockEntity.GRID) * CELL + 0.875F;
            float z = -(i % RBMKDisplayBlockEntity.GRID) * CELL + CELL * 3F;

            RBMKGridPainter.column(consumer, matrix, PLATE_DEPTH, y, z, i, col);
        }
    }
}
