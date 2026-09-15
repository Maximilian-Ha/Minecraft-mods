package com.hbm.hazard.modifier;

import com.hbm.items.machine.ZirnoxRodItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.hazard.modifier.HazardModifierFuelRadiation.
 *
 * Je weiter ein Brennstoff abgebrannt ist, desto naeher rueckt seine Strahlung an den Zielwert
 * heran. Die Kurve steigt anfangs steil (Exponent 0,4), weil die kurzlebigen Spaltprodukte
 * frueh entstehen.
 */
public class HazardModifierFuelRadiation extends HazardModifier {

    private final float target;

    public HazardModifierFuelRadiation(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        double depletion = Math.pow(getDepletion(stack), 0.4D);
        return (float) (level + (this.target - level) * depletion);
    }

    /**
     * Wie weit der Stapel verbraucht ist, zwischen null und eins.
     *
     * ABWEICHUNG: das Original liest getDurabilityForDisplay, das es in 1.21 nicht mehr gibt.
     * Der Ersatz ist der Fortschrittsbalken -- er zaehlt andersherum (dreizehn heisst voll) und
     * ist auf vierzehn Stufen gerastert. Der ZIRNOX-Stab fuehrt seine Restlaufzeit als Zahl mit,
     * dort wird ungerastert gerechnet.
     */
    protected static double getDepletion(ItemStack stack) {

        if(stack.getItem() instanceof ZirnoxRodItem) {
            int max = ZirnoxRodItem.getMaxLifeTime(stack);
            if(max <= 0) return 0D;
            return Mth.clamp((double) ZirnoxRodItem.getLifeTime(stack) / (double) max, 0D, 1D);
        }

        if(!stack.getItem().isBarVisible(stack)) return 0D;
        return 1D - stack.getItem().getBarWidth(stack) / 13D;
    }
}
