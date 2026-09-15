package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.WatzBlockEntity;
import com.hbm.inventory.menus.WatzMenu;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIWatz.
 *
 * Die Oberflaeche wird selbst heiss: je waermer der Kern, desto roeter der obere Teil des
 * Hintergrunds. Rechts stehen die drei Tanks -- kaltes Kuehlmittel, heisses, Schlamm --, unten
 * links die Waerme und rechts daneben der Fluss.
 *
 * Der Schalter rechts sperrt die Faecher auf ihre jetzige Bestueckung: was danach hereinkommt,
 * muss zu dem passen, was beim Sperren drinlag. Das ist der Sinn der Sache -- Trichter sollen
 * abgebrannte Pellets ersetzen, ohne den Kern umzubauen.
 */
public class WatzScreen extends InfoScreen<WatzMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_watz.png");

    private final WatzBlockEntity be;

    public WatzScreen(WatzMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.be = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 229;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 13, this.topPos + 100, 18, 18,
                Component.literal(String.format(Locale.US, "%,d", this.be.heat) + " TU"));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 143, this.topPos + 71, 16, 16,
                Component.literal(this.be.isLocked ? "Unlock pellet IO configuration" : "Lock pellet IO configuration"));

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 142, this.topPos + 23, 6, 45);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 148, this.topPos + 23, 6, 45);
        this.be.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 154, this.topPos + 23, 6, 45);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        /* Der Fluss steht kleiner als der uebrige Text und rechtsbuendig an der Anzeige. */
        double scale = 1.25D;
        String flux = String.format(Locale.US, "%,.1f", this.be.fluxDisplay);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale((float) (1D / scale), (float) (1D / scale), 1F);
        guiGraphics.drawString(this.font, flux, (int) (161 * scale - this.font.width(flux)), (int) (107 * scale), 0x00ff00, false);
        guiGraphics.pose().popPose();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 142, 70, 18, 18)) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("lock", true);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        /* Der obere Teil faerbt sich mit der Waerme -- logarithmisch, sonst waere er sofort rot. */
        float col = Mth.clamp(1F - (float) Math.log(this.be.heat / 100_000D + 1D) * 0.4F, 0F, 1F);

        guiGraphics.setColor(1.0F, col, col, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 131, 122);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(TEXTURE, this.leftPos + 131, this.topPos, 131, 0, 36, 122);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos + 130, 0, 130, this.imageWidth, 99);
        guiGraphics.blit(TEXTURE, this.leftPos + 126, this.topPos + 31, 176, 31, 9, 60);
        guiGraphics.blit(TEXTURE, this.leftPos + 105, this.topPos + 96, 185, 26, 30, 26);
        guiGraphics.blit(TEXTURE, this.leftPos + 9, this.topPos + 96, 184, 0, 26, 26);

        if(this.be.isOn) guiGraphics.blit(TEXTURE, this.leftPos + 147, this.topPos + 8, 176, 0, 8, 8);
        if(this.be.isLocked) guiGraphics.blit(TEXTURE, this.leftPos + 142, this.topPos + 70, 210, 0, 18, 18);

        ScreenElements.drawSmoothGauge(this.leftPos + 22, this.topPos + 109, 1F - col, 5, 2, 1, 0x7F0000);

        this.be.tanks[0].renderTank(this.leftPos + 143, this.topPos + 69, 0F, 4, 43);
        this.be.tanks[1].renderTank(this.leftPos + 149, this.topPos + 69, 0F, 4, 43);
        this.be.tanks[2].renderTank(this.leftPos + 155, this.topPos + 69, 0F, 4, 43);
    }
}
