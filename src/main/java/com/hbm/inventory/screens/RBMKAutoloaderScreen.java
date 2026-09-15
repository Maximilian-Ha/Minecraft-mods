package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKAutoloaderBlockEntity;
import com.hbm.inventory.menus.RBMKAutoloaderMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKAutoloader.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class RBMKAutoloaderScreen extends InfoScreen<RBMKAutoloaderMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_autoloader.png");

    private final RBMKAutoloaderBlockEntity loader;

    public RBMKAutoloaderScreen(RBMKAutoloaderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.loader = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 182;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 74, 36, 12, 12)) this.sendCycle("minus");
        if(this.isHovered(mouseX, mouseY, 90, 36, 12, 12)) this.sendCycle("plus");

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendCycle(String key) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.loader.getBlockPos()));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);

        Component percent = Component.literal(this.loader.cycle + "%");
        guiGraphics.drawString(this.font, percent, this.imageWidth / 2 - this.font.width(percent) / 2, 23, 0x00FF00, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
