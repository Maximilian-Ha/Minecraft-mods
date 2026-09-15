package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity;
import com.hbm.inventory.menus.MachinePASourceMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
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
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIPASource.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 *
 * Die eine Anzeige, die den ganzen Ring beschreibt: ein farbiges Band in der Mitte, das sagt, was
 * mit dem Strahl los ist. Daneben ein Knopf, der ihn abbricht -- ein steckengebliebenes Teilchen
 * laesst sich sonst nicht loswerden.
 */
public class MachinePASourceScreen extends InfoScreen<MachinePASourceMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/particleaccelerator/gui_source.png");

    private final MachinePASourceBlockEntity be;

    public MachinePASourceScreen(MachinePASourceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.coolantTanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 134, this.topPos + 36, 16, 52);
        this.be.coolantTanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 36, 16, 52);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 18, 16, 52, this.be.power, this.be.getMaxPower());

        List<Component> info = new ArrayList<>();
        info.add(Component.literal("Last momentum: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(String.format(Locale.US, "%,d", this.be.lastSpeed)).withStyle(ChatFormatting.WHITE)));
        for(String line : I18nUtil.resolveKeyArray("pa." + this.be.state.name().toLowerCase(Locale.US) + ".desc")) {
            info.add(Component.literal(line).withStyle(ChatFormatting.YELLOW));
        }
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 105, this.topPos + 16, 10, 10, mouseX, mouseY, info);
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 105, this.topPos + 28, 10, 10, mouseX, mouseY,
                Component.literal("Cancel operation").withStyle(ChatFormatting.RED));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 105, 28, 10, 10)) {
            this.click();
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("cancel", true);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2 - 9, 4, 0xffffff, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);

        guiGraphics.drawString(this.font, Component.literal("/123K").withStyle(ChatFormatting.AQUA), 136, 22, 4210752, false);

        int heat = (int) Math.ceil(this.be.temperature);
        Component label = Component.literal(heat + "K").withStyle(heat > 123 ? ChatFormatting.RED : ChatFormatting.AQUA);
        guiGraphics.drawString(this.font, label, 166 - this.font.width(label), 12, 4210752, false);

        Component state = Component.translatable("pa." + this.be.state.name().toLowerCase(Locale.US));
        guiGraphics.drawString(this.font, state, 79 - this.font.width(state) / 2, 76, this.be.state.color, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int power = (int) (this.be.power * 52 / this.be.getMaxPower());
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 70 - power, 184, 52 - power, 16, power);

        int heat = (int) Math.ceil(this.be.temperature);
        if(heat <= 123) guiGraphics.blit(TEXTURE, this.leftPos + 44, this.topPos + 16, 176, 8, 8, 8);
        if(this.be.power >= MachinePASourceBlockEntity.usage) guiGraphics.blit(TEXTURE, this.leftPos + 44, this.topPos + 41, 176, 8, 8, 8);

        /* Das Zustandsband wird eingefaerbt statt neu gezeichnet -- eine Textur je Zustand waeren
         * dreizehn. */
        int color = this.be.state.color;
        guiGraphics.setColor(((color >> 16) & 0xff) / 255F, ((color >> 8) & 0xff) / 255F, (color & 0xff) / 255F, 1F);
        guiGraphics.blit(TEXTURE, this.leftPos + 45, this.topPos + 73, 176, 52, 68, 14);
        guiGraphics.setColor(1F, 1F, 1F, 1F);

        this.be.coolantTanks[0].renderTank(this.leftPos + 134, this.topPos + 88, 0, 16, 52);
        this.be.coolantTanks[1].renderTank(this.leftPos + 152, this.topPos + 88, 0, 16, 52);
    }
}
