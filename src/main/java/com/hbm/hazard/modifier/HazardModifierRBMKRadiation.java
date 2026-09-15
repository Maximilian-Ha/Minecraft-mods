package com.hbm.hazard.modifier;

import com.hbm.hazard.HazardRegistry;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.machine.RBMKPelletItem;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.hazard.modifier.HazardModifierRBMKRadiation.
 *
 * Die Strahlung eines RBMK-Brennstabs oder -Pellets. Sie steigt mit dem Abbrand und, sofern der
 * Stab Xenon ansammelt, zusaetzlich mit dem Xenongehalt.
 *
 * Nicht linear, sofern nicht ausdruecklich gewuenscht: die Strahlung steigt schneller als der
 * Abbrand, weil die kurzlebigen Spaltprodukte frueh entstehen -- so steht es im Original.
 */
public class HazardModifierRBMKRadiation extends HazardModifier {

    private final float target;
    private final boolean linear;

    public HazardModifierRBMKRadiation(float target, boolean linear) {
        this.target = target;
        this.linear = linear;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {

        if(stack.getItem() instanceof RBMKRodItem) {

            double enrichment = RBMKRodItem.getEnrichment(stack);
            double depletion = this.linear ? 1D - enrichment : 1D - Math.pow(enrichment, 2);

            level = (float) (level + (this.target - level) * depletion);
            level += HazardRegistry.xe135 * RBMKRodItem.getPoisonLevel(stack);

        } else if(stack.getItem() instanceof RBMKPelletItem) {

            int meta = RBMKPelletItem.rectify(MetaHelper.getMeta(stack));

            // Die Einerstelle modulo fuenf ist die Abbrandstufe, vier ist ganz abgebrannt.
            level = level + (this.target - level) * ((meta % 5) / 4F);

            if(RBMKPelletItem.hasXenon(meta)) level += HazardRegistry.xe135 * HazardRegistry.nugget;
        }

        return level;
    }
}
