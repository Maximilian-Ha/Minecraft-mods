package com.hbm.items.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.IMetaItem;
import com.hbm.items.special.WasteDropItem;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemRBMKPellet.
 *
 * Die Pellets sind das, was beim Zerlegen eines abgebrannten Brennstabs herauskommt. Ihr
 * Zustand steckt wie im Original in einer Zahl von 0 bis 9: die Einerstelle modulo fuenf
 * nennt den Abbrand, ab fuenf ist das Pellet zusaetzlich stark xenonvergiftet.
 *
 * Wie im Original leitet die Klasse von der Muell-Grundlage ab: fallen gelassene Pellets
 * verfallen nicht und lassen sich nicht zerstoeren (Runde 290). Offen bleibt der Eintrag im
 * HazardSystem -- den tragen Staebe und Pellets gemeinsam nach, wenn die Strahlung drankommt.
 */
public class RBMKPelletItem extends WasteDropItem implements IMetaItem, ICustomItemModelRegister {

    /** Voller Name des Brennstoffs, wird im Tooltip angezeigt. */
    public final String fullName;
    /** Neutronenquellen koennen kein Xenon ansammeln, ihnen fehlen die oberen fuenf Zustaende. */
    protected boolean hasXenon = true;

    public RBMKPelletItem(String fullName, Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
        this.fullName = fullName;
    }

    public RBMKPelletItem disableXenon() {
        this.hasXenon = false;
        return this;
    }

    public boolean isXenonEnabled() { return this.hasXenon; }

    /** Wie viele Zustaende dieses Pellet kennt -- zehn mit Xenon, sonst fuenf. */
    public int getStateCount() { return this.hasXenon ? 10 : 5; }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(int i = 0; i < this.getStateCount(); i++) stacks.add(MetaHelper.newStack(item, 1, i));
    }

    /**
     * Das Original zeichnet in drei Durchgaengen: Grundtextur, Abbrandschicht und, falls
     * vergiftet, die Xenonschicht. In 1.21 uebernimmt das der Modellueberschreiber ueber
     * item_meta -- je Zustand ein Modell aus zwei oder drei Texturschichten.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {

        ItemModelBuilder builder = provider.getBuilder(modelLocation.toString());
        String path = modelLocation.getPath();
        String namespace = modelLocation.getNamespace();

        for(int i = 0; i < this.getStateCount(); i++) {

            ItemModelBuilder state = provider.getBuilder(path + "_" + i)
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", ResourceLocation.fromNamespaceAndPath(namespace, "item/" + path))
                    .texture("layer1", ResourceLocation.fromNamespaceAndPath(namespace, "item/rbmk_pellet_overlay_e" + (i % 5)));

            if(hasXenon(i)) state.texture("layer2", ResourceLocation.fromNamespaceAndPath(namespace, "item/rbmk_pellet_overlay_xenon"));

            builder.override().predicate(NuclearTechMod.withDefaultNamespace("item_meta"), i).model(state).end();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

        list.add(Component.literal(this.fullName).withStyle(ChatFormatting.ITALIC));
        list.add(Component.translatable("trait.rbmk.pellet").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        int meta = rectify(MetaHelper.getMeta(stack));

        switch(meta % 5) {
            case 0 -> list.add(Component.translatable("trait.rbmk.pellet.depletion0").withStyle(ChatFormatting.GOLD));
            case 1 -> list.add(Component.translatable("trait.rbmk.pellet.depletion1").withStyle(ChatFormatting.YELLOW));
            case 2 -> list.add(Component.translatable("trait.rbmk.pellet.depletion2").withStyle(ChatFormatting.GREEN));
            case 3 -> list.add(Component.translatable("trait.rbmk.pellet.depletion3").withStyle(ChatFormatting.DARK_GREEN));
            case 4 -> list.add(Component.translatable("trait.rbmk.pellet.depletion4").withStyle(ChatFormatting.DARK_GRAY));
        }

        if(hasXenon(meta)) list.add(Component.translatable("trait.rbmk.pellet.xenon").withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static boolean hasXenon(int meta) { return rectify(meta) >= 5; }

    public static int rectify(int meta) { return Math.abs(meta) % 10; }
}
