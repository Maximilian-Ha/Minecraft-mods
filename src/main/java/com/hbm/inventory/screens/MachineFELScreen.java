package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineFELBlockEntity;
import com.hbm.inventory.menus.MachineFELMenu;
import com.hbm.items.machine.FelCrystalItem.Wellenlaenge;
import com.hbm.main.NuclearTechMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFEL.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Zwei kurze Striche in der Farbe der Wellenlaenge zeigen den Strahl: einer im Geraet, einer
 * am linken Rand, wo er das Fenster verlaesst. Darueber steht LIVE, solange er eine SILEX
 * trifft, und ERR., solange nicht.
 */
public class MachineFELScreen extends InfoScreen<MachineFELMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_fel.png");

    private final MachineFELBlockEntity be;

    public MachineFELScreen(MachineFELMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 203;
        this.imageHeight = 169;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 182, this.topPos + 27, 16, 113,
                this.be.power, MachineFELBlockEntity.MAX_POWER);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, this.leftPos + 142, this.topPos + 41, 29, 17)) {
            this.click();
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        int titleX = 90 + this.imageWidth / 2 - this.font.width(this.title) / 2;
        guiGraphics.drawString(this.font, this.title, titleX, 7, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 98, 4210752, false);

        if(!this.be.isOn) return;

        int statusX = this.imageWidth / 2 - this.font.width(this.title) / 2;

        if(this.be.missingValidSilex) {
            guiGraphics.drawString(this.font, Component.literal("ERR.").withStyle(ChatFormatting.RED), 55 + statusX, 9, 0xFF0000, false);
        } else {
            guiGraphics.drawString(this.font, Component.literal("LIVE").withStyle(ChatFormatting.GREEN), 54 + statusX, 9, 0x00FF00, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.be.isOn) guiGraphics.blit(TEXTURE, this.leftPos + 142, this.topPos + 41, 203, 0, 29, 17, 256, 256);

        int power = (int) this.be.getPowerScaled(114);
        if(power > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 182, this.topPos + 27 + 113 - power, 203, 17 + 113 - power, 16, power, 256, 256);
        }

        if(this.be.isBeamActive()) this.drawBeam(guiGraphics);
    }

    /** Die beiden Striche des Strahls; sichtbares Licht schillert wie im Fenster der SILEX. */
    private void drawBeam(GuiGraphics guiGraphics) {

        if(this.be.getLevel() == null) return;

        int color = this.be.mode != Wellenlaenge.VISIBLE
                ? this.be.mode.fensterfarbe
                : Color.HSBtoRGB(this.be.getLevel().getGameTime() / 50.0F, 0.5F, 1F) & 0xFFFFFF;

        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(5F);
        RenderSystem.disableDepthTest();

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float y = this.topPos + 31.5F;
        buffer.addVertex(matrix, this.leftPos + 113F, y, 0F).setColor(red, green, blue, 1F);
        buffer.addVertex(matrix, this.leftPos + 135F, y, 0F).setColor(red, green, blue, 1F);
        buffer.addVertex(matrix, 0F, y, 0F).setColor(red, green, blue, 1F);
        buffer.addVertex(matrix, this.leftPos + 4F, y, 0F).setColor(red, green, blue, 1F);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1F);
    }
}
