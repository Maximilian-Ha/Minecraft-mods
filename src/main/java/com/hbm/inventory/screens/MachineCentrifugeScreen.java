package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineCentrifugeBlockEntity;
import com.hbm.inventory.menus.MachineCentrifugeMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineCentrifuge.
 *
 * Runde 159: die Masse standen bis hierher alle falsch. Die Oberflaeche ist 182x189 gross,
 * nicht 176x186, und die Fuellbalken liegen im Atlas bei u = 182, nicht bei 176 -- der Port
 * hatte ueberall die gewoehnlichen Werte eingesetzt. Sichtbar war das als abgeschnittener
 * rechter und unterer Rand, als Schaechte, die sieben Pixel ueber ihren gemalten Rahmen
 * sassen, und als Balken, die sechs Pixel neben ihrer Mulde liefen.
 */
public class MachineCentrifugeScreen extends InfoScreen<MachineCentrifugeMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_centrifuge.png");

    private final MachineCentrifugeBlockEntity be;

    public MachineCentrifugeScreen(MachineCentrifugeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 182;
        this.imageHeight = 189;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 37, this.be.getPower(), this.be.getMaxPower());
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 160, this.topPos + 16, 8, 8, mouseX, mouseY, this.getUpgradeInfo());

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /* Die Zentrifuge nimmt Aufwertungen und sagte es bisher nirgends: die beiden Schaechte
     * waren da, die Anzeige dazu fehlte. */
    private List<Component> getUpgradeInfo() {
        return upgradeInfo(this.be, this.be, 6, 7);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 + 18 - this.font.width(this.title) / 2, 6, 0xffffff, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 11, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.be.hasPower()) {
            int power = this.be.getPowerRemainingScaled(37);
            guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 55 - power, 182, 37 - power, 16, power);
        }

        if(this.be.isProcessing()) {
            int progress = this.be.getCentrifugeProgressScaled(145);
            for(int i = 0; i < 4; i++) {
                int piece = Math.min(progress, 36);
                if(piece > 0) {
                    guiGraphics.blit(TEXTURE, this.leftPos + 72 + i * 20, this.topPos + 57 - piece, 182, 73 - piece, 12, piece);
                }
                progress -= piece;
            }
        }

        this.drawInfoPanel(guiGraphics, this.leftPos + 160, this.topPos + 16, 8);
    }
}
