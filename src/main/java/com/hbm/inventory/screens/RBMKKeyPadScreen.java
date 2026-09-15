package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKKeyPadBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKKeyPadBlockEntity.KeyUnit;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRBMKKeyPad.
 *
 * Je Taste vier Eingabefelder: Farbe (sechsstellig hexadezimal), Beschriftung, Kanal und der zu
 * sendende Befehl. Dazu die beiden Schalter fuer sichtbar und staendig senden.
 *
 * Alle Koordinaten unveraendert.
 */
public class RBMKKeyPadScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rbmk_keypad.png");

    private static final int KEYS = RBMKKeyPadBlockEntity.KEYS;

    private final RBMKKeyPadBlockEntity keypad;

    private final int imageWidth = 256;
    private final int imageHeight = 204;
    private int leftPos;
    private int topPos;

    private final EditBox[] color = new EditBox[KEYS];
    private final EditBox[] label = new EditBox[KEYS];
    private final EditBox[] rtty = new EditBox[KEYS];
    private final EditBox[] cmd = new EditBox[KEYS];
    private final boolean[] active = new boolean[KEYS];
    private final boolean[] polling = new boolean[KEYS];

    public RBMKKeyPadScreen(RBMKKeyPadBlockEntity keypad) {
        super(Component.translatable("container.rbmkKeyPad"));
        this.keypad = keypad;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int oX = 4;
        int oY = 4;

        for(int i = 0; i < KEYS; i++) {

            KeyUnit unit = this.keypad.keys[i];

            this.color[i] = this.field(this.leftPos + 27 + oX, this.topPos + 55 + oY + i * 36, 72 - oX * 2, 6, String.format("%06x", unit.color));
            this.label[i] = this.field(this.leftPos + 175 + oX, this.topPos + 55 + oY + i * 36, 72 - oX * 2, 15, unit.label);
            this.rtty[i] = this.field(this.leftPos + 27 + oX, this.topPos + 73 + oY + i * 36, 72 - oX * 2, RadioTorchScreen.MAX_CHAN_LENGTH, unit.rtty);
            this.cmd[i] = this.field(this.leftPos + 121 + oX, this.topPos + 73 + oY + i * 36, 126 - oX * 2, 32, unit.command);

            this.active[i] = unit.active;
            this.polling[i] = unit.polling;
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

        for(int i = 0; i < KEYS; i++) {
            if(this.active[i]) guiGraphics.blit(TEXTURE, this.leftPos + 111, this.topPos + i * 36 + 54, 18, 204, 16, 16);
            if(this.polling[i]) guiGraphics.blit(TEXTURE, this.leftPos + 128, this.topPos + i * 36 + 53, 0, 204, 18, 18);
        }

        for(int i = 0; i < KEYS; i++) {
            this.color[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.label[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.rtty[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.cmd[i].render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int i = 0; i < KEYS; i++) {

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 111, i * 36 + 54, 16, 16)) {
                this.active[i] = !this.active[i];
                ScreenUtils.click(this.minecraft);
                return true;
            }

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 128, i * 36 + 53, 18, 18)) {
                this.polling[i] = !this.polling[i];
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

        for(int i = 0; i < KEYS; i++) {
            if(this.active[i]) activeBits |= (byte) (1 << i);
            if(this.polling[i]) pollingBits |= (byte) (1 << i);
        }

        tag.putByte("active", activeBits);
        tag.putByte("polling", pollingBits);

        for(int i = 0; i < KEYS; i++) {
            /*
             * Eine unleserliche Farbe bleibt weg -- und die Tafel liest das fehlende Feld als 0,
             * die Taste wird also schwarz statt ihre Farbe zu behalten. So ist es im Original,
             * und so bleibt es hier.
             */
            try { tag.putInt("color" + i, Integer.parseInt(this.color[i].getValue().trim(), 16)); } catch(NumberFormatException ignored) { }
            tag.putString("label" + i, this.label[i].getValue());
            tag.putString("rtty" + i, this.rtty[i].getValue());
            tag.putString("cmd" + i, this.cmd[i].getValue());
        }

        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.keypad.getBlockPos()));
    }
}
