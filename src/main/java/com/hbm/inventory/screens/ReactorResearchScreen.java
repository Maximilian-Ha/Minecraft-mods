package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.ReactorResearchBlockEntity;
import com.hbm.inventory.menus.ReactorResearchMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.ChatFormatting;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIReactorResearch.
 *
 * Links die beiden Anzeigen -- Fluss und Temperatur --, in der Mitte das Becken mit seinen zwoelf
 * Faechern, rechts das Eingabefeld fuer die Stabstellung in Prozent.
 *
 * Die kleinen Marken zwischen den Faechern zeigt das Original nur, solange die Staebe zu mehr als
 * der Haelfte drinstecken -- sie stellen die Staebe selbst dar.
 *
 * ABWEICHUNG: das Original hat einen eigenen Siebensegment-Zeichner (NumberDisplay). Den gibt es
 * im Port nicht; die Zahlen stehen hier als gewoehnlicher Text an derselben Stelle.
 */
public class ReactorResearchScreen extends InfoScreen<ReactorResearchMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_research_reactor.png");

    private final ReactorResearchBlockEntity reactor;
    private EditBox field;
    /** Zaehlt herunter, solange der Hebel nach dem Absenden umgelegt gezeigt wird. */
    private int leverTimer;

    public ReactorResearchScreen(ReactorResearchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.reactor = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelY = this.imageHeight - 94;

        this.field = new EditBox(this.font, this.leftPos + 20, this.topPos + 99, 22, 10, Component.empty());
        this.field.setTextColor(0x08FF00);
        this.field.setTextColorUneditable(0x08FF00);
        this.field.setBordered(false);
        this.field.setMaxLength(3);
        this.field.setValue(String.valueOf((int) Math.round(this.reactor.targetLevel * 100D)));
        this.addRenderableWidget(this.field);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        /* Fluss und Temperatur, an der Stelle der Siebensegmentanzeigen des Originals. */
        guiGraphics.drawString(this.font, Component.literal(String.valueOf(this.reactor.totalFlux))
                .withStyle(ChatFormatting.GREEN), 6, 20, 0x08FF00, false);
        guiGraphics.drawString(this.font, Component.literal(this.reactor.getDisplayHeat() + "K")
                .withStyle(ChatFormatting.GREEN), 6, 58, 0x08FF00, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Die Steuerstaebe stecken drin: neun plus vier Marken zwischen den Faechern. */
        if(this.reactor.rodLevel <= 0.5D) {
            for(int x = 0; x < 3; x++) {
                for(int y = 0; y < 3; y++) {
                    guiGraphics.blit(TEXTURE, this.leftPos + 81 + 36 * x, this.topPos + 26 + 36 * y, 176, 0, 8, 8);
                }
            }
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    guiGraphics.blit(TEXTURE, this.leftPos + 99 + 36 * x, this.topPos + 44 + 36 * y, 176, 0, 8, 8);
                }
            }
        }

        if(this.leverTimer > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 44, this.topPos + 97, 176, 8, 11, 20);
            this.leverTimer--;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.field.mouseClicked(mouseX, mouseY, button)) return true;

        /* Der Hebel neben dem Eingabefeld sendet die Stellung ab. */
        if(this.isHovered(mouseX, mouseY, 44, 97, 11, 20)) {
            this.sendLevel();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.field.isFocused() && this.field.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.field.isFocused() && this.field.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    private void sendLevel() {

        if(!NumberUtils.isParsable(this.field.getValue())) {
            this.field.setValue("0");
            return;
        }

        int percent = Mth.clamp((int) NumberUtils.toDouble(this.field.getValue()), 0, 100);
        this.field.setValue(String.valueOf(percent));

        CompoundTag tag = new CompoundTag();
        tag.putDouble("level", percent * 0.01D);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.reactor.getBlockPos()));

        this.leverTimer = 15;
    }
}
