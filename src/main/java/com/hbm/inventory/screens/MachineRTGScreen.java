package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineRTGBlockEntity;
import com.hbm.inventory.menus.MachineRTGMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.RTGPelletItem.RTGPelletType;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineRTG.
 * Alle Blit- und Tooltip-Koordinaten unveraendert uebernommen.
 */
public class MachineRTGScreen extends InfoScreen<MachineRTGMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/generators/gui_rtg.png");

    private final MachineRTGBlockEntity rtg;

    public MachineRTGScreen(MachineRTGMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.rtg = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 188;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 146, this.topPos + 9, 16, 51, this.rtg.power, MachineRTGBlockEntity.MAX_POWER);

        List<Component> heatText = new ArrayList<>();
        for(String s : I18nUtil.resolveKeyArray("desc.gui.rtg.heat", this.rtg.heat)) heatText.add(Component.literal(s));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 124, this.topPos + 9, 16, 51, mouseX, mouseY, heatText);

        List<Component> pelletText = new ArrayList<>();
        pelletText.add(Component.literal(I18nUtil.resolveKey("desc.gui.rtg.pellets")));

        for(RTGPelletType pellet : RTGPelletType.values()) {
            String name = I18nUtil.resolveKey(NtmItems.PELLET_RTG.get().getDescriptionId() + "." + pellet.name().toLowerCase(Locale.US));
            pelletText.add(Component.literal(I18nUtil.resolveKey("desc.gui.rtg.pelletPower", name, pellet.getHeat() * 5)));
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 12, this.topPos + 25, 16, 16, this.leftPos - 8, this.topPos + 36 + 16, pelletText);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 60 - this.font.width(this.title) / 2, 7, 10925486, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.rtg.hasHeat()) {
            int i = this.rtg.getHeatScaled(51);
            guiGraphics.blit(TEXTURE, this.leftPos + 124, this.topPos + 61 - i, 176, 10 + (51 - i), 16, i, 256, 256);
        }

        if(this.rtg.hasPower()) {
            int i = (int) this.rtg.getPowerScaled(51);
            guiGraphics.blit(TEXTURE, this.leftPos + 146, this.topPos + 61 - i, 192, 10 + (51 - i), 16, i, 256, 256);
        }

        this.drawInfoPanel(guiGraphics, this.leftPos - 12, this.topPos + 25, 2);
    }
}
