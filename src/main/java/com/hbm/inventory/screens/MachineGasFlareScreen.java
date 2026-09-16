package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.oil.MachineGasFlareBlockEntity;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.menus.MachineGasFlareMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineGasFlare.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 */
public class MachineGasFlareScreen extends InfoScreen<MachineGasFlareMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/generators/gui_flare_stack.png");

    private final MachineGasFlareBlockEntity be;

    public MachineGasFlareScreen(MachineGasFlareMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 203;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 16, 35, 10, mouseX, mouseY,
                Component.translatable("flare.valve"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 50, 35, 14, mouseX, mouseY,
                Component.translatable("flare.ignition"));

        this.be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 17, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 143, this.topPos + 17, 16, 52,
                this.be.power, MachineGasFlareBlockEntity.MAX_POWER);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {

        if(this.isHovered(x, y, 89, 16, 16, 10)) {
            this.click();
            this.sendToggle("valve");
            return true;
        }

        if(this.isHovered(x, y, 89, 50, 16, 14)) {
            this.click();
            this.sendToggle("dial");
            return true;
        }

        return super.mouseClicked(x, y, button);
    }

    private void sendToggle(String key) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int j = (int) (this.be.power * 52L / Math.max(MachineGasFlareBlockEntity.MAX_POWER, 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 143, this.topPos + 69 - j, 176, 94 - j, 16, j, 256, 256);

        if(this.be.isOn) guiGraphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 15, 176, 0, 35, 10, 256, 256);
        if(this.be.doesBurn) guiGraphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 49, 176, 10, 35, 14, 256, 256);

        if(this.be.isOn && this.be.doesBurn && this.be.tank.getFill() > 0 && this.be.tank.getTankType().hasTrait(FT_Flammable.class)) {
            guiGraphics.blit(TEXTURE, this.leftPos + 88, this.topPos + 29, 176, 24, 18, 18, 256, 256);
        }

        this.be.tank.renderTank(this.leftPos + 35, this.topPos + 69, 0, 16, 52);
    }
}
