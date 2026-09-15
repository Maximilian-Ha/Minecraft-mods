package com.hbm.items.special;

import com.hbm.items.EnumMultiItem;
import com.hbm.items.ISatChip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SatelliteItem extends EnumMultiItem implements ISatChip {

    public SatelliteItem(Properties properties) {
        super(properties, SatType.class, true, true);
    }

    public enum SatType {
        SPY,
        SCANNER,
        RADAR,
        MINER_ASTRO,
        MINER_LUNAR,
        PRECISION_LASER,
        DEATH_RAY,
        XENIUM_RESONATOR,
        RELAY,
        DETECTOR,
        /* ABWEICHUNG, seit Runde 123: im Original steht zwischen DETECTOR und SCIENCE noch
         * RAY_SCAN. Den Satelliten gibt es im Port nicht; ein Platzhalter waere eine
         * Gegenstandsart, die nichts tut. Ab hier laufen die Stellenwerte des Ports und die des
         * Originals also auseinander -- gespeicherte Staende teilen die beiden ohnehin nicht. */
        SCIENCE
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.hbmsntm.obj_sat_chip.frequency", this.getFreq(stack)).withStyle(ChatFormatting.GRAY));
    }
}
