package com.hbm.inventory.screens;

import com.hbm.blockentity.network.RadioTorchCounterBlockEntity;
import com.hbm.inventory.menus.RadioTorchCounterMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICounterTorch.
 *
 * Drei Zeilen zu je einem Musterfach und einem Kanalfeld, dazu zwei Knoepfe rechts oben: der
 * obere schaltet zwischen "nur bei Aenderung" und "jeden Tick", der untere schickt die drei
 * Kanalnamen zum Block.
 *
 * Die Hinweise an den Musterfaechern kommen aus FilterScreen -- sie sind bei allen
 * Musterfiltern des Ports dieselben.
 */
public class RadioTorchCounterScreen extends FilterScreen<RadioTorchCounterMenu> {

    private static final ResourceLocation TEXTURE =
            NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_rtty_counter.png");

    /** Der Umschalter fuer die Betriebsart, Masse aus dem Original. */
    private static final int KNOPF_X = 193;
    private static final int KNOPF_ART_Y = 8;
    private static final int KNOPF_SPEICHERN_Y = 30;
    private static final int KNOPF_GROESSE = 18;

    private final RadioTorchCounterBlockEntity counter;

    private EditBox[] kanal;

    public RadioTorchCounterScreen(RadioTorchCounterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.counter = menu.be;
        this.imageWidth = 218;
        this.imageHeight = 238;
    }

    @Override
    protected void init() {
        super.init();

        this.kanal = new EditBox[RadioTorchCounterBlockEntity.KANAELE];

        for(int i = 0; i < this.kanal.length; i++) {
            EditBox feld = new EditBox(this.font, this.leftPos + 29, this.topPos + 21 + 44 * i, 86, 14, Component.empty());
            feld.setTextColor(0x00ff00);
            feld.setTextColorUneditable(0x00ff00);
            feld.setBordered(false);
            feld.setMaxLength(RadioTorchScreen.MAX_CHAN_LENGTH);
            feld.setValue(this.counter.channel[i] == null ? "" : this.counter.channel[i]);
            this.addRenderableWidget(feld);
            this.kanal[i] = feld;
        }
    }

    @Override
    protected @Nullable String modeAt(int slot) {
        return this.counter.matcher.modes[slot];
    }

    @Override
    protected void renderOwnTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        if(this.isHovered(mouseX, mouseY, KNOPF_X, KNOPF_ART_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable(this.counter.polling ? "rtty.polling" : "rtty.stateChange")), mouseX, mouseY);
        }

        if(this.isHovered(mouseX, mouseY, KNOPF_X, KNOPF_SPEICHERN_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("rtty.saveSettings")), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, KNOPF_X, KNOPF_ART_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("polling", true);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.counter.getBlockPos()));
            return true;
        }

        if(this.isHovered(mouseX, mouseY, KNOPF_X, KNOPF_SPEICHERN_Y, KNOPF_GROESSE, KNOPF_GROESSE)) {
            ScreenUtils.click(this.minecraft);
            CompoundTag tag = new CompoundTag();
            for(int i = 0; i < this.kanal.length; i++) tag.putString("c" + i, this.kanal[i].getValue());
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.counter.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Der Umschalter leuchtet, solange jeden Tick gefunkt wird.
        if(this.counter.polling) {
            guiGraphics.blit(TEXTURE, this.leftPos + KNOPF_X, this.topPos + KNOPF_ART_Y, 218, 0, KNOPF_GROESSE, KNOPF_GROESSE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component name = this.title;
        guiGraphics.drawString(this.font, name, 184 / 2 - this.font.width(name) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 16, this.imageHeight - 96 + 2, 4210752, false);
    }
}
