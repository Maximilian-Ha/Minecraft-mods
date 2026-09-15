package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKGraphBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKGraphBlockEntity.GraphUnit;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRBMKGraph.
 *
 * Je Schreiber vier Eingabefelder: Kanal, Beschriftung, Kleinst- und Groesstwert. Die beiden
 * Grenzfelder duerfen leer bleiben -- dann sucht sich der Schreiber die Grenze selbst.
 *
 * Alle Koordinaten unveraendert.
 */
public class RBMKGraphScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rbmk_graph.png");

    private static final int GRAPHS = RBMKGraphBlockEntity.GRAPHS;

    private final RBMKGraphBlockEntity graph;

    private final int imageWidth = 256;
    private final int imageHeight = 150;
    private int leftPos;
    private int topPos;

    private final EditBox[] label = new EditBox[GRAPHS];
    private final EditBox[] rtty = new EditBox[GRAPHS];
    private final EditBox[] min = new EditBox[GRAPHS];
    private final EditBox[] max = new EditBox[GRAPHS];
    private final boolean[] active = new boolean[GRAPHS];
    private final boolean[] polling = new boolean[GRAPHS];

    public RBMKGraphScreen(RBMKGraphBlockEntity graph) {
        super(Component.translatable("container.rbmkGraph"));
        this.graph = graph;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int oX = 4;
        int oY = 4;

        for(int i = 0; i < GRAPHS; i++) {

            GraphUnit unit = this.graph.graphs[i];

            this.label[i] = this.field(this.leftPos + 27 + oX, this.topPos + 73 + oY + i * 54, 72 - oX * 2, 30, unit.label);
            this.rtty[i] = this.field(this.leftPos + 27 + oX, this.topPos + 55 + oY + i * 54, 72 - oX * 2, RadioTorchScreen.MAX_CHAN_LENGTH, unit.rtty);
            this.min[i] = this.field(this.leftPos + 175 + oX, this.topPos + 55 + oY + i * 54, 72 - oX * 2, 15, unit.minBound ? String.valueOf(unit.min) : "");
            this.max[i] = this.field(this.leftPos + 175 + oX, this.topPos + 73 + oY + i * 54, 72 - oX * 2, 15, unit.maxBound ? String.valueOf(unit.max) : "");

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

        for(int i = 0; i < GRAPHS; i++) {
            if(this.active[i]) guiGraphics.blit(TEXTURE, this.leftPos + 111, this.topPos + i * 54 + 54, 18, 150, 16, 16);
            if(this.polling[i]) guiGraphics.blit(TEXTURE, this.leftPos + 128, this.topPos + i * 54 + 53, 0, 150, 18, 18);
        }

        for(int i = 0; i < GRAPHS; i++) {
            this.label[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.rtty[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.min[i].render(guiGraphics, mouseX, mouseY, partialTick);
            this.max[i].render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int i = 0; i < GRAPHS; i++) {

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 111, i * 54 + 54, 16, 16)) {
                this.active[i] = !this.active[i];
                ScreenUtils.click(this.minecraft);
                return true;
            }

            if(ScreenUtils.isHovered(this.leftPos, this.topPos, mouseX, mouseY, 128, i * 54 + 53, 18, 18)) {
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

        for(int i = 0; i < GRAPHS; i++) {
            if(this.active[i]) activeBits |= (byte) (1 << i);
            if(this.polling[i]) pollingBits |= (byte) (1 << i);
        }

        tag.putByte("active", activeBits);
        tag.putByte("polling", pollingBits);

        for(int i = 0; i < GRAPHS; i++) {
            tag.putString("label" + i, this.label[i].getValue());
            tag.putString("rtty" + i, this.rtty[i].getValue());
            /*
             * Ein leeres oder unleserliches Feld wird gar nicht erst mitgeschickt -- und genau das
             * liest die Tafel als "Grenze nicht festnageln".
             */
            putBound(tag, "min" + i, this.min[i].getValue());
            putBound(tag, "max" + i, this.max[i].getValue());
        }

        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.graph.getBlockPos()));
    }

    private static void putBound(CompoundTag tag, String key, String value) {
        if(value == null || value.trim().isEmpty()) return;
        try { tag.putLong(key, Long.parseLong(value.trim())); } catch(NumberFormatException ignored) { }
    }
}
