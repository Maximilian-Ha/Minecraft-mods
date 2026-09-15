package com.hbm.items;

import com.hbm.inventory.MetaHelper;
import com.hbm.util.TagsUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.BrokenItem.
 *
 * Der Ausschuss. Die Praezisionsmontage geraet nicht jedes Mal; misslingt ein Stueck, faellt statt
 * des Werkstuecks das hier heraus -- und merkt sich im Datenanhang, was es haette werden sollen.
 * Zurueck in die Maschine gelegt, gibt es einen Teil der Zutaten wieder her.
 *
 * ABWEICHUNG: das Original zeichnet den zerbrochenen Gegenstand in zwei Durchgaengen -- unten das
 * Bild dessen, was er haette werden sollen, darueber das Sprungbild. Auf 1.21 gibt es die
 * Mehrfachdurchgaenge des Gegenstandsrenderers nicht mehr in dieser Form; hier steht nur das
 * Sprungbild. WAS ES HAETTE WERDEN SOLLEN, STEHT IM NAMEN -- der wird gelesen wie im Original.
 */
public class BrokenItem extends Item {

    /** Die beiden Schluessel des Datenanhangs; die Namen sind die des Originals. */
    public static final String KEY_ID = "itemID";
    public static final String KEY_META = "itemMeta";

    public BrokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {

        ItemStack original = getOriginal(stack);
        if(original.isEmpty()) return super.getName(stack);

        return Component.translatable(this.getDescriptionId() + ".prefix", original.getHoverName());
    }

    /** Was der Ausschuss haette werden sollen -- ein leerer Stapel, wenn nichts vermerkt ist. */
    public static ItemStack getOriginal(ItemStack stack) {

        if(!TagsUtil.hasCustomData(stack)) return ItemStack.EMPTY;

        CompoundTag tag = TagsUtil.getCustomData(stack);
        if(!tag.contains(KEY_ID)) return ItemStack.EMPTY;

        ResourceLocation id = ResourceLocation.tryParse(tag.getString(KEY_ID));
        if(id == null || !BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;

        return MetaHelper.newStack(BuiltInRegistries.ITEM.get(id), 1, tag.getInt(KEY_META));
    }

    public static ItemStack make(ItemStack stack) { return make(stack.getItem(), stack.getCount(), MetaHelper.getMeta(stack)); }
    public static ItemStack make(Item item) { return make(item, 1, 0); }
    public static ItemStack make(Item item, int meta) { return make(item, 1, meta); }

    public static ItemStack make(Item item, int stacksize, int meta) {

        ItemStack stack = new ItemStack(NtmItems.BROKEN_ITEM.get(), stacksize);

        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_ID, BuiltInRegistries.ITEM.getKey(item).toString());
        tag.putInt(KEY_META, meta);
        TagsUtil.putCustomData(stack, tag);

        return stack;
    }
}
