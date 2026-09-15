package com.hbm.render.util;

import com.hbm.util.Clock;
import com.hbm.util.ColorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: der Markierungsteil von com.hbm.render.util.RenderOverhead.
 *
 * Der Server kann einzelne Bloecke im Sichtfeld des Spielers einrahmen und beschriften -- so
 * melden die grossen Reaktoren, warum sich ihr Bau nicht zusammensetzen laesst ("Non-reactor
 * block" an genau der Stelle, an der der falsche Block steht). Eine Markierung verfaellt nach
 * ihrer Frist oder sobald der Spieler weit genug weg ist.
 *
 * ABWEICHUNGEN:
 * - Das Original zeichnet den Rahmen mit dem Tessellator und GL_LINES, hier uebernimmt das
 *   RenderType.lines().
 * - Die Entfernungsangabe erscheint im Original, wenn der Blick fast genau auf die Markierung
 *   zeigt. Das ist wortgetreu uebernommen, samt der Schranke 0.15.
 */
public class RenderMarkers {

    /** Was der Netzwerkhandler hineinlegt; wird beim naechsten Zeichnen uebernommen. */
    public static final Map<BlockPos, Marker> queued = new HashMap<>();
    private static final Map<BlockPos, Marker> active = new HashMap<>();

    public static void queue(BlockPos pos, Marker marker) {
        queued.put(pos, marker);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, Camera camera) {

        active.putAll(queued);
        queued.clear();

        if(active.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null) return;

        Vec3 eye = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(-eye.x, -eye.y, -eye.z);

        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        List<Map.Entry<BlockPos, Marker>> visible = new ArrayList<>();

        Iterator<Map.Entry<BlockPos, Marker>> it = active.entrySet().iterator();

        while(it.hasNext()) {

            Map.Entry<BlockPos, Marker> entry = it.next();
            BlockPos pos = entry.getKey();
            Marker marker = entry.getValue();

            box(consumer, matrix, pos, marker);
            visible.add(entry);

            if(marker.expire > 0 && Clock.get_ms() > marker.expire) {
                it.remove();
            } else if(marker.maxDist > 0 && center(pos, marker).distanceTo(eye) > marker.maxDist) {
                it.remove();
            }
        }

        poseStack.popPose();

        for(Map.Entry<BlockPos, Marker> entry : visible) label(poseStack, buffer, camera, entry.getKey(), entry.getValue());
    }

    /** Die zwoelf Kanten des Rahmens. */
    private static void box(VertexConsumer consumer, Matrix4f matrix, BlockPos pos, Marker marker) {

        float r = ColorUtil.fr(marker.color);
        float g = ColorUtil.fg(marker.color);
        float b = ColorUtil.fb(marker.color);

        float x1 = (float) (pos.getX() + marker.minX);
        float y1 = (float) (pos.getY() + marker.minY);
        float z1 = (float) (pos.getZ() + marker.minZ);
        float x2 = (float) (pos.getX() + marker.maxX);
        float y2 = (float) (pos.getY() + marker.maxY);
        float z2 = (float) (pos.getZ() + marker.maxZ);

        // unten
        edge(consumer, matrix, x1, y1, z1, x2, y1, z1, r, g, b);
        edge(consumer, matrix, x2, y1, z1, x2, y1, z2, r, g, b);
        edge(consumer, matrix, x2, y1, z2, x1, y1, z2, r, g, b);
        edge(consumer, matrix, x1, y1, z2, x1, y1, z1, r, g, b);
        // oben
        edge(consumer, matrix, x1, y2, z1, x2, y2, z1, r, g, b);
        edge(consumer, matrix, x2, y2, z1, x2, y2, z2, r, g, b);
        edge(consumer, matrix, x2, y2, z2, x1, y2, z2, r, g, b);
        edge(consumer, matrix, x1, y2, z2, x1, y2, z1, r, g, b);
        // senkrecht
        edge(consumer, matrix, x1, y1, z1, x1, y2, z1, r, g, b);
        edge(consumer, matrix, x2, y1, z1, x2, y2, z1, r, g, b);
        edge(consumer, matrix, x2, y1, z2, x2, y2, z2, r, g, b);
        edge(consumer, matrix, x1, y1, z2, x1, y2, z2, r, g, b);
    }

    private static void edge(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {

        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float n = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if(n == 0) { nx = 0; ny = 1; nz = 0; } else { nx /= n; ny /= n; nz /= n; }

        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, 1F).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, 1F).setNormal(nx, ny, nz);
    }

    /**
     * Die Beschriftung. Sie haengt immer hoechstens sechzehn Bloecke vor dem Spieler, damit sie
     * auch aus der Ferne lesbar bleibt -- so macht es das Original.
     */
    private static void label(PoseStack poseStack, MultiBufferSource buffer, Camera camera, BlockPos pos, Marker marker) {

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null) return;

        Vec3 eye = camera.getPosition();
        Vec3 toMarker = center(pos, marker).subtract(eye);

        double dist = toMarker.length();
        if(dist == 0) return;

        Vec3 placed = toMarker.scale(Math.min(dist, 16D) / dist);

        String text = marker.label == null ? "" : marker.label;

        Vec3 look = mc.player.getLookAngle();
        Vec3 dir = toMarker.normalize();
        boolean staring = Math.abs(look.x - dir.x) + Math.abs(look.y - dir.y) + Math.abs(look.z - dir.z) < 0.15D;
        if(staring) text += (text.isEmpty() ? "" : " ") + (int) dist + "m";

        if(text.isEmpty()) return;

        Font font = mc.font;

        poseStack.pushPose();
        poseStack.translate(placed.x, placed.y, placed.z);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        font.drawInBatch(text, -font.width(text) / 2F, 0, marker.color, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);

        poseStack.popPose();
    }

    private static Vec3 center(BlockPos pos, Marker marker) {
        return new Vec3(
                pos.getX() + (marker.maxX + marker.minX) / 2D,
                pos.getY() + (marker.maxY + marker.minY) / 2D,
                pos.getZ() + (marker.maxZ + marker.minZ) / 2D);
    }

    /** Eine einzelne Markierung: Rahmen, Farbe, Beschriftung, Frist und Hoechstentfernung. */
    public static class Marker {

        public double minX = 0;
        public double minY = 0;
        public double minZ = 0;
        public double maxX = 1;
        public double maxY = 1;
        public double maxZ = 1;

        public final int color;
        public String label;

        /** Zeitpunkt in Millisekunden, ab dem die Markierung verschwindet. Null heisst nie. */
        public long expire;
        /** Entfernung, ab der die Markierung verschwindet. Null heisst nie. */
        public double maxDist;

        public Marker(int color) { this.color = color; }

        public Marker setExpire(long expire) { this.expire = expire; return this; }
        public Marker setDist(double maxDist) { this.maxDist = maxDist; return this; }
        public Marker withLabel(String label) { this.label = label; return this; }
    }
}
