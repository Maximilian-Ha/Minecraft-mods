package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.api.PanelString;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Der Energiebalken der Kartenleser.
 *
 * Gezeichnet wird er mit Rechtecken statt mit einer Textur: die Oberflaechen des Originals
 * haben an dieser Stelle keine, weil die Tafel dort ihren Strom aus IC2 bezog und den
 * Fuellstand gar nicht anzeigte.
 */
public final class EnergyBar {

    private static final int HEIGHT = 7;

    private static final int BORDER = 0xFF373737;
    private static final int BACKGROUND = 0xFF101010;
    private static final int FULL = 0xFFE0B000;
    private static final int LOW = 0xFFB03030;

    private EnergyBar() { }

    public static int height() {
        return HEIGHT;
    }

    /** Zeichnet den Balken. x und y sind die linke obere Ecke im Bildschirmraster. */
    public static void render(GuiGraphics guiGraphics, int x, int y, int width, int energy, int capacity) {
        guiGraphics.fill(x, y, x + width, y + HEIGHT, BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + HEIGHT - 1, BACKGROUND);

        if(capacity <= 0 || energy <= 0) return;

        int inner = width - 2;
        int filled = (int) ((long) inner * Math.min(energy, capacity) / capacity);
        if(filled <= 0) filled = 1;

        // Unter einem Zehntel wird der Balken rot -- dann steht die Tafel bald still.
        int color = energy * 10 < capacity ? LOW : FULL;
        guiGraphics.fill(x + 1, y + 1, x + 1 + filled, y + HEIGHT - 1, color);
    }

    /** Dasselbe stehend, fuer Oberflaechen, die in der Breite keinen Platz haben. */
    public static void renderVertical(GuiGraphics guiGraphics, int x, int y, int width, int height, int energy, int capacity) {
        guiGraphics.fill(x, y, x + width, y + height, BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BACKGROUND);

        if(capacity <= 0 || energy <= 0) return;

        int inner = height - 2;
        int filled = (int) ((long) inner * Math.min(energy, capacity) / capacity);
        if(filled <= 0) filled = 1;

        int color = energy * 10 < capacity ? LOW : FULL;
        guiGraphics.fill(x + 1, y + height - 1 - filled, x + width - 1, y + height - 1, color);
    }

    /** Die Einblendung beim Zeigen auf den Balken. */
    public static void tooltip(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY,
                               int x, int y, int width, int energy, int capacity) {
        tooltip(guiGraphics, font, mouseX, mouseY, x, y, width, HEIGHT, energy, capacity);
    }

    public static void tooltip(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY,
                               int x, int y, int width, int height, int energy, int capacity) {
        if(mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) return;

        guiGraphics.renderTooltip(font, Component.translatable("msg.ec.EnergyStored",
                PanelString.format(energy), PanelString.format(capacity)), mouseX, mouseY);
    }
}
