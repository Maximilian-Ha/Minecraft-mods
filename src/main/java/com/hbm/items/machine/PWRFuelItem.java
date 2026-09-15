package com.hbm.items.machine;

import com.hbm.items.EnumMultiItem;
import com.hbm.util.EnumUtil;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLogarithmic;
import com.hbm.util.function.Function.FunctionSqrt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemPWRFuel.
 *
 * Der Brennstoff des Druckwasserreaktors. Fuenfzehn Sorten, jede mit zwei Kennzahlen: wie viel
 * Waerme sie je Flusseinheit abgibt, und nach welcher Kurve sie auf den Fluss antwortet.
 *
 * Die Kurve ist das Entscheidende. Eine Wurzelkurve waechst ohne Grenze -- so ein Brennstoff
 * laesst sich hochfahren, bis der Kern durchgeht. Eine Logarithmuskurve flacht ab und faengt
 * sich von selbst. Darum steht in der Beschreibung nicht nur die Formel, sondern auch, wie
 * gefaehrlich sie ist.
 *
 * FEHLER DES ORIGINALS BEHOBEN: der dreiarmige Konstruktor nimmt dort eine Ausbeute entgegen und
 * schreibt sie nicht weg -- das Feld behaelt seinen Vorgabewert. Die beiden Balefire-Sorten
 * sollten mit 250 Millionen ein Viertel der ueblichen Ausbeute haben und hatten in Wahrheit die
 * volle Milliarde. Hier wird der Wert zugewiesen.
 */
public class PWRFuelItem extends EnumMultiItem {

    public PWRFuelItem(Properties properties) {
        super(properties, EnumPWRFuel.class, true, true);
    }

    public enum EnumPWRFuel {

        MEU(         5.0D, new FunctionLogarithmic(20 * 30).withDiv(2_500)),
        HEU233(      7.5D, new FunctionSqrt(25)),
        HEU235(      7.5D, new FunctionSqrt(22.5)),
        MEN(         7.5D, new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
        HEN237(      7.5D, new FunctionSqrt(27.5)),
        MOX(         7.5D, new FunctionLogarithmic(20 * 30).withDiv(2_500)),
        MEP(         7.5D, new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
        HEP239(     10.0D, new FunctionSqrt(22.5)),
        HEP241(     10.0D, new FunctionSqrt(25)),
        MEA(         7.5D, new FunctionLogarithmic(25 * 30).withDiv(2_500)),
        HEA242(     10.0D, new FunctionSqrt(25)),
        HES326(     12.5D, new FunctionSqrt(27.5)),
        HES327(     12.5D, new FunctionSqrt(30)),
        BFB_AM_MIX(  2.5D, new FunctionSqrt(15), 250_000_000),
        BFB_PU241(   2.5D, new FunctionSqrt(15), 250_000_000);

        /** Gesamtausbeute, bis der Stab durch ist. */
        public final double yield;
        /** Waerme je Flusseinheit. */
        public final double heatEmission;
        public final Function function;

        EnumPWRFuel(double heatEmission, Function function, double yield) {
            this.heatEmission = heatEmission;
            this.function = function;
            this.yield = yield;
        }

        EnumPWRFuel(double heatEmission, Function function) {
            this(heatEmission, function, 1_000_000_000);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        EnumPWRFuel num = EnumUtil.grabEnumSafely(EnumPWRFuel.class, com.hbm.inventory.MetaHelper.getMeta(stack));

        list.add(Component.translatable("desc.item.pwrFuel.heat", num.heatEmission).withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("desc.item.pwrFuel.function", num.function.getLabelForFuel()).withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("desc.item.pwrFuel.type").withStyle(ChatFormatting.GOLD)
                .append(num.function.getDangerFromFuel()));

        super.appendHoverText(stack, context, list, flag);
    }
}
