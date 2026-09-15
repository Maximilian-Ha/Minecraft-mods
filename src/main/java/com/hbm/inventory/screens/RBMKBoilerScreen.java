package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKBoilerBlockEntity;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.menus.RBMKBoilerMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKBoiler.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class RBMKBoilerScreen extends InfoScreen<RBMKBoilerMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_boiler.png");

    private final RBMKBoilerBlockEntity boiler;

    public RBMKBoilerScreen(RBMKBoilerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.boiler = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.boiler.feed.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 126, this.topPos + 24, 16, 56);
        this.boiler.steam.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 89, this.topPos + 39, 8, 28);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 33, 21, 20, 64)) {

            if(this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
            }

            CompoundTag tag = new CompoundTag();
            tag.putBoolean("compression", true);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.boiler.getBlockPos()));
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

        int i = this.boiler.feed.getFill() * 58 / this.boiler.feed.getMaxFill();
        guiGraphics.blit(TEXTURE, this.leftPos + 126, this.topPos + 82 - i, 176, 58 - i, 14, i, 256, 256);

        int j = this.boiler.steam.getFill() * 22 / this.boiler.steam.getMaxFill();
        if(j > 0) j++;
        if(j > 22) j++;
        guiGraphics.blit(TEXTURE, this.leftPos + 91, this.topPos + 65 - j, 190, 24 - j, 4, j, 256, 256);

        FluidType type = this.boiler.steam.getTankType();
        if(type == Fluids.STEAM)            guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 24, 194, 0, 14, 58, 256, 256);
        if(type == Fluids.HOTSTEAM)         guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 24, 208, 0, 14, 58, 256, 256);
        if(type == Fluids.SUPERHOTSTEAM)    guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 24, 222, 0, 14, 58, 256, 256);
        if(type == Fluids.ULTRAHOTSTEAM)    guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 24, 236, 0, 14, 58, 256, 256);
    }
}
