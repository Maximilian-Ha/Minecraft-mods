package com.hbm.inventory.screens;

import com.hbm.blockentity.network.CraneBoxerBlockEntity;
import com.hbm.inventory.menus.CraneBoxerMenu;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICraneBoxer.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 *
 * Der Schalter bei 151,34 zeigt die Betriebsart als Bild an: die vier Bilder liegen in der
 * Textur untereinander, jeweils achtzehn Pixel hoch.
 */
public class CraneBoxerScreen extends InfoScreen<CraneBoxerMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/storage/gui_crane_boxer.png");

    public CraneBoxerScreen(CraneBoxerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 185;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if(this.isHovered(mouseX, mouseY, 151, 34, 18, 18)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable(modeKey(this.menu.be.mode)).withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private static String modeKey(byte mode) {
        return switch(mode) {
            case CraneBoxerBlockEntity.MODE_8 -> "craneBoxer.mode8";
            case CraneBoxerBlockEntity.MODE_16 -> "craneBoxer.mode16";
            case CraneBoxerBlockEntity.MODE_REDSTONE -> "craneBoxer.modeRedstone";
            default -> "craneBoxer.mode4";
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 151, 34, 18, 18)) {
            this.click();
            CompoundTag data = new CompoundTag();
            data.putBoolean("toggle", true);
            PacketDistributor.sendToServer(new CompoundTagControl(data, this.menu.be.getBlockPos()));
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
        guiGraphics.blit(TEXTURE, this.leftPos + 151, this.topPos + 34, 176, this.menu.be.mode * 18, 18, 18, 256, 256);
    }
}
