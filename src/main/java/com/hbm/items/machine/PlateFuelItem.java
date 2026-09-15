package com.hbm.items.machine;

import com.hbm.util.BobMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemPlateFuel.
 *
 * Die Brennstoffplatte des Forschungsreaktors. Sie nimmt den Neutronenfluss ihrer Nachbarn
 * entgegen und gibt selbst welchen ab -- nach welcher Kennlinie, entscheidet die Sorte:
 *
 *   LOGARITHM          saettigt frueh; viel Fluss bringt wenig mehr
 *   SQUARE_ROOT        saettigt spaeter
 *   NEGATIVE_QUADRATIC steigt bis 5.000 Fluss und faellt danach wieder ab -- zu viel Fluss
 *                      erstickt die Reaktion
 *   LINEAR             haelt, was man hineinsteckt
 *   PASSIVE            gibt immer gleich viel ab und braucht gar keinen Fluss; das sind die
 *                      beiden Neutronenquellen, mit denen sich der Reaktor ueberhaupt erst
 *                      anwerfen laesst
 *
 * ABWEICHUNG: das Original ist @Deprecated und steht in keinem Kreativreiter; die Platten haben
 * kein einziges Rezept. Der Port uebernimmt beides: die Platten stehen im Kreativreiter, weil sie
 * sonst totes Inventar waeren, bekommen aber weiterhin kein Rezept.
 */
public class PlateFuelItem extends FuelRodItem {

    public final FunctionType function;
    public final int reactivity;

    public PlateFuelItem(int lifeTime, FunctionType function, int reactivity, Properties properties) {
        super(lifeTime, properties);
        this.function = function;
        this.reactivity = reactivity;
    }

    public enum FunctionType {
        LOGARITHM,
        SQUARE_ROOT,
        NEGATIVE_QUADRATIC,
        LINEAR,
        PASSIVE
    }

    public String getFunctionDesc() {
        return switch(this.function) {
            case LOGARITHM -> "f(x) = log10(x + 1) * 0.5 * " + this.reactivity;
            case SQUARE_ROOT -> "f(x) = sqrt(x) * " + this.reactivity + " / 10";
            case NEGATIVE_QUADRATIC -> "f(x) = [x - (x^2 / 10000)] / 100 * " + this.reactivity;
            case LINEAR -> "f(x) = x / 100 * " + this.reactivity;
            case PASSIVE -> "f(x) = " + this.reactivity;
        };
    }

    /**
     * Ein Reaktionsschritt. Gibt zurueck, wie viel Fluss die Platte abgibt, und zaehlt den
     * Verbrauch hoch -- bei den passiven Quellen um ihre eigene Leistung, sonst um den Fluss,
     * den sie abbekommen haben.
     */
    public int react(ItemStack stack, int flux) {

        if(this.function != FunctionType.PASSIVE) {
            setLifeTime(stack, getLifeTime(stack) + flux);
        }

        return switch(this.function) {
            case LOGARITHM -> (int) (Math.log10(flux + 1) * 0.5D * this.reactivity);
            case SQUARE_ROOT -> (int) (Math.sqrt(flux) * this.reactivity / 10D);
            case NEGATIVE_QUADRATIC -> (int) Math.max((flux - (flux * (double) flux / 10000D)) / 100D * this.reactivity, 0D);
            case LINEAR -> (int) (flux / 100D * this.reactivity);
            case PASSIVE -> {
                setLifeTime(stack, getLifeTime(stack) + this.reactivity);
                yield this.reactivity;
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("[Research Reactor Plate Fuel]").withStyle(ChatFormatting.YELLOW));
        components.add(Component.literal("   " + this.getFunctionDesc()).withStyle(ChatFormatting.DARK_AQUA));
        components.add(Component.literal("   Yield of " + BobMathUtil.getShortNumber(this.lifeTime) + " events").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, components, flag);
    }
}
