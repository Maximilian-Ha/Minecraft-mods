package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.icf.ICFPressBlockEntity;
import com.hbm.inventory.menus.ICFPressMenu;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIICFPress.
 *
 * Links der Myonenvorrat als schmaler Balken, daneben und rechts die beiden Tanks. Die beiden
 * Barrenfaecher sagen im Hinsehen, von welcher Seite ein Trichter sie erreicht.
 */
public class ICFPressScreen extends InfoScreen<ICFPressMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_icf_press.png");

    private final ICFPressBlockEntity be;

    public ICFPressScreen(ICFPressMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.be = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 179;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.be.tanks[0].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 44, this.topPos + 18, 16, 52);
        this.be.tanks[1].renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 152, this.topPos + 18, 16, 52);

        if(this.menu.getCarried().isEmpty()) {
            if(this.isHovering(62, 54, 16, 16, mouseX, mouseY) && !this.menu.slots.get(ICFPressBlockEntity.SLOT_MATERIAL_FIRST).hasItem()) {
                guiGraphics.renderComponentTooltip(this.font, List.of(Component.translatable("icfpress.inputTopBottom").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
            }
            if(this.isHovering(134, 54, 16, 16, mouseX, mouseY) && !this.menu.slots.get(ICFPressBlockEntity.SLOT_MATERIAL_SECOND).hasItem()) {
                guiGraphics.renderComponentTooltip(this.font, List.of(Component.translatable("icfpress.inputSides").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int m = this.be.muon * 52 / ICFPressBlockEntity.MAX_MUON;
        guiGraphics.blit(TEXTURE, this.leftPos + 28, this.topPos + 70 - m, 176, 52 - m, 4, m);

        this.be.tanks[0].renderTank(this.leftPos + 44, this.topPos + 70, 0F, 16, 52);
        this.be.tanks[1].renderTank(this.leftPos + 152, this.topPos + 70, 0F, 16, 52);
    }
}
