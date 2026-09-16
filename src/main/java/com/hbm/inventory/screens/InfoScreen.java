package com.hbm.inventory.screens;

import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;

public abstract class InfoScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    private static final ResourceLocation GUI_UTIL = NuclearTechMod.withDefaultNamespace("textures/gui/gui_utility.png");

    public InfoScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }


    public void drawElectricityInfo(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height, long power, long maxPower) {
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, x, y, width, height, mouseX, mouseY, Component.literal(BobMathUtil.getShortNumber(power) + "/" + BobMathUtil.getShortNumber(maxPower) + I18nUtil.resolveKey("he")));
    }

    public void drawCustomInfoStat(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, Component... text) {
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, x, y, width, height, tPosX, tPosY, Arrays.asList(text));
    }

    public void drawCustomInfoStat(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, List<Component> text) {

        if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {
            guiGraphics.renderComponentTooltip(this.font, text, tPosX, tPosY);
        }
    }

    public void drawCustomInfoStat(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height, Component... text) {

        List<Component> components = Arrays.asList(text);
        if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) guiGraphics.renderComponentTooltip(this.font, components, mouseX, mouseY);
    }

    /**
     * Die Zeilen, die das kleine Infofeld neben den Aufwertungsplaetzen zeigt: fuer jede
     * eingesetzte Aufwertung, was sie an dieser Maschine bewirkt. Die Maschine beschreibt sich
     * dabei selbst ueber IUpgradeInfoProvider -- der Schirm weiss nichts ueber die Wirkung.
     */
    public static List<Component> upgradeInfo(MachineBaseBlockEntity be, IUpgradeInfoProvider provider, int firstSlot, int lastSlot) {

        Map<UpgradeType, Integer> levels = new EnumMap<>(UpgradeType.class);

        for(int slot = firstSlot; slot <= lastSlot; slot++) {
            ItemStack stack = be.getItem(slot);
            if(stack.getItem() instanceof MachineUpgradeItem item && provider.getValidUpgrades().containsKey(item.type)) {
                levels.merge(item.type, item.tier, Integer::sum);
            }
        }

        List<Component> lines = new ArrayList<>();

        if(levels.isEmpty()) {
            lines.add(Component.translatable("upgrade.none"));
            return lines;
        }

        for(Map.Entry<UpgradeType, Integer> entry : levels.entrySet()) {
            if(entry.getValue() <= 0) continue;
            if(!provider.canProvideInfo(entry.getKey(), entry.getValue(), TooltipFlag.Default.NORMAL)) continue;
            provider.provideInfo(entry.getKey(), entry.getValue(), lines, TooltipFlag.Default.NORMAL);
        }

        return lines;
    }

    public void drawInfoPanel(GuiGraphics guiGraphics, int x, int y, int type) {
        switch(type) {
            case 0 -> guiGraphics.blit(GUI_UTIL, x, y, 0, 0, 8, 8); //Small blue I
            case 1 -> guiGraphics.blit(GUI_UTIL, x, y,  0, 8, 8, 8); //Small green I
            case 2 -> guiGraphics.blit(GUI_UTIL, x, y, 8, 0, 16, 16); //Large blue I
            case 3 -> guiGraphics.blit(GUI_UTIL, x, y, 24, 0, 16, 16); //Large green I
            case 4 -> guiGraphics.blit(GUI_UTIL, x, y, 0, 16, 8, 8); //Small red !
            case 5 -> guiGraphics.blit(GUI_UTIL, x, y, 0, 24, 8, 8); //Small yellow !
            case 6 -> guiGraphics.blit(GUI_UTIL, x, y, 8, 16, 16, 16); //Large red !
            case 7 -> guiGraphics.blit(GUI_UTIL, x, y, 24, 16, 16, 16); //Large yellow !
            case 8 -> guiGraphics.blit(GUI_UTIL, x, y, 0, 32, 8, 8); //Small blue *
            case 9 -> guiGraphics.blit(GUI_UTIL, x, y, 0, 40, 8, 8); //Small grey *
            case 10 -> guiGraphics.blit(GUI_UTIL, x, y, 8, 32, 16, 16); //Large blue *
            case 11 -> guiGraphics.blit(GUI_UTIL, x, y, 24, 32, 16, 16); //Large grey *
        }
    }

    protected boolean isHovered(double mouseX, double mouseY, int left, int top, int sizeX, int sizeY) {
        return leftPos + left <= mouseX && leftPos + left + sizeX > mouseX && topPos + top < mouseY && topPos + top + sizeY >= mouseY;
    }

    @SuppressWarnings("DataFlowIssue")
    public void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }
}
