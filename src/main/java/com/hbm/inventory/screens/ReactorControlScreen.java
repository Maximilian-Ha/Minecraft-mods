package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.ReactorControlBlockEntity;
import com.hbm.blockentity.machine.ReactorControlBlockEntity.RodFunction;
import com.hbm.inventory.menus.ReactorControlMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIReactorControl.
 *
 * Vier Eingabefelder -- obere und untere Stabstellung in Prozent, obere und untere Temperatur in
 * Fuenfzigerschritten --, drei Knoepfe fuer die Kennlinie und ein Knopf zum Uebernehmen.
 *
 * ABWEICHUNG: die drei Siebensegmentanzeigen des Originals stehen hier als Text.
 */
public class ReactorControlScreen extends InfoScreen<ReactorControlMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_reactor_control.png");

    /** Der Faktor zwischen Anzeige und gespeicherter Temperatur. */
    private static final int HEAT_STEP = 50;

    private final ReactorControlBlockEntity control;
    private final EditBox[] fields = new EditBox[4];

    public ReactorControlScreen(ReactorControlMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.control = menu.be;
    }

    @Override
    protected void init() {
        super.init();

        for(int i = 0; i < 2; i++) {
            this.fields[i] = this.makeField(this.leftPos + 35 + 30 * i, this.topPos + 38, 3);
            this.fields[i + 2] = this.makeField(this.leftPos + 35 + 30 * i, this.topPos + 49, 4);
        }

        this.fields[0].setValue(String.valueOf((int) this.control.levelUpper));
        this.fields[1].setValue(String.valueOf((int) this.control.levelLower));
        this.fields[2].setValue(String.valueOf((int) this.control.heatUpper / HEAT_STEP));
        this.fields[3].setValue(String.valueOf((int) this.control.heatLower / HEAT_STEP));
    }

    private EditBox makeField(int x, int y, int length) {
        EditBox box = new EditBox(this.font, x, y, 26, 8, Component.empty());
        box.setTextColor(0x08FF00);
        box.setTextColorUneditable(0x08FF00);
        box.setBordered(false);
        box.setMaxLength(length);
        this.addRenderableWidget(box);
        return box;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        int[] data = this.control.getDisplayData();

        guiGraphics.drawString(this.font, data[0] + "%", 6, 20, 0x08FF00, false);
        guiGraphics.drawString(this.font, String.valueOf(data[1]), 66, 20, 0x08FF00, false);
        guiGraphics.drawString(this.font, data[2] + "K", 126, 20, 0x08FF00, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Der gewaehlte Knopf steht eingedrueckt. */
        int selected = this.control.function.ordinal();
        guiGraphics.blit(TEXTURE, this.leftPos + 7, this.topPos + 37 + selected * 11, 176, 0, 22, 10);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(EditBox field : this.fields) {
            if(field.mouseClicked(mouseX, mouseY, button)) return true;
        }

        /* Der Uebernehmen-Knopf. */
        if(this.isHovered(mouseX, mouseY, 33, 59, 58, 10)) {
            this.sendBounds();
            return true;
        }

        /* Die drei Kennlinien. */
        for(int i = 0; i < RodFunction.values().length; i++) {
            if(this.isHovered(mouseX, mouseY, 7, 37 + i * 11, 22, 10)) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("function", i);
                PacketDistributor.sendToServer(new CompoundTagControl(tag, this.control.getBlockPos()));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for(EditBox field : this.fields) {
            if(field.isFocused() && field.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for(EditBox field : this.fields) {
            if(field.isFocused() && field.charTyped(codePoint, modifiers)) return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void sendBounds() {

        double[] values = new double[4];

        for(int i = 0; i < 4; i++) {

            /* Die beiden oberen Felder sind Prozente, die beiden unteren Temperaturstufen. */
            int max = i < 2 ? 100 : 1000;
            int step = i < 2 ? 1 : HEAT_STEP;

            if(!NumberUtils.isParsable(this.fields[i].getValue())) {
                this.fields[i].setValue("0");
                continue;
            }

            int value = Mth.clamp((int) NumberUtils.toDouble(this.fields[i].getValue()), 0, max);
            this.fields[i].setValue(String.valueOf(value));
            values[i] = value * step;
        }

        CompoundTag tag = new CompoundTag();
        tag.putDouble("levelUpper", values[0]);
        tag.putDouble("levelLower", values[1]);
        tag.putDouble("heatUpper", values[2]);
        tag.putDouble("heatLower", values[3]);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.control.getBlockPos()));
    }
}
