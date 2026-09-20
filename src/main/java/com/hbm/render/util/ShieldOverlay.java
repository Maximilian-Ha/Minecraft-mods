package com.hbm.render.util;

import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.main.NuclearTechMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Portiert aus 1.7.10: RenderScreenOverlay.renderShieldBar.
 *
 * Die Schildleiste ueber der Lebensanzeige. Sie erscheint nur, wenn der Spieler ueberhaupt
 * ein Schild hat -- das Original prueft dasselbe in ModEventHandlerClient (Z. 402), bevor
 * es zeichnet.
 *
 * DIE ZAHLEN SIND DIE DES ORIGINALS: die leere Leiste liegt in overlay_misc.png bei
 * (146, 0) und ist 81 mal 9 Pixel gross, die Fuellung bei (147, 9) und hoechstens 79 breit.
 * Die Beschriftung steht viermal schwarz und einmal hellgelb uebereinander -- so macht das
 * Original seinen Schattenwurf, ohne den Schattenwurf der Schriftart zu benutzen.
 *
 * WO SIE HINGEHOERT. Das Original haengt sich an GuiIngameForge.left_height und schiebt den
 * Wert um zehn hoch, damit Hunger und Luft darunter rutschen. Diesen Zaehler gibt es in
 * 1.21 nicht mehr; die Leiste zeichnet sich hier NACH der Lebensanzeige und setzt sich um
 * eine Leistenhoehe darueber. Hunger und Luft bleiben, wo sie sind -- das heisst, die
 * Schildleiste legt sich ueber die Ruestungsleiste statt sie zu verdraengen. Den Zaehler
 * nachzubauen hiesse, in jede fremde Leiste einzugreifen.
 */
@OnlyIn(Dist.CLIENT)
public class ShieldOverlay {

    private static final ResourceLocation MISC = NuclearTechMod.withDefaultNamespace("textures/misc/overlay_misc.png");

    /** Die Breite der leeren Leiste, ihre Hoehe, und die groesste Fuellbreite. */
    private static final int BREITE = 81, HOEHE = 9, FUELLUNG = 79;

    public static void handleOverlay(RenderGuiLayerEvent.Post event, Player player) {

        if(!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) return;

        HbmPlayerAttachments props = HbmPlayerAttachments.getData(player);
        float grenze = props.getEffectiveMaxShield(player);

        if(grenze <= 0) return;

        zeichne(event.getGuiGraphics(), props.shield, grenze);
    }

    private static void zeichne(GuiGraphics guiGraphics, float schild, float grenze) {

        Font font = Minecraft.getInstance().font;

        int links = guiGraphics.guiWidth() / 2 - 91;

        /* Die Lebensanzeige steht 39 Pixel ueber dem unteren Rand und ist zehn hoch; die
         * Schildleiste kommt darueber. Im Original macht das left_height. */
        int oben = guiGraphics.guiHeight() - 39 - HOEHE - 1;

        guiGraphics.blit(MISC, links, oben, 146, 0, BREITE, HOEHE);

        int gefuellt = (int) Math.ceil(schild * FUELLUNG / grenze);
        if(gefuellt > 0) guiGraphics.blit(MISC, links + 1, oben, 147, 9, Math.min(gefuellt, FUELLUNG), HOEHE);

        String beschriftung = String.valueOf(((int) (schild * 10F)) / 10D);
        int mitte = links + 40 - font.width(beschriftung) / 2;

        guiGraphics.drawString(font, beschriftung, mitte + 1, oben + 1, 0x000000, false);
        guiGraphics.drawString(font, beschriftung, mitte - 1, oben + 1, 0x000000, false);
        guiGraphics.drawString(font, beschriftung, mitte, oben, 0x000000, false);
        guiGraphics.drawString(font, beschriftung, mitte, oben + 2, 0x000000, false);
        guiGraphics.drawString(font, beschriftung, mitte, oben + 1, 0xFFFF80, false);
    }
}
