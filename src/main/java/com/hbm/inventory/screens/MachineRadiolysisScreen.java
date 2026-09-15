package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineRadiolysisBlockEntity;
import com.hbm.inventory.menus.MachineRadiolysisMenu;
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
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRadiolysis.
 * Alle Blit- und Tooltip-Koordinaten unveraendert uebernommen.
 */
public class MachineRadiolysisScreen extends InfoScreen<MachineRadiolysisMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/gui_radiolysis.png");

    private final MachineRadiolysisBlockEntity radiolysis;

    public MachineRadiolysisScreen(MachineRadiolysisMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.radiolysis = menu.be;
        this.imageWidth = 230;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.radiolysis.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 61, this.topPos + 17, 8, 52);
        this.radiolysis.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 87, this.topPos + 17, 12, 16);
        this.radiolysis.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 87, this.topPos + 53, 12, 16);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 17, 16, 34,
                this.radiolysis.power, MachineRadiolysisBlockEntity.MAX_POWER);

        List<Component> descText = new ArrayList<>();
        for(String s : I18nUtil.resolveKeyArray("desc.gui.radiolysis.desc")) descText.add(Component.literal(s));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 16, 16, 16,
                this.leftPos - 8, this.topPos + 32, descText);

        List<Component> heatText = new ArrayList<>();
        for(String s : I18nUtil.resolveKeyArray("desc.gui.rtg.heat", this.radiolysis.heat)) heatText.add(Component.literal(s));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 34, 16, 16,
                this.leftPos - 8, this.topPos + 50, heatText);

        List<Component> pelletText = new ArrayList<>();
        pelletText.add(Component.literal(I18nUtil.resolveKey("desc.gui.rtg.pellets")));

        /* Hier zaehlt die Waerme zehnfach statt fuenffach -- die Radiolyse ist der bessere Wandler. */
        for(RTGPelletType pellet : RTGPelletType.values()) {
            String name = I18nUtil.resolveKey(NtmItems.PELLET_RTG.get().getDescriptionId() + "." + pellet.name().toLowerCase(Locale.US));
            pelletText.add(Component.literal(I18nUtil.resolveKey("desc.gui.rtg.pelletPower", name, pellet.getHeat() * 10)));
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 52, 16, 16,
                this.leftPos - 8, this.topPos + 68, pelletText);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 88 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int i = (int) (this.radiolysis.power * 34 / MachineRadiolysisBlockEntity.MAX_POWER);
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 51 - i, 240, 34 - i, 16, i, 256, 256);

        this.radiolysis.tanks[0].renderTank(this.leftPos + 61, this.topPos + 69, 1F, 8, 52);

        for(int j = 0; j < 2; j++) {
            this.radiolysis.tanks[j + 1].renderTank(this.leftPos + 87, this.topPos + 33 + j * 36, 1F, 12, 16);
        }

        this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 16, 10);
        this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 34, 2);
        this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 52, 3);
    }
}
