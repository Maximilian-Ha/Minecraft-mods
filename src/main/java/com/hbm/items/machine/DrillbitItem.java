package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.util.EnumUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemDrillbit.
 *
 * Der Bohrkopf des Baggers. Zehn Sorten, in fuenf Stufen zu je zwei: ohne und mit Diamant.
 *
 * DIE STUFE ENTSCHEIDET, AN WELCHE ERZE DER BAGGER HERANKOMMT -- ein Erz der Stufe drei bleibt
 * einem Stahlbohrer verschlossen. Der Glueckszuschlag erhoeht die Ausbeute der Rohprobe, die
 * Geschwindigkeit das Tempo des Bohrens.
 *
 * ADERNBAU UND SEIDENBERUEHRUNG sind im Original Merkmale der besseren Koepfe; der Bagger des
 * Ports wertet bisher nur Stufe, Tempo und Glueck aus. Die Felder stehen trotzdem hier, weil sie
 * die Anzeige tragen und der Bagger sie spaeter braucht.
 */
public class DrillbitItem extends EnumMultiItem {

    public DrillbitItem(Properties properties) {
        super(properties, EnumDrillType.class, true, true);
    }

    public enum EnumDrillType {
        STEEL           (1.0D, 1, 0, false, false),
        STEEL_DIAMOND   (1.0D, 1, 2, false, true),
        HSS             (1.2D, 2, 0, true,  false),
        HSS_DIAMOND     (1.2D, 2, 3, true,  true),
        DESH            (1.5D, 3, 1, true,  true),
        DESH_DIAMOND    (1.5D, 3, 4, true,  true),
        TCALLOY         (2.0D, 4, 1, true,  true),
        TCALLOY_DIAMOND (2.0D, 4, 4, true,  true),
        FERRO           (2.5D, 5, 1, true,  true),
        FERRO_DIAMOND   (2.5D, 5, 4, true,  true);

        public final double speed;
        public final int tier;
        public final int fortune;
        public final boolean vein;
        public final boolean silk;

        EnumDrillType(double speed, int tier, int fortune, boolean vein, boolean silk) {
            this.speed = speed;
            this.tier = tier;
            this.fortune = fortune;
            this.vein = vein;
            this.silk = silk;
        }
    }

    /** Der Bohrkopf zu einem Stapel -- null, wenn es keiner ist. */
    public static EnumDrillType getType(ItemStack stack) {
        if(!(stack.getItem() instanceof DrillbitItem)) return null;
        return EnumUtil.grabEnumSafely(EnumDrillType.class, MetaHelper.getMeta(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        EnumDrillType type = getType(stack);
        if(type == null) return;

        components.add(Component.translatable("desc.item.drillbit.speed", (int) (type.speed * 100)).withStyle(ChatFormatting.YELLOW));
        components.add(Component.translatable("desc.item.drillbit.tier", type.tier).withStyle(ChatFormatting.YELLOW));
        if(type.fortune > 0) components.add(Component.translatable("desc.item.drillbit.fortune", type.fortune).withStyle(ChatFormatting.LIGHT_PURPLE));
        if(type.vein) components.add(Component.translatable("desc.item.drillbit.vein").withStyle(ChatFormatting.GREEN));
        if(type.silk) components.add(Component.translatable("desc.item.drillbit.silk").withStyle(ChatFormatting.GREEN));
    }
}
