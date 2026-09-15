package com.hbm.inventory.screens;

import com.hbm.inventory.menus.FilterMenuBase;
import com.hbm.module.ModulePatternMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Gemeinsamer Unterbau der Bildschirme mit Musterfiltern. Portiert aus den drawScreen-Methoden
 * von GUICraneExtractor, GUICraneGrabber und GUICraneRouter, die dort dieselben sind.
 *
 * ER ZEICHNET DEN HINWEIS AM MUSTERFACH: faehrt man mit leerer Hand darueber, steht da, WIE
 * dieses Fach vergleicht und dass ein Rechtsklick weiterschaltet. Ohne den Hinweis waere die
 * Vergleichsart unsichtbar -- man saehe nur den Gegenstand, nicht die Regel dahinter.
 *
 * GEFRAGT WIRD NACH DER ART, NICHT NACH DEM VERGLEICHER. Auszieher und Greifer haben einen,
 * der Verteiler sechs; welcher davon fuer ein Fach zustaendig ist, weiss nur die Maschine.
 *
 * DIE REIHENFOLGE STEHT HIER FEST, und das mit Absicht: erst der Bildschirm, dann der
 * gewoehnliche Gegenstands-Hinweis, dann die eigenen der Maschine, zuletzt der Muster-Hinweis.
 * Der Muster-Hinweis muss ueber dem Gegenstands-Hinweis liegen, sonst verdeckte der Name des
 * Musters die Regel, um die es geht. So haelt es auch das Original.
 */
public abstract class FilterScreen<T extends FilterMenuBase<?>> extends InfoScreen<T> {

    public FilterScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    /** Die eingestellte Vergleichsart dieses Musterfaches, oder null fuer ein leeres Fach. */
    protected abstract @Nullable String modeAt(int slot);

    /** Platz fuer die Hinweise, die nur diese eine Maschine hat -- etwa ihre Umschalter. */
    protected void renderOwnTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) { }

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderOwnTooltips(guiGraphics, mouseX, mouseY);
        this.renderPatternTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderPatternTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        if(!this.menu.getCarried().isEmpty()) return;

        for(int i = 0; i < this.menu.patternSlotCount() && i < this.menu.slots.size(); i++) {

            String mode = this.modeAt(i);
            if(mode == null) continue;

            Slot slot = this.menu.getSlot(i);
            if(!this.isHovered(mouseX, mouseY, slot.x, slot.y, 16, 16)) continue;

            /* Dreissig Pixel hoeher als der Zeiger, damit der Hinweis nicht das Fach verdeckt. */
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("patternMatcher.rightClick").withStyle(ChatFormatting.RED),
                    ModulePatternMatcher.getLabel(mode)), mouseX, mouseY - 30);
            return;
        }
    }
}
