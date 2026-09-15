package com.hbm.items.machine;

import com.hbm.items.EnumMultiItem;
import com.hbm.items.ISatChip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemOrbitalAssembly.
 *
 * Die Bauteile, die nur im Orbit entstehen koennen. Bisher gibt es genau eines: den
 * Kristallschaltkreis, das teuerste Erzeugnis der Praezisionsmontage.
 *
 * ER TRAEGT EINE FREQUENZ wie ein Satellitenchip -- im Original ist er der Schluessel zum
 * Weltraumbau. Die Frequenz wird hier mitgefuehrt und angezeigt; wer sie spaeter braucht, findet
 * sie vor.
 */
public class OrbitalAssemblyItem extends EnumMultiItem implements ISatChip {

    public OrbitalAssemblyItem(Properties properties) {
        super(properties, EnumOrbitalAssembly.class, true, false);
    }

    public enum EnumOrbitalAssembly {
        CRYSTAL_CIRCUIT
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.hbmsntm.obj_sat_chip.frequency", this.getFreq(stack)).withStyle(ChatFormatting.AQUA));
    }
}
