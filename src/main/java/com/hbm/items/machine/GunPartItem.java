package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.IMetaItem;
import com.hbm.items.component.NtmDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Portiert aus 1.7.10: die sieben ItemAutogen(...) aus ModItems -- part_barrel_light,
 * part_barrel_heavy, part_receiver_light, part_receiver_heavy, part_mechanism, part_stock
 * und part_grip.
 *
 * EIN Gegenstand je Bauteil, alle Materialien in den Metadaten -- dieselbe Bauart wie das
 * Grundgesteinsbruchstueck, und die Zahl IST die Materialnummer aus Mats. Welche Materialien
 * ein Bauteil zulaesst, steht nicht hier, sondern am Material: Mats traegt fuer jedes ein
 * setAutogen(...), und diese Liste war im Port schon vollstaendig, lange bevor es die
 * Gegenstaende gab.
 *
 * WARUM DAS WICHTIG IST: an diesen sieben Bauteilen haengt JEDER Waffenbauplan des Originals.
 * Solange sie fehlten, war im Port keine einzige Waffe baubar -- nachgemessen ueber
 * NtmRecipeProvider, der null NtmItems.GUN_ nennt.
 */
public class GunPartItem extends Item implements IMetaItem {

    /** Welche Form dieses Bauteil ist. Entscheidet, welche Materialien es gibt. */
    public final MaterialShapes shape;

    public GunPartItem(Properties properties, MaterialShapes shape) {
        super(properties.component(NtmDataComponents.META.get(), 0));
        this.shape = shape;
    }

    /** Das Material zu einem Stapel -- null, wenn die Nummer zu keinem gehoert. */
    public static NTMMaterial getMaterial(ItemStack stack) {
        return Mats.matById.get(MetaHelper.getMeta(stack));
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(NTMMaterial mat : Mats.orderedList) {
            if(mat.autogen.contains(this.shape)) stacks.add(MetaHelper.newStack(item, 1, mat.id));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        NTMMaterial mat = getMaterial(stack);
        if(mat == null) return super.getName(stack);
        return Component.translatable(this.getDescriptionId() + ".named", mat.getName());
    }
}
