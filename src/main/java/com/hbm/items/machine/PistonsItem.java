package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.EnumMultiItem;
import com.hbm.util.EnumUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemPistons.
 *
 * Kolbensatz fuer den Verbrennungsmotor. Jeder Untertyp hat ein eigenes
 * Wirkungsgrad-Array ueber die Treibstoffgueten (FuelGrade), unveraendert
 * aus dem Original uebernommen.
 *
 * Untertypen laufen im Port ueber NtmDataComponents.META bzw. MetaHelper --
 * das Gegenstueck zu ItemEnumMulti heisst hier EnumMultiItem und wird wie bei
 * ZirnoxRodItem mit (multiName, multiTexture) = (true, true) benutzt.
 */
public class PistonsItem extends EnumMultiItem {

    public PistonsItem(Properties properties) {
        super(properties, PistonType.class, true, true);
    }

    /** Wirkungsgrad des Kolbensatzes aus dem Stack fuer die gegebene Treibstoffguete. */
    public static double getEfficiency(ItemStack stack, FuelGrade grade) {
        PistonType type = EnumUtil.grabEnumSafely(PistonType.class, MetaHelper.getMeta(stack));
        return type.getEfficiency(grade);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        PistonType type = EnumUtil.grabEnumSafely(PistonType.class, MetaHelper.getMeta(stack));

        components.add(Component.translatable("item.hbmsntm.piston_set.desc").withStyle(ChatFormatting.YELLOW));

        for(int i = 0; i < type.eff.length; i++) {
            components.add(Component.literal("-").append(FuelGrade.values()[i].getLocalizedName()).append(": ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal((int) (type.eff[i] * 100) + "%").withStyle(ChatFormatting.RED)));
        }
    }

    public enum PistonType {

        STEEL       (1.00D, 0.75D, 0.25D, 0.00D, 0.00D),
        DURA        (0.50D, 1.00D, 0.90D, 0.50D, 0.00D),
        DESH        (0.00D, 0.50D, 1.00D, 0.75D, 0.00D),
        STARMETAL   (0.50D, 0.75D, 1.00D, 0.90D, 0.50D);

        /** Wirkungsgrad in der Reihenfolge der FuelGrade-Konstanten. */
        public final double[] eff;

        PistonType(double... eff) {
            this.eff = new double[Math.min(FuelGrade.values().length, eff.length)];
            System.arraycopy(eff, 0, this.eff, 0, this.eff.length);
        }

        public double getEfficiency(FuelGrade grade) {
            if(grade == null) return 0D;
            int index = grade.ordinal();
            if(index >= this.eff.length) return 0D;
            return this.eff[index];
        }
    }
}
