package com.hbm.inventory.screens;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.inventory.recipes.PedestalRecipes.PedestalRecipe;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenClayTablet.
 *
 * Was die Tontafel zeigt: ein Sockelrezept, teils verdeckt. Oben das Erzeugnis mit Namen,
 * darunter die neun Plaetze im Quadrat, rechts unten das Zeichen der Zusatzbedingung --
 * Vollmond, Neumond oder Sonne.
 *
 * WELCHE PLAETZE SICHTBAR SIND, haengt am Wurf im Stapel und nicht am Zufall des Augenblicks:
 * derselbe Random, derselbe Kern, dieselbe Folge. Die Tafel zeigt darum bei jedem Oeffnen
 * dasselbe. Im Original ist das genauso geloest, und es ist der Grund, warum der Wurf
 * ueberhaupt gespeichert wird.
 *
 * DIE REIHENFOLGE DER ZUEGE ist die des Originals und darf nicht wandern: erst das Rezept
 * aus der Menge, dann Zeile fuer Zeile die neun Plaetze. Wer hier eine Wuerfelei einschiebt,
 * verschiebt alle folgenden -- die Tafel zeigte dann ein anderes Bild als im Original.
 *
 * ZWEI TAFELN, ZWEI MENGEN. Metadatenwert null ist die helle Tafel: erste Rezeptmenge, jede
 * zweite Zelle offen. Eins ist die dunkle: zweite Menge, nur jede vierte Zelle offen, und
 * die Sinnbilder kommen sechzehn Pixel weiter rechts aus der Tafel.
 */
public class ClayTabletScreen extends Screen {

    private static final ResourceLocation TEXTUR = NuclearTechMod.withDefaultNamespace("textures/gui/guide_pedestal.png");

    private static final int BREITE = 142;
    private static final int HOEHE = 84;

    private final ItemStack tafel;

    public ClayTabletScreen(ItemStack tafel) {
        super(Component.empty());
        this.tafel = tafel.copy();
    }

    @Override public boolean isPauseScreen() { return false; }

    /** Abgedunkelt wie in 1.7.10, ohne den 1.21-Weichzeichner. */
    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(gui);
    }

    /**
     * super.render zuerst: es zeichnet den Hintergrund. Stand es wie vorher am Ende, lag der
     * Hintergrund -- samt Weichzeichner -- ueber der schon gezeichneten Tafel.
     */
    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {

        super.render(gui, mouseX, mouseY, partialTick);

        int links = (this.width - BREITE) / 2;
        int oben = (this.height - HOEHE) / 2;

        int meta = MetaHelper.getMeta(this.tafel);
        boolean dunkel = meta == 1;
        int tafelVersatz = dunkel ? 84 : 0;
        int bildVersatz = dunkel ? 16 : 0;
        float aufdeckung = dunkel ? 0.25F : 0.5F;

        gui.blit(TEXTUR, links, oben, 0, tafelVersatz, BREITE, HOEHE);

        List<PedestalRecipe> menge = PedestalRecipes.recipeSets[Math.abs(meta) % PedestalRecipes.recipeSets.length];
        Long kern = this.tafel.get(NtmDataComponents.TABLET_SEED.get());

        if(kern == null || menge.isEmpty()) {
            zeichneAllesVerdeckt(gui, links, oben, bildVersatz);
            return;
        }

        Random wurf = new Random(kern);
        PedestalRecipe rezept = menge.get(wurf.nextInt(menge.size()));

        /* Das Zeichen der Bedingung, rechts unten. Die drei Zeilen des Originals in einer. */
        int zeichen = switch(rezept.extra) {
            case FULL_MOON -> 32;
            case NEW_MOON -> 48;
            case SUN -> 64;
            default -> -1;
        };
        if(zeichen >= 0) gui.blit(TEXTUR, links + 120, oben + 62, 142 + bildVersatz, zeichen, 16, 16);

        for(int zeile = 0; zeile < 3; zeile++) {
            for(int spalte = 0; spalte < 3; spalte++) {

                int x = links + 7 + spalte * 27;
                int y = oben + 7 + zeile * 27;

                if(wurf.nextFloat() > aufdeckung) {
                    gui.blit(TEXTUR, x, y, 142 + bildVersatz, 16, 16, 16);
                    continue;
                }

                AStack zutat = rezept.input[spalte + zeile * 3];
                if(zutat == null) {
                    gui.blit(TEXTUR, x, y, 142 + bildVersatz, 0, 16, 16);
                    continue;
                }

                /* Ein Tag steht fuer mehrere Gegenstaende; der Port blaettert sie im
                 * Sekundentakt durch -- extractForCyclingDisplay(20) ist genau das, und es
                 * liefert von sich aus "nothing", wenn nichts passt. */
                ItemStack bild = zutat.extractForCyclingDisplay(20);
                gui.renderItem(bild, x, y);
                gui.renderItemDecorations(this.font, bild, x, y);
            }
        }

        ItemStack erzeugnis = rezept.output;
        gui.renderItem(erzeugnis, links + BREITE / 2 - 8, oben - 20);
        gui.renderItemDecorations(this.font, erzeugnis, links + BREITE / 2 - 8, oben - 20);

        Component name = erzeugnis.getHoverName();
        gui.drawString(this.font, name, links + (BREITE - this.font.width(name)) / 2, oben - 30, 0xFFFFFF, false);
    }

    private void zeichneAllesVerdeckt(GuiGraphics gui, int links, int oben, int bildVersatz) {
        for(int zeile = 0; zeile < 3; zeile++) {
            for(int spalte = 0; spalte < 3; spalte++) {
                gui.blit(TEXTUR, links + 7 + spalte * 27, oben + 7 + zeile * 27, 142 + bildVersatz, 16, 16, 16);
            }
        }
    }

    /**
     * ANDERS ALS IM ORIGINAL schliesst nur Escape und die Inventartaste -- das erledigt
     * Screen von sich aus. Das Original schreibt dafuer ein eigenes keyTyped, weil GuiScreen
     * in 1.7.10 bei einem Bildschirm ohne Container nichts dergleichen mitbringt.
     */
}
