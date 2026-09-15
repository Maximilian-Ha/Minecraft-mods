package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachinePWRControllerBlockEntity;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.menus.MachinePWRMenu;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIPWR.
 *
 * Links die beiden Tanks, in der Mitte der Fortschritt und der eingefahrene Brennstoff, rechts
 * die beiden Waermeanzeigen fuer Kern und Huelle. Darunter der Steuerstabbalken und das Feld,
 * in das man die gewuenschte Stellung eintippt.
 *
 * Eine Feinheit aus dem Original: im Feld steht die EINGEFAHRENE Stellung, im Datenmodell die
 * AUSGEFAHRENE. Hundert minus hundert. Wer hundert eintippt, faehrt die Staebe ganz ein.
 *
 * Blit-Koordinaten unveraendert uebernommen.
 */
public class MachinePWRScreen extends InfoScreen<MachinePWRMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_pwr.png");

    private final MachinePWRControllerBlockEntity be;

    private EditBox field;

    public MachinePWRScreen(MachinePWRMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 188;
    }

    @Override
    protected void init() {
        super.init();

        this.field = new EditBox(this.font, this.leftPos + 57, this.topPos + 63, 30, 8, Component.empty());
        this.field.setTextColor(0x00ff00);
        this.field.setTextColorUneditable(0x008000);
        this.field.setBordered(false);
        this.field.setMaxLength(3);
        this.field.setValue(String.valueOf((int) (100 - this.be.rodTarget)));
        this.addRenderableWidget(this.field);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 115, this.topPos + 31, 18, 18, mouseX, mouseY,
                Component.literal("Core: " + String.format(Locale.US, "%,d", this.be.coreHeat) + " / " + String.format(Locale.US, "%,d", this.be.coreHeatCapacity) + " TU"));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 151, this.topPos + 31, 18, 18, mouseX, mouseY,
                Component.literal("Hull: " + String.format(Locale.US, "%,d", this.be.hullHeat) + " / " + String.format(Locale.US, "%,d", MachinePWRControllerBlockEntity.HULL_HEAT_CAPACITY_BASE) + " TU"));

        if(this.be.processTime > 0) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 52, this.topPos + 31, 36, 18, mouseX, mouseY,
                    Component.literal((int) (this.be.progress * 100 / this.be.processTime) + "%"));
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 52, this.topPos + 53, 54, 4, mouseX, mouseY,
                Component.literal("Control rod level: " + (100 - Math.round(this.be.rodLevel)) + "%"));

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 5, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 5, 16, 52);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);

        /* Der Flusswert rechts unten, etwas kleiner als die uebrige Schrift. */
        String flux = String.format(Locale.US, "%,.1f", this.be.flux);
        float scale = 1.25F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1F / scale, 1F / scale, 1F);
        guiGraphics.drawString(this.font, flux, (int) (165 * scale - this.font.width(flux)), (int) (64 * scale), 0x00ff00, false);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Ab vier Fuenfteln der Kapazitaet leuchtet die Warnung. */
        if(this.be.hullHeat > MachinePWRControllerBlockEntity.HULL_HEAT_CAPACITY_BASE * 0.8
                || this.be.coreHeat > this.be.coreHeatCapacity * 0.8) {
            guiGraphics.blit(TEXTURE, this.leftPos + 147, this.topPos, 176, 14, 26, 26);
        }

        if(this.be.processTime > 0) {
            int p = (int) (this.be.progress * 33 / this.be.processTime);
            guiGraphics.blit(TEXTURE, this.leftPos + 54, this.topPos + 33, 176, 0, p, 14);
        }

        int c = (int) (this.be.rodLevel * 52 / 100);
        guiGraphics.blit(TEXTURE, this.leftPos + 53, this.topPos + 54, 176, 40, c, 2);

        ScreenElements.drawSmoothGauge(this.leftPos + 124, this.topPos + 40, (float) ((double) this.be.coreHeat / (double) this.be.coreHeatCapacity), 5, 2, 1, 0xFF7F0000);
        ScreenElements.drawSmoothGauge(this.leftPos + 160, this.topPos + 40, (float) ((double) this.be.hullHeat / (double) MachinePWRControllerBlockEntity.HULL_HEAT_CAPACITY_BASE), 5, 2, 1, 0xFF7F0000);

        this.be.tanks[0].renderTank(this.leftPos + 8, this.topPos + 57, 1F, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 26, this.topPos + 57, 1F, 16, 52);

        if(this.be.typeLoaded != -1 && this.be.amountLoaded > 0) {
            ItemStack display = MetaHelper.newStack(NtmItems.PWR_FUEL.get(), 1, this.be.typeLoaded);
            guiGraphics.renderItem(display, this.leftPos + 89, this.topPos + 5);
            guiGraphics.renderItemDecorations(this.font, display, this.leftPos + 89, this.topPos + 5,
                    ChatFormatting.YELLOW + "" + this.be.amountLoaded + "/" + this.be.rodCount);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.sendRodTarget();
            return true;
        }

        if(this.field != null && this.field.isFocused()) {
            if(this.field.keyPressed(keyCode, scanCode, modifiers)) return true;
            if(keyCode != GLFW.GLFW_KEY_ESCAPE) return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** Schickt die eingetippte Stellung zurueck -- eingefahren im Feld, ausgefahren im Modell. */
    private void sendRodTarget() {

        int entered;
        try { entered = Integer.parseInt(this.field.getValue().trim()); } catch(NumberFormatException ignored) { return; }

        CompoundTag tag = new CompoundTag();
        tag.putDouble("rodTarget", 100 - Math.max(0, Math.min(100, entered)));

        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
    }
}
