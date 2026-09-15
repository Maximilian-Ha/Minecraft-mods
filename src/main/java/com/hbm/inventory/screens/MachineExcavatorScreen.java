package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineExcavatorBlockEntity;
import com.hbm.inventory.menus.MachineExcavatorMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineExcavator.
 *
 * Fuenf grosse Schalter in einer Reihe. Drei davon koennen NICHT wollen: Bohrer, Adernbau und
 * Seidenberuehrung blinken, wenn sie eingeschaltet sind, aber der Bohrkopf sie nicht hergibt oder
 * der Strom fehlt.
 */
public class MachineExcavatorScreen extends InfoScreen<MachineExcavatorMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/excavator.png");

    /** Die fuenf Schalter: x-Stelle und Name im Steuerpaket. */
    private static final int[] SWITCH_X = new int[] { 6, 30, 54, 78, 102 };
    private static final String[] SWITCH_KEY = new String[] { "drill", "crusher", "walling", "veinminer", "silktouch" };

    private final MachineExcavatorBlockEntity be;

    public MachineExcavatorScreen(MachineExcavatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.be = menu.be;

        this.imageWidth = 242;
        this.imageHeight = 204;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        for(int i = 0; i < SWITCH_X.length; i++) {
            this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, this.leftPos + SWITCH_X[i], this.topPos + 42, 20, 40, mouseX, mouseY,
                    List.of(Component.translatable("excavator." + SWITCH_KEY[i])));
        }

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, this.leftPos + 220, this.topPos + 18, 16, 52, be.getPower(), MachineExcavatorBlockEntity.MAX_POWER);
        be.tank.renderTankTooltip(guiGraphics, mouseX, mouseY, this.leftPos + 202, this.topPos + 18, 16, 52);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for(int i = 0; i < SWITCH_X.length; i++) {
            if(this.isHovered(mouseX, mouseY, SWITCH_X[i], 42, 20, 40)) {

                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(NtmSoundEvents.LEVER_LARGE.get(), 1.0F));

                CompoundTag data = new CompoundTag();
                data.putBoolean(SWITCH_KEY[i], true);
                PacketDistributor.sendToServer(new CompoundTagControl(data, be.getBlockPos()));
                break;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 41, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        /* Die Oberflaeche ist zweiteilig: oben das breite Bedienfeld, unten das schmalere
         * Spielerinventar. */
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 242, 96);
        guiGraphics.blit(TEXTURE, this.leftPos + 33, this.topPos + 104, 33, 104, 176, 100);

        int i = (int) (be.getPower() * 52 / be.getMaxPower());
        guiGraphics.blit(TEXTURE, this.leftPos + 220, this.topPos + 70 - i, 229, 156 - i, 16, i);

        if(be.getPower() > be.getPowerConsumption()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 224, this.topPos + 4, 239, 156, 9, 12);
        }

        /* Kein Bohrkopf im Fach: das Fach blinkt. */
        if(be.getInstalledDrill() == null && blink()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 171, this.topPos + 74, 209, 154, 18, 18);
        }

        this.drawSwitch(guiGraphics, 0, be.enableDrill, be.getInstalledDrill() != null && be.getPower() >= be.getPowerConsumption());
        this.drawSwitch(guiGraphics, 1, be.enableCrusher, true);
        this.drawSwitch(guiGraphics, 2, be.enableWalling, true);
        this.drawSwitch(guiGraphics, 3, be.enableVeinMiner, be.canVeinMine());
        this.drawSwitch(guiGraphics, 4, be.enableSilkTouch, be.canSilkTouch());

        be.tank.renderTank(this.leftPos + 202, this.topPos + 70, 1F, 16, 52);
    }

    /** Ein gedrueckter Schalter samt Leuchte: gruen, wenn er wirkt, sonst blinkend rot. */
    private void drawSwitch(GuiGraphics guiGraphics, int index, boolean enabled, boolean effective) {

        if(!enabled) return;

        int x = SWITCH_X[index];

        guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + 42, 209, 114, 20, 40);

        if(effective) {
            guiGraphics.blit(TEXTURE, this.leftPos + x + 5, this.topPos + 5, 209, 104, 10, 10);
        } else if(blink()) {
            guiGraphics.blit(TEXTURE, this.leftPos + x + 5, this.topPos + 5, 219, 104, 10, 10);
        }
    }

    private static boolean blink() {
        return System.currentTimeMillis() % 1000 < 500;
    }
}
