package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.menus.InfoPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiInfoPanel.
 *
 * Links die vier Faecher, rechts je ein Schalter fuer jede Zeile, die die steckende
 * Karte anbieten kann, darunter Beschriftung und Takt.
 *
 * Im Original waren die Schalter selbstgebaute Ankreuzfelder mit eigener Grafik; hier
 * sind es die Knoepfe von Minecraft. Ihr Text sagt mit einem Haken, ob die Zeile an ist.
 */
public class InfoPanelScreen extends AbstractContainerScreen<InfoPanelMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_info_panel.png");

    private int knownSettings = -1;

    public InfoPanelScreen(InfoPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 201;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();

        InfoPanelBlockEntity panel = menu.be;
        List<PanelSetting> settings = panel.getSettingsList();
        int current = panel.getDisplaySettings(panel.getItem(InfoPanelBlockEntity.SLOT_CARD));
        knownSettings = settingsFingerprint();

        int y = topPos + 18;
        for(PanelSetting setting : settings) {
            boolean on = (current & setting.displayBit) != 0;
            // Die Kennung des Knopfes ist die Stelle im Einstellungswort, nicht die Maske.
            int bitIndex = Integer.numberOfTrailingZeros(setting.displayBit);
            Component label = Component.literal(on ? "☑ " : "☐ ").append(setting.title);
            addRenderableWidget(Button.builder(label, button -> send(bitIndex))
                    .bounds(leftPos + 30, y, 138, 14).build());
            y += 15;
            if(y > topPos + 100) break;
        }

        addRenderableWidget(Button.builder(
                        Component.translatable("msg.ec.cbShowLabels")
                                .append(": ")
                                .append(Component.translatable(panel.getShowLabels() ? "options.on" : "options.off")),
                        button -> send(InfoPanelMenu.BUTTON_LABELS))
                .bounds(leftPos + 30, topPos + 101, 138, 14).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("msg.ec.PanelRefreshRate")
                                .append(": ")
                                .append(Component.translatable("msg.ec.Ticks", panel.getTickRate())),
                        button -> send(InfoPanelMenu.BUTTON_TICKRATE))
                .bounds(leftPos + 30, topPos + 84, 138, 14).build());

        if(panel.hasColorUpgrade()) {
            addRenderableWidget(Button.builder(Component.translatable("msg.ec.ColorText"),
                            button -> send(InfoPanelMenu.BUTTON_COLOR_TEXT))
                    .bounds(leftPos + 30, topPos + 67, 68, 14).build());
            addRenderableWidget(Button.builder(Component.translatable("msg.ec.ColorBackground"),
                            button -> send(InfoPanelMenu.BUTTON_COLOR_BACKGROUND))
                    .bounds(leftPos + 100, topPos + 67, 68, 14).build());
        }
    }

    /**
     * Woran sich erkennen laesst, dass die Knoepfe nicht mehr passen: andere Karte,
     * andere Schalterstellungen, andere Aufwertungen.
     */
    private int settingsFingerprint() {
        InfoPanelBlockEntity panel = menu.be;
        int hash = panel.getItem(InfoPanelBlockEntity.SLOT_CARD).getItem().hashCode();
        hash = hash * 31 + panel.getDisplaySettings(panel.getItem(InfoPanelBlockEntity.SLOT_CARD));
        hash = hash * 31 + panel.getTickRate();
        hash = hash * 31 + (panel.getShowLabels() ? 1 : 0);
        hash = hash * 31 + (panel.hasColorUpgrade() ? 1 : 0);
        return hash;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if(settingsFingerprint() != knownSettings) rebuildButtons();
    }

    private void send(int id) {
        if(minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
