package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.storage.MachineBatteryBlockEntity;
import com.hbm.inventory.menus.MachineBatteryMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineBattery.
 *
 * Alle Koordinaten unveraendert. Die Leistungsanzeige ist der Wert delta aus der
 * BlockEntity: die Differenz zwischen dem aktuellen und dem 20 Ticks alten
 * Fuellstand, also HE/s, gefaerbt nach Vorzeichen.
 */
public class MachineBatteryScreen extends InfoScreen<MachineBatteryMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/storage/gui_battery.png");

    public MachineBatteryBlockEntity be;

    public MachineBatteryScreen(MachineBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.be = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        String deltaText = BobMathUtil.getShortNumber(Math.abs(be.delta)) + "HE/s";

        if(be.delta > 0) deltaText = ChatFormatting.GREEN + "+" + deltaText;
        else if(be.delta < 0) deltaText = ChatFormatting.RED + "-" + deltaText;
        else deltaText = ChatFormatting.YELLOW + "+" + deltaText;

        List<Component> info = List.of(
                Component.literal(BobMathUtil.getShortNumber(be.power) + "/" + BobMathUtil.getShortNumber(be.getMaxPower()) + "HE"),
                Component.literal(deltaText)
        );

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, leftPos + 62, topPos + 69 - 52, 52, 52, mouseX, mouseY, info);

        String lang = switch(be.priority) {
            case LOW -> "low";
            case HIGH -> "high";
            default -> "normal";
        };

        List<Component> priority = new ArrayList<>();
        priority.add(Component.translatable("battery.priority." + lang).withStyle(ChatFormatting.GRAY));
        priority.add(Component.translatable("battery.priority.recommended").withStyle(ChatFormatting.GRAY));
        String[] desc = I18nUtil.resolveKeyArray("battery.priority." + lang + ".desc");
        for(String s : desc) priority.add(Component.literal(s).withStyle(ChatFormatting.GRAY));

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, leftPos + 152, topPos + 35, 16, 16, mouseX, mouseY, priority);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {

        CompoundTag tag = new CompoundTag();

        if(this.isHovered(x, y, 133, 16, 18, 18)) { this.click(); tag.putBoolean("low", true); }
        if(this.isHovered(x, y, 133, 52, 18, 18)) { this.click(); tag.putBoolean("high", true); }
        if(this.isHovered(x, y, 152, 35, 16, 16)) { this.click(); tag.putBoolean("priority", true); }

        if(!tag.isEmpty()) PacketDistributor.sendToServer(new CompoundTagControl(tag, be.getBlockPos()));

        return super.mouseClicked(x, y, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int partialTicks) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(be.power > 0 && be.getMaxPower() > 0) {
            int p = (int) be.getPowerRemainingScaled(52);
            guiGraphics.blit(TEXTURE, this.leftPos + 62, this.topPos + 69 - p, 176, 52 - p, 52, p);
        }

        guiGraphics.blit(TEXTURE, this.leftPos + 133, this.topPos + 16, 176, 52 + be.redLow * 18, 18, 18);
        guiGraphics.blit(TEXTURE, this.leftPos + 133, this.topPos + 52, 176, 52 + be.redHigh * 18, 18, 18);
        guiGraphics.blit(TEXTURE, this.leftPos + 152, this.topPos + 35, 194, 52 + be.priority.ordinal() * 16 - 16, 16, 16);
    }

    /** Der Titel traegt im Original den Fuellstand in Klammern. */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component name = Component.literal(this.title.getString() + " (" + be.power + " HE)");
        guiGraphics.drawString(this.font, name, this.imageWidth / 2 - this.font.width(name) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
