package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineArcFurnaceLargeBlockEntity;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.menus.MachineArcFurnaceLargeMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineArcFurnaceLarge.
 * Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * ABWEICHUNG: das Original zeichnet die Materialsaeule additiv und legt einen halbdurchsichtigen
 * weissen Schleier darueber. GuiGraphics kennt keinen Wechsel der Mischfunktion, deshalb zeichnet
 * der Port beide Lagen ueber die regulaere Mischung. Das Ergebnis ist etwas weniger leuchtend.
 */
public class MachineArcFurnaceLargeScreen extends InfoScreen<MachineArcFurnaceLargeMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_arc_furnace.png");

    private final MachineArcFurnaceLargeBlockEntity arc;

    public MachineArcFurnaceLargeScreen(MachineArcFurnaceLargeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.arc = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 256;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawStackInfo(guiGraphics, this.arc.liquids, mouseX, mouseY, 152, 36);
        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 8, this.topPos + 36, 7, 70, this.arc.getPower(), this.arc.getMaxPower());

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 151, 17, 18, 18)) {
            this.click();
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("liquid", true);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.arc.getBlockPos()));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if(this.arc.liquidMode) guiGraphics.blit(TEXTURE, this.leftPos + 151, this.topPos + 17, 190, 18, 18, 18, 256, 256);
        if(this.arc.isProgressing) guiGraphics.blit(TEXTURE, this.leftPos + 7, this.topPos + 17, 190, 0, 18, 18, 256, 256);

        int p = (int) (this.arc.power * 70 / MachineArcFurnaceLargeBlockEntity.maxPower);
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 106 - p, 176, 70 - p, 7, p, 256, 256);

        int o = (int) (this.arc.progress * 70);
        guiGraphics.blit(TEXTURE, this.leftPos + 17, this.topPos + 106 - o, 183, 70 - o, 7, o, 256, 256);

        this.drawStack(guiGraphics, this.arc.liquids, MachineArcFurnaceLargeBlockEntity.maxLiquid, 152, 106);
    }

    /** Was gerade im Ofen steht, als Liste unter dem Mauszeiger. */
    protected void drawStackInfo(GuiGraphics guiGraphics, List<MaterialStack> stacks, int mouseX, int mouseY, int x, int y) {

        List<Component> list = new ArrayList<>();

        if(stacks.isEmpty()) {
            list.add(Component.translatable("desc.gui.arcfurnace.empty").withStyle(ChatFormatting.RED));
        }

        for(MaterialStack stack : stacks) {
            list.add(stack.material.getName()
                    .append(": " + Mats.formatAmount(stack.amount, Screen.hasShiftDown()))
                    .withStyle(ChatFormatting.YELLOW));
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + x, this.topPos + y, 16, 70, mouseX, mouseY, list);
    }

    /** Die Saeule aus geschmolzenem Material, von unten nach oben in Schichten. */
    protected void drawStack(GuiGraphics guiGraphics, List<MaterialStack> stacks, int capacity, int x, int y) {

        if(stacks.isEmpty()) return;

        int lastHeight = 0;
        int lastQuant = 0;

        for(MaterialStack stack : stacks) {

            int targetHeight = (lastQuant + stack.amount) * 70 / capacity;

            if(lastHeight == targetHeight) continue; // Schichten unter einem Pixel entfallen

            Color color = new Color(stack.material.moltenColor);
            guiGraphics.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
            guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + y - targetHeight, 208, 70 - targetHeight, 16, targetHeight - lastHeight, 256, 256);
            guiGraphics.setColor(1F, 1F, 1F, 0.3F);
            guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + y - targetHeight, 208, 70 - targetHeight, 16, targetHeight - lastHeight, 256, 256);
            guiGraphics.setColor(1F, 1F, 1F, 1F);

            lastQuant += stack.amount;
            lastHeight = targetHeight;
        }
    }
}
