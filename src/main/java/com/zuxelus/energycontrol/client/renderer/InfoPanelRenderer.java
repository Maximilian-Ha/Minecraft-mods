package com.zuxelus.energycontrol.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.blocks.InfoPanelBlock;
import com.zuxelus.energycontrol.blocks.PanelThickness;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.renderers.TileEntityInfoPanelRenderer.
 *
 * Zeichnet den Schirm der Tafel -- und, wenn Erweiterungen angebaut sind, ueber die ganze
 * Flaeche, so dass aus mehreren Bloecken ein Bildschirm wird.
 *
 * Zwei Dinge macht dieser Renderer, beides wie das Original:
 *
 * 1. Er legt ueber jede Blockflaeche des Schirms ein Viereck aus {@code panel_screen.png}.
 *    Das ist eine Kachelkarte aus vier mal vier Feldern: jedes Feld hat an null bis vier
 *    Kanten einen Rahmen, und jeder Block bekommt das Feld, dessen Rahmen zu seiner Lage
 *    im Schirm passt. Innen liegen die Felder rahmenlos aneinander -- erst dadurch wird
 *    aus der Tafel samt Erweiterungen eine durchgehende Flaeche statt einer Mauer aus
 *    eingerahmten Kaesten. Das Viereck bekommt die Hintergrundfarbe der Tafel und, solange
 *    Strom da ist, volles Licht; genau so schaltete das Original hier die Beleuchtung ab.
 * 2. Er schreibt die Zeilen der Karte darauf, **eingepasst** in die Flaeche: die breiteste
 *    Zeile und die Zahl der Zeilen ergeben einen Massstab, und der gilt fuer alle Zeilen.
 */
public class InfoPanelRenderer implements BlockEntityRenderer<InfoPanelBlockEntity> {

    /** Die Kachelkarte des Schirms: vier mal vier Felder zu je einem Viertel. */
    private static final ResourceLocation SCREEN = EnergyControl.loc("textures/block/info_panel_panel_screen.png");

    /** Welche Kante eines Feldes einen Rahmen traegt -- die Nummern des Originals. */
    private static final int BORDER_RIGHT = 1;
    private static final int BORDER_LEFT = 2;
    private static final int BORDER_TOP = 4;
    private static final int BORDER_BOTTOM = 8;

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

    /**
     * Eine Tafel soll man auch von weitem ablesen koennen -- das Original erlaubte
     * 256 Bloecke, die ueblichen 64 sind fuer eine Anzeigetafel zu wenig.
     */
    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(InfoPanelBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(InfoPanelBlock.FACING);
        // Die fortgeschrittene Tafel ist duenner als ein voller Block; ihre Schauseite
        // liegt entsprechend weiter hinten, und der Schirm gehoert genau davor.
        float depth = state.hasProperty(PanelThickness.THICKNESS)
                ? -0.5F + state.getValue(PanelThickness.THICKNESS) / 16F
                : 0.5F;

        // Die Flaeche in Blockeinheiten des oertlichen Achsenkreuzes: wo ihre Raender
        // gegenueber dieser Tafel liegen. Nach alignToFace zeigt die oertliche X-Achse
        // nach rechts und die Y-Achse nach oben, von vorn gesehen.
        BlockPos self = be.getBlockPos();
        Vec3i right = right(facing);
        Vec3i up = up(facing);

        int x1 = dot(be.getScreenMin(), self, right);
        int x2 = dot(be.getScreenMax(), self, right);
        int y1 = dot(be.getScreenMin(), self, up);
        int y2 = dot(be.getScreenMax(), self, up);

        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);

        // Unter Strom leuchtet der Schirm aus sich selbst; ohne Strom ist er eine
        // Blockflaeche wie jede andere. So hielt es auch das Original.
        boolean powered = be.isPowered();
        int screenLight = powered ? LightTexture.FULL_BRIGHT : light;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        alignToFace(pose, facing);

        drawScreen(pose, buffers, screenLight, be.getColorBackground(), xMin, xMax, yMin, yMax, depth);

        if(powered) drawLines(be, pose, buffers, screenLight, xMin, xMax, yMin, yMax, depth);

        pose.popPose();
    }

    /**
     * Legt ueber jede Blockflaeche des Schirms ein Viereck aus der Kachelkarte. Der
     * Nullpunkt des Achsenkreuzes sitzt in der Mitte der Tafel selbst, darum laeuft die
     * Schleife von den Raendern des Schirms aus, die gegenueber der Tafel gezaehlt sind.
     */
    private void drawScreen(PoseStack pose, MultiBufferSource buffers, int light, int color,
                            int xMin, int xMax, int yMin, int yMax, float depth) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.text(SCREEN));
        Matrix4f matrix = pose.last().pose();

        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        // Ein Hauch vor der Blockflaeche, damit nichts flimmert.
        float z = depth + 0.002F;

        for(int x = xMin; x <= xMax; x++) {
            for(int y = yMin; y <= yMax; y++) {
                int tile = (x == xMin ? BORDER_LEFT : 0) | (x == xMax ? BORDER_RIGHT : 0)
                        | (y == yMax ? BORDER_TOP : 0) | (y == yMin ? BORDER_BOTTOM : 0);
                // Waagerechte Rahmen waehlen die Spalte, senkrechte die Zeile der Karte.
                float u0 = (tile / 4) * 0.25F;
                float v0 = (tile % 4) * 0.25F;

                float left = x - 0.5F;
                float rightEdge = x + 0.5F;
                float bottom = y - 0.5F;
                float top = y + 0.5F;

                // Gegen den Uhrzeigersinn, von vorn gesehen; oben auf dem Schirm ist das
                // kleine V der Kachel, denn Bildzeilen zaehlen von oben nach unten.
                consumer.addVertex(matrix, left, bottom, z).setColor(r, g, b, 1F).setUv(u0, v0 + 0.25F).setLight(light);
                consumer.addVertex(matrix, rightEdge, bottom, z).setColor(r, g, b, 1F).setUv(u0 + 0.25F, v0 + 0.25F).setLight(light);
                consumer.addVertex(matrix, rightEdge, top, z).setColor(r, g, b, 1F).setUv(u0 + 0.25F, v0).setLight(light);
                consumer.addVertex(matrix, left, top, z).setColor(r, g, b, 1F).setUv(u0, v0).setLight(light);
            }
        }
    }

    /** Die Zeilen der Karte, auf die Flaeche eingepasst. */
    private void drawLines(InfoPanelBlockEntity be, PoseStack pose, MultiBufferSource buffers, int light,
                           int xMin, int xMax, int yMin, int yMax, float depth) {
        List<PanelString> lines = be.getPanelStringList(be.getShowLabels());
        if(lines == null || lines.isEmpty()) return;

        float centerX = (xMin + xMax) / 2F;
        float centerY = (yMin + yMax) / 2F;
        float displayWidth = xMax - xMin + 1 - MARGIN;
        float displayHeight = yMax - yMin + 1 - MARGIN;

        // Massstab: die breiteste Zeile und alle Zeilen zusammen muessen hineinpassen.
        int maxWidth = TEXT_PADDING;
        for(PanelString line : lines) maxWidth = Math.max(maxWidth, font.width(joined(line)) + TEXT_PADDING);

        int lineHeight = font.lineHeight + LINE_SPACING;
        int requiredHeight = lineHeight * lines.size();

        float scaleX = displayWidth / maxWidth;
        float scaleY = displayHeight / requiredHeight;
        float scale = Math.min(scaleX, scaleY);

        pose.pushPose();
        // Erst in die Mitte der Flaeche, dann nach vorn -- vor den Schirm, nicht hinein.
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
