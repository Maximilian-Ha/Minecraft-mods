package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineRotaryFurnaceBlockEntity;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.menus.MachineRotaryFurnaceMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.awt.Color;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineRotaryFurnace.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * ABWEICHUNG: die Materialsaeule zeichnet der Port ueber die regulaere Mischung statt additiv --
 * GuiGraphics kennt keinen Wechsel der Mischfunktion.
 */
public class MachineRotaryFurnaceScreen extends InfoScreen<MachineRotaryFurnaceMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_rotary_furnace.png");

    private final MachineRotaryFurnaceBlockEntity furnace;

    public MachineRotaryFurnaceScreen(MachineRotaryFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.furnace = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.furnace.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 36, 52, 16);
        this.furnace.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 18, 16, 52);
        this.furnace.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 52);

        // Ueber dem leeren Brennstofffach steht, welche Brennstoffe der Ofen besonders mag
        Slot fuel = this.menu.slots.get(4);
        if(!fuel.hasItem() && this.isHovered(mouseX, mouseY, fuel.x, fuel.y, 16, 16)) {
            List<Component> bonuses = MachineRotaryFurnaceBlockEntity.burnModule.getDesc();
            if(!bonuses.isEmpty()) guiGraphics.renderComponentTooltip(this.font, bonuses, mouseX, mouseY);
        }

        if(this.furnace.output == null) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 98, this.topPos + 18, 16, 52, mouseX, mouseY,
                    Component.translatable("desc.gui.arcfurnace.empty").withStyle(ChatFormatting.RED));
        } else {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 98, this.topPos + 18, 16, 52, mouseX, mouseY,
                    this.furnace.output.material.getName()
                            .append(": " + Mats.formatAmount(this.furnace.output.amount, Screen.hasShiftDown()))
                            .withStyle(ChatFormatting.YELLOW));
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, (this.imageWidth - 54) / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int p = (int) Math.ceil(this.furnace.progress * 33);
        if(p > 0) guiGraphics.blit(TEXTURE, this.leftPos + 63, this.topPos + 30, 176, 0, p, 10, 256, 256);

        if(this.furnace.maxBurnTime > 0) {
            int b = this.furnace.burnTime * 14 / this.furnace.maxBurnTime;
            if(b > 0) guiGraphics.blit(TEXTURE, this.leftPos + 26, this.topPos + 69 - b, 176, 24 - b, 14, b, 256, 256);
        }

        if(this.furnace.output != null) {

            int amount = this.furnace.output.amount * 52 / MachineRotaryFurnaceBlockEntity.maxOutput;

            if(amount > 0) {
                Color color = new Color(this.furnace.output.material.moltenColor);
                guiGraphics.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
                guiGraphics.blit(TEXTURE, this.leftPos + 98, this.topPos + 70 - amount, 176, 76 - amount, 16, amount, 256, 256);
                guiGraphics.setColor(1F, 1F, 1F, 0.3F);
                guiGraphics.blit(TEXTURE, this.leftPos + 98, this.topPos + 70 - amount, 176, 76 - amount, 16, amount, 256, 256);
                guiGraphics.setColor(1F, 1F, 1F, 1F);
            }
        }

        this.furnace.tanks[0].renderTank(this.leftPos + 8, this.topPos + 52, 0F, 52, 16, 1);
        this.furnace.tanks[1].renderTank(this.leftPos + 134, this.topPos + 70, 0F, 16, 52);
        this.furnace.tanks[2].renderTank(this.leftPos + 152, this.topPos + 70, 0F, 16, 52);
    }
}
