package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineFunnelBlockEntity;
import com.hbm.inventory.menus.MachineFunnelMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFunnel.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Der einzige Schalter der Maschine sitzt rechts unten: er blaettert durch die drei
 * Betriebsarten, und der Hinweis nennt die gerade eingestellte beim Namen.
 */
public class MachineFunnelScreen extends InfoScreen<MachineFunnelMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_funnel.png");

    private static final int BUTTON_X = 159;
    private static final int BUTTON_Y = 73;

    private final MachineFunnelBlockEntity be;

    public MachineFunnelScreen(MachineFunnelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 168;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        String key = switch(this.be.mode) {
            case MachineFunnelBlockEntity.MODE_3x3 -> "funnel.mode.3x3";
            case MachineFunnelBlockEntity.MODE_2x2 -> "funnel.mode.2x2";
            default -> "funnel.mode.all";
        };

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + BUTTON_X, this.topPos + BUTTON_Y, 10, 10, mouseX, mouseY,
                Component.translatable("funnel.mode", Component.translatable(key)));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, BUTTON_X, BUTTON_Y, 10, 10)) {
            this.click();
            CompoundTag data = new CompoundTag();
            data.putBoolean("toggle", true);
            PacketDistributor.sendToServer(new CompoundTagControl(data, this.be.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        guiGraphics.blit(TEXTURE, this.leftPos + BUTTON_X, this.topPos + BUTTON_Y, 176, this.be.mode * 10, 10, 10, 256, 256);
    }
}
