package com.hbm.datagen;

import com.google.gson.JsonObject;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ICustomBlockModelRegister;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.MachineDetectorBlock;
import com.hbm.blocks.machine.icf.ICFLaserComponentBlock;
import com.hbm.blocks.machine.icf.ICFWrapperBlock;
import com.hbm.blocks.generic.ToolConversionBlock;
import com.hbm.blocks.generic.BarbedWireBlock;
import com.hbm.blocks.generic.GrateBlock;
import com.hbm.blocks.generic.LayeringBlock;
import com.hbm.blocks.generic.OreBasaltBlock;
import com.hbm.blocks.generic.OreBasaltBlock.BasaltOreType;
import com.hbm.blocks.generic.SellafieldSlakedBlock;
import com.hbm.blocks.generic.UberConcreteBlock;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.blocks.network.ConveyorBaseBlock;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.blocks.network.ConveyorBendableBlock;
import com.hbm.blocks.network.ConveyorLiftBlock;
import com.hbm.blocks.network.FluidDuctConnectingBlock;
import com.hbm.blocks.states.NtmBlockStateProperties;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.model.loader.NtmGeometry.BakedModelType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class NtmBlockStateProvider extends BlockStateProvider {

    public NtmBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, NuclearTechMod.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {

        NtmBlocks.BLOCKS.getEntries().forEach(holder -> {
            Block block = holder.get();

            if(block instanceof ICustomBlockModelRegister icbmr) {
                ResourceLocation loc = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block));
                icbmr.registerModel(this, loc);
            }
        });

        this.registerRbmk();
        this.simpleCubeAllBlock(NtmBlocks.STONE_CRACKED);
        this.simpleCubeAllBlock(NtmBlocks.DIRT_DEAD);
        this.simpleCubeAllBlock(NtmBlocks.DIRT_OILY);
        this.simpleCubeAllBlock(NtmBlocks.SAND_OILY);
        this.simpleCubeAllBlock(NtmBlocks.SAND_RED_OILY);
        this.layeringBlock(NtmBlocks.OIL_SPILL.get(), modLoc("block/oil_spill"));

        /* Die vier Fluessigkeitsbloecke. Ihr Aussehen bestimmt der Fluid-Renderer aus der
         * Fluessigkeit selbst; aus der Blockstate holt sich das Spiel nur die Partikeltextur.
         * Ohne diese Zeilen hat der Block gar keine Blockstate -- das Spiel meldet ein
         * fehlendes Modell und zeichnet schwarz-violette Partikel. */
        this.fluidBlock(NtmBlocks.CORIUM, "corium_still");
        this.fluidBlock(NtmBlocks.MUD, "mud_still");
        this.fluidBlock(NtmBlocks.RAD_LAVA, "rad_lava_still");
        this.fluidBlock(NtmBlocks.VOLCANIC_LAVA, "volcanic_lava_still");
        this.simpleCubeAllBlock(NtmBlocks.ORE_OIL);
        this.simpleCubeAllBlock(NtmBlocks.ORE_OIL_EMPTY);
        this.simpleCubeAllBlock(NtmBlocks.ORE_OIL_SAND);
        this.simpleCubeAllBlock(NtmBlocks.ORE_BEDROCK_OIL);
        this.simpleCubeAllBlock(NtmBlocks.ORE_URANIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_URANIUM_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_URANIUM_SCORCHED);
        this.simpleCubeAllBlock(NtmBlocks.ORE_BERYLLIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_BERYLLIUM_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_TUNGSTEN);
        this.simpleCubeAllBlock(NtmBlocks.ORE_TUNGSTEN_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_TITANIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_TITANIUM_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_LEAD);
        this.simpleCubeAllBlock(NtmBlocks.ORE_LEAD_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_ALUMINIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_ALUMINIUM_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_ASBESTOS);
        this.simpleCubeAllBlock(NtmBlocks.ORE_ASBESTOS_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_THORIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_THORIUM_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_NITER);
        this.simpleCubeAllBlock(NtmBlocks.ORE_NITER_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_COBALT);
        this.simpleCubeAllBlock(NtmBlocks.ORE_COBALT_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_CINNABAR);
        this.simpleCubeAllBlock(NtmBlocks.ORE_CINNABAR_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_FLUORITE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_FLUORITE_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_METEOR_IRON);
        this.simpleCubeAllBlock(NtmBlocks.ORE_METEOR_COBALT);
        this.simpleCubeAllBlock(NtmBlocks.ORE_METEOR_ALUMINIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_METEOR_COPPER);
        this.simpleCubeAllBlock(NtmBlocks.ORE_METEOR_RARE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_RARE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_RARE_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_SULFUR);
        this.simpleCubeAllBlock(NtmBlocks.ORE_SULFUR_DEEPSLATE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_LIGNITE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_DEEPSLATE_LIGNITE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_SCHRABIDIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_NETHER_URANIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_NETHER_URANIUM_SCORCHED);
        this.simpleCubeAllBlock(NtmBlocks.ORE_NETHER_SCHRABIDIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_TIKITE);
        this.simpleCubeAllBlock(NtmBlocks.ORE_GNEISS_URANIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_GNEISS_URANIUM_SCORCHED);
        this.simpleCubeAllBlock(NtmBlocks.ORE_NETHER_PLUTONIUM);
        this.simpleCubeAllBlock(NtmBlocks.ORE_GNEISS_SCHRABIDIUM);
        this.simpleCubeAllBlock(NtmBlocks.RESOURCE_LIMESTONE);
        this.simpleCubeAllBlock(NtmBlocks.RESOURCE_BAUXITE);
        this.simpleCubeAllBlock(NtmBlocks.RESOURCE_HEMATITE);
        this.simpleCubeAllBlock(NtmBlocks.RESOURCE_MALACHITE);
        this.simpleCubeAllBlock(NtmBlocks.RESOURCE_CHRYSOTILE);
        this.simpleCubeAllBlock(NtmBlocks.RESOURCE_SULFUROUS_STONE);
        this.simpleCubeAllBlock(NtmBlocks.DECO_ALUMINIUM);
        this.simpleCubeAllBlock(NtmBlocks.DECO_BERYLLIUM);
        this.simpleCubeAllBlock(NtmBlocks.DECO_LEAD);
        this.simpleCubeAllBlock(NtmBlocks.DECO_STEEL);
        this.simpleCubeAllBlock(NtmBlocks.DECO_RUSTY_STEEL);
        this.simpleCubeAllBlock(NtmBlocks.DECO_TITANIUM);
        this.simpleCubeAllBlock(NtmBlocks.DECO_TUNGSTEN);
        this.simpleCubeAllBlock(NtmBlocks.DECO_RED_COPPER);
        this.simpleCubeAllBlock(NtmBlocks.DECO_ASBESTOS);
        // Die beiden RBMK-Deko-Bloecke tragen nicht ihre eigene Textur, sondern die
        // Saeulendecke des Originals -- darum die ausgeschriebene Form.
        this.simpleBlockWithItem(NtmBlocks.DECO_RBMK.get(), this.models().cubeAll(this.name(NtmBlocks.DECO_RBMK.get()), modLoc("block/rbmk_top")));
        this.simpleBlockWithItem(NtmBlocks.DECO_RBMK_SMOOTH.get(), this.models().cubeAll(this.name(NtmBlocks.DECO_RBMK_SMOOTH.get()), modLoc("block/rbmk_blank_top")));

        this.registerOreBasalt();

        this.logBlock(NtmBlocks.BASALT.get());
        this.simpleCubeAllBlock(NtmBlocks.BASALT_SMOOTH);
        this.simpleCubeAllBlock(NtmBlocks.BASALT_BRICK);
        this.simpleCubeAllBlock(NtmBlocks.BASALT_POLISHED);
        this.simpleCubeAllBlock(NtmBlocks.BASALT_TILES);

        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SCRAP);

        this.particleOnlyBlock(NtmBlocks.BOBBLEHEAD, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.PLUSHIE, modLoc("block/block_fiberglass_side"));
        this.simpleCubeAllBlock(NtmBlocks.PLANT_FLOWER);

        this.simpleCubeAllBlock(NtmBlocks.GRAVEL_OBSIDIAN);
        this.simpleCubeAllBlock(NtmBlocks.GRAVEL_DIAMOND);
        this.simpleCubeAllBlock(NtmBlocks.MOON_TURF);

        this.simpleCubeAllBlock(NtmBlocks.ASPHALT);
        this.simpleCubeAllBlock(NtmBlocks.ASPHALT_LIGHT);

        this.simpleCubeAllBlock(NtmBlocks.BRICK_CONCRETE);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_CONCRETE_MOSSY);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_CONCRETE_CRACKED);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_CONCRETE_BROKEN);
        this.simpleBlockWithItem(
                NtmBlocks.BRICK_CONCRETE_MARKED,
                this.models().cubeColumn(
                        name(NtmBlocks.BRICK_CONCRETE_MARKED),
                        blockTexture(NtmBlocks.BRICK_CONCRETE_MARKED),
                        blockTexture(NtmBlocks.BRICK_CONCRETE)
                )
        );
        this.simpleCubeAllBlock(NtmBlocks.BRICK_OBSIDIAN);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_LIGHT);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_ASBESTOS);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_FIRE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_SMOOTH);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_ASBESTOS);
        this.simpleCubeAllBlock(NtmBlocks.DUCRETE);
        this.simpleCubeAllBlock(NtmBlocks.DUCRETE_SMOOTH);
        this.simpleCubeAllBlock(NtmBlocks.DUCRETE_REINFORCED);
        this.simpleCubeAllBlock(NtmBlocks.DUCRETE_BRICK);
        this.simpleCubeAllBlock(NtmBlocks.REINFORCED_LAMINATE);
        this.simpleCubeAllBlock(NtmBlocks.STEEL_SCAFFOLD);
        this.simpleCubeAllBlock(NtmBlocks.SAND_QUARTZ);
        this.simpleCubeAllBlock(NtmBlocks.GLASS_QUARTZ);
        this.simpleCubeAllBlock(NtmBlocks.GLASS_LEAD);
        this.simpleCubeAllBlock(NtmBlocks.GLASS_BORON);
        this.simpleCubeAllBlock(NtmBlocks.TRANSFORMER);

        this.slabBlock(NtmBlocks.BRICK_CONCRETE_SLAB.get(), blockTexture(NtmBlocks.BRICK_CONCRETE), blockTexture(NtmBlocks.BRICK_CONCRETE));
        this.slabBlock(NtmBlocks.BRICK_CONCRETE_MOSSY_SLAB.get(), blockTexture(NtmBlocks.BRICK_CONCRETE_MOSSY), blockTexture(NtmBlocks.BRICK_CONCRETE_MOSSY));
        this.slabBlock(NtmBlocks.BRICK_CONCRETE_CRACKED_SLAB.get(), blockTexture(NtmBlocks.BRICK_CONCRETE_CRACKED), blockTexture(NtmBlocks.BRICK_CONCRETE_CRACKED));
        this.slabBlock(NtmBlocks.BRICK_CONCRETE_BROKEN_SLAB.get(), blockTexture(NtmBlocks.BRICK_CONCRETE_BROKEN), blockTexture(NtmBlocks.BRICK_CONCRETE_BROKEN));
        this.blockItem(NtmBlocks.BRICK_CONCRETE_SLAB);
        this.blockItem(NtmBlocks.BRICK_CONCRETE_MOSSY_SLAB);
        this.blockItem(NtmBlocks.BRICK_CONCRETE_CRACKED_SLAB);
        this.blockItem(NtmBlocks.BRICK_CONCRETE_BROKEN_SLAB);

        this.stairsBlock(NtmBlocks.BRICK_CONCRETE_STAIRS.get(), blockTexture(NtmBlocks.BRICK_CONCRETE));
        this.stairsBlock(NtmBlocks.BRICK_CONCRETE_MOSSY_STAIRS.get(), blockTexture(NtmBlocks.BRICK_CONCRETE_MOSSY));
        this.stairsBlock(NtmBlocks.BRICK_CONCRETE_CRACKED_STAIRS.get(), blockTexture(NtmBlocks.BRICK_CONCRETE_CRACKED));
        this.stairsBlock(NtmBlocks.BRICK_CONCRETE_BROKEN_STAIRS.get(), blockTexture(NtmBlocks.BRICK_CONCRETE_BROKEN));
        this.blockItem(NtmBlocks.BRICK_CONCRETE_STAIRS);
        this.blockItem(NtmBlocks.BRICK_CONCRETE_MOSSY_STAIRS);
        this.blockItem(NtmBlocks.BRICK_CONCRETE_CRACKED_STAIRS);
        this.blockItem(NtmBlocks.BRICK_CONCRETE_BROKEN_STAIRS);

        /* Runde 89: die Bloecke, die den Bauwerken des Originals fehlen. */
        this.simpleCubeAllBlock(NtmBlocks.REINFORCED_STONE);
        this.simpleCubeAllBlock(NtmBlocks.REINFORCED_BRICK);
        this.simpleCubeAllBlock(NtmBlocks.REINFORCED_SAND);
        this.simpleCubeAllBlock(NtmBlocks.REINFORCED_LIGHT);
        this.simpleCubeAllBlock(NtmBlocks.REINFORCED_GLASS);
        this.simpleCubeAllBlock(NtmBlocks.BRICK_COMPOUND);

        /* Die Panzerlampe hat zwei Bilder, eines je Schaltzustand. */
        ModelFile lampOff = this.models().cubeAll("reinforced_lamp_off", modLoc("block/reinforced_lamp_off"));
        ModelFile lampOn =  this.models().cubeAll("reinforced_lamp_on",  modLoc("block/reinforced_lamp_on"));
        this.getVariantBuilder(NtmBlocks.REINFORCED_LAMP.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(BlockStateProperties.LIT) ? lampOn : lampOff)
                .build());
        this.simpleBlockItem(NtmBlocks.REINFORCED_LAMP.get(), lampOff);

        /* Die Scheibe borgt sich das Gittermodell; ihre Kante hat eine eigene Textur. */
        this.paneBlock(NtmBlocks.REINFORCED_GLASS_PANE.get(), modLoc("block/reinforced_glass_pane"), modLoc("block/reinforced_glass_pane_edge"));
        this.itemModels().basicItem(NtmBlocks.REINFORCED_GLASS_PANE.asItem());

        this.slabBlock(NtmBlocks.CONCRETE_SLAB.get(), blockTexture(NtmBlocks.CONCRETE), blockTexture(NtmBlocks.CONCRETE));
        this.slabBlock(NtmBlocks.CONCRETE_SMOOTH_SLAB.get(), blockTexture(NtmBlocks.CONCRETE_SMOOTH), blockTexture(NtmBlocks.CONCRETE_SMOOTH));
        this.slabBlock(NtmBlocks.CONCRETE_ASBESTOS_SLAB.get(), blockTexture(NtmBlocks.CONCRETE_ASBESTOS), blockTexture(NtmBlocks.CONCRETE_ASBESTOS));
        this.slabBlock(NtmBlocks.REINFORCED_STONE_SLAB.get(), blockTexture(NtmBlocks.REINFORCED_STONE), blockTexture(NtmBlocks.REINFORCED_STONE));
        this.slabBlock(NtmBlocks.REINFORCED_BRICK_SLAB.get(), blockTexture(NtmBlocks.REINFORCED_BRICK), blockTexture(NtmBlocks.REINFORCED_BRICK));
        this.slabBlock(NtmBlocks.BRICK_LIGHT_SLAB.get(), blockTexture(NtmBlocks.BRICK_LIGHT), blockTexture(NtmBlocks.BRICK_LIGHT));
        this.slabBlock(NtmBlocks.BRICK_COMPOUND_SLAB.get(), blockTexture(NtmBlocks.BRICK_COMPOUND), blockTexture(NtmBlocks.BRICK_COMPOUND));
        this.blockItem(NtmBlocks.CONCRETE_SLAB);
        this.blockItem(NtmBlocks.CONCRETE_SMOOTH_SLAB);
        this.blockItem(NtmBlocks.CONCRETE_ASBESTOS_SLAB);
        this.blockItem(NtmBlocks.REINFORCED_STONE_SLAB);
        this.blockItem(NtmBlocks.REINFORCED_BRICK_SLAB);
        this.blockItem(NtmBlocks.BRICK_LIGHT_SLAB);
        this.blockItem(NtmBlocks.BRICK_COMPOUND_SLAB);

        this.stairsBlock(NtmBlocks.CONCRETE_STAIRS.get(), blockTexture(NtmBlocks.CONCRETE));
        this.stairsBlock(NtmBlocks.CONCRETE_SMOOTH_STAIRS.get(), blockTexture(NtmBlocks.CONCRETE_SMOOTH));
        this.stairsBlock(NtmBlocks.CONCRETE_ASBESTOS_STAIRS.get(), blockTexture(NtmBlocks.CONCRETE_ASBESTOS));
        this.stairsBlock(NtmBlocks.REINFORCED_STONE_STAIRS.get(), blockTexture(NtmBlocks.REINFORCED_STONE));
        this.stairsBlock(NtmBlocks.REINFORCED_BRICK_STAIRS.get(), blockTexture(NtmBlocks.REINFORCED_BRICK));
        this.stairsBlock(NtmBlocks.BRICK_LIGHT_STAIRS.get(), blockTexture(NtmBlocks.BRICK_LIGHT));
        this.stairsBlock(NtmBlocks.BRICK_OBSIDIAN_STAIRS.get(), blockTexture(NtmBlocks.BRICK_OBSIDIAN));
        this.stairsBlock(NtmBlocks.BRICK_COMPOUND_STAIRS.get(), blockTexture(NtmBlocks.BRICK_COMPOUND));
        this.blockItem(NtmBlocks.CONCRETE_STAIRS);
        this.blockItem(NtmBlocks.CONCRETE_SMOOTH_STAIRS);
        this.blockItem(NtmBlocks.CONCRETE_ASBESTOS_STAIRS);
        this.blockItem(NtmBlocks.REINFORCED_STONE_STAIRS);
        this.blockItem(NtmBlocks.REINFORCED_BRICK_STAIRS);
        this.blockItem(NtmBlocks.BRICK_LIGHT_STAIRS);
        this.blockItem(NtmBlocks.BRICK_OBSIDIAN_STAIRS);
        this.blockItem(NtmBlocks.BRICK_COMPOUND_STAIRS);

        /* Runde 90: die Rohrfamilie. */
        this.registerPipes();

        /* Runde 91: Meteoritenbau, Laborfliesen und Leuchtstein. */
        this.simpleCubeAllBlock(NtmBlocks.METEOR_POLISHED);
        this.simpleCubeAllBlock(NtmBlocks.METEOR_BRICK);
        this.simpleCubeAllBlock(NtmBlocks.METEOR_BRICK_CHISELED);
        this.logBlock(NtmBlocks.METEOR_PILLAR.get());
        this.blockItem(NtmBlocks.METEOR_PILLAR);
        /* Der Statikgenerator: oben die Sternenmetallspule, ringsum und unten die Wand des Bruters. */
        this.simpleBlockWithItem(NtmBlocks.METEOR_BATTERY, this.models().cubeTop(
                name(NtmBlocks.METEOR_BATTERY), modLoc("block/meteor_spawner_side"), modLoc("block/meteor_power")));

        this.simpleCubeAllBlock(NtmBlocks.TILE_LAB);
        this.simpleCubeAllBlock(NtmBlocks.TILE_LAB_CRACKED);
        this.simpleCubeAllBlock(NtmBlocks.TILE_LAB_BROKEN);

        this.simpleCubeAllBlock(NtmBlocks.LIGHTSTONE);
        this.simpleCubeAllBlock(NtmBlocks.LIGHTSTONE_TILE);
        this.simpleCubeAllBlock(NtmBlocks.LIGHTSTONE_BRICKS);
        /* Die beiden gemeisselten haben oben und unten ein eigenes Bild. */
        this.simpleBlockWithItem(NtmBlocks.LIGHTSTONE_BRICKS_CHISELED, this.models().cubeColumn(
                name(NtmBlocks.LIGHTSTONE_BRICKS_CHISELED), blockTexture(NtmBlocks.LIGHTSTONE_BRICKS_CHISELED), modLoc("block/lightstone_bricks_chiseled_top")));
        this.simpleBlockWithItem(NtmBlocks.LIGHTSTONE_CHISELED, this.models().cubeColumn(
                name(NtmBlocks.LIGHTSTONE_CHISELED), blockTexture(NtmBlocks.LIGHTSTONE_CHISELED), modLoc("block/lightstone_chiseled_top")));
        this.stairsBlock(NtmBlocks.LIGHTSTONE_BRICKS_STAIRS.get(), blockTexture(NtmBlocks.LIGHTSTONE_BRICKS));
        this.blockItem(NtmBlocks.LIGHTSTONE_BRICKS_STAIRS);

        /* Runde 94: die Foerderbaender. */
        this.conveyorBlock(NtmBlocks.CONVEYOR, "conveyor");
        this.conveyorBlock(NtmBlocks.CONVEYOR_EXPRESS, "conveyor_express");
        this.conveyorBlock(NtmBlocks.CONVEYOR_DOUBLE, "conveyor_double");
        this.conveyorBlock(NtmBlocks.CONVEYOR_TRIPLE, "conveyor_triple");
        this.conveyorVertical();
        this.craneBlock(NtmBlocks.CRANE_INSERTER);
        this.craneBlock(NtmBlocks.CRANE_EXTRACTOR);
        this.craneBlock(NtmBlocks.CRANE_GRABBER, modLoc("block/crane_pull"), modLoc("block/crane_side_pull"));

        /* Runde 100: Packer und Entpacker. Beide tragen an ihrer AUSGANGSflaeche das Paketbild
         * des Originals -- dort geht bei beiden das Paket durch, beim Packer hinaus, beim
         * Entpacker herein. */
        this.craneBlock(NtmBlocks.CRANE_BOXER, modLoc("block/crane_in"), modLoc("block/crane_side_in"),
                modLoc("block/crane_box"), modLoc("block/crane_side_box"));
        this.craneBlock(NtmBlocks.CRANE_UNBOXER, modLoc("block/crane_in"), modLoc("block/crane_side_in"),
                modLoc("block/crane_box"), modLoc("block/crane_side_box"));

        /*
         * Runde 101: der Verteiler. Er hat keine Seitenrollen -- jede seiner sechs Flaechen ist
         * gleichwertig --, also kein Kranmodell, sondern ein schlichter Wuerfel mit dem
         * Aufdruck des Originals darueber. Der Aufdruck liegt einen hundertstel Pixel vor der
         * Flaeche; ohne diesen Abstand flimmerten die beiden Lagen gegeneinander.
         */
        this.simpleBlock(NtmBlocks.CRANE_ROUTER.get(), this.models()
                .getBuilder("crane_router")
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .renderType("cutout")
                .texture("base", modLoc("block/crane_in"))
                .texture("overlay", modLoc("block/crane_router_overlay"))
                .texture("particle", modLoc("block/crane_in"))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(-0.01F, -0.01F, -0.01F).to(16.01F, 16.01F, 16.01F)
                .allFaces((dir, face) -> face.texture("#overlay")).end());
        this.blockItem(NtmBlocks.CRANE_ROUTER);

        /*
         * Runde 102: der Portionierer. Ein zwoelf Pixel hoher Kasten; die Flaeche, an der die
         * Ware hereinkommt, traegt das Bild des Originals, die uebrigen sein Seitenbild.
         */
        this.horizontalBlock(NtmBlocks.CRANE_PARTITIONER.get(), this.models()
                .withExistingParent("crane_partitioner", mcLoc("block/orientable"))
                .texture("top", modLoc("block/crane_top"))
                .texture("front", modLoc("block/crane_partitioner_back"))
                .texture("side", modLoc("block/crane_partitioner_side"))
                .texture("particle", modLoc("block/crane_partitioner_side")));
        this.blockItem(NtmBlocks.CRANE_PARTITIONER);

        /*
         * Die beiden Funkfackeln aus Runde 112. Sie hatten Beutetabelle und Rezepte, aber KEIN
         * Modell -- im Spiel waeren sie der schwarz-violette Ersatzwuerfel gewesen. Gefunden
         * beim Abgleich der 498 registrierten Bloecke gegen den Blockzustandsgeber.
         *
         * ABWEICHUNG: das Original hat je zwei Bilder, eines fuer an und eines fuer aus. Der
         * Block des Ports fuehrt nur FACING und keinen Leuchtzustand, deshalb steht hier das
         * Bild fuer "aus". Die beiden "an"-Bilder bleiben draussen, solange es den Zustand
         * nicht gibt.
         */
        this.directionalBlock(NtmBlocks.RADIO_TORCH_SENDER.get(), this.models()
                .cubeAll("radio_torch_sender", modLoc("block/rtty_sender_off")));
        this.blockItem(NtmBlocks.RADIO_TORCH_SENDER);

        this.directionalBlock(NtmBlocks.RADIO_TORCH_RECEIVER.get(), this.models()
                .cubeAll("radio_torch_receiver", modLoc("block/rtty_rec_off")));
        this.blockItem(NtmBlocks.RADIO_TORCH_RECEIVER);

        /*
         * Runde 103: die Weiche. Sie ist zwei Bloecke breit, und beide tragen verschiedene
         * Bilder -- der Kern die linke Spur, der Beiblock die rechte. Gewaehlt wird nach der
         * Bauteilart des Blockstates, gedreht nach seiner Richtung.
         */
        ModelFile splitterLeft = this.models().cubeBottomTop("crane_splitter_left",
                modLoc("block/crane_splitter_left"), modLoc("block/crane_side"), modLoc("block/crane_splitter_top_left"));
        ModelFile splitterRight = this.models().cubeBottomTop("crane_splitter_right",
                modLoc("block/crane_splitter_right"), modLoc("block/crane_side"), modLoc("block/crane_splitter_top_right"));

        this.getVariantBuilder(NtmBlocks.CRANE_SPLITTER.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(DummyableBlock.isCore(state) ? splitterLeft : splitterRight)
                .rotationY((int) state.getValue(DummyableBlock.FACING).toYRot())
                .build());

        this.simpleBlockItem(NtmBlocks.CRANE_SPLITTER.get(), splitterLeft);

        /* Runde 98: Tueren, Leitern, Zaun, Falltuer, Kette und Schmalspurgleis. */
        this.doorBlockWithRenderType(NtmBlocks.DOOR_METAL.get(), modLoc("block/door_metal_bottom"), modLoc("block/door_metal_top"), "cutout");
        this.doorBlockWithRenderType(NtmBlocks.DOOR_OFFICE.get(), modLoc("block/door_office_bottom"), modLoc("block/door_office_top"), "cutout");
        this.doorBlockWithRenderType(NtmBlocks.DOOR_BUNKER.get(), modLoc("block/door_bunker_bottom"), modLoc("block/door_bunker_top"), "cutout");
        this.trapdoorBlockWithRenderType(NtmBlocks.TRAPDOOR_STEEL.get(), modLoc("block/trapdoor_steel"), true, "cutout");
        this.blockItem(NtmBlocks.TRAPDOOR_STEEL, "_bottom");

        /* Die Leiter haengt an einer Wand; Modell und Gegenstandsbild teilen sich die Textur. */
        this.getVariantBuilder(NtmBlocks.LADDER_STEEL.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(this.models().withExistingParent("ladder_steel", mcLoc("block/ladder"))
                        .renderType("cutout").texture("texture", modLoc("block/ladder_steel"))
                        .texture("particle", modLoc("block/ladder_steel")))
                .rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot())
                .build(), BlockStateProperties.WATERLOGGED);
        this.itemModels().basicItem(NtmBlocks.LADDER_STEEL.asItem());

        this.fenceBlock(NtmBlocks.FENCE_METAL.get(), modLoc("block/fence_metal"));
        this.fenceBlock(NtmBlocks.FENCE_METAL_POST.get(), modLoc("block/fence_metal_post"));
        this.blockItem(NtmBlocks.FENCE_METAL, "_inventory");
        this.blockItem(NtmBlocks.FENCE_METAL_POST, "_inventory");

        this.axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) NtmBlocks.DUNGEON_CHAIN.get(),
                this.models().withExistingParent("dungeon_chain", mcLoc("block/chain"))
                        .renderType("cutout").texture("all", modLoc("block/dungeon_chain"))
                        .texture("particle", modLoc("block/dungeon_chain")),
                this.models().withExistingParent("dungeon_chain_horizontal", mcLoc("block/chain"))
                        .renderType("cutout").texture("all", modLoc("block/dungeon_chain"))
                        .texture("particle", modLoc("block/dungeon_chain")));
        this.itemModels().basicItem(NtmBlocks.DUNGEON_CHAIN.asItem());


        this.railBlock(NtmBlocks.RAIL_NARROW);

        /* Runde 92: der Rest der Betonfamilie. */
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_WHITE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_ORANGE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_MAGENTA);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_LIGHT_BLUE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_YELLOW);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_LIME);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_PINK);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_GRAY);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_LIGHT_GRAY);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_CYAN);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_PURPLE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_BLUE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_BROWN);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_GREEN);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_RED);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_BLACK);

        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_MACHINE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_INDIGO);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_PURPLE);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_PINK);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_HAZARD);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_SAND);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_EXT_BRONZE);
        /* Der gestreifte Maschinenbeton zeigt oben und unten den ungestreiften. */
        this.simpleBlockWithItem(NtmBlocks.CONCRETE_EXT_MACHINE_STRIPE, this.models().cubeColumn(
                name(NtmBlocks.CONCRETE_EXT_MACHINE_STRIPE), blockTexture(NtmBlocks.CONCRETE_EXT_MACHINE_STRIPE), blockTexture(NtmBlocks.CONCRETE_EXT_MACHINE)));

        this.logBlock(NtmBlocks.CONCRETE_PILLAR.get());
        this.blockItem(NtmBlocks.CONCRETE_PILLAR);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_REBAR);
        this.simpleCubeAllBlock(NtmBlocks.CONCRETE_SUPER_BROKEN);
        this.registerUberConcrete();

        this.registerBarbedWire();
        this.registerSpikes();

        this.simpleCubeBottomTopBlock(NtmBlocks.WASTE_EARTH);
        this.simpleBlockWithItem(
                NtmBlocks.WASTE_MYCELIUM,
                this.models().cubeBottomTop(
                        name(NtmBlocks.WASTE_MYCELIUM),
                        blockTexture(NtmBlocks.WASTE_MYCELIUM, "_side"),
                        blockTexture(NtmBlocks.WASTE_EARTH, "_bottom"),
                        blockTexture(NtmBlocks.WASTE_MYCELIUM, "_top")
                )
        );
        this.simpleCubeAllBlock(NtmBlocks.WASTE_TRINITITE);
        this.simpleCubeAllBlock(NtmBlocks.WASTE_TRINITITE_RED);
        this.logBlock(NtmBlocks.WASTE_LOG.get());
        this.simpleBlockWithItem(
                NtmBlocks.WASTE_LEAVES,
                this.models().cubeAll(
                        name(NtmBlocks.WASTE_LEAVES),
                        blockTexture(NtmBlocks.WASTE_LEAVES)
                ).renderType("cutout_mipped")
        );
        this.simpleCubeAllBlock(NtmBlocks.WASTE_PLANKS);
        this.simpleCubeAllBlock(NtmBlocks.FROZEN_DIRT);
        this.simpleBlockWithItem(
                NtmBlocks.FROZEN_GRASS,
                this.models().cubeBottomTop(
                        name(NtmBlocks.FROZEN_GRASS),
                        blockTexture(NtmBlocks.FROZEN_GRASS, "_side"),
                        blockTexture(NtmBlocks.FROZEN_DIRT),
                        blockTexture(NtmBlocks.FROZEN_GRASS, "_top")
                )
        );
        this.logBlock(NtmBlocks.FROZEN_LOG.get());
        this.simpleCubeAllBlock(NtmBlocks.FROZEN_PLANKS);
        this.layeringBlock(NtmBlocks.LEAVES_LAYER.get(), modLoc("block/waste_leaves"));
        ResourceLocation texture = modLoc("block/ash");
        ModelFile falloutModel = models()
                .getBuilder("fallout")
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("all", texture)
                .texture("particle", texture)
                .element()
                .from(0, 0, 0)
                .to(16, 2, 16)
                .face(Direction.UP).texture("#all").end()
                .face(Direction.DOWN).texture("#all").end()
                .face(Direction.NORTH).texture("#all").end()
                .face(Direction.SOUTH).texture("#all").end()
                .face(Direction.WEST).texture("#all").end()
                .face(Direction.EAST).texture("#all").end()
                .end();
        this.getVariantBuilder(NtmBlocks.FALLOUT.get()).partialState().setModels(new ConfiguredModel(falloutModel));
        this.sellafieldSlaked(NtmBlocks.SELLAFIELD_SLAKED.get(), "sellafield_slaked");
        this.sellafieldOre(NtmBlocks.ORE_SELLAFIELD_DIAMOND.get(), "sellafield_ore_diamond", "block/ore_diamond_overlay");
        this.sellafieldOre(NtmBlocks.ORE_SELLAFIELD_EMERALD.get(), "sellafield_ore_emerald", "block/ore_emerald_overlay");
        this.sellafieldSlaked(NtmBlocks.SELLAFIELD_BEDROCK.get(), "sellafield_bedrock");

        this.particleOnlyBlock(NtmBlocks.NUKE_GADGET, blockTexture(NtmBlocks.NUKE_GADGET));
        this.particleOnlyBlock(NtmBlocks.NUKE_LITTLE_BOY, blockTexture(NtmBlocks.NUKE_LITTLE_BOY));
        this.particleOnlyBlock(NtmBlocks.NUKE_FAT_MAN, blockTexture(NtmBlocks.NUKE_FAT_MAN));
        this.particleOnlyBlock(NtmBlocks.NUKE_IVY_MIKE, blockTexture(NtmBlocks.NUKE_IVY_MIKE));
        this.particleOnlyBlock(NtmBlocks.NUKE_TSAR_BOMBA, blockTexture(NtmBlocks.NUKE_TSAR_BOMBA));
        this.particleOnlyBlock(NtmBlocks.NUKE_PROTOTYPE, blockTexture(NtmBlocks.NUKE_PROTOTYPE));
        this.particleOnlyBlock(NtmBlocks.NUKE_FLEIJA, blockTexture(NtmBlocks.NUKE_FLEIJA));
        this.particleOnlyBlock(NtmBlocks.NUKE_SOLINIUM, blockTexture(NtmBlocks.NUKE_SOLINIUM));
        this.particleOnlyBlock(NtmBlocks.NUKE_N2, blockTexture(NtmBlocks.NUKE_N2));
        this.particleOnlyBlock(NtmBlocks.NUKE_FSTBMB, blockTexture(NtmBlocks.NUKE_FSTBMB));

        this.particleOnlyBlock(NtmBlocks.CRASHED_BOMB, modLoc("block/block_rust"), true);
        this.simpleCubeBottomTopBlock(NtmBlocks.DYNAMITE);
        this.simpleCubeBottomTopBlock(NtmBlocks.TNT);
        this.simpleCubeBottomTopBlock(NtmBlocks.SEMTEX);
        this.simpleCubeBottomTopBlock(NtmBlocks.C4);
        this.simpleCubeBottomTopBlock(NtmBlocks.FISSURE_BOMB);

        this.particleOnlyBlock(NtmBlocks.MINE_AP, blockTexture(NtmBlocks.MINE_AP));
        this.particleOnlyBlock(NtmBlocks.MINE_HE, blockTexture(NtmBlocks.MINE_HE));
        this.particleOnlyBlock(NtmBlocks.MINE_SHRAP, blockTexture(NtmBlocks.MINE_SHRAP));
        this.particleOnlyBlock(NtmBlocks.MINE_FAT, blockTexture(NtmBlocks.MINE_FAT));
        this.particleOnlyBlock(NtmBlocks.MINE_NAVAL, blockTexture(NtmBlocks.MINE_NAVAL));

        this.simpleCubeAllBlock(NtmBlocks.DET_CHARGE);
        this.registerDetCord();
        this.cubeTop(NtmBlocks.DET_NUKE);
        this.cubeTop(NtmBlocks.DET_MINER);
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_RED.get(), blockTexture(NtmBlocks.BARREL_RED));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_PINK.get(), blockTexture(NtmBlocks.BARREL_PINK));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_LOX.get(), blockTexture(NtmBlocks.BARREL_LOX));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_TAINT.get(), blockTexture(NtmBlocks.BARREL_TAINT));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_PLASTIC.get(), blockTexture(NtmBlocks.BARREL_PLASTIC));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_STEEL.get(), blockTexture(NtmBlocks.BARREL_STEEL));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_CORRODED.get(), blockTexture(NtmBlocks.BARREL_CORRODED));
        this.barrelLoaderBlockItem(NtmBlocks.BARREL_TCALLOY.get(), blockTexture(NtmBlocks.BARREL_TCALLOY));
        this.crateBlock(NtmBlocks.CRATE_IRON.get(), "crate_iron_side", "crate_iron_top");
        this.crateBlock(NtmBlocks.CRATE_TUNGSTEN.get(), "crate_tungsten_side", "crate_tungsten_top");
        this.crateBlock(NtmBlocks.CRATE_STEEL.get(), "crate_steel_side", "crate_steel_top");
        this.crateBlock(NtmBlocks.CRATE_DESH.get(), "crate_desh_side", "crate_desh_top");
        this.crateBlock(NtmBlocks.CRATE_TEMPLATE.get(), "crate_template", "crate_template");

        this.particleOnlyBlock(NtmBlocks.GEIGER, blockTexture(NtmBlocks.GEIGER));

        this.simpleCubeAllBlock(NtmBlocks.PRESS_PREHEATER);
        this.particleOnlyBlock(NtmBlocks.MACHINE_PRESS, blockTexture(NtmBlocks.MACHINE_PRESS));
        this.particleOnlyBlock(NtmBlocks.MACHINE_WELL, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PUMPJACK, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_FRACKING_TOWER, modLoc("block/block_steel"));

        this.registerCable();

        this.registerFluidDuct();

        this.particleOnlyBlock(NtmBlocks.MACHINE_BATTERY_SOCKET, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_BATTERY_REDD, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ASSEMBLY_MACHINE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PRECASS, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ORE_SLOPPER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_EXCAVATOR, modLoc("block/block_steel"));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_MISSILE_ASSEMBLY, this.models().cubeAll("machine_missile_assembly", modLoc("block/machine_missile_assembly")));

        /* Das Grundgesteinserz traegt vorerst die Grundgesteinstextur; die zehn Erzformen des
         * Originals gehoeren in eine eigene Runde zum Blockrenderer. */
        this.simpleBlockWithItem(NtmBlocks.ORE_BEDROCK, this.models().cubeAll("ore_bedrock", ResourceLocation.withDefaultNamespace("block/bedrock")));
        this.simpleCubeAllBlock(NtmBlocks.STONE_DEPTH);
        this.particleOnlyBlock(NtmBlocks.HEAT_BOILER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_INDUSTRIAL_BOILER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.HEATER_FIREBOX, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.HEATER_OVEN, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.HEATER_OILBURNER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.HEATER_ELECTRIC, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.HEATER_HEATEX, modLoc("block/block_steel"));
        this.registerMachineShredder();
        this.registerMachineElectricFurnace();
        this.registerMachineRtgFurnace();
        this.registerMachineDiFurnaceRtg();
        this.registerMachineDiFurnace();
        this.particleOnlyBlock(NtmBlocks.MACHINE_COMBUSTION_ENGINE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_TURBINEGAS, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_TURBOFAN, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CRYSTALLIZER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ASHPIT, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.CHIMNEY_BRICK, mcLoc("block/bricks"));
        this.particleOnlyBlock(NtmBlocks.CHIMNEY_INDUSTRIAL, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.RBMK_AUTOLOADER, modLoc("block/rbmk_autoloader"));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_WASTE_DRUM.get(), this.models().cubeBottomTop(this.name(NtmBlocks.MACHINE_WASTE_DRUM.get()), modLoc("block/waste_drum_side"), modLoc("block/waste_drum_top"), modLoc("block/waste_drum_top")));
        this.particleOnlyBlock(NtmBlocks.MACHINE_THRESHER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_AUTOSAW, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_SAWMILL, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_RTG, modLoc("block/rtg"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CONDENSER_POWERED, modLoc("block/condenser"));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_CONDENSER.get(), this.models().cubeAll(this.name(NtmBlocks.MACHINE_CONDENSER.get()), modLoc("block/condenser")));
        this.registerCableDiode();
        this.registerCableDetector();
        this.registerCableGauge();
        this.registerMachineBattery();
        this.registerCableSwitch();
        this.particleOnlyBlock(NtmBlocks.MACHINE_DIFURNACE_EXTENSION, modLoc("block/difurnace_extension"));
        this.simpleCubeAllBlock(NtmBlocks.RED_WIRE_COATED);
        this.simpleCubeAllBlock(NtmBlocks.STEEL_BEAM);
        this.simpleCubeAllBlock(NtmBlocks.STONE_GNEISS);
        this.registerSteelGrate();
        this.simpleCubeAllBlock(NtmBlocks.MACHINE_CONVERTER_HE_RF);
        this.simpleCubeAllBlock(NtmBlocks.MACHINE_CONVERTER_RF_HE);
        this.particleOnlyBlock(NtmBlocks.RED_CONNECTOR, modLoc("block/red_connector"));
        this.particleOnlyBlock(NtmBlocks.RED_CONNECTOR_SUPER, modLoc("block/red_connector"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON, modLoc("block/red_pylon"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON_STEEL, modLoc("block/red_pylon"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON_MEDIUM_WOOD, modLoc("block/red_pylon"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER, modLoc("block/red_pylon"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON_MEDIUM_STEEL, modLoc("block/red_pylon"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER, modLoc("block/red_pylon"));
        this.particleOnlyBlock(NtmBlocks.RED_PYLON_LARGE, modLoc("block/red_pylon_large"));
        this.particleOnlyBlock(NtmBlocks.SUBSTATION, modLoc("block/substation"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CHUNGUS, modLoc("block/block_steel"));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_INDUSTRIAL_TURBINE.get(), this.models().cubeAll("machine_industrial_turbine", modLoc("block/block_steel")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_STRAND_CASTER.get(), this.models().cubeAll("machine_strand_caster", modLoc("block/brick_fire")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_HEPHAESTUS.get(), this.models().cubeAll("machine_hephaestus", modLoc("block/block_steel_machine")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_TELEPORTER.get(), this.models().cubeBottomTop("machine_teleporter",
                modLoc("block/teleporter_side"), modLoc("block/teleporter_bottom"), modLoc("block/teleporter_top")));

        this.simpleBlockWithItem(NtmBlocks.MACHINE_CHEMICAL_FACTORY.get(), this.models().cubeAll("machine_chemical_factory", modLoc("block/block_steel")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_ASSEMBLY_FACTORY.get(), this.models().cubeAll("machine_assembly_factory", modLoc("block/block_steel")));

        this.simpleBlockWithItem(NtmBlocks.MACHINE_KEY_FORGE.get(), this.models().cubeBottomTop("machine_keyforge",
                modLoc("block/machine_keyforge_side"), modLoc("block/machine_keyforge_bottom"), modLoc("block/machine_keyforge_top")));

        /* Der Melder hat zwei Bilder, eines je Zustand. */
        ModelFile detectorOff = this.models().cubeAll("machine_detector_off", modLoc("block/machine_detector_off"));
        ModelFile detectorOn = this.models().cubeAll("machine_detector_on", modLoc("block/machine_detector"));

        this.getVariantBuilder(NtmBlocks.MACHINE_DETECTOR.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(MachineDetectorBlock.POWERED) ? detectorOn : detectorOff).build());
        this.simpleBlockItem(NtmBlocks.MACHINE_DETECTOR.get(), detectorOff);
        this.particleOnlyBlock(NtmBlocks.MACHINE_FLUID_TANK, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_SOLDERING_STATION, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_REFINERY, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.FURNACE_COMBINATION, modLoc("block/block_steel"));

        /*
         * Runde 105: Absauger, Geblaese und die beiden Pumpen. Das Original zeichnet sie mit
         * eigenen Renderern aus OBJ-Modellen; hier steht ein Stahlkasten. Die Form stimmt nicht,
         * die Abmessungen des Bauwerks aber sehr wohl -- der Absauger ist drei Bloecke lang, das
         * Geblaese zwei mal zwei, die Pumpe drei mal drei und vier hoch.
         */
        this.simpleBlockWithItem(NtmBlocks.MACHINE_DRAIN.get(), this.models().cubeAll("machine_drain", modLoc("block/block_steel_machine")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_INTAKE.get(), this.models().cubeAll("machine_intake", modLoc("block/block_steel_machine")));
        this.simpleBlockWithItem(NtmBlocks.PUMP_ELECTRIC.get(), this.models().cubeAll("pump_electric", modLoc("block/block_steel_machine")));
        this.simpleBlockWithItem(NtmBlocks.PUMP_STEAM.get(), this.models().cubeAll("pump_steam", modLoc("block/block_steel_machine")));

        /* Runde 107: das Original zeichnet die Presse aus zwei OBJ-Modellen; hier steht sie als
         * Saeule aus drei Kaesten mit dem Bild des Originals -- wie die Maschinen der Runde 105. */
        this.simpleBlockWithItem(NtmBlocks.MACHINE_EPRESS.get(), this.models().cubeAll("machine_epress", modLoc("block/machine_epress")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_FUNNEL.get(), this.models().cubeBottomTop("machine_funnel",
                modLoc("block/machine_funnel_side"), modLoc("block/machine_funnel_bottom"), modLoc("block/machine_funnel_top")));

        /* Runde 106: der Selbstbauer -- drei eigene Bilder, wie im Original. */
        this.simpleBlockWithItem(NtmBlocks.MACHINE_AUTOCRAFTER.get(), this.models().cubeBottomTop("machine_autocrafter",
                modLoc("block/machine_autocrafter_side"), modLoc("block/machine_autocrafter_bottom"), modLoc("block/machine_autocrafter_top")));
        this.particleOnlyBlock(NtmBlocks.MACHINE_BLAST_FURNACE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_WOOD_BURNER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CENTRIFUGE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ARC_FURNACE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CRUCIBLE, modLoc("block/brick_fire"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ROTARY_FURNACE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.RBMK_GAUGE, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_INDICATOR, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_NUMITRON, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_LEVER, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_KEYPAD, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_GRAPH, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_DISPLAY, modLoc("block/rbmk_blank_side"));
        this.particleOnlyBlock(NtmBlocks.RBMK_TERMINAL, modLoc("block/rbmk_blank_side"));

        /*
         * Die Blankotafel als einziger der neun Tafelbloecke ohne eigenen Renderer -- sie braucht
         * daher ein richtiges Modell. Vier Stueck, eins je Blickrichtung, mit den Kanten der
         * VoxelShapes aus RBMKMiniPanelBlock. Ausdruecklich NICHT ueber eine Modelldrehung: die
         * vier Kaesten sind hier abgezaehlt und stimmen so mit den Formen ueberein.
         */
        this.rbmkBlankPanel();

        this.simpleCubeAllBlock(NtmBlocks.BLOCK_CORIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_CORIUM_COBBLE);
        this.particleOnlyBlock(NtmBlocks.MACHINE_PUREX, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_RADIOLYSIS, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CHEMICAL_PLANT, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ARC_WELDER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_DIESEL, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_COMPRESSOR, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_GAS_CENT, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_CYCLOTRON, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PA_SOURCE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PA_BEAMLINE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PA_RFC, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PA_QUADRUPOLE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PA_DIPOLE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_PA_DETECTOR, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_EXPOSURE_CHAMBER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_RAD_GEN, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_MINING_LASER, modLoc("block/block_steel"));
        this.simpleBlock(NtmBlocks.BARRICADE.get(), this.models().cubeAll("barricade", modLoc("block/barricade")));
        this.particleOnlyBlock(NtmBlocks.MACHINE_COMPRESSOR_COMPACT, modLoc("block/block_steel"));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_TURBINE.get(), this.models().cubeBottomTop("machine_turbine", modLoc("block/machine_turbine_base"), modLoc("block/machine_turbine_top"), modLoc("block/machine_turbine_top")));
        this.particleOnlyBlock(NtmBlocks.MACHINE_SOLAR_BOILER, blockTexture(NtmBlocks.MACHINE_SOLAR_BOILER));
        this.particleOnlyBlock(NtmBlocks.SOLAR_MIRROR, blockTexture(NtmBlocks.SOLAR_MIRROR));
        this.particleOnlyBlock(NtmBlocks.FURNACE_IRON, modLoc("block/block_aluminium"));
        this.particleOnlyBlock(NtmBlocks.FURNACE_STEEL, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_MIXER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ROCK_MILL, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_STEAM_ENGINE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_STIRLING, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.REACTOR_ZIRNOX, modLoc("block/block_steel"));

        /* Watz: Kern und Deckel zeichnet der Blockentitaeten-Renderer, der Rest sind Wuerfel. */
        this.particleOnlyBlock(NtmBlocks.WATZ, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.WATZ_PUMP, modLoc("block/block_steel"));
        this.simpleCubeAllBlock(NtmBlocks.STRUCT_WATZ_CORE);
        this.pwrColumn(NtmBlocks.WATZ_ELEMENT, "watz_element");
        this.pwrColumn(NtmBlocks.WATZ_COOLER, "watz_cooler");
        this.watzEnd();

        /*
         * Chicago Pile. ABWEICHUNG: die Ein- und Auslaesse haben im Original eigene Texturen, die
         * aber nur als Connected-Texture-Blaetter vorliegen -- und die sind gestrichen
         * (ENTSCHEIDUNGEN.md). Alle Zustaende bekommen deshalb dieselbe Textur; woran man ist,
         * sagt die Einblendung beim Hinsehen.
         */
        this.pileBrick();
        this.pileBlock();
        this.particleOnlyBlock(NtmBlocks.PILE_LOADER, modLoc("block/pile_block"));
        this.particleOnlyBlock(NtmBlocks.PILE_VENT, modLoc("block/pile_block"));
        this.particleOnlyBlock(NtmBlocks.PILE_CONTROL, modLoc("block/pile_block"));

        /* Traegheitsfusion. Die Kammer zeichnet der Blockentitaeten-Renderer. */
        this.particleOnlyBlock(NtmBlocks.ICF, modLoc("block/icf_component"));
        this.simpleCubeAllBlock(NtmBlocks.ICF_CONTROLLER);
        this.simpleCubeAllBlock(NtmBlocks.STRUCT_ICF);
        this.icfComponent();
        this.icfLaserComponent();
        this.icfWrapper();
        this.particleOnlyBlock(NtmBlocks.FUSION_TORUS, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_BREEDER, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_COLLECTOR, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_COUPLER, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_BOILER, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_MHDT, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_KLYSTRON, modLoc("block/fusion_component"));
        this.particleOnlyBlock(NtmBlocks.FUSION_PLASMA_FORGE, modLoc("block/fusion_component"));
        this.fusionComponent();
        this.particleOnlyBlock(NtmBlocks.REACTOR_RESEARCH, modLoc("block/machine_reactor_small"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_REACTOR_BREEDING, modLoc("block/machine_reactor"));
        this.particleOnlyBlock(NtmBlocks.TURRET_SENTRY, modLoc("block/block_steel_machine"));
        this.particleOnlyBlock(NtmBlocks.TURRET_SENTRY_DAMAGED, modLoc("block/block_steel_machine"));
        this.particleOnlyBlock(NtmBlocks.TURRET_JEREMY, modLoc("block/block_steel_machine"));
        this.particleOnlyBlock(NtmBlocks.TURRET_HOWARD, modLoc("block/block_steel_machine"));
        this.particleOnlyBlock(NtmBlocks.TURRET_HOWARD_DAMAGED, modLoc("block/block_steel_machine"));
        this.particleOnlyBlock(NtmBlocks.TURRET_CHEKHOV, modLoc("block/block_steel_machine"));
        this.particleOnlyBlock(NtmBlocks.TURRET_FRIENDLY, modLoc("block/block_steel_machine"));
        this.reactorControl();
        this.simpleBlockWithItem(NtmBlocks.WEAPON_TABLE.get(), this.models().cubeBottomTop("weapon_table",
                modLoc("block/gun_table_side"), modLoc("block/gun_table_bottom"), modLoc("block/gun_table_top")));
        this.simpleBlockWithItem(NtmBlocks.ARMOR_TABLE.get(), this.models().cubeBottomTop("armor_table",
                modLoc("block/armor_table_side"), modLoc("block/armor_table_bottom"), modLoc("block/armor_table_top")));
        this.simpleBlockWithItem(NtmBlocks.MACHINE_ICF_PRESS.get(), this.models().cubeBottomTop("machine_icf_press",
                modLoc("block/machine_icf_press_side"), modLoc("block/machine_icf_press_top"), modLoc("block/machine_icf_press_top")));
        this.particleOnlyBlock(NtmBlocks.ZIRNOX_DESTROYED, modLoc("block/block_steel"));
        this.simpleBlockWithItem(
                NtmBlocks.EMP_BOMB,
                this.models().cubeColumn(
                        name(NtmBlocks.EMP_BOMB),
                        modLoc("block/bomb_emp_side"),
                        modLoc("block/bomb_emp_top")
                )
        );

        this.cubeTop(NtmBlocks.MACHINE_SATLINKER);
        this.particleOnlyBlock(NtmBlocks.MACHINE_SAT_LINK, modLoc("block/block_steel"));
        this.simpleCubeAllBlock(NtmBlocks.MACHINE_SAT_DOCK);
        this.particleOnlyBlock(NtmBlocks.MACHINE_TAPE_DRIVE, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_SUPER_COMPUTER, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_AMMO_PRESS, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.RADAR_SCREEN, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_ANNIHILATOR, modLoc("block/block_steel"));
        /* Oben und unten Stahl, an den Seiten das Gitter -- wie im Original. */
        this.simpleBlockWithItem(NtmBlocks.MACHINE_SIREN.get(), this.models().cubeBottomTop("machine_siren",
                modLoc("block/machine_siren"), modLoc("block/block_steel"), modLoc("block/block_steel")));

        this.simpleBlockWithItem(
                NtmBlocks.DECONTAMINATOR,
                this.models().cubeBottomTop(
                        name(NtmBlocks.DECONTAMINATOR),
                        blockTexture(NtmBlocks.DECONTAMINATOR, "_side"),
                        blockTexture(NtmBlocks.DECONTAMINATOR, "_side"),
                        blockTexture(NtmBlocks.DECONTAMINATOR, "_top")
                )
        );

        this.registerPwr();
        this.simpleBlock(NtmBlocks.BALEFIRE.get(), this.models().withExistingParent("balefire", mcLoc("block/cross")).renderType("cutout_mipped").texture("cross", modLoc("block/balefire")));
        this.simpleBlock(NtmBlocks.FIRE_DIGAMMA.get(), this.models().withExistingParent("fire_digamma", mcLoc("block/cross")).renderType("cutout_mipped").texture("cross", modLoc("block/fire_digamma")));
        // VOLCANO_CORE uses custom register!
        // VOLCANO_RAD_CORE uses custom register!

        this.particleOnlyBlock(NtmBlocks.LAUNCH_PAD, blockTexture(NtmBlocks.LAUNCH_PAD));
        this.particleOnlyBlock(NtmBlocks.LAUNCH_PAD_LARGE, modLoc("block/block_steel"));
        this.simpleBlock(NtmBlocks.SOYUZ_LAUNCHER.get(), this.models().getBuilder(name(NtmBlocks.SOYUZ_LAUNCHER) + "_particle").texture("particle", modLoc("item/soyuz_launcher")));
        this.itemModels().basicItem(NtmBlocks.SOYUZ_LAUNCHER.asItem());
        this.particleOnlyBlock(NtmBlocks.MACHINE_RADAR, modLoc("block/block_steel"));
        this.particleOnlyBlock(NtmBlocks.MACHINE_RADAR_LARGE, modLoc("block/block_steel"));

        this.itemModels().basicItem(NtmBlocks.GAS_RADON.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_RADON_DENSE.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_RADON_TOMB.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_MELTDOWN.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_MONOXIDE.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_ASBESTOS.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_COAL.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_FLAMMABLE.asItem());
        this.itemModels().basicItem(NtmBlocks.GAS_EXPLOSIVE.asItem());
        this.simpleCubeAllBlock(NtmBlocks.OIL_PIPE);

        this.simpleCubeAllBlock(NtmBlocks.TAINT);
        this.registerMaterialBlocks();
    }

    /**
     * Zehn Varianten, eine je Hoehe der Blockstate-Eigenschaft "layer". Ein eigenes Modell je
     * Variante ist noetig, weil ein Blockstate ein Modell nur drehen, aber nicht verschieben kann.
     */
    /**
     * Die RBMK-Saeulen. Der Deckel ist eine eigene Blockstate-Eigenschaft und tauscht die
     * Texturen der ganzen Saeule aus -- im Original macht der Renderer genau dasselbe.
     * Die Richtung und die Rolle im Multiblock aendern am Aussehen nichts.
     */
    private void registerRbmk() {

        this.rbmkColumn(NtmBlocks.RBMK_BLANK.get(), "rbmk_blank", true);
        this.rbmkColumn(NtmBlocks.RBMK_MODERATOR.get(), "rbmk_moderator", true);
        this.rbmkColumn(NtmBlocks.RBMK_ABSORBER.get(), "rbmk_absorber", true);
        this.rbmkColumn(NtmBlocks.RBMK_REFLECTOR.get(), "rbmk_reflector", true);
        this.rbmkColumn(NtmBlocks.RBMK_ROD.get(), "rbmk_element", true);
        this.rbmkColumn(NtmBlocks.RBMK_ROD_MOD.get(), "rbmk_element_mod", true);
        // Steuerstaebe bringen ihren Deckel fest mit und haben deshalb keine Deckeltexturen.
        this.rbmkColumn(NtmBlocks.RBMK_CONTROL.get(), "rbmk_control", false);
        this.rbmkColumn(NtmBlocks.RBMK_CONTROL_MOD.get(), "rbmk_control_mod", false);

        this.rbmkColumn(NtmBlocks.RBMK_BOILER.get(), "rbmk_boiler", true);
        this.rbmkColumn(NtmBlocks.RBMK_COOLER.get(), "rbmk_cooler", true);
        this.rbmkColumn(NtmBlocks.RBMK_STORAGE.get(), "rbmk_storage", true);

        this.rbmkColumn(NtmBlocks.RBMK_HEATER.get(), "rbmk_heater", true);
        this.rbmkColumn(NtmBlocks.RBMK_OUTGASSER.get(), "rbmk_outgasser", true);
        this.rbmkColumn(NtmBlocks.RBMK_ROD_REASIM.get(), "rbmk_element_reasim", true);
        this.rbmkColumn(NtmBlocks.RBMK_ROD_REASIM_MOD.get(), "rbmk_element_reasim_mod", true);
        this.rbmkColumn(NtmBlocks.RBMK_CONTROL_AUTO.get(), "rbmk_control_auto", false);
        this.rbmkColumn(NtmBlocks.RBMK_CONTROL_REASIM.get(), "rbmk_control_reasim", false);
        this.rbmkColumn(NtmBlocks.RBMK_CONTROL_REASIM_AUTO.get(), "rbmk_control_reasim_auto", false);

        this.simpleBlockWithItem(NtmBlocks.RBMK_STEAM_INLET.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_STEAM_INLET.get()), modLoc("block/rbmk_steam_inlet")));
        this.simpleBlockWithItem(NtmBlocks.RBMK_STEAM_OUTLET.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_STEAM_OUTLET.get()), modLoc("block/rbmk_steam_outlet")));
        this.simpleBlockWithItem(NtmBlocks.RBMK_LOADER.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_LOADER.get()), modLoc("block/rbmk_loader")));
        /*
         * Seit das Pult einen Renderer hat, darf es keinen eigenen Wuerfel mehr zeichnen -- sonst
         * steckt das Modell in einem Kasten. Die Textur bleibt als Partikelquelle stehen.
         */
        this.particleOnlyBlock(NtmBlocks.RBMK_CONSOLE, modLoc("block/rbmk_console"));
        this.simpleBlockWithItem(NtmBlocks.RBMK_CRANE_CONSOLE.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_CRANE_CONSOLE.get()), modLoc("block/rbmk_crane_console")));
        this.simpleBlockWithItem(NtmBlocks.RBMK_DEBRIS.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_DEBRIS.get()), modLoc("block/rbmk_debris")));
        this.simpleBlockWithItem(NtmBlocks.RBMK_DEBRIS_BURNING.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_DEBRIS_BURNING.get()), modLoc("block/rbmk_debris_burning")));
        /* Die Abklingstufe steht im Blockzustand, das Modell ist fuer alle sechzehn dasselbe. */
        this.simpleBlockWithItem(NtmBlocks.RBMK_DEBRIS_RADIATING.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_DEBRIS_RADIATING.get()), modLoc("block/rbmk_debris_radiating")));
        this.simpleBlockWithItem(NtmBlocks.RBMK_DEBRIS_DIGAMMA.get(), this.models().cubeAll(this.name(NtmBlocks.RBMK_DEBRIS_DIGAMMA.get()), modLoc("block/rbmk_debris_digamma")));
    }

    private void rbmkColumn(Block block, String texture, boolean hasLids) {

        String name = this.name(block);

        ModelFile plain = this.models().cubeBottomTop(name, modLoc("block/" + texture + "_side"), modLoc("block/" + texture + "_top"), modLoc("block/" + texture + "_top"));
        ModelFile cover = hasLids ? this.models().cubeBottomTop(name + "_cover", modLoc("block/" + texture + "_cover_side"), modLoc("block/" + texture + "_cover_top"), modLoc("block/" + texture + "_cover_top")) : plain;
        ModelFile glass = hasLids ? this.models().cubeBottomTop(name + "_glass", modLoc("block/" + texture + "_glass_side"), modLoc("block/" + texture + "_glass_top"), modLoc("block/" + texture + "_glass_top")) : plain;

        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            ModelFile model = switch(state.getValue(RBMKBaseBlock.LID)) {
                case CONCRETE -> cover;
                case GLASS -> glass;
                default -> plain;
            };
            return ConfiguredModel.builder().modelFile(model).build();
        }, DummyableBlock.FACING, DummyableBlock.TYPE);

        this.simpleBlockItem(block, plain);
    }

    /** Die vier Modelle der Blankotafel, eins je Blickrichtung. */
    private void rbmkBlankPanel() {

        Block block = NtmBlocks.RBMK_DISPLAY_BLANK.get();
        ResourceLocation texture = modLoc("block/rbmk_display");
        String name = this.name(block);

        ModelFile north = this.panelBox(name + "_north", texture, 0, 4, 16, 16);
        ModelFile south = this.panelBox(name + "_south", texture, 0, 0, 16, 12);
        ModelFile west = this.panelBox(name + "_west", texture, 4, 0, 16, 16);
        ModelFile east = this.panelBox(name + "_east", texture, 0, 0, 12, 16);

        this.getVariantBuilder(block).forAllStates(state -> {
            ModelFile model = switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case SOUTH -> south;
                case WEST -> west;
                case EAST -> east;
                default -> north;
            };
            return ConfiguredModel.builder().modelFile(model).build();
        });

        this.simpleBlockItem(block, north);
    }

    /** Ein Kasten voller Hoehe, von (x1, z1) bis (x2, z2), rundum mit derselben Textur. */
    private ModelFile panelBox(String name, ResourceLocation texture, int x1, int z1, int x2, int z2) {

        var builder = this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("all", texture)
                .texture("particle", texture)
                .element()
                .from(x1, 0, z1)
                .to(x2, 16, z2);

        for(Direction dir : Direction.values()) builder = builder.face(dir).texture("#all").end();

        return builder.end();
    }

    private void registerSteelGrate() {
        Block block = NtmBlocks.STEEL_GRATE.get();
        String blockName = this.name(block);
        ModelFile first = null;

        VariantBlockStateBuilder builder = this.getVariantBuilder(block);

        for(int layer = 0; layer <= 9; layer++) {
            float from = GrateBlock.getY(layer) * 16F;

            ModelFile model = this.models()
                    .withExistingParent(blockName + "_" + layer, this.mcLoc("block/block"))
                    .texture("top", this.modLoc("block/grate_top"))
                    .texture("side", this.modLoc("block/grate_side"))
                    .texture("particle", this.modLoc("block/grate_top"))
                    .renderType("cutout")
                    .element()
                    .from(0, from, 0)
                    .to(16, from + 2F, 16)
                    .face(Direction.UP).texture("#top").end()
                    .face(Direction.DOWN).texture("#top").end()
                    .face(Direction.NORTH).texture("#side").end()
                    .face(Direction.SOUTH).texture("#side").end()
                    .face(Direction.WEST).texture("#side").end()
                    .face(Direction.EAST).texture("#side").end()
                    .end();

            if(layer == 0) first = model;

            builder.partialState().with(GrateBlock.LAYER, layer).modelForState().modelFile(model).addModel();
        }

        this.simpleBlockItem(block, first);
    }

    private void registerOreBasalt() {
        Block block = NtmBlocks.ORE_BASALT.get();

        Enum<?>[] enums = BasaltOreType.values();
        this.getVariantBuilder(block).forAllStates(state -> {

            int meta = state.getValue(OreBasaltBlock.SUBTYPE);
            Enum<?> num = enums[meta];

            String path = "block/" + this.name(block) + "." + num.name().toLowerCase(Locale.US);
            ModelFile model = this.models().cubeTop(path, this.modLoc(path), this.modLoc(path + "_top"));

            return ConfiguredModel.builder().modelFile(model).build();
        });

        ItemModelBuilder builder = this.itemModels().getBuilder(this.name(block));
        for(Enum<?> num : enums) {
            String parent = "block/" + this.name(block) + "." + num.name().toLowerCase(Locale.US);
            builder.override()
                    .predicate(NuclearTechMod.withDefaultNamespace("item_meta"), num.ordinal())
                    .model(this.itemModels().getBuilder(this.name(block) + "." + num.name().toLowerCase(Locale.US))
                            .parent(new ModelFile.UncheckedModelFile(this.modLoc(parent)))
                    ).end();
        }
    }

    private void registerCable() {
        Block block = NtmBlocks.RED_CABLE.get();

        this.simpleBlock(block, this.models().getBuilder(this.key(block).getPath()).customLoader(CableBlockLoaderBuilder::new).texture("texture", modLoc("block/cable_neo")).end());
        this.entityBlockItem(block, false);
    }

    private void registerDetCord() {
        Block block = NtmBlocks.DET_CORD.get();

        this.simpleBlock(block, this.models().getBuilder(this.key(block).getPath()).customLoader(DetCordBlockLoaderBuilder::new).texture("texture", modLoc("block/det_cord")).end());
        this.entityBlockItem(block, false);
    }

    private void registerFluidDuct() {
        Block block = NtmBlocks.FLUID_DUCT_NEO.get();

        this.getVariantBuilder(block).forAllStatesExcept(state -> {

            int meta = state.getValue(NtmBlockStateProperties.META);
            
            ModelFile model;

            switch(meta) {
                case 2 -> model = this.models().getBuilder("hbmsntm:block/fluid_duct_silver").customLoader(DuctBlockLoaderBuilder::new).texture("texture", modLoc("block/pipe_silver")).texture("overlay", modLoc("block/pipe_silver_overlay")).end();
                case 1 -> model = this.models().getBuilder("hbmsntm:block/fluid_duct_colored").customLoader(DuctBlockLoaderBuilder::new).texture("texture", modLoc("block/pipe_colored")).texture("overlay", modLoc("block/pipe_colored_overlay")).end();
                default -> model = this.models().getBuilder("hbmsntm:block/fluid_duct_neo").customLoader(DuctBlockLoaderBuilder::new).texture("texture", modLoc("block/pipe_neo")).texture("overlay", modLoc("block/pipe_neo_overlay")).end();
            }

            return ConfiguredModel.builder().modelFile(model).build();
        }, FluidDuctConnectingBlock.NORTH, FluidDuctConnectingBlock.SOUTH, FluidDuctConnectingBlock.EAST, FluidDuctConnectingBlock.WEST, FluidDuctConnectingBlock.UP, FluidDuctConnectingBlock.DOWN);

        this.entityBlockItem(block, false);
    }

    private void registerMaterialBlocks() {
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_ACTINIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_STEEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_ALUMINIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_ASBESTOS);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_GRAPHITE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_BORON);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_AUSTRALIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_BERYLLIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_BISMUTH);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_CADMIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_CDALLOY);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_COLTAN);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_COMBINE_STEEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_COPPER);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_DESH);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_DINEUTRONIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_DURA_STEEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_EUPHEMIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_FOAM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_LANTHANIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_LEAD);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_LITHIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_MAGNETIZED_TUNGSTEN);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_METEOR);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_METEOR_MOLTEN);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_METEOR_COBBLE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_METEOR_BROKEN);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_METEOR_TREASURE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_MOX_FUEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_NEPTUNIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_NIOBIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_NITER);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_PLUTONIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_PLUTONIUM_FUEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_POLONIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_PU238);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_PU239);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_PU240);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_PU_MIX);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_RA226);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_RED_COPPER);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SATURNITE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SCHRABIDATE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SCHRABIDIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SCHRABIDIUM_FUEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SCHRARANIUM);
        this.simpleBlockWithItem(NtmBlocks.BLOCK_SMORE.get(), this.models().cubeBottomTop("block_smore", modLoc("block/block_smore_side"), modLoc("block/block_smore_side"), modLoc("block/block_smore_top")));
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SOLINIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SULFUR);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_TANTALIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_TCALLOY);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_THORIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_THORIUM_FUEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_TITANIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_TUNGSTEN);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_U233);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_U235);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_U238);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_URANIUM);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_URANIUM_FUEL);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_WASTE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_WASTE_PAINTED);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_WASTE_VITRIFIED);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_YELLOWCAKE);
        this.simpleCubeAllBlock(NtmBlocks.BLOCK_SLAG);
        this.simpleBlockWithItem(NtmBlocks.BLOCK_FIBERGLASS.get(), this.models().cubeBottomTop("block_fiberglass", modLoc("block/block_fiberglass_side"), modLoc("block/block_fiberglass_side"), modLoc("block/block_fiberglass_top")));
        this.simpleBlockWithItem(NtmBlocks.BLOCK_INSULATOR.get(), this.models().cubeBottomTop("block_insulator", modLoc("block/block_insulator_side"), modLoc("block/block_insulator_side"), modLoc("block/block_insulator_top")));
    }

    private void registerBarbedWire() {
        Block block = NtmBlocks.BARBED_WIRE.get();

        this.getVariantBuilder(block).forAllStates(state -> {

            int subType = state.getValue(BarbedWireBlock.SUBTYPE);

            ModelFile model;

            switch(subType) {
                case 5 -> model = this.models().getBuilder(this.name(block) + "_ultradeath").customLoader(BarbedWireBlockLoaderBuilder::new).texture("texture", modLoc("block/barbed_wire_ultradeath")).end();
                case 4 -> model = this.models().getBuilder(this.name(block) + "_wither").customLoader(BarbedWireBlockLoaderBuilder::new).texture("texture", modLoc("block/barbed_wire_wither")).end();
                case 3 -> model = this.models().getBuilder(this.name(block) + "_acid").customLoader(BarbedWireBlockLoaderBuilder::new).texture("texture", modLoc("block/barbed_wire_acid")).end();
                case 2 -> model = this.models().getBuilder(this.name(block) + "_poison").customLoader(BarbedWireBlockLoaderBuilder::new).texture("texture", modLoc("block/barbed_wire_poison")).end();
                case 1 -> model = this.models().getBuilder(this.name(block) + "_fire").customLoader(BarbedWireBlockLoaderBuilder::new).texture("texture", modLoc("block/barbed_wire_fire")).end();
                default -> model = this.models().getBuilder(this.name(block)).customLoader(BarbedWireBlockLoaderBuilder::new).texture("texture", modLoc("block/barbed_wire")).end();
            }

            return ConfiguredModel.builder().modelFile(model).build();
        });

        this.entityBlockItem(block, false);
    }

    private void registerSpikes() {
        Block block = NtmBlocks.SPIKES.get();

        this.simpleBlock(block, this.models().getBuilder(this.key(block).getPath()).customLoader(SpikesLoaderBuilder::new).texture("texture", this.blockTexture(block)).end());
        this.entityBlockItem(block, false);
    }

    private void barrelLoaderBlockItem(Block block, ResourceLocation texture) {
        this.simpleBlock(block, this.models().getBuilder(this.key(block).getPath()).customLoader(BarrelBlockModelBuilder::new).texture("texture", texture).end());
        this.entityBlockItem(block, false);
    }

    private void registerCableDiode() {
        Block block = NtmBlocks.CABLE_DIODE.get();

        // Original (RenderDiode): Platte 14/16..16/16 auf der Ausgangsseite (bei FACING=NORTH
        // also die Suedflaeche), Kern 2/16..14/16 mit der Textur von hadron_coil_alloy.
        ModelFile model = this.models().withExistingParent(this.name(block), mcLoc("block/block"))
                .texture("particle", modLoc("block/cable_diode"))
                .texture("plate", modLoc("block/cable_diode"))
                .texture("core", modLoc("block/hadron_coil_alloy"))
                .element()
                .from(0, 0, 14).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#plate"))
                .end()
                .element()
                .from(2, 2, 2).to(14, 14, 14)
                .allFaces((dir, face) -> face.texture("#core"))
                .end();

        this.getVariantBuilder(block).forAllStates(state -> {
            net.minecraft.core.Direction facing = state.getValue(com.hbm.blocks.network.CableDiodeBlock.FACING);
            int x = switch(facing) { case UP -> 270; case DOWN -> 90; default -> 0; };
            int y = switch(facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });

        this.simpleBlockItem(block, model);
    }

    private void registerCableDetector() {
        Block block = NtmBlocks.CABLE_DETECTOR.get();

        ModelFile modelOff = this.models().cubeAll(this.name(block) + "_off", modLoc("block/cable_detector_off"));
        ModelFile modelOn = this.models().cubeAll(this.name(block) + "_on", modLoc("block/cable_detector_on"));

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(com.hbm.blocks.network.CableDetectorBlock.LIT) ? modelOn : modelOff).build());

        this.simpleBlockItem(block, modelOff);
    }

    private void registerCableGauge() {
        Block block = NtmBlocks.RED_CABLE_GAUGE.get();

        ResourceLocation side = modLoc("block/deco_red_copper");
        ResourceLocation gauge = modLoc("block/cable_gauge");

        // Die Anzeige liegt im Grundmodell auf Nord und wird ueber x/y in die Setzrichtung gedreht.
        ModelFile model = this.models().cube(this.name(block), side, side, gauge, side, side, side).texture("particle", side);

        this.getVariantBuilder(block).forAllStates(state -> {
            net.minecraft.core.Direction facing = state.getValue(com.hbm.blocks.network.CableGaugeBlock.FACING);
            int x = switch(facing) { case UP -> 270; case DOWN -> 90; default -> 0; };
            int y = switch(facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });

        this.simpleBlockItem(block, model);
    }

    private void registerMachineBattery() {
        this.batteryBlock(NtmBlocks.MACHINE_BATTERY_POTATO.get(), "battery_potato_top", "battery_potato_front", "battery_potato_side");
        this.batteryBlock(NtmBlocks.MACHINE_BATTERY.get(), "battery_top", "battery_front_alt", "battery_side_alt");
        this.batteryBlock(NtmBlocks.MACHINE_LITHIUM_BATTERY.get(), "battery_lithium_top", "battery_lithium_front", "battery_lithium_side");
        this.batteryBlock(NtmBlocks.MACHINE_SCHRABIDIUM_BATTERY.get(), "battery_schrabidium_top", "battery_schrabidium_front", "battery_schrabidium_side");
        this.batteryBlock(NtmBlocks.MACHINE_DINEUTRONIUM_BATTERY.get(), "battery_dineutronium_top", "battery_dineutronium_front", "battery_dineutronium_side");
    }

    /** Ober- und Unterseite teilen sich eine Textur, die Vorderseite liegt auf Nord und wird ueber rotationY gedreht. */
    private void batteryBlock(Block block, String top, String front, String side) {
        ResourceLocation topTex = modLoc("block/" + top);
        ResourceLocation frontTex = modLoc("block/" + front);
        ResourceLocation sideTex = modLoc("block/" + side);

        ModelFile model = this.models().cube(this.name(block), topTex, topTex, frontTex, sideTex, sideTex, sideTex).texture("particle", sideTex);

        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachineBatteryBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationY(y).build();
        });

        this.simpleBlockItem(block, model);
    }

    private void registerMachineDiFurnace() {
        Block block = NtmBlocks.MACHINE_DIFURNACE.get();

        ResourceLocation brick = modLoc("block/brick_fire");
        ResourceLocation side = modLoc("block/difurnace_side_alt");
        ResourceLocation sideTall = modLoc("block/difurnace_side_tall");

        // cube(name, down, up, north, south, east, west) -- Vorderseite liegt auf Nord
        // und wird ueber rotationY in die jeweilige Blickrichtung gedreht.
        ModelFile off = this.models().cube(this.name(block),
                brick, modLoc("block/difurnace_top_off_alt"), modLoc("block/difurnace_front_off_alt"),
                side, side, side).texture("particle", side);

        ModelFile on = this.models().cube(this.name(block) + "_on",
                brick, modLoc("block/difurnace_top_on_alt"), modLoc("block/difurnace_front_on_alt"),
                side, side, side).texture("particle", side);

        // Mit Aufsatz: schmale Seitentexturen, die Oberseite wird zur Feuerfestziegeldecke.
        ModelFile offCovered = this.models().cube(this.name(block) + "_covered",
                brick, brick, modLoc("block/difurnace_front_off_tall"),
                sideTall, sideTall, sideTall).texture("particle", sideTall);

        ModelFile onCovered = this.models().cube(this.name(block) + "_covered_on",
                brick, brick, modLoc("block/difurnace_front_on_tall"),
                sideTall, sideTall, sideTall).texture("particle", sideTall);

        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachineDiFurnaceBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            boolean lit = state.getValue(com.hbm.blocks.machine.MachineDiFurnaceBlock.LIT);
            boolean covered = state.getValue(com.hbm.blocks.machine.MachineDiFurnaceBlock.COVERED);
            ModelFile model = covered ? (lit ? onCovered : offCovered) : (lit ? on : off);
            return ConfiguredModel.builder().modelFile(model).rotationY(y).build();
        });

        this.simpleBlockItem(block, off);
    }

    private void registerCableSwitch() {
        Block block = NtmBlocks.CABLE_SWITCH.get();

        ModelFile modelOff = this.models().cubeAll(this.name(block) + "_off", modLoc("block/cable_switch_off"));
        ModelFile modelOn = this.models().cubeAll(this.name(block) + "_on", modLoc("block/cable_switch_on"));

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(com.hbm.blocks.network.CableSwitchBlock.LIT) ? modelOn : modelOff).build());

        this.simpleBlockItem(block, modelOff);
    }

    private void registerMachineDiFurnaceRtg() {
        Block block = NtmBlocks.MACHINE_DIFURNACE_RTG.get();

        ResourceLocation side = modLoc("block/machine_rtg_furnace_side_alt");

        ModelFile modelOff = this.models().cube(
                this.name(block),
                side, modLoc("block/rtg_difurnace_top_off"),
                modLoc("block/rtg_difurnace_front_off"),
                side, side, side
        ).texture("particle", side);

        ModelFile modelOn = this.models().cube(
                this.name(block) + "_on",
                side, modLoc("block/rtg_difurnace_top_on"),
                modLoc("block/rtg_difurnace_front_on"),
                side, side, side
        ).texture("particle", side);

        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachineDiFurnaceRtgBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            boolean lit = state.getValue(com.hbm.blocks.machine.MachineDiFurnaceRtgBlock.LIT);
            return ConfiguredModel.builder().modelFile(lit ? modelOn : modelOff).rotationY(y).build();
        });

        this.simpleBlockItem(block, modelOff);
    }

    /** Die elf Bauteile des Druckwasserreaktors. */
    private void registerPwr() {

        /* Die Steuerung dreht sich zum Spieler; das Modell zeigt nach Norden und wird gedreht.
         * Der Zustand wird GENAU EINMAL erklaert: simpleBlockWithItem hat hier frueher schon
         * eine Variante gesetzt, und der Erzeuger haette den Block danach zweimal beschrieben. */
        ModelFile controller = this.models().cube(
                this.name(NtmBlocks.PWR_CONTROLLER.get()),
                modLoc("block/pwr_casing_blank"), modLoc("block/pwr_casing_blank"),
                modLoc("block/pwr_controller"),
                modLoc("block/pwr_casing_blank"), modLoc("block/pwr_casing_blank"), modLoc("block/pwr_casing_blank")
        ).texture("particle", modLoc("block/pwr_casing_blank"));

        this.getVariantBuilder(NtmBlocks.PWR_CONTROLLER.get()).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachinePWRControllerBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(controller).rotationY(y).build();
        });

        this.simpleBlockItem(NtmBlocks.PWR_CONTROLLER.get(), controller);

        this.simpleCubeAllBlock(NtmBlocks.PWR_CASING);
        this.simpleCubeAllBlock(NtmBlocks.PWR_REFLECTOR);
        this.simpleCubeAllBlock(NtmBlocks.PWR_PORT);
        this.simpleCubeAllBlock(NtmBlocks.PWR_HEATEX);
        this.simpleCubeAllBlock(NtmBlocks.PWR_HEATSINK);
        this.simpleCubeAllBlock(NtmBlocks.PWR_NEUTRON_SOURCE);

        /* Die drei Saeulenbauteile: Deckel und Boden anders als die Seiten. */
        this.pwrColumn(NtmBlocks.PWR_FUEL_CHANNEL, "pwr_fuel");
        this.pwrColumn(NtmBlocks.PWR_CONTROL, "pwr_control");
        this.pwrColumn(NtmBlocks.PWR_CHANNEL, "pwr_channel");

        /* Der Stellvertreter sieht ueberall gleich aus, ob Anschlussstelle oder nicht. */
        ModelFile pwrBlock = this.models().cubeAll(this.name(NtmBlocks.PWR_BLOCK.get()), modLoc("block/pwr_block"));
        this.getVariantBuilder(NtmBlocks.PWR_BLOCK.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(pwrBlock).build());
    }

    /** Die Aussenwand: roh und verschraubt, im Original zwei Metadatenwerte. */
    /**
     * Der Graphitziegel. Er traegt drei Texturen: oben und unten die eine, an zwei Seiten die
     * zweite, an den beiden uebrigen die dritte -- genau wie im Original.
     */
    /**
     * Die vier Baustufen des Fusionsbauteils. Anders als beim ICF haengen sie nicht aneinander:
     * Spulen und verschweisste Spulen bilden ein Paar, Decke und Verrohrung sind je eigene Teile.
     */
    /** Das Reaktorpult: eigene Vorder-, Rueck- und Deckflaeche, gedreht nach der Blickrichtung. */
    private void reactorControl() {

        Block block = NtmBlocks.REACTOR_CONTROL.get();

        ModelFile model = this.models().orientable(this.name(block),
                modLoc("block/machine_controller_side"),
                modLoc("block/machine_controller"),
                modLoc("block/machine_controller_top"));

        this.horizontalBlock(block, model);
        this.simpleBlockItem(block, model);
    }

    private void fusionComponent() {

        Block block = NtmBlocks.FUSION_COMPONENT.get();
        String[] textures = {"fusion_component", "fusion_component.bscco_welded",
                "fusion_component.blanket", "fusion_component.motor"};

        ModelFile[] models = new ModelFile[8];
        for(int i = 0; i < 8; i++) {
            String texture = textures[Math.min(i, textures.length - 1)];
            models[i] = this.models().cubeAll(this.name(block) + (i == 0 ? "" : "_" + i), modLoc("block/" + texture));
        }

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(models[state.getValue(ToolConversionBlock.STAGE)]).build());

        this.simpleBlockItem(block, models[0]);
    }

    /** Die fuenf Baustufen des Kammerbauteils. Stufe 0 traegt die Grundtextur. */
    private void icfComponent() {

        Block block = NtmBlocks.ICF_COMPONENT.get();
        String[] textures = {"icf_component", "icf_component.vessel", "icf_component.vessel_welded",
                "icf_component.structure", "icf_component.structure_bolted"};

        ModelFile[] models = new ModelFile[8];
        for(int i = 0; i < 8; i++) {
            String texture = textures[Math.min(i, textures.length - 1)];
            models[i] = this.models().cubeAll(this.name(block) + (i == 0 ? "" : "_" + i), modLoc("block/" + texture));
        }

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(models[state.getValue(ToolConversionBlock.STAGE)]).build());

        this.simpleBlockItem(block, models[0]);
    }

    /** Die sechs Laserbauteile. Kondensator und Turbolader teilen sich die Deckflaeche. */
    private void icfLaserComponent() {

        Block block = NtmBlocks.ICF_LASER_COMPONENT.get();
        String[] sides = {"icf_casing", "icf_port", "icf_cell", "icf_emitter", "icf_capacitor_side", "icf_turbocharger"};
        String[] tops = {"icf_casing", "icf_port", "icf_cell", "icf_emitter", "icf_capacitor_top", "icf_capacitor_top"};

        ModelFile[] models = new ModelFile[sides.length];
        for(int i = 0; i < sides.length; i++) {
            models[i] = this.models().cubeColumn(this.name(block) + "_" + i, modLoc("block/" + sides[i]), modLoc("block/" + tops[i]));
        }

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(models[state.getValue(ICFLaserComponentBlock.SUBTYPE)]).build());

        this.simpleBlockItem(block, models[0]);
    }

    /** Der Stellvertreter: Huelle und Anschlussstelle sehen verschieden aus. */
    private void icfWrapper() {

        Block block = NtmBlocks.ICF_BLOCK.get();
        ModelFile plain = this.models().cubeAll(this.name(block), modLoc("block/icf_block"));
        ModelFile port = this.models().cubeAll(this.name(block) + "_port", modLoc("block/icf_block_port"));

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(ICFWrapperBlock.PORT) ? port : plain).build());
    }

    private void pileBrick() {
        Block block = NtmBlocks.PILE_BRICK.get();
        ModelFile model = this.models().cube(this.name(block),
                modLoc("block/pile_brick_top"), modLoc("block/pile_brick_top"),
                modLoc("block/pile_brick"), modLoc("block/pile_brick"),
                modLoc("block/pile_brick_side"), modLoc("block/pile_brick_side"))
                .texture("particle", modLoc("block/pile_brick"));
        this.simpleBlockWithItem(block, model);
    }

    private void pileBlock() {
        Block block = NtmBlocks.PILE_BLOCK.get();
        ModelFile model = this.models().cubeBottomTop(this.name(block), modLoc("block/pile_block"), modLoc("block/pile_block_top"), modLoc("block/pile_block_top"));
        this.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
    }

    private void watzEnd() {
        Block block = NtmBlocks.WATZ_END.get();
        ModelFile plain = this.models().cubeAll(this.name(block), modLoc("block/watz_casing"));
        ModelFile bolted = this.models().cubeAll(this.name(block) + "_bolted", modLoc("block/watz_casing_bolted"));

        this.getVariantBuilder(block).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(ToolConversionBlock.STAGE) == 0 ? plain : bolted).build());

        this.simpleBlockItem(block, plain);
    }

    private void pwrColumn(DeferredBlock<? extends Block> block, String texture) {
        ModelFile model = this.models().cubeColumn(this.name(block.get()), modLoc("block/" + texture + "_side"), modLoc("block/" + texture + "_top"));
        this.simpleBlockWithItem(block.get(), model);
    }

    private void registerMachineRtgFurnace() {
        Block block = NtmBlocks.MACHINE_RTG_FURNACE.get();

        ResourceLocation cap = modLoc("block/machine_rtg_furnace_base_alt");
        ResourceLocation side = modLoc("block/machine_rtg_furnace_side_alt");

        ModelFile modelOff = this.models().cube(
                this.name(block),
                cap, cap,
                modLoc("block/machine_rtg_furnace_off_alt"),
                side, side, side
        ).texture("particle", side);

        ModelFile modelOn = this.models().cube(
                this.name(block) + "_on",
                cap, cap,
                modLoc("block/machine_rtg_furnace_on_alt"),
                side, side, side
        ).texture("particle", side);

        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachineRtgFurnaceBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            boolean lit = state.getValue(com.hbm.blocks.machine.MachineRtgFurnaceBlock.LIT);
            return ConfiguredModel.builder().modelFile(lit ? modelOn : modelOff).rotationY(y).build();
        });

        this.simpleBlockItem(block, modelOff);
    }

    private void registerMachineElectricFurnace() {
        Block block = NtmBlocks.MACHINE_ELECTRIC_FURNACE.get();

        ResourceLocation bottom = modLoc("block/machine_electric_furnace_bottom");
        ResourceLocation top = modLoc("block/machine_electric_furnace_top");
        ResourceLocation side = modLoc("block/machine_electric_furnace_side");

        // cube(name, down, up, north, south, east, west) -- Vorderseite liegt auf Nord
        // und wird ueber rotationY in die jeweilige Blickrichtung gedreht.
        ModelFile modelOff = this.models().cube(
                this.name(block),
                bottom, top,
                modLoc("block/machine_electric_furnace_front_off"),
                side, side, side
        ).texture("particle", side);

        ModelFile modelOn = this.models().cube(
                this.name(block) + "_on",
                bottom, top,
                modLoc("block/machine_electric_furnace_front_on"),
                side, side, side
        ).texture("particle", side);

        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachineElectricFurnaceBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            boolean lit = state.getValue(com.hbm.blocks.machine.MachineElectricFurnaceBlock.LIT);
            return ConfiguredModel.builder().modelFile(lit ? modelOn : modelOff).rotationY(y).build();
        });

        this.simpleBlockItem(block, modelOff);
    }

    private void registerMachineShredder() {
        Block block = NtmBlocks.MACHINE_SHREDDER.get();

        ModelFile model = this.models().cube(
                this.name(block),
                modLoc("block/machine_shredder_bottom_alt"),
                modLoc("block/machine_shredder_top_alt"),
                modLoc("block/machine_shredder_front_alt"),
                modLoc("block/machine_shredder_front_alt"),
                modLoc("block/machine_shredder_side_alt"),
                modLoc("block/machine_shredder_side_alt")
        );

        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.MachineShredderBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationY(y).build();
        });

        this.simpleBlockItem(block, model);
    }

    private void fluidBlock(DeferredBlock<? extends Block> block, String stillTexture) {
        this.simpleBlock(block.get(), this.models()
                .getBuilder(name(block))
                .texture("particle", modLoc("block/" + stillTexture)));
    }

    private void layeringBlock(Block block, ResourceLocation texture) {
        String blockName = this.key(block).getPath();
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        for(int i = 1; i <= 8; i++) {
            float height = i * 2f / 16f;

            ModelFile model = models()
                    .withExistingParent(blockName + "_" + i, mcLoc("block/block"))
                    .texture("all", texture)
                    .texture("particle", texture)
                    .renderType("cutout_mipped")
                    .element()
                    .from(0, 0, 0)
                    .to(16, height * 16, 16)
                    .face(Direction.UP).texture("#all").end()
                    .face(Direction.DOWN).texture("#all").end()
                    .face(Direction.NORTH).texture("#all").end()
                    .face(Direction.SOUTH).texture("#all").end()
                    .face(Direction.WEST).texture("#all").end()
                    .face(Direction.EAST).texture("#all").end()
                    .end();

            builder.part()
                    .modelFile(model)
                    .addModel()
                    .condition(LayeringBlock.LAYERS, i)
                    .end();
        }
    }

    private void sellafieldSlaked(Block block, String modelBaseName) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int variant = state.getValue(SellafieldSlakedBlock.VARIANT);
            String modelName = modelBaseName + (variant == 0 ? "" : "_" + variant);
            String texName = "sellafield_slaked" + (variant == 0 ? "" : "_" + variant);

            ModelFile tintedModel = models().withExistingParent(modelName, mcLoc("block/cube"))
                    .texture("particle", modLoc("block/" + texName))
                    .texture("down", modLoc("block/" + texName))
                    .texture("up", modLoc("block/" + texName))
                    .texture("north", modLoc("block/" + texName))
                    .texture("south", modLoc("block/" + texName))
                    .texture("west", modLoc("block/" + texName))
                    .texture("east", modLoc("block/" + texName))
                    .element()
                    .from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#" + dir.getName()).tintindex(0))
                    .end();

            return ConfiguredModel.builder().modelFile(tintedModel).build();
            }, SellafieldSlakedBlock.COLOR_LEVEL);

        itemModels().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath(), modLoc("block/sellafield_slaked"));
    }

    private void sellafieldOre(Block block, String baseName, String overlayTexture) {

        this.getVariantBuilder(block).forAllStatesExcept(state -> {
            int variant = state.getValue(SellafieldSlakedBlock.VARIANT);
            String modelName = baseName + (variant == 0 ? "" : "_" + variant);
            String baseTex = "sellafield_slaked" + (variant == 0 ? "" : "_" + variant);

            ModelFile oreModel = models().withExistingParent(modelName, mcLoc("block/cube"))
                    .renderType("cutout")
                    .texture("base", modLoc("block/" + baseTex))
                    .texture("overlay", modLoc(overlayTexture))
                    .texture("particle", modLoc(overlayTexture))
                    .element()
                    .from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#base").tintindex(0))
                    .end()
                    .element()
                    .from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#overlay"))
                    .end();
            return ConfiguredModel.builder().modelFile(oreModel).build();

            }, SellafieldSlakedBlock.COLOR_LEVEL);

        itemModels().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath(), modLoc("block/" + baseName));
    }

    public void simpleBlockWithItem(DeferredBlock<? extends Block> block, ModelFile model) {
        this.simpleBlockWithItem(block.get(), model);
    }

    /** Creates block with item, uses cube all model */
    public void simpleCubeAllBlock(DeferredBlock<? extends Block> block) { this.simpleBlockWithItem(block.get(), this.cubeAll(block.get())); }

    private void particleOnlyBlock(DeferredBlock<? extends Block> block, ResourceLocation particleTexture) { this.particleOnlyBlock(block, particleTexture, false); }
    private void particleOnlyBlock(DeferredBlock<? extends Block> block, ResourceLocation particleTexture, boolean frontLight) {
        this.simpleBlock(block.get(), this.models().getBuilder(name(block) + "_particle").texture("particle", particleTexture));
        this.entityBlockItem(block.get(), frontLight);
    }

    private void entityBlockItem(Block block, boolean frontLight) {
        this.itemModels().getBuilder(this.key(block).getPath()).parent(new ModelFile.UncheckedModelFile("builtin/entity")).guiLight(frontLight ? BlockModel.GuiLight.FRONT : BlockModel.GuiLight.SIDE);
    }

    private void crateBlock(Block block, String sideTexture, String topTexture) {
        String blockName = this.key(block).getPath();
        ModelFile model = this.models().cubeBottomTop(blockName, modLoc("block/" + sideTexture), modLoc("block/" + topTexture), modLoc("block/" + topTexture));
        this.getVariantBuilder(block).forAllStates(state -> {
            int y = switch(state.getValue(com.hbm.blocks.machine.CrateBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationY(y).build();
        });
        this.simpleBlockItem(block, model);
    }

    public void simpleCubeBottomTopBlock(DeferredBlock<? extends Block> block) {
        String blockName = name(block);
        this.simpleBlockWithItem(block, this.models().cubeBottomTop(blockName, modLoc("block/" + blockName + "_side"), modLoc("block/" + blockName + "_bottom"), modLoc("block/" + blockName + "_top")));
    }

    public void cubeTop(DeferredBlock<? extends Block> block) {
        String blockName = name(block);
        this.simpleBlockWithItem(block, this.models().cubeTop(blockName, modLoc("block/" + blockName + "_side"), modLoc("block/" + blockName + "_top")));
    }


    // ------------------------------------------------------------------------------------
    // Runde 90: die Rohrfamilie
    // ------------------------------------------------------------------------------------

    private static final float OCT_FLAT = 0.92387953F; // cos(22,5 Grad)
    private static final float OCT_EDGE = 0.38268343F; // sin(22,5 Grad)
    private static final float OCT_SHIM = 0.05F;       // Versatz gegen flimmernde Deckel

    /**
     * Die Rohre des Originals werden von einem eigenen Renderer aus OBJ-Modellen gezeichnet.
     * Auf 1.21 gibt es dafuer keine Entsprechung mehr; die Form muss aus Modellkaesten
     * zusammengesetzt werden. Das Achteck entsteht aus vier Baendern -- zwei achsenparallelen
     * und denselben beiden um 45 Grad gedreht. Ihre Vereinigung ist exakt das Achteck: die vier
     * Ecken jedes Bandes liegen auf vier der acht Achteckspitzen.
     */
    private void registerPipes() {

        /* Der Block heisst im Original "rusted", seine Textur aber "rusty". */
        String[] texture = { "", "_rusty", "_green", "_green_rusty", "_red", "_marked" };

        List<DeferredBlock<RotatedPillarBlock>> plain = List.of(
                NtmBlocks.DECO_PIPE, NtmBlocks.DECO_PIPE_RUSTED, NtmBlocks.DECO_PIPE_GREEN,
                NtmBlocks.DECO_PIPE_GREEN_RUSTED, NtmBlocks.DECO_PIPE_RED, NtmBlocks.DECO_PIPE_MARKED);
        List<DeferredBlock<RotatedPillarBlock>> rim = List.of(
                NtmBlocks.DECO_PIPE_RIM, NtmBlocks.DECO_PIPE_RIM_RUSTED, NtmBlocks.DECO_PIPE_RIM_GREEN,
                NtmBlocks.DECO_PIPE_RIM_GREEN_RUSTED, NtmBlocks.DECO_PIPE_RIM_RED, NtmBlocks.DECO_PIPE_RIM_MARKED);
        List<DeferredBlock<RotatedPillarBlock>> quad = List.of(
                NtmBlocks.DECO_PIPE_QUAD, NtmBlocks.DECO_PIPE_QUAD_RUSTED, NtmBlocks.DECO_PIPE_QUAD_GREEN,
                NtmBlocks.DECO_PIPE_QUAD_GREEN_RUSTED, NtmBlocks.DECO_PIPE_QUAD_RED, NtmBlocks.DECO_PIPE_QUAD_MARKED);
        List<DeferredBlock<RotatedPillarBlock>> framed = List.of(
                NtmBlocks.DECO_PIPE_FRAMED, NtmBlocks.DECO_PIPE_FRAMED_RUSTED, NtmBlocks.DECO_PIPE_FRAMED_GREEN,
                NtmBlocks.DECO_PIPE_FRAMED_GREEN_RUSTED, NtmBlocks.DECO_PIPE_FRAMED_RED, NtmBlocks.DECO_PIPE_FRAMED_MARKED);

        for(int i = 0; i < texture.length; i++) {
            ResourceLocation top =  this.modLoc("block/pipe_top" + texture[i]);
            ResourceLocation side = this.modLoc("block/pipe_side" + texture[i]);

            this.pipeBlock(plain.get(i),  this.pipeModel(this.name(plain.get(i)),  top, side, false, false));
            this.pipeBlock(rim.get(i),    this.pipeModel(this.name(rim.get(i)),    top, side, true,  false));
            this.pipeBlock(quad.get(i),   this.pipeModelQuad(this.name(quad.get(i)), top, side));
            this.pipeBlock(framed.get(i), this.pipeModel(this.name(framed.get(i)), top, side, true,  true));
        }
    }

    /**
     * Ein Rohr ist eine Saeule: beim Setzen richtet es sich nach der angeklickten Flaeche aus.
     * Das Modell steht senkrecht; die beiden waagerechten Lagen entstehen daraus durch Drehung,
     * genau wie bei einem Baumstamm.
     */
    private void pipeBlock(DeferredBlock<RotatedPillarBlock> block, ModelFile model) {

        this.getVariantBuilder(block.get()).forAllStates(state -> switch(state.getValue(RotatedPillarBlock.AXIS)) {
            case Y -> ConfiguredModel.builder().modelFile(model).build();
            case Z -> ConfiguredModel.builder().modelFile(model).rotationX(90).build();
            case X -> ConfiguredModel.builder().modelFile(model).rotationX(90).rotationY(90).build();
        });

        this.blockItem(block);
    }

    /**
     * Das nackte Rohr: eine achteckige Saeule von sechs Pixeln Radius. Mit Bund kommen an beide
     * Enden je ein Pixel hohe Ringe von sieben Pixeln Radius dazu, mit Gehaeuse ausserdem vier
     * Eckpfosten und die vier Gitterwaende zwischen ihnen.
     */
    private ModelFile pipeModel(String name, ResourceLocation top, ResourceLocation side, boolean hasRim, boolean hasFrame) {

        BlockModelBuilder builder = this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("top", top)
                .texture("side", side)
                .texture("particle", side);

        builder = this.octagon(builder, 8F, 8F, 6F, 0F, 16F);

        if(hasRim) {
            builder = this.octagon(builder, 8F, 8F, 7F, 0F, 1F);
            builder = this.octagon(builder, 8F, 8F, 7F, 15F, 16F);
        }

        if(hasFrame) {
            builder = builder.texture("frame", this.modLoc("block/pipe_frame"))
                    .texture("mesh", this.modLoc("block/pipe_mesh"))
                    .renderType("cutout");

            /* Vier Eckpfosten von zwei mal zwei Pixeln, ueber die volle Laenge. */
            for(int x = 0; x <= 14; x += 14) for(int z = 0; z <= 14; z += 14) {
                var post = builder.element().from(x, 0, z).to(x + 2, 16, z + 2);
                for(Direction dir : Direction.values()) post = post.face(dir).texture("#frame").end();
                builder = post.end();
            }

            /* Vier Gitterwaende ohne Dicke, je zwischen zwei Pfosten. */
            builder = this.pipeMeshX(builder, 1F);
            builder = this.pipeMeshX(builder, 15F);
            builder = this.pipeMeshZ(builder, 1F);
            builder = this.pipeMeshZ(builder, 15F);
        }

        return builder;
    }

    /** Das Vierlingsrohr: vier duenne Saeulen von zwei Pixeln Radius um die Blockmitte herum. */
    private ModelFile pipeModelQuad(String name, ResourceLocation top, ResourceLocation side) {

        BlockModelBuilder builder = this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("top", top)
                .texture("side", side)
                .texture("particle", side);

        for(int x = 4; x <= 12; x += 8) for(int z = 4; z <= 12; z += 8) {
            builder = this.octagon(builder, x, z, 2F, 0F, 16F);
        }

        return builder;
    }

    /**
     * Eine achteckige Saeule aus vier Baendern. Jedes Band traegt zwei der acht Achteckflaechen,
     * zusammen ergeben sie genau das Achteck.
     *
     * In der Mitte ueberlappen sich alle vier, und damit lagen auch ihre Deckel exakt
     * aufeinander. Bei den beiden gedrehten Baendern steht die Textur des Deckels um 45 Grad
     * schief -- zwei verschiedene Bilder in derselben Ebene flimmern gegeneinander. Die
     * gedrehten Baender enden deshalb ein Zwanzigstel Pixel frueher: zu wenig, um es zu sehen,
     * genug fuer den Tiefenpuffer. Die beiden ungedrehten duerfen sich weiter ueberlappen,
     * ihre Deckel zeigen an derselben Stelle dasselbe Bild.
     */
    private BlockModelBuilder octagon(BlockModelBuilder builder, float cx, float cz, float radius, float y1, float y2) {

        float flat = radius * OCT_FLAT; // halber Abstand Flaeche zu Flaeche
        float edge = radius * OCT_EDGE; // halbe Kantenlaenge

        builder = this.pipeBand(builder, cx, cz, flat, edge, y1, y2,  0F, Direction.WEST,  Direction.EAST);
        builder = this.pipeBand(builder, cx, cz, edge, flat, y1, y2,  0F, Direction.NORTH, Direction.SOUTH);
        builder = this.pipeBand(builder, cx, cz, flat, edge, y1 + OCT_SHIM, y2 - OCT_SHIM, 45F, Direction.WEST,  Direction.EAST);
        builder = this.pipeBand(builder, cx, cz, edge, flat, y1 + OCT_SHIM, y2 - OCT_SHIM, 45F, Direction.NORTH, Direction.SOUTH);

        return builder;
    }

    /** Ein Band des Achtecks. Es zeigt nur die beiden Flanken, die aussen liegen, dazu beide Enden. */
    private BlockModelBuilder pipeBand(BlockModelBuilder builder, float cx, float cz, float halfX, float halfZ,
            float y1, float y2, float angle, Direction first, Direction second) {

        var element = builder.element()
                .from(cx - halfX, y1, cz - halfZ)
                .to(cx + halfX, y2, cz + halfZ);

        if(angle != 0F) element = element.rotation().origin(cx, 8F, cz).axis(Direction.Axis.Y).angle(angle).end();

        return element
                .face(first).texture("#side").end()
                .face(second).texture("#side").end()
                .face(Direction.UP).texture("#top").end()
                .face(Direction.DOWN).texture("#top").end()
                .end();
    }

    /** Eine Gitterwand ohne Dicke, senkrecht zur X-Achse, zwischen zwei Eckpfosten gespannt. */
    private BlockModelBuilder pipeMeshX(BlockModelBuilder builder, float x) {

        return builder.element().from(x, 0F, 2F).to(x, 16F, 14F)
                .face(Direction.WEST).texture("#mesh").end()
                .face(Direction.EAST).texture("#mesh").end()
                .end();
    }

    /** Eine Gitterwand ohne Dicke, senkrecht zur Z-Achse. */
    private BlockModelBuilder pipeMeshZ(BlockModelBuilder builder, float z) {

        return builder.element().from(2F, 0F, z).to(14F, 16F, z)
                .face(Direction.NORTH).texture("#mesh").end()
                .face(Direction.SOUTH).texture("#mesh").end()
                .end();
    }

    /**
     * Der Hochleistungsbeton zeigt seinen Zerfall: bis zum neunten Grad sieht er unversehrt aus,
     * danach in vier Stufen immer rissiger. Die Schwellen sind die des Originals.
     */
    private void registerUberConcrete() {

        ModelFile whole =   this.models().cubeAll("concrete_super",    this.modLoc("block/concrete_super"));
        ModelFile cracked = this.models().cubeAll("concrete_super_m0", this.modLoc("block/concrete_super_m0"));
        ModelFile worse =   this.models().cubeAll("concrete_super_m1", this.modLoc("block/concrete_super_m1"));
        ModelFile bad =     this.models().cubeAll("concrete_super_m2", this.modLoc("block/concrete_super_m2"));
        ModelFile ruined =  this.models().cubeAll("concrete_super_m3", this.modLoc("block/concrete_super_m3"));

        this.getVariantBuilder(NtmBlocks.CONCRETE_SUPER.get()).forAllStates(state -> {
            int decay = state.getValue(UberConcreteBlock.DECAY);
            ModelFile model = decay == 15 ? ruined
                    : decay == 14 ? bad
                    : decay > 11 ? worse
                    : decay > 9 ? cracked
                    : whole;
            return ConfiguredModel.builder().modelFile(model).build();
        });

        this.simpleBlockItem(NtmBlocks.CONCRETE_SUPER.get(), whole);
    }

    // ------------------------------------------------------------------------------------
    // Runde 94: die Foerderbaender
    // ------------------------------------------------------------------------------------

    /**
     * Ein Band ist eine vier Pixel hohe Platte. Oben, unten und an den beiden Stirnseiten liegt
     * das Bandbild, an den beiden Laengsseiten das Wangenblech -- so haelt es das Original. In
     * der Kurve zeigen alle vier Seiten das Wangenblech, weil dort keine Stirnseite mehr ist.
     *
     * Das Grundmodell zeigt nach Norden; die drei uebrigen Richtungen entstehen durch Drehung.
     */
    private void conveyorBlock(DeferredBlock<? extends Block> block, String texture) {

        ModelFile straight = this.conveyorModel(texture, texture, false);
        ModelFile left =     this.conveyorModel(texture + "_curve_left", texture + "_curve_left", true);
        ModelFile right =    this.conveyorModel(texture + "_curve_right", texture + "_curve_right", true);

        this.getVariantBuilder(block.get()).forAllStates(state -> {

            ModelFile model = switch(state.getValue(ConveyorBendableBlock.SHAPE)) {
                case LEFT -> left;
                case RIGHT -> right;
                case STRAIGHT -> straight;
            };

            Direction facing = state.getValue(ConveyorBendableBlock.HORIZONTAL_FACING);

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(((facing.get2DDataValue() + 2) % 4) * 90)
                    .build();
        });

        this.blockItem(block);
    }

    /**
     * Steigband und Schacht sind volle Saeulen: rundum das Wangenblech, oben und unten das
     * Bandbild. Das oberste Stueck des Steigbands ist nur halb so hoch -- dort wird der
     * Gegenstand waagerecht abgesetzt, und ueber ihm muss Platz sein.
     *
     * ABWEICHUNG: das Original zeichnet beide mit einem eigenen Renderer, der Rollen und Kette
     * zeigt. Hier stehen zwei schlichte Kaesten; das Bild ist einfacher, die Form dieselbe.
     */
    private void conveyorVertical() {

        ModelFile liftFull = this.conveyorColumn("conveyor_lift", 16);
        ModelFile liftTop =  this.conveyorColumn("conveyor_lift_top", 8);

        this.getVariantBuilder(NtmBlocks.CONVEYOR_LIFT.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ConveyorLiftBlock.TOP) ? liftTop : liftFull)
                .rotationY(((state.getValue(ConveyorBaseBlock.HORIZONTAL_FACING).get2DDataValue() + 2) % 4) * 90)
                .build());
        this.blockItem(NtmBlocks.CONVEYOR_LIFT);

        ModelFile chute = this.conveyorColumn("conveyor_chute", 16);

        this.getVariantBuilder(NtmBlocks.CONVEYOR_CHUTE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(chute)
                .rotationY(((state.getValue(ConveyorBaseBlock.HORIZONTAL_FACING).get2DDataValue() + 2) % 4) * 90)
                .build());
        this.blockItem(NtmBlocks.CONVEYOR_CHUTE);
    }

    /**
     * Eine Kranmaschine zeigt an ihren Flaechen, wo es hinein- und wo es hinausgeht. Weil beide
     * Seiten frei einstellbar sind, gibt es dreissig Kombinationen; die Modelle dafuer werden
     * hier erzeugt, statt sie von Hand zu schreiben.
     *
     * ABWEICHUNG: das Original hat fuer jede Seitenkombination ein eigenes Pfeilbild -- mit
     * Abbiegungen nach links, rechts, oben und unten, dreizehn Bilder je Maschine. Hier stehen
     * nur zwei: eines fuer den Eingang, eines fuer den Ausgang. Man sieht, WO es hinein- und
     * hinausgeht, aber nicht, wie der Weg im Inneren verlaeuft.
     */
    private void craneBlock(DeferredBlock<? extends Block> block) {
        this.craneBlock(block, modLoc("block/crane_in"), modLoc("block/crane_side_in"));
    }

    /**
     * Wie oben, aber mit eigenen Bildern fuer die Eingangsflaechen. Der Greifer nimmt damit
     * die Greifhand des Originals statt des Einlegepfeils -- man sieht am Block, dass er zieht
     * und nicht annimmt.
     */
    private void craneBlock(DeferredBlock<? extends Block> block, ResourceLocation in, ResourceLocation sideIn) {
        this.craneBlock(block, in, sideIn, modLoc("block/crane_out"), modLoc("block/crane_side_out"));
    }

    /** Wie oben, aber auch mit eigenen Bildern fuer die Ausgangsflaechen. */
    private void craneBlock(DeferredBlock<? extends Block> block, ResourceLocation in, ResourceLocation sideIn,
            ResourceLocation out, ResourceLocation sideOut) {

        String name = this.name(block);
        Map<String, ModelFile> models = new LinkedHashMap<>();

        for(Direction input : Direction.values()) {
            for(Direction output : Direction.values()) {

                if(input == output) continue;

                String key = name + "_" + input.getSerializedName() + "_" + output.getSerializedName();
                models.put(key, this.craneModel(key, input, output, in, sideIn, out, sideOut));
            }
        }

        this.getVariantBuilder(block.get()).forAllStates(state -> {

            Direction input = state.getValue(CraneBaseBlock.INPUT);
            Direction output = state.getValue(CraneBaseBlock.OUTPUT);

            /* Ein Blockstate mit gleicher Ein- und Ausgangsseite kommt im Spiel nicht vor,
             * die Blockstate-Datei muss ihn aber trotzdem kennen. */
            if(input == output) output = input.getOpposite();

            String key = name + "_" + input.getSerializedName() + "_" + output.getSerializedName();
            return ConfiguredModel.builder().modelFile(models.get(key)).build();
        });

        this.simpleBlockItem(block.get(), models.get(name + "_north_south"));
    }

    private ModelFile craneModel(String name, Direction input, Direction output, ResourceLocation in, ResourceLocation sideIn,
            ResourceLocation out, ResourceLocation sideOut) {

        var builder = this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("top", this.modLoc("block/crane_top"))
                .texture("side", this.modLoc("block/crane_side"))
                .texture("in", in)
                .texture("side_in", sideIn)
                .texture("out", out)
                .texture("side_out", sideOut)
                .texture("particle", this.modLoc("block/crane_side"))
                .element()
                .from(0, 0, 0)
                .to(16, 16, 16);

        for(Direction dir : Direction.values()) {

            boolean vertical = dir.getAxis() == Direction.Axis.Y;

            String texture;
            if(dir == input) texture = vertical ? "#in" : "#side_in";
            else if(dir == output) texture = vertical ? "#out" : "#side_out";
            else texture = vertical ? "#top" : "#side";

            builder = builder.face(dir).texture(texture).cullface(dir).end();
        }

        return builder.end();
    }

    /**
     * Ein Gleis: gerade in zwei Lagen, in der Kurve ein eigenes Bild, und die vier Steigungen
     * sind das gerade Bild, aufgestellt. Genau so macht es Minecraft mit seinen eigenen Gleisen.
     */
    private void railBlock(DeferredBlock<? extends Block> block) {

        String name = this.name(block);

        ModelFile flat = this.models().withExistingParent(name, mcLoc("block/rail_flat"))
                .renderType("cutout").texture("rail", modLoc("block/" + name));
        ModelFile curved = this.models().withExistingParent(name + "_curved", mcLoc("block/rail_curved"))
                .renderType("cutout").texture("rail", modLoc("block/" + name + "_turned"));
        ModelFile raised = this.models().withExistingParent(name + "_raised_ne", mcLoc("block/template_rail_raised_ne"))
                .renderType("cutout").texture("rail", modLoc("block/" + name));

        this.getVariantBuilder(block.get()).forAllStates(state -> {

            RailShape shape = state.getValue(((net.minecraft.world.level.block.RailBlock) block.get()).getShapeProperty());

            return switch(shape) {
                case NORTH_SOUTH ->      ConfiguredModel.builder().modelFile(flat).build();
                case EAST_WEST ->        ConfiguredModel.builder().modelFile(flat).rotationY(90).build();
                case ASCENDING_EAST ->   ConfiguredModel.builder().modelFile(raised).rotationY(90).build();
                case ASCENDING_WEST ->   ConfiguredModel.builder().modelFile(raised).rotationY(270).build();
                case ASCENDING_NORTH ->  ConfiguredModel.builder().modelFile(raised).build();
                case ASCENDING_SOUTH ->  ConfiguredModel.builder().modelFile(raised).rotationY(180).build();
                case SOUTH_EAST ->       ConfiguredModel.builder().modelFile(curved).build();
                case SOUTH_WEST ->       ConfiguredModel.builder().modelFile(curved).rotationY(90).build();
                case NORTH_WEST ->       ConfiguredModel.builder().modelFile(curved).rotationY(180).build();
                case NORTH_EAST ->       ConfiguredModel.builder().modelFile(curved).rotationY(270).build();
            };
        });

        this.itemModels().basicItem(block.asItem());
    }

    private ModelFile conveyorColumn(String name, int height) {

        var builder = this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("top", this.modLoc("block/conveyor"))
                .texture("side", this.modLoc("block/conveyor_side"))
                .texture("particle", this.modLoc("block/conveyor_side"))
                .element()
                .from(0, 0, 0)
                .to(16, height, 16);

        builder = builder.face(Direction.UP).texture("#top").end();
        builder = builder.face(Direction.DOWN).texture("#top").end();

        for(Direction dir : Direction.Plane.HORIZONTAL) builder = builder.face(dir).texture("#side").end();

        return builder.end();
    }

    private ModelFile conveyorModel(String name, String texture, boolean allSides) {

        var builder = this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("top", this.modLoc("block/" + texture))
                .texture("side", this.modLoc("block/conveyor_side"))
                .texture("particle", this.modLoc("block/conveyor_side"))
                .element()
                .from(0, 0, 0)
                .to(16, 4, 16);

        builder = builder.face(Direction.UP).texture("#top").end();
        builder = builder.face(Direction.DOWN).texture("#top").end();

        /* Die Stirnseiten liegen quer zur Fahrt -- im Grundmodell also Norden und Sueden. */
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            boolean lengthwise = dir.getAxis() == Direction.Axis.X;
            builder = builder.face(dir).texture(allSides || lengthwise ? "#side" : "#top").end();
        }

        return builder.end();
    }

    protected String name(Block block) { return this.key(block).getPath(); }
    protected String name(DeferredBlock<? extends Block> block) { return this.key(block).getPath(); }
    protected ResourceLocation key(Block block) { return BuiltInRegistries.BLOCK.getKey(block); }
    protected ResourceLocation key(DeferredBlock<? extends Block> block) { return BuiltInRegistries.BLOCK.getKey(block.get()); }
    protected ResourceLocation blockTexture(DeferredBlock<? extends Block> block) { ResourceLocation name = this.key(block); return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), "block/" + name.getPath()); }
    protected ResourceLocation blockTexture(DeferredBlock<? extends Block> block, String toAppend) { ResourceLocation name = this.key(block); return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), "block/" + name.getPath() + toAppend); }

    private void blockItem(DeferredBlock<? extends Block> block) { this.blockItem(block, ""); }
    private void blockItem(DeferredBlock<? extends Block> block, String suffix) { this.simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile("hbmsntm:block/" + block.getId().getPath() + suffix)); }

    protected static class DuctBlockLoaderBuilder extends BlockModelBuilderBase {
        public DuctBlockLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.PIPE; }
    }
    protected static class BarrelBlockModelBuilder extends BlockModelBuilderBase {
        public BarrelBlockModelBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.BARREL; }
    }
    protected static class CableBlockLoaderBuilder extends BlockModelBuilderBase {
        public CableBlockLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.CABLE; }
    }
    protected static class DetCordBlockLoaderBuilder extends BlockModelBuilderBase {
        public DetCordBlockLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.DET_CORD; }
    }
    protected static class BarbedWireBlockLoaderBuilder extends BlockModelBuilderBase {
        public BarbedWireBlockLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.BARBED_WIRE; }
    }
    protected static class SpikesLoaderBuilder extends BlockModelBuilderBase {
        public SpikesLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.SPIKES; }
    }
    public static class AnvilLoaderBuilder extends BlockModelBuilderBase {
        public AnvilLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(parent, helper);
        }
        @Override public BakedModelType getType() { return BakedModelType.ANVIL; }
    }
    public static abstract class BlockModelBuilderBase extends CustomLoaderBuilder<BlockModelBuilder> {

        private final Map<String, ResourceLocation> textures = new LinkedHashMap<>();

        protected BlockModelBuilderBase(BlockModelBuilder parent, ExistingFileHelper helper) {
            super(NuclearTechMod.withDefaultNamespace("ntm_geometry_loader"), parent, helper, false);
        }

        public BlockModelBuilderBase texture(String key, ResourceLocation location) {
            this.textures.put(key, location);
            return this;
        }

        @Override
        public JsonObject toJson(JsonObject json) {
            super.toJson(json);

            JsonObject texturesObject = new JsonObject();
            for(Entry<String, ResourceLocation> entry : this.textures.entrySet()) {
                texturesObject.addProperty(entry.getKey(), entry.getValue().toString());
            }
            json.add("textures", texturesObject);
            json.addProperty("type", this.getType().name().toLowerCase(Locale.US));

            return json;
        }

        public abstract BakedModelType getType();
    }

    public static abstract class ItemModelBuilderBase extends CustomLoaderBuilder<ItemModelBuilder> {

        private final Map<String, ResourceLocation> textures = new LinkedHashMap<>();

        protected ItemModelBuilderBase(ItemModelBuilder parent, ExistingFileHelper helper) {
            super(NuclearTechMod.withDefaultNamespace("ntm_geometry_loader"), parent, helper, false);
        }

        public ItemModelBuilderBase texture(String key, ResourceLocation location) {
            this.textures.put(key, location);
            return this;
        }

        @Override
        public JsonObject toJson(JsonObject json) {
            super.toJson(json);

            JsonObject texturesObject = new JsonObject();
            for(Entry<String, ResourceLocation> entry : this.textures.entrySet()) {
                texturesObject.addProperty(entry.getKey(), entry.getValue().toString());
            }
            json.add("textures", texturesObject);
            json.addProperty("type", this.getType().name().toLowerCase(Locale.US));

            return json;
        }

        public abstract BakedModelType getType();
    }
}
