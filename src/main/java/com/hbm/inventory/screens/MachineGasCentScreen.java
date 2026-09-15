package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineGasCentBlockEntity;
import com.hbm.blockentity.machine.MachineGasCentBlockEntity.PseudoFluidTank;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineGasCentMenu;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineGasCent.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * DIE PSEUDOTANKS WERDEN MIT DEM BILD DES ROHRFLUIDS GEZEICHNET, wie im Original: eine Stufe
 * hat kein eigenes Aussehen, und UF6 soll auch als hochangereichertes UF6 nach UF6 aussehen.
 * Dafuer steht hier ein Anzeigetank, der nur zum Zeichnen dient -- so muss das Zeichenwerk des
 * FluidTank nicht nachgebaut werden.
 *
 * DIE BEIDEN STUFEN STEHEN IM TEXT, nicht im Bild. Gold heisst "das ist die schnelle Stufe",
 * Dunkelrot "sie steht, weil die schnelle Zentrifuge fehlt".
 */
public class MachineGasCentScreen extends InfoScreen<MachineGasCentMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_centrifuge_gas.png");

    private final MachineGasCentBlockEntity be;

    /** Nur zum Zeichnen; haelt nie eigenen Zustand ueber einen Aufruf hinaus. */
    private final FluidTank display = new FluidTank(Fluids.NONE, 1);

    public MachineGasCentScreen(MachineGasCentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 206;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 15, this.topPos + 15, 24, 55,
                mouseX, mouseY, this.tankInfo(this.be.inputTank, true));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 137, this.topPos + 15, 25, 55,
                mouseX, mouseY, this.tankInfo(this.be.outputTank, false));

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 182, this.topPos + 17, 16, 52,
                this.be.power, MachineGasCentBlockEntity.maxPower);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 12, this.topPos + 16, 16, 16,
                this.leftPos - 8, this.topPos + 32, lines("desc.gui.gasCent.enrichment"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 12, this.topPos + 32, 16, 16,
                this.leftPos - 8, this.topPos + 48, lines("desc.gui.gasCent.output"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /** Ein Text mit Trennzeichen als Zeilen; das Trennzeichen des Mods ist das Dollarzeichen. */
    private static List<Component> lines(String key) {
        List<Component> list = new ArrayList<>();
        for(String line : I18nUtil.resolveKeyArray(key)) list.add(Component.literal(line));
        return list;
    }

    /**
     * Name der Stufe und Fuellstand. Bei der schnellen Stufe faerbt sich der Name: gold, wenn
     * die Zentrifuge sie fahren kann, dunkelrot, wenn nicht.
     */
    private Component[] tankInfo(PseudoFluidTank tank, boolean isInput) {

        Component name = tank.getTankType().getName();

        if(tank.getTankType().getIfHighSpeed()) {
            name = name.copy().withStyle(isInput && !this.be.isFast() ? ChatFormatting.DARK_RED : ChatFormatting.GOLD);
        }

        return new Component[] { name, Component.literal(tank.getFill() + " / " + tank.getMaxFill() + " mB") };
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int power = (int) (this.be.power * 52 / MachineGasCentBlockEntity.maxPower);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 182, this.topPos + 69 - power, 206, 52 - power, 16, power, 256, 256);

        int progress = this.be.progress * 36 / Math.max(1, this.be.getProcessingSpeed());
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 70, this.topPos + 35, 206, 52, progress, 13, 256, 256);

        this.renderPseudoTank(this.be.inputTank, this.leftPos + 16, this.topPos + 68);
        this.renderPseudoTank(this.be.inputTank, this.leftPos + 32, this.topPos + 68);
        this.renderPseudoTank(this.be.outputTank, this.leftPos + 138, this.topPos + 68);
        this.renderPseudoTank(this.be.outputTank, this.leftPos + 154, this.topPos + 68);

        this.drawInfoPanel(guiGraphics, this.leftPos - 12, this.topPos + 16, 3);
        this.drawInfoPanel(guiGraphics, this.leftPos - 12, this.topPos + 32, 2);
    }

    /** y ist die UNTERKANTE, wie bei FluidTank.renderTank. */
    private void renderPseudoTank(PseudoFluidTank tank, int x, int y) {

        if(tank.getTankType() == PseudoFluidType.NONE || tank.getFill() <= 0) return;

        this.display.setTankType(this.be.tank.getTankType());
        this.display.changeTankSize(tank.getMaxFill());
        this.display.setFill(tank.getFill());
        this.display.renderTank(x, y, 0, 6, 52);
    }
}
