package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.RBMKColumn;
import com.hbm.render.NtmRenderTypes;
import com.hbm.util.ColorUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * Das Saeulenraster, wie es Reaktorpult und Rasteranzeige gleichermassen zeichnen.
 *
 * Ein Quadrat je Saeule, eingefaerbt nach Hitze oder Farbmarkierung, und darauf bei Brennstoff-
 * und Regelsaeulen ein achteckiger Punkt. Beide Renderer benutzen dieselben Zahlen; im Original
 * stehen sie zweimal da, einmal je Renderer.
 */
public final class RBMKGridPainter {

    private RBMKGridPainter() { }

    /** Halbe Kantenlaenge des Saeulenquadrats. */
    private static final float SQUARE = 0.0625F * 0.75F;
    /** Halbe Kantenlaenge des Punkts, und seine schraege Ecke. */
    private static final float DOT = 0.03125F;
    private static final float DOT_EDGE = 0.022097F;

    /** Die Farben der Regelstabmarkierungen, in der Reihenfolge von RBMKColor. */
    private static final int[] COLORS = { 0xFF0000, 0xFFFF00, 0x008000, 0x0000FF, 0x8000FF };

    public static VertexConsumer consumer(MultiBufferSource buffer) {
        return buffer.getBuffer(NtmRenderTypes.SOLID_COLOR);
    }

    /**
     * Zeichnet eine Saeule samt ihrem Punkt.
     *
     * @param x     wie weit das Raster vor der Tafel schwebt
     * @param index Stelle im Raster; jede zweite Spalte wird eine Spur heller
     */
    public static void column(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int index, RBMKColumn col) {

        float r;
        float g;
        float b;

        if(col.data.getByte("indicator") > 0) {
            /* Die Saeule, die der Kran gerade anfaehrt. */
            r = 1F; g = 1F; b = 0F;

        } else if(col.data.contains("color") && col.data.getShort("color") >= 0) {
            int color = COLORS[Math.min(col.data.getShort("color"), COLORS.length - 1)];
            r = ColorUtil.fr(color); g = ColorUtil.fg(color); b = ColorUtil.fb(color);

        } else {
            /*
             * Sonst die Hitze. Jede zweite Spalte ist eine Spur heller, damit das Raster nicht zu
             * einer Flaeche verschwimmt.
             */
            double maxHeat = col.data.getDouble("maxHeat");
            float heat = maxHeat <= 0D ? 0F : (float) (col.data.getDouble("heat") / maxHeat);
            float base = 0.65F + (index % 2) * 0.05F;
            r = base + (1F - base) * heat; g = base; b = base;
        }

        quad(consumer, matrix, x, r, g, b,
                y + SQUARE, z - SQUARE,
                y + SQUARE, z + SQUARE,
                y - SQUARE, z + SQUARE,
                y - SQUARE, z - SQUARE);

        /* Der Punkt sitzt eine Winzigkeit vor dem Quadrat, damit er nicht damit streitet. */
        float dotX = x + 0.01F;

        switch(col.type) {
            case FUEL, FUEL_SIM -> dot(consumer, matrix, dotX, y, z, 0F, 0.25F + (float) col.data.getDouble("enrichment") * 0.75F, 0F);
            case CONTROL -> {
                float level = (float) col.data.getDouble("level");
                dot(consumer, matrix, dotX, y, z, level, level, 0F);
            }
            case CONTROL_AUTO -> {
                float level = (float) col.data.getDouble("level");
                dot(consumer, matrix, dotX, y, z, level, 0F, level);
            }
            default -> { }
        }
    }

    /** Der Punkt in der Mitte einer Saeule -- ein Achteck aus drei Vierecken, wie im Original. */
    private static void dot(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float r, float g, float b) {

        quad(consumer, matrix, x, r, g, b,
                y + DOT, z,
                y + DOT_EDGE, z + DOT_EDGE,
                y, z + DOT,
                y - DOT_EDGE, z + DOT_EDGE);

        quad(consumer, matrix, x, r, g, b,
                y + DOT_EDGE, z - DOT_EDGE,
                y + DOT, z,
                y - DOT_EDGE, z - DOT_EDGE,
                y, z - DOT);

        quad(consumer, matrix, x, r, g, b,
                y + DOT, z,
                y - DOT_EDGE, z + DOT_EDGE,
                y - DOT, z,
                y - DOT_EDGE, z - DOT_EDGE);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, float x, float r, float g, float b,
                             float y1, float z1, float y2, float z2, float y3, float z3, float y4, float z4) {

        consumer.addVertex(matrix, x, y1, z1).setColor(r, g, b, 1F);
        consumer.addVertex(matrix, x, y2, z2).setColor(r, g, b, 1F);
        consumer.addVertex(matrix, x, y3, z3).setColor(r, g, b, 1F);
        consumer.addVertex(matrix, x, y4, z4).setColor(r, g, b, 1F);
    }
}
