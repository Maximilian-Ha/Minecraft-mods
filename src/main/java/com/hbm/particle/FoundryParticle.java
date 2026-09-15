package com.hbm.particle;

import com.hbm.main.NuclearTechMod;
import com.hbm.particle.engine.ParticleNT;
import com.hbm.render.NtmRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.particle.ParticleFoundry.
 *
 * Der Strahl fluessigen Metalls, der aus einem Ausguss faellt. Er besteht aus zwei Teilen: dem
 * kurzen Stueck, das waagerecht aus der Oeffnung tritt, und dem langen, das senkrecht nach unten
 * faellt. Beide bestehen aus flachen Vierecken, deren Bildausschnitt mit der Zeit wandert, damit
 * es fliesst.
 */
public class FoundryParticle extends ParticleNT {

    public static final ResourceLocation LAVA = NuclearTechMod.withDefaultNamespace("textures/particle/lava_gray.png");

    protected final int color;
    protected final Direction dir;
    /** wie weit das Metall unter dem Ansatzpunkt herunterfaellt */
    protected final double length;
    /** das Stueck, das direkt aus der Oeffnung kommt */
    protected final double base;
    /** wie weit der obere Teil nach hinten versetzt ist */
    protected final double offset;

    public FoundryParticle(ClientLevel level, double x, double y, double z, int color, Direction dir, double length, double base, double offset) {
        super(level, x, y, z);

        this.color = color;
        this.dir = dir;
        this.length = length;
        this.base = base;
        this.offset = offset;

        this.lifetime = 20;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if(this.age++ >= this.lifetime) this.remove();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {

        Vec3 camPos = camera.getPosition();
        float pX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
        float pY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
        float pZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);

        Direction rot = this.dir.getClockWise();

        double progress = (this.age + partialTicks) / this.lifetime;
        double width = 0.0625D + progress * 0.0625D;
        double girth = 0.125D * (1D - progress);

        // dieselbe Aufhellung wie im Original: erst heller machen, dann Richtung Weiss ziehen
        Color base = new Color(this.color).brighter();
        float brightener = 0.7F;
        float r = (float) (255D - (255D - base.getRed()) * brightener) / 255F;
        float g = (float) (255D - (255D - base.getGreen()) * brightener) / 255F;
        float b = (float) (255D - (255D - base.getBlue()) * brightener) / 255F;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(pX, pY, pZ);
        Matrix4f matrix = poseStack.last().pose();

        double dirXG = this.dir.getStepX() * girth;
        double dirZG = this.dir.getStepZ() * girth;
        double rotXW = rot.getStepX() * width;
        double rotZW = rot.getStepZ() * width;

        double uMin = 0.5D - width;
        double uMax = 0.5D + width;
        double vMin = 0D;
        double vMax = this.length;

        // laesst die Textur wandern, damit der Strahl fliesst
        double add = (int) (System.currentTimeMillis() / 100 % 16) / 16D;

        // unten hinten
        quad(consumer, matrix, r, g, b,
                rotXW, girth, rotZW, uMax, vMax + add + girth,
                -rotXW, girth, -rotZW, uMin, vMax + add + girth,
                -rotXW, -this.length, -rotZW, uMin, vMin + add,
                rotXW, -this.length, rotZW, uMax, vMin + add);

        // unten vorn
        quad(consumer, matrix, r, g, b,
                dirXG + rotXW, 0, dirZG + rotZW, uMax, vMax + add,
                dirXG - rotXW, 0, dirZG - rotZW, uMin, vMax + add,
                dirXG - rotXW, -this.length, dirZG - rotZW, uMin, vMin + add,
                dirXG + rotXW, -this.length, dirZG + rotZW, uMax, vMin + add);

        double wMin = 0D;
        double wMax = girth;

        // unten links
        quad(consumer, matrix, r, g, b,
                rotXW, girth, rotZW, wMin, vMax + add + girth,
                dirXG + rotXW, 0, dirZG + rotZW, wMax, vMax + add,
                dirXG + rotXW, -this.length, dirZG + rotZW, wMax, vMin + add,
                rotXW, -this.length, rotZW, wMin, vMin + add);

        // unten rechts
        quad(consumer, matrix, r, g, b,
                -rotXW, girth, -rotZW, wMin, vMax + add + girth,
                dirXG - rotXW, 0, dirZG - rotZW, wMax, vMax + add,
                dirXG - rotXW, -this.length, dirZG - rotZW, wMax, vMin + add,
                -rotXW, -this.length, -rotZW, wMin, vMin + add);

        double dirOX = this.dir.getStepX() * this.offset;
        double dirOZ = this.dir.getStepZ() * this.offset;

        vMax = this.offset;

        // oben hinten
        quad(consumer, matrix, r, g, b,
                rotXW, 0, rotZW, uMax, vMax - add,
                -rotXW, 0, -rotZW, uMin, vMax - add,
                -rotXW - dirOX, this.base, -rotZW - dirOZ, uMin, vMin - add,
                rotXW - dirOX, this.base, rotZW - dirOZ, uMax, vMin - add);

        // oben vorn
        quad(consumer, matrix, r, g, b,
                rotXW, girth, rotZW, uMax, vMax - add + 0.25,
                -rotXW, girth, -rotZW, uMin, vMax - add + 0.25,
                -rotXW - dirOX, this.base + girth, -rotZW - dirOZ, uMin, vMin - add + 0.25,
                rotXW - dirOX, this.base + girth, rotZW - dirOZ, uMax, vMin - add + 0.25);

        // oben links
        quad(consumer, matrix, r, g, b,
                rotXW, 0, rotZW, wMax, vMax - add + 0.75,
                rotXW, girth, rotZW, wMin, vMax - add + 0.75,
                rotXW - dirOX, this.base + girth, rotZW - dirOZ, wMin, vMin - add + 0.75,
                rotXW - dirOX, this.base, rotZW - dirOZ, wMax, vMin - add + 0.75);

        // oben rechts
        quad(consumer, matrix, r, g, b,
                -rotXW, 0, -rotZW, wMax, vMax - add + 0.75,
                -rotXW, girth, -rotZW, wMin, vMax - add + 0.75,
                -rotXW - dirOX, this.base + girth, -rotZW - dirOZ, wMin, vMin - add + 0.75,
                -rotXW - dirOX, this.base, -rotZW - dirOZ, wMax, vMin - add + 0.75);

        // der Knick zwischen beiden Teilen
        quad(consumer, matrix, r, g, b,
                dirXG + rotXW, 0, dirZG + rotZW, uMax, vMin + add + 0.75,
                dirXG - rotXW, 0, dirZG - rotZW, uMin, vMin + add + 0.75,
                -rotXW, girth, -rotZW, uMin, vMax + add + 0.75,
                rotXW, girth, rotZW, uMax, vMax + add + 0.75);

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, float r, float g, float b,
                             double x1, double y1, double z1, double u1, double v1,
                             double x2, double y2, double z2, double u2, double v2,
                             double x3, double y3, double z3, double u3, double v3,
                             double x4, double y4, double z4, double u4, double v4) {

        vertex(consumer, matrix, r, g, b, x1, y1, z1, u1, v1);
        vertex(consumer, matrix, r, g, b, x2, y2, z2, u2, v2);
        vertex(consumer, matrix, r, g, b, x3, y3, z3, u3, v3);
        vertex(consumer, matrix, r, g, b, x4, y4, z4, u4, v4);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float r, float g, float b,
                               double x, double y, double z, double u, double v) {

        consumer.addVertex(matrix, (float) x, (float) y, (float) z)
                .setUv((float) u, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setColor(r, g, b, 1F)
                .setNormal(0F, 1F, 0F)
                .setLight(240);
    }

    @Override
    public RenderType getRenderType() {
        return NtmRenderTypes.entitySmoth(LAVA);
    }
}
