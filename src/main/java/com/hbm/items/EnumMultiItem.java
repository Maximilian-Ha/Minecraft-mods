package com.hbm.items;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.interfaces.IOrderedEnum;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.EnumUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.List;
import java.util.Locale;

public class EnumMultiItem extends Item implements IMetaItem, ICustomItemModelRegister {

    //hell yes, now we're thinking with enums!
    protected final Class<? extends Enum<?>> theEnum;
    public final boolean multiName;
    public final boolean multiTexture;

    public EnumMultiItem(Properties properties, Class<? extends Enum<?>> theEnum, boolean multiName, boolean multiTexture) {
        super(properties.component(NtmDataComponents.META.get(), 0));
        this.theEnum = theEnum;
        this.multiName = multiName;
        this.multiTexture = multiTexture;
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {

        Enum<?>[] order = theEnum.getEnumConstants();
        if(order[0] instanceof IOrderedEnum ord) order = ord.getOrder();

        for(Enum<?> anEnum : order) {
            stacks.add(MetaHelper.newStack(item, 1, anEnum.ordinal()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {

        /*
         * Nur Gegenstaende mit eigener Textur je Wert bekommen hier ein Modell. Alle anderen
         * nennt NtmItemModelProvider einzeln -- entweder mit basicItem, wenn es eine Textur
         * gibt, oder mit entityItem, wenn ein eigener Darsteller sie zeichnet. Ein Modell an
         * dieser Stelle waere fuer die zweite Gruppe falsch: battery_pack etwa hat gar keine
         * Textur, und der Datenerzeuger bricht ab, sobald eines auf sie zeigt.
         */
        if(multiTexture) {
            Enum<?>[] enums = theEnum.getEnumConstants();

            ItemModelBuilder builder = provider.getBuilder(modelLocation.toString());

            for(int i = 0; i < enums.length; i++) {
                Enum<?> num = enums[i];

                builder.override()
                        .predicate(NuclearTechMod.withDefaultNamespace("item_meta"), i)
                        .model(provider.getBuilder(modelLocation.getPath() + "_" + i)
                                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                                .texture("layer0", ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "item/" + modelLocation.getPath() + "." + num.name().toLowerCase(Locale.US))))
                        .end();
            }
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if(multiName) {
            Enum<?> num = EnumUtil.grabEnumSafely(theEnum, MetaHelper.getMeta(stack));
            return super.getDescriptionId() + "." + num.name().toLowerCase(Locale.US);
        } else {
            return super.getDescriptionId(stack);
        }
    }
}
