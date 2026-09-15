package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineCombustionEngineBlockEntity;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.menus.MachineCombustionEngineMenu;
import com.hbm.items.machine.PistonsItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUICombustionEngine.
 * Blit- und Trefferkoordinaten unveraendert uebernommen.
 */
public class MachineCombustionEngineScreen extends InfoScreen<MachineCombustionEngineMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/generators/gui_combustion.png");

    private final MachineCombustionEngineBlockEntity be;

    public MachineCombustionEngineScreen(MachineCombustionEngineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 203;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 143, this.topPos + 17, 16, 52, this.be.getPower(), MachineCombustionEngineBlockEntity.MAX_POWER);
        this.be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 35, this.topPos + 17, 16, 52);

        // Drosselklappe: Durchsatz in mB/t, Anzeige an den Schieberegler geklemmt
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 80, this.topPos + 38, 34, 8,
                Mth.clamp(mouseX, this.leftPos + 80, this.leftPos + 114), Mth.clamp(mouseY, this.topPos + 38, this.topPos + 46),
                Component.literal(((this.be.setting * 2) / 10D) + "mB/t"));

        ItemStack pistons = this.be.getItem(MachineCombustionEngineBlockEntity.SLOT_PISTONS);
        if(pistons.getItem() instanceof PistonsItem) {
            double output = 0D;

            if(this.be.tank.getTankType().hasTrait(FT_Combustible.class)) {
                FT_Combustible trait = this.be.tank.getTankType().getTrait(FT_Combustible.class);
                output = this.be.setting * 0.2D * trait.getCombustionEnergy() / 1_000D * PistonsItem.getEfficiency(pistons, trait.getGrade());
            }

            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 50, 35, 14, mouseX, mouseY,
                    Component.literal(String.format(Locale.US, "%,d", (int) output) + " HE/t").withStyle(ChatFormatting.YELLOW),
                    Component.literal(String.format(Locale.US, "%,d", (int) (output * 20)) + " HE/s").withStyle(ChatFormatting.YELLOW));
        }

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + 79, this.topPos + 13, 35, 15, mouseX, mouseY,
                Component.translatable("gui.combustion_engine.ignition"));

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        ItemStack pistons = this.be.getItem(MachineCombustionEngineBlockEntity.SLOT_PISTONS);
        if(pistons.getItem() instanceof PistonsItem) {
            int meta = MetaHelper.getMeta(pistons);
            guiGraphics.blit(TEXTURE, this.leftPos + 80, this.topPos + 51, 176, 52 + meta * 12, 25, 12, 256, 256);
        }

        guiGraphics.blit(TEXTURE, this.leftPos + 79 + (this.be.setting * 32 / 30), this.topPos + 38, 192, 15, 4, 8, 256, 256);

        if(this.be.isOn) guiGraphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 13, 192, 0, 35, 15, 256, 256);

        int power = (int) (this.be.getPower() * 53 / MachineCombustionEngineBlockEntity.MAX_POWER);
        if(power > 0) guiGraphics.blit(TEXTURE, this.leftPos + 143, this.topPos + 69 - power, 176, 52 - power, 16, power, 256, 256);

        this.be.tank.renderTank(this.leftPos + 35, this.topPos + 69, 0, 16, 52);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(button == 0 && this.isHovered(mouseX, mouseY, 89, 13, 16, 14)) {
            this.click();
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("turnOn", true);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            return true;
        }

        if(button == 0 && this.isHovered(mouseX, mouseY, 79, 38, 36, 8)) {
            this.click();
            int setting = (int) ((mouseX - this.leftPos - 81) * 30 / 32);
            setting = Mth.clamp(setting, 0, MachineCombustionEngineBlockEntity.MAX_SETTING);

            CompoundTag tag = new CompoundTag();
            tag.putInt("setting", setting);
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
