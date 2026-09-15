package com.hbm.render.blockentity;

import com.hbm.blockentity.network.PylonBaseBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.hbm.util.ColorUtil;
import com.hbm.util.Vec3NT;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/** Zeichnet die Leitungen zwischen zwei Masten. Im Original RenderPylonBase. */
public abstract class RenderPylonBase<T extends PylonBaseBlockEntity> extends BlockEntityRendererNT<T> {

    /** todo config: im Original ClientConfig.RENDER_CABLE_HANG */
    protected static final boolean RENDER_CABLE_HANG = true;

    public static final int LINE_COLOR = 0xBB3311;

    /**
     * The closest we have to a does-all solution. It will figure out if it needs to draw multiple lines,
     * iterate through all the mounting points, try to find the matching mounting points and then draw the lines.
     */
    public void renderLinesGeneric(PylonBaseBlockEntity pyl, MultiBufferSource buffer) {

        Level level = pyl.getLevel();
        if(level == null) return;

        ResourceLocation tex = pyl.color == 0 ? ResourceManager.WIRE_TEX : ResourceManager.WIRE_GREYSCALE_TEX;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        BlockPos ownPos = pyl.getBlockPos();

        for(BlockPos wire : pyl.getConnected()) {

            BlockEntity be = level.getBlockEntity(wire);

            if(be instanceof PylonBaseBlockEntity pylon) {

                Vec3NT[] m1 = pyl.getMountPos();
                Vec3NT[] m2 = pylon.getMountPos();

                int lineCount = Math.min(m1.length, m2.length);

                for(int line = 0; line < lineCount; line++) {

                    Vec3NT first = m1[line % m1.length];
                    int secondIndex = line % m2.length;

                    /*
                     * hacky hacky hack
                     * this will shift the mount point order by 2 to prevent wires from crossing
                     * when meta 12 and 15 pylons are connected. this isn't a great solution
                     * and there's still ways to cross the wires in an ugly way but for now
                     * it should be enough.
                     */
                    if(lineCount == 4 && (
                            (getFacing(pyl) == Direction.EAST && getFacing(pylon) == Direction.NORTH) ||
                            (getFacing(pyl) == Direction.NORTH && getFacing(pylon) == Direction.EAST))) {

                        secondIndex += 2;
                        secondIndex %= m2.length;
                    }

                    Vec3NT second = m2[secondIndex];
                    BlockPos otherPos = pylon.getBlockPos();

                    double sX = second.xCoord + otherPos.getX() - ownPos.getX();
                    double sY = second.yCoord + otherPos.getY() - ownPos.getY();
                    double sZ = second.zCoord + otherPos.getZ() - ownPos.getZ();

                    renderLine(level, pyl, consumer, matrix,
                            first.xCoord,
                            first.yCoord,
                            first.zCoord,
                            first.xCoord + (sX - first.xCoord) * 0.5,
                            first.yCoord + (sY - first.yCoord) * 0.5,
                            first.zCoord + (sZ - first.zCoord) * 0.5);
                }
            }
        }
    }

    /** Blickrichtung des Kerns; im Original die Metadaten minus 10 */
    protected static Direction getFacing(PylonBaseBlockEntity pylon) {
        BlockState state = pylon.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    /**
     * Renders half a line.
     * First coords: the pylon's mounting point.
     * Second coords: the midway point exactly between the mounting points. The "hang" doesn't need
     * to be accounted for, it's calculated in here.
     */
    public void renderLine(Level level, PylonBaseBlockEntity pyl, VertexConsumer consumer, Matrix4f matrix, double x0, double y0, double z0, double x1, double y1, double z1) {

        float count = 10;
        int color = pyl.color == 0 ? 0xffffff : pyl.color;
        int overlay = RenderContext.overlay();

        Vec3NT delta = new Vec3NT(x0 - x1, y0 - y1, z0 - z1);

        double girth = 0.03125D;
        double hyp = Math.sqrt(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
        double yaw = Math.atan2(delta.xCoord, delta.zCoord);
        double pitch = Math.atan2(delta.yCoord, hyp);
        double rotator = Math.PI * 0.5D;
        double newPitch = pitch + rotator;
        double newYaw = yaw + rotator;
        double iZ = Math.cos(yaw) * Math.cos(newPitch) * girth;
        double iX = Math.sin(yaw) * Math.cos(newPitch) * girth;
        double iY = Math.sin(newPitch) * girth;
        double jZ = Math.cos(newYaw) * girth;
        double jX = Math.sin(newYaw) * girth;

        BlockPos pos = pyl.getBlockPos();

        if(!RENDER_CABLE_HANG) {
            drawLineSegment(consumer, matrix, color, RenderContext.light(), overlay, x0, y0, z0, x1, y1, z1, iX, iY, iZ, jX, jZ);
        } else {

            double hang = Math.min(delta.length() / 15D, 2.5D);

            for(float j = 0; j < count; j++) {

                float k = j + 1;

                double sagJ = Math.sin(j / count * Math.PI * 0.5) * hang;
                double sagK = Math.sin(k / count * Math.PI * 0.5) * hang;
                double sagMean = (sagJ + sagK) / 2D;

                double deltaX = x1 - x0;
                double deltaY = y1 - y0;
                double deltaZ = z1 - z0;

                double ja = j + 0.5D;
                double ix = pos.getX() + x0 + deltaX / (double) (count) * ja;
                double iy = pos.getY() + y0 + deltaY / (double) (count) * ja - sagMean;
                double iz = pos.getZ() + z0 + deltaZ / (double) (count) * ja;

                int brightness = LevelRenderer.getLightColor(level, BlockPos.containing(ix, iy, iz));

                drawLineSegment(consumer, matrix, color, brightness, overlay,
                        x0 + (deltaX * j / count),
                        y0 + (deltaY * j / count) - sagJ,
                        z0 + (deltaZ * j / count),
                        x0 + (deltaX * k / count),
                        y0 + (deltaY * k / count) - sagK,
                        z0 + (deltaZ * k / count),
                        iX, iY, iZ, jX, jZ);
            }
        }
    }

    /**
     * Draws a single segment from the first to the second 3D coordinate.
     * Not fantastic but it looks good enough.
     */
    public void drawLineSegment(VertexConsumer consumer, Matrix4f matrix, int color, int light, int overlay, double x, double y, double z, double a, double b, double c, double iX, double iY, double iZ, double jX, double jZ) {

        double deltaX = a - x;
        double deltaY = b - y;
        double deltaZ = c - z;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        int wrap = (int) Math.ceil(length * 8);

        if(deltaX + deltaZ < 0) {
            wrap *= -1;
            jZ *= -1;
            jX *= -1;
        }

        float r = ColorUtil.fr(color);
        float g = ColorUtil.fg(color);
        float bl = ColorUtil.fb(color);

        vertex(consumer, matrix, r, g, bl, light, overlay, x + iX, y + iY, z + iZ, 0, 0);
        vertex(consumer, matrix, r, g, bl, light, overlay, x - iX, y - iY, z - iZ, 0, 1);
        vertex(consumer, matrix, r, g, bl, light, overlay, a - iX, b - iY, c - iZ, wrap, 1);
        vertex(consumer, matrix, r, g, bl, light, overlay, a + iX, b + iY, c + iZ, wrap, 0);
        vertex(consumer, matrix, r, g, bl, light, overlay, x + jX, y, z + jZ, 0, 0);
        vertex(consumer, matrix, r, g, bl, light, overlay, x - jX, y, z - jZ, 0, 1);
        vertex(consumer, matrix, r, g, bl, light, overlay, a - jX, b, c - jZ, wrap, 1);
        vertex(consumer, matrix, r, g, bl, light, overlay, a + jX, b, c + jZ, wrap, 0);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float r, float g, float b, int light, int overlay, double x, double y, double z, float u, float v) {
        consumer.addVertex(matrix, (float) x, (float) y, (float) z)
                .setColor(r, g, b, 1F)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0F, 1F, 0F);
    }

    @Override
    public AABB getRenderBoundingBox(T be) {
        return be.getRenderBoundingBox();
    }
}
