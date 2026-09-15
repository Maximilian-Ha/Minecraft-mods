package com.hbm.inventory.screens.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ScreenElements {

    public static void drawSmoothGauge(int x, int y, float progress, float tipLength, float backLength, float backSide, int color) {
        drawSmoothGauge(x, y, progress, tipLength, backLength, backSide, color, 0xFF000000);
    }

    private static final Vector3f tip = new Vector3f();
    private static final Vector3f left = new Vector3f();
    private static final Vector3f right = new Vector3f();

    public static void drawSmoothGauge(int x, int y, float progress, float tipLength, float backLength, float backSide, int color, int colorOuter) {

        progress = Mth.clamp(progress, 0, 1);

        // -progress * 270 - 45 became this because we are using Vector3f
        float angle = (float) Math.toRadians(progress * 270 + 45);

        tip.set(0, tipLength, 0);
        left.set(backSide, -backLength, 0);
        right.set(-backSide, -backLength, 0);

        tip.rotateZ(angle);
        left.rotateZ(angle);
        right.rotateZ(angle);

        float mult = 1.5F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(x + tip.x * mult, y + tip.y * mult, 1F).setColor(colorOuter);
        buffer.addVertex(x + left.x * mult, y + left.y * mult, 1F).setColor(colorOuter);
        buffer.addVertex(x + right.x * mult, y + right.y * mult, 1F).setColor(colorOuter);
        buffer.addVertex(x + tip.x, y + tip.y, 1F).setColor(color);
        buffer.addVertex(x + left.x, y + left.y, 1F).setColor(color);
        buffer.addVertex(x + right.x, y + right.y, 1F).setColor(color);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void drawSmoothLinearGauge(int x, int y, float progress, float tipLength, float backLength, float backSide, float scale, float rotation, int color) {
        drawSmoothLinearGauge(x, y, progress, tipLength, backLength, backSide, scale, rotation, color, 0xFF000000);
    }

    private static final Vector3f bLeft = new Vector3f();
    private static final Vector3f bRight = new Vector3f();

    public static void drawSmoothLinearGauge(int x, int y, float progress, float tipLength, float backLength, float backSide, float scale, float rotation, int color, int colorOuter) {

        scale = Math.max(scale, 1);
        progress = Math.clamp(progress, 0, 1) * scale;

        tip.set(0, -tipLength, 0);
        right.set(-backSide, 0, 0);
        bRight.set(-backSide, backLength, 0);
        bLeft.set(backSide, backLength, 0);
        left.set(backSide, 0, 0);

        float angle = (float) Math.toRadians(rotation);

        tip.rotateZ(angle);
        right.rotateZ(angle);
        bRight.rotateZ(angle);
        bLeft.rotateZ(angle);
        left.rotateZ(angle);

        float deltaX = progress * Mth.cos(angle);
        float deltaY = progress * Mth.sin(angle);

        float mult = 1.5F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(x + deltaX + tip.x * mult, y + deltaY + tip.y * mult, 1F).setColor(colorOuter);
        buffer.addVertex(x + deltaX + right.x * mult, y + deltaY + right.y * mult, 1F).setColor(colorOuter);
        buffer.addVertex(x + deltaX + bRight.x * mult, y + deltaY + bRight.y, 1F).setColor(colorOuter);
        buffer.addVertex(x + deltaX + bLeft.x * mult,  y + deltaY + bLeft.y, 1F).setColor(colorOuter);
        buffer.addVertex(x + deltaX + left.x * mult, y + deltaY + left.y * mult, 1F).setColor(colorOuter);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(x + deltaX + tip.x, y + deltaY + tip.y, 1F).setColor(color);
        buffer.addVertex(x + deltaX + right.x, y + deltaY + right.y, 1F).setColor(color);
        buffer.addVertex(x + deltaX + bRight.x, y + deltaY + bRight.y, 1F).setColor(color);
        buffer.addVertex(x + deltaX + bLeft.x, y + deltaY + bLeft.y, 1F).setColor(color);
        buffer.addVertex(x + deltaX + left.x, y + deltaY + left.y, 1F).setColor(color);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /**
     * Portiert aus 1.7.10: com.hbm.inventory.gui.element.GUIElements.drawSmoothTextureModalCircle.
     *
     * Blendet einen quadratischen Texturausschnitt kreisfoermig ein: der sichtbare
     * Bereich waechst mit dem Fortschritt vom unteren linken Rand aus gegen den
     * Uhrzeigersinn ueber 270 Grad. Wird von der Gasturbine als Drehzahlanzeige
     * benutzt. Die Texturkoordinaten gehen wie im Original von einer 256x256
     * grossen Textur aus.
     *
     * @param xDraw   linke Kante des Zielrechtecks auf dem Bildschirm
     * @param yDraw   obere Kante des Zielrechtecks auf dem Bildschirm
     * @param zDraw   Tiefe
     * @param xStart  linke Kante des Ausschnitts in der Textur
     * @param yStart  obere Kante des Ausschnitts in der Textur
     * @param xDelta  Breite des Ausschnitts
     * @param yDelta  Hoehe des Ausschnitts
     * @param progress Fortschritt 0..1
     */
    public static void drawSmoothTextureModalCircle(GuiGraphics guiGraphics, ResourceLocation texture, int xDraw, int yDraw, float zDraw, int xStart, int yStart, int xDelta, int yDelta, double progress) {

        float uvScale = 0.00390625F; // 1/256

        if(progress < 0D) progress = 0D;
        if(progress > 1D) progress = 1D;

        float angle = (float) (-progress * 270.0);
        double theta = Math.toRadians(angle - 135);
        int addons = 0;
        double xTarget = 0;
        double yTarget = 0;

        // addons ist nur ein Zaehler dafuer, wie viele feste Eckpunkte zusaetzlich gebraucht werden
        if(angle >= -180 && angle < -90) {
            addons = 1;
        } else if(angle >= -270 && angle < -180) {
            addons = 2;
        }

        // die folgende Fallunterscheidung legt den letzten, wandernden Punkt auf den Rand des Quadrats
        if(angle >= -90) {
            xTarget = -1;
            yTarget = -Math.tan(theta);
        } else if(angle > -135 && angle < -90) {
            xTarget = Math.tan(Math.PI / 2 - theta);
            yTarget = 1;
        } else if(angle > -180 && angle < -135) {
            xTarget = Math.tan(Math.PI / 2 - theta);
            yTarget = 1;
        } else if(angle <= -180) {
            xTarget = 1;
            yTarget = Math.tan(theta);
        } else if(angle == -135) {
            xTarget = 0;
            yTarget = 1;
        }

        double xMid = (double) xDelta / 2;
        double yMid = (double) yDelta / 2;

        xTarget *= xMid;
        yTarget *= yMid;

        // Eckpunkte des Faechers, relativ zur linken oberen Ecke des Ausschnitts
        double[] bottomLeft = new double[] { 0, yDelta };
        double[] middle = new double[] { xMid, yMid };
        double[] topLeft = new double[] { 0, 0 };
        double[] topRight = new double[] { xDelta, 0 };
        double[] target = new double[] { xTarget + xMid, -yTarget + yMid };

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);

        if(addons == 0) {
            addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, bottomLeft);
            addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, middle);
            addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, target);
        } else {
            addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, bottomLeft);
            addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, middle);
            addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, topLeft);

            if(addons == 2) {
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, topLeft);
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, middle);
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, topRight);

                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, topRight);
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, middle);
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, target);
            } else {
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, topLeft);
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, middle);
                addCircleVertex(buffer, matrix, xDraw, yDraw, zDraw, xStart, yStart, uvScale, target);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addCircleVertex(BufferBuilder buffer, Matrix4f matrix, int xDraw, int yDraw, float zDraw, int xStart, int yStart, float uvScale, double[] point) {
        buffer.addVertex(matrix, (float) (xDraw + point[0]), (float) (yDraw + point[1]), zDraw)
                .setUv((float) ((xStart + point[0]) * uvScale), (float) ((yStart + point[1]) * uvScale));
    }
}
