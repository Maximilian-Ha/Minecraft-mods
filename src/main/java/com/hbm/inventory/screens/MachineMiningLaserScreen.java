package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineMiningLaserBlockEntity;
import com.hbm.inventory.menus.MachineMiningLaserMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMiningLaser.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * DIE LISTE DER AUFWERTUNGEN steht als Hinweisfeld daneben, weil sie sonst nirgends steht:
 * vier der sechs Sonderaufwertungen schliessen einander aus, und das sieht man ihnen nicht an.
 */
public class MachineMiningLaserScreen extends InfoScreen<MachineMiningLaserMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_laser_miner.png");

    private final MachineMiningLaserBlockEntity be;

    public MachineMiningLaserScreen(MachineMiningLaserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 88,
                this.be.power, MachineMiningLaserBlockEntity.maxPower);

        this.be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 72, 7, 52);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 87, this.topPos + 31, 8, 8,
                this.leftPos + 141, this.topPos + 55,
                Component.translatable("desc.gui.upgrade"),
                Component.translatable("desc.gui.upgrade.speed12"),
                Component.translatable("desc.gui.upgrade.effect12"),
                Component.translatable("desc.gui.upgrade.overdrive"),
                Component.translatable("desc.gui.upgrade.fortune"),
                Component.translatable("desc.gui.upgrade.smelter"),
                Component.translatable("desc.gui.upgrade.shredder"),
                Component.translatable("desc.gui.upgrade.centrifuge"),
                Component.translatable("desc.gui.upgrade.crystallizer"),
                Component.translatable("desc.gui.upgrade.nullifier"));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 61, this.topPos + 17, 18, 18, mouseX, mouseY,
                Component.translatable(this.be.isOn ? "desc.miningLaser.on" : "desc.miningLaser.off"),
                Component.translatable("desc.miningLaser.width", this.be.getWidth(), this.be.getWidth()));

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

        int power = (int) (this.be.power * 88 / MachineMiningLaserBlockEntity.maxPower);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 106 - power, 176, 88 - power, 16, power, 256, 256);

        int progress = (int) (this.be.clientBreakProgress * 34);
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 61, this.topPos + 41, 192, 0, progress, 8, 256, 256);

        if(this.be.isOn) guiGraphics.blit(TEXTURE, this.leftPos + 61, this.topPos + 17, 176, 88, 18, 18, 256, 256);

        this.drawInfoPanel(guiGraphics, this.leftPos + 87, this.topPos + 31, 8);

        this.be.tank.renderTank(this.leftPos + 35, this.topPos + 124, 0, 7, 52);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(button == 0 && this.isHovered(mouseX, mouseY, 61, 17, 18, 18)) {
            this.click();
            PacketDistributor.sendToServer(new CompoundTagControl(new CompoundTag(), this.be.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
