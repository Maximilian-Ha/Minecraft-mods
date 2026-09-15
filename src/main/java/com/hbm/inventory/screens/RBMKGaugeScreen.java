package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKGaugeBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKGaugeBlockEntity.GaugeUnit;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRBMKGauge.
 *
 * Die Einstelloberflaeche der Zeigertafel: je Instrument Farbe, Beschriftung, Kanal sowie
 * Kleinst- und Groesstwert, dazu zwei Schalter fuer sichtbar und Abfragebetrieb. Uebernommen
 * wird alles erst beim Klick auf den Knopf oben rechts -- so wie im Original.
 *
 * Alle Koordinaten unveraendert.
 */
public class RBMKGaugeScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rbmk_gauge.png");

    private static final int GAUGES = RBMKGaugeBlockEntity.GAUGES;

    private final RBMKGaugeBlockEntity gauge;

    private final int imageWidth = 256;
    private final int imageHeight = 204;
    private int leftPos;
    private int topPos;

    private final EditBox[] color = new EditBox[GAUGES];
    private final EditBox[] label = new EditBox[GAUGES];
    private final EditBox[] rtty = new EditBox[GAUGES];
    private final EditBox[] min = new EditBox[GAUGES];
    private final EditBox[] max = new EditBox[GAUGES];
    private final boolean[] active = new boolean[GAUGES];
    private final boolean[] polling = new boolean[GAUGES];

    public RBMKGaugeScreen(RBMKGaugeBlockEntity gauge) {
        super(Component.translatable("container.rbmkGauge"));
        this.gauge = gauge;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int oX = 4;
        int oY = 4;

        for(int i = 0; i < GAUGES; i++) {

            GaugeUnit unit = this.gauge.gauges[i];

            this.color[i] = this.field(this.leftPos + 27 + oX, this.topPos + 55 + oY + i * 36, 72 - oX * 2, 6, String.format("%06x", unit.color));
            this.label[i] = this.field(this.leftPos + 175 + oX, this.topPos + 55 + oY + i * 36, 72 - oX * 2, 15, unit.label);
            this.rtty[i] = this.field(this.leftPos + 27 + oX, this.topPos + 73 + oY + i * 36, 72 - oX * 2, RadioTorchScreen.MAX_CHAN_LENGTH, unit.rtty);
            this.min[i] = this.field(this.leftPos + 121 + oX, this.topPos + 73 + oY + i * 36, 52 - oX * 2, 32, String.valueOf(unit.min));
            this.max[i] = this.field(this.leftPos + 195 + oX, this.topPos + 73 + oY + i * 36, 52 - oX * 2, 32, String.valueOf(unit.max));

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

        for(int i = 0; i < GAUGES; i++) {
            if(this.active[i]) guiGraphics.blit(TEXTURE, this.leftPos + 111, this.topPos + i * 36 + 54, 18, 204, 16, 16);
            if(this.polling[i]) guiGraphics.blit(TEXTURE, this.leftPos + 128, this.topPos + i * 36 + 53, 0, 204, 18, 18);
        }

        for(int i = 0; i < GAUGES; i++) {
            this.color[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.label[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.rtty[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.min[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.max[i].render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int i = 0; i < GAUGES; i++) {

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

        for(int i = 0; i < GAUGES; i++) {
            if(this.active[i]) activeBits |= (byte) (1 << i);
            if(this.polling[i]) pollingBits |= (byte) (1 << i);
        }

        tag.putByte("active", activeBits);
        tag.putByte("polling", pollingBits);

        for(int i = 0; i < GAUGES; i++) {
            /* Unleserliche Eingaben bleiben einfach weg -- dann behaelt die Tafel den alten Wert. */
            try { tag.putInt("color" + i, Integer.parseInt(this.color[i].getValue().trim(), 16)); } catch(NumberFormatException ignored) { }
            tag.putString("label" + i, this.label[i].getValue());
            tag.putString("rtty" + i, this.rtty[i].getValue());
            try { tag.putInt("min" + i, Integer.parseInt(this.min[i].getValue().trim())); } catch(NumberFormatException ignored) { }
            try { tag.putInt("max" + i, Integer.parseInt(this.max[i].getValue().trim())); } catch(NumberFormatException ignored) { }
        }

        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.gauge.getBlockPos()));
    }
}
