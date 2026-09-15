package com.hbm.render.util;

import com.hbm.util.ColorUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * Portiert aus 1.7.10: com.hbm.render.util.RenderSparks.
 *
 * Zeichnet einen Funkenschlag: ein Streckenzug, der vom Startpunkt aus in zufaellige Richtungen
 * weiterspringt, jede Strecke zweimal -- einmal breit in der Aussenfarbe, einmal schmal in der
 * Kernfarbe. Der Zufall haengt am uebergebenen Keim, derselbe Keim ergibt also denselben Funken.
 *
 * ABWEICHUNGEN:
 * - Das Original stellt die Strichbreite mit glLineWidth auf 5 und 2. In 1.21 zeichnet
 *   RenderType.lines() mit einer festen Breite; die beiden Durchgaenge unterscheiden sich daher
 *   nur noch durch die Farbe, nicht durch die Dicke.
 * - Das Original schaltet Textur und Beleuchtung von Hand ab. RenderType.lines() bringt beides
 *   schon mit.
 */
public class RenderSparks {

    public static void renderSpark(MultiBufferSource buffer, int seed, double x, double y, double z,
                                   float length, int min, int max, int color1, int color2) {

        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        Random rand = new Random(seed);

        // Die Sprungrichtung wird einmal gezogen und bleibt fuer den ganzen Funken dieselbe.
        double dirX = rand.nextDouble() - 0.5D;
        double dirY = rand.nextDouble() - 0.5D;
        double dirZ = rand.nextDouble() - 0.5D;
        double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if(len == 0) return;
        dirX /= len;
        dirY /= len;
        dirZ /= len;

        int segments = min + rand.nextInt(max);

        for(int i = 0; i < segments; i++) {

            double prevX = x;
            double prevY = y;
            double prevZ = z;

            x = prevX + dirX * length * rand.nextFloat();
            y = prevY + dirY * length * rand.nextFloat();
            z = prevZ + dirZ * length * rand.nextFloat();

            line(consumer, matrix, prevX, prevY, prevZ, x, y, z, color1);
            line(consumer, matrix, prevX, prevY, prevZ, x, y, z, color2);
        }
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix,
                             double x1, double y1, double z1, double x2, double y2, double z2, int color) {

        float r = ColorUtil.fr(color);
        float g = ColorUtil.fg(color);
        float b = ColorUtil.fb(color);

        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float n = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if(n == 0) { nx = 0; ny = 1; nz = 0; } else { nx /= n; ny /= n; nz /= n; }

        consumer.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 1F).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(r, g, b, 1F).setNormal(nx, ny, nz);
    }
}
