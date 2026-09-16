package com.zuxelus.energycontrol.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.blocks.InfoPanelBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.renderers.TileEntityInfoPanelRenderer.
 *
 * Zeichnet die Zeilen der Karte auf die Schauseite der Tafel -- und, wenn Erweiterungen
 * angebaut sind, ueber die ganze Flaeche. Das Original zeichnete zusaetzlich den farbigen
 * Hintergrund; hier ist der Hintergrund die Blocktextur selbst, der Renderer kuemmert sich nur
 * um die Schrift.
 */
public class InfoPanelRenderer implements BlockEntityRenderer<InfoPanelBlockEntity> {

    /** Ein Block der Schauseite ist ein Feld von 64 mal 64 Schriftpunkten. */
    private static final float SCALE = 1F / 64F;
    private static final int BLOCK_UNITS = 64;
    private static final int LINE_HEIGHT = 9;
    /** Abstand zum Rand, damit die Schrift nicht an der Fuge klebt. */
    private static final int MARGIN = 3;

    private final Font font;

    public InfoPanelRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(InfoPanelBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        // Ohne Strom bleibt der Schirm leer, wie im Original.
        if(!be.isPowered()) return;

        List<PanelString> lines = be.getPanelStringList(be.getShowLabels());
        if(lines == null || lines.isEmpty()) return;

        Direction facing = be.getBlockState().getValue(InfoPanelBlock.FACING);

        // Die Flaeche in Blockeinheiten des oertlichen Achsenkreuzes: wo ihre Mitte
        // gegenueber dieser Tafel liegt und wie gross sie ist.
        BlockPos self = be.getBlockPos();
        Vec3i right = right(facing);
        Vec3i up = up(facing);

        int x1 = dot(be.getScreenMin(), self, right);
        int x2 = dot(be.getScreenMax(), self, right);
        int y1 = dot(be.getScreenMin(), self, up);
        int y2 = dot(be.getScreenMax(), self, up);

        float centerX = (Math.min(x1, x2) + Math.max(x1, x2)) / 2F;
        float centerY = (Math.min(y1, y2) + Math.max(y1, y2)) / 2F;
        int width = Math.abs(x2 - x1) + 1;
        int height = Math.abs(y2 - y1) + 1;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        alignToFace(pose, facing);
        // Erst in die Mitte der Flaeche, dann einen halben Block nach vorn -- plus ein Hauch,
        // damit die Schrift nicht in der Blockflaeche steckt und flimmert.
        pose.translate(centerX, centerY, 0.5F + 0.005F);
        // Negatives Y, weil die Schriftart nach unten laeuft, die Welt aber nach oben.
        pose.scale(SCALE, -SCALE, SCALE);

        int drawWidth = BLOCK_UNITS * width;
        int drawHeight = BLOCK_UNITS * height;
        int maxLines = Math.max(1, (drawHeight - 2 * MARGIN) / LINE_HEIGHT);

        int count = Math.min(lines.size(), maxLines);
        float top = -(count * LINE_HEIGHT) / 2F;
        int color = be.getColorText();
        float edge = drawWidth / 2F - MARGIN;

        for(int i = 0; i < count; i++) {
            PanelString line = lines.get(i);
            float y = top + i * LINE_HEIGHT;
            draw(pose, buffers, light, line.textLeft, colorOr(line.colorLeft, color), -edge, y, Align.LEFT);
            draw(pose, buffers, light, line.textCenter, colorOr(line.colorCenter, color), 0F, y, Align.CENTER);
            draw(pose, buffers, light, line.textRight, colorOr(line.colorRight, color), edge, y, Align.RIGHT);
        }

        pose.popPose();
    }

    private enum Align { LEFT, CENTER, RIGHT }

    private void draw(PoseStack pose, MultiBufferSource buffers, int light, Component text, int color, float x, float y, Align align) {
        if(text == null) return;

        float width = font.width(text);
        float drawX = switch(align) {
            case LEFT -> x;
            case CENTER -> x - width / 2F;
            case RIGHT -> x - width;
        };

        font.drawInBatch(text, drawX, y, 0xFF000000 | color, false, pose.last().pose(), buffers,
                Font.DisplayMode.POLYGON_OFFSET, 0, light);
    }

    /** Eine eigene Zeilenfarbe schlaegt die Grundfarbe der Tafel. */
    private static int colorOr(int lineColor, int panelColor) {
        return lineColor != 0 ? lineColor : panelColor;
    }

    private static int dot(BlockPos pos, BlockPos origin, Vec3i axis) {
        return (pos.getX() - origin.getX()) * axis.getX()
                + (pos.getY() - origin.getY()) * axis.getY()
                + (pos.getZ() - origin.getZ()) * axis.getZ();
    }

    /**
     * Die Weltrichtung, die auf dem Schirm nach rechts zeigt -- also die oertliche X-Achse,
     * nachdem {@link #alignToFace} gedreht hat.
     */
    private static Vec3i right(Direction facing) {
        return switch(facing) {
            case NORTH -> new Vec3i(-1, 0, 0);
            case EAST -> new Vec3i(0, 0, -1);
            case WEST -> new Vec3i(0, 0, 1);
            default -> new Vec3i(1, 0, 0);
        };
    }

    /** Dasselbe fuer oben: die oertliche Y-Achse. */
    private static Vec3i up(Direction facing) {
        return switch(facing) {
            case UP -> new Vec3i(0, 0, -1);
            case DOWN -> new Vec3i(0, 0, 1);
            default -> new Vec3i(0, 1, 0);
        };
    }

    /**
     * Dreht das oertliche Achsenkreuz so, dass seine Z-Achse zur Schauseite zeigt. Die
     * Schrift wird im XY-Feld gezeichnet und ist von +Z aus lesbar.
     */
    private static void alignToFace(PoseStack pose, Direction facing) {
        switch(facing) {
            case SOUTH -> { }
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(-90F));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(-90F));
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(90F));
        }
    }
}
