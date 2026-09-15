package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.albion.MachinePADipoleBlockEntity;
import com.hbm.inventory.menus.MachinePADipoleMenu;
import com.hbm.items.machine.PACoilItem;
import com.hbm.items.machine.PACoilItem.EnumCoilType;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.math.NumberUtils;
import com.mojang.math.Axis;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIPADipole.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Drei Kompassrosen, drei Knoepfe: unterhalb der Schwelle, oberhalb, und bei Redstone. Der blaue
 * Zeiger ist der Spieler, der rote der eingestellte Ausgang -- man dreht sich hin und liest ab,
 * wohin der Strahl geht.
 *
 * ABWEICHUNG: das Original zeichnet die Zeiger mit dem Tessellator und rechnet den Winkel selbst
 * in einen Vektor um. Hier dreht der Posenstapel ein Rechteck; das Ergebnis ist dasselbe, der
 * Weg dorthin ist der von 1.21.
 */
public class MachinePADipoleScreen extends InfoScreen<MachinePADipoleMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/particleaccelerator/gui_dipole.png");

    private final MachinePADipoleBlockEntity be;
    private EditBox threshold;

    public MachinePADipoleScreen(MachinePADipoleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    protected void init() {
        super.init();

        this.threshold = new EditBox(this.font, this.leftPos + 47, this.topPos + 77, 66, 8, Component.empty());
        this.threshold.setTextColor(0x00ff00);
        this.threshold.setTextColorUneditable(0x00ff00);
        this.threshold.setBordered(false);
        this.threshold.setMaxLength(9);
        this.threshold.setValue(String.valueOf(this.be.threshold));
        this.addRenderableWidget(this.threshold);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.coolantTanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 36, 16, 52);
        this.be.coolantTanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 36, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 52, this.be.power, this.be.getMaxPower());

        this.dirTooltip(guiGraphics, mouseX, mouseY, 29, this.be.dirLower);
        this.dirTooltip(guiGraphics, mouseX, mouseY, 43, this.be.dirUpper);
        this.dirTooltip(guiGraphics, mouseX, mouseY, 57, this.be.dirRedstone);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void dirTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int y, int dir) {
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 62, this.topPos + y, 12, 12, mouseX, mouseY,
                Component.literal("Player orientation").withStyle(ChatFormatting.BLUE),
                Component.literal("Output orientation:").withStyle(ChatFormatting.RED),
                Component.literal(MachinePADipoleBlockEntity.ditToDir(dir).name()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 62, 29, 12, 12)) this.send("lower");
        if(this.isHovered(mouseX, mouseY, 62, 43, 12, 12)) this.send("upper");
        if(this.isHovered(mouseX, mouseY, 62, 57, 12, 12)) this.send("redstone");

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void send(String key) {
        this.click();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {

        if(this.threshold.charTyped(codePoint, modifiers)) {
            this.pushThreshold();
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(this.threshold.isFocused() && this.threshold.keyPressed(keyCode, scanCode, modifiers)) {
            this.pushThreshold();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void pushThreshold() {

        String digits = this.threshold.getValue();
        while(digits.startsWith("0") && !"0".equals(digits)) digits = digits.substring(1);
        if(digits.isEmpty()) digits = "0";
        this.threshold.setValue(digits);

        if(!NumberUtils.isDigits(digits)) return;

        CompoundTag tag = new CompoundTag();
        tag.putInt("threshold", NumberUtils.toInt(digits));
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2 - 9, 6, 0xffffff, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);

        guiGraphics.drawString(this.font, Component.literal("/123K").withStyle(ChatFormatting.AQUA), 136, 22, 4210752, false);

        int heat = (int) Math.ceil(this.be.temperature);
        Component label = Component.literal(heat + "K").withStyle(heat > 123 ? ChatFormatting.RED : ChatFormatting.AQUA);
        guiGraphics.drawString(this.font, label, 166 - this.font.width(label), 12, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int power = (int) (this.be.power * 52 / this.be.getMaxPower());
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 70 - power, 184, 52 - power, 16, power);

        int heat = (int) Math.ceil(this.be.temperature);
        if(heat <= 123) guiGraphics.blit(TEXTURE, this.leftPos + 93, this.topPos + 54, 176, 8, 8, 8);

        EnumCoilType type = PACoilItem.getCoil(this.be.slots.get(MachinePADipoleBlockEntity.SLOT_COIL));
        if(type != null) {
            guiGraphics.blit(TEXTURE, this.leftPos + 103, this.topPos + 54, 176, 8, 8, 8);
            int u = type == EnumCoilType.NIOBIUM || type == EnumCoilType.CHLOROPHYTE ? 228 : 200;
            int v = type == EnumCoilType.BSCCO || type == EnumCoilType.CHLOROPHYTE ? 124 : 96;
            guiGraphics.blit(TEXTURE, this.leftPos + 83, this.topPos + 20, u, v, 28, 28);
        }

        if(this.be.power >= MachinePADipoleBlockEntity.usage) guiGraphics.blit(TEXTURE, this.leftPos + 83, this.topPos + 54, 176, 8, 8, 8);

        float yaw = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getYRot() : 0F;

        this.needle(guiGraphics, 68, 35, 0xff8080ff, 180F);
        this.needle(guiGraphics, 68, 35, 0xffff0000, yaw - this.be.dirLower * 90F);
        this.needle(guiGraphics, 68, 49, 0xff8080ff, 180F);
        this.needle(guiGraphics, 68, 49, 0xffff0000, yaw - this.be.dirUpper * 90F);
        this.needle(guiGraphics, 68, 63, 0xff8080ff, 180F);
        this.needle(guiGraphics, 68, 63, 0xffff0000, yaw - this.be.dirRedstone * 90F);

        this.be.coolantTanks[0].renderTank(this.leftPos + 134, this.topPos + 88, 0, 16, 52);
        this.be.coolantTanks[1].renderTank(this.leftPos + 152, this.topPos + 88, 0, 16, 52);
    }

    /** Ein sechs Pixel langer Zeiger, der aus der Mitte der Rose in die gegebene Richtung zeigt. */
    private void needle(GuiGraphics guiGraphics, int x, int y, int color, float yaw) {

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(this.leftPos + x, this.topPos + y, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(yaw));
        guiGraphics.fill(0, 0, 1, 6, color);
        pose.popPose();
    }
}
