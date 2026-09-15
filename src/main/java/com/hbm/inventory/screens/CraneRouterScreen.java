package com.hbm.inventory.screens;

import com.hbm.blockentity.network.CraneRouterBlockEntity;
import com.hbm.inventory.menus.CraneRouterMenu;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICraneRouter.
 * Alle Blit-Koordinaten sind unveraendert uebernommen.
 *
 * Neben jeder Musterreihe steht ihr Schalter: ein Klick darauf schaltet die Betriebsart dieser
 * Seite weiter, und der Hinweis nennt sie beim Namen.
 *
 * ABWEICHUNG: das Original zeigt am Ende noch die Fachnummern an, solange die linke Alt-Taste
 * gedrueckt ist -- eine Hilfe fuer den Entwickler, die im Spiel nichts zu suchen hat.
 */
public class CraneRouterScreen extends FilterScreen<CraneRouterMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/storage/gui_crane_router.png");

    public CraneRouterScreen(CraneRouterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 201;
    }

    @Override
    protected String modeAt(int slot) {
        return this.menu.be.patterns[slot / CraneRouterBlockEntity.PATTERNS_PER_SIDE]
                .modes[slot % CraneRouterBlockEntity.PATTERNS_PER_SIDE];
    }

    /** Die Schaltflaeche der Seite: links die Seiten null bis zwei, rechts drei bis fuenf. */
    private static int buttonX(int side) { return 7 + (side / 3) * 222; }
    private static int buttonY(int side) { return 16 + (side % 3) * 26; }

    @Override
    protected void renderOwnTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        for(int side = 0; side < CraneRouterBlockEntity.SIDES; side++) {

            if(!this.isHovered(mouseX, mouseY, buttonX(side), buttonY(side), 18, 18)) continue;

            int mode = this.menu.be.modes[side];

            List<Component> text = switch(mode) {
                case CraneRouterBlockEntity.MODE_WHITELIST -> List.of(
                        Component.translatable("craneRouter.whitelist").withStyle(ChatFormatting.GREEN),
                        Component.translatable("craneRouter.whitelist.desc"));
                case CraneRouterBlockEntity.MODE_BLACKLIST -> List.of(
                        Component.translatable("craneRouter.blacklist").withStyle(ChatFormatting.RED),
                        Component.translatable("craneRouter.blacklist.desc"));
                case CraneRouterBlockEntity.MODE_WILDCARD -> List.of(
                        Component.translatable("craneRouter.wildcard").withStyle(ChatFormatting.YELLOW),
                        Component.translatable("craneRouter.wildcard.desc"));
                default -> List.of(Component.translatable("craneRouter.off").withStyle(ChatFormatting.GRAY));
            };

            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            return;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int side = 0; side < CraneRouterBlockEntity.SIDES; side++) {

            if(!this.isHovered(mouseX, mouseY, buttonX(side), buttonY(side), 18, 18)) continue;

            this.click();
            CompoundTag data = new CompoundTag();
            data.putInt("toggle", side);
            PacketDistributor.sendToServer(new CompoundTagControl(data, this.menu.be.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 5, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 47, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        /* Zwei Teile: oben der breite Kopf, unten der schmalere Rucksackbereich. */
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 256, 93, 256, 256);
        guiGraphics.blit(TEXTURE, this.leftPos + 39, this.topPos + 93, 39, 93, 176, 108, 256, 256);

        for(int side = 0; side < CraneRouterBlockEntity.SIDES; side++) {
            guiGraphics.blit(TEXTURE, this.leftPos + buttonX(side), this.topPos + buttonY(side),
                    238, 93 + this.menu.be.modes[side] * 18, 18, 18, 256, 256);
        }
    }
}
