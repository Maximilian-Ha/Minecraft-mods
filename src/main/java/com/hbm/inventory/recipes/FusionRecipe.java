package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.FusionRecipe.
 *
 * Ein Fusionsrezept traegt drei Zahlen mehr als ein gewoehnliches:
 *
 * - ignitionTemp: wie viel Klystronleistung noetig ist, damit das Plasma ueberhaupt zuendet.
 * - outputTemp: wie viel Plasmawaerme bei voller Fahrt herauskommt.
 * - neutronFlux: wie viel Neutronenfluss nebenher anfaellt -- davon lebt der Brutreaktor.
 *
 * Dazu die Farbe des Plasmas, die der Torus an alle Abnehmer weitergibt.
 */
public class FusionRecipe extends GenericRecipe {

    /** Mindest-Klystronleistung, damit das Plasma zuendet. */
    public long ignitionTemp;
    /** Plasmawaerme bei voller Fahrt. */
    public long outputTemp;
    /** Neutronenfluss bei voller Fahrt. */
    public double neutronFlux;

    public float r = 1F;
    public float g = 0.2F;
    public float b = 0.6F;

    public FusionRecipe(String name) { super(name); }

    public FusionRecipe setInputEnergy(long ignitionTemp) { this.ignitionTemp = ignitionTemp; return this; }
    public FusionRecipe setOutputEnergy(long outputTemp) { this.outputTemp = outputTemp; return this; }
    public FusionRecipe setOutputFlux(double neutronFlux) { this.neutronFlux = neutronFlux; return this; }
    public FusionRecipe setRGB(float r, float g, float b) { this.r = r; this.g = g; this.b = b; return this; }

    @Override
    public List<Component> print() {

        List<Component> list = new ArrayList<>();
        list.add(this.getName().withStyle(ChatFormatting.YELLOW));

        this.duration(list);
        this.power(list);

        list.add(Component.literal(I18nUtil.resolveKey("gui.recipe.fusionIn") + ": "
                + BobMathUtil.getShortNumber(this.ignitionTemp) + "KyU/t").withStyle(ChatFormatting.LIGHT_PURPLE));
        list.add(Component.literal(I18nUtil.resolveKey("gui.recipe.fusionOut") + ": "
                + BobMathUtil.getShortNumber(this.outputTemp) + "TU/t").withStyle(ChatFormatting.LIGHT_PURPLE));
        list.add(Component.literal(I18nUtil.resolveKey("gui.recipe.fusionFlux") + ": "
                + ((int) (this.neutronFlux * 10)) / 10D + " flux/t").withStyle(ChatFormatting.LIGHT_PURPLE));

        this.input(list);
        this.output(list);

        return list;
    }
}
