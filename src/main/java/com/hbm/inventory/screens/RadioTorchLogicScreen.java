package com.hbm.inventory.screens;

import com.hbm.blockentity.network.RadioTorchLogicBlockEntity;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRadioTorchLogic.
 *
 * Sechzehn Zeilen in zwei Spalten. Jede besteht aus einem Knopf fuer die Vergleichsart und
 * einem Feld fuer die Konstante; der Knopf schaltet beim Anklicken weiter und laesst sich
 * ebenso mit dem Mausrad durchdrehen. Oben rechts drei Felder: Reihenfolge, Betriebsart,
 * Speichern.
 *
 * Die Vergleichsarten stehen bis zum Speichern NUR im Bildschirm -- so wie die Konstanten
 * auch. Sonst wuerde jedes Weiterschalten sofort wirken, und das Original will das nicht.
 */
public class RadioTorchLogicScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rtty_logic_receiver.png");

    /** Masse und Lage, alle aus dem Original. */
    private static final int KNOPF_GROESSE = 18;
    private static final int KNOPF_Y = 17;
    private static final int KNOPF_REIHENFOLGE_X = 137;
    private static final int KNOPF_ART_X = 173;
    private static final int KNOPF_SPEICHERN_X = 209;

    /** Anzahl der Vergleichsarten: 0 bis 9. */
    private static final int ARTEN = 10;

    private final RadioTorchLogicBlockEntity logik;

    private final int imageWidth = 256;
    private final int imageHeight = 204;
    private int leftPos;
    private int topPos;

    private EditBox frequenz;
    private EditBox[] konstante;
    private int[] arten;

    public RadioTorchLogicScreen(RadioTorchLogicBlockEntity logik) {
        super(Component.translatable("container.rttyLogic"));
        this.logik = logik;
    }

    @Override public boolean isPauseScreen() { return false; }

    private static int zeileX(int i) { return 7 + 130 * (i / 8); }
    private static int zeileY(int i) { return 53 + 18 * (i % 8); }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.frequenz = new EditBox(this.font, this.leftPos + 29, this.topPos + 21, 82, 14, Component.empty());
        this.schmuecke(this.frequenz, RadioTorchScreen.MAX_CHAN_LENGTH, this.logik.channel);

        this.konstante = new EditBox[16];
        this.arten = new int[16];

        for(int i = 0; i < 16; i++) {
            this.konstante[i] = new EditBox(this.font,
                    this.leftPos + zeileX(i) + 22, this.topPos + zeileY(i) + 4, 46, 14, Component.empty());
            this.schmuecke(this.konstante[i], 15, this.logik.mapping[i]);
            this.arten[i] = this.logik.conditions[i];
        }
    }

    private void schmuecke(EditBox feld, int hoechstlaenge, String inhalt) {
        feld.setTextColor(0x00ff00);
        feld.setTextColorUneditable(0x00ff00);
        feld.setBordered(false);
        feld.setMaxLength(hoechstlaenge);
        feld.setValue(inhalt == null ? "" : inhalt);
        this.addRenderableWidget(feld);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.logik.descending) guiGraphics.blit(TEXTURE, this.leftPos + KNOPF_REIHENFOLGE_X, this.topPos + KNOPF_Y, 0, 204, KNOPF_GROESSE, KNOPF_GROESSE);
        if(this.logik.polling) guiGraphics.blit(TEXTURE, this.leftPos + KNOPF_ART_X, this.topPos + KNOPF_Y, 0, 222, KNOPF_GROESSE, KNOPF_GROESSE);

        for(int i = 0; i < 16; i++) {

            int x = this.leftPos + zeileX(i);
            int y = this.topPos + zeileY(i);

            /*
             * Ob eine Zeile scharf ist, haengt an der Konstanten IM BLOCK, nicht an der im
             * Feld -- eine noch nicht gespeicherte Eingabe wirkt noch nicht. Die untere Reihe
             * der Knopfbilder ist die matte, die obere die scharfe.
             */
            if(this.logik.mapping[i] == null || this.logik.mapping[i].isEmpty()) {
                if(this.arten[i] != 0) {
                    guiGraphics.blit(TEXTURE, x, y, 18 + this.arten[i] * 18, 222, KNOPF_GROESSE, KNOPF_GROESSE);
                }
            } else {
                guiGraphics.blit(TEXTURE, x, y, 18 + this.arten[i] * 18, 204, KNOPF_GROESSE, KNOPF_GROESSE);
                guiGraphics.blit(TEXTURE, this.leftPos + 85 + 130 * (i / 8), this.topPos + 57 + 18 * (i % 8), 198, 204, 14, 10);
            }
        }

        for(EditBox feld : this.konstante) feld.render(guiGraphics, mouseX, mouseY, partialTick);
        this.frequenz.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title,
                this.leftPos + this.imageWidth / 2 - this.font.width(this.title) / 2, this.topPos + 6, 4210752, false);

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_REIHENFOLGE_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable(this.logik.descending ? "rtty.descending" : "rtty.ascending")), mouseX, mouseY);
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_ART_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable(this.logik.polling ? "rtty.polling" : "rtty.stateChange")), mouseX, mouseY);
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_SPEICHERN_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("rtty.saveSettings")), mouseX, mouseY);
        }

        int zeile = this.zeileUnter(mouseX, mouseY);
        if(zeile >= 0) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("desc.gui.rttyLogic.cond" + this.arten[zeile])), mouseX, mouseY);
        }
    }

    /**
     * Nummer des Vergleichsknopfes unter dem Zeiger, oder -1.
     *
     * Der Rahmen wird hier selbst gerechnet und nicht ueber ScreenUtils.isHovered: das Original
     * prueft die sechzehn Knoepfe oben einschliesslich und unten ausschliesslich, ScreenUtils
     * macht es andersherum. Eine Zeile misst achtzehn Pixel, da faende die falsche Kante die
     * Nachbarzeile.
     */
    private int zeileUnter(double mouseX, double mouseY) {
        for(int i = 0; i < 16; i++) {
            int x = this.leftPos + zeileX(i);
            int y = this.topPos + zeileY(i);
            if(x <= mouseX && x + KNOPF_GROESSE > mouseX && y <= mouseY && y + KNOPF_GROESSE > mouseY) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        int zeile = this.zeileUnter(mouseX, mouseY);
        if(zeile >= 0 && scrollY != 0) {
            this.arten[zeile] = (this.arten[zeile] + (scrollY > 0 ? 1 : ARTEN - 1)) % ARTEN;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.frequenz.mouseClicked(mouseX, mouseY, button);
        for(EditBox feld : this.konstante) feld.mouseClicked(mouseX, mouseY, button);

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_REIHENFOLGE_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("d", !this.logik.descending);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.logik.getBlockPos()));
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_ART_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("p", !this.logik.polling);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.logik.getBlockPos()));
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_SPEICHERN_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putString("c", this.frequenz.getValue());
            for(int i = 0; i < 16; i++) {
                tag.putString("m" + i, this.konstante[i].getValue());
                tag.putInt("k" + i, this.arten[i]);
            }
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.logik.getBlockPos()));
        }

        int zeile = this.zeileUnter(mouseX, mouseY);
        if(zeile >= 0) {
            ScreenUtils.click(this.minecraft);
            this.arten[zeile] = (this.arten[zeile] + 1) % ARTEN;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.frequenz.charTyped(codePoint, modifiers)) return true;
        for(EditBox feld : this.konstante) if(feld.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(this.frequenz.keyPressed(keyCode, scanCode, modifiers)) return true;
        for(EditBox feld : this.konstante) if(feld.keyPressed(keyCode, scanCode, modifiers)) return true;

        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if(keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
