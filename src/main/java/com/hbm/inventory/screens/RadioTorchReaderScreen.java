package com.hbm.inventory.screens;

import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.network.RadioTorchReaderBlockEntity;
import com.hbm.blocks.network.RadioTorchBaseBlock;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRadioTorchReader.
 *
 * Acht Zeilen zu je zwei Feldern -- links der Kanal, rechts der Name des Wertes --, dazu drei
 * Felder oben: das linke zeigt beim Ueberfahren, welche Werte die Maschine dahinter ueberhaupt
 * hergibt, das mittlere schaltet die Betriebsart um, das rechte schickt alle sechzehn
 * Eingaben zum Block.
 *
 * Wie Sender und Empfaenger hat der Leser kein Inventar und damit kein Menue: der Bildschirm
 * haelt die Blockentitaet unmittelbar und geht ueber IScreenProvider auf.
 */
public class RadioTorchReaderScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rtty_reader.png");

    /** Masse und Lage der drei Schaltflaechen, alle aus dem Original. */
    private static final int KNOPF_GROESSE = 18;
    private static final int KNOPF_Y = 17;
    private static final int KNOPF_INFO_X = 29;
    private static final int KNOPF_ART_X = 173;
    private static final int KNOPF_SPEICHERN_X = 209;

    /** Hoechstlaenge eines Wertnamens, aus dem Original. */
    private static final int MAX_NAME_LENGTH = 25;

    private final RadioTorchReaderBlockEntity leser;

    private final int imageWidth = 256;
    private final int imageHeight = 204;
    private int leftPos;
    private int topPos;

    private EditBox[] kanal;
    private EditBox[] name;

    public RadioTorchReaderScreen(RadioTorchReaderBlockEntity leser) {
        super(Component.translatable("container.rttyReader"));
        this.leser = leser;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.kanal = new EditBox[RadioTorchReaderBlockEntity.KANAELE];
        this.name = new EditBox[RadioTorchReaderBlockEntity.KANAELE];

        for(int i = 0; i < RadioTorchReaderBlockEntity.KANAELE; i++) {

            this.kanal[i] = this.feld(this.leftPos + 29, this.topPos + 57 + i * 18, 64,
                    RadioTorchScreen.MAX_CHAN_LENGTH, this.leser.channels[i]);
            this.name[i] = this.feld(this.leftPos + 123, this.topPos + 57 + i * 18, 118,
                    MAX_NAME_LENGTH, this.leser.names[i]);
        }
    }

    private EditBox feld(int x, int y, int breite, int hoechstlaenge, String inhalt) {

        EditBox feld = new EditBox(this.font, x, y, breite, 14, Component.empty());
        feld.setTextColor(0x00ff00);
        feld.setTextColorUneditable(0x00ff00);
        feld.setBordered(false);
        feld.setMaxLength(hoechstlaenge);
        feld.setValue(inhalt == null ? "" : inhalt);
        this.addRenderableWidget(feld);
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

        // Der Umschalter leuchtet, solange jeden Tick gefunkt wird.
        if(this.leser.polling) {
            guiGraphics.blit(TEXTURE, this.leftPos + KNOPF_ART_X, this.topPos + KNOPF_Y, 0, 204, KNOPF_GROESSE, KNOPF_GROESSE);
        }

        for(EditBox feld : this.kanal) feld.render(guiGraphics, mouseX, mouseY, partialTick);
        for(EditBox feld : this.name) feld.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title,
                this.leftPos + this.imageWidth / 2 - this.font.width(this.title) / 2, this.topPos + 6, 4210752, false);

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_INFO_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, this.lesbareWerte(), mouseX, mouseY);
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_ART_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable(this.leser.polling ? "rtty.polling" : "rtty.stateChange")), mouseX, mouseY);
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_SPEICHERN_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("rtty.saveSettings")), mouseX, mouseY);
        }
    }

    /**
     * Was die Maschine hinter der Fackel hergibt. Ohne diese Liste muesste man die Wertnamen
     * raten -- sie stehen sonst nirgends.
     */
    private List<Component> lesbareWerte() {

        List<Component> zeilen = new ArrayList<>();
        zeilen.add(Component.translatable("rtty.readableValues"));

        Direction hinten = this.leser.getBlockState().getValue(RadioTorchBaseBlock.FACING).getOpposite();

        if(this.leser.getLevel() != null
                && this.leser.getLevel().getBlockEntity(this.leser.getBlockPos().relative(hinten)) instanceof IRORValueProvider geber) {

            for(String eintrag : geber.getFunctionInfo()) {
                if(eintrag.startsWith(IRORValueProvider.PREFIX_VALUE)) {
                    zeilen.add(Component.literal(eintrag.substring(IRORValueProvider.PREFIX_VALUE.length()))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }

        return zeilen;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(EditBox feld : this.kanal) feld.mouseClicked(mouseX, mouseY, button);
        for(EditBox feld : this.name) feld.mouseClicked(mouseX, mouseY, button);

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_ART_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("p", !this.leser.polling);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.leser.getBlockPos()));
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, KNOPF_SPEICHERN_X, KNOPF_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            for(int i = 0; i < RadioTorchReaderBlockEntity.KANAELE; i++) {
                tag.putString("c" + i, this.kanal[i].getValue());
                tag.putString("n" + i, this.name[i].getValue());
            }
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.leser.getBlockPos()));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for(EditBox feld : this.kanal) if(feld.charTyped(codePoint, modifiers)) return true;
        for(EditBox feld : this.name) if(feld.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        for(EditBox feld : this.kanal) if(feld.keyPressed(keyCode, scanCode, modifiers)) return true;
        for(EditBox feld : this.name) if(feld.keyPressed(keyCode, scanCode, modifiers)) return true;

        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if(keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
