package com.hbm.items.machine;

import com.hbm.blockentity.machine.rbmk.IRBMKFluxReceiver.NType;
import com.hbm.blockentity.machine.rbmk.RBMKDials;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.items.component.RBMKFuelData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemRBMKRod.
 *
 * Ein RBMK-Brennstab. Der Stab nimmt Fluss auf, gibt Fluss ab, vergiftet sich mit Xenon, brennt
 * ab und heizt sich dabei auf.
 */
public class RBMKRodItem extends Item {

    /** Voller Name des Brennstoffs, wird im Tooltip angezeigt. */
    public final String fullName;

    /** Endpunkt der Flusskurve. */
    public double reactivity;
    /** Selbst erzeugter Fluss bei Neutronenquellen. */
    public double selfRate;
    public EnumBurnFunc function = EnumBurnFunc.LOG_TEN;
    public EnumDepleteFunc depFunc = EnumDepleteFunc.GENTLE_SLOPE;
    /** Faktor fuer die Xenonproduktion. */
    public double xGen = 0.5D;
    /** Teiler fuer den Xenonabbau. */
    public double xBurn = 50D;
    /** Waerme pro abgegebenem Fluss. */
    public double heat = 1D;
    /** Gesamtmenge an Fluss, die der Stab in seinem Leben aufnehmen kann. */
    public double yield;
    /** Ab dieser Huellentemperatur geht die Saeule hoch. Der Kern darf beliebig heiss werden. */
    public double meltingPoint = 1000D;
    /** Wie schnell der Kern die Huelle aufheizt. */
    public double diffusion = 0.02D;
    /** Neutronenart, mit der der Stab am besten spaltet. */
    public NType nType = NType.SLOW;
    /** Neutronenart, die der Stab freisetzt. */
    public NType rType = NType.FAST;
    /** Farbe des Stabs im Brennkanal. */
    public int colorTint = 0x304825;
    /** Ab welcher Kerntemperatur der Temperaturkoeffizient greift. */
    public double heatCoeffStart = 0D;
    /** Wie viel Kerntemperatur danach noetig ist, bis die Reaktion auf null faellt. */
    public double heatCoeffLength = 0D;

    /**
     * Das Pellet, in das dieser Stab beim Zerlegen zerfaellt. Bleibt leer, wenn es fuer den
     * Stab keins gibt -- dann laesst er sich auch nicht zerlegen.
     */
    public Supplier<Item> pellet;

    public RBMKRodItem(String fullName, Properties properties) {
        super(properties.stacksTo(1));
        this.fullName = fullName;
    }

    public RBMKRodItem setPellet(Supplier<Item> pellet) { this.pellet = pellet; return this; }
    public RBMKRodItem setTint(int tint) { this.colorTint = tint; return this; }
    public RBMKRodItem setYield(double yield) { this.yield = yield; return this; }
    public RBMKRodItem setStats(double funcEnd) { return setStats(funcEnd, 0); }

    public RBMKRodItem setStats(double funcEnd, double selfRate) {
        this.reactivity = funcEnd;
        this.selfRate = selfRate;
        return this;
    }

    public RBMKRodItem setFunction(EnumBurnFunc func) { this.function = func; return this; }
    public RBMKRodItem setDepletionFunction(EnumDepleteFunc func) { this.depFunc = func; return this; }

    public RBMKRodItem setHeatCoeff(double start, double length) {
        this.heatCoeffStart = start;
        this.heatCoeffLength = length;
        return this;
    }

    public RBMKRodItem setXenon(double gen, double burn) {
        this.xGen = gen;
        this.xBurn = burn;
        return this;
    }

    public RBMKRodItem setHeat(double heat) { this.heat = heat; return this; }
    public RBMKRodItem setDiffusion(double diffusion) { this.diffusion = diffusion; return this; }
    public RBMKRodItem setMeltingPoint(double meltingPoint) { this.meltingPoint = meltingPoint; return this; }

    public RBMKRodItem setNeutronTypes(NType nType, NType rType) {
        this.nType = nType;
        this.rType = rType;
        return this;
    }

    /**
     * Rechnet den eingehenden Fluss ueber die Xenonvergiftung, erzeugt und verbrennt Xenon,
     * bestimmt daraus den ausgehenden Fluss und zieht den Abbrand ab.
     */
    public double burn(Level level, ItemStack stack, double inFlux) {

        inFlux += selfRate;

        RBMKFuelData data = getData(stack);

        if(RBMKDials.getXenon(level)) {

            double xenon = data.xenon();
            xenon -= xenonBurnFunc(inFlux);

            inFlux *= (1D - data.xenon() / 100D);

            xenon += xenonGenFunc(inFlux);

            if(xenon < 0D) xenon = 0D;
            if(xenon > 100D) xenon = 100D;

            data = data.withXenon(xenon);
        }

        double mult = 1D;
        double coreHeat = data.coreHeat();

        if(this.heatCoeffStart != 0 && coreHeat >= this.heatCoeffStart) {
            double prog = (coreHeat - this.heatCoeffStart) / this.heatCoeffLength;
            if(prog > 1) prog = 1;
            mult = Math.sin((prog * Math.PI + Math.PI) / 2);
        }

        double enrichment = data.yield() / this.yield;
        double outFlux = reactivityFunc(inFlux, enrichment * mult) * RBMKDials.getReactivityMod(level);

        if(RBMKDials.getDepletion(level)) {
            double y = data.yield() - inFlux;
            if(y < 0D) y = 0D;
            data = data.withYield(y);
        }

        coreHeat += outFlux * heat;
        setData(stack, data.withCoreHeat(rectify(coreHeat)));

        return outFlux;
    }

    private double rectify(double num) {
        if(num > 1_000_000D) num = 1_000_000D;
        if(num < 20D || Double.isNaN(num)) num = 20D;
        return num;
    }

    /** Heizt den Kern und schiebt einen Teil der Waerme in die Huelle. */
    public void updateHeat(Level level, ItemStack stack, double mod) {

        RBMKFuelData data = getData(stack);

        double coreHeat = data.coreHeat();
        double hullHeat = data.hullHeat();

        if(coreHeat > hullHeat) {

            double mid = (coreHeat - hullHeat) / 2D;
            double step = mid * this.diffusion * RBMKDials.getFuelDiffusionMod(level) * mod;

            coreHeat -= step;
            hullHeat += step;

            setData(stack, data.withCoreHeat(rectify(coreHeat)).withHullHeat(rectify(hullHeat)));
        }
    }

    /**
     * Gibt die Waerme eines Ticks an die Saeule ab und kuehlt dabei die Huelle.
     * Schmilzt die Huelle, wird alles sofort ausgeglichen -- fuer die Saeule ist das toedlich.
     */
    public double provideHeat(Level level, ItemStack stack, double heat, double mod) {

        RBMKFuelData data = getData(stack);
        double hullHeat = data.hullHeat();

        if(hullHeat > this.meltingPoint) {
            double avg = (heat + hullHeat + data.coreHeat()) / 3D;
            setData(stack, data.withCoreHeat(avg).withHullHeat(avg));
            return avg - heat;
        }

        if(hullHeat <= heat) return 0;

        double ret = (hullHeat - heat) / 2;
        ret *= RBMKDials.getFuelHeatProvision(level) * mod;

        setData(stack, data.withHullHeat(hullHeat - ret));

        return ret;
    }

    public enum EnumBurnFunc {
        /** konstant, ohne eigene Reaktivitaet */
        PASSIVE(ChatFormatting.DARK_GREEN + "SAFE / PASSIVE"),
        LOG_TEN(ChatFormatting.YELLOW + "MEDIUM / LOGARITHMIC"),
        PLATEU(ChatFormatting.GREEN + "SAFE / EULER"),
        ARCH(ChatFormatting.RED + "DANGEROUS / NEGATIVE-QUADRATIC"),
        SIGMOID(ChatFormatting.GREEN + "SAFE / SIGMOID"),
        SQUARE_ROOT(ChatFormatting.YELLOW + "MEDIUM / SQUARE ROOT"),
        LINEAR(ChatFormatting.RED + "DANGEROUS / LINEAR"),
        QUADRATIC(ChatFormatting.RED + "DANGEROUS / QUADRATIC"),
        EXPERIMENTAL(ChatFormatting.RED + "EXPERIMENTAL / SINE SLOPE");

        public final String title;

        EnumBurnFunc(String title) {
            this.title = title;
        }
    }

    /** @param enrichment Anreicherung von 0 bis 1 */
    public double reactivityFunc(double in, double enrichment) {

        double flux = in * reactivityModByEnrichment(enrichment);

        return switch(this.function) {
            case PASSIVE -> selfRate * enrichment;
            case LOG_TEN -> Math.log10(flux + 1) * 0.5D * reactivity;
            case PLATEU -> (1 - Math.pow(Math.E, -flux / 25D)) * reactivity;
            case ARCH -> Math.max((flux - (flux * flux / 10000D)) / 100D * reactivity, 0D);
            case SIGMOID -> reactivity / (1 + Math.pow(Math.E, -(flux - 50D) / 10D));
            case SQUARE_ROOT -> Math.sqrt(flux) * reactivity / 10D;
            case LINEAR -> flux / 100D * reactivity;
            case QUADRATIC -> flux * flux / 10000D * reactivity;
            case EXPERIMENTAL -> flux * (Math.sin(flux) + 1) * reactivity;
        };
    }

    public String getFuncDescription(ItemStack stack) {

        String function = switch(this.function) {
            case PASSIVE -> ChatFormatting.RED + "" + selfRate;
            case LOG_TEN -> "log10(%1$s + 1) * 0.5 * %2$s";
            case PLATEU -> "(1 - e^(-%1$s / 25)) * %2$s";
            case ARCH -> "(%1$s - %1$s² / 10000) / 100 * %2$s [0;∞]";
            case SIGMOID -> "%2$s / (1 + e^(-(%1$s - 50) / 10))";
            case SQUARE_ROOT -> "sqrt(%1$s) * %2$s / 10";
            case LINEAR -> "%1$s / 100 * %2$s";
            case QUADRATIC -> "%1$s² / 10000 * %2$s";
            case EXPERIMENTAL -> "%1$s * (sin(%1$s) + 1) * %2$s";
        };

        double enrichment = getEnrichment(stack);
        String flux = selfRate > 0 ? "(x" + ChatFormatting.RED + " + " + selfRate + "" + ChatFormatting.WHITE + ")" : "x";

        if(enrichment < 1) {
            enrichment = reactivityModByEnrichment(enrichment);
            String enrichmentMod = ChatFormatting.YELLOW + "" + ((int) (enrichment * 1000D) / 1000D) + ChatFormatting.WHITE;
            String enrichmentPer = ChatFormatting.GOLD + " (" + ((int) (enrichment * 1000D) / 10D) + "%)";

            flux = "(" + flux + " * " + enrichmentMod + ")";
            return String.format(Locale.US, function, flux, reactivity).concat(enrichmentPer);
        }

        return String.format(Locale.US, function, flux, reactivity);
    }

    public enum EnumDepleteFunc {
        /** die alte Funktion */
        LINEAR,
        /** fuer Brutstoffe wie MEU, Hoechstwert 110 Prozent bei 28 Prozent Abbrand */
        RAISING_SLOPE,
        /** fuer starke Brutstoffe wie Th232, Hoechstwert 132 Prozent bei 64 Prozent Abbrand */
        BOOSTED_SLOPE,
        /** fuer die meisten Brennstoffe zu empfehlen, Hoechstwert knapp ueber dem Start */
        GENTLE_SLOPE,
        /** fuer Neutronenquellen im Arcade-Stil */
        STATIC
    }

    public double reactivityModByEnrichment(double enrichment) {
        return switch(this.depFunc) {
            case LINEAR -> enrichment;
            case STATIC -> 1D;
            case BOOSTED_SLOPE -> enrichment + Math.sin((enrichment - 1) * (enrichment - 1) * Math.PI);
            case RAISING_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 2D);
            case GENTLE_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 3D);
        };
    }

    /** Xenon, das pro Tick entsteht -- linear. */
    public double xenonGenFunc(double flux) {
        return flux * xGen;
    }

    /** Xenon, das pro Tick zerfaellt -- quadratisch. */
    public double xenonBurnFunc(double flux) {
        return (flux * flux) / xBurn;
    }

    /* Zugriff auf die Datenkomponente. Fehlt sie, wird ein frischer Stab angenommen. */

    public static RBMKFuelData getData(ItemStack stack) {
        RBMKFuelData data = stack.get(NtmDataComponents.RBMK_FUEL.get());
        if(data != null) return data;
        double yield = stack.getItem() instanceof RBMKRodItem rod ? rod.yield : 0D;
        return new RBMKFuelData(yield, 0D, 20D, 20D);
    }

    public static void setData(ItemStack stack, RBMKFuelData data) {
        stack.set(NtmDataComponents.RBMK_FUEL.get(), data);
    }

    public static double getYield(ItemStack stack) {
        return stack.getItem() instanceof RBMKRodItem ? getData(stack).yield() : 0D;
    }

    public static void setYield(ItemStack stack, double yield) { setData(stack, getData(stack).withYield(yield)); }

    public static double getPoison(ItemStack stack) { return getData(stack).xenon(); }
    public static void setPoison(ItemStack stack, double xenon) { setData(stack, getData(stack).withXenon(xenon)); }

    public static double getCoreHeat(ItemStack stack) { return getData(stack).coreHeat(); }
    public static void setCoreHeat(ItemStack stack, double heat) { setData(stack, getData(stack).withCoreHeat(heat)); }

    public static double getHullHeat(ItemStack stack) { return getData(stack).hullHeat(); }
    public static void setHullHeat(ItemStack stack, double heat) { setData(stack, getData(stack).withHullHeat(heat)); }

    /** @return Anreicherung von 0 bis 1 */
    public static double getEnrichment(ItemStack stack) {
        if(!(stack.getItem() instanceof RBMKRodItem rod) || rod.yield <= 0) return 0D;
        return getData(stack).yield() / rod.yield;
    }

    /** @return Xenonvergiftung von 0 bis 1 */
    public static double getPoisonLevel(ItemStack stack) {
        return getPoison(stack) / 100D;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnrichment(stack) < 1D;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13.0D * getEnrichment(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return this.colorTint;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        list.add(Component.literal(this.fullName).withStyle(ChatFormatting.ITALIC));

        if(getHullHeat(stack) >= 50 || getCoreHeat(stack) >= 50) {
            list.add(Component.translatable("desc.item.wasteCooling").withStyle(ChatFormatting.GOLD));
        }

        if(selfRate > 0 || this.function == EnumBurnFunc.SIGMOID) {
            list.add(Component.translatable("trait.rbmk.source").withStyle(ChatFormatting.RED));
        }

        list.add(Component.translatable("trait.rbmk.depletion", ((int) (((yield - getYield(stack)) / yield) * 100000D)) / 1000D + "%").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("trait.rbmk.xenon", ((int) (getPoison(stack) * 1000D) / 1000D) + "%").withStyle(ChatFormatting.DARK_PURPLE));
        list.add(Component.translatable("trait.rbmk.splitsWith", Component.translatable(nType.unlocalized)).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("trait.rbmk.splitsInto", Component.translatable(rType.unlocalized)).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("trait.rbmk.fluxFunc", getFuncDescription(stack)).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("trait.rbmk.funcType", this.function.title).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("trait.rbmk.xenonGen", "x * " + xGen).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("trait.rbmk.xenonBurn", "x² / " + xBurn).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("trait.rbmk.heat", heat + "°C").withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("trait.rbmk.diffusion", diffusion + "¹/²").withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("trait.rbmk.skinTemp", ((int) (getHullHeat(stack) * 10D) / 10D) + "°C").withStyle(ChatFormatting.RED));
        list.add(Component.translatable("trait.rbmk.coreTemp", ((int) (getCoreHeat(stack) * 10D) / 10D) + "°C").withStyle(ChatFormatting.RED));
        list.add(Component.translatable("trait.rbmk.melt", meltingPoint + "°C").withStyle(ChatFormatting.DARK_RED));
    }
}
