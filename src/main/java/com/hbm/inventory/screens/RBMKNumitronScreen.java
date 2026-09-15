package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKNumitronBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKNumitronBlockEntity.DisplayUnit;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRBMKDisplay.
 *
 * Die Einstelloberflaeche der Ziffernanzeige. Je Anzeige gibt es zwei Eingabefelder
 * (Beschriftung und Kanal) und vier Schalter: sichtbar, staendig abfragen, grosse Zahlen
 * abkuerzen, fuehrende Nullen schreiben.
 *
 * Alle Koordinaten unveraendert.
 */
public class RBMKNumitronScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rbmk_numitron.png");

    private static final int DISPLAYS = RBMKNumitronBlockEntity.DISPLAYS;

    private final RBMKNumitronBlockEntity numitron;

    private final int imageWidth = 256;
    private final int imageHeight = 150;
    private int leftPos;
    private int topPos;

    private final EditBox[] label = new EditBox[DISPLAYS];
    private final EditBox[] rtty = new EditBox[DISPLAYS];
    private final boolean[] active = new boolean[DISPLAYS];
    private final boolean[] polling = new boolean[DISPLAYS];
    private final boolean[] shortenNumber = new boolean[DISPLAYS];
    private final boolean[] leadingZeroes = new boolean[DISPLAYS];

    public RBMKNumitronScreen(RBMKNumitronBlockEntity numitron) {
        super(Component.translatable("container.rbmkNumitron"));
        this.numitron = numitron;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int oX = 4;
        int oY = 4;

        for(int i = 0; i < DISPLAYS; i++) {

            DisplayUnit unit = this.numitron.displays[i];

            this.label[i] = this.field(this.leftPos + 27 + oX, this.topPos + 73 + oY + i * 54, 85 - oX * 2, 30, unit.label);
            this.rtty[i] = this.field(this.leftPos + 27 + oX, this.topPos + 55 + oY + i * 54, 85 - oX * 2, RadioTorchScreen.MAX_CHAN_LENGTH, unit.rtty);

            this.active[i] = unit.active;
            this.polling[i] = unit.polling;
            this.shortenNumber[i] = unit.shortenNumber;
            this.leadingZeroes[i] = unit.leadingZeroes;
        }
    }

    private EditBox field(int x, int y, int width, int maxLength, String value) {
        EditBox box = new EditBox(this.font, x, y, width, 14, Component.empty());
        box.setTextColor(0x00ff00);
        box.setTextColorUneditable(0x00ff00);
        box.setBordered(false);
        box.setMaxLength(maxLength);
        box.setValue(value == null ? "" : value);
        this.addRenderableWidget(box);
        return box;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title,
                this.leftPos + this.imageWidth / 2 - this.font.width(this.title) / 2, this.topPos + 6, 4210752, false);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        for(int i = 0; i < DISPLAYS; i++) {
            if(this.active[i]) guiGraphics.blit(TEXTURE, this.leftPos + 124, this.topPos + i * 54 + 54, 18, 150, 16, 16);
            if(this.polling[i]) guiGraphics.blit(TEXTURE, this.leftPos + 159, this.topPos + i * 54 + 53, 0, 150, 18, 18);
            if(this.shortenNumber[i]) guiGraphics.blit(TEXTURE, this.leftPos + 195, this.topPos + i * 54 + 53, 34, 150, 18, 18);
            if(this.leadingZeroes[i]) guiGraphics.blit(TEXTURE, this.leftPos + 231, this.topPos + i * 54 + 53, 52, 150, 18, 18);
        }

        for(int i = 0; i < DISPLAYS; i++) {
            this.label[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.rtty[i].render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int i = 0; i < DISPLAYS; i++) {

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 124, i * 54 + 54, 16, 16)) {
                this.active[i] = !this.active[i];
                ScreenUtils.click(this.minecraft);
                return true;
            }

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 159, i * 54 + 53, 18, 18)) {
                this.polling[i] = !this.polling[i];
                ScreenUtils.click(this.minecraft);
                return true;
            }

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 195, i * 54 + 53, 18, 18)) {
                this.shortenNumber[i] = !this.shortenNumber[i];
                ScreenUtils.click(this.minecraft);
                return true;
            }

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 231, i * 54 + 53, 18, 18)) {
                this.leadingZeroes[i] = !this.leadingZeroes[i];
                ScreenUtils.click(this.minecraft);
                return true;
            }
        }

        if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 209, 17, 18, 18)) {
            ScreenUtils.click(this.minecraft);
            this.send();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Schickt alle Einstellungen in einem Rutsch zurueck. */
    private void send() {

        CompoundTag tag = new CompoundTag();

        byte activeBits = 0;
        byte pollingBits = 0;
        byte shortenBits = 0;
        byte leadingBits = 0;

        for(int i = 0; i < DISPLAYS; i++) {
            if(this.active[i]) activeBits |= (byte) (1 << i);
            if(this.polling[i]) pollingBits |= (byte) (1 << i);
            if(this.shortenNumber[i]) shortenBits |= (byte) (1 << i);
            if(this.leadingZeroes[i]) leadingBits |= (byte) (1 << i);
        }

        tag.putByte("active", activeBits);
        tag.putByte("polling", pollingBits);
        tag.putByte("shorten_number", shortenBits);
        tag.putByte("leading_zeroes", leadingBits);

        for(int i = 0; i < DISPLAYS; i++) {
            tag.putString("label" + i, this.label[i].getValue());
            tag.putString("rtty" + i, this.rtty[i].getValue());
        }

        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.numitron.getBlockPos()));
    }
}
