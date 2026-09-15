package com.hbm.inventory.screens;

import com.hbm.inventory.menus.CraneExtractorMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICraneExtractor.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 *
 * NICHT UEBERNOMMEN: der Schalter "nur volle Stapel" bei 187,34 -- siehe die Begruendung in
 * CraneExtractorBlockEntity. Ohne die Stapelaufwertung koennte er nie etwas aendern.
 */
public class CraneExtractorScreen extends FilterScreen<CraneExtractorMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/storage/gui_crane_ejector.png");

    public CraneExtractorScreen(CraneExtractorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 212;
        this.imageHeight = 185;
    }

    @Override
    protected String modeAt(int slot) {
        return this.menu.be.matcher.modes[slot];
    }

    @Override
    protected void renderOwnTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        if(this.isHovered(mouseX, mouseY, 128, 30, 14, 26)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable(this.menu.be.isWhitelist ? "crane.whitelist" : "crane.blacklist")
                            .withStyle(this.menu.be.isWhitelist ? ChatFormatting.GREEN : ChatFormatting.RED)),
                    mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 128, 30, 14, 26)) {
            this.click();
            CompoundTag data = new CompoundTag();
            data.putBoolean("whitelist", true);
            PacketDistributor.sendToServer(new CompoundTagControl(data, this.menu.be.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 26, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        /* Der Zeiger am Umschalter: oben heisst Weissliste, unten Schwarzliste. */
        if(this.menu.be.isWhitelist) {
            guiGraphics.blit(TEXTURE, this.leftPos + 139, this.topPos + 33, 212, 18, 3, 6, 256, 256);
        } else {
            guiGraphics.blit(TEXTURE, this.leftPos + 139, this.topPos + 47, 212, 18, 3, 6, 256, 256);
        }
    }
}
