package com.hbm.inventory.screens;

import com.hbm.inventory.menus.WeaponTableMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIWeaponTable.
 *
 * Links die Waffe, rechts die sieben Aufsatzplaetze. Dazwischen sitzt der Umschalter: bei einer
 * beidhaendigen Waffe waehlt er, welcher der beiden Empfaenger gerade auf dem Tisch liegt. Bei
 * allen anderen Waffen tut er nichts, weil es nur einen Empfaenger gibt.
 *
 * ABWEICHUNG: das Original zeigt im Tisch die Waffe selbst, in der Ausfuehrung des gewaehlten
 * Empfaengers. Der Port hat dafuer keine Entsprechung -- der Tisch zeigt das Gegenstandsbild.
 */
public class WeaponTableScreen extends InfoScreen<WeaponTableMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_weapon_modifier.png");

    private static final int SWITCH_X = 35;
    private static final int SWITCH_Y = 112;
    private static final int SWITCH_WIDTH = 6;
    private static final int SWITCH_HEIGHT = 8;

    public WeaponTableScreen(WeaponTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 240;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Die Anzeige rechts neben der Waffe sagt, welcher Empfaenger gerade gemeint ist. */
        guiGraphics.blit(TEXTURE, this.leftPos + SWITCH_X, this.topPos + SWITCH_Y,
                176 + SWITCH_WIDTH * this.menu.index, 0, SWITCH_WIDTH, SWITCH_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int configs = this.menu.getConfigCount();

        if(configs > 1 && this.minecraft != null && this.minecraft.gameMode != null) {

            double x = mouseX - this.leftPos - SWITCH_X;
            double y = mouseY - this.topPos - SWITCH_Y;

            if(x >= 0 && x < SWITCH_WIDTH && y >= 0 && y < SWITCH_HEIGHT) {
                int next = (this.menu.index + 1) % configs;
                this.menu.index = next;
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, next);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
