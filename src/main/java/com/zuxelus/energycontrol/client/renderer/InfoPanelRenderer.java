package com.zuxelus.energycontrol.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.blocks.InfoPanelBlock;
import com.zuxelus.energycontrol.blocks.PanelThickness;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.renderers.TileEntityInfoPanelRenderer.
 *
 * Zeichnet die Zeilen der Karte auf die Schauseite der Tafel -- und, wenn Erweiterungen
 * angebaut sind, ueber die ganze Flaeche. Das Original zeichnete zusaetzlich den farbigen
 * Hintergrund; hier ist der Hintergrund die Blocktextur selbst, der Renderer kuemmert sich
 * nur um die Schrift.
 *
 * Die Schrift wird auf die Flaeche **eingepasst**, wie im Original: die breiteste Zeile und
 * die Zahl der Zeilen ergeben einen Massstab, und der gilt fuer alle Zeilen. Ohne das steht
 * die Schrift in fester Groesse da und laeuft bei langen Zahlen ueber den Rand hinaus --
 * genau das war der Fehler der ersten Fassung dieses Ports.
 */
public class InfoPanelRenderer implements BlockEntityRenderer<InfoPanelBlockEntity> {

    /** Rand ringsum, in Blockeinheiten -- ein Sechzehntel je Seite, wie im Original. */
    private static final float MARGIN = 2F / 16F;

    /** Zeilenabstand in Schriftpunkten: Zeilenhoehe plus zwei, wie im Original. */
    private static final int LINE_SPACING = 2;

    /** Etwas Luft vor der ersten und hinter der letzten Spalte. */
    private static final int TEXT_PADDING = 4;

    private final Font font;

    public InfoPanelRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    /**
     * Ein grosser Schirm reicht ueber den Block hinaus, an dem die Tafel haengt. Bliebe es
     * bei der ueblichen Sichtpruefung, verschwaende seine Schrift, sobald dieser eine Block
     * aus dem Bild laeuft. Einzelne Tafeln behalten die uebliche Pruefung.
     */
    @Override
    public boolean shouldRenderOffScreen(InfoPanelBlockEntity be) {
        return be.hasLargeScreen();
    }

    @Override
    public void render(InfoPanelBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        // Ohne Strom bleibt der Schirm leer, wie im Original.
        if(!be.isPowered()) return;

        List<PanelString> lines = be.getPanelStringList(be.getShowLabels());
        if(lines == null || lines.isEmpty()) return;

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(InfoPanelBlock.FACING);
        // Die fortgeschrittene Tafel ist duenner als ein voller Block; ihre Schauseite
        // liegt entsprechend weiter hinten, und die Schrift gehoert genau davor.
        float depth = state.hasProperty(PanelThickness.THICKNESS)
                ? -0.5F + state.getValue(PanelThickness.THICKNESS) / 16F
                : 0.5F;

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
        float displayWidth = Math.abs(x2 - x1) + 1 - MARGIN;
        float displayHeight = Math.abs(y2 - y1) + 1 - MARGIN;

        // Massstab: die breiteste Zeile und alle Zeilen zusammen muessen hineinpassen.
        int maxWidth = TEXT_PADDING;
        for(PanelString line : lines) maxWidth = Math.max(maxWidth, font.width(joined(line)) + TEXT_PADDING);

        int lineHeight = font.lineHeight + LINE_SPACING;
        int requiredHeight = lineHeight * lines.size();

        float scaleX = displayWidth / maxWidth;
        float scaleY = displayHeight / requiredHeight;
        float scale = Math.min(scaleX, scaleY);

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        alignToFace(pose, facing);
        // Erst in die Mitte der Flaeche, dann nach vorn -- plus ein Hauch, damit die
        // Schrift nicht in der Blockflaeche steckt und flimmert.
        pose.translate(centerX, centerY, depth + 0.005F);
        // Negatives Y, weil die Schriftart nach unten laeuft, die Welt aber nach oben.
        pose.scale(scale, -scale, scale);

        // Die Flaeche in Schriftpunkten, nachdem der Massstab feststeht.
        float realWidth = displayWidth / scale;
        float realHeight = displayHeight / scale;

        // Begrenzt die Breite, bleibt die Schrift links stehen und sitzt senkrecht mittig;
        // begrenzt die Hoehe, ist es umgekehrt. So haelt es auch das Original.
        float offsetX = scaleX < scaleY ? TEXT_PADDING / 2F : (realWidth - maxWidth) / 2F + TEXT_PADDING / 2F;
        float offsetY = scaleX < scaleY ? (realHeight - requiredHeight) / 2F : 0F;

        int color = be.getColorText();

        for(int row = 0; row < lines.size(); row++) {
            PanelString line = lines.get(row);
            float y = offsetY - realHeight / 2F + row * lineHeight;

            draw(pose, buffers, light, line.textLeft, colorOr(line.colorLeft, color),
                    offsetX - realWidth / 2F, y);
            if(line.textCenter != null) {
                draw(pose, buffers, light, line.textCenter, colorOr(line.colorCenter, color),
                        -font.width(line.textCenter) / 2F, y);
            }
            if(line.textRight != null) {
                draw(pose, buffers, light, line.textRight, colorOr(line.colorRight, color),
                        realWidth / 2F - font.width(line.textRight), y);
            }
        }

        pose.popPose();
    }

    /** Die ganze Zeile am Stueck -- nur zum Ausmessen, gezeichnet wird sie in drei Teilen. */
    private Component joined(PanelString line) {
        MutableComponent result = Component.empty();
        boolean first = true;
        for(Component part : new Component[] { line.textLeft, line.textCenter, line.textRight }) {
            if(part == null || part.getString().isEmpty()) continue;
            if(!first) result.append(" ");
            result.append(part);
            first = false;
        }
        return result;
    }

    private void draw(PoseStack pose, MultiBufferSource buffers, int light, Component text, int color, float x, float y) {
        if(text == null) return;
        font.drawInBatch(text, x, y, 0xFF000000 | color, false, pose.last().pose(), buffers,
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
