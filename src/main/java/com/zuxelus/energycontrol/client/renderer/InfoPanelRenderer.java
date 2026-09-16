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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.renderers.TileEntityInfoPanelRenderer.
 *
 * Zeichnet die Zeilen der Karte auf die Schauseite der Tafel. Das Original zeichnete
 * zusaetzlich den farbigen Hintergrund; hier ist der Hintergrund die Blocktextur selbst,
 * der Renderer kuemmert sich nur um die Schrift.
 */
public class InfoPanelRenderer implements BlockEntityRenderer<InfoPanelBlockEntity> {

    /** Die Schauseite wird als Feld von 64 mal 64 Schriftpunkten behandelt. */
    private static final float SCALE = 1F / 64F;
    private static final int LINE_HEIGHT = 9;
    private static final int MAX_LINES = 6;

    private final Font font;

    public InfoPanelRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(InfoPanelBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        List<PanelString> lines = be.getPanelStringList(be.getShowLabels());
        if(lines == null || lines.isEmpty()) return;

        Direction facing = be.getBlockState().getValue(InfoPanelBlock.FACING);

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        alignToFace(pose, facing);
        // Ein halber Block nach vorn, plus ein Hauch, damit die Schrift nicht in der
        // Blockflaeche steckt und flimmert.
        pose.translate(0F, 0F, 0.5F + 0.005F);
        // Negatives Y, weil die Schriftart nach unten laeuft, die Welt aber nach oben.
        pose.scale(SCALE, -SCALE, SCALE);

        int count = Math.min(lines.size(), MAX_LINES);
        float top = -(count * LINE_HEIGHT) / 2F;
        int color = be.getColorText();

        for(int i = 0; i < count; i++) {
            PanelString line = lines.get(i);
            float y = top + i * LINE_HEIGHT;
            drawPart(pose, buffers, light, line.textLeft, colorOr(line.colorLeft, color), -30F, y, Align.LEFT);
            drawPart(pose, buffers, light, line.textCenter, colorOr(line.colorCenter, color), 0F, y, Align.CENTER);
            drawPart(pose, buffers, light, line.textRight, colorOr(line.colorRight, color), 30F, y, Align.RIGHT);
        }

        pose.popPose();
    }

    private enum Align { LEFT, CENTER, RIGHT }

    private void drawPart(PoseStack pose, MultiBufferSource buffers, int light, Component text, int color, float x, float y, Align align) {
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
