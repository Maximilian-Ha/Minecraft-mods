package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.IMetaItem;
import com.hbm.items.NtmItems;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemScraps.
 *
 * Ein Klumpen Material -- das, was aus dem Tiegel kommt oder wieder hineingeht. Das Material
 * steckt in der Meta-Komponente (die Material-ID), die Menge in Quanten in einer eigenen
 * Komponente. Fluessiger Schrott ist kein richtiger Gegenstand, sondern nur die Anzeige eines
 * Rezeptergebnisses; er laesst sich weder giessen noch stapeln.
 *
 * ABWEICHUNG: das Original erzeugt fuer jedes Material zur Laufzeit eine eingefaerbte Textur
 * (ItemAutogen). Der Port hat dieses Autogen-System noch nicht, deshalb steht hier eine
 * Graustufentextur, die ueber den Farbwert des Materials eingefaerbt wird -- dasselbe Verfahren,
 * das der Port schon fuer die Fluidsymbole benutzt.
 */
public class ScrapsItem extends Item implements IMetaItem, ICustomItemModelRegister {

    /** Zustaende fuer die Modellauswahl: fest, fluessig, fluessiger Zuschlagstoff. */
    public static final int STATE_SOLID = 0;
    public static final int STATE_LIQUID = 1;
    public static final int STATE_ADDITIVE = 2;

    public ScrapsItem(Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
    }

    public static @Nullable MaterialStack getMats(ItemStack stack) {

        if(!(stack.getItem() instanceof ScrapsItem)) return null;

        NTMMaterial mat = Mats.matById.get(MetaHelper.getMeta(stack));
        if(mat == null) return null;

        return new MaterialStack(mat, stack.getOrDefault(NtmDataComponents.SCRAPS_AMOUNT.get(), MaterialShapes.INGOT.q(1)));
    }

    public static ItemStack create(MaterialStack stack) {
        return create(stack, false);
    }

    public static ItemStack create(MaterialStack stack, boolean liquid) {

        if(stack == null || stack.material == null) return new ItemStack(NtmItems.NOTHING.get());

        ItemStack scrap = MetaHelper.newStack(NtmItems.SCRAPS.get(), 1, stack.material.id);
        scrap.set(NtmDataComponents.SCRAPS_AMOUNT.get(), stack.amount);
        if(liquid) scrap.set(NtmDataComponents.SCRAPS_LIQUID.get(), true);

        return scrap;
    }

    public static boolean isLiquid(ItemStack stack) {
        return stack.getOrDefault(NtmDataComponents.SCRAPS_LIQUID.get(), false);
    }

    /** Welches Modell der Klumpen bekommt -- siehe die drei STATE-Konstanten. */
    public static int getState(ItemStack stack) {

        if(!isLiquid(stack)) return STATE_SOLID;

        NTMMaterial mat = Mats.matById.get(MetaHelper.getMeta(stack));
        if(mat != null && mat.smeltable == SmeltingBehavior.ADDITIVE) return STATE_ADDITIVE;

        return STATE_LIQUID;
    }

    /** Die Farbe, mit der die Graustufentextur eingefaerbt wird. */
    public static int getColor(ItemStack stack) {

        NTMMaterial mat = Mats.matById.get(MetaHelper.getMeta(stack));
        if(mat == null) return 0xFFFFFF;

        return isLiquid(stack) ? mat.moltenColor : mat.solidColorLight;
    }

    @Override
    public Component getName(ItemStack stack) {

        MaterialStack contents = getMats(stack);
        if(contents == null) return Component.literal("Foundry Scraps");

        // Fluessiges hat keinen eigenen Namen, es heisst schlicht wie das Material.
        if(isLiquid(stack)) return contents.material.getName();

        return Component.translatable(this.getDescriptionId(), contents.material.getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        MaterialStack contents = getMats(stack);
        if(contents == null) return;

        components.add(Component.literal(Mats.formatAmount(contents.amount, Screen.hasShiftDown())));

        if(isLiquid(stack) && contents.material.smeltable == SmeltingBehavior.ADDITIVE) {
            components.add(Component.translatable("desc.item.scraps.additive").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(NTMMaterial mat : Mats.orderedList) {
            if(mat.smeltable == SmeltingBehavior.SMELTABLE || mat.smeltable == SmeltingBehavior.ADDITIVE) {
                stacks.add(create(new MaterialStack(mat, MaterialShapes.INGOT.q(1))));
            }
        }
    }

    @Override
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {

        ItemModelBuilder builder = provider.getBuilder(modelLocation.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "item/scraps"));

        String[] textures = { "scraps", "scraps_liquid", "scraps_additive" };

        for(int i = 1; i < textures.length; i++) {
            builder.override()
                    .predicate(NuclearTechMod.withDefaultNamespace("scraps_state"), i)
                    .model(provider.getBuilder(textures[i])
                            .parent(new ModelFile.UncheckedModelFile("item/generated"))
                            .texture("layer0", ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "item/" + textures[i])))
                    .end();
        }
    }
}
