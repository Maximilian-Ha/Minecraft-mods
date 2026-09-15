package com.hbm.util.function;

import com.hbm.util.BobMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.util.function.Function.
 *
 * Kurvenformen fuer Brennstoffe -- linear, logarithmisch, Wurzel, quadratisch. Anders als die
 * fest eingebauten RBMK-Kurven traegt jede Form ihre Konstanten selbst, damit dieselbe Klasse
 * fuer PWR, Watz und ICF taugt.
 *
 * ABWEICHUNG: das Original gibt beide Beschriftungen als String zurueck und klebt die Farbe mit
 * EnumChatFormatting davor. Hier bleibt die Formel ein String -- sie hat keine Farbe --, die
 * Gefahrenstufe wird eine Component, weil sie eine traegt.
 */
public abstract class Function {

    /** Teiler, der auf x angewandt wird, bevor die Kurve rechnet. */
    protected double div = 1D;
    /** Verschiebung, die nach dem Teilen auf x addiert wird. */
    protected double off = 0D;

    /** Die deutsche Aussprache von f(x) -- "F von X". Der Scherz stammt aus dem Original. */
    public abstract double effonix(double x);

    /** Die Kurve als Formel, wie sie im Tooltip des Brennstoffs steht. */
    public abstract String getLabelForFuel();

    /** Wie gefaehrlich diese Kurve ist, farbig. */
    public abstract Component getDangerFromFuel();

    public Function withDiv(double div) { this.div = div; return this; }
    public Function withOff(double off) { this.off = off; return this; }

    public double getX(double x) { return x / this.div + this.off; }

    public String getXName() { return this.getXName(true); }

    /**
     * Der Name von x samt Teiler und Verschiebung.
     *
     * ACHTUNG, Fehler aus dem Original uebernommen: die Klammern werden nie gesetzt, weil das
     * Original die Hilfsvariable mod anlegt, auf false laesst und nie beschreibt. Der Port
     * uebernimmt das Verhalten -- wer es aendert, aendert jeden Brennstoff-Tooltip.
     */
    public String getXName(boolean brackets) {

        String x = "x";
        if(this.div != 1D) x += " / " + String.format(Locale.US, "%,.1f", this.div);
        if(this.off != 0D) x += " + " + String.format(Locale.US, "%,.1f", this.off);

        return x;
    }

    /** Kurve mit einem Kennwert. */
    public abstract static class FunctionSingleArg extends Function {
        protected double level;
        public FunctionSingleArg(double level) { this.level = level; }
    }

    /** Kurve mit einem Kennwert und einer senkrechten Verschiebung. */
    public abstract static class FunctionDoubleArg extends Function {
        protected double level;
        protected double vOff;
        public FunctionDoubleArg(double level, double vOff) { this.level = level; this.vOff = vOff; }
    }

    public static class FunctionLogarithmic extends FunctionSingleArg {

        public FunctionLogarithmic(double level) { super(level); this.withOff(1D); }

        @Override public double effonix(double x) { return Math.log10(this.getX(x)) * this.level; }
        @Override public String getLabelForFuel() { return "log10(" + this.getXName(false) + ") * " + String.format(Locale.US, "%,.1f", this.level); }
        @Override public Component getDangerFromFuel() { return Component.literal("MEDIUM / LOGARITHMIC").withStyle(ChatFormatting.YELLOW); }
    }

    public static class FunctionPassive extends FunctionSingleArg {

        public FunctionPassive(double level) { super(level); }

        @Override public double effonix(double x) { return this.level; }
        @Override public String getLabelForFuel() { return String.format(Locale.US, "%,.1f", this.level); }
        @Override public Component getDangerFromFuel() { return Component.literal("SAFE / PASSIVE").withStyle(ChatFormatting.DARK_GREEN); }
    }

    public static class FunctionSqrt extends FunctionSingleArg {

        public FunctionSqrt(double level) { super(level); }

        @Override public double effonix(double x) { return BobMathUtil.squirt(this.getX(x)) * this.level; }
        /** Nicht ganz richtig, aber gut genug -- so steht es im Original. */
        @Override public String getLabelForFuel() { return "sqrt(" + this.getXName(false) + ") * " + String.format(Locale.US, "%,.3f", this.level); }
        @Override public Component getDangerFromFuel() { return Component.literal("MEDIUM / SQUARE ROOT").withStyle(ChatFormatting.YELLOW); }
    }

    /** Wurzelkurve, die mit wachsendem x faellt statt steigt. */
    public static class FunctionSqrtFalling extends FunctionSqrt {

        public FunctionSqrtFalling(double fallFactor) {
            super(1D / fallFactor);
            this.withOff(fallFactor * fallFactor);
        }
    }

    public static class FunctionLinear extends FunctionSingleArg {

        public FunctionLinear(double level) { super(level); }

        @Override public double effonix(double x) { return this.getX(x) * this.level; }
        @Override public String getLabelForFuel() { return this.getXName(true) + " * " + String.format(Locale.US, "%,.1f", this.level); }
        @Override public Component getDangerFromFuel() { return Component.literal("DANGEROUS / LINEAR").withStyle(ChatFormatting.RED); }
    }

    public static class FunctionQuadratic extends FunctionDoubleArg {

        public FunctionQuadratic(double level) { super(level, 0D); }
        public FunctionQuadratic(double level, double vOff) { super(level, vOff); }

        @Override public double effonix(double x) { return this.getX(x) * this.getX(x) * this.level + this.vOff; }

        @Override
        public String getLabelForFuel() {
            return this.getXName(true) + "² * " + String.format(Locale.US, "%,.1f", this.level)
                    + (this.vOff != 0 ? " + " + String.format(Locale.US, "%,.1f", this.vOff) : "");
        }

        @Override public Component getDangerFromFuel() { return Component.literal("DANGEROUS / QUADRATIC").withStyle(ChatFormatting.RED); }
    }
}
