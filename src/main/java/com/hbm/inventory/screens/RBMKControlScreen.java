package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKControlBlockEntity;
import com.hbm.inventory.menus.RBMKControlMenu;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKControl.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Abweichung vom Original: die Farbgruppen der Steuerstaebe gehoeren zum Reaktorpult und kommen
 * mit diesem in einer spaeteren Runde, ebenso die Stromanzeige der ReaSim-Bauform.
 */
public class RBMKControlScreen extends InfoScreen<RBMKControlMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_control.png");

    private final RBMKControlBlockEntity rod;

    public RBMKControlScreen(RBMKControlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.rod = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 71, this.topPos + 29, 16, 56, mouseX, mouseY,
                Component.literal((int) (this.rod.rodLevel * 100) + "%"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int k = 0; k < 5; k++) {

            if(this.isHovered(mouseX, mouseY, 118, 26 + k * 11, 30, 11)) {

                if(this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
                }

                CompoundTag tag = new CompoundTag();
                tag.putDouble("level", 1.0D - (k * 0.25D));
                PacketDistributor.sendToServer(new CompoundTagControl(tag, this.rod.getBlockPos()));
            }
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

        int height = (int) (56 * (1D - this.rod.rodLevel));

        if(height > 0) guiGraphics.blit(TEXTURE, this.leftPos + 75, this.topPos + 29, 176, 56 - height, 8, height, 256, 256);
    }
}
