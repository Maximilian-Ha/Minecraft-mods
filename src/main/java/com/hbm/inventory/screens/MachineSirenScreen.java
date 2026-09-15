package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineSirenBlockEntity;
import com.hbm.inventory.menus.MachineSirenMenu;
import com.hbm.items.machine.CassetteItem.TrackType;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineSiren.
 *
 * Ein Fach und der Name der eingelegten Spur daneben.
 */
public class MachineSirenScreen extends AbstractContainerScreen<MachineSirenMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_siren.png");

    private final MachineSirenBlockEntity be;

    public MachineSirenScreen(MachineSirenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        TrackType track = this.be.getCurrentTrack();

        if(track != TrackType.NULL) {
            guiGraphics.drawString(this.font, Component.literal(track.title), 30, 39, track.color, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("container.siren.empty").withStyle(ChatFormatting.DARK_GRAY), 30, 39, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
