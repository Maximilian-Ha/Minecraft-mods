package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.fusion.FusionKlystronBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionTorusBlockEntity;
import com.hbm.inventory.menus.FusionKlystronMenu;
import com.hbm.inventory.screens.element.ScreenElements;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.hbm.util.BobMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIFusionKlystron.
 *
 * Ein Eingabefeld fuer die gewuenschte Leistung, drei Lampen und drei Zeiger: Leistung, Luft,
 * Strom. Leuchten alle drei gruen, laeuft das Klystron auf Ziel.
 */
public class FusionKlystronScreen extends InfoScreen<FusionKlystronMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_fusion_klystron.png");

    private final FusionKlystronBlockEntity klystron;
    private EditBox field;

    public FusionKlystronScreen(FusionKlystronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.klystron = menu.be;

        this.imageWidth = 194;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = 115 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 35;
        this.inventoryLabelY = this.imageHeight - 93;

        this.field = new EditBox(this.font, this.leftPos + 84, this.topPos + 22, 102, 12, Component.empty());
        this.field.setTextColor(0x00FF00);
        this.field.setTextColorUneditable(0x00FF00);
        this.field.setBordered(false);
        this.field.setMaxLength(10);
        this.field.setValue(String.valueOf(this.klystron.outputTarget));
        this.addRenderableWidget(this.field);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 52,
                this.klystron.power, this.klystron.getMaxPower());

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 43, this.topPos + 71, 18, 18,
                Component.literal(ChatFormatting.RED + "<- " + ChatFormatting.RESET
                        + BobMathUtil.getShortNumber(this.klystron.output) + "KyU / "
                        + BobMathUtil.getShortNumber(this.klystron.outputTarget) + "KyU"));

        this.klystron.compair.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 76, this.topPos + 71, 18, 18);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 115, this.topPos + 71, 18, 18,
                Component.literal(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET
                        + BobMathUtil.getShortNumber(this.klystron.output) + "HE / "
                        + BobMathUtil.getShortNumber(this.klystron.outputTarget) + "HE"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        String result = "= " + BobMathUtil.getShortNumber(this.klystron.outputTarget) + "KyU";
        if(this.klystron.outputTarget == FusionKlystronBlockEntity.MAX_OUTPUT) result += " (max)";
        guiGraphics.drawString(this.font, result, 183 - this.font.width(result), 40, 0x00FF00, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int p = (int) (this.klystron.power * 52 / Math.max(this.klystron.getMaxPower(), 1L));
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 70 - p, 194, 52 - p, 16, p);

        double outputGauge = this.klystron.outputTarget <= 0 ? 0 : (double) this.klystron.output / (double) this.klystron.outputTarget;
        double airGauge = (double) this.klystron.compair.getFill() / (double) this.klystron.compair.getMaxFill();
        double powerGauge = FusionTorusBlockEntity.getSpeedScaled(this.klystron.maxPower, this.klystron.power);

        this.led(guiGraphics, 160, powerGauge);
        this.led(guiGraphics, 170, airGauge);

        if(this.klystron.output >= this.klystron.outputTarget && this.klystron.output > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 180, this.topPos + 71, 210, 8, 8, 8);
        } else if(this.klystron.output > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 180, this.topPos + 71, 210, 0, 8, 8);
        }

        ScreenElements.drawSmoothGauge(this.leftPos + 52, this.topPos + 80, (float) outputGauge, 5, 2, 1, 0xA00000);
        ScreenElements.drawSmoothGauge(this.leftPos + 88, this.topPos + 80, (float) airGauge, 5, 2, 1, 0xA00000);
        ScreenElements.drawSmoothGauge(this.leftPos + 124, this.topPos + 80, (float) powerGauge, 5, 2, 1, 0xA00000);
    }

    /** Gruen, wenn die Anlage laeuft und der Vorrat reicht; sonst rot, solange ueberhaupt etwas da ist. */
    private void led(GuiGraphics guiGraphics, int x, double gauge) {
        if(gauge >= 0.5 && this.klystron.output > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + 71, 210, 8, 8, 8);
        } else if(gauge > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + 71, 210, 0, 8, 8);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.field.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.field.keyPressed(keyCode, scanCode, modifiers)) {
            this.sendTarget();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.field.charTyped(codePoint, modifiers)) {
            this.sendTarget();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void sendTarget() {
        if(!NumberUtils.isParsable(this.field.getValue())) return;

        long value = Math.max(NumberUtils.toLong(this.field.getValue()), 0L);

        CompoundTag tag = new CompoundTag();
        tag.putLong("amount", value);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.klystron.getBlockPos()));
    }
}
