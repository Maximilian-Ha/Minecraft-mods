package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKGraphBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKGraphBlockEntity.GraphUnit;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKGraph.
 *
 * Zwei Kurvenschreiber. Das Gehaeuse ist dasselbe Modell wie bei der Ziffernanzeige; auf seiner
 * Scheibe wird ein Linienzug aus den dreissig Messwerten gezogen, dazu die beiden Skalenendwerte
 * am linken Rand.
 *
 * ABWEICHUNG: das Original zieht die Linie als GL_LINES mit fester Breite und selbst
 * abgeschalteter Textur. Hier uebernimmt RenderType.lines() beides.
 */
/* Kein IBEWLRProvider: die Tafel traegt wie im Original ein flaches Sinnbild
 * (rbmk/rbmk_display) als Gegenstandsmodell, keinen eigenen Darsteller. */
public class RenderRBMKGraph extends BlockEntityRendererNT<RBMKGraphBlockEntity> {

    /** Wo die Kurve auf der Scheibe liegt -- Zahlen aus dem Original. */
    private static final float PLOT_DEPTH = 0.03225F;
    private static final float PLOT_BOTTOM = 0.5F - 0.03125F;
    private static final float PLOT_HEIGHT = 0.1875F;
    private static final float PLOT_LEFT = 0.375F;
    private static final float PLOT_WIDTH = 0.75F;

    @Override
    public BlockEntityRenderer<RBMKGraphBlockEntity> create(Context context) {
        return new RenderRBMKGraph();
    }

    @Override
    public void render(RBMKGraphBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        for(int i = 0; i < RBMKGraphBlockEntity.GRAPHS; i++) {

            GraphUnit unit = be.graphs[i];
            if(!unit.active) continue;

            RenderContext.pushPose();
            RenderContext.translate(0.25F, i * -0.5F + 0.25F, 0F);

            /* Gehaeuse und Scheibe teilt sich die Kurventafel mit der Ziffernanzeige. */
            bindTexture(RenderRBMKNumitron.TEXTURE);
            ResourceManager.rbmk_numitron.renderAll();

            long lowest = unit.minBound ? unit.min : BobMathUtil.min(unit.values);
            long highest = unit.maxBound ? unit.max : BobMathUtil.max(unit.values);

            this.drawCurve(unit, buffer, lowest, highest);
            this.drawScale(buffer, lowest, highest);
            this.drawLabel(unit, buffer);

            RenderContext.popPose();
        }
    }

    /** Der Linienzug aus den dreissig Messwerten, von links nach rechts. */
    private void drawCurve(GraphUnit unit, MultiBufferSource buffer, long lowest, long highest) {

        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        long range = Math.max(highest - lowest, 1);
        int last = unit.values.length - 1;

        for(int v = 0; v < last; v++) {

            float y1 = height(unit.values[v], lowest, highest, range);
            float y2 = height(unit.values[v + 1], lowest, highest, range);
            float z1 = PLOT_LEFT - v * PLOT_WIDTH / last;
            float z2 = PLOT_LEFT - (v + 1) * PLOT_WIDTH / last;

            /* Die Normale zeigt entlang der Linie; RenderType.lines() braucht sie fuer die Breite. */
            float ny = y2 - y1;
            float nz = z2 - z1;
            float n = (float) Math.sqrt(ny * ny + nz * nz);
            if(n == 0F) { ny = 0F; nz = 1F; } else { ny /= n; nz /= n; }

            consumer.addVertex(matrix, PLOT_DEPTH, y1, z1).setColor(0F, 1F, 0F, 1F).setNormal(0F, ny, nz);
            consumer.addVertex(matrix, PLOT_DEPTH, y2, z2).setColor(0F, 1F, 0F, 1F).setNormal(0F, ny, nz);
        }
    }

    /** Ein Messwert als Hoehe auf der Scheibe; was ausserhalb liegt, klebt am Rand. */
    private static float height(long value, long lowest, long highest, long range) {

        long clamped = Math.min(Math.max(value, lowest), highest);
        return PLOT_BOTTOM + (clamped - lowest) * PLOT_HEIGHT / range;
    }

    /** Die beiden Skalenendwerte am linken Rand, unten der Kleinst-, oben der Groesstwert. */
    private void drawScale(MultiBufferSource buffer, long lowest, long highest) {

        Font font = Minecraft.getInstance().font;
        String lower = String.valueOf(lowest);
        String upper = String.valueOf(highest);

        float lineScale = 0.0025F;

        RenderContext.pushPose();
        RenderContext.translate(0.032F, 0.5F - 0.03125F * 1.5F, -PLOT_LEFT + 0.03125F);
        RenderContext.scale(lineScale, -lineScale, lineScale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        FullBright.enable();

        font.drawInBatch(lower, -font.width(lower), -font.lineHeight / 2F, 0x00ff00, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());

        /* Der Abstand ist in Blockmassen gemeint, darum wird er durch den Massstab geteilt. */
        RenderContext.translate(0F, -0.03125F * 7F / lineScale, 0F);

        font.drawInBatch(upper, -font.width(upper), -font.lineHeight / 2F, 0x00ff00, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());

        FullBright.disable();
        RenderContext.popPose();
    }

    /** Die Beschriftung unter dem Schreiber. */
    private void drawLabel(GraphUnit unit, MultiBufferSource buffer) {

        if(unit.label == null || unit.label.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int width = font.width(unit.label);

        RenderContext.translate(0.01F, 0.3125F, 0F);

        /* Lange Beschriftungen werden kleiner, damit sie nicht ueber die Tafel hinauslaufen. */
        float scale = Math.min(0.0125F, 0.75F / Math.max(width, 1));
        RenderContext.scale(scale, -scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        FullBright.enable();
        font.drawInBatch(unit.label, -width / 2F, -font.lineHeight / 2F, 0x00ff00, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        FullBright.disable();
    }
}
