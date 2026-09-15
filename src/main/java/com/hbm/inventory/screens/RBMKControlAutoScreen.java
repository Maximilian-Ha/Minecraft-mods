package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKControlAutoBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKControlAutoBlockEntity.RBMKFunction;
import com.hbm.inventory.menus.RBMKControlAutoMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKControlAuto.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Die vier Eingabefelder stehen fuer Stand bei Hoechsttemperatur, Stand bei Mindesttemperatur,
 * Hoechsttemperatur und Mindesttemperatur -- in dieser Reihenfolge, wie im Original.
 */
public class RBMKControlAutoScreen extends InfoScreen<RBMKControlAutoMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_control_auto.png");

    private final RBMKControlAutoBlockEntity rod;
    private final EditBox[] fields = new EditBox[4];

    public RBMKControlAutoScreen(RBMKControlAutoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.rod = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();

        for(int i = 0; i < 4; i++) {
            EditBox field = new EditBox(this.font, this.leftPos + 30, this.topPos + 27 + 11 * i, 26, 8, Component.empty());
            field.setTextColor(0xFFFFFF);
            field.setTextColorUneditable(0xFFFFFF);
            field.setBordered(false);
            field.setMaxLength(i < 2 ? 3 : 4);
            this.fields[i] = field;
            this.addRenderableWidget(field);
        }

        this.fields[0].setValue(String.valueOf((int) this.rod.levelUpper));
        this.fields[1].setValue(String.valueOf((int) this.rod.levelLower));
        this.fields[2].setValue(String.valueOf((int) this.rod.heatUpper));
        this.fields[3].setValue(String.valueOf((int) this.rod.heatLower));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 124, this.topPos + 29, 16, 56, mouseX, mouseY,
                Component.literal((int) (this.rod.rodLevel * 100) + "%"));

        String func = switch(this.rod.function) {
            case LINEAR -> "Linear";
            case QUAD_UP -> "Quadratic";
            case QUAD_DOWN -> "Inverse Quadratic";
        };

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 58, this.topPos + 26, 28, 19, mouseX, mouseY, Component.literal("Function: " + func));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 61, this.topPos + 48, 22, 10, mouseX, mouseY, Component.literal("Select linear interpolation"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 61, this.topPos + 59, 22, 10, mouseX, mouseY, Component.literal("Select quadratic interpolation"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 61, this.topPos + 70, 22, 10, mouseX, mouseY, Component.literal("Select inverse quadratic interpolation"));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 28, this.topPos + 26, 30, 10, mouseX, mouseY,
                Component.literal("Level at max heat"), Component.literal("Should be smaller than level at min heat"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 28, this.topPos + 37, 30, 10, mouseX, mouseY,
                Component.literal("Level at min heat"), Component.literal("Should be larger than level at max heat"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 28, this.topPos + 48, 30, 10, mouseX, mouseY,
                Component.literal("Max heat"), Component.literal("Must be larger than min heat"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 28, this.topPos + 59, 30, 10, mouseX, mouseY,
                Component.literal("Min heat"), Component.literal("Must be smaller than max heat"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 28, this.topPos + 70, 30, 10, mouseX, mouseY,
                Component.literal("Save parameters"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 28, 70, 30, 10)) {
            this.playClick();
            this.sendValues();
        }

        for(int i = 0; i < 3; i++) {
            if(this.isHovered(mouseX, mouseY, 61, 48 + i * 11, 22, 10)) {
                this.playClick();
                CompoundTag tag = new CompoundTag();
                tag.putInt("function", i);
                PacketDistributor.sendToServer(new CompoundTagControl(tag, this.rod.getBlockPos()));
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Liest die vier Felder, klemmt sie und schickt sie an den Stab. */
    private void sendValues() {

        double[] values = new double[4];

        for(int i = 0; i < 4; i++) {

            double clamp = i < 2 ? 100 : 9999;
            int value = 0;

            try {
                value = (int) Mth.clamp(Double.parseDouble(this.fields[i].getValue()), 0, clamp);
            } catch(NumberFormatException ignored) {
                // Unleserliches Feld wird wie im Original auf null gesetzt.
            }

            this.fields[i].setValue(String.valueOf(value));
            values[i] = value;
        }

        CompoundTag tag = new CompoundTag();
        tag.putDouble("levelUpper", values[0]);
        tag.putDouble("levelLower", values[1]);
        tag.putDouble("heatUpper", values[2]);
        tag.putDouble("heatLower", values[3]);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.rod.getBlockPos()));
    }

    private void playClick() {
        if(this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int height = (int) (56 * (1D - this.rod.rodLevel));
        if(height > 0) guiGraphics.blit(TEXTURE, this.leftPos + 128, this.topPos + 29, 176, 56 - height, 8, height, 256, 256);

        int func = this.rod.function == RBMKFunction.LINEAR ? 0 : this.rod.function == RBMKFunction.QUAD_UP ? 1 : 2;
        guiGraphics.blit(TEXTURE, this.leftPos + 61, this.topPos + 48 + func * 11, 184, 0, 22, 10, 256, 256);
    }
}
