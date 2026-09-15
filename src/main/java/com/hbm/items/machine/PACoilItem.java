package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.util.EnumUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemPACoil.
 *
 * Die Spulen der Magnete. Sie sind das eine Bauteil, das der Spieler am Beschleuniger wirklich
 * auswaehlen muss -- und die Wahl entscheidet, wie schnell der Strahl werden darf.
 *
 * JEDE SPULE HAT EIN FENSTER. Darunter zieht die Maschine das Zehnfache an Strom, darueber
 * zerreisst es den Strahl. Gold traegt bis 2.200, Niob bis 8.400, BSCCO bis 15.000, Chlorophyt
 * bis 75.000 -- und wer das Digamma-Teilchen will, braucht 70.000.
 *
 * DIPOLE VERLANGEN AUSSERDEM EINE MINDESTKANTENLAENGE: fuenfzehn Bloecke bei Gold, einundfuenfzig
 * bei Chlorophyt. Eine bessere Spule macht den Ring also nicht kleiner, sondern groesser.
 *
 * TREFFEN BEIDE STRAFEN ZUSAMMEN -- zu langsam UND zu enge Kurve --, stuerzt das Teilchen im
 * Dipol ab. Das Hundertfache an Strom gibt es nicht.
 */
public class PACoilItem extends EnumMultiItem {

    public PACoilItem(Properties properties) {
        super(properties.stacksTo(1), EnumCoilType.class, true, true);
    }

    public enum EnumCoilType {
        GOLD(0, 2_200, 0, 2_200, 15),
        NIOBIUM(1_500, 8_400, 1_500, 8_400, 21),
        BSCCO(7_500, 15_000, 7_500, 15_000, 27),
        CHLOROPHYTE(14_500, 75_000, 14_500, 75_000, 51);

        public final int quadMin;
        public final int quadMax;
        public final int diMin;
        public final int diMax;
        public final int diDistMin;

        EnumCoilType(int quadMin, int quadMax, int diMin, int diMax, int diDistMin) {
            this.quadMin = quadMin;
            this.quadMax = quadMax;
            this.diMin = diMin;
            this.diMax = diMax;
            this.diDistMin = diDistMin;
        }
    }

    /** Die Spule aus einem Fach, oder null, wenn dort keine liegt. */
    public static EnumCoilType getCoil(ItemStack stack) {
        if(stack.isEmpty() || !(stack.getItem() instanceof PACoilItem)) return null;
        return EnumUtil.grabEnumSafely(EnumCoilType.class, MetaHelper.getMeta(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        EnumCoilType type = EnumUtil.grabEnumSafely(EnumCoilType.class, MetaHelper.getMeta(stack));

        components.add(Component.literal("Quadrupole operational range: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(String.format(Locale.US, "%,d", type.quadMin) + " - " + String.format(Locale.US, "%,d", type.quadMax)).withStyle(ChatFormatting.WHITE)));
        components.add(Component.literal("Dipole operational range: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(String.format(Locale.US, "%,d", type.diMin) + " - " + String.format(Locale.US, "%,d", type.diMax)).withStyle(ChatFormatting.WHITE)));
        components.add(Component.literal("Dipole minimum side length: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal("" + type.diDistMin).withStyle(ChatFormatting.WHITE)));
        components.add(Component.literal("Minimums not met result in a power draw penalty!").withStyle(ChatFormatting.RED));
        components.add(Component.literal("Maximums exceeded result in the particle crashing!").withStyle(ChatFormatting.RED));
        components.add(Component.literal("Particles will crash in dipoles if both penalties take effect!").withStyle(ChatFormatting.RED));
    }
}
