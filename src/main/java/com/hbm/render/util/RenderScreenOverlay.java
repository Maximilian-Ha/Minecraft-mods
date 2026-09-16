package com.hbm.render.util;

import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.Clock;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class RenderScreenOverlay {

    public static final ResourceLocation MISC_TEXTURE = NuclearTechMod.withDefaultNamespace("textures/misc/overlay_misc.png");

    private static long lastSurvey;
    private static float prevResult;
    private static float lastResult;

    /**
     * Portiert aus 1.7.10: RenderScreenOverlay.renderScope.
     *
     * Das Bild der Zieloptik ueber dem ganzen Schirm. Der Zielkreis sitzt in der Mitte des Bildes
     * und nimmt neun Sechzehntel seiner Kantenlaenge ein -- er soll genau die KUERZERE Seite des
     * Schirms ausfuellen, damit er auf jedem Fenster gleich gross erscheint und rund bleibt.
     *
     * Daraus folgt die Kantenlaenge, mit der das ganze Bild gezeichnet wird: die kuerzere Seite
     * mal sechzehn Neuntel. Was daneben uebrigbleibt, wird schwarz zugemalt -- das Bild selbst
     * kann es nicht abdecken, weil es in der Mitte durchsichtig ist.
     */
    public static void renderScope(GuiGraphics guiGraphics, ResourceLocation texture) {

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int span = (int) Math.ceil(Math.min(width, height) * 16D / 9D);
        int left = (width - span) / 2;
        int top = (height - span) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(texture, left, top, span, span, 0F, 0F, 256, 256, 256, 256);

        RenderSystem.disableBlend();

        /* Der Rand ausserhalb des Bildes: nur dort, wo ueberhaupt etwas uebrigbleibt. */
        if(left > 0) {
            guiGraphics.fill(0, 0, left, height, 0xFF000000);
            guiGraphics.fill(left + span, 0, width, height, 0xFF000000);
        }
        if(top > 0) {
            guiGraphics.fill(0, 0, width, top, 0xFF000000);
            guiGraphics.fill(0, top + span, width, height, 0xFF000000);
        }
    }

    public static void renderRadCounter(GuiGraphics guiGraphics, float in) {
        Minecraft mc = Minecraft.getInstance();

        if (!mc.options.getCameraType().isFirstPerson()) return;
        if (mc.options.hideGui) return;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) return;

        float radiation;

        radiation = lastResult - prevResult;

        if (Clock.get_ms() >= lastSurvey + 1000) {
            lastSurvey = Clock.get_ms();
            prevResult = lastResult;
            lastResult = in;
        }

        int length = 74;
        int maxRad = 1000;

        int bar = getScaled(in, maxRad, 74);

        int posX = 16;
        int posY = guiGraphics.guiHeight() - 20;

        guiGraphics.blit(MISC_TEXTURE, posX, posY, 0, 0, 94, 18);
        guiGraphics.blit(MISC_TEXTURE, posX + 1, posY + 1, 1, 19, bar, 16);

        if (radiation >= 25) {
            guiGraphics.blit(MISC_TEXTURE, posX + length + 2, posY - 18, 36, 36, 18, 18);
        } else if (radiation >= 10) {
            guiGraphics.blit(MISC_TEXTURE, posX + length + 2, posY - 18, 18, 36, 18, 18);
        } else if (radiation >= 2.5) {
            guiGraphics.blit(MISC_TEXTURE, posX + length + 2, posY - 18, 0, 36, 18, 18);
        }

        Font font = Minecraft.getInstance().font;

        if (radiation > 1000) {
            guiGraphics.drawString(font, ">1000 RAD/s", posX, posY - 8, 0xFF0000);
        } else if (radiation >= 1) {
            guiGraphics.drawString(font, Math.round(radiation) + " RAD/s", posX, posY - 8, 0xFF0000);
        } else if (radiation > 0) {
            guiGraphics.drawString(font, "<1 RAD/s", posX, posY - 8, 0xFF0000);
        }
    }

    private static int getScaled(double cur, double max, double scale) {
        return (int) Math.min(cur / max * scale, scale);
    }

    public static void renderCustomCrosshairs(GuiGraphics guiGraphics, Crosshair cross) {

        if(cross == Crosshair.NONE) return;
        Window window = Minecraft.getInstance().getWindow();

        int size = cross.size;

        guiGraphics.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        guiGraphics.blit(MISC_TEXTURE, window.getGuiScaledWidth() / 2 - (size / 2), window.getGuiScaledHeight() / 2 - (size / 2), cross.x, cross.y, size, size);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    /**
     * Portiert aus 1.7.10: der Helmvorsatz von ArmorFSB, ArmorGasMask und ArmorHazmat.
     *
     * Das Original zeichnet dafuer von Hand ein Viereck ueber den ganzen Schirm, mit
     * abgeschaltetem Tiefentest und der ueblichen Alpha-Mischung. Genau das macht blit
     * hier auch: das ganze 256x256-Bild wird auf die volle Schirmflaeche gezogen.
     */
    public static void renderHelmetOverlay(GuiGraphics guiGraphics, ResourceLocation texture) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(texture, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0F, 0F, 256, 256, 256, 256);

        RenderSystem.disableBlend();
    }
}
