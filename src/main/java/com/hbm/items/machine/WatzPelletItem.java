package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.util.EnumUtil;
import com.hbm.util.TagsUtil;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLinear;
import com.hbm.util.function.Function.FunctionQuadratic;
import com.hbm.util.function.Function.FunctionSqrt;
import com.hbm.util.function.Function.FunctionSqrtFalling;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemWatzPellet.
 *
 * Der Brennstoff des Watz-Reaktors. Zwoelf Sorten, und anders als bei jedem anderen Reaktor des
 * Mods sind nicht alle davon Brennstoff: Blei, Bor und abgereichertes Uran brennen nicht,
 * sondern schlucken Fluss und setzen ihn in Waerme um. Wer den Kern nur mit Brennstoff fuellt,
 * bekommt keinen Reaktor, sondern eine Bombe mit Verzoegerung.
 *
 * Die Kennzahlen je Sorte:
 * - passive:      Grundfluss, den die Sorte auch ohne Anregung abgibt. Was hier ueber null
 *                 steht, zuendet sich selbst.
 * - heatEmission: Waerme je erzeugter Flusseinheit.
 * - mudContent:   Schlamm je Flusseinheit. Laeuft der Schlammtank ueber, fliegt die Anlage.
 * - burnFunc:     Fluss hinein, Reaktivitaet heraus -- die eigentliche Kettenreaktion.
 * - heatDiv:      Temperaturkoeffizient. Teilt die Reaktivitaet, je heisser der Kern wird.
 *                 Eine fallende Wurzel bremst sich selbst; bei NQD und NQR waechst sie
 *                 stattdessen mit der Hitze, und das ist genau so gewollt.
 * - absorbFunc:   Fluss hinein, Waerme heraus -- fuer die Sorten, die nur schlucken.
 *
 * ABWEICHUNG: das Original faerbt die Symbole zur Laufzeit ein, indem es aus einer
 * Graustufenvorlage je Sorte eine eigene Textur baut und den Grauverlauf zwischen zwei Farben
 * neu verteilt (RGBMutatorInterpolatedComponentRemap). Dieses Verfahren gibt es auf 1.21 nicht
 * mehr. Der Port faerbt stattdessen ueber einen Farbgeber ein -- eine Multiplikation statt einer
 * Neuverteilung. Der Unterschied ist sichtbar, aber gering, und beide Farben der Sorte bleiben
 * erhalten: die helle traegt das Symbol, die dunkle den Rand.
 */
public class WatzPelletItem extends EnumMultiItem {

    /**
     * Gesamtausbeute einer Sorte. Im Original ein Feld der Aufzaehlung, das fuer jede Sorte
     * denselben Wert bekommt und nirgends ueberschrieben wird -- es sieht aus, als haette es
     * einmal schwanken sollen. Der Port haelt es deshalb als Konstante.
     */
    public static final double YIELD = 500_000_000D;

    /** Ob dies die abgebrannte Ausfuehrung ist. Sie ist reines Abfallprodukt. */
    public final boolean depleted;

    public WatzPelletItem(Properties properties, boolean depleted) {
        super(properties, EnumWatzType.class, true, false);
        this.depleted = depleted;
    }

    public enum EnumWatzType {

        SCHRABIDIUM( 0x32FFFF, 0x005C5C, 2_000, 20D,  0.01D,    new FunctionLinear(1.5D),  new FunctionSqrtFalling(10D), null),
        HES(         0x66DCD6, 0x023933, 1_750, 20D,  0.005D,   new FunctionLinear(1.25D), new FunctionSqrtFalling(15D), null),
        MES(         0xCBEADF, 0x28473C, 1_500, 15D,  0.0025D,  new FunctionLinear(1.15D), new FunctionSqrtFalling(15D), null),
        LES(         0xABB4A8, 0x0C1105, 1_250, 15D,  0.00125D, new FunctionLinear(1D),    new FunctionSqrtFalling(20D), null),
        HEN(         0xA6B2A6, 0x030F03, 0,     10D,  0.0005D,  new FunctionSqrt(100),     new FunctionSqrtFalling(10D), null),
        MEU(         0xC1C7BD, 0x2B3227, 0,     10D,  0.0005D,  new FunctionSqrt(75),      new FunctionSqrtFalling(10D), null),
        MEP(         0x9AA3A0, 0x111A17, 0,     15D,  0.0005D,  new FunctionSqrt(150),     new FunctionSqrtFalling(10D), null),
        /* Standardabsorber: schluckt nach einer Wurzel, also mit fallendem Zugewinn. */
        LEAD(        0xA6A6B2, 0x03030F, 0,     0,    0.0025D,  null, null, new FunctionSqrt(10)),
        /* Besserer Absorber: linear, schluckt also auch bei hohem Fluss weiter mit. */
        BORON(       0xBDC8D2, 0x29343E, 0,     0,    0.0025D,  null, null, new FunctionLinear(10)),
        /* Absorber mit positivem Koeffizienten: je mehr Fluss, desto gieriger. */
        DU(          0xC1C7BD, 0x2B3227, 0,     0,    0.0025D,  null, null, new FunctionQuadratic(1D, 1D).withDiv(100)),
        NQD(         0x4B4B4B, 0x121212, 2_000, 20,   0.01D,    new FunctionLinear(2D),    new FunctionSqrt(1D / 25D).withOff(25D * 25D), null),
        NQR(         0x2D2D2D, 0x0B0B0B, 2_500, 30,   0.01D,    new FunctionLinear(1.5D),  new FunctionSqrt(1D / 25D).withOff(25D * 25D), null);

        public final int colorLight;
        public final int colorDark;
        /** Grundfluss ohne Anregung. */
        public final double passive;
        /** Waerme je abgegebener Flusseinheit. */
        public final double heatEmission;
        /** Schlamm je Flusseinheit. */
        public final double mudContent;
        /** Fluss zu Reaktivitaet. */
        @Nullable public final Function burnFunc;
        /** Temperaturkoeffizient: teilt die Reaktivitaet abhaengig von der Hitze. */
        @Nullable public final Function heatDiv;
        /** Fluss zu Waerme, fuer die Sorten, die nichts abgeben. */
        @Nullable public final Function absorbFunc;

        EnumWatzType(int colorLight, int colorDark, double passive, double heatEmission, double mudContent,
                     @Nullable Function burnFunc, @Nullable Function heatDiv, @Nullable Function absorbFunc) {
            this.colorLight = colorLight;
            this.colorDark = colorDark;
            this.passive = passive;
            this.heatEmission = heatEmission;
            /* Halbiert, genau wie im Original -- der angegebene Wert gilt fuer zwei Durchlaeufe. */
            this.mudContent = mudContent / 2D;
            this.burnFunc = burnFunc;
            this.heatDiv = heatDiv;
            this.absorbFunc = absorbFunc;
        }
    }

    /* Restausbeute des Stapels. */

    public static double getYield(ItemStack stack) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        return tag.contains("yield") ? tag.getDouble("yield") : YIELD;
    }

    public static void setYield(ItemStack stack, double yield) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putDouble("yield", yield);
        TagsUtil.putCustomData(stack, tag);
    }

    /** Anteil der noch vorhandenen Ausbeute, zwischen null und eins. */
    public static double getEnrichment(ItemStack stack) {
        return Mth.clamp(getYield(stack) / YIELD, 0D, 1D);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        /* Wie im Original: die Ausbeute frueh eintragen, damit sie nirgends fehlen kann. */
        if(!this.depleted) setYield(stack, YIELD);
        super.onCraftedBy(stack, level, player);
    }

    @Override public boolean isBarVisible(ItemStack stack) { return !this.depleted && getEnrichment(stack) < 1D; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13F * (float) getEnrichment(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        if(this.depleted) {
            super.appendHoverText(stack, context, list, flag);
            return;
        }

        EnumWatzType num = EnumUtil.grabEnumSafely(EnumWatzType.class, MetaHelper.getMeta(stack));

        list.add(Component.literal("Depletion: " + String.format(Locale.US, "%.1f", (1D - getEnrichment(stack)) * 100D) + "%")
                .withStyle(ChatFormatting.GREEN));

        if(num.passive > 0) {
            list.add(Component.literal("Base fission rate: " + num.passive).withStyle(ChatFormatting.GOLD));
            list.add(Component.literal("Self-igniting!").withStyle(ChatFormatting.RED));
        }
        if(num.heatEmission > 0) list.add(Component.literal("Heat per flux: " + num.heatEmission + " TU").withStyle(ChatFormatting.GOLD));
        if(num.burnFunc != null) {
            list.add(Component.literal("Reaction function: " + num.burnFunc.getLabelForFuel()).withStyle(ChatFormatting.GOLD));
            list.add(Component.literal("Fuel type: ").withStyle(ChatFormatting.GOLD).append(num.burnFunc.getDangerFromFuel()));
        }
        if(num.heatDiv != null) list.add(Component.literal("Thermal multiplier: " + num.heatDiv.getLabelForFuel() + " TU-1").withStyle(ChatFormatting.GOLD));
        if(num.absorbFunc != null) list.add(Component.literal("Flux capture: " + num.absorbFunc.getLabelForFuel()).withStyle(ChatFormatting.GOLD));

        super.appendHoverText(stack, context, list, flag);
    }

    /**
     * Die Entsaettigung der abgebrannten Ausfuehrung, unveraendert aus dem Original: den
     * Farbabstand zum Mittelwert auf ein Zehntel zusammenziehen und das Ganze auf drei Viertel
     * abdunkeln.
     */
    public static int desaturate(int color) {

        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);

        int avg = (r + g + b) / 3;
        double approach = 0.9D;
        double mult = 0.75D;

        r -= (int) ((r - avg) * approach);
        g -= (int) ((g - avg) * approach);
        b -= (int) ((b - avg) * approach);

        r *= mult;
        g *= mult;
        b *= mult;

        return (r << 16) | (g << 8) | b;
    }
}
