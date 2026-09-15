package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineMissileAssemblyBlockEntity;
import com.hbm.inventory.menus.MachineMissileAssemblyMenu;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineMissileAssembly.
 *
 * Ueber jedem Bauteilfach sitzt eine Leuchte: gruen heisst passt, und beim Leitwerk gibt es eine
 * zweite Farbe fuer "passt nicht" -- ein fehlendes Leitwerk ist kein Fehler und bleibt dunkel.
 *
 * NICHT UEBERNOMMEN: die drehende Vorschau der Rakete in der Mitte der Oberflaeche. Sie braucht
 * die Modelle je Bauteil, die dem Port noch fehlen (MissilePart, MissileMultipart,
 * MissilePronter); die Ampel und der Bauknopf arbeiten auch ohne sie.
 */
public class MachineMissileAssemblyScreen extends InfoScreen<MachineMissileAssemblyMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/missile_assembly.png");

    private final MachineMissileAssemblyBlockEntity be;

    public MachineMissileAssemblyScreen(MachineMissileAssemblyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.be = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.isHovered(mouseX, mouseY, 115, 35, 18, 18)) {

            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            CompoundTag data = new CompoundTag();
            data.putBoolean("build", true);
            PacketDistributor.sendToServer(new CompoundTagControl(data, this.be.getBlockPos()));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        /* Nur das Leitwerk hat eine zweite Farbe. Bei den anderen Faechern heisst dunkel
         * schlicht "noch nicht" -- so macht es das Original, und es ist richtig so: ein leeres
         * Fach ist kein Fehler, den man anleuchten muesste. */
        this.lamp(guiGraphics, 13, this.be.chipState() == MachineMissileAssemblyBlockEntity.STATE_GOOD, false);
        this.lamp(guiGraphics, 31, this.be.warheadState() == MachineMissileAssemblyBlockEntity.STATE_GOOD, false);
        this.lamp(guiGraphics, 49, this.be.fuselageState() == MachineMissileAssemblyBlockEntity.STATE_GOOD, false);
        this.lamp(guiGraphics, 67, this.be.stabilityState() == MachineMissileAssemblyBlockEntity.STATE_GOOD,
                this.be.stabilityState() == MachineMissileAssemblyBlockEntity.STATE_BAD);
        this.lamp(guiGraphics, 85, this.be.thrusterState() == MachineMissileAssemblyBlockEntity.STATE_GOOD, false);

        if(this.be.canBuild()) guiGraphics.blit(TEXTURE, this.leftPos + 115, this.topPos + 35, 176, 0, 18, 18);
    }

    /** Eine Leuchte ueber einem Fach: gruen bei gut, rot nur dort, wo das Original sie zeigt. */
    private void lamp(GuiGraphics guiGraphics, int x, boolean good, boolean bad) {
        if(good) guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + 23, 194, 0, 6, 8);
        else if(bad) guiGraphics.blit(TEXTURE, this.leftPos + x, this.topPos + 23, 200, 0, 6, 8);
    }
}
