package com.hbm.inventory.screens;

import com.hbm.blockentity.network.RadioTelexBlockEntity;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Random;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRadioTelex.
 *
 * Oben die fuenf Sendezeilen, die man hier tippt, unten die fuenf Empfangszeilen. Die
 * Steuerzeichen bekommen eigene Knoepfe und erscheinen im Text als rote Kuerzel.
 *
 * ABWEICHUNG: das Original zieht den Ausschlag des Senders als GL_LINES-Zug mit dem
 * Tessellator. In 1.21 gibt es im Oberflaechenzeichner keine Linien mehr; der Zug entsteht
 * hier aus lauter kleinen Rechtecken, eines je Schritt. Dieselbe Kurve, andere Grundfigur.
 */
public class RadioTelexScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_telex.png");

    private final RadioTelexBlockEntity telex;

    private final int imageWidth = 256;
    private final int imageHeight = 244;
    private int leftPos;
    private int topPos;

    private EditBox txFrequency;
    private EditBox rxFrequency;

    private boolean textFocus = false;
    private final String[] txBuffer;
    private int cursorPos = 0;

    public RadioTelexScreen(RadioTelexBlockEntity telex) {
        super(Component.translatable("container.radiotelex"));
        this.telex = telex;

        this.txBuffer = new String[telex.txBuffer.length];
        System.arraycopy(telex.txBuffer, 0, this.txBuffer, 0, this.txBuffer.length);

        for(int i = this.txBuffer.length - 1; i > 0; i--) {
            if(!this.txBuffer[i].isEmpty()) { this.cursorPos = i; break; }
        }
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.txFrequency = this.kanalfeld(this.topPos + 110, this.telex.txChannel);
        this.rxFrequency = this.kanalfeld(this.topPos + 224, this.telex.rxChannel);

        this.addRenderableWidget(this.txFrequency);
        this.addRenderableWidget(this.rxFrequency);
    }

    private EditBox kanalfeld(int y, String wert) {
        EditBox feld = new EditBox(this.font, this.leftPos + 29, y, 90, 14, Component.empty());
        feld.setTextColor(0x00ff00);
        feld.setTextColorUneditable(0x00ff00);
        feld.setBordered(false);
        feld.setMaxLength(10);
        feld.setValue(wert == null ? "" : wert);
        return feld;
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

        this.txFrequency.render(guiGraphics, mouseX, mouseY, partialTick);
        this.rxFrequency.render(guiGraphics, mouseX, mouseY, partialTick);

        this.zeichneSendeText(guiGraphics);
        this.zeichneEmpfangsText(guiGraphics);
        this.zeichneAusschlag(guiGraphics);
    }

    /** Die Sendezeilen: jedes Zeichen einzeln, damit die Schrittweite gleich bleibt. */
    private void zeichneSendeText(GuiGraphics guiGraphics) {

        for(int zeile = 0; zeile < RadioTelexBlockEntity.LINES; zeile++) {

            String inhalt = this.txBuffer[zeile];
            int y = 11 + 14 * zeile;
            String format = ChatFormatting.RESET.toString();

            for(int i = 0; i < inhalt.length(); i++) {

                char c = inhalt.charAt(i);
                int x = 11 + 7 * i + (7 - this.font.width(String.valueOf(c))) / 2;

                if(c == '\u00a7' && inhalt.length() > i + 1) {
                    format = "\u00a7" + inhalt.charAt(i + 1);
                    x -= 3;
                }

                String glyphe = format + c;
                if(c == RadioTelexBlockEntity.BELL)  glyphe = ChatFormatting.RED + "B";
                if(c == RadioTelexBlockEntity.PRINT) glyphe = ChatFormatting.RED + "P";
                if(c == RadioTelexBlockEntity.CLEAR) glyphe = ChatFormatting.RED + "<";
                if(c == RadioTelexBlockEntity.PAUSE) glyphe = ChatFormatting.RED + "W";

                guiGraphics.drawString(this.font, glyphe, this.leftPos + x, this.topPos + y, 0x00ff00, false);
            }

            // Der Schreibbalken blinkt im Sekundentakt, aber nur in der gewaehlten Zeile.
            if(this.textFocus && this.cursorPos == zeile && Util.getMillis() % 1000 < 500) {
                int x = Math.max(11 + 7 * (inhalt.length() - 1) + 7, 11);
                guiGraphics.drawString(this.font, "|", this.leftPos + x, this.topPos + y, 0x00ff00, false);
            }
        }
    }

    /**
     * Die Empfangszeilen. Anders als oben wandert x hier mit, weil ein Farbzeichen und das
     * Zeichen dahinter zusammen keine Breite belegen duerfen -- so steht es im Original.
     */
    private void zeichneEmpfangsText(GuiGraphics guiGraphics) {

        for(int zeile = 0; zeile < RadioTelexBlockEntity.LINES; zeile++) {

            String inhalt = this.telex.rxBuffer[zeile];
            int y = 145 + 14 * zeile;
            String format = ChatFormatting.RESET.toString();
            int x = 11;

            for(int i = 0; i < inhalt.length(); i++) {

                char c = inhalt.charAt(i);
                x += (7 - this.font.width(String.valueOf(c))) / 2;

                if(c == '\u00a7' && inhalt.length() > i + 1) {
                    format = "\u00a7" + inhalt.charAt(i + 1);
                    c = ' ';
                } else if(c == '\u00a7') {
                    c = ' ';
                } else if(i > 0 && inhalt.charAt(i - 1) == '\u00a7') {
                    c = ' ';
                    x -= 14;
                }

                guiGraphics.drawString(this.font, format + c, this.leftPos + x, this.topPos + y, 0x00ff00, false);
                x += 7;
            }
        }
    }

    /**
     * Der Ausschlag des Senders. Der Zufall haengt am gerade gesendeten Zeichen, damit
     * dieselbe Sendung immer denselben Zug ergibt -- genau wie im Original.
     */
    private void zeichneAusschlag(GuiGraphics guiGraphics) {

        Random zufall = new Random(this.telex.sendingChar);
        int mitte = this.topPos + 93;
        double vorher = 0;

        for(int i = 0; i < 48; i++) {

            double jetzt = 0;
            if(this.telex.sendingChar != ' ' && i > 4 && i < 43) {
                jetzt = Mth.clamp(zufall.nextGaussian() * 7, -7D, 7D);
            }

            int x = this.leftPos + 199 + i;
            int von = (int) Math.round(Math.min(vorher, jetzt));
            int bis = (int) Math.round(Math.max(vorher, jetzt));

            // Ein Rechteck je Schritt, hoch genug um beide Enden zu verbinden.
            guiGraphics.fill(x, mitte + von, x + 2, mitte + bis + 2, 0xff00ff00);

            vorher = jetzt;
        }
    }

    private void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        this.hinweis(guiGraphics, mouseX, mouseY,   7,  85, ChatFormatting.GOLD, "bell");
        this.hinweis(guiGraphics, mouseX, mouseY,  27,  85, ChatFormatting.GOLD, "print");
        this.hinweis(guiGraphics, mouseX, mouseY,  47,  85, ChatFormatting.GOLD, "clear");
        this.hinweis(guiGraphics, mouseX, mouseY,  67,  85, ChatFormatting.GOLD, "format");
        this.hinweis(guiGraphics, mouseX, mouseY,  87,  85, ChatFormatting.GOLD, "pause");

        this.hinweis(guiGraphics, mouseX, mouseY, 127, 105, ChatFormatting.GREEN, "save");
        this.hinweis(guiGraphics, mouseX, mouseY, 147, 105, ChatFormatting.YELLOW, "send");
        this.hinweis(guiGraphics, mouseX, mouseY, 167, 105, ChatFormatting.RED, "delete");

        this.hinweis(guiGraphics, mouseX, mouseY, 127, 219, ChatFormatting.GREEN, "save");
        this.hinweis(guiGraphics, mouseX, mouseY, 147, 219, ChatFormatting.AQUA, "rxprint");
        this.hinweis(guiGraphics, mouseX, mouseY, 167, 219, ChatFormatting.RED, "rxclear");
    }

    private void hinweis(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top, ChatFormatting farbe, String schluessel) {
        if(!ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, left, top, 18, 18)) return;
        guiGraphics.renderComponentTooltip(this.font, List.of(
                Component.translatable("container.radiotelex." + schluessel).withStyle(farbe),
                Component.translatable("container.radiotelex." + schluessel + ".desc")), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.txFrequency.mouseClicked(mouseX, mouseY, button);
        this.rxFrequency.mouseClicked(mouseX, mouseY, button);

        this.textFocus = ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 7, 7, 242, 74);

        char zeichen = '\0';
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY,  7, 85, 18, 18)) zeichen = RadioTelexBlockEntity.BELL;
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 27, 85, 18, 18)) zeichen = RadioTelexBlockEntity.PRINT;
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 47, 85, 18, 18)) zeichen = RadioTelexBlockEntity.CLEAR;
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 67, 85, 18, 18)) zeichen = '\u00a7';
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 87, 85, 18, 18)) zeichen = RadioTelexBlockEntity.PAUSE;

        String befehl = null;
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 127, 105, 18, 18)
                || ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 127, 219, 18, 18)) befehl = "sve";
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 147, 105, 18, 18)) befehl = "snd";
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 147, 219, 18, 18)) befehl = "rxprt";
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 167, 219, 18, 18)) befehl = "rxcls";

        // Der Loeschknopf des Sendespeichers wirkt nur hier und schickt den leeren Speicher mit.
        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 167, 105, 18, 18)) {
            ScreenUtils.click(this.minecraft);
            for(int i = 0; i < this.txBuffer.length; i++) this.txBuffer[i] = "";
            this.sendeSpeicher(new CompoundTag());
        }

        if(befehl != null) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putString("cmd", befehl);

            if("snd".equals(befehl)) {
                for(int i = 0; i < this.txBuffer.length; i++) tag.putString("tx" + i, this.txBuffer[i]);
            }
            if("sve".equals(befehl)) {
                tag.putString("txChan", this.txFrequency.getValue());
                tag.putString("rxChan", this.rxFrequency.getValue());
            }

            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.telex.getBlockPos()));
        }

        if(zeichen != '\0') {
            ScreenUtils.click(this.minecraft);
            this.textFocus = true;
            this.txFrequency.setFocused(false);
            this.rxFrequency.setFocused(false);
            this.tippe(zeichen);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {

        if(this.txFrequency.charTyped(codePoint, modifiers)) return true;
        if(this.rxFrequency.charTyped(codePoint, modifiers)) return true;

        if(this.textFocus && StringUtil.isAllowedChatCharacter(codePoint)) {
            this.tippe(codePoint);
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(this.txFrequency.keyPressed(keyCode, scanCode, modifiers)) return true;
        if(this.rxFrequency.keyPressed(keyCode, scanCode, modifiers)) return true;

        if(this.textFocus) {

            if(keyCode == GLFW.GLFW_KEY_UP) this.cursorPos--;
            if(keyCode == GLFW.GLFW_KEY_DOWN) this.cursorPos++;
            this.cursorPos = Mth.clamp(this.cursorPos, 0, RadioTelexBlockEntity.LINES - 1);

            if(keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.txBuffer[this.cursorPos].isEmpty()) {
                String zeile = this.txBuffer[this.cursorPos];
                this.txBuffer[this.cursorPos] = zeile.substring(0, zeile.length() - 1);
                return true;
            }

            if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.textFocus = false;
                return true;
            }
        }

        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if(keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void tippe(char c) {
        String zeile = this.txBuffer[this.cursorPos];
        if(zeile.length() < RadioTelexBlockEntity.LINE_WIDTH) this.txBuffer[this.cursorPos] = zeile + c;
    }

    /** Beim Schliessen wandert der getippte Text zum Geraet, damit er nicht verlorengeht. */
    @Override
    public void onClose() {
        this.sendeSpeicher(new CompoundTag());
        super.onClose();
    }

    private void sendeSpeicher(CompoundTag tag) {
        for(int i = 0; i < this.txBuffer.length; i++) tag.putString("tx" + i, this.txBuffer[i]);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.telex.getBlockPos()));
    }
}
