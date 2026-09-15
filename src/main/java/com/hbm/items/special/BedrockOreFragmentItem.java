package com.hbm.items.special;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.IMetaItem;
import com.hbm.items.NtmItems;
import com.hbm.items.component.NtmDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Portiert aus 1.7.10: das ItemAutogen(MaterialShapes.FRAGMENT) aus ModItems, dort
 * "bedrock_ore_fragment".
 *
 * EIN Gegenstand fuer alle Materialien -- der Zahlenwert ist die Materialnummer aus Mats. Das
 * Original erzeugt daraus zur Laufzeit ein Bild je Material; der Port faerbt eine Graustufen-
 * vorlage ueber einen Farbgeber ein, wie schon beim Grundgesteinserz.
 *
 * ACHT BRUCHSTUECKE SIND EIN BARREN (MaterialShapes.FRAGMENT hat die Menge 8, dieselbe wie ein
 * Nugget) -- daher zaehlt der Schlaemmer in Bruchstuecken und nicht in Barren.
 */
public class BedrockOreFragmentItem extends Item implements IMetaItem {

    public BedrockOreFragmentItem(Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
    }

    public static ItemStack make(NTMMaterial mat, int amount) {
        return MetaHelper.newStack(NtmItems.BEDROCK_ORE_FRAGMENT.get(), amount, mat.id);
    }

    /** Das Material zu einem Stapel -- null, wenn die Nummer zu keinem gehoert. */
    public static NTMMaterial getMaterial(ItemStack stack) {
        return Mats.matById.get(MetaHelper.getMeta(stack));
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(NTMMaterial mat : Mats.orderedList) {
            if(mat.autogen.contains(MaterialShapes.FRAGMENT)) stacks.add(MetaHelper.newStack(item, 1, mat.id));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        NTMMaterial mat = getMaterial(stack);
        if(mat == null) return super.getName(stack);
        return Component.translatable(this.getDescriptionId() + ".named", mat.getName());
    }
}
