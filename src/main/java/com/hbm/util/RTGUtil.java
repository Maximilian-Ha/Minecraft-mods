package com.hbm.util;

import com.hbm.config.VersatileConfig;
import com.hbm.interfaces.HalfLifeType;
import com.hbm.items.machine.RTGPelletItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.util.RTGUtil.
 *
 * Rechnet die Gesamtwaerme aller eingelegten Pellets zusammen und laesst sie dabei
 * altern. Statt des ItemStack-Arrays aus 1.7.10 kommt hier die NonNullList des
 * BlockEntity herein; "null" im Original entspricht ItemStack.EMPTY.
 */
public class RTGUtil {

    /** Leistung eines einzelnen Pellets, je nach Config skaliert oder nicht. */
    public static short getPower(ItemStack stack) {
        return VersatileConfig.scaleRTGPower() ? RTGPelletItem.getScaledPower(stack) : RTGPelletItem.getHeat(stack);
    }

    /** Liegt ueberhaupt ein Pellet in einem der RTG-Slots? */
    public static boolean hasHeat(NonNullList<ItemStack> inventory, int[] rtgSlots) {
        for(int slot : rtgSlots) {

            ItemStack stack = inventory.get(slot);

            if(stack.isEmpty()) continue;

            if(stack.getItem() instanceof RTGPelletItem) return true;
        }

        return false;
    }

    /** Summiert die Waerme aller Pellets und laesst jedes davon um einen Tick altern. */
    public static int updateRTGs(NonNullList<ItemStack> inventory, int[] rtgSlots) {
        int newHeat = 0;

        for(int slot : rtgSlots) {

            ItemStack stack = inventory.get(slot);

            if(stack.isEmpty()) continue;

            if(!(stack.getItem() instanceof RTGPelletItem)) continue;

            newHeat += getPower(stack);
            inventory.set(slot, RTGPelletItem.handleDecay(stack));
        }

        return newHeat;
    }

    /**
     * Lebensdauer eines RTG-Pellets aus seiner Halbwertszeit.
     *
     * @author UFFR
     * @param halfLife  die Halbwertszeit
     * @param type      Einheit der Halbwertszeit, siehe {@link HalfLifeType}
     * @param realYears true rechnet mit 365 Tagen pro Jahr statt mit 100
     * @return die Halbwertszeit in Minecraft-Ticks
     */
    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        float life = 0;

        switch(type) {
            case LONG -> life = (48000 * (realYears ? 365 : 100) * 100) * halfLife;
            case MEDIUM -> life = (48000 * (realYears ? 365 : 100)) * halfLife;
            case SHORT -> life = 48000 * halfLife;
        }

        return (long) life;
    }
}
