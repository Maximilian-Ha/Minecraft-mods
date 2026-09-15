package com.hbm.datagen;

import java.util.Map;
import java.util.LinkedHashMap;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import com.hbm.inventory.recipes.RotaryFurnaceRecipes;

import static com.hbm.inventory.NtmTags.Items.*;

public class NtmItemTagProvider extends ItemTagsProvider {

    private static final TagKey<Item> ENCHANTABLE_DIGGER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/digger"));
    private static final TagKey<Item> ENCHANTABLE_PICKAXE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/pickaxe"));
    private static final TagKey<Item> ENCHANTABLE_AXE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/axe"));
    private static final TagKey<Item> ENCHANTABLE_SHOVEL = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/shovel"));
    private static final TagKey<Item> ENCHANTABLE_HOE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/hoe"));
    private static final TagKey<Item> PICKAXES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "pickaxes"));
    private static final TagKey<Item> AXES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "axes"));
    private static final TagKey<Item> SHOVELS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "shovels"));
    private static final TagKey<Item> HOES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "hoes"));
    private static final TagKey<Item> ENCHANTABLE_MINING = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/mining"));
    private static final TagKey<Item> ENCHANTABLE_MINING_LOOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/mining_loot"));
    private static final TagKey<Item> BALLS_HE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "balls_he"));
    private static final TagKey<Item> BARS_HARD_PLASTIC = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "bars_hard_plastic"));
    private static final TagKey<Item> POWDERS_PLASTIC = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "powders_plastic"));
    private static final TagKey<Item> ANY_RUBBERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_rubbers"));
    private static final TagKey<Item> ANY_TARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_tars"));
    private static final TagKey<Item> ANY_CONCRETE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_concrete"));
    private static final TagKey<Item> ANY_RESISTANT_ALLOY = ItemTags.create(ResourceLocation.fromNamespaceAndPath("ntm", "any_resistant_alloy"));

    public NtmItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper helper) {
        super(output, provider, blockTags, NuclearTechMod.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        // Sammeltag fuer alle Kokssorten -- das Gegenstueck zu ANY_COKE im Original
        this.tag(RotaryFurnaceRecipes.COKE).add(
                NtmItems.COKE_COAL.get(),
                NtmItems.COKE_LIGNITE.get(),
                NtmItems.COKE_PETROLEUM.get()
        );

        this.registerMaterialTags();
        this.registerOreTags();

        /*
         * TANKS
         */
        this.tag(UNIVERSAL_TANK).add(NtmItems.FLUID_TANK_FULL.get());
        this.tag(HAZARD_TANK).add(NtmItems.FLUID_TANK_LEAD_FULL.get());
        this.tag(UNIVERSAL_BARREL).add(NtmItems.FLUID_BARREL_FULL.get());

        /*
         * TOOLS
         */
        this.tag(PICKAXES).add(
                NtmItems.STEEL_PICKAXE.get(),
                NtmItems.TITANIUM_PICKAXE.get(),
                NtmItems.DESH_PICKAXE.get(),
                NtmItems.COBALT_PICKAXE.get(),
                NtmItems.COBALT_DECORATED_PICKAXE.get(),
                NtmItems.CMB_PICKAXE.get(),
                NtmItems.BISMUTH_PICKAXE.get(),
                NtmItems.STARMETAL_PICKAXE.get(),
                NtmItems.SCHRABIDIUM_PICKAXE.get(),
                NtmItems.MESE_PICKAXE.get(),
                NtmItems.VOLCANIC_PICKAXE.get(),
                NtmItems.CHLOROPHYTE_PICKAXE.get()
        );

        this.tag(AXES).add(
                NtmItems.STEEL_AXE.get(),
                NtmItems.TITANIUM_AXE.get(),
                NtmItems.DESH_AXE.get(),
                NtmItems.COBALT_AXE.get(),
                NtmItems.COBALT_DECORATED_AXE.get(),
                NtmItems.CMB_AXE.get(),
                NtmItems.BISMUTH_AXE.get(),
                NtmItems.STARMETAL_AXE.get(),
                NtmItems.SCHRABIDIUM_AXE.get(),
                NtmItems.MESE_AXE.get(),
                NtmItems.VOLCANIC_AXE.get(),
                NtmItems.CHLOROPHYTE_AXE.get()
        );

        this.tag(SHOVELS).add(
                NtmItems.STEEL_SHOVEL.get(),
                NtmItems.TITANIUM_SHOVEL.get(),
                NtmItems.DESH_SHOVEL.get(),
                NtmItems.COBALT_SHOVEL.get(),
                NtmItems.COBALT_DECORATED_SHOVEL.get(),
                NtmItems.CMB_SHOVEL.get(),
                NtmItems.STARMETAL_SHOVEL.get(),
                NtmItems.SCHRABIDIUM_SHOVEL.get()
        );

        this.tag(HOES).add(
                NtmItems.STEEL_HOE.get(),
                NtmItems.TITANIUM_HOE.get(),
                NtmItems.DESH_HOE.get(),
                NtmItems.COBALT_HOE.get(),
                NtmItems.COBALT_DECORATED_HOE.get(),
                NtmItems.CMB_HOE.get(),
                NtmItems.STARMETAL_HOE.get(),
                NtmItems.SCHRABIDIUM_HOE.get()
        );

        this.tag(ENCHANTABLE_MINING).addTag(PICKAXES).addTag(AXES).addTag(SHOVELS).addTag(HOES);
        this.tag(ENCHANTABLE_MINING_LOOT).addTag(PICKAXES).addTag(AXES).addTag(SHOVELS).addTag(HOES);

        this.tag(ENCHANTABLE_DIGGER).add(
                NtmItems.STEEL_PICKAXE.get(),
                NtmItems.STEEL_AXE.get(),
                NtmItems.STEEL_SHOVEL.get(),
                NtmItems.STEEL_HOE.get(),
                NtmItems.TITANIUM_PICKAXE.get(),
                NtmItems.TITANIUM_AXE.get(),
                NtmItems.TITANIUM_SHOVEL.get(),
                NtmItems.TITANIUM_HOE.get(),
                NtmItems.DESH_PICKAXE.get(),
                NtmItems.DESH_AXE.get(),
                NtmItems.DESH_SHOVEL.get(),
                NtmItems.DESH_HOE.get(),
                NtmItems.COBALT_PICKAXE.get(),
                NtmItems.COBALT_AXE.get(),
                NtmItems.COBALT_SHOVEL.get(),
                NtmItems.COBALT_HOE.get(),
                NtmItems.COBALT_DECORATED_PICKAXE.get(),
                NtmItems.COBALT_DECORATED_AXE.get(),
                NtmItems.COBALT_DECORATED_SHOVEL.get(),
                NtmItems.COBALT_DECORATED_HOE.get(),
                NtmItems.CMB_PICKAXE.get(),
                NtmItems.CMB_AXE.get(),
                NtmItems.CMB_SHOVEL.get(),
                NtmItems.CMB_HOE.get(),
                NtmItems.BISMUTH_PICKAXE.get(),
                NtmItems.BISMUTH_AXE.get(),
                NtmItems.STARMETAL_PICKAXE.get(),
                NtmItems.STARMETAL_AXE.get(),
                NtmItems.STARMETAL_SHOVEL.get(),
                NtmItems.STARMETAL_HOE.get(),
                NtmItems.SCHRABIDIUM_PICKAXE.get(),
                NtmItems.SCHRABIDIUM_AXE.get(),
                NtmItems.SCHRABIDIUM_SHOVEL.get(),
                NtmItems.SCHRABIDIUM_HOE.get(),
                NtmItems.MESE_PICKAXE.get(),
                NtmItems.MESE_AXE.get(),
                NtmItems.VOLCANIC_PICKAXE.get(),
                NtmItems.VOLCANIC_AXE.get(),
                NtmItems.CHLOROPHYTE_PICKAXE.get(),
                NtmItems.CHLOROPHYTE_AXE.get()
        );

        this.tag(ENCHANTABLE_PICKAXE).add(
                NtmItems.STEEL_PICKAXE.get(),
                NtmItems.TITANIUM_PICKAXE.get(),
                NtmItems.DESH_PICKAXE.get(),
                NtmItems.COBALT_PICKAXE.get(),
                NtmItems.COBALT_DECORATED_PICKAXE.get(),
                NtmItems.CMB_PICKAXE.get(),
                NtmItems.BISMUTH_PICKAXE.get(),
                NtmItems.STARMETAL_PICKAXE.get(),
                NtmItems.SCHRABIDIUM_PICKAXE.get(),
                NtmItems.MESE_PICKAXE.get(),
                NtmItems.VOLCANIC_PICKAXE.get(),
                NtmItems.CHLOROPHYTE_PICKAXE.get()
        );

        this.tag(ENCHANTABLE_AXE).add(
                NtmItems.STEEL_AXE.get(),
                NtmItems.TITANIUM_AXE.get(),
                NtmItems.DESH_AXE.get(),
                NtmItems.COBALT_AXE.get(),
                NtmItems.COBALT_DECORATED_AXE.get(),
                NtmItems.CMB_AXE.get(),
                NtmItems.BISMUTH_AXE.get(),
                NtmItems.STARMETAL_AXE.get(),
                NtmItems.SCHRABIDIUM_AXE.get(),
                NtmItems.MESE_AXE.get(),
                NtmItems.VOLCANIC_AXE.get(),
                NtmItems.CHLOROPHYTE_AXE.get()
        );

        this.tag(ENCHANTABLE_SHOVEL).add(
                NtmItems.STEEL_SHOVEL.get(),
                NtmItems.TITANIUM_SHOVEL.get(),
                NtmItems.DESH_SHOVEL.get(),
                NtmItems.COBALT_SHOVEL.get(),
                NtmItems.COBALT_DECORATED_SHOVEL.get(),
                NtmItems.CMB_SHOVEL.get(),
                NtmItems.STARMETAL_SHOVEL.get(),
                NtmItems.SCHRABIDIUM_SHOVEL.get()
        );

        this.tag(ENCHANTABLE_HOE).add(
                NtmItems.STEEL_HOE.get(),
                NtmItems.TITANIUM_HOE.get(),
                NtmItems.DESH_HOE.get(),
                NtmItems.COBALT_HOE.get(),
                NtmItems.COBALT_DECORATED_HOE.get(),
                NtmItems.CMB_HOE.get(),
                NtmItems.STARMETAL_HOE.get(),
                NtmItems.SCHRABIDIUM_HOE.get()
        );

        this.tag(BALLS_HE).add(
                NtmItems.BALL_TNT.get(),
                NtmItems.BALL_TATB.get()
        );

        this.tag(BARS_HARD_PLASTIC).add(
                NtmItems.INGOT_PC.get(),
                NtmItems.INGOT_PVC.get()
        );

        this.tag(POWDERS_PLASTIC).add(
                NtmItems.POWDER_POLYMER.get(),
                NtmItems.POWDER_BAKELITE.get()
        );

        this.tag(ANY_RUBBERS).add(
                NtmItems.INGOT_RUBBER.get(),
                NtmItems.INGOT_BIORUBBER.get()
        );

        // Gegenstueck zu OreDictManager.ANY_CONCRETE (Original Z. 501-503). Die gefaerbten
        // Betonvarianten des Originals gibt es im Port nicht, der Rest ist vollstaendig.
        // Gegenstueck zu OreDictManager.ANY_RESISTANTALLOY (Original Z. 309: TCALLOY, CDALLOY).
        this.tag(ANY_RESISTANT_ALLOY).add(
                NtmItems.INGOT_TCALLOY.get(),
                NtmItems.INGOT_CDALLOY.get()
        );

        this.tag(ANY_CONCRETE).add(
                NtmBlocks.CONCRETE.asItem(),
                NtmBlocks.CONCRETE_SMOOTH.asItem(),
                NtmBlocks.CONCRETE_ASBESTOS.asItem(),
                NtmBlocks.DUCRETE.asItem(),
                NtmBlocks.DUCRETE_SMOOTH.asItem()
        );

        this.tag(ANY_TARS).add(
                NtmItems.OIL_TAR_CRUDE.get(),
                NtmItems.OIL_TAR_CRACK.get(),
                NtmItems.OIL_TAR_WOOD.get(),
                NtmItems.OIL_TAR_COAL.get()
                );

    }

    /**
     * Erzeugt die Form-und-Material-Tags des Materialsystems, also das Gegenstueck zu den
     * OreDictionary-Namen des Originals: aus "ingotSteel" wird "c:ingots/steel".
     *
     * Die Zuordnung laeuft ueber die Namen der Gegenstaende, weil der Port sie ohnehin nach
     * Form und Material benennt -- "ingot_steel", "powder_coal", "wire_red_copper". Was es
     * nicht gibt, wird uebersprungen; so wachsen die Tags von selbst mit, sobald ein
     * Gegenstand dazukommt.
     *
     * Nicht dabei sind die Formen, die im Port als Untertyp-Gegenstand vorliegen (Gussplatte,
     * dichter Draht): dort steckt das Material in den Metadaten, nicht im Namen.
     */
    private void registerMaterialTags() {

        Map<MaterialShapes, String> prefixes = new LinkedHashMap<>();
        prefixes.put(MaterialShapes.INGOT, "ingot_");
        prefixes.put(MaterialShapes.PLATE, "plate_");
        prefixes.put(MaterialShapes.DUST, "powder_");
        prefixes.put(MaterialShapes.NUGGET, "nugget_");
        prefixes.put(MaterialShapes.BILLET, "billet_");
        prefixes.put(MaterialShapes.WIRE, "wire_");
        prefixes.put(MaterialShapes.BOLT, "bolt_");
        prefixes.put(MaterialShapes.SHELL, "shell_");
        prefixes.put(MaterialShapes.GEM, "gem_");
        prefixes.put(MaterialShapes.CRYSTAL, "crystal_");
        prefixes.put(MaterialShapes.FRAGMENT, "fragment_");

        int count = 0;

        for(Map.Entry<MaterialShapes, String> entry : prefixes.entrySet()) {
            MaterialShapes shape = entry.getKey();

            for(NTMMaterial material : Mats.orderedList) {

                // Gesucht wird unter allen Namen des Materials, nicht nur dem ersten: das
                // Original fuehrt fuer Isotope beide Schreibweisen ("Americium241" und
                // "Am241"), und der Port benennt seine Gegenstaende nach der kurzen.
                Item item = Items.AIR;

                for(String name : material.names) {
                    Item candidate = BuiltInRegistries.ITEM.get(NuclearTechMod.withDefaultNamespace(entry.getValue() + NTMMaterial.toTagName(name)));
                    if(candidate != Items.AIR) { item = candidate; break; }
                }

                if(item == Items.AIR) continue;

                this.tag(shape.getTag(material)).add(item);
                count++;
            }
        }

        NuclearTechMod.LOGGER.info("Materialsystem: {} Form-und-Material-Tags erzeugt.", count);
    }

    /**
     * Haengt die Erze des Mods in die gemeinsamen Erz-Tags (c:ores/...). Erst damit findet
     * MatDistribution sie, und nebenbei sehen andere Mods sie auch.
     *
     * Die Namen der Erzbloecke folgen demselben Muster wie die uebrigen Gegenstaende, also
     * laeuft auch das ueber die Namen: ore_uranium und ore_uranium_deepslate landen beide in
     * c:ores/uranium.
     */
    private void registerOreTags() {

        int count = 0;

        for(var holder : NtmBlocks.BLOCKS.getEntries()) {
            String path = holder.getId().getPath();
            if(!path.startsWith("ore_")) continue;

            Item item = holder.get().asItem();
            if(item == Items.AIR) continue;

            String material = path.substring("ore_".length());
            for(String suffix : new String[] { "_deepslate", "_scorched", "_nether" }) {
                if(material.endsWith(suffix)) material = material.substring(0, material.length() - suffix.length());
            }

            // Die Schreibweise der gemeinsamen Tags ist die amerikanische.
            if(material.equals("aluminium")) material = "aluminum";

            this.tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + material))).add(item);
            count++;
        }

        NuclearTechMod.LOGGER.info("Materialsystem: {} Erze in die gemeinsamen Tags eingehaengt.", count);
    }
}
