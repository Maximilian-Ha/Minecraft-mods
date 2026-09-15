package com.hbm.items.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.material.MatShapeItems;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.IMetaItem;
import com.hbm.items.NtmItems;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemMold.
 *
 * Die Giessform. Welche Form es ist, steht in der Meta-Komponente; jede Form weiss, wieviel
 * Material sie fasst und was daraus wird.
 *
 * ABWEICHUNG: das Original sucht das Ergebnis einer Formform ueber den OreDictionary-Namen
 * ("ingot" + "Steel"). Im Port steht dafuer der Form-und-Material-Tag (c:ingots/steel), sonst
 * ist die Suche dieselbe: erst ein Gegenstand dieses Mods, Bruchstuecke zuletzt, sonst der
 * erste beste.
 *
 * NICHT UEBERNOMMEN: die Formen fuer Gussplatte, dichten Draht, Rohr, Block, Huelsen und die
 * sieben Waffenteile. Ihre Formen haben im Port noch keine Gegenstaende -- sie waeren
 * Sackgassen, in die man giessen kann, ohne je etwas herauszubekommen. Sobald es die
 * Gegenstaende gibt, ist hier nur je eine Zeile nachzutragen.
 */
public class MoldItem extends Item implements IMetaItem, ICustomItemModelRegister {

    /** Alle Formen in Anzeigereihenfolge. */
    public static final List<Mold> molds = new ArrayList<>();
    /** Alle Formen unter ihrer festen Kennzahl, die auch in der Meta-Komponente steht. */
    public static final HashMap<Integer, Mold> moldById = new HashMap<>();

    /** Materialien, deren Block kein Form-und-Material-Tag ist, sondern ein Vanillablock. */
    public static final HashMap<NTMMaterial, ItemStack> blockOverrides = new HashMap<>();

    private static int nextOrder = 0;

    private static final int S = 0; // kleine Form
    private static final int L = 1; // Becken

    private static boolean registered = false;

    /**
     * Die Formen werden erst angelegt, wenn sie zum ersten Mal gebraucht werden, nicht schon
     * beim Laden der Klasse: sie greifen auf andere Gegenstaende zu, und die sind waehrend der
     * Registrierung noch nicht alle da.
     */
    public static void registerMolds() {

        if(registered) return;
        registered = true;

        blockOverrides.put(Mats.MAT_STONE, new ItemStack(Blocks.STONE));
        blockOverrides.put(Mats.MAT_OBSIDIAN, new ItemStack(Blocks.OBSIDIAN));

        registerMold(new MoldShape(0, S, "nugget", MaterialShapes.NUGGET));
        registerMold(new MoldShape(1, S, "billet", MaterialShapes.BILLET));
        registerMold(new MoldShape(2, S, "ingot", MaterialShapes.INGOT));
        registerMold(new MoldShape(3, S, "plate", MaterialShapes.PLATE));
        registerMold(new MoldShape(4, S, "wire", MaterialShapes.WIRE, 8));
        registerMold(new MoldShape(8, S, "shell", MaterialShapes.SHELL));

        registerMold(new MoldMulti(5, S, "blade", MaterialShapes.INGOT.q(3),
                Mats.MAT_TITANIUM, new ItemStack(NtmItems.BLADE_TITANIUM.get()),
                Mats.MAT_TUNGSTEN, new ItemStack(NtmItems.BLADE_TUNGSTEN.get())));

        registerMold(new MoldMulti(6, S, "blades", MaterialShapes.INGOT.q(4),
                Mats.MAT_STEEL, new ItemStack(NtmItems.BLADES_STEEL.get()),
                Mats.MAT_TITANIUM, new ItemStack(NtmItems.BLADES_TITANIUM.get())));

        registerMold(new MoldMulti(7, S, "stamp", MaterialShapes.INGOT.q(4),
                Mats.MAT_STONE, new ItemStack(NtmItems.STAMP_STONE_FLAT.get()),
                Mats.MAT_IRON, new ItemStack(NtmItems.STAMP_IRON_FLAT.get()),
                Mats.MAT_STEEL, new ItemStack(NtmItems.STAMP_STEEL_FLAT.get()),
                Mats.MAT_TITANIUM, new ItemStack(NtmItems.STAMP_TITANIUM_FLAT.get()),
                Mats.MAT_OBSIDIAN, new ItemStack(NtmItems.STAMP_OBSIDIAN_FLAT.get())));

        registerMold(new MoldShape(10, L, "ingots", MaterialShapes.INGOT, 9));
        registerMold(new MoldShape(11, L, "plates", MaterialShapes.PLATE, 9));

        // Gussplatte und dichter Draht liegen im Port als Untertyp-Gegenstand vor und lassen
        // sich deshalb nicht ueber Tags finden -- MatShapeItems loest sie auf.
        registerMold(new MoldSubtype(19, S, "plate_cast", MaterialShapes.CASTPLATE, 1, MatShapeItems::castPlateOf));
        registerMold(new MoldSubtype(20, S, "wire_dense", MaterialShapes.DENSEWIRE, 1, MatShapeItems::wireDenseOf));
        registerMold(new MoldSubtype(13, L, "plates_cast", MaterialShapes.CASTPLATE, 3, MatShapeItems::castPlateOf));
        registerMold(new MoldSubtype(21, L, "wires_dense", MaterialShapes.DENSEWIRE, 9, MatShapeItems::wireDenseOf));
    }

    public MoldItem(Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
    }

    public static void registerMold(Mold mold) {
        molds.add(mold);
        moldById.put(mold.id, mold);
    }

    public static @Nullable Mold getMold(ItemStack stack) {
        registerMolds();
        Mold mold = moldById.get(MetaHelper.getMeta(stack));
        return mold != null ? mold : (molds.isEmpty() ? null : molds.get(0));
    }

    public static ItemStack create(Mold mold) {
        return MetaHelper.newStack(NtmItems.MOLD.get(), 1, mold.id);
    }

    @Override
    public Component getName(ItemStack stack) {
        Mold mold = getMold(stack);
        if(mold == null) return super.getName(stack);
        return Component.translatable(this.getDescriptionId(), mold.getTitle());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        Mold mold = getMold(stack);
        if(mold == null) return;

        components.add(mold.getTitle().copy().withStyle(ChatFormatting.YELLOW));

        if(mold.size == S) components.add(NtmBlocks.FOUNDRY_MOLD.get().getName().withStyle(ChatFormatting.GOLD));
        if(mold.size == L) components.add(NtmBlocks.FOUNDRY_BASIN.get().getName().withStyle(ChatFormatting.RED));
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        registerMolds();
        for(Mold mold : molds) stacks.add(MetaHelper.newStack(item, 1, mold.id));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {

        registerMolds();

        ItemModelBuilder builder = provider.getBuilder(modelLocation.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "item/mold_base"));

        for(Mold mold : molds) {
            builder.override()
                    .predicate(NuclearTechMod.withDefaultNamespace("item_meta"), mold.id)
                    .model(provider.getBuilder("mold_" + mold.name)
                            .parent(new ModelFile.UncheckedModelFile("item/generated"))
                            .texture("layer0", ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "item/mold_" + mold.name)))
                    .end();
        }
    }

    public abstract static class Mold {

        public final int order;
        public final int id;
        public final int size;
        public final String name;

        protected Mold(int id, int size, String name) {
            this.order = nextOrder++;
            this.id = id;
            this.size = size;
            this.name = name;
        }

        /** Was aus diesem Material wird -- leer, wenn diese Form es nicht verarbeitet. */
        public abstract ItemStack getOutput(@Nullable Level level, @Nullable NTMMaterial mat);

        /** Wieviel Material die Form fasst, in Quanten. */
        public abstract int getCost();

        public abstract Component getTitle();
    }

    /** Eine Form, die aus dem Material schlicht die zugehoerige Form macht: Barren, Platte, Draht. */
    public static class MoldShape extends Mold {

        public final MaterialShapes shape;
        public final int amount;

        public MoldShape(int id, int size, String name, MaterialShapes shape) {
            this(id, size, name, shape, 1);
        }

        public MoldShape(int id, int size, String name, MaterialShapes shape, int amount) {
            super(id, size, name);
            this.shape = shape;
            this.amount = amount;
        }

        @Override
        public ItemStack getOutput(@Nullable Level level, @Nullable NTMMaterial mat) {

            if(mat == null) return ItemStack.EMPTY;

            Item fallback = Items.AIR;

            for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.shape.getTag(mat))) {

                Item item = holder.value();
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);

                // Gegenstaende dieses Mods haben Vorrang, Bruchstuecke kommen zuletzt
                if(NuclearTechMod.MODID.equals(key.getNamespace()) && !key.getPath().contains("fragment")) {
                    return new ItemStack(item, this.amount);
                }

                if(fallback == Items.AIR) fallback = item;
            }

            return fallback == Items.AIR ? ItemStack.EMPTY : new ItemStack(fallback, this.amount);
        }

        @Override public int getCost() { return this.shape.q(this.amount); }

        @Override
        public Component getTitle() {
            return Component.translatable("shape." + this.shape.name()).append(" x" + this.amount);
        }
    }

    /**
     * Eine Form fuer die beiden Formen, deren Gegenstand im Port Metadaten traegt. Sie
     * verhaelt sich wie MoldShape, sucht das Ergebnis aber nicht ueber einen Tag, sondern
     * ueber die uebergebene Aufloesung.
     */
    public static class MoldSubtype extends Mold {

        public final MaterialShapes shape;
        public final int amount;
        private final Function<NTMMaterial, ItemStack> resolver;

        public MoldSubtype(int id, int size, String name, MaterialShapes shape, int amount, Function<NTMMaterial, ItemStack> resolver) {
            super(id, size, name);
            this.shape = shape;
            this.amount = amount;
            this.resolver = resolver;
        }

        @Override
        public ItemStack getOutput(@Nullable Level level, @Nullable NTMMaterial mat) {

            if(mat == null) return ItemStack.EMPTY;

            ItemStack out = this.resolver.apply(mat);
            if(out.isEmpty()) return ItemStack.EMPTY;

            out.setCount(this.amount);
            return out;
        }

        @Override public int getCost() { return this.shape.q(this.amount); }

        @Override
        public Component getTitle() {
            return Component.translatable("shape." + this.shape.name()).append(" x" + this.amount);
        }
    }

    /** Eine Form, die je nach Material etwas anderes ergibt -- Klingen, Stempel. */
    public static class MoldMulti extends Mold {

        public final HashMap<NTMMaterial, ItemStack> map = new HashMap<>();
        public final int amount;
        public int stacksize;

        public MoldMulti(int id, int size, String name, int amount, Object... inputs) {
            super(id, size, name);
            this.amount = amount;

            for(int i = 0; i < inputs.length; i += 2) {
                ItemStack out = (ItemStack) inputs[i + 1];
                this.map.put((NTMMaterial) inputs[i], out);
                if(i == 0) this.stacksize = out.getCount();
            }
        }

        @Override
        public ItemStack getOutput(@Nullable Level level, @Nullable NTMMaterial mat) {
            ItemStack out = this.map.get(mat);
            return out == null ? ItemStack.EMPTY : out.copy();
        }

        @Override public int getCost() { return this.amount; }

        @Override
        public Component getTitle() {
            return Component.translatable("shape." + this.name).append(" x" + this.stacksize);
        }
    }
}
