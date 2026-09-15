package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineTurbineGasBlockEntity;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.menus.MachineTurbineGasMenu;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineTurbineGas.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Abweichung: das Ziehen des Leistungsreglers (mouseClickMove im Original) ist
 * durch einen Klick an die gewuenschte Stelle der Reglerbahn und durch das
 * Mausrad ersetzt, siehe mouseClicked/mouseScrolled.
 */
public class MachineTurbineGasScreen extends InfoScreen<MachineTurbineGasMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/generators/gui_turbinegas.png");

    private final MachineTurbineGasBlockEntity be;

    public MachineTurbineGasScreen(MachineTurbineGasMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 223;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 26, this.topPos + 108, 142, 16, this.be.power, this.be.getMaxPower());

        if(this.be.state == 1) {
            double consumption = MachineTurbineGasBlockEntity.getMaxConsumption(this.be.tanks[0].getTankType());
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 36, this.topPos + 36, 16, 66, mouseX, mouseY,
                    Component.literal("Fuel consumption: " + 20 * (consumption * 0.05D + consumption * this.be.throttle / 100) + " mb/s"));
        } else {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 36, this.topPos + 36, 16, 66, mouseX, mouseY,
                    Component.literal("Generator offline"));
        }

        if(this.be.temp >= 20) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 133, this.topPos + 23, 8, 72, mouseX, mouseY,
                    Component.literal("Temperature: " + this.be.temp + "\u00B0C"));
        } else {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 133, this.topPos + 23, 8, 72, mouseX, mouseY,
                    Component.literal("Temperature: 20\u00B0C"));
        }

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 16, 16, 48);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 70, 16, 32);
        this.be.tanks[2].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 147, this.topPos + 61, 16, 36);
        this.be.tanks[3].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 147, this.topPos + 21, 16, 36);

        List<Component> info = new ArrayList<>();
        for(String s : I18nUtil.resolveKeyArray("desc.gui.turbinegas.automode")) info.add(Component.literal(s));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 34, 16, 16, this.leftPos - 8, this.topPos + 44 + 16, info);

        List<Component> fuels = new ArrayList<>();
        fuels.add(Component.literal(I18nUtil.resolveKey("desc.gui.turbinegas.fuels")));
        for(FluidType type : Fluids.getInNiceOrder()) {
            if(type.hasTrait(FT_Combustible.class) && type.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS) {
                fuels.add(Component.literal("  ").append(type.getName()));
            }
        }
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 34 + 16, 16, 16, this.leftPos - 8, this.topPos + 44 + 16, fuels);

        if(this.be.tanks[0].getFill() < 5000 || this.be.tanks[1].getFill() < 1000) {
            List<Component> warning = new ArrayList<>();
            for(String s : I18nUtil.resolveKeyArray("desc.gui.turbinegas.warning")) warning.add(Component.literal(s));
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos - 16, this.topPos + 34 + 32, 16, 16, this.leftPos - 8, this.topPos + 44 + 16, warning);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.be.autoMode) {
            guiGraphics.blit(TEXTURE, this.leftPos + 74, this.topPos + 86, 194, 11, 29, 13, 256, 256); // Automatikknopf
        } else {
            guiGraphics.blit(TEXTURE, this.leftPos + 74, this.topPos + 86, 194, 24, 29, 13, 256, 256);
        }

        switch(this.be.state) {
            case 0 -> guiGraphics.blit(TEXTURE, this.leftPos + 80, this.topPos + 32, 178, 38, 16, 16, 256, 256); // roter Knopf
            case -1 -> {
                guiGraphics.blit(TEXTURE, this.leftPos + 80, this.topPos + 32, 194, 38, 16, 16, 256, 256); // oranger Knopf
                this.displayStartup(guiGraphics);
            }
            case 1 -> {
                guiGraphics.blit(TEXTURE, this.leftPos + 80, this.topPos + 32, 210, 38, 16, 16, 256, 256); // gruener Knopf
                this.drawPowerMeterDisplay(guiGraphics, 20 * this.be.instantPowerOutput);
            }
            default -> { }
        }

        guiGraphics.blit(TEXTURE, this.leftPos + 36, this.topPos + 97 - this.be.powerSliderPos, 178, 0, 16, 6, 256, 256); // Leistungsregler

        int power = (int) (this.be.power * 142 / MachineTurbineGasBlockEntity.maxPower); // Stromspeicher
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 26, this.topPos + 109, 0, 223, power, 16, 256, 256);

        ScreenElements.drawSmoothTextureModalCircle(guiGraphics, TEXTURE, this.leftPos + 64, this.topPos + 16, 0F, 176, 64, 48, 48, (double) this.be.rpm / 100);
        this.drawThermometer(guiGraphics, this.be.temp);

        this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 34, 3); // Hinweis
        this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 34 + 16, 2); // Treibstoffe
        if(this.be.tanks[0].getFill() < 5000 || this.be.tanks[1].getFill() < 1000) {
            this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 34 + 32, 7);
        }
        if(this.be.tanks[0].getFill() == 0 || this.be.tanks[1].getFill() == 0) {
            this.drawInfoPanel(guiGraphics, this.leftPos - 16, this.topPos + 34 + 32, 6);
        }

        this.be.tanks[0].renderTank(this.leftPos + 8, this.topPos + 65, 0F, 16, 48);
        this.be.tanks[1].renderTank(this.leftPos + 8, this.topPos + 103, 0F, 16, 32);
        this.be.tanks[2].renderTank(this.leftPos + 147, this.topPos + 98, 0F, 16, 36);
        this.be.tanks[3].renderTank(this.leftPos + 147, this.topPos + 58, 0F, 16, 36);
    }

    private int numberToDisplay = 0; // fuer den Anlauf
    private int digitNumber = 0;
    private int exponent = 0;

    /** Laesst die Ziffernanzeige beim Anlauf hochzaehlen, wie im Original */
    public void displayStartup(GuiGraphics guiGraphics) {

        if(this.numberToDisplay < 8888888 && this.be.counter < 60) { // 48 Bilder bis zum Abschluss

            this.digitNumber++;
            if(this.digitNumber == 9) {
                this.digitNumber = 1;
                this.exponent++;
            }
            this.numberToDisplay += (int) Math.pow(10, this.exponent);
        }

        if(this.be.counter > 50) this.numberToDisplay = 0;

        this.drawPowerMeterDisplay(guiGraphics, this.numberToDisplay);
    }

    /** Siebenstellige Ziffernanzeige aus der Textur */
    protected void drawPowerMeterDisplay(GuiGraphics guiGraphics, int number) {

        int firstDigitX = 65;
        int firstDigitY = 62;

        int[] digit = new int[7];

        for(int i = 6; i >= 0; i--) { // baut ein Feld aus den einzelnen Ziffern

            digit[i] = number % 10;

            number = number / 10;

            guiGraphics.blit(TEXTURE, this.leftPos + firstDigitX + i * 7, this.topPos + 9 + firstDigitY, 194 + digit[i] * 5, 0, 5, 11, 256, 256);
        }

        int uselessZeros = 0;

        for(int i = 0; i < 6; i++) { // zaehlt die fuehrenden Nullen, damit 57 statt 000057 steht

            if(digit[i] == 0) {
                uselessZeros++;
            } else {
                break;
            }
        }

        for(int i = 0; i < uselessZeros; i++) { // blendet die fuehrenden Nullen aus

            guiGraphics.blit(TEXTURE, this.leftPos + firstDigitX + i * 7, this.topPos + 9 + firstDigitY, 244, 0, 5, 11, 256, 256);
        }
    }

    /** Temperatursaeule, per Tesselator gezeichnet, weil der Ausschnitt mit der Temperatur waechst */
    protected void drawThermometer(GuiGraphics guiGraphics, int temp) {

        int xPos = this.leftPos + 136;
        int yPos = this.topPos + 28;

        int width = 2;
        int height = 64;

        int maxTemp = 800;

        float uMin = (176F / 256F);
        float uMax = (178F / 256F);
        float vMin = ((64F - 64F * temp / maxTemp) / 256F);
        float vMax = (64F / 256F);

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, xPos, yPos + height, 0F).setUv(uMin, vMax);
        buffer.addVertex(matrix, xPos + width, yPos + height, 0F).setUv(uMax, vMax);
        buffer.addVertex(matrix, xPos + width, yPos + 64 - (64F * temp / maxTemp), 0F).setUv(uMax, vMin);
        buffer.addVertex(matrix, xPos, yPos + 64 - (64F * temp / maxTemp), 0F).setUv(uMin, vMin);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        // runder Start-Stopp-Knopf, Mittelpunkt (88, 40), Radius 8
        if(Math.sqrt(Math.pow(mouseX - this.leftPos - 88, 2) + Math.pow(mouseY - this.topPos - 40, 2)) <= 8) {

            if(this.be.counter == 0 || this.be.counter == 579) {

                int state = this.be.state - 1; // aus(0) zu Anlauf(-1), Betrieb(1) zu aus(0)

                this.click();

                CompoundTag tag = new CompoundTag();
                tag.putInt("state", state);
                PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            }

            return true;
        }

        // Automatikknopf
        if(this.be.state == 1 && mouseX > this.leftPos + 74 && mouseX <= this.leftPos + 74 + 29 && mouseY >= this.topPos + 86 && mouseY < this.topPos + 86 + 13) {

            boolean automode = !this.be.autoMode;
            this.click();

            CompoundTag tag = new CompoundTag();
            tag.putBoolean("autoMode", automode);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            return true;
        }

        // Reglerbahn: Klick setzt die Reglerstellung direkt und schaltet die Automatik ab
        if(this.be.state == 1 && this.leftPos + 36 < mouseX && this.leftPos + 52 >= mouseX && this.topPos + 37 < mouseY && this.topPos + 103 >= mouseY) {

            int slidPos = (int) (this.topPos + 100 - mouseY);
            if(slidPos > 60) slidPos = 60;
            if(slidPos < 0) slidPos = 0;

            this.click();

            CompoundTag tag = new CompoundTag();
            tag.putBoolean("autoMode", false); // wer den Regler anfasst, schaltet die Automatik ab
            tag.putInt("slidPos", slidPos);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        // Feinverstellung des Reglers, ersetzt das Ziehen aus dem Original
        if(this.be.state == 1 && !this.be.autoMode && this.leftPos + 36 < mouseX && this.leftPos + 52 >= mouseX && this.topPos + 37 < mouseY && this.topPos + 103 >= mouseY) {

            int slidPos = this.be.powerSliderPos + (scrollY > 0 ? 1 : -1);
            if(slidPos > 60) slidPos = 60;
            if(slidPos < 0) slidPos = 0;

            CompoundTag tag = new CompoundTag();
            tag.putInt("slidPos", slidPos);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
