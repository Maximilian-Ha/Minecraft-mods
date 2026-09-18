package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKNumitronBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKNumitronBlockEntity.DisplayUnit;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKNumitron.
 *
 * Zwei Ziffernanzeigen uebereinander. Die Ziffern selbst sind keine Schrift, sondern sieben
 * einzelne Vierecke, deren Bildausschnitt auf das jeweilige Zeichen zeigt -- eine Zeichentafel
 * mit zehn Ziffern in der oberen und den Vorsatzzeichen in der unteren Haelfte.
 */
/* Kein IBEWLRProvider: die Tafel traegt wie im Original ein flaches Sinnbild
 * (rbmk/rbmk_display) als Gegenstandsmodell, keinen eigenen Darsteller. */
public class RenderRBMKNumitron extends BlockEntityRendererNT<RBMKNumitronBlockEntity> {

    public static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/network/numitron.png");
    public static final ResourceLocation LIGHTS_TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/network/numitron_lights.png");

    /** Halbe Breite und halbe Hoehe eines Zeichens, in Bloecken. Zahlen aus dem Original. */
    private static final float CHAR_WIDTH = 8F / 200F;
    private static final float CHAR_HEIGHT = 13F / 200F;
    /** Hoehe der Ziffernreihe ueber dem Fuss der Anzeige. */
    private static final float ROW_HEIGHT = 0.5625F;
    /** Wie weit die Ziffern vor der Tafel schweben, damit sie nicht in ihr verschwinden. */
    private static final float ROW_DEPTH = 0.03135F;

    @Override
    public BlockEntityRenderer<RBMKNumitronBlockEntity> create(Context context) {
        return new RenderRBMKNumitron();
    }

    @Override
    public void render(RBMKNumitronBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        for(int i = 0; i < RBMKNumitronBlockEntity.DISPLAYS; i++) {

            DisplayUnit unit = be.displays[i];
            if(!unit.active) continue;

            RenderContext.pushPose();
            RenderContext.translate(0.25F, i * -0.5F + 0.25F, 0F);

            bindTexture(TEXTURE);
            ResourceManager.rbmk_numitron.renderAll();

            this.drawDigits(unit, buffer);
            this.drawLabel(unit, buffer);

            RenderContext.popPose();
        }
    }

    /** Die sieben Stellen einer Anzeige, von links nach rechts. */
    private void drawDigits(DisplayUnit unit, MultiBufferSource buffer) {

        String value = pad(text(unit), unit.leadingZeroes);

        FullBright.enable();

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.entitySmoth(LIGHTS_TEXTURE));
        Matrix4f matrix = RenderContext.poseStack().last().pose();
        int light = RenderContext.light();
        int overlay = RenderContext.overlay();

        for(int j = 0; j < RBMKNumitronBlockEntity.DIGITS; j++) {

            /* Abgeschaltete Stellen bleiben dunkel. Das oberste der sieben Bits ist die linke Stelle. */
            if((unit.activeDigits & (0x40L >> j)) == 0) continue;

            char c = value.charAt(j);
            if(c == ' ') continue;

            float u = charU(c);
            float v = charV(c);
            float z = (j - 3) * 0.1F;

            consumer.addVertex(matrix, ROW_DEPTH, -CHAR_HEIGHT + ROW_HEIGHT, CHAR_WIDTH - z)
                    .setColor(1F, 1F, 1F, 1F).setUv(u, v + 0.5F).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
            consumer.addVertex(matrix, ROW_DEPTH, CHAR_HEIGHT + ROW_HEIGHT, CHAR_WIDTH - z)
                    .setColor(1F, 1F, 1F, 1F).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
            consumer.addVertex(matrix, ROW_DEPTH, CHAR_HEIGHT + ROW_HEIGHT, -CHAR_WIDTH - z)
                    .setColor(1F, 1F, 1F, 1F).setUv(u + 0.1F, v).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
            consumer.addVertex(matrix, ROW_DEPTH, -CHAR_HEIGHT + ROW_HEIGHT, -CHAR_WIDTH - z)
                    .setColor(1F, 1F, 1F, 1F).setUv(u + 0.1F, v + 0.5F).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
        }

        FullBright.disable();
    }

    /** Der anzuzeigende Wert als Text, noch ohne Auffuellen. */
    private static String text(DisplayUnit unit) {

        if(unit.shortenNumber) return BobMathUtil.getShortNumber(unit.value);

        /* Sieben Stellen sind das Ende der Fahnenstange; ein Minus kostet eine davon. */
        if(unit.value > 9999999L) return "9999999";
        if(unit.value < -999999L) return "-999999";
        return Long.toString(unit.value);
    }

    /**
     * Fuellt auf sieben Stellen auf -- mit Nullen oder Leerzeichen. Bei einer negativen Zahl
     * stehen die Nullen zwischen Minus und Ziffern, sonst saehe es aus wie "000-42".
     */
    private static String pad(String value, boolean leadingZeroes) {

        if(value.length() >= RBMKNumitronBlockEntity.DIGITS) return value;

        StringBuilder sb = new StringBuilder();

        if(leadingZeroes && value.charAt(0) == '-') {
            String digits = value.substring(1);
            sb.append('-');
            for(int i = digits.length(); i < RBMKNumitronBlockEntity.DIGITS - 1; i++) sb.append('0');
            sb.append(digits);
        } else {
            char fill = leadingZeroes ? '0' : ' ';
            for(int i = value.length(); i < RBMKNumitronBlockEntity.DIGITS; i++) sb.append(fill);
            sb.append(value);
        }

        return sb.toString();
    }

    /** Die linke Kante des Zeichens auf der Zeichentafel. Unbekanntes wird zum Minus. */
    private static float charU(char c) {
        if(c >= '0' && c <= '9') return 0.1F * (c - '0');
        return switch(c) {
            case '.' -> 0.9F;
            case 'k' -> 0.0F;
            case 'M' -> 0.1F;
            case 'G' -> 0.2F;
            case 'T' -> 0.3F;
            case 'P' -> 0.4F;
            case 'E' -> 0.5F;
            default -> 0.8F;
        };
    }

    /** Ziffern stehen in der oberen Haelfte der Zeichentafel, alles andere in der unteren. */
    private static float charV(char c) {
        return (c >= '0' && c <= '9') ? 0.0F : 0.5F;
    }

    /** Die Beschriftung unter der Anzeige. */
    private void drawLabel(DisplayUnit unit, MultiBufferSource buffer) {

        if(unit.label == null || unit.label.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int width = font.width(unit.label);

        RenderContext.translate(0.01F, 0.3125F, 0F);

        /* Lange Beschriftungen werden kleiner, damit sie nicht ueber die Tafel hinauslaufen. */
        float scale = Math.min(0.0125F, 0.75F / Math.max(width, 1));
        RenderContext.scale(scale, -scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        FullBright.enable();
        font.drawInBatch(unit.label, -width / 2F, -font.lineHeight / 2F, 0x00ff00, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        FullBright.disable();
    }
}
