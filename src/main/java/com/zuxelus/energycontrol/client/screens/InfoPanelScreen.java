package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.items.cards.ItemCardText;
import com.zuxelus.energycontrol.menus.InfoPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiInfoPanel.
 *
 * Links die vier Faecher, oben der Energiebalken, darunter Beschriftung und Takt, in der
 * Mitte je ein Schalter fuer jede Zeile, die die steckende Karte anbieten kann, unten die
 * Farbwahl -- die nur erscheint, wenn die Farbaufwertung steckt.
 *
 * Im Original waren die Schalter selbstgebaute Ankreuzfelder mit eigener Grafik; hier sind es
 * die Knoepfe von Minecraft. Ihr Text sagt mit einem Haken, ob die Zeile an ist.
 */
public class InfoPanelScreen extends AbstractContainerScreen<InfoPanelMenu> {

    private static final ResourceLocation TEXTURE = EnergyControl.loc("textures/gui/gui_info_panel.png");

    private static final int BAR_X = 8;
    private static final int BAR_Y = 16;
    private static final int BAR_WIDTH = 160;

    /** Zwei Spalten mal vier Zeilen -- so viele Schalter bietet keine Karte je an. */
    private static final int SETTING_COLUMNS = 2;
    private static final int SETTING_ROWS = 4;
    private static final int SETTING_WIDTH = 68;
    private static final int SETTING_HEIGHT = 12;
    private static final int SETTING_X = 30;
    private static final int SETTING_Y = 44;
    /** Drei Knoepfe in der unteren Reihe, von x=30 bis zum rechten Rand bei x=168. */
    private static final int BOTTOM_WIDTH = 45;

    private int knownState = -1;

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
        knownState = fingerprint();

        InfoPanelBlockEntity panel = menu.be;

        addRenderableWidget(Button.builder(
                        Component.translatable("msg.ec.cbShowLabels")
                                .append(": ")
                                .append(Component.translatable(panel.getShowLabels() ? "options.on" : "options.off")),
                        button -> send(InfoPanelMenu.BUTTON_LABELS))
                .bounds(leftPos + 8, topPos + 26, 80, 13).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("msg.ec.PanelRefreshRate")
                                .append(": ")
                                .append(Component.translatable("msg.ec.Ticks", panel.getTickRate())),
                        button -> send(InfoPanelMenu.BUTTON_TICKRATE))
                .bounds(leftPos + 90, topPos + 26, 78, 13).build());

        List<PanelSetting> settings = panel.getSettingsList();
        int current = panel.getDisplaySettings(panel.getItem(InfoPanelBlockEntity.SLOT_CARD));

        for(int i = 0; i < settings.size() && i < SETTING_COLUMNS * SETTING_ROWS; i++) {
            PanelSetting setting = settings.get(i);
            boolean on = (current & setting.displayBit) != 0;
            // Die Kennung des Knopfes ist die Stelle im Einstellungswort, nicht die Maske.
            int bitIndex = Integer.numberOfTrailingZeros(setting.displayBit);
            Component label = Component.literal(on ? "☑ " : "☐ ").append(setting.title);

            int x = leftPos + SETTING_X + (i % SETTING_COLUMNS) * (SETTING_WIDTH + 2);
            int y = topPos + SETTING_Y + (i / SETTING_COLUMNS) * SETTING_HEIGHT;
            addRenderableWidget(Button.builder(label, button -> send(bitIndex))
                    .bounds(x, y, SETTING_WIDTH, SETTING_HEIGHT).build());
        }

        /*
         * Die untere Reihe fasst bis zu drei kurze Knoepfe: die beiden Farbwahlen, wenn die
         * Farbaufwertung steckt, und die Texteingabe, wenn eine Textkarte im Fach liegt. Die
         * Beschriftungen sind bewusst kurz -- drei Knoepfe nebeneinander haben je 46 Punkte,
         * und ein ueberlaufender Text sieht schlechter aus als ein knapper.
         */
        ItemStack card = panel.getItem(InfoPanelBlockEntity.SLOT_CARD);
        int x = leftPos + 30;

        if(panel.hasColorUpgrade()) {
            addRenderableWidget(Button.builder(Component.translatable("msg.ec.ColorTextShort"),
                            button -> openColorPicker("colorText", panel.getColorText(), "msg.ec.ColorText"))
                    .bounds(x, topPos + 92, BOTTOM_WIDTH, SETTING_HEIGHT).build());
            x += BOTTOM_WIDTH + 2;

            addRenderableWidget(Button.builder(Component.translatable("msg.ec.ColorBackgroundShort"),
                            button -> openColorPicker("colorBackground", panel.getColorBackground(), "msg.ec.ColorBackground"))
                    .bounds(x, topPos + 92, BOTTOM_WIDTH, SETTING_HEIGHT).build());
            x += BOTTOM_WIDTH + 2;
        }

        if(card.getItem() instanceof ItemCardText) {
            addRenderableWidget(Button.builder(Component.translatable("gui.energycontrol.edit_text"),
                            button -> openTextEditor(card))
                    .bounds(x, topPos + 92, BOTTOM_WIDTH, SETTING_HEIGHT).build());
        }
    }

    private void openColorPicker(String action, int current, String titleKey) {
        if(minecraft == null) return;
        minecraft.setScreen(new ColorPickerScreen(this, menu.be.getBlockPos(), action, current,
                Component.translatable(titleKey)));
    }

    private void openTextEditor(ItemStack card) {
        if(minecraft == null) return;
        minecraft.setScreen(new CardTextScreen(this, menu.be.getBlockPos(), card));
    }

    /**
     * Woran sich erkennen laesst, dass die Knoepfe nicht mehr passen: andere Karte, andere
     * Schalterstellungen, anderer Takt, andere Aufwertungen.
     */
    private int fingerprint() {
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
        if(fingerprint() != knownState) rebuildButtons();
    }

    private void send(int id) {
        if(minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Den Hintergrund zeichnet AbstractContainerScreen selbst -- ein zweiter Aufruf
        // legte die Abdunklung doppelt uebereinander.
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if(ECConfig.requirePower()) {
            EnergyBar.tooltip(guiGraphics, font, mouseX, mouseY, leftPos + BAR_X, topPos + BAR_Y, BAR_WIDTH,
                    menu.getSyncedEnergy(), menu.be.getMaxEnergyStored());
        }

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if(ECConfig.requirePower()) {
            EnergyBar.render(guiGraphics, leftPos + BAR_X, topPos + BAR_Y, BAR_WIDTH,
                    menu.getSyncedEnergy(), menu.be.getMaxEnergyStored());
        }
    }
}
