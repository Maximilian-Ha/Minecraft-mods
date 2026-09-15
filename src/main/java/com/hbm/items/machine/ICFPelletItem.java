package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemICFPellet.
 *
 * Das Brennstoffkuegelchen der Traegheitsfusion. Es traegt ZWEI Stoffe, und aus deren Paarung
 * ergibt sich alles Weitere:
 *
 * - reactionMult: wie viel Waerme je eingestrahlter Einheit herauskommt. Die beiden Faktoren
 *   werden multipliziert -- Kohlenstoff mit Kohlenstoff gaebe das Vierfache.
 * - depletionSpeed: wie schnell sich das Kuegelchen verbraucht. Auch hier multiplizieren sich
 *   die beiden; Bor haelt doppelt so lange wie sonst etwas.
 * - fusingDifficulty: wie stark der Laser sein muss, damit ueberhaupt etwas zuendet. Auch das
 *   ein Produkt -- Kalzium mit Kalzium braucht das Neunzigfache von Wasserstoff mit Wasserstoff.
 *
 * Das ist die ganze Abwaegung des Geraets: schwere Kerne geben mehr her und halten laenger,
 * verlangen aber einen Laser, den man erst einmal bauen muss.
 *
 * Myonenkatalyse viertelt die noetige Laserstaerke. Sie steckt als Kennzeichen im Kuegelchen.
 *
 * ABWEICHUNG: das Original zeichnet zwei Durchgaenge -- einen eingefaerbten Hintergrund und das
 * Symbol darueber. Auf 1.21 ist das ein Modell mit zwei Lagen; die untere traegt die gemischte
 * Farbe der beiden Stoffe, die obere bleibt ungefaerbt. Das Ergebnis ist dasselbe.
 */
public class ICFPelletItem extends Item {

    /** Grundausbeute, bevor die beiden Abbrandfaktoren sie teilen. */
    public static final long BASE_DEPLETION = 50_000_000_000L;
    /** Grundschwelle, bevor die beiden Schwierigkeitsfaktoren sie vervielfachen. */
    public static final long BASE_DIFFICULTY = 10_000_000L;

    public ICFPelletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public enum EnumICFFuel {

        HYDROGEN(  0x4040FF, 1.00D, 0.85D, 1.00D),
        DEUTERIUM( 0x2828CB, 1.25D, 1.00D, 1.00D),
        TRITIUM(   0x000092, 1.50D, 1.00D, 1.05D),
        HELIUM3(   0xFFF09F, 1.75D, 1.00D, 1.25D),
        HELIUM4(   0xFF9B60, 2.00D, 1.00D, 1.50D),
        LITHIUM(   0xE9E9E9, 1.25D, 0.85D, 2.00D),
        BERYLLIUM( 0xA79D80, 2.00D, 1.00D, 2.50D),
        BORON(     0x697F89, 3.00D, 0.50D, 3.50D),
        CARBON(    0x454545, 2.00D, 1.00D, 5.00D),
        OXYGEN(    0xB4E2FF, 1.25D, 1.50D, 7.50D),
        SODIUM(    0xDFE4E7, 3.00D, 0.75D, 8.75D),
        /* Aluminium, Silizium und Phosphor fehlen auch im Original. */
        CHLORINE(  0xDAE598, 2.50D, 1.00D, 9.25D),
        CALCIUM(   0xD2C7A9, 3.00D, 1.00D, 9.75D);
        /* Titan fehlt ebenfalls. */

        public final int color;
        public final double reactionMult;
        public final double depletionSpeed;
        public final double fusingDifficulty;

        EnumICFFuel(int color, double react, double depl, double laser) {
            this.color = color;
            this.reactionMult = react;
            this.depletionSpeed = depl;
            this.fusingDifficulty = laser;
        }
    }

    /** Welches Fluid welchen Stoff ergibt -- die Presse schlaegt hier nach. */
    public static final Map<FluidType, EnumICFFuel> FLUID_MAP = new HashMap<>();
    /** Welcher Werkstoff welchen Stoff ergibt. */
    public static final Map<NTMMaterial, EnumICFFuel> MATERIAL_MAP = new HashMap<>();

    public static void init() {

        if(!FLUID_MAP.isEmpty() && !MATERIAL_MAP.isEmpty()) return;

        FLUID_MAP.put(Fluids.HYDROGEN, EnumICFFuel.HYDROGEN);
        FLUID_MAP.put(Fluids.DEUTERIUM, EnumICFFuel.DEUTERIUM);
        FLUID_MAP.put(Fluids.TRITIUM, EnumICFFuel.TRITIUM);
        FLUID_MAP.put(Fluids.HELIUM3, EnumICFFuel.HELIUM3);
        FLUID_MAP.put(Fluids.HELIUM4, EnumICFFuel.HELIUM4);
        FLUID_MAP.put(Fluids.OXYGEN, EnumICFFuel.OXYGEN);
        FLUID_MAP.put(Fluids.CHLORINE, EnumICFFuel.CHLORINE);

        MATERIAL_MAP.put(Mats.MAT_LITHIUM, EnumICFFuel.LITHIUM);
        MATERIAL_MAP.put(Mats.MAT_BERYLLIUM, EnumICFFuel.BERYLLIUM);
        MATERIAL_MAP.put(Mats.MAT_BORON, EnumICFFuel.BORON);
        MATERIAL_MAP.put(Mats.MAT_GRAPHITE, EnumICFFuel.CARBON);
        MATERIAL_MAP.put(Mats.MAT_SODIUM, EnumICFFuel.SODIUM);
        MATERIAL_MAP.put(Mats.MAT_CALCIUM, EnumICFFuel.CALCIUM);
    }

    /* --- Was im Kuegelchen steht --- */

    public static EnumICFFuel getType(ItemStack stack, boolean first) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        String key = first ? "type1" : "type2";
        if(!tag.contains(key)) return first ? EnumICFFuel.DEUTERIUM : EnumICFFuel.TRITIUM;
        return EnumUtil.grabEnumSafely(EnumICFFuel.class, tag.getByte(key));
    }

    public static boolean isMuonCatalyzed(ItemStack stack) {
        return TagsUtil.getCustomData(stack).getBoolean("muon");
    }

    public static ItemStack setup(ItemStack stack, EnumICFFuel type1, EnumICFFuel type2, boolean muon) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putByte("type1", (byte) type1.ordinal());
        tag.putByte("type2", (byte) type2.ordinal());
        tag.putBoolean("muon", muon);
        TagsUtil.putCustomData(stack, tag);
        return stack;
    }

    /* --- Rechnung --- */

    public static long getMaxDepletion(ItemStack stack) {
        long base = BASE_DEPLETION;
        base /= getType(stack, true).depletionSpeed;
        base /= getType(stack, false).depletionSpeed;
        return base;
    }

    public static long getFusingDifficulty(ItemStack stack) {
        long base = (long) (BASE_DIFFICULTY * getType(stack, true).fusingDifficulty * getType(stack, false).fusingDifficulty);
        if(isMuonCatalyzed(stack)) base /= 4;
        return base;
    }

    public static long getDepletion(ItemStack stack) {
        return TagsUtil.getCustomData(stack).getLong("depletion");
    }

    /** Traegt die Einstrahlung ein und gibt zurueck, wie viel Waerme dabei entsteht. */
    public static long react(ItemStack stack, long heat) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putLong("depletion", tag.getLong("depletion") + heat);
        TagsUtil.putCustomData(stack, tag);
        return (long) (heat * getType(stack, true).reactionMult * getType(stack, false).reactionMult);
    }

    /** Die gemischte Farbe der beiden Stoffe -- der Farbgeber nimmt sie fuer die untere Lage. */
    public static int getMixedColor(ItemStack stack) {

        int c1 = getType(stack, true).color;
        int c2 = getType(stack, false).color;

        int r = (((c1 & 0xff0000) >> 16) + ((c2 & 0xff0000) >> 16)) / 2;
        int g = (((c1 & 0x00ff00) >> 8) + ((c2 & 0x00ff00) >> 8)) / 2;
        int b = ((c1 & 0x0000ff) + (c2 & 0x0000ff)) / 2;

        return r << 16 | g << 8 | b;
    }

    /* --- Anzeige --- */

    private static double depletionFraction(ItemStack stack) {
        long max = getMaxDepletion(stack);
        return max <= 0 ? 0D : (double) getDepletion(stack) / (double) max;
    }

    @Override public boolean isBarVisible(ItemStack stack) { return depletionFraction(stack) > 0D; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13F * (1F - (float) depletionFraction(stack)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        list.add(Component.literal("Depletion: " + String.format(Locale.US, "%.1f", depletionFraction(stack) * 100D) + "%")
                .withStyle(ChatFormatting.GREEN));

        list.add(Component.literal("Fuel: ")
                .append(Component.translatable("icffuel." + getType(stack, true).name().toLowerCase(Locale.US)))
                .append(Component.literal(" / "))
                .append(Component.translatable("icffuel." + getType(stack, false).name().toLowerCase(Locale.US)))
                .withStyle(ChatFormatting.YELLOW));

        list.add(Component.literal("Heat required: " + BobMathUtil.getShortNumber(getFusingDifficulty(stack)) + "TU")
                .withStyle(ChatFormatting.YELLOW));

        double mult = getType(stack, true).reactionMult * getType(stack, false).reactionMult;
        list.add(Component.literal("Reactivity multiplier: x" + (int) (mult * 100) / 100D).withStyle(ChatFormatting.YELLOW));

        if(isMuonCatalyzed(stack)) list.add(Component.literal("Muon catalyzed!").withStyle(ChatFormatting.DARK_AQUA));
    }
}
