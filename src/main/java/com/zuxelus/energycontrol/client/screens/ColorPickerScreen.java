package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Die Farbwahl fuer Schrift und Hintergrund einer Tafel.
 *
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiScreenColor. Das Original hatte dafuer
 * zwei Grafiken (gui_colors, gui_color_picker) mit einem Farbfeld darauf. Hier stehen die
 * sechzehn Farben des Originals als Felder zur Wahl, und wer eine andere will, tippt sie als
 * Sechsstelligen Hexwert ein -- das ist mehr Freiheit als die Vorlage bot und braucht keine
 * eigene Grafik.
 */
public class ColorPickerScreen extends Screen {

    private static final int SWATCH = 18;
    private static final int COLUMNS = 8;

    private final Screen parent;
    private final BlockPos pos;
    private final String action;

    private int selected;
    private EditBox hex;

    public ColorPickerScreen(Screen parent, BlockPos pos, String action, int current, Component title) {
        super(title);
        this.parent = parent;
        this.pos = pos;
        this.action = action;
        this.selected = current & 0xFFFFFF;
    }

    private int gridLeft() {
        return (width - COLUMNS * SWATCH) / 2;
    }

    private int gridTop() {
        return height / 2 - 40;
    }

    @Override
    protected void init() {
        super.init();

        hex = new EditBox(font, width / 2 - 40, gridTop() + 3 * SWATCH + 8, 80, 16, Component.empty());
        hex.setMaxLength(6);
        hex.setValue(String.format("%06X", selected));
        hex.setResponder(value -> {
            try {
                selected = Integer.parseInt(value.trim(), 16) & 0xFFFFFF;
            } catch(NumberFormatException ignored) {
                // Halb getippte Eingabe -- die Vorschau bleibt einfach stehen.
            }
        });
        addRenderableWidget(hex);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> apply())
                .bounds(width / 2 - 82, gridTop() + 3 * SWATCH + 30, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> back())
                .bounds(width / 2 + 2, gridTop() + 3 * SWATCH + 30, 80, 20).build());
    }

    private void apply() {
        ControlSender.sendInt(pos, action, selected);
        back();
    }

    private void back() {
        if(minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = swatchAt(mouseX, mouseY);
        if(index >= 0) {
            selected = InfoPanelBlockEntity.COLORS[index];
            hex.setValue(String.format("%06X", selected));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int swatchAt(double mouseX, double mouseY) {
        int left = gridLeft();
        int top = gridTop();
        for(int i = 0; i < InfoPanelBlockEntity.COLORS.length; i++) {
            int x = left + (i % COLUMNS) * SWATCH;
            int y = top + (i / COLUMNS) * SWATCH;
            if(mouseX >= x && mouseX < x + SWATCH - 2 && mouseY >= y && mouseY < y + SWATCH - 2) return i;
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(font, title, width / 2, gridTop() - 24, 0xFFFFFF);

        int left = gridLeft();
        int top = gridTop();
        for(int i = 0; i < InfoPanelBlockEntity.COLORS.length; i++) {
            int x = left + (i % COLUMNS) * SWATCH;
            int y = top + (i / COLUMNS) * SWATCH;
            guiGraphics.fill(x - 1, y - 1, x + SWATCH - 1, y + SWATCH - 1, 0xFF000000);
            guiGraphics.fill(x, y, x + SWATCH - 2, y + SWATCH - 2, 0xFF000000 | InfoPanelBlockEntity.COLORS[i]);
        }

        // Vorschau der gewaehlten Farbe neben dem Eingabefeld.
        int previewX = width / 2 + 46;
        int previewY = gridTop() + 3 * SWATCH + 8;
        guiGraphics.fill(previewX - 1, previewY - 1, previewX + 17, previewY + 17, 0xFF000000);
        guiGraphics.fill(previewX, previewY, previewX + 16, previewY + 16, 0xFF000000 | selected);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        back();
    }
}
