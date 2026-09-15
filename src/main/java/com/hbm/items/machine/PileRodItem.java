package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemPileRodMK2.
 *
 * Die Staebe des Chicago Pile. Neun Sorten, drei Aufgaben:
 *
 * - Die beiden Neutronenquellen (Radium-Beryllium und Polonium-Beryllium) geben eine
 *   Neutroneneinheit je Takt ab, ohne Rueckkopplung und ohne sich abzunutzen. Ohne sie faengt
 *   nichts an.
 * - Zirkonium tut gar nichts und ist der Platzhalter, in den sich alles Abgebrannte verwandelt.
 * - Der Rest brennt: aus dem einlaufenden Fluss wird ueber eine Wurzel neuer Fluss, dazu Waerme.
 *   Ist die Lebensdauer aufgebraucht, wird der Stab zu dem, was in seinem "turnsInto" steht.
 *
 * Die Wurzelkennlinie ist der Grund, warum der Pile sich nicht von selbst aufschaukelt: der
 * Zugewinn faellt mit steigendem Fluss. Gefaehrlich wird er trotzdem -- ueber die Waerme.
 */
public class PileRodItem extends EnumMultiItem {

    /** Schluessel, unter dem der Abbrand am Stapel haengt. Wie im Original. */
    public static final String KEY_NBT_DEPLETION = "depletion";

    public PileRodItem(Properties properties) {
        super(properties, EnumPileRod.class, true, true);
    }

    public enum EnumPileRod {

        /* 0 */ RA226BE(1D),
        /* 1 */ PO210BE(1D),
        /* 2 */ ZR(          0D,      0D, 0D,    2),
        /* 3 */ NU(          1D, 25_000D, 0.25D, 4),
        /* 4 */ PU239(       1D,    500D, 0.5D,  5),
        /* 5 */ RGP(         1D,  1_000D, 0.5D,  6),
        /* 6 */ WASTE(       1D,      0D, 1.5D,  6),
        /* 7 */ THORIUM(     1D, 35_000D, 0.25D, 8),
        /* 8 */ THORIUM_FUEL(1D,  2_000D, 0.5D,  6);

        /** Vorfaktor der Wurzelkennlinie. Null heisst: brennt nicht. */
        public final double reactionMult;
        /** Wie viel Fluss der Stab vertraegt, bis er durch ist. Null heisst: nutzt sich nie ab. */
        public final double life;
        /** Waerme je abgegebener Flusseinheit. */
        public final double heatMult;
        /** Fluss, den der Stab von sich aus abgibt -- nur die beiden Quellen tun das. */
        public final double neutronSource;
        /** Untertyp, in den sich der aufgebrauchte Stab verwandelt. */
        public final int turnsInto;

        EnumPileRod(double neutronSource) {
            this.neutronSource = neutronSource;
            this.reactionMult = 0D;
            this.life = 0D;
            this.heatMult = 0D;
            this.turnsInto = 0;
        }

        EnumPileRod(double reaction, double life, double heat, int turnsInto) {
            this.reactionMult = reaction;
            this.life = life;
            this.heatMult = heat;
            this.turnsInto = turnsInto;
            this.neutronSource = 0D;
        }
    }

    private static EnumPileRod type(ItemStack stack) {
        return EnumUtil.grabEnumSafely(EnumPileRod.class, MetaHelper.getMeta(stack));
    }

    /* --- Abbrand --- */

    public static double getDepletion(ItemStack stack) {
        return TagsUtil.getCustomData(stack).getDouble(KEY_NBT_DEPLETION);
    }

    public static void setDepletion(ItemStack stack, double depletion) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putDouble(KEY_NBT_DEPLETION, depletion);
        TagsUtil.putCustomData(stack, tag);
    }

    public static double getDepletionPercent(ItemStack stack) {
        if(stack.isEmpty()) return 0D;
        double life = type(stack).life;
        if(life <= 0) return 0D;
        return (getDepletion(stack) / life) * 100D;
    }

    /* --- Reaktion --- */

    /** Was der Stab bei diesem einlaufenden Fluss abgibt. */
    public static double getReactivity(ItemStack stack, double inFlux) {
        EnumPileRod rod = type(stack);
        double outFlux = rod.neutronSource;
        if(rod.reactionMult > 0) outFlux += BobMathUtil.squirt(inFlux) * rod.reactionMult;
        return outFlux;
    }

    public static double getHeatPerNeutron(ItemStack stack) {
        return type(stack).heatMult;
    }

    /** Nutzt den Stab ab und gibt zurueck, was danach im Kanal liegt. */
    public static ItemStack react(ItemStack stack, double inFlux) {

        EnumPileRod rod = type(stack);
        if(rod.life <= 0) return stack;

        double depletion = getDepletion(stack) + inFlux;

        if(depletion < rod.life) {
            setDepletion(stack, depletion);
            return stack;
        }

        return MetaHelper.newStack(stack.getItem(), 1, rod.turnsInto);
    }

    /* --- Anzeige --- */

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        EnumPileRod rod = type(stack);

        if(rod.life > 0) {
            list.add(Component.literal("Lifetime: " + (int) Math.round(rod.life)));
            double depletion = getDepletionPercent(stack);
            if(depletion > 0) list.add(Component.literal("Depletion: " + (int) Math.round(depletion) + "%"));
        }

        list.add(Component.translatable(this.getDescriptionId(stack) + ".desc").withStyle(ChatFormatting.YELLOW));

        super.appendHoverText(stack, context, list, flag);
    }

    @Override public boolean isBarVisible(ItemStack stack) { return getDepletionPercent(stack) > 0D; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13F * (1F - (float) (getDepletionPercent(stack) / 100D)));
    }
}
