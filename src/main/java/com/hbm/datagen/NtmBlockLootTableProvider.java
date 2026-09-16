package com.hbm.datagen;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.OreBasaltBlock;
import com.hbm.items.NtmItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.Set;

public class NtmBlockLootTableProvider extends BlockLootSubProvider {

    protected NtmBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {


        /* RBMK: die Saeulen fallen als Block, der Schutt bleibt liegen. */
        this.dropSelf(NtmBlocks.RBMK_BLANK.get());
        this.dropSelf(NtmBlocks.RBMK_MODERATOR.get());
        this.dropSelf(NtmBlocks.RBMK_ABSORBER.get());
        this.dropSelf(NtmBlocks.RBMK_REFLECTOR.get());
        this.dropSelf(NtmBlocks.RBMK_ROD.get());
        this.dropSelf(NtmBlocks.RBMK_ROD_MOD.get());
        this.dropSelf(NtmBlocks.RBMK_CONTROL.get());
        this.dropSelf(NtmBlocks.RBMK_CONTROL_MOD.get());
        this.dropSelf(NtmBlocks.RBMK_BOILER.get());
        this.dropSelf(NtmBlocks.RBMK_COOLER.get());
        this.dropSelf(NtmBlocks.RBMK_STORAGE.get());
        this.dropSelf(NtmBlocks.RBMK_HEATER.get());
        this.dropSelf(NtmBlocks.RBMK_OUTGASSER.get());
        this.dropSelf(NtmBlocks.RBMK_ROD_REASIM.get());
        this.dropSelf(NtmBlocks.RBMK_ROD_REASIM_MOD.get());
        this.dropSelf(NtmBlocks.RBMK_CONTROL_AUTO.get());
        this.dropSelf(NtmBlocks.RBMK_CONTROL_REASIM.get());
        this.dropSelf(NtmBlocks.RBMK_CONTROL_REASIM_AUTO.get());
        this.dropSelf(NtmBlocks.RBMK_STEAM_INLET.get());
        this.dropSelf(NtmBlocks.RBMK_STEAM_OUTLET.get());
        this.dropSelf(NtmBlocks.RBMK_LOADER.get());

        this.dropSelf(NtmBlocks.CONVEYOR.get());
        this.dropSelf(NtmBlocks.CONVEYOR_EXPRESS.get());
        this.dropSelf(NtmBlocks.CONVEYOR_DOUBLE.get());
        this.dropSelf(NtmBlocks.CONVEYOR_TRIPLE.get());
        this.dropSelf(NtmBlocks.CONVEYOR_LIFT.get());
        this.dropSelf(NtmBlocks.CONVEYOR_CHUTE.get());
        this.dropSelf(NtmBlocks.CRANE_INSERTER.get());
        this.dropSelf(NtmBlocks.CRANE_EXTRACTOR.get());
        this.dropSelf(NtmBlocks.CRANE_GRABBER.get());
        this.dropSelf(NtmBlocks.CRANE_BOXER.get());
        this.dropSelf(NtmBlocks.CRANE_UNBOXER.get());
        this.dropSelf(NtmBlocks.CRANE_ROUTER.get());
        this.dropSelf(NtmBlocks.CRANE_PARTITIONER.get());
        /* Wie die uebrigen Mehrblockbauten des Ports: dropSelf. Dass dabei nur einmal etwas
         * faellt, besorgt DummyableBlock, indem es die Beibloecke still entfernt. */
        this.dropSelf(NtmBlocks.CRANE_SPLITTER.get());

        /* Runde 98. Eine Tuer besteht aus zwei Bloecken und darf nur einmal fallen. */
        this.add(NtmBlocks.DOOR_METAL.get(), this::createDoorTable);
        this.add(NtmBlocks.DOOR_OFFICE.get(), this::createDoorTable);
        this.add(NtmBlocks.DOOR_BUNKER.get(), this::createDoorTable);
        this.dropSelf(NtmBlocks.TRAPDOOR_STEEL.get());
        this.dropSelf(NtmBlocks.LADDER_STEEL.get());
        this.dropSelf(NtmBlocks.FENCE_METAL.get());
        this.dropSelf(NtmBlocks.FENCE_METAL_POST.get());
        this.dropSelf(NtmBlocks.DUNGEON_CHAIN.get());
        this.dropSelf(NtmBlocks.RAIL_NARROW.get());
        this.dropSelf(NtmBlocks.RBMK_CONSOLE.get());
        this.dropSelf(NtmBlocks.RBMK_CRANE_CONSOLE.get());
        this.dropSelf(NtmBlocks.RBMK_AUTOLOADER.get());
        this.dropSelf(NtmBlocks.MACHINE_WASTE_DRUM.get());
        this.dropSelf(NtmBlocks.RBMK_DEBRIS.get());
        this.dropSelf(NtmBlocks.RBMK_DEBRIS_BURNING.get());
        this.dropSelf(NtmBlocks.RBMK_DEBRIS_RADIATING.get());
        this.dropSelf(NtmBlocks.RBMK_DEBRIS_DIGAMMA.get());
        this.dropSelf(NtmBlocks.STONE_CRACKED.get());
        this.dropSelf(NtmBlocks.DIRT_OILY.get());
        this.dropSelf(NtmBlocks.SAND_OILY.get());
        this.dropSelf(NtmBlocks.SAND_RED_OILY.get());
        this.dropSelf(NtmBlocks.DIRT_DEAD.get());
        this.dropSelf(NtmBlocks.OIL_SPILL.get());
        this.dropSelf(NtmBlocks.ORE_OIL.get());
        this.dropSelf(NtmBlocks.ORE_OIL.get());
        this.dropSelf(NtmBlocks.ORE_OIL_EMPTY.get());
        this.dropSelf(NtmBlocks.ORE_OIL_SAND.get());
        this.dropSelf(NtmBlocks.ORE_BEDROCK_OIL.get());
        this.dropSelf(NtmBlocks.ORE_URANIUM.get());
        this.dropSelf(NtmBlocks.ORE_URANIUM_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_URANIUM_SCORCHED.get());
        this.dropSelf(NtmBlocks.ORE_BERYLLIUM.get());
        this.dropSelf(NtmBlocks.ORE_BERYLLIUM_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_TUNGSTEN.get());
        this.dropSelf(NtmBlocks.ORE_TUNGSTEN_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_TITANIUM.get());
        this.dropSelf(NtmBlocks.ORE_TITANIUM_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_LEAD.get());
        this.dropSelf(NtmBlocks.ORE_LEAD_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_ALUMINIUM.get());
        this.dropSelf(NtmBlocks.ORE_ALUMINIUM_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_ASBESTOS.get());
        this.dropSelf(NtmBlocks.ORE_ASBESTOS_DEEPSLATE.get());
        this.dropSelf(NtmBlocks.ORE_THORIUM.get());
        this.dropSelf(NtmBlocks.ORE_THORIUM_DEEPSLATE.get());
        this.add(NtmBlocks.ORE_NITER.get(), block -> this.oreDrop(block, NtmItems.NITER.get()));
        this.add(NtmBlocks.ORE_NITER_DEEPSLATE.get(), block -> this.oreDrop(block, NtmItems.NITER.get()));
        this.add(NtmBlocks.ORE_COBALT.get(), block -> this.oreDrop(block, NtmItems.FRAGMENT_COBALT.get(), 5, 8));
        this.add(NtmBlocks.ORE_COBALT_DEEPSLATE.get(), block -> this.oreDrop(block, NtmItems.FRAGMENT_COBALT.get(), 5, 8));
        this.add(NtmBlocks.ORE_CINNABAR.get(), block -> this.oreDrop(block, NtmItems.CINNABAR.get()));
        this.add(NtmBlocks.ORE_CINNABAR_DEEPSLATE.get(), block -> this.oreDrop(block, NtmItems.CINNABAR.get()));
        this.add(NtmBlocks.ORE_FLUORITE.get(), block -> this.oreDrop(block, NtmItems.FLUORITE.get(), 2, 4));
        this.add(NtmBlocks.ORE_FLUORITE_DEEPSLATE.get(), block -> this.oreDrop(block, NtmItems.FLUORITE.get(), 2, 4));
        this.dropSelf(NtmBlocks.ORE_METEOR_IRON.get());
        this.dropSelf(NtmBlocks.ORE_METEOR_COBALT.get());
        this.dropSelf(NtmBlocks.ORE_METEOR_ALUMINIUM.get());
        this.dropSelf(NtmBlocks.ORE_METEOR_COPPER.get());
        this.dropSelf(NtmBlocks.ORE_METEOR_RARE.get());
        this.add(NtmBlocks.ORE_LIGNITE.get(), block -> this.oreDropNoFortune(block, NtmItems.LIGNITE.get()));
        this.add(NtmBlocks.ORE_DEEPSLATE_LIGNITE.get(), block -> this.oreDropNoFortune(block, NtmItems.LIGNITE.get()));
        this.add(NtmBlocks.ORE_RARE.get(), block -> this.oreDrop(block, NtmItems.RARE_EARTH_ORE_CHUNK.get()));
        this.add(NtmBlocks.ORE_RARE_DEEPSLATE.get(), block -> this.oreDrop(block, NtmItems.RARE_EARTH_ORE_CHUNK.get()));
        this.add(NtmBlocks.ORE_SULFUR.get(), block -> this.oreDrop(block, NtmItems.SULFUR.get(), 2, 4));
        this.add(NtmBlocks.ORE_SULFUR_DEEPSLATE.get(), block -> this.oreDrop(block, NtmItems.SULFUR.get(), 2, 4));
        this.dropSelf(NtmBlocks.ORE_SCHRABIDIUM.get());
        this.dropSelf(NtmBlocks.ORE_NETHER_URANIUM.get());
        this.dropSelf(NtmBlocks.ORE_NETHER_URANIUM_SCORCHED.get());
        this.dropSelf(NtmBlocks.ORE_NETHER_PLUTONIUM.get());
        this.dropSelf(NtmBlocks.ORE_NETHER_SCHRABIDIUM.get());
        this.dropSelf(NtmBlocks.ORE_TIKITE.get());
        this.dropSelf(NtmBlocks.ORE_GNEISS_URANIUM.get());
        this.dropSelf(NtmBlocks.ORE_GNEISS_URANIUM_SCORCHED.get());
        this.dropSelf(NtmBlocks.ORE_GNEISS_SCHRABIDIUM.get());
        this.dropSelf(NtmBlocks.RESOURCE_LIMESTONE.get());
        this.dropSelf(NtmBlocks.RESOURCE_BAUXITE.get());
        this.dropSelf(NtmBlocks.RESOURCE_HEMATITE.get());
        this.dropSelf(NtmBlocks.RESOURCE_MALACHITE.get());
        this.dropSelf(NtmBlocks.RESOURCE_CHRYSOTILE.get());
        this.dropSelf(NtmBlocks.RESOURCE_SULFUROUS_STONE.get());
        this.dropSelf(NtmBlocks.DECO_ALUMINIUM.get());
        this.dropSelf(NtmBlocks.DECO_BERYLLIUM.get());
        this.dropSelf(NtmBlocks.DECO_LEAD.get());
        this.dropSelf(NtmBlocks.DECO_RED_COPPER.get());
        this.dropSelf(NtmBlocks.DECO_STEEL.get());
        this.dropSelf(NtmBlocks.DECO_ALUMINIUM.get());
        this.dropSelf(NtmBlocks.DECO_RUSTY_STEEL.get());
        this.dropSelf(NtmBlocks.DECO_TITANIUM.get());
        this.dropSelf(NtmBlocks.DECO_ALUMINIUM.get());
        this.dropSelf(NtmBlocks.DECO_TUNGSTEN.get());
        this.dropSelf(NtmBlocks.DECO_ASBESTOS.get());
        this.dropSelf(NtmBlocks.DECO_RBMK.get());
        this.dropSelf(NtmBlocks.DECO_RBMK_SMOOTH.get());


        this.add(NtmBlocks.ORE_BASALT.get(), block -> LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1F))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(NtmItems.NOTHING).when(this.propertyEquals(block, OreBasaltBlock.SUBTYPE, 0)),
                                LootItem.lootTableItem(NtmItems.DETONATOR).when(this.propertyEquals(block, OreBasaltBlock.SUBTYPE, 1)),
                                LootItem.lootTableItem(NtmItems.CAN_KEY).when(this.propertyEquals(block, OreBasaltBlock.SUBTYPE, 2)),
                                LootItem.lootTableItem(NtmItems.CRACKPIPE).when(this.propertyEquals(block, OreBasaltBlock.SUBTYPE, 3)),
                                LootItem.lootTableItem(NtmItems.CIGARETTE).when(this.propertyEquals(block, OreBasaltBlock.SUBTYPE, 4))
                        ))
                )
        );

        this.dropSelf(NtmBlocks.BASALT.get());
        this.dropSelf(NtmBlocks.BASALT_SMOOTH.get());
        this.dropSelf(NtmBlocks.BASALT_BRICK.get());
        this.dropSelf(NtmBlocks.BASALT_POLISHED.get());
        this.dropSelf(NtmBlocks.BASALT_TILES.get());

        this.dropSelf(NtmBlocks.BLOCK_SCRAP.get());

        this.dropSelf(NtmBlocks.BOBBLEHEAD.get());
        this.dropSelf(NtmBlocks.PLUSHIE.get());
        this.dropSelf(NtmBlocks.PLANT_FLOWER.get());

        this.dropSelf(NtmBlocks.GRAVEL_OBSIDIAN.get());
        this.dropSelf(NtmBlocks.GRAVEL_DIAMOND.get());
        this.dropSelf(NtmBlocks.MOON_TURF.get());

        this.dropSelf(NtmBlocks.ASPHALT.get());
        this.dropSelf(NtmBlocks.ASPHALT_LIGHT.get());

        this.dropSelf(NtmBlocks.BRICK_CONCRETE.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_MOSSY.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_BROKEN.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_CRACKED.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_MARKED.get());
        this.dropSelf(NtmBlocks.BRICK_OBSIDIAN.get());
        this.dropSelf(NtmBlocks.BRICK_LIGHT.get());
        this.dropSelf(NtmBlocks.BRICK_ASBESTOS.get());
        this.dropSelf(NtmBlocks.BRICK_FIRE.get());
        this.dropSelf(NtmBlocks.CONCRETE.get());
        this.dropSelf(NtmBlocks.CONCRETE_SMOOTH.get());
        this.dropSelf(NtmBlocks.CONCRETE_ASBESTOS.get());
        this.dropSelf(NtmBlocks.DUCRETE.get());
        this.dropSelf(NtmBlocks.DUCRETE_SMOOTH.get());
        this.dropSelf(NtmBlocks.DUCRETE_REINFORCED.get());
        this.dropSelf(NtmBlocks.DUCRETE_BRICK.get());
        this.dropSelf(NtmBlocks.REINFORCED_LAMINATE.get());
        this.dropSelf(NtmBlocks.STEEL_SCAFFOLD.get());
        this.dropSelf(NtmBlocks.SAND_QUARTZ.get());
        this.dropSelf(NtmBlocks.GLASS_QUARTZ.get());
        this.dropSelf(NtmBlocks.GLASS_LEAD.get());
        this.dropSelf(NtmBlocks.GLASS_BORON.get());

        this.dropSelf(NtmBlocks.ANVIL.get());

        this.add(NtmBlocks.BRICK_CONCRETE_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.BRICK_CONCRETE_MOSSY_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.BRICK_CONCRETE_BROKEN_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.BRICK_CONCRETE_CRACKED_SLAB.get(), this::createSlabItemTable);

        this.dropSelf(NtmBlocks.BRICK_CONCRETE_STAIRS.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_MOSSY_STAIRS.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_BROKEN_STAIRS.get());
        this.dropSelf(NtmBlocks.BRICK_CONCRETE_CRACKED_STAIRS.get());

        /* Runde 89 */
        this.dropSelf(NtmBlocks.REINFORCED_STONE.get());
        this.dropSelf(NtmBlocks.REINFORCED_BRICK.get());
        this.dropSelf(NtmBlocks.REINFORCED_SAND.get());
        this.dropSelf(NtmBlocks.REINFORCED_LIGHT.get());
        this.dropSelf(NtmBlocks.REINFORCED_LAMP.get());
        this.dropSelf(NtmBlocks.REINFORCED_GLASS.get());
        this.dropSelf(NtmBlocks.REINFORCED_GLASS_PANE.get());
        this.dropSelf(NtmBlocks.BRICK_COMPOUND.get());

        this.add(NtmBlocks.CONCRETE_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.CONCRETE_SMOOTH_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.CONCRETE_ASBESTOS_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.REINFORCED_STONE_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.REINFORCED_BRICK_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.BRICK_LIGHT_SLAB.get(), this::createSlabItemTable);
        this.add(NtmBlocks.BRICK_COMPOUND_SLAB.get(), this::createSlabItemTable);

        this.dropSelf(NtmBlocks.CONCRETE_STAIRS.get());
        this.dropSelf(NtmBlocks.CONCRETE_SMOOTH_STAIRS.get());
        this.dropSelf(NtmBlocks.CONCRETE_ASBESTOS_STAIRS.get());
        this.dropSelf(NtmBlocks.REINFORCED_STONE_STAIRS.get());
        this.dropSelf(NtmBlocks.REINFORCED_BRICK_STAIRS.get());
        this.dropSelf(NtmBlocks.BRICK_LIGHT_STAIRS.get());
        this.dropSelf(NtmBlocks.BRICK_OBSIDIAN_STAIRS.get());
        this.dropSelf(NtmBlocks.BRICK_COMPOUND_STAIRS.get());

        /* Runde 90: die Rohrfamilie. */
        this.dropSelf(NtmBlocks.DECO_PIPE.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_GREEN.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_GREEN_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_MARKED.get());

        this.dropSelf(NtmBlocks.DECO_PIPE_RIM.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RIM_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RIM_GREEN.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RIM_GREEN_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RIM_RED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_RIM_MARKED.get());

        this.dropSelf(NtmBlocks.DECO_PIPE_QUAD.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_QUAD_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_QUAD_GREEN.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_QUAD_GREEN_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_QUAD_RED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_QUAD_MARKED.get());

        this.dropSelf(NtmBlocks.DECO_PIPE_FRAMED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_FRAMED_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_FRAMED_GREEN.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_FRAMED_GREEN_RUSTED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_FRAMED_RED.get());
        this.dropSelf(NtmBlocks.DECO_PIPE_FRAMED_MARKED.get());

        /* Runde 91. */
        this.dropSelf(NtmBlocks.METEOR_POLISHED.get());
        this.dropSelf(NtmBlocks.METEOR_BRICK.get());
        this.dropSelf(NtmBlocks.METEOR_BRICK_CHISELED.get());
        this.dropSelf(NtmBlocks.METEOR_PILLAR.get());
        this.dropSelf(NtmBlocks.METEOR_BATTERY.get());
        this.dropSelf(NtmBlocks.TILE_LAB.get());
        this.dropSelf(NtmBlocks.TILE_LAB_CRACKED.get());
        this.dropSelf(NtmBlocks.TILE_LAB_BROKEN.get());
        this.dropSelf(NtmBlocks.LIGHTSTONE.get());
        this.dropSelf(NtmBlocks.LIGHTSTONE_TILE.get());
        this.dropSelf(NtmBlocks.LIGHTSTONE_BRICKS.get());
        this.dropSelf(NtmBlocks.LIGHTSTONE_BRICKS_CHISELED.get());
        this.dropSelf(NtmBlocks.LIGHTSTONE_CHISELED.get());
        this.dropSelf(NtmBlocks.LIGHTSTONE_BRICKS_STAIRS.get());

        /* Runde 92. */
        this.dropSelf(NtmBlocks.CONCRETE_WHITE.get());
        this.dropSelf(NtmBlocks.CONCRETE_ORANGE.get());
        this.dropSelf(NtmBlocks.CONCRETE_MAGENTA.get());
        this.dropSelf(NtmBlocks.CONCRETE_LIGHT_BLUE.get());
        this.dropSelf(NtmBlocks.CONCRETE_YELLOW.get());
        this.dropSelf(NtmBlocks.CONCRETE_LIME.get());
        this.dropSelf(NtmBlocks.CONCRETE_PINK.get());
        this.dropSelf(NtmBlocks.CONCRETE_GRAY.get());
        this.dropSelf(NtmBlocks.CONCRETE_LIGHT_GRAY.get());
        this.dropSelf(NtmBlocks.CONCRETE_CYAN.get());
        this.dropSelf(NtmBlocks.CONCRETE_PURPLE.get());
        this.dropSelf(NtmBlocks.CONCRETE_BLUE.get());
        this.dropSelf(NtmBlocks.CONCRETE_BROWN.get());
        this.dropSelf(NtmBlocks.CONCRETE_GREEN.get());
        this.dropSelf(NtmBlocks.CONCRETE_RED.get());
        this.dropSelf(NtmBlocks.CONCRETE_BLACK.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_MACHINE.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_MACHINE_STRIPE.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_INDIGO.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_PURPLE.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_PINK.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_HAZARD.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_SAND.get());
        this.dropSelf(NtmBlocks.CONCRETE_EXT_BRONZE.get());
        this.dropSelf(NtmBlocks.CONCRETE_PILLAR.get());
        this.dropSelf(NtmBlocks.CONCRETE_REBAR.get());
        this.dropSelf(NtmBlocks.CONCRETE_SUPER.get());
        this.dropSelf(NtmBlocks.CONCRETE_SUPER_BROKEN.get());

        this.dropSelf(NtmBlocks.BARBED_WIRE.get());
        this.dropSelf(NtmBlocks.SPIKES.get());

        this.add(NtmBlocks.WASTE_EARTH.get(), block -> createSingleItemTable(Blocks.DIRT));
        this.dropSelf(NtmBlocks.WASTE_MYCELIUM.get());
        this.dropSelf(NtmBlocks.WASTE_TRINITITE.get());
        this.dropSelf(NtmBlocks.WASTE_TRINITITE_RED.get());
        this.add(NtmBlocks.WASTE_LOG.get(), block ->
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(AlternativesEntry.alternatives(
                                        LootItem.lootTableItem(NtmItems.BURNT_BARK.get()).when(LootItemRandomChanceCondition.randomChance(0.001f)).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))),
                                        LootItem.lootTableItem(Items.COAL).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))
                                ))
                )
        );
        this.add(NtmBlocks.WASTE_LEAVES.get(), BlockLootSubProvider::createShearsOnlyDrop);
        this.dropSelf(NtmBlocks.WASTE_PLANKS.get());
        this.add(NtmBlocks.FROZEN_DIRT.get(), block -> createSingleItemTable(Items.SNOWBALL));
        this.add(NtmBlocks.FROZEN_GRASS.get(), block -> createSingleItemTable(Items.SNOWBALL));
        this.add(NtmBlocks.FROZEN_LOG.get(), block -> createSingleItemTable(Items.SNOWBALL));
        this.add(NtmBlocks.FROZEN_PLANKS.get(), block -> createSingleItemTable(Items.SNOWBALL));
        this.add(NtmBlocks.LEAVES_LAYER.get(), BlockLootSubProvider::createShearsOnlyDrop);
        this.dropSelf(NtmBlocks.FALLOUT.get()); // todo make item drop
        this.dropSelf(NtmBlocks.SELLAFIELD_SLAKED.get());
        this.add(NtmBlocks.ORE_SELLAFIELD_EMERALD.get(), block -> this.createOreDrop(block, Items.EMERALD));
        this.add(NtmBlocks.ORE_SELLAFIELD_DIAMOND.get(), block -> this.createOreDrop(block, Items.DIAMOND));
        // SELLAFIELD_BEDROCK has no drops

        this.dropSelf(NtmBlocks.NUKE_GADGET.get());
        this.dropSelf(NtmBlocks.NUKE_LITTLE_BOY.get());
        this.dropSelf(NtmBlocks.NUKE_FAT_MAN.get());
        this.dropSelf(NtmBlocks.NUKE_IVY_MIKE.get());
        this.dropSelf(NtmBlocks.NUKE_TSAR_BOMBA.get());
        this.dropSelf(NtmBlocks.NUKE_PROTOTYPE.get());
        this.dropSelf(NtmBlocks.NUKE_FLEIJA.get());
        this.dropSelf(NtmBlocks.NUKE_SOLINIUM.get());
        this.dropSelf(NtmBlocks.NUKE_N2.get());
        this.dropSelf(NtmBlocks.NUKE_FSTBMB.get());

        // CRASHED_BOMB has no drops
        this.dropSelf(NtmBlocks.DYNAMITE.get());
        this.dropSelf(NtmBlocks.TNT.get());
        this.dropSelf(NtmBlocks.SEMTEX.get());
        this.dropSelf(NtmBlocks.C4.get());
        this.dropSelf(NtmBlocks.FISSURE_BOMB.get());

        this.dropSelf(NtmBlocks.MINE_AP.get());
        this.dropSelf(NtmBlocks.MINE_HE.get());
        this.dropSelf(NtmBlocks.MINE_SHRAP.get());
        this.dropSelf(NtmBlocks.MINE_FAT.get());
        this.dropSelf(NtmBlocks.MINE_NAVAL.get());

        this.dropSelf(NtmBlocks.DET_CORD.get());
        this.dropSelf(NtmBlocks.DET_CHARGE.get());
        this.dropSelf(NtmBlocks.DET_NUKE.get());
        this.dropSelf(NtmBlocks.DET_MINER.get());
        this.dropSelf(NtmBlocks.BARREL_RED.get());
        this.dropSelf(NtmBlocks.BARREL_PINK.get());
        this.dropSelf(NtmBlocks.BARREL_LOX.get());
        this.dropSelf(NtmBlocks.BARREL_TAINT.get());
        this.dropSelf(NtmBlocks.BARREL_PLASTIC.get());
        this.dropSelf(NtmBlocks.BARREL_STEEL.get());
        this.dropSelf(NtmBlocks.BARREL_CORRODED.get());
        this.dropSelf(NtmBlocks.BARREL_TCALLOY.get());
        this.dropSelf(NtmBlocks.CRATE_IRON.get());
        this.dropSelf(NtmBlocks.CRATE_TUNGSTEN.get());
        this.dropSelf(NtmBlocks.CRATE_STEEL.get());
        this.dropSelf(NtmBlocks.CRATE_DESH.get());
        this.dropSelf(NtmBlocks.CRATE_TEMPLATE.get());

        this.dropSelf(NtmBlocks.GEIGER.get());

        this.dropSelf(NtmBlocks.PRESS_PREHEATER.get());
        this.dropSelf(NtmBlocks.MACHINE_PRESS.get());

        this.dropSelf(NtmBlocks.REACTOR_ZIRNOX.get());
        this.dropSelf(NtmBlocks.ZIRNOX_DESTROYED.get());

        this.dropSelf(NtmBlocks.RED_CABLE.get());
        this.dropSelf(NtmBlocks.RED_WIRE_COATED.get());
        this.dropSelf(NtmBlocks.STEEL_BEAM.get());
        this.dropSelf(NtmBlocks.STEEL_GRATE.get());
        this.dropSelf(NtmBlocks.STONE_GNEISS.get());
        this.dropSelf(NtmBlocks.CABLE_SWITCH.get());
        this.dropSelf(NtmBlocks.RED_CONNECTOR.get());
        this.dropSelf(NtmBlocks.RED_CONNECTOR_SUPER.get());
        this.dropSelf(NtmBlocks.RED_PYLON.get());
        this.dropSelf(NtmBlocks.RED_PYLON_STEEL.get());
        this.dropSelf(NtmBlocks.RED_PYLON_MEDIUM_WOOD.get());
        this.dropSelf(NtmBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get());
        this.dropSelf(NtmBlocks.RED_PYLON_MEDIUM_STEEL.get());
        this.dropSelf(NtmBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get());
        this.dropSelf(NtmBlocks.RED_PYLON_LARGE.get());
        this.dropSelf(NtmBlocks.SUBSTATION.get());

        this.dropSelf(NtmBlocks.FLUID_DUCT_NEO.get());
        this.dropSelf(NtmBlocks.RADIO_TORCH_SENDER.get());
        this.dropSelf(NtmBlocks.RADIO_TORCH_RECEIVER.get());

        this.dropSelf(NtmBlocks.MACHINE_BATTERY_SOCKET.get());
        this.dropSelf(NtmBlocks.MACHINE_BATTERY_REDD.get());
        this.dropSelf(NtmBlocks.MACHINE_ASSEMBLY_MACHINE.get());
        this.dropSelf(NtmBlocks.MACHINE_PRECASS.get());
        this.dropSelf(NtmBlocks.MACHINE_ORE_SLOPPER.get());
        this.dropSelf(NtmBlocks.MACHINE_EXCAVATOR.get());
        this.dropSelf(NtmBlocks.MACHINE_MISSILE_ASSEMBLY.get());
        /* Beide sind unzerstoerbar; sie fallen nie als Gegenstand an. */
        this.add(NtmBlocks.ORE_BEDROCK.get(), noDrop());
        this.add(NtmBlocks.STONE_DEPTH.get(), noDrop());
        this.dropSelf(NtmBlocks.MACHINE_FLUID_TANK.get());
        this.dropSelf(NtmBlocks.MACHINE_CHUNGUS.get());
        this.dropSelf(NtmBlocks.MACHINE_INDUSTRIAL_TURBINE.get());
        this.dropSelf(NtmBlocks.MACHINE_STRAND_CASTER.get());
        this.dropSelf(NtmBlocks.MACHINE_HEPHAESTUS.get());
        this.dropSelf(NtmBlocks.MACHINE_TELEPORTER.get());
        this.dropSelf(NtmBlocks.MACHINE_KEY_FORGE.get());
        this.dropSelf(NtmBlocks.MACHINE_DETECTOR.get());
        this.dropSelf(NtmBlocks.MACHINE_CHEMICAL_FACTORY.get());
        this.dropSelf(NtmBlocks.MACHINE_ASSEMBLY_FACTORY.get());
        this.dropSelf(NtmBlocks.MACHINE_SOLDERING_STATION.get());
        this.dropSelf(NtmBlocks.HEATER_FIREBOX.get());
        this.dropSelf(NtmBlocks.HEATER_OVEN.get());
        this.dropSelf(NtmBlocks.HEATER_OILBURNER.get());
        this.dropSelf(NtmBlocks.HEATER_ELECTRIC.get());
        this.dropSelf(NtmBlocks.HEATER_HEATEX.get());
        this.dropSelf(NtmBlocks.MACHINE_WELL.get());
        this.dropSelf(NtmBlocks.MACHINE_PUMPJACK.get());
        this.dropSelf(NtmBlocks.MACHINE_FRACKING_TOWER.get());
        this.dropSelf(NtmBlocks.MACHINE_REFINERY.get());
        this.dropSelf(NtmBlocks.MACHINE_FRACTION_TOWER.get());
        this.dropSelf(NtmBlocks.FRACTION_SPACER.get());
        this.dropSelf(NtmBlocks.MACHINE_CATALYTIC_REFORMER.get());
        this.dropSelf(NtmBlocks.MACHINE_HYDROTREATER.get());
        this.dropSelf(NtmBlocks.MACHINE_VACUUM_DISTILL.get());
        this.dropSelf(NtmBlocks.MACHINE_SOLIDIFIER.get());
        this.dropSelf(NtmBlocks.MACHINE_PYRO_OVEN.get());
        this.dropSelf(NtmBlocks.MACHINE_LIQUEFACTOR.get());
        this.dropSelf(NtmBlocks.MACHINE_GAS_FLARE.get());
        this.dropSelf(NtmBlocks.MACHINE_BLAST_FURNACE.get());
        this.dropSelf(NtmBlocks.MACHINE_WOOD_BURNER.get());
        this.dropSelf(NtmBlocks.MACHINE_CENTRIFUGE.get());
        this.dropSelf(NtmBlocks.MACHINE_ARC_FURNACE.get());
        this.dropSelf(NtmBlocks.MACHINE_ROTARY_FURNACE.get());
        this.dropSelf(NtmBlocks.MACHINE_CRUCIBLE.get());
        this.dropSelf(NtmBlocks.FOUNDRY_CHANNEL.get());
        this.dropSelf(NtmBlocks.FOUNDRY_MOLD.get());
        this.dropSelf(NtmBlocks.FOUNDRY_BASIN.get());
        this.dropSelf(NtmBlocks.RBMK_GAUGE.get());
        this.dropSelf(NtmBlocks.RBMK_INDICATOR.get());
        this.dropSelf(NtmBlocks.RBMK_NUMITRON.get());
        this.dropSelf(NtmBlocks.RBMK_LEVER.get());
        this.dropSelf(NtmBlocks.RBMK_KEYPAD.get());
        this.dropSelf(NtmBlocks.RBMK_GRAPH.get());
        this.dropSelf(NtmBlocks.RBMK_DISPLAY.get());
        this.dropSelf(NtmBlocks.RBMK_TERMINAL.get());
        this.dropSelf(NtmBlocks.RBMK_DISPLAY_BLANK.get());
        this.dropSelf(NtmBlocks.BLOCK_CORIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_CORIUM_COBBLE.get());
        this.dropSelf(NtmBlocks.MACHINE_PUREX.get());
        this.dropSelf(NtmBlocks.MACHINE_RADIOLYSIS.get());
        this.dropSelf(NtmBlocks.MACHINE_CHEMICAL_PLANT.get());
        this.dropSelf(NtmBlocks.MACHINE_ARC_WELDER.get());
        this.dropSelf(NtmBlocks.MACHINE_SHREDDER.get());
        this.dropSelf(NtmBlocks.MACHINE_ELECTRIC_FURNACE.get());
        this.dropSelf(NtmBlocks.MACHINE_RTG_FURNACE.get());
        this.dropSelf(NtmBlocks.MACHINE_DIFURNACE_RTG.get());
        this.dropSelf(NtmBlocks.MACHINE_DIFURNACE.get());
        this.dropSelf(NtmBlocks.MACHINE_COMBUSTION_ENGINE.get());
        this.dropSelf(NtmBlocks.MACHINE_TURBINEGAS.get());
        this.dropSelf(NtmBlocks.MACHINE_TURBOFAN.get());
        this.dropSelf(NtmBlocks.MACHINE_CRYSTALLIZER.get());
        this.dropSelf(NtmBlocks.MACHINE_ASHPIT.get());
        this.dropSelf(NtmBlocks.CHIMNEY_BRICK.get());
        this.dropSelf(NtmBlocks.CHIMNEY_INDUSTRIAL.get());
        this.dropSelf(NtmBlocks.MACHINE_CONVERTER_HE_RF.get());
        this.dropSelf(NtmBlocks.MACHINE_CONVERTER_RF_HE.get());
        this.dropSelf(NtmBlocks.MACHINE_THRESHER.get());
        this.dropSelf(NtmBlocks.MACHINE_AUTOSAW.get());
        this.dropSelf(NtmBlocks.MACHINE_SAWMILL.get());
        this.dropSelf(NtmBlocks.MACHINE_RTG.get());
        this.dropSelf(NtmBlocks.MACHINE_CONDENSER.get());
        this.dropSelf(NtmBlocks.MACHINE_CONDENSER_POWERED.get());
        this.dropSelf(NtmBlocks.CABLE_DIODE.get());
        this.dropSelf(NtmBlocks.CABLE_DETECTOR.get());
        this.dropSelf(NtmBlocks.RED_CABLE_GAUGE.get());
        this.dropSelf(NtmBlocks.MACHINE_BATTERY_POTATO.get());
        this.dropSelf(NtmBlocks.MACHINE_BATTERY.get());
        this.dropSelf(NtmBlocks.MACHINE_LITHIUM_BATTERY.get());
        this.dropSelf(NtmBlocks.MACHINE_SCHRABIDIUM_BATTERY.get());
        this.dropSelf(NtmBlocks.MACHINE_DINEUTRONIUM_BATTERY.get());
        this.dropSelf(NtmBlocks.MACHINE_DIFURNACE_EXTENSION.get());
        this.dropSelf(NtmBlocks.MACHINE_DIESEL.get());
        this.dropSelf(NtmBlocks.MACHINE_COMPRESSOR.get());
        this.dropSelf(NtmBlocks.MACHINE_GAS_CENT.get());
        this.dropSelf(NtmBlocks.MACHINE_CYCLOTRON.get());
        this.dropSelf(NtmBlocks.MACHINE_PA_SOURCE.get());
        this.dropSelf(NtmBlocks.MACHINE_PA_BEAMLINE.get());
        this.dropSelf(NtmBlocks.MACHINE_PA_RFC.get());
        this.dropSelf(NtmBlocks.MACHINE_PA_QUADRUPOLE.get());
        this.dropSelf(NtmBlocks.MACHINE_PA_DIPOLE.get());
        this.dropSelf(NtmBlocks.MACHINE_PA_DETECTOR.get());
        this.dropSelf(NtmBlocks.MACHINE_EXPOSURE_CHAMBER.get());
        this.dropSelf(NtmBlocks.MACHINE_RAD_GEN.get());
        this.dropSelf(NtmBlocks.MACHINE_MINING_LASER.get());
        /* Die Barrikade steht hier absichtlich NICHT: der Block traegt noLootTable(), seine
         * Tabelle ist damit die leere von Minecraft. Wer ihm trotzdem eine anlegt, bekommt
         * "Created block loot tables for non-blocks: [minecraft:empty]" -- die Tabelle bleibt
         * uebrig, weil kein Block sie abholt. */
        this.dropSelf(NtmBlocks.MACHINE_COMPRESSOR_COMPACT.get());
        this.dropSelf(NtmBlocks.MACHINE_TURBINE.get());
        this.dropSelf(NtmBlocks.MACHINE_SOLAR_BOILER.get());
        this.dropSelf(NtmBlocks.SOLAR_MIRROR.get());
        this.dropSelf(NtmBlocks.FURNACE_IRON.get());
        this.dropSelf(NtmBlocks.FURNACE_STEEL.get());
        this.dropSelf(NtmBlocks.MACHINE_MIXER.get());
        this.dropSelf(NtmBlocks.MACHINE_ROCK_MILL.get());
        this.dropSelf(NtmBlocks.MACHINE_STEAM_ENGINE.get());
        this.dropSelf(NtmBlocks.MACHINE_STIRLING.get());
        this.dropSelf(NtmBlocks.HEAT_BOILER.get());
        this.dropSelf(NtmBlocks.MACHINE_INDUSTRIAL_BOILER.get());
        this.dropSelf(NtmBlocks.FURNACE_COMBINATION.get());
        this.dropSelf(NtmBlocks.MACHINE_DRAIN.get());
        this.dropSelf(NtmBlocks.MACHINE_INTAKE.get());
        this.dropSelf(NtmBlocks.PUMP_ELECTRIC.get());
        this.dropSelf(NtmBlocks.PUMP_STEAM.get());
        this.dropSelf(NtmBlocks.MACHINE_AUTOCRAFTER.get());
        this.dropSelf(NtmBlocks.MACHINE_EPRESS.get());
        this.dropSelf(NtmBlocks.MACHINE_FUNNEL.get());
        this.dropSelf(NtmBlocks.EMP_BOMB.get());
        this.dropSelf(NtmBlocks.TRANSFORMER.get());

        this.dropSelf(NtmBlocks.MACHINE_SATLINKER.get());
        this.dropSelf(NtmBlocks.MACHINE_SAT_LINK.get());
        this.dropSelf(NtmBlocks.MACHINE_SAT_DOCK.get());
        this.dropSelf(NtmBlocks.MACHINE_TAPE_DRIVE.get());
        this.dropSelf(NtmBlocks.MACHINE_SUPER_COMPUTER.get());
        this.dropSelf(NtmBlocks.MACHINE_AMMO_PRESS.get());
        this.dropSelf(NtmBlocks.RADAR_SCREEN.get());
        this.dropSelf(NtmBlocks.MACHINE_SIREN.get());
        this.dropSelf(NtmBlocks.MACHINE_ANNIHILATOR.get());

        this.dropSelf(NtmBlocks.DECONTAMINATOR.get());

        this.dropSelf(NtmBlocks.PWR_CONTROLLER.get());
        this.dropSelf(NtmBlocks.PWR_CASING.get());
        this.dropSelf(NtmBlocks.PWR_REFLECTOR.get());
        this.dropSelf(NtmBlocks.PWR_PORT.get());
        this.dropSelf(NtmBlocks.PWR_HEATEX.get());
        this.dropSelf(NtmBlocks.PWR_HEATSINK.get());
        this.dropSelf(NtmBlocks.PWR_NEUTRON_SOURCE.get());
        this.dropSelf(NtmBlocks.PWR_FUEL_CHANNEL.get());
        this.dropSelf(NtmBlocks.PWR_CONTROL.get());
        this.dropSelf(NtmBlocks.PWR_CHANNEL.get());
        this.dropSelf(NtmBlocks.STRUCT_WATZ_CORE.get());
        this.dropSelf(NtmBlocks.WATZ_ELEMENT.get());
        this.dropSelf(NtmBlocks.WATZ_COOLER.get());
        this.dropSelf(NtmBlocks.WATZ_END.get());
        this.dropSelf(NtmBlocks.WATZ_PUMP.get());
        this.dropSelf(NtmBlocks.PILE_BRICK.get());
        /* PILE_BLOCK bekommt keine Beutetabelle -- er setzt beim Abbauen selbst einen Graphitziegel. */
        this.add(NtmBlocks.PILE_BLOCK.get(), noDrop());
        this.dropSelf(NtmBlocks.PILE_LOADER.get());
        this.dropSelf(NtmBlocks.PILE_VENT.get());
        this.dropSelf(NtmBlocks.PILE_CONTROL.get());
        this.dropSelf(NtmBlocks.ICF_COMPONENT.get());
        this.dropSelf(NtmBlocks.ICF_LASER_COMPONENT.get());
        this.dropSelf(NtmBlocks.ICF_CONTROLLER.get());
        this.dropSelf(NtmBlocks.STRUCT_ICF.get());
        this.dropSelf(NtmBlocks.MACHINE_ICF_PRESS.get());
        this.dropSelf(NtmBlocks.FUSION_TORUS.get());
        this.dropSelf(NtmBlocks.FUSION_BREEDER.get());
        this.dropSelf(NtmBlocks.FUSION_COLLECTOR.get());
        this.dropSelf(NtmBlocks.FUSION_COUPLER.get());
        this.dropSelf(NtmBlocks.FUSION_BOILER.get());
        this.dropSelf(NtmBlocks.FUSION_MHDT.get());
        this.dropSelf(NtmBlocks.FUSION_KLYSTRON.get());
        this.dropSelf(NtmBlocks.FUSION_PLASMA_FORGE.get());
        this.dropSelf(NtmBlocks.FUSION_COMPONENT.get());
        this.dropSelf(NtmBlocks.REACTOR_RESEARCH.get());
        this.dropSelf(NtmBlocks.MACHINE_REACTOR_BREEDING.get());
        this.dropSelf(NtmBlocks.REACTOR_CONTROL.get());
        this.dropSelf(NtmBlocks.TURRET_SENTRY.get());
        this.dropSelf(NtmBlocks.TURRET_SENTRY_DAMAGED.get());
        this.dropSelf(NtmBlocks.TURRET_JEREMY.get());
        this.dropSelf(NtmBlocks.TURRET_HOWARD.get());
        this.dropSelf(NtmBlocks.TURRET_HOWARD_DAMAGED.get());
        this.dropSelf(NtmBlocks.TURRET_CHEKHOV.get());
        this.dropSelf(NtmBlocks.TURRET_FRIENDLY.get());
        this.dropSelf(NtmBlocks.WEAPON_TABLE.get());
        this.dropSelf(NtmBlocks.ARMOR_TABLE.get());
        /* ICF_BLOCK und ICF bekommen keine Beutetabelle -- beide geben ihre Bauteile zurueck. */
        this.add(NtmBlocks.ICF_BLOCK.get(), noDrop());
        this.add(NtmBlocks.ICF.get(), noDrop());
        /* WATZ bekommt keine Beutetabelle -- er schuettet beim Abbauen seine Bauteile aus. */
        this.add(NtmBlocks.WATZ.get(), noDrop());

        /* PWR_BLOCK bekommt keine Beutetabelle -- er gibt beim Abbauen sein Bauteil zurueck. */

        this.add(NtmBlocks.BALEFIRE.get(), noDrop());
        this.add(NtmBlocks.FIRE_DIGAMMA.get(), noDrop());
        this.add(NtmBlocks.VOLCANO_CORE.get(), noDrop());
        this.add(NtmBlocks.VOLCANO_RAD_CORE.get(), noDrop());

        this.dropSelf(NtmBlocks.LAUNCH_PAD.get());
        this.dropSelf(NtmBlocks.LAUNCH_PAD_LARGE.get());
        this.dropSelf(NtmBlocks.SOYUZ_LAUNCHER.get());
        this.dropSelf(NtmBlocks.MACHINE_RADAR.get());
        this.dropSelf(NtmBlocks.MACHINE_RADAR_LARGE.get());

        // liquid blocks has no drops

        // gas blocks has no drops

        // ??? blocks has no drops

        this.dropSelf(NtmBlocks.BLOCK_ACTINIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_STEEL.get());
        this.dropSelf(NtmBlocks.BLOCK_ALUMINIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_ASBESTOS.get());
        this.dropSelf(NtmBlocks.BLOCK_GRAPHITE.get());
        this.dropSelf(NtmBlocks.BLOCK_BORON.get());
        this.dropSelf(NtmBlocks.BLOCK_AUSTRALIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_BERYLLIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_BISMUTH.get());
        this.dropSelf(NtmBlocks.BLOCK_CADMIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_CDALLOY.get());
        this.dropSelf(NtmBlocks.BLOCK_COLTAN.get());
        this.dropSelf(NtmBlocks.BLOCK_COMBINE_STEEL.get());
        this.dropSelf(NtmBlocks.BLOCK_COPPER.get());
        this.dropSelf(NtmBlocks.BLOCK_DESH.get());
        this.dropSelf(NtmBlocks.BLOCK_DINEUTRONIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_DURA_STEEL.get());
        this.dropSelf(NtmBlocks.BLOCK_EUPHEMIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_FOAM.get());
        this.dropSelf(NtmBlocks.BLOCK_LANTHANIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_LEAD.get());
        this.dropSelf(NtmBlocks.BLOCK_LITHIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_MAGNETIZED_TUNGSTEN.get());
        this.dropSelf(NtmBlocks.BLOCK_METEOR.get());
        this.add(NtmBlocks.BLOCK_METEOR_COBBLE.get(), block -> this.oreDrop(block, NtmItems.FRAGMENT_METEORITE.get(), 1, 3));
        this.add(NtmBlocks.BLOCK_METEOR_BROKEN.get(), block -> this.oreDrop(block, NtmItems.FRAGMENT_METEORITE.get(), 1, 3));
        this.dropSelf(NtmBlocks.BLOCK_METEOR_MOLTEN.get());
        this.add(NtmBlocks.BLOCK_METEOR_TREASURE.get(), this::meteorTreasureDrop);
        this.dropSelf(NtmBlocks.BLOCK_MOX_FUEL.get());
        this.dropSelf(NtmBlocks.BLOCK_NEPTUNIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_NIOBIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_NITER.get());
        this.dropSelf(NtmBlocks.BLOCK_PLUTONIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_PLUTONIUM_FUEL.get());
        this.dropSelf(NtmBlocks.BLOCK_POLONIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_PU238.get());
        this.dropSelf(NtmBlocks.BLOCK_PU239.get());
        this.dropSelf(NtmBlocks.BLOCK_PU240.get());
        this.dropSelf(NtmBlocks.BLOCK_PU_MIX.get());
        this.dropSelf(NtmBlocks.BLOCK_RA226.get());
        this.dropSelf(NtmBlocks.BLOCK_RED_COPPER.get());
        this.dropSelf(NtmBlocks.BLOCK_SATURNITE.get());
        this.dropSelf(NtmBlocks.BLOCK_SCHRABIDATE.get());
        this.dropSelf(NtmBlocks.BLOCK_SCHRABIDIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_SCHRABIDIUM_FUEL.get());
        this.dropSelf(NtmBlocks.BLOCK_SCHRARANIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_SMORE.get());
        this.dropSelf(NtmBlocks.BLOCK_SOLINIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_SULFUR.get());
        this.dropSelf(NtmBlocks.BLOCK_TANTALIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_TCALLOY.get());
        this.dropSelf(NtmBlocks.BLOCK_THORIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_THORIUM_FUEL.get());
        this.dropSelf(NtmBlocks.BLOCK_TITANIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_TUNGSTEN.get());
        this.dropSelf(NtmBlocks.BLOCK_U233.get());
        this.dropSelf(NtmBlocks.BLOCK_U235.get());
        this.dropSelf(NtmBlocks.BLOCK_U238.get());
        this.dropSelf(NtmBlocks.BLOCK_URANIUM.get());
        this.dropSelf(NtmBlocks.BLOCK_URANIUM_FUEL.get());
        this.dropSelf(NtmBlocks.BLOCK_WASTE.get());
        this.dropSelf(NtmBlocks.BLOCK_WASTE_PAINTED.get());
        this.dropSelf(NtmBlocks.BLOCK_WASTE_VITRIFIED.get());
        this.dropSelf(NtmBlocks.BLOCK_YELLOWCAKE.get());
        this.dropSelf(NtmBlocks.BLOCK_FIBERGLASS.get());
        this.dropSelf(NtmBlocks.BLOCK_INSULATOR.get());
        this.dropSelf(NtmBlocks.BLOCK_SLAG.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        ArrayList<Block> blocks = new ArrayList<>();
        NtmBlocks.BLOCKS.getEntries().stream().map(Holder::value).forEach(blocks::add);
        return blocks;
    }

    private LootTable.Builder oreDrop(Block block, Item item) {
        return this.oreDrop(block, item, 1, 1);
    }

    private LootTable.Builder oreDrop(Block block, Item item, int min, int max) {
        return this.createSilkTouchDispatchTable(block, LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                .apply(ApplyBonusCount.addOreBonusCount(
                        this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE))));
    }

    private LootTable.Builder oreDropNoFortune(Block block, Item item) {
        return this.createSilkTouchDispatchTable(block, LootItem.lootTableItem(item));
    }

    private LootTable.Builder meteorTreasureDrop(Block block) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(NtmItems.COBALT_PICKAXE.get()).setWeight(10))
                        .add(LootItem.lootTableItem(NtmItems.INGOT_ZIRCONIUM.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 16))))
                        .add(LootItem.lootTableItem(NtmItems.INGOT_NIOBIUM.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 16))))
                        .add(LootItem.lootTableItem(NtmItems.INGOT_COBALT.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 16))))
                        .add(LootItem.lootTableItem(NtmItems.INGOT_BORON.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 16))))
                        .add(LootItem.lootTableItem(NtmItems.INGOT_STARMETAL.get()).setWeight(5))
                        .add(LootItem.lootTableItem(NtmItems.CRYSTAL_GOLD.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                        .add(LootItem.lootTableItem(NtmItems.CIRCUIT_VACUUM_TUBE.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8))))
                        .add(LootItem.lootTableItem(NtmItems.CIRCUIT_MICROCHIP.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                        .add(LootItem.lootTableItem(NtmItems.LAUNCH_CODE_PIECE.get()).setWeight(5))
                        .add(LootItem.lootTableItem(NtmItems.GEM_ALEXANDRITE.get()).setWeight(1))
                );
    }

    public LootItemCondition.Builder propertyEquals(Block block, IntegerProperty property, int equals) {
        return LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(property, equals));
    }
}
