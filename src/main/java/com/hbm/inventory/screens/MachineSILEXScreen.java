package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineSILEXBlockEntity;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.menus.MachineSILEXMenu;
import com.hbm.items.machine.FelCrystalItem.Wellenlaenge;
import com.hbm.main.NuclearTechMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUISILEX.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * DIE WELLE IST DER GANZE WITZ DIESES FENSTERS: quer ueber die Maschine laeuft eine Sinuskurve,
 * deren Frequenz sich mit jeder Stufe der Wellenlaenge verdoppelt -- infrarot schwingt traege,
 * Digamma flimmert. Sichtbares Licht hat keine feste Farbe, sondern schillert durch den
 * Farbkreis; alle anderen tragen die Farbe ihres Kristalls.
 *
 * Ohne Kristall im FEL steht gar keine Welle da, und die Maschine tut nichts.
 */
public class MachineSILEXScreen extends InfoScreen<MachineSILEXMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/machine/gui_silex.png");

    private final MachineSILEXBlockEntity be;

    public MachineSILEXScreen(MachineSILEXMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 42, 52, 7);

        if(this.be.current != null) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 27, this.topPos + 72, 16, 52, mouseX, mouseY,
                    Component.literal(this.be.currentFill + "/" + MachineSILEXBlockEntity.MAX_FILL + "mB"),
                    this.be.current.toStack().getHoverName());
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 10, this.topPos + 92, 10, 10, mouseX, mouseY,
                Component.translatable("desc.gui.silex.void"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, this.leftPos + 10, this.topPos + 92, 12, 12)) {
            this.click();
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2 - 54, 8, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);

        if(this.be.mode != Wellenlaenge.NULL) {
            Component name = Component.translatable(this.be.mode.name).withStyle(this.be.mode.textfarbe);
            guiGraphics.drawString(this.font, name, 100 + (32 - this.font.width(name) / 2), 16, 0, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.be.mode != Wellenlaenge.NULL) this.drawWave(guiGraphics);

        /* Der Balken unter dem Tank sagt, ob die Maschine mit dieser Fluessigkeit etwas anfangen
         * kann: gruen fuer Peroxid und alles unmittelbar Einleitbare, rot fuer den Rest. */
        if(this.be.tank.getFill() > 0) {
            boolean usable = this.be.tank.getTankType() == Fluids.PEROXIDE || MachineSILEXBlockEntity.acceptsFluid(this.be.tank.getTankType());
            guiGraphics.blit(TEXTURE, this.leftPos + 7, this.topPos + 41, 176, usable ? 118 : 109, 54, 9, 256, 256);
        }

        int progress = this.be.getProgressScaled(69);
        if(progress > 0) guiGraphics.blit(TEXTURE, this.leftPos + 45, this.topPos + 82, 176, 0, progress, 43, 256, 256);

        int fill = this.be.getFillScaled(52);
        if(fill > 0) guiGraphics.blit(TEXTURE, this.leftPos + 26, this.topPos + 124 - fill, 176, 109 - fill, 16, fill, 256, 256);

        int fluid = this.be.getFluidScaled(52);
        if(fluid > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 42, 176,
                    this.be.tank.getTankType() == Fluids.PEROXIDE ? 43 : 50, fluid, 7, 256, 256);
        }
    }

    /**
     * Die Sinuskurve. Masse und Schrittweite wie im Original: 84 breit, 16 hoch, aufgeloest in
     * halben Bildpunkten, und sie wandert mit der Weltzeit nach links.
     */
    private void drawWave(GuiGraphics guiGraphics) {

        if(this.be.getLevel() == null) return;

        final float x = 81F;
        final float y = 46F;
        final float scale = 8F;
        final float resolution = 0.5F;
        final float samples = 84F / resolution;

        float freq = 0.1F * (float) Math.pow(2, this.be.mode.ordinal());
        long time = this.be.getLevel().getGameTime();

        int color = this.be.mode != Wellenlaenge.VISIBLE
                ? this.be.mode.fensterfarbe
                : Color.HSBtoRGB(time / 50.0F, 0.5F, 1F) & 0xFFFFFF;

        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

        float offset = (float) (time % (long) Math.max(4D * Math.PI / freq, 1D));

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(3F);
        RenderSystem.disableDepthTest();

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for(int i = 1; i < samples; i++) {
            double currentX = offset + x + i * resolution;
            double nextX = offset + x + (i + 1) * resolution;
            double currentY = y + scale * Math.sin(freq * currentX);
            double nextY = y + scale * Math.sin(freq * nextX);

            buffer.addVertex(matrix, (float) (this.leftPos + currentX - offset), (float) (this.topPos + currentY), 0F).setColor(red, green, blue, 1F);
            buffer.addVertex(matrix, (float) (this.leftPos + nextX - offset), (float) (this.topPos + nextY), 0F).setColor(red, green, blue, 1F);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1F);
    }
}
