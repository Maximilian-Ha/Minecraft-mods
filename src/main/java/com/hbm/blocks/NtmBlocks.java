package com.hbm.blocks;

import com.hbm.blocks.bomb.*;
import com.hbm.blocks.fluids.RadLiquidBlock;
import com.hbm.blocks.turret.TurretChekhovBlock;
import com.hbm.blocks.turret.TurretFriendlyBlock;
import com.hbm.blocks.turret.TurretHowardBlock;
import com.hbm.blocks.turret.TurretHowardDamagedBlock;
import com.hbm.blocks.turret.TurretJeremyBlock;
import com.hbm.blocks.turret.TurretSentryBlock;
import com.hbm.blocks.turret.TurretSentryDamagedBlock;
import com.hbm.blocks.machine.pile.PileControlBlock;
import com.hbm.blocks.machine.pile.PileLoaderBlock;
import com.hbm.blocks.machine.pile.PileVentBlock;
import com.hbm.blocks.machine.fusion.*;
import com.hbm.blocks.machine.icf.*;
import com.hbm.blocks.machine.pile.PileBlock;
import com.hbm.blocks.machine.pile.PileBrickBlock;
import com.hbm.blocks.machine.WatzBlock;
import com.hbm.blocks.machine.WatzPumpBlock;
import com.hbm.blocks.machine.WatzStructBlock;
import com.hbm.blocks.generic.ToolConversionBlock;
import com.hbm.blocks.generic.SteelRoofBlock;
import com.hbm.blocks.generic.LootDecoBlock;
import com.hbm.blocks.generic.FileCabinetBlock;
import com.hbm.blocks.generic.WandLogicBlock;
import com.hbm.blocks.generic.WandLootBlock;
import com.hbm.blocks.generic.SupplyCrateBlock;
import com.hbm.blocks.generic.DecoPoleSatelliteReceiverBlock;
import com.hbm.blocks.generic.MushBlock;
import com.hbm.blocks.generic.MushHugeBlock;
import com.hbm.blocks.generic.LootCrateBlock;
import com.hbm.blocks.machine.SoyuzCapsuleBlock;
import com.hbm.blocks.machine.FloodlightBlock;
import com.hbm.blocks.machine.ChargerBlock;
import com.hbm.blocks.machine.BroadcasterBlock;
import com.hbm.blocks.machine.DemonLampBlock;
import com.hbm.blocks.machine.FurnaceBrickBlock;
import com.hbm.blocks.machine.MachineMicrowaveBlock;
import com.hbm.blocks.generic.RedBrickBlock;
import com.hbm.blocks.generic.DungeonSpawnerBlock;
import com.hbm.blocks.machine.MeteorSpawnerBlock;
import com.hbm.blocks.generic.KeyholeBlock;
import com.hbm.blocks.generic.RedBrickKeyholeBlock;
import com.hbm.blocks.generic.PedestalBlock;
import com.hbm.blocks.generic.SkeletonHolderBlock;
import com.hbm.blocks.machine.TeslaBlock;
import com.hbm.blocks.network.RadioRecBlock;
import com.hbm.blocks.network.RadioTelexBlock;
import com.hbm.blocks.machine.FloodlightBeamBlock;
import com.hbm.blocks.fluids.CoriumLiquidBlock;
import com.hbm.blocks.fluids.MudLiquidBlock;
import com.hbm.blocks.fluids.SulfuricAcidLiquidBlock;
import com.hbm.blocks.fluids.ToxicLiquidBlock;
import com.hbm.blocks.fluids.VolcanicLiquidBlock;
import com.hbm.blocks.gas.*;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.machine.rbmk.RBMKBoilerBlock;
import com.hbm.blocks.machine.rbmk.RBMKHeaterBlock;
import com.hbm.blocks.machine.rbmk.RBMKOutgasserBlock;
import com.hbm.blocks.machine.rbmk.RBMKPortBlock;
import com.hbm.blocks.machine.rbmk.RBMKLoaderBlock;
import com.hbm.blocks.network.ConveyorBlock;
import com.hbm.blocks.network.CraneExtractorBlock;
import com.hbm.blocks.network.CraneInserterBlock;
import com.hbm.blocks.network.ConveyorChuteBlock;
import com.hbm.blocks.network.ConveyorLiftBlock;
import com.hbm.blocks.network.ConveyorDoubleBlock;
import com.hbm.blocks.network.ConveyorExpressBlock;
import com.hbm.blocks.network.ConveyorTripleBlock;
import com.hbm.blocks.network.FluidCounterValveBlock;
import com.hbm.blocks.network.FluidDuctGaugeBlock;
import com.hbm.blocks.network.FluidSwitchBlock;
import com.hbm.blocks.network.FluidValveBlock;
import com.hbm.blocks.machine.WasteDrumBlock;
import com.hbm.blocks.machine.rbmk.RBMKAutoloaderBlock;
import com.hbm.blocks.machine.rbmk.RBMKConsoleBlock;
import com.hbm.blocks.machine.rbmk.RBMKCraneConsoleBlock;
import com.hbm.blocks.machine.rbmk.RBMKControlAutoBlock;
import com.hbm.blocks.machine.rbmk.RBMKControlBlock;
import com.hbm.blocks.machine.rbmk.RBMKGaugeBlock;
import com.hbm.blocks.machine.rbmk.RBMKIndicatorBlock;
import com.hbm.blocks.machine.rbmk.RBMKNumitronBlock;
import com.hbm.blocks.machine.rbmk.RBMKLeverBlock;
import com.hbm.blocks.machine.rbmk.RBMKKeyPadBlock;
import com.hbm.blocks.machine.rbmk.RBMKGraphBlock;
import com.hbm.blocks.machine.rbmk.RBMKDisplayBlock;
import com.hbm.blocks.machine.rbmk.RBMKTerminalBlock;
import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBlock;
import com.hbm.blocks.machine.rbmk.RBMKDebrisRadiatingBlock;
import com.hbm.blocks.machine.rbmk.RBMKCoolerBlock;
import com.hbm.blocks.machine.rbmk.RBMKStorageBlock;
import com.hbm.blocks.machine.rbmk.RBMKDebrisBlock;
import com.hbm.blocks.machine.rbmk.RBMKDebrisBurningBlock;
import com.hbm.blocks.machine.rbmk.RBMKDebrisDigammaBlock;
import com.hbm.blocks.machine.rbmk.RBMKPassiveBlock;
import com.hbm.blocks.machine.rbmk.RBMKRodBlock;
import com.hbm.blocks.machine.rbmk.RBMKRodReaSimBlock;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.blocks.machine.*;
import com.hbm.blocks.machine.albion.*;
import com.hbm.blocks.machine.heater.*;
import com.hbm.blocks.network.*;
import com.hbm.fluids.NtmFluids;
import com.hbm.items.NtmItems;
import com.hbm.items.block.BlastInfoBlockItem;
import com.hbm.items.block.BlockItemBase;
import com.hbm.items.tools.HEVBatteryItem;
import com.hbm.items.block.LoreBlockItem;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NtmBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NuclearTechMod.MODID);

    // Ores
    public static final DeferredBlock<Block> ORE_OIL = register("ore_oil", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_OIL_EMPTY = register("ore_oil_empty", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_OIL_SAND = register("ore_oil_sand", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_BEDROCK_OIL = registerBlastInfoBlock("ore_bedrock_oil", () -> new Block(BlockBehaviour.Properties.of().strength(-1.0F, 2160000.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    /* Runde 131, das Grundgesteinserz. Beide sind unzerstoerbar (Haerte -1): an das Erz kommt nur
     * der Bagger, an das Tiefengestein nur Werkzeug mit IDepthRockTool. */
    public static final DeferredBlock<Block> ORE_BEDROCK = register("ore_bedrock", () -> new BedrockOreBlock(BlockBehaviour.Properties.of().strength(-1.0F, 1000000.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).isValidSpawn(Blocks::never)));
    public static final DeferredBlock<Block> STONE_DEPTH = register("stone_depth", () -> new DepthRockBlock(BlockBehaviour.Properties.of().strength(-1.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));

    public static final DeferredBlock<Block> ORE_URANIUM = register("ore_uranium", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_URANIUM_DEEPSLATE = register("ore_uranium_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_URANIUM_SCORCHED = register("ore_uranium_scorched", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_BERYLLIUM = register("ore_beryllium", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_BERYLLIUM_DEEPSLATE = register("ore_beryllium_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_TUNGSTEN = register("ore_tungsten", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_TUNGSTEN_DEEPSLATE = register("ore_tungsten_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_TITANIUM = register("ore_titanium", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F, 6F).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> ORE_TITANIUM_DEEPSLATE = register("ore_titanium_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F, 8F).sound(SoundType.DEEPSLATE).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> ORE_LEAD = register("ore_lead", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_LEAD_DEEPSLATE = register("ore_lead_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_ALUMINIUM = register("ore_aluminium", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_ALUMINIUM_DEEPSLATE = register("ore_aluminium_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_ASBESTOS = register("ore_asbestos", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_ASBESTOS_DEEPSLATE = register("ore_asbestos_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_THORIUM = register("ore_thorium", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_THORIUM_DEEPSLATE = register("ore_thorium_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_NITER = register("ore_niter", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_NITER_DEEPSLATE = register("ore_niter_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_COBALT = register("ore_cobalt", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_COBALT_DEEPSLATE = register("ore_cobalt_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_CINNABAR = register("ore_cinnabar", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_CINNABAR_DEEPSLATE = register("ore_cinnabar_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_FLUORITE = register("ore_fluorite", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_FLUORITE_DEEPSLATE = register("ore_fluorite_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_METEOR_IRON = register("ore_meteor_iron", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> ORE_METEOR_COBALT = register("ore_meteor_cobalt", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> ORE_METEOR_COPPER = register("ore_meteor_copper", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> ORE_METEOR_ALUMINIUM = register("ore_meteor_aluminium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> ORE_METEOR_RARE = register("ore_meteor_rare", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> ORE_RARE = register("ore_rare", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_RARE_DEEPSLATE = register("ore_rare_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_SULFUR = register("ore_sulfur", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(6.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_SULFUR_DEEPSLATE = register("ore_sulfur_deepslate", () -> new Block(BlockBehaviour.Properties.of().strength(0.8F).explosionResistance(8.0F).sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> ORE_LIGNITE = register("ore_lignite", () -> new Block(BlockBehaviour.Properties.of().strength(3F, 3F).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> ORE_DEEPSLATE_LIGNITE = register("ore_deepslate_lignite", () -> new Block(BlockBehaviour.Properties.of().strength(4.5F, 3F).sound(SoundType.DEEPSLATE).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> ORE_SCHRABIDIUM = register("ore_schrabidium", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F).explosionResistance(600.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_NETHER_URANIUM = register("ore_nether_uranium", () -> new Block(BlockBehaviour.Properties.of().strength(0.4F).explosionResistance(10.0F).sound(SoundType.NETHER_ORE)));
    public static final DeferredBlock<Block> ORE_NETHER_URANIUM_SCORCHED = register("ore_nether_uranium_scorched", () -> new Block(BlockBehaviour.Properties.of().strength(0.4F).explosionResistance(10.0F).sound(SoundType.NETHER_ORE)));
    public static final DeferredBlock<Block> ORE_NETHER_PLUTONIUM = register("ore_nether_plutonium", () -> new Block(BlockBehaviour.Properties.of().strength(0.4F).explosionResistance(10.0F).sound(SoundType.NETHER_ORE)));
    public static final DeferredBlock<Block> ORE_NETHER_SCHRABIDIUM = register("ore_nether_schrabidium", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F).explosionResistance(600.0F).sound(SoundType.NETHER_ORE)));
    public static final DeferredBlock<Block> ORE_TIKITE = register("ore_tikite", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_GNEISS_URANIUM = register("ore_gneiss_uranium", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_GNEISS_URANIUM_SCORCHED = register("ore_gneiss_uranium_scorched", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).explosionResistance(10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ORE_GNEISS_SCHRABIDIUM = register("ore_gneiss_schrabidium", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).explosionResistance(10.0F).sound(SoundType.STONE)));

    //Resource stones
    public static final DeferredBlock<Block> RESOURCE_LIMESTONE = register("resource_limestone", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.NETHERRACK)));
    public static final DeferredBlock<Block> RESOURCE_BAUXITE = register("resource_bauxite", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.NETHERRACK)));
    public static final DeferredBlock<Block> RESOURCE_HEMATITE = register("resource_hematite", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.NETHERRACK)));
    public static final DeferredBlock<Block> RESOURCE_MALACHITE = register("resource_malachite", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.NETHERRACK)));
    public static final DeferredBlock<Block> RESOURCE_CHRYSOTILE = register("resource_chrysotile", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.NETHERRACK)));
    public static final DeferredBlock<Block> RESOURCE_SULFUROUS_STONE = register("resource_sulfurous_stone", () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.NETHERRACK)));

    // Basalt ores
    public static final DeferredBlock<Block> ORE_BASALT = registerNew("ore_basalt", () -> new OreBasaltBlock(BlockBehaviour.Properties.of().strength(5F, 10F).sound(SoundType.BASALT).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));

    // Stone Variants
    public static final DeferredBlock<RotatedPillarBlock> BASALT = register("basalt",          () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(5F, 10F).sound(SoundType.BASALT).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> BASALT_SMOOTH =       register("basalt_smooth",   () -> new Block(BlockBehaviour.Properties.of().strength(5F, 10F).sound(SoundType.BASALT).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> BASALT_BRICK =        register("basalt_brick",    () -> new Block(BlockBehaviour.Properties.of().strength(5F, 10F).sound(SoundType.BASALT).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> BASALT_POLISHED =     register("basalt_polished", () -> new Block(BlockBehaviour.Properties.of().strength(5F, 10F).sound(SoundType.BASALT).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> BASALT_TILES =        register("basalt_tiles",    () -> new Block(BlockBehaviour.Properties.of().strength(5F, 10F).sound(SoundType.BASALT).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> STONE_CRACKED =       register("stone_cracked",   () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DIRT_OILY =           register("dirt_oily",       () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<Block> SAND_OILY =           register("sand_oily",       () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.SAND)));
    public static final DeferredBlock<Block> SAND_RED_OILY =       register("sand_oily_red",   () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.SAND)));
    public static final DeferredBlock<Block> DIRT_DEAD =           register("dirt_dead",       () -> new Block(BlockBehaviour.Properties.of().strength(0.7F).explosionResistance(5.0F).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<Block> OIL_SPILL =           register("oil_spill",       () -> new OilSpillBlock(BlockBehaviour.Properties.of().strength(0.1F).sound(SoundType.SLIME_BLOCK).noOcclusion().isValidSpawn(Blocks::never).isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never).pushReaction(PushReaction.DESTROY).isRedstoneConductor(NtmBlocks::never)));

    // Blocks
    public static final DeferredBlock<Block> BLOCK_SCRAP = register("block_scrap", () -> new ColoredFallingBlock(new ColorRGBA(-8356741), BlockBehaviour.Properties.of().strength(2.5F, 5.0F).sound(SoundType.GRAVEL)));


    public static final DeferredBlock<Block> BLOCK_ACTINIUM = register("block_actinium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_STEEL = register("block_steel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_ALUMINIUM = register("block_aluminium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_ASBESTOS = register("block_asbestos", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_GRAPHITE = register("block_graphite", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> BLOCK_BORON = register("block_boron", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_AUSTRALIUM = register("block_australium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_BERYLLIUM = register("block_beryllium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_BISMUTH = register("block_bismuth", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_CADMIUM = register("block_cadmium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_CDALLOY = register("block_cdalloy", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_COLTAN = register("block_coltan", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_COMBINE_STEEL = register("block_combine_steel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_COPPER = register("block_copper", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_DESH = register("block_desh", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Stufe 5: Sternmetall traegt im Original ein Leuchtfeuer (BlockBeaconable) -- im Port
     * macht das der Tag minecraft:beacon_base_blocks. Werte aus ModBlocks.java:1392. */
    public static final DeferredBlock<Block> BLOCK_STARMETAL = register("block_starmetal", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 400.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Stufe 5: die Erde der Bauwerke. Sie sieht aus wie gewoehnliche Erde, heisst so und
     * faellt auch als solche ab -- im Original ein BlockNTMDirt, damit die Bauwerke eine
     * eigene Erde setzen koennen, ohne dass der Spieler etwas davon merkt. */
    public static final DeferredBlock<Block> NTM_DIRT = register("ntm_dirt", () -> new Block(BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GRAVEL).mapColor(MapColor.DIRT)));

    /* Stufe 5: Elektroschrott faellt, wenn ihm der Halt fehlt. Werte aus ModBlocks.java:1377. */
    public static final DeferredBlock<Block> BLOCK_ELECTRICAL_SCRAP = register("block_electrical_scrap", () -> new SimpleFallingBlock(BlockBehaviour.Properties.of().strength(2.5F, 5.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_DINEUTRONIUM = register("block_dineutronium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_DURA_STEEL = register("block_dura_steel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_EUPHEMIUM = register("block_euphemium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_FOAM = register("block_foam", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_LANTHANIUM = register("block_lanthanium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_LEAD = register("block_lead", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_LITHIUM = register("block_lithium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_MAGNETIZED_TUNGSTEN = register("block_magnetized_tungsten", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_METEOR = register("block_meteor", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_METEOR_MOLTEN = register("block_meteor_molten", () -> new MoltenMeteorBlock(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).lightLevel(state -> 12).randomTicks()));
    public static final DeferredBlock<Block> BLOCK_METEOR_COBBLE = register("block_meteor_cobble", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_METEOR_BROKEN = register("block_meteor_broken", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_METEOR_TREASURE = register("block_meteor_treasure", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_MOX_FUEL = register("block_mox_fuel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_NEPTUNIUM = register("block_neptunium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_NIOBIUM = register("block_niobium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_NITER = register("block_niter", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_PLUTONIUM = register("block_plutonium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_PLUTONIUM_FUEL = register("block_plutonium_fuel", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_POLONIUM = register("block_polonium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_PU238 = register("block_pu238", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_PU239 = register("block_pu239", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_PU240 = register("block_pu240", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_PU_MIX = register("block_pu_mix", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_RA226 = register("block_ra226", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_RED_COPPER = register("block_red_copper", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SATURNITE = register("block_saturnite", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SCHRABIDATE = register("block_schrabidate", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SCHRABIDIUM = register("block_schrabidium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SCHRABIDIUM_FUEL = register("block_schrabidium_fuel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SCHRARANIUM = register("block_schraranium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SMORE = register("block_smore", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SOLINIUM = register("block_solinium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SULFUR = register("block_sulfur", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_TANTALIUM = register("block_tantalium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_TCALLOY = register("block_tcalloy", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_THORIUM = register("block_thorium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_THORIUM_FUEL = register("block_thorium_fuel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_TITANIUM = register("block_titanium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_TUNGSTEN = register("block_tungsten", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_U233 = register("block_u233", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_U235 = register("block_u235", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_U238 = register("block_u238", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_URANIUM = register("block_uranium", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_URANIUM_FUEL = register("block_uranium_fuel", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_WASTE = register("block_waste", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_WASTE_PAINTED = register("block_waste_painted", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_WASTE_VITRIFIED = register("block_waste_vitrified", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_YELLOWCAKE = register("block_yellowcake", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_FIBERGLASS = register("block_fiberglass", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> REINFORCED_LAMINATE = registerNew("reinforced_laminate", () -> new Block(BlockBehaviour.Properties.of().strength(0.3F, 3.0F).sound(SoundType.GLASS).noOcclusion().mapColor(MapColor.NONE)));
    public static final DeferredBlock<Block> BLOCK_INSULATOR = register("block_insulator", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BLOCK_SLAG = register("block_slag", () -> new SolidSlagBlock(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    // Deco Blocks
    public static final DeferredBlock<Block> BOBBLEHEAD = registerNew("bobblehead", () -> new BobbleBlock(BlockBehaviour.Properties.of().noOcclusion().instabreak().mapColor(DyeColor.WHITE)));
    public static final DeferredBlock<Block> PLUSHIE = registerNew("plushie", () -> new PlushieBlock(BlockBehaviour.Properties.of().noOcclusion().instabreak().sound(SoundType.WOOL).mapColor(DyeColor.WHITE)));
    public static final DeferredBlock<Block> PLANT_FLOWER = registerNew("plant_flower", () -> new Block(BlockBehaviour.Properties.of().noOcclusion().instabreak().sound(SoundType.GRASS).mapColor(DyeColor.GREEN)));
    public static final DeferredBlock<Block> GLASS_QUARTZ = register("glass_quartz", () -> new Block(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(SoundType.GLASS).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> GLASS_LEAD = register("glass_lead", () -> new Block(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(SoundType.GLASS).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> GLASS_BORON = register("glass_boron", () -> new Block(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(SoundType.GLASS).mapColor(MapColor.METAL)));

    // Gravel
    public static final DeferredBlock<Block> GRAVEL_OBSIDIAN = registerBlastInfoBlock("gravel_obsidian", () -> new ColoredFallingBlock(new ColorRGBA(-8356741), BlockBehaviour.Properties.of().strength(5.0F, 240.0F).sound(SoundType.GRAVEL).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> GRAVEL_DIAMOND = register("gravel_diamond", () -> new ColoredFallingBlock(new ColorRGBA(-8356741), BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRAVEL).mapColor(MapColor.STONE)), LoreBlockItem.class, new Properties().rarity(Rarity.RARE));
    public static final DeferredBlock<Block> MOON_TURF = register("moon_turf", () -> new ColoredFallingBlock(new ColorRGBA(-176741), BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SAND).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> SAND_QUARTZ = register("sand_quartz", () -> new ColoredFallingBlock(new ColorRGBA(-845741), BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SAND).mapColor(MapColor.SNOW)));
    /**
     * Borsand -- der Sand, den der Feuerloescher verschiesst, sobald er aufgehaeuft eine
     * volle Schicht erreicht hat. Im Original ist er eine Metadaten-Spielart von sand_mix;
     * den gibt es hier nicht, und SAND_QUARTZ zeigt, wie der Port es sonst haelt: ein
     * eigener Block mit eigenem Namen.
     */
    public static final DeferredBlock<Block> SAND_BORON = register("sand_boron", () -> new ColoredFallingBlock(new ColorRGBA(-2434342), BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SAND).mapColor(MapColor.TERRACOTTA_WHITE)));

    // Reinforced Blocks
    public static final DeferredBlock<Block> ASPHALT =       registerBlastInfoBlock("asphalt",       () -> new SpeedyBlock(1.5, BlockBehaviour.Properties.of().strength(15.0F, 120.0F)                                  .mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> ASPHALT_LIGHT = registerBlastInfoBlock("asphalt_light", () -> new SpeedyBlock(1.5, BlockBehaviour.Properties.of().strength(15.0F, 120.0F).lightLevel(state -> 15).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> STEEL_SCAFFOLD = register("steel_scaffold", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    // Werte 1:1 aus ModBlocks.java:1601 bzw. :1269 des Originals.
    /* BERICHTIGT (Stufe 5): der Traeger ist im Original kein Wuerfel, sondern eine duenne
     * Saeule mit eigenem Modell (beam.obj). Werte aus ModBlocks.java:1601. */
    public static final DeferredBlock<Block> STEEL_BEAM = register("steel_beam", () -> new SteelBeamBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Stufe 5: das Dachblech. Nur die unterste Lage traegt, die Streben darueber sind
     * Aussehen. Werte aus ModBlocks.java:1600. */
    public static final DeferredBlock<Block> STEEL_ROOF = register("steel_roof", () -> new SteelRoofBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Stufe 5: die drei Scheinwerfer und ihr Lichtkegel. Reichweiten 2, 8 und 32 Bloecke wie
     * im Original (ModBlocks.java:1483 ff.); das Rotsteinsignal schaltet sie AUS. */
    public static final DeferredBlock<Block> SPOTLIGHT_INCANDESCENT = register("spotlight_incandescent", () -> new SpotlightBlock(spotlight(), 2, SpotlightBlock.Bauart.GLUEHBIRNE));
    public static final DeferredBlock<Block> SPOTLIGHT_FLUORO = register("spotlight_fluoro", () -> new SpotlightBlock(spotlight(), 8, SpotlightBlock.Bauart.LEUCHTSTOFF));
    public static final DeferredBlock<Block> SPOTLIGHT_HALOGEN = register("spotlight_halogen", () -> new SpotlightBlock(spotlight(), 32, SpotlightBlock.Bauart.HALOGEN));
    /* Stufe 5: der alte Kessel aus den Bauwerken. Im Original gibt es ihn nur in der
     * Aus-Fassung (MachineBoiler(false), ModBlocks.java:2233) -- ein Ueberbleibsel, das nichts
     * tut und nur herumsteht. Die Vorderseite richtet sich nach der Blickrichtung. */
    public static final DeferredBlock<Block> MACHINE_BOILER_OFF = register("machine_boiler_off", () -> new DecoFacingBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Stufe 5: die Deko der Bauwerke. Der Bildschirm in vier Zustaenden, der Toaster in drei
     * Werkstoffen, dazu das Tonbandgeraet -- im Original Metadaten eines Blocks
     * (ModBlocks.java:1590 ff.), im Port je ein Block. Alle richten sich nach der
     * Blickrichtung. */
    public static final DeferredBlock<Block> DECO_CRT_CLEAN = register("deco_crt_clean", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> DECO_CRT_BROKEN = register("deco_crt_broken", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> DECO_CRT_BLINKING = register("deco_crt_blinking", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> DECO_CRT_BSOD = register("deco_crt_bsod", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> DECO_TOASTER_IRON = register("deco_toaster_iron", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> DECO_TOASTER_STEEL = register("deco_toaster_steel", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> DECO_TOASTER_WOOD = register("deco_toaster_wood", () -> new DecoFacingBlock(deco()));
    public static final DeferredBlock<Block> TAPE_RECORDER = register("tape_recorder", () -> new DecoFacingBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Der Buerorechner. Das Original hat nur eine Ausfuehrung (IBM_300PL) und gibt ihm die
     * Masse (2|0|0)-(14|14|10). */
    public static final DeferredBlock<Block> DECO_COMPUTER = register("deco_computer", () -> new DecoFacingBlock(deco(), Block.box(2, 0, 0, 14, 14, 10)));

    /* Stufe 5: der Aufsatz der Antennenmasten. Werte aus ModBlocks.java:1596. */
    public static final DeferredBlock<Block> POLE_TOP = register("pole_top", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Stufe 5: der Mastaufsatz mit Richtfunkschuessel. Werte aus ModBlocks.java:1597. */
    public static final DeferredBlock<Block> POLE_SATELLITE_RECEIVER = register("pole_satellite_receiver", () -> new DecoPoleSatelliteReceiverBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));

    /* Stufe 5: der Pilz und sein Riesenwuchs. Werte aus ModBlocks.java:1649 bis 1651.
     * setLightLevel(0.5F) des Originals sind sieben Lichtstufen, setLightLevel(1.0F) fuenfzehn. */
    public static final DeferredBlock<Block> MUSH = register("mush", () -> new MushBlock(BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).mapColor(MapColor.COLOR_LIGHT_GREEN).lightLevel(state -> 7).randomTicks().noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> MUSH_BLOCK = register("mush_block", () -> new MushHugeBlock(BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.GRASS).mapColor(MapColor.COLOR_LIGHT_GREEN).lightLevel(state -> 15)));
    public static final DeferredBlock<Block> MUSH_BLOCK_STEM = register("mush_block_stem", () -> new MushHugeBlock(BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.GRASS).mapColor(MapColor.COLOR_LIGHT_GREEN).lightLevel(state -> 15)));
    /* Der Lichtkegel selbst: unsichtbar, nicht anfassbar, nur hell. */
    public static final DeferredBlock<Block> SPOTLIGHT_BEAM = BLOCKS.register("spotlight_beam", () -> new SpotlightBeamBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable().air().lightLevel(state -> 15).pushReaction(PushReaction.DESTROY)));

    /* Stufe 5: das Flutlicht. Es haengt an der Flaeche, auf die man es setzt, zieht Strom
     * aus dem Block dahinter und wirft fuenfzehn Strahlen nach vorn. Werte aus
     * ModBlocks.java:1490. */
    public static final DeferredBlock<Block> FLOODLIGHT = register("floodlight", () -> new FloodlightBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Stufe 5: die Ladestation an der Wand. Werte aus ModBlocks.java:2071. */
    public static final DeferredBlock<Block> CHARGER = register("charger", () -> new ChargerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Stufe 5: die Mikrowelle. Werte aus ModBlocks.java:1825. */
    public static final DeferredBlock<Block> MACHINE_MICROWAVE = register("machine_microwave", () -> new MachineMicrowaveBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    /* Stufe 5: der Funkempfaenger. Werte aus ModBlocks.java:2278. */
    public static final DeferredBlock<Block> RADIOREC = register("radiorec", () -> new RadioRecBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Der verseuchte Rundfunksender. Gehaeuse und Umriss wie beim Empfaenger -- das
     * Original benutzt fuer beide dasselbe Modell. Werte aus ModBlocks.java:1632. */
    public static final DeferredBlock<Block> BROADCASTER_PC = register("broadcaster_pc", () -> new BroadcasterBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.COLOR_PINK).noOcclusion()));
    /* Die Daemonenkern-Lampe. Sie haengt an der angeklickten Flaeche und leuchtet voll.
     * Werte aus ModBlocks.java:1479. */
    public static final DeferredBlock<Block> LAMP_DEMON = register("lamp_demon", () -> new DemonLampBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().lightLevel(state -> 15)));
    /* Stufe 5: der Fernschreiber, zwei Bloecke breit. Werte aus ModBlocks.java:1896. */
    public static final DeferredBlock<Block> RADIO_TELEX = register("radio_telex", () -> new RadioTelexBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion()));
    /* Stufe 5: die Teslaspule. Werte aus ModBlocks.java:2074. */
    public static final DeferredBlock<Block> TESLA = register("tesla", () -> new TeslaBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Stufe 5: der Sockel aus den Weltbauwerken. Werte aus ModBlocks.java:1448; wie im
     * Original in keinem Kreativreiter -- er wird nur von der Weltgenerierung gesetzt. */
    /**
     * Der Sockel, Runde 231. Stein wie im Original (Material.rock); die Werte sind die des
     * Skeletthalters nebenan, denn beide sind dasselbe: ein Gestell, das einen Gegenstand
     * zeigt. noOcclusion, weil er schmaler ist als ein voller Block.
     */
    /**
     * DAS ROTE ZIMMER, Runde 233. Drei Bloecke, die zusammengehoeren: der Ziegel, aus dem
     * das Zimmer besteht, und die beiden Schluessellochbloecke, die es ausheben.
     *
     * SPRENGWIDERSTAND ZEHNTAUSEND wie im Original -- das Zimmer soll durch die Tuer
     * betreten werden, nicht aufgesprengt. Und noLootTable(): der Ziegel gibt sich nicht
     * her (getItemDropped liefert im Original null).
     */
    public static final DeferredBlock<Block> BRICK_RED = register("brick_red", () -> new RedBrickBlock(BlockBehaviour.Properties.of().strength(2.0F, 10_000F).sound(SoundType.STONE).mapColor(MapColor.COLOR_RED).noLootTable()));
    public static final DeferredBlock<Block> STONE_KEYHOLE = register("stone_keyhole", () -> new KeyholeBlock(BlockBehaviour.Properties.of().strength(1.5F, 10_000F).sound(SoundType.STONE).mapColor(MapColor.STONE).noLootTable()));
    public static final DeferredBlock<Block> STONE_KEYHOLE_META = register("stone_keyhole_meta", () -> new RedBrickKeyholeBlock(BlockBehaviour.Properties.of().strength(2.0F, 10_000F).sound(SoundType.STONE).mapColor(MapColor.COLOR_RED).noLootTable()));

    /** Die Tuer des Roten Zimmers -- die vierte neben Metall, Buero und Bunker. */
    public static final DeferredBlock<DoorBlock> DOOR_RED = register("door_red", () -> new DoorBlock(NtmBlockSetTypes.METAL, BlockBehaviour.Properties.of().strength(10.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.COLOR_RED).noOcclusion().pushReaction(PushReaction.DESTROY)));

    /**
     * Der Aufrufer, Runde 234. Er sieht aus wie Stein; was er tut, steht in seiner
     * Blockentitaet. Kein Reiter, kein Rezept -- die Weltgenerierung setzt ihn.
     */
    public static final DeferredBlock<Block> DUNGEON_SPAWNER = register("dungeon_spawner", () -> new DungeonSpawnerBlock(BlockBehaviour.Properties.of().strength(2.0F, 10_000F).sound(SoundType.STONE).mapColor(MapColor.STONE).noLootTable()));
    /**
     * Das Krabbennest aus den Sternenmetall-Ruinen, Runde 236. Im Original heisst seine
     * Klasse BlockCybercrab; Material und Haerte uebernimmt es dort vom Meteoritengestein.
     */
    public static final DeferredBlock<Block> METEOR_SPAWNER = register("meteor_spawner", () -> new MeteorSpawnerBlock(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).noLootTable()));

    public static final DeferredBlock<Block> PEDESTAL = register("pedestal", () -> new PedestalBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).noOcclusion()));
    public static final DeferredBlock<Block> SKELETON_HOLDER = register("skeleton_holder", () -> new SkeletonHolderBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.SOUL_SAND).mapColor(MapColor.COLOR_BROWN).noOcclusion()));
    /* Sein Lichtfleck: unsichtbar, nicht anfassbar, nur hell -- aber mit eigener
     * Blockentitaet, weil er sich Quelle und Strahlnummer merken muss. */
    public static final DeferredBlock<Block> FLOODLIGHT_BEAM = BLOCKS.register("floodlight_beam", () -> new FloodlightBeamBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable().air().lightLevel(state -> 15).pushReaction(PushReaction.DESTROY)));

    /* Stufe 5: totes Gewaechs, fuenf Formen. Im Original ein Block mit Metadaten
     * (ModBlocks.java:1657), im Port fuenf Bloecke. */
    public static final DeferredBlock<Block> PLANT_DEAD_GENERIC = register("plant_dead_generic", () -> new DeadPlantBlock(deadPlant()));
    public static final DeferredBlock<Block> PLANT_DEAD_GRASS = register("plant_dead_grass", () -> new DeadPlantBlock(deadPlant()));
    public static final DeferredBlock<Block> PLANT_DEAD_FLOWER = register("plant_dead_flower", () -> new DeadPlantBlock(deadPlant()));
    public static final DeferredBlock<Block> PLANT_DEAD_BIGFLOWER = register("plant_dead_bigflower", () -> new DeadPlantBlock(deadPlant()));
    public static final DeferredBlock<Block> PLANT_DEAD_FERN = register("plant_dead_fern", () -> new DeadPlantBlock(deadPlant()));

    /* Stufe 5: die Masten der Bauwerke. Werte aus ModBlocks.java:1595. */
    public static final DeferredBlock<Block> STEEL_POLES = register("steel_poles", () -> new SteelPolesBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> STONE_GNEISS = register("stone_gneiss", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE)));
    // Runde 10: Rezeptbaustein beider Schornsteine. Im Original BlockGrate, eine zwei Pixel
    // hohe Platte auf einer von zehn Hoehen im Block.
    public static final DeferredBlock<Block> STEEL_GRATE = register("steel_grate", () -> new GrateBlock(BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    /* Stufe 5: das breite Gitter der Bauwerke. Dieselbe Platte, nur mit dem groberen Bild --
     * im Original derselbe BlockGrate, der seine Oberseitentextur am Block festmacht. */
    public static final DeferredBlock<Block> STEEL_GRATE_WIDE = register("steel_grate_wide", () -> new GrateBlock(BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Stufe 5: Wand und Aussenecke aus Stahl. Werte 1:1 aus ModBlocks.java:1598 f. des
     * Originals; die Formen stehen in DecoBlock und in den Darstellern RenderSteelWall und
     * RenderSteelCorner. */
    public static final DeferredBlock<Block> STEEL_WALL = register("steel_wall", () -> new SteelWallBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> STEEL_CORNER = register("steel_corner", () -> new SteelCornerBlock(BlockBehaviour.Properties.of().strength(15.0F, 15.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));

    /* Stufe 5: die Holzteile der Bauwerke. Werte 1:1 aus ModBlocks.java:1461 f. des Originals. */
    public static final DeferredBlock<Block> WOOD_BARRIER = register("wood_barrier", () -> new WoodBarrierBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> WOOD_STRUCTURE_ROOF = register("wood_structure_roof", () -> new WoodStructureBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion(), WoodStructureBlock.Type.ROOF));
    public static final DeferredBlock<Block> WOOD_STRUCTURE_SCAFFOLD = register("wood_structure_scaffold", () -> new WoodStructureBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion(), WoodStructureBlock.Type.SCAFFOLD));
    public static final DeferredBlock<Block> WOOD_STRUCTURE_CEILING = register("wood_structure_ceiling", () -> new WoodStructureBlock(BlockBehaviour.Properties.of().strength(5.0F, 15.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion(), WoodStructureBlock.Type.CEILING));

    // Bricks
    public static final DeferredBlock<Block> BRICK_CONCRETE =         registerBlastInfoBlock("brick_concrete",         () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 160.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> BRICK_CONCRETE_MOSSY =   registerBlastInfoBlock("brick_concrete_mossy",   () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 160.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> BRICK_CONCRETE_CRACKED = registerBlastInfoBlock("brick_concrete_cracked", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 60.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> BRICK_CONCRETE_BROKEN =  registerBlastInfoBlock("brick_concrete_broken",  () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 45.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> BRICK_CONCRETE_MARKED =  registerBlastInfoBlock("brick_concrete_marked",  () -> new WritingBlock(BlockBehaviour.Properties.of().strength(15.0F, 160.0F ).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> BRICK_OBSIDIAN =         registerBlastInfoBlock("brick_obsidian",         () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 120.0F).mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)));
    public static final DeferredBlock<Block> BRICK_LIGHT =            registerBlastInfoBlock("brick_light",            () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 20.0F).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> BRICK_ASBESTOS =         register(              "brick_asbestos",         () -> new OutgasBlock(true, true, BlockBehaviour.Properties.of().strength(5.0F, 1000.0F).mapColor(MapColor.SNOW)));
    /** Erstarrte Coriumquelle: dicht, bleischwer, hochradioaktiv. */
    public static final DeferredBlock<Block> BLOCK_CORIUM =           register(              "block_corium",           () -> new HazardBlock(BlockBehaviour.Properties.of().strength(100.0F, 6000.0F).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops()));
    /** Erstarrtes ausgelaufenes Corium: poroes, gast Radon aus. */
    public static final DeferredBlock<Block> BLOCK_CORIUM_COBBLE =    register(              "block_corium_cobble",    () -> new OutgasBlock(true, true, true, BlockBehaviour.Properties.of().strength(100.0F, 6000.0F).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops()));
    /* Der Uraltschrott aus dem Gruftbauwerk. Dieselben drei Schalter wie das Koriumgestein,
     * aber er atmet Gruftradon aus -- und wer ihn zerschlaegt, steht mitten in einer Wolke. */
    public static final DeferredBlock<Block> ANCIENT_SCRAP =          register(              "ancient_scrap",          () -> new OutgasBlock(true, true, true, BlockBehaviour.Properties.of().strength(100.0F, 6000.0F).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BRICK_FIRE =             registerBlastInfoBlock("brick_fire",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 35.0F).mapColor(MapColor.FIRE)));
    public static final DeferredBlock<Block> CONCRETE =               registerBlastInfoBlock("concrete",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 84.0F).mapColor(MapColor.SNOW)));
    public static final DeferredBlock<Block> CONCRETE_SMOOTH =        registerBlastInfoBlock("concrete_smooth",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 84.0F).mapColor(MapColor.SNOW)));
    public static final DeferredBlock<Block> CONCRETE_ASBESTOS =      registerBlastInfoBlock("concrete_asbestos",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 900.0F).mapColor(MapColor.SNOW)));
    public static final DeferredBlock<Block> DUCRETE =                registerBlastInfoBlock("ducrete",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 300.0F).mapColor(MapColor.DEEPSLATE)));
    public static final DeferredBlock<Block> DUCRETE_SMOOTH =         registerBlastInfoBlock("ducrete_smooth",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 300.0F).mapColor(MapColor.DEEPSLATE)));
    public static final DeferredBlock<Block> DUCRETE_REINFORCED =     registerBlastInfoBlock("ducrete_reinforced",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 600.0F).mapColor(MapColor.DEEPSLATE)));
    public static final DeferredBlock<Block> DUCRETE_BRICK =          registerBlastInfoBlock("ducrete_brick",             () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 450.0F).mapColor(MapColor.DEEPSLATE)));

    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_SLAB =         register("brick_concrete_slab",         () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_MOSSY_SLAB =   register("brick_concrete_mossy_slab",   () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_CRACKED_SLAB = register("brick_concrete_cracked_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_BROKEN_SLAB =  register("brick_concrete_broken_slab",  () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));

    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_STAIRS =         register("brick_concrete_stairs",         () -> new StairBlock(BRICK_CONCRETE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_MOSSY_STAIRS =   register("brick_concrete_mossy_stairs",   () -> new StairBlock(BRICK_CONCRETE_MOSSY.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_CRACKED_STAIRS = register("brick_concrete_cracked_stairs", () -> new StairBlock(BRICK_CONCRETE_CRACKED.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_BROKEN_STAIRS =  register("brick_concrete_broken_stairs",  () -> new StairBlock(BRICK_CONCRETE_BROKEN.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BRICK_CONCRETE.get())));

    /*
     * Runde 89 -- erster Stapel der Bloecke, die den 79 Bauwerken des Originals fehlen. Werte
     * 1:1 aus ModBlocks.java:1464-1546 des Originals.
     *
     * DIE PANZERFAMILIE ist das Baumaterial der Bunker und Silos: sehr hart, sehr sprengfest,
     * ohne jedes Verhalten. Die Panzerlampe ist der einzige Block mit Regung -- sie leuchtet,
     * wenn Redstone anliegt.
     */
    public static final DeferredBlock<Block> REINFORCED_STONE = registerBlastInfoBlock("reinforced_stone", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 100.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> REINFORCED_BRICK = registerBlastInfoBlock("reinforced_brick", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 300.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> REINFORCED_SAND =  registerBlastInfoBlock("reinforced_sand",  () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 40.0F).mapColor(MapColor.SAND)));
    /** Leuchtet dauerhaft aus sich selbst -- das Deckenlicht der Anlagen. */
    public static final DeferredBlock<Block> REINFORCED_LIGHT = registerBlastInfoBlock("reinforced_light", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 80.0F).lightLevel(state -> 15).mapColor(MapColor.STONE)));
    /**
     * ZUSAMMENGEFASST: das Original hat zwei Bloecke, reinforced_lamp_off und _on, und tauscht
     * sie beim Schalten aus. In 1.21 ist das ein Block mit der Eigenschaft LIT -- der Umsetzer
     * bildet den einen Namen auf lit=false ab, den anderen auf lit=true.
     */
    public static final DeferredBlock<Block> REINFORCED_LAMP = registerBlastInfoBlock("reinforced_lamp", () -> new RedstoneLampBlock(BlockBehaviour.Properties.of().strength(15.0F, 80.0F).lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> REINFORCED_GLASS = register("reinforced_glass", () -> new Block(BlockBehaviour.Properties.of().noOcclusion().strength(2.0F, 25.0F).sound(SoundType.GLASS).mapColor(MapColor.NONE)));
    public static final DeferredBlock<IronBarsBlock> REINFORCED_GLASS_PANE = register("reinforced_glass_pane", () -> new IronBarsBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.0F, 25.0F).sound(SoundType.GLASS).mapColor(MapColor.NONE)));
    public static final DeferredBlock<Block> BRICK_COMPOUND = registerBlastInfoBlock("brick_compound", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 200.0F).mapColor(MapColor.STONE)));

    /*
     * Die Stufen. Im Original ist das EIN Block je Familie, dessen Metadaten-Zahl den Werkstoff
     * auswaehlt (BlockMultiSlab); in 1.21 wird daraus je Werkstoff ein eigener SlabBlock.
     * Angelegt sind nur die Werkstoffe, die in den 79 Bauwerken wirklich vorkommen.
     */
    public static final DeferredBlock<SlabBlock> CONCRETE_SLAB =          register("concrete_slab",          () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CONCRETE.get())));
    public static final DeferredBlock<SlabBlock> CONCRETE_SMOOTH_SLAB =   register("concrete_smooth_slab",   () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CONCRETE_SMOOTH.get())));
    public static final DeferredBlock<SlabBlock> CONCRETE_ASBESTOS_SLAB = register("concrete_asbestos_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CONCRETE_ASBESTOS.get())));
    public static final DeferredBlock<SlabBlock> REINFORCED_STONE_SLAB =  register("reinforced_stone_slab",  () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(REINFORCED_STONE.get())));
    public static final DeferredBlock<SlabBlock> REINFORCED_BRICK_SLAB =  register("reinforced_brick_slab",  () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(REINFORCED_BRICK.get())));
    public static final DeferredBlock<SlabBlock> BRICK_LIGHT_SLAB =       register("brick_light_slab",       () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BRICK_LIGHT.get())));
    public static final DeferredBlock<SlabBlock> BRICK_COMPOUND_SLAB =    register("brick_compound_slab",    () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BRICK_COMPOUND.get())));

    /* Die Treppen. Sie leihen sich die Textur ihres Grundblocks, so wie im Original. */
    public static final DeferredBlock<StairBlock> CONCRETE_STAIRS =          register("concrete_stairs",          () -> new StairBlock(CONCRETE.get().defaultBlockState(),          BlockBehaviour.Properties.ofFullCopy(CONCRETE.get())));
    public static final DeferredBlock<StairBlock> CONCRETE_SMOOTH_STAIRS =   register("concrete_smooth_stairs",   () -> new StairBlock(CONCRETE_SMOOTH.get().defaultBlockState(),   BlockBehaviour.Properties.ofFullCopy(CONCRETE_SMOOTH.get())));
    public static final DeferredBlock<StairBlock> CONCRETE_ASBESTOS_STAIRS = register("concrete_asbestos_stairs", () -> new StairBlock(CONCRETE_ASBESTOS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(CONCRETE_ASBESTOS.get())));
    public static final DeferredBlock<StairBlock> REINFORCED_STONE_STAIRS =  register("reinforced_stone_stairs",  () -> new StairBlock(REINFORCED_STONE.get().defaultBlockState(),  BlockBehaviour.Properties.ofFullCopy(REINFORCED_STONE.get())));
    public static final DeferredBlock<StairBlock> REINFORCED_BRICK_STAIRS =  register("reinforced_brick_stairs",  () -> new StairBlock(REINFORCED_BRICK.get().defaultBlockState(),  BlockBehaviour.Properties.ofFullCopy(REINFORCED_BRICK.get())));
    public static final DeferredBlock<StairBlock> BRICK_LIGHT_STAIRS =       register("brick_light_stairs",       () -> new StairBlock(BRICK_LIGHT.get().defaultBlockState(),       BlockBehaviour.Properties.ofFullCopy(BRICK_LIGHT.get())));
    public static final DeferredBlock<StairBlock> BRICK_OBSIDIAN_STAIRS =    register("brick_obsidian_stairs",    () -> new StairBlock(BRICK_OBSIDIAN.get().defaultBlockState(),    BlockBehaviour.Properties.ofFullCopy(BRICK_OBSIDIAN.get())));
    public static final DeferredBlock<StairBlock> BRICK_COMPOUND_STAIRS =    register("brick_compound_stairs",    () -> new StairBlock(BRICK_COMPOUND.get().defaultBlockState(),    BlockBehaviour.Properties.ofFullCopy(BRICK_COMPOUND.get())));

    // Other defensive stuff
    public static final DeferredBlock<Block> BARBED_WIRE = registerNew("barbed_wire", () -> new BarbedWireBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).noCollission()));
    public static final DeferredBlock<Block> SPIKES = register("spikes", () -> new SpikesBlock(BlockBehaviour.Properties.of().strength(2.5F, 5.0F).noCollission()));

    // Waste
    public static final DeferredBlock<Block> WASTE_EARTH =         register("waste_earth",         () -> new Block(               BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRASS).mapColor(MapColor.DIRT)));
    public static final DeferredBlock<Block> WASTE_MYCELIUM =      register("waste_mycelium",      () -> new WasteMyceliumBlock(  BlockBehaviour.Properties.of().strength(0.6F).lightLevel(value -> 10).sound(SoundType.GRASS).mapColor(MapColor.COLOR_LIGHT_GREEN)));
    public static final DeferredBlock<Block> WASTE_TRINITITE =     register("waste_trinitite",     () -> new WasteTrinititeBlock( BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.SAND).mapColor(MapColor.SAND).instrument(NoteBlockInstrument.SNARE)));
    public static final DeferredBlock<Block> WASTE_TRINITITE_RED = register("waste_trinitite_red", () -> new WasteTrinititeBlock( BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.SAND).mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.SNARE)));
    public static final DeferredBlock<RotatedPillarBlock> WASTE_LOG = register("waste_log",    () -> new RotatedPillarBlock( BlockBehaviour.Properties.of().strength(5.0F, 2.5F).sound(SoundType.WOOD).mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> WASTE_LEAVES =           register("waste_leaves", () -> new WasteLeavesBlock(   BlockBehaviour.Properties.of().strength(0.2F).randomTicks().mapColor(MapColor.COLOR_BROWN).sound(SoundType.GRASS).noOcclusion().isValidSpawn(Blocks::never).isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never).ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor(NtmBlocks::never)));
    public static final DeferredBlock<Block> WASTE_PLANKS =           register("waste_planks", () -> new Block(              BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.WOOD).mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> FROZEN_DIRT =             register("frozen_dirt",   () -> new FrozenBlock(        BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.GLASS).mapColor(DyeColor.LIGHT_BLUE)));
    public static final DeferredBlock<Block> FROZEN_GRASS =            register("frozen_grass",  () -> new FrozenBlock(        BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.GLASS).mapColor(DyeColor.WHITE)));
    public static final DeferredBlock<RotatedPillarBlock> FROZEN_LOG = register("frozen_log",    () -> new RotatedPillarBlock( BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.GLASS).mapColor(DyeColor.LIGHT_BLUE)));
    public static final DeferredBlock<Block> FROZEN_PLANKS =           register("frozen_planks", () -> new Block(              BlockBehaviour.Properties.of().strength(0.5F, 2.5F).sound(SoundType.GLASS).mapColor(DyeColor.LIGHT_BLUE)));
    /**
     * Die beiden Loeschschichten des Feuerloeschers. Beide wachsen von Schuss zu Schuss um
     * eine Lage; auf die siebte folgt der volle Block -- aus dem Schaum BLOCK_FOAM, aus dem
     * Sand SAND_BORON. Das uebernimmt nicht der Block, sondern die Trefferregel in
     * XFactoryTool: die Schicht selbst weiss nichts vom Loeschen.
     */
    public static final DeferredBlock<Block> FOAM_LAYER = register("foam_layer", () -> new LayeringBlock(BlockBehaviour.Properties.of().strength(0.1F).mapColor(MapColor.SNOW).sound(SoundType.SNOW).noOcclusion().isValidSpawn(Blocks::never).isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never).pushReaction(PushReaction.DESTROY).isRedstoneConductor(NtmBlocks::never)));
    public static final DeferredBlock<Block> SAND_BORON_LAYER = register("sand_boron_layer", () -> new LayeringBlock(BlockBehaviour.Properties.of().strength(0.1F).mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.SAND).noOcclusion().isValidSpawn(Blocks::never).isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never).pushReaction(PushReaction.DESTROY).isRedstoneConductor(NtmBlocks::never)));
    public static final DeferredBlock<Block> LEAVES_LAYER = register("leaves_layer", () -> new LayeringBlock(BlockBehaviour.Properties.of().strength(0.2F).randomTicks().mapColor(MapColor.COLOR_BROWN).sound(SoundType.GRASS).noOcclusion().isValidSpawn(Blocks::never).isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never).ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor(NtmBlocks::never)));
    public static final DeferredBlock<Block> FALLOUT = register("fallout", () -> new FalloutBlock(BlockBehaviour.Properties.of().replaceable().strength(0.1F).sound(SoundType.GRAVEL).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> SELLAFIELD_SLAKED = register("sellafield_slaked", () -> new SellafieldSlakedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).isValidSpawn(Blocks::never).requiresCorrectToolForDrops().strength(3.0F, 10.0F)));
    public static final DeferredBlock<Block> ORE_SELLAFIELD_DIAMOND = register("ore_sellafield_diamond", () -> new SellafieldSlakedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).isValidSpawn(Blocks::never).requiresCorrectToolForDrops().strength(3.0F, 10.0F)));
    public static final DeferredBlock<Block> ORE_SELLAFIELD_EMERALD = register("ore_sellafield_emerald", () -> new SellafieldSlakedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).isValidSpawn(Blocks::never).requiresCorrectToolForDrops().strength(3.0F, 10.0F)));
    public static final DeferredBlock<Block> SELLAFIELD_BEDROCK = register("sellafield_bedrock", () -> new SellafieldSlakedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).isValidSpawn(Blocks::never).noLootTable().strength(-1.0F, 6000000.0F).instrument(NoteBlockInstrument.BASEDRUM)));

    // Nukes
    public static final DeferredBlock<Block> NUKE_GADGET =     register("nuke_gadget",     () -> new NukeGadgetBlock(    BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_LITTLE_BOY = register("nuke_little_boy", () -> new NukeLittleBoyBlock( BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_FAT_MAN =    register("nuke_fat_man",    () -> new NukeFatManBlock(    BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_IVY_MIKE =   register("nuke_ivy_mike",   () -> new NukeIvyMikeBlock(   BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_TSAR_BOMBA = register("nuke_tsar_bomba", () -> new NukeTsarBombaBlock( BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_PROTOTYPE =  register("nuke_prototype",  () -> new NukePrototypeBlock( BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_FLEIJA =     register("nuke_fleija",     () -> new NukeFleijaBlock(    BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_SOLINIUM =   register("nuke_solinium",   () -> new NukeSoliniumBlock(  BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_N2 =         register("nuke_n2",         () -> new NukeN2Block(        BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> NUKE_FSTBMB =     register("nuke_fstbmb",     () -> new NukeBalefireBlock(  BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 200.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    // Generic Bombs
    // todo add bomb_multi
    public static final DeferredBlock<Block> CRASHED_BOMB = registerNew("crashed_bomb", () -> new CrashedBombBlock(BlockBehaviour.Properties.of().noLootTable().noOcclusion().strength(6000.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DYNAMITE = register("dynamite", () -> new DynamiteBlock( BlockBehaviour.Properties.of().instabreak().ignitedByLava().isRedstoneConductor(NtmBlocks::never).sound(SoundType.GRASS).mapColor(MapColor.FIRE)));
    public static final DeferredBlock<Block> TNT =      register("tnt",      () -> new TNTBlock(      BlockBehaviour.Properties.of().instabreak().ignitedByLava().isRedstoneConductor(NtmBlocks::never).sound(SoundType.GRASS).mapColor(MapColor.FIRE)));
    public static final DeferredBlock<Block> SEMTEX =   register("semtex",   () -> new SemtexBlock(   BlockBehaviour.Properties.of().instabreak().ignitedByLava().isRedstoneConductor(NtmBlocks::never).sound(SoundType.GRASS).mapColor(MapColor.FIRE)));
    public static final DeferredBlock<Block> C4 =       register("c4",       () -> new C4Block(       BlockBehaviour.Properties.of().instabreak().ignitedByLava().isRedstoneConductor(NtmBlocks::never).sound(SoundType.GRASS).mapColor(MapColor.FIRE)));
    public static final DeferredBlock<Block> FISSURE_BOMB = register("fissure_bomb", () -> new FissureBombBlock(BlockBehaviour.Properties.of().instabreak().ignitedByLava().isRedstoneConductor(NtmBlocks::never).sound(SoundType.GRASS).mapColor(MapColor.FIRE)));

    // Mines
    public static final DeferredBlock<Block> MINE_AP =    register("mine_ap",    () -> new LandmineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 0.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), 1.5D, 1D));
    public static final DeferredBlock<Block> MINE_HE =    register("mine_he",    () -> new LandmineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 0.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), 2D, 5D));
    public static final DeferredBlock<Block> MINE_SHRAP = register("mine_shrap", () -> new LandmineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 0.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), 1.5D, 1D));
    public static final DeferredBlock<Block> MINE_FAT =   register("mine_fat",   () -> new LandmineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 0.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), 2.5D, 1D));
    public static final DeferredBlock<Block> MINE_NAVAL = register("mine_naval", () -> new LandmineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 0.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), 2.5D, 1D));

    // Anvil construction placeholders
    public static final DeferredBlock<Block> DECO_ALUMINIUM = register("deco_aluminium", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_BERYLLIUM = register("deco_beryllium", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_LEAD = register("deco_lead", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_RED_COPPER = register("deco_red_copper", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_STEEL = register("deco_steel", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_RUSTY_STEEL = register("deco_rusty_steel", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_TITANIUM = register("deco_titanium", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_TUNGSTEN = register("deco_tungsten", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_ASBESTOS = register("deco_asbestos", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Die beiden RBMK-Deko-Bloecke. Haerte und Widerstand wie im Original: 5.0 / 100.0. */
    public static final DeferredBlock<Block> DECO_RBMK = register("deco_rbmk", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> DECO_RBMK_SMOOTH = register("deco_rbmk_smooth", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /*
     * Runde 90: die Rohrfamilie des Originals -- vier Formen in je sechs Farben.
     *
     * Die Formen heissen im Original nach ihrem Renderer: das nackte Rohr, das Rohr mit
     * Bund an beiden Enden (rim), das Vierlingsrohr (quad) und das eingehauste Rohr
     * (framed), bei dem vier Eckpfosten und vier Gitterwaende um den Bund stehen.
     *
     * Alle vierundzwanzig sind reine Zier: sie tun nichts, sie reagieren auf nichts. Die
     * Blockklasse ist deshalb die von Minecraft mitgebrachte Saeule -- beim Setzen richtet
     * sie sich nach der angeklickten Flaeche aus, genau wie im Original. Haerte und
     * Widerstand wie dort: 2.0 / 5.0.
     *
     * Die Umrissform bleibt der volle Wuerfel. Das ist nicht Nachlaessigkeit, sondern
     * Treue: BlockPipe setzt im Original keine eigenen Blockgrenzen, man steht also auch
     * dort auf einem vollen Wuerfel, obwohl nur ein duennes Rohr zu sehen ist.
     */
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE =                      registerPipe("deco_pipe");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RUSTED =               registerPipe("deco_pipe_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_GREEN =                registerPipe("deco_pipe_green");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_GREEN_RUSTED =         registerPipe("deco_pipe_green_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RED =                  registerPipe("deco_pipe_red");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_MARKED =               registerPipe("deco_pipe_marked");

    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RIM =                  registerPipe("deco_pipe_rim");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RIM_RUSTED =           registerPipe("deco_pipe_rim_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RIM_GREEN =            registerPipe("deco_pipe_rim_green");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RIM_GREEN_RUSTED =     registerPipe("deco_pipe_rim_green_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RIM_RED =              registerPipe("deco_pipe_rim_red");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_RIM_MARKED =           registerPipe("deco_pipe_rim_marked");

    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_QUAD =                 registerPipe("deco_pipe_quad");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_QUAD_RUSTED =          registerPipe("deco_pipe_quad_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_QUAD_GREEN =           registerPipe("deco_pipe_quad_green");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_QUAD_GREEN_RUSTED =    registerPipe("deco_pipe_quad_green_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_QUAD_RED =             registerPipe("deco_pipe_quad_red");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_QUAD_MARKED =          registerPipe("deco_pipe_quad_marked");

    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_FRAMED =               registerPipe("deco_pipe_framed");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_FRAMED_RUSTED =        registerPipe("deco_pipe_framed_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_FRAMED_GREEN =         registerPipe("deco_pipe_framed_green");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_FRAMED_GREEN_RUSTED =  registerPipe("deco_pipe_framed_green_rusted");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_FRAMED_RED =           registerPipe("deco_pipe_framed_red");
    public static final DeferredBlock<RotatedPillarBlock> DECO_PIPE_FRAMED_MARKED =        registerPipe("deco_pipe_framed_marked");

    /*
     * Runde 91: der Meteoritenbau, die Laborfliesen und der Leuchtstein.
     *
     * Der METEORITENBAU ist das Baumaterial der Sternenmetall-Ruinen: aus Meteoritengestein
     * geschlagen, sehr hart und sehr sprengfest (15.0 / 360.0 wie im Original).
     */
    public static final DeferredBlock<Block> METEOR_POLISHED = registerBlastInfoBlock("meteor_polished", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> METEOR_BRICK = registerBlastInfoBlock("meteor_brick", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> METEOR_BRICK_CHISELED = registerBlastInfoBlock("meteor_brick_chiseled", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));
    /*
     * Runde 250: die beiden VERWITTERTEN ZIEGEL. Runde 91 hat sie uebersehen, weil sie im
     * Kreativreiter neben dem glatten Ziegel kaum auffallen -- gebraucht werden sie aber an
     * jeder Wand des Meteoritenverlieses: dessen Blockwaehler ersetzt jeden gesetzten
     * meteor_brick zu drei Zehnteln durch den bemoosten und zu drei Zehnteln durch den
     * rissigen. Ohne sie waere das ganze Verlies gleichfoermig glatt.
     */
    public static final DeferredBlock<Block> METEOR_BRICK_MOSSY = registerBlastInfoBlock("meteor_brick_mossy", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> METEOR_BRICK_CRACKED = registerBlastInfoBlock("meteor_brick_cracked", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<RotatedPillarBlock> METEOR_PILLAR = registerBlastInfoBlock("meteor_pillar", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));
    /* Der Statikgenerator steht immer aufrecht; er ist keine drehbare Saeule, nur oben anders. */
    public static final DeferredBlock<Block> METEOR_BATTERY = registerBlastInfoBlock("meteor_battery", () -> new Block(BlockBehaviour.Properties.of().strength(15.0F, 360.0F).mapColor(MapColor.STONE)));

    /*
     * Die LABORFLIESEN gasen Asbest aus, wie der Asbestziegel. Beim Zerschlagen setzen alle drei
     * eine Wolke frei; nur die zerbrochene tut das ausserdem von selbst, wenn man ueber sie laeuft.
     */
    public static final DeferredBlock<Block> TILE_LAB = register("tile_lab", () -> new OutgasBlock(false, true, BlockBehaviour.Properties.of().strength(1.0F, 20.0F).sound(SoundType.GLASS).mapColor(MapColor.SNOW)));
    public static final DeferredBlock<Block> TILE_LAB_CRACKED = register("tile_lab_cracked", () -> new OutgasBlock(false, true, BlockBehaviour.Properties.of().strength(1.0F, 20.0F).sound(SoundType.GLASS).mapColor(MapColor.SNOW)));
    public static final DeferredBlock<Block> TILE_LAB_BROKEN = register("tile_lab_broken", () -> new OutgasBlock(true, true, BlockBehaviour.Properties.of().strength(1.0F, 20.0F).sound(SoundType.GLASS).mapColor(MapColor.SNOW)));

    /*
     * Der LEUCHTSTEIN ist im Original EIN Block, dessen Metadaten-Zahl die Spielart auswaehlt
     * (BlockEnumMulti mit LightstoneType); in 1.21 wird daraus je Spielart ein eigener Block.
     * Alle fuenf kommen in den 79 Bauwerken vor, also stehen hier auch alle fuenf. Die beiden
     * gemeisselten haben oben und unten ein eigenes Bild, die drei anderen nicht.
     *
     * Er leuchtet uebrigens nicht, trotz seines Namens -- das Original setzt keine Lichtstaerke.
     */
    public static final DeferredBlock<Block> LIGHTSTONE = register("lightstone", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 15.0F).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> LIGHTSTONE_TILE = register("lightstone_tile", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 15.0F).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> LIGHTSTONE_BRICKS = register("lightstone_bricks", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 15.0F).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> LIGHTSTONE_BRICKS_CHISELED = register("lightstone_bricks_chiseled", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 15.0F).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> LIGHTSTONE_CHISELED = register("lightstone_chiseled", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 15.0F).mapColor(MapColor.SAND)));
    public static final DeferredBlock<StairBlock> LIGHTSTONE_BRICKS_STAIRS = register("lightstone_bricks_stairs", () -> new StairBlock(LIGHTSTONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(LIGHTSTONE_BRICKS.get())));

    /*
     * Runde 92: der Rest der Betonfamilie.
     *
     * DER FARBBETON ist im Original EIN Block, dessen Metadaten-Zahl die Farbe waehlt; in 1.21
     * wird daraus je Farbe ein eigener Block. Die Bauwerke benutzen elf der sechzehn Farben --
     * angelegt sind alle sechzehn, weil eine halbe Farbfamilie seltsamer waere als eine ganze.
     * Haerte und Widerstand wie im Original: 15.0 / 140.0.
     */
    public static final DeferredBlock<Block> CONCRETE_WHITE = registerBlastInfoBlock("concrete_white", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.WHITE)));
    public static final DeferredBlock<Block> CONCRETE_ORANGE = registerBlastInfoBlock("concrete_orange", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.ORANGE)));
    public static final DeferredBlock<Block> CONCRETE_MAGENTA = registerBlastInfoBlock("concrete_magenta", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.MAGENTA)));
    public static final DeferredBlock<Block> CONCRETE_LIGHT_BLUE = registerBlastInfoBlock("concrete_light_blue", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.LIGHT_BLUE)));
    public static final DeferredBlock<Block> CONCRETE_YELLOW = registerBlastInfoBlock("concrete_yellow", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.YELLOW)));
    public static final DeferredBlock<Block> CONCRETE_LIME = registerBlastInfoBlock("concrete_lime", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.LIME)));
    public static final DeferredBlock<Block> CONCRETE_PINK = registerBlastInfoBlock("concrete_pink", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.PINK)));
    public static final DeferredBlock<Block> CONCRETE_GRAY = registerBlastInfoBlock("concrete_gray", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.GRAY)));
    public static final DeferredBlock<Block> CONCRETE_LIGHT_GRAY = registerBlastInfoBlock("concrete_light_gray", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.LIGHT_GRAY)));
    public static final DeferredBlock<Block> CONCRETE_CYAN = registerBlastInfoBlock("concrete_cyan", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.CYAN)));
    public static final DeferredBlock<Block> CONCRETE_PURPLE = registerBlastInfoBlock("concrete_purple", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.PURPLE)));
    public static final DeferredBlock<Block> CONCRETE_BLUE = registerBlastInfoBlock("concrete_blue", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.BLUE)));
    public static final DeferredBlock<Block> CONCRETE_BROWN = registerBlastInfoBlock("concrete_brown", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.BROWN)));
    public static final DeferredBlock<Block> CONCRETE_GREEN = registerBlastInfoBlock("concrete_green", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.GREEN)));
    public static final DeferredBlock<Block> CONCRETE_RED = registerBlastInfoBlock("concrete_red", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.RED)));
    public static final DeferredBlock<Block> CONCRETE_BLACK = registerBlastInfoBlock("concrete_black", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(DyeColor.BLACK)));

    /*
     * DER SONDERBETON (concrete_colored_ext) ist die zweite Farbreihe des Originals: acht
     * Toene, die es im Farbeimer nicht gibt. Zwei seiner Namen -- Purpur und Rosa -- gibt es in
     * der ersten Reihe schon; deshalb tragen alle acht das Kuerzel "ext", nach dem Blocknamen
     * des Originals. Der gestreifte Maschinenbeton zeigt oben und unten den ungestreiften.
     */
    public static final DeferredBlock<Block> CONCRETE_EXT_MACHINE = registerBlastInfoBlock("concrete_ext_machine", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_MACHINE_STRIPE = registerBlastInfoBlock("concrete_ext_machine_stripe", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_INDIGO = registerBlastInfoBlock("concrete_ext_indigo", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_PURPLE = registerBlastInfoBlock("concrete_ext_purple", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_PINK = registerBlastInfoBlock("concrete_ext_pink", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_HAZARD = registerBlastInfoBlock("concrete_ext_hazard", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_SAND = registerBlastInfoBlock("concrete_ext_sand", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_EXT_BRONZE = registerBlastInfoBlock("concrete_ext_bronze", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(15.0F, 140.0F).mapColor(MapColor.STONE)));

    /* Die Betonsaeule, der Stahlbeton und der Hochleistungsbeton, der von selbst zerfaellt. */
    public static final DeferredBlock<RotatedPillarBlock> CONCRETE_PILLAR = registerBlastInfoBlock("concrete_pillar", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(15.0F, 180.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_REBAR = registerBlastInfoBlock("concrete_rebar", () -> new NoSpawnBlock(BlockBehaviour.Properties.of().strength(50.0F, 240.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_SUPER = registerBlastInfoBlock("concrete_super", () -> new UberConcreteBlock(BlockBehaviour.Properties.of().strength(150.0F, 1000.0F).mapColor(MapColor.STONE)));
    public static final DeferredBlock<Block> CONCRETE_SUPER_BROKEN = registerBlastInfoBlock("concrete_super_broken", () -> new SimpleFallingBlock(BlockBehaviour.Properties.of().strength(10.0F, 20.0F).mapColor(MapColor.STONE)));

    // Block Bombs
    public static final DeferredBlock<Block> DET_CHARGE =  register("det_charge",  () -> new ExplosiveChargeBlock(BlockBehaviour.Properties.of().strength(0.1F, 0.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> DET_CORD =    register("det_cord",    () -> new DetCordBlock(BlockBehaviour.Properties.of().strength(0.1F, 0.0F).noOcclusion().isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> DET_NUKE =    register("det_nuke",    () -> new ExplosiveChargeBlock(BlockBehaviour.Properties.of().strength(0.1F, 0.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> DET_MINER =   register("det_miner",   () -> new ExplosiveChargeBlock(BlockBehaviour.Properties.of().strength(0.1F, 0.0F).sound(SoundType.METAL)));
    /* Die vier Haftladungen mit Schaltuhr, Runde 269. Sie kleben an der angeklickten Flaeche,
     * zaehlen herunter und lassen sich nur mit dem Entschaerfer wieder abnehmen. Im Original
     * steht ihre Sprengfestigkeit ausdruecklich auf 1.0F -- sie sollen von einer fremden
     * Explosion nicht einfach weggeraeumt werden, sondern selbst hochgehen. */
    public static final DeferredBlock<Block> CHARGE_DYNAMITE = register("charge_dynamite", () -> new ChargeDynamiteBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 1.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> CHARGE_MINER =    register("charge_miner",    () -> new ChargeMinerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 1.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> CHARGE_C4 =       register("charge_c4",       () -> new ChargeC4Block(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 1.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> CHARGE_SEMTEX =   register("charge_semtex",   () -> new ChargeSemtexBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 1.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> BARREL_RED =   register("barrel_red",   () -> new RedBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 2.5F).sound(SoundType.METAL), true));
    /* Stufe 5: die beiden Strahlenfaesser. Das gelbe strahlt zehnmal so stark wie das
     * verglaste und zuendet als einziges mit, wenn nebenan etwas hochgeht. */
    public static final DeferredBlock<Block> YELLOW_BARREL = register("yellow_barrel", () -> new YellowBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().randomTicks().strength(0.5F, 2.5F).sound(SoundType.METAL), 5.0F, true));
    public static final DeferredBlock<Block> VITRIFIED_BARREL = register("vitrified_barrel", () -> new YellowBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().randomTicks().strength(0.5F, 2.5F).sound(SoundType.METAL), 0.5F, false));
    public static final DeferredBlock<Block> BARREL_PINK =  register("barrel_pink",  () -> new RedBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 2.5F).sound(SoundType.METAL), true));
    public static final DeferredBlock<Block> BARREL_LOX =   register("barrel_lox",   () -> new RedBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 2.5F).sound(SoundType.METAL), false));
    public static final DeferredBlock<Block> BARREL_TAINT = register("barrel_taint", () -> new RedBarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.1F, 2.5F).sound(SoundType.METAL), false));

    // Geiger Counter
    public static final DeferredBlock<Block> GEIGER = register("geiger", () -> new GeigerCounterBlock(BlockBehaviour.Properties.of().strength(15.0F, 0.25F).sound(SoundType.METAL).mapColor(MapColor.COLOR_YELLOW)));

    // Machines
    public static final DeferredBlock<Block> ANVIL = registerNew("anvil", () -> new NTMAnvilBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.ANVIL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<Block> PRESS_PREHEATER = register("press_preheater", () -> new Block(BlockBehaviour.Properties.of().strength(0.6F, 10.0F).mapColor(MapColor.FIRE)));
    public static final DeferredBlock<Block> MACHINE_PRESS = register("machine_press", () -> new MachinePressBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    public static final DeferredBlock<Block> HEAT_BOILER = register("heat_boiler", () -> new MachineHeatBoiler(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_INDUSTRIAL_BOILER = register("machine_industrial_boiler", () -> new MachineIndustrialBoiler(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> HEATER_FIREBOX = register("heater_firebox", () -> new HeaterFireboxBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> HEATER_OVEN = register("heater_oven", () -> new HeaterOvenBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> HEATER_OILBURNER = register("heater_oilburner", () -> new HeaterOilburnerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> HEATER_ELECTRIC = register("heater_electric", () -> new HeaterElectricBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> HEATER_HEATEX = register("heater_heatex", () -> new HeaterHeatexBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_TURBINE = register("machine_turbine", () -> new MachineTurbineBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_TURBINEGAS = register("machine_turbinegas", () -> new MachineTurbineGasBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_TURBOFAN = register("machine_turbofan", () -> new MachineTurbofanBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_CRYSTALLIZER = register("machine_crystallizer", () -> new MachineCrystallizerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    // Runde 10: Aschegrube und die beiden Schornsteine. Zusammen schliessen sie die
    // Verschmutzungskette: die Rauchtanks der Maschinen hatten bisher keinen Abnehmer.
    public static final DeferredBlock<Block> MACHINE_ASHPIT = register("machine_ashpit", () -> new MachineAshpitBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> CHIMNEY_BRICK = register("chimney_brick", () -> new MachineChimneyBrickBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.STONE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> CHIMNEY_INDUSTRIAL = register("chimney_industrial", () -> new MachineChimneyIndustrialBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));

    // Runde 13: der einzige Uebergang zwischen dem Stromnetz des Mods und Forge-Energie.
    public static final DeferredBlock<Block> MACHINE_CONVERTER_HE_RF = register("machine_converter_he_rf", () -> new MachineConverterHeRfBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_CONVERTER_RF_HE = register("machine_converter_rf_he", () -> new MachineConverterRfHeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_AUTOSAW = register("machine_autosaw", () -> new MachineAutosawBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_THRESHER = register("machine_thresher", () -> new MachineThresherBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_SAWMILL = register("machine_sawmill", () -> new MachineSawmillBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_RTG = register("machine_rtg", () -> new MachineRTGBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    // Das Original setzt fuer beide Kondensatoren kein Harvest-Level, sie droppen dort also
    // auch ohne Spitzhacke. Deshalb hier bewusst OHNE requiresCorrectToolForDrops().
    public static final DeferredBlock<Block> MACHINE_CONDENSER = register("machine_condenser", () -> new MachineCondenserBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_TOWER_SMALL = register("machine_tower_small", () -> new MachineTowerSmallBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_TOWER_LARGE = register("machine_tower_large", () -> new MachineTowerLargeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_CONDENSER_POWERED = register("machine_condenser_powered", () -> new MachineCondenserPoweredBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_SOLAR_BOILER = register("machine_solar_boiler", () -> new MachineSolarBoilerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SOLAR_MIRROR = register("solar_mirror", () -> new SolarMirrorBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> FURNACE_IRON = register("furnace_iron", () -> new FurnaceIronBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    /* Der Ziegelofen. Im Original zwei Bloecke (machine_furnace_brick_off und _on), hier
     * einer mit der Eigenschaft LIT. Werte aus ModBlocks.java:1795. */
    public static final DeferredBlock<Block> MACHINE_FURNACE_BRICK = register("machine_furnace_brick", () -> new FurnaceBrickBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(FurnaceBrickBlock.LIT) ? 15 : 0)));
    public static final DeferredBlock<Block> FURNACE_STEEL = register("furnace_steel", () -> new FurnaceSteelBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_COMPRESSOR_COMPACT = register("machine_compressor_compact", () -> new MachineCompressorCompactBlock(BlockBehaviour.Properties.of().strength(10.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_MIXER = register("machine_mixer", () -> new MachineMixerBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_ROCK_MILL = register("machine_rock_mill", () -> new MachineRockMillBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_MINING_LASER = register("machine_mining_laser", () -> new MachineMiningLaserBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Der Daemmblock des Bergbaulasers. Er ist KEIN Baustoff: der Laser setzt ihn selbst, wo
     * Fluessigkeit in den Schacht laufen wuerde, und er laesst sich nicht aufheben. Deshalb ohne
     * Gegenstand und ohne Beute -- wie im Original, das ihn aus jeder Schoepferrunde heraushaelt. */
    public static final DeferredBlock<Block> BARRICADE = BLOCKS.register("barricade", () -> new Block(BlockBehaviour.Properties.of().strength(1.0F, 2.5F).sound(SoundType.SAND).mapColor(MapColor.SAND).noLootTable()));

    public static final DeferredBlock<Block> MACHINE_CYCLOTRON = register("machine_cyclotron", () -> new MachineCyclotronBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_SILEX = register("machine_silex", () -> new MachineSILEXBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_FEL = register("machine_fel", () -> new MachineFELBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Der Teilchenbeschleuniger: sechs Bauteile, die einzeln nichts tun und nur zusammen einen Ring ergeben. */
    public static final DeferredBlock<Block> MACHINE_PA_SOURCE = register("machine_pa_source", () -> new MachinePASourceBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PA_BEAMLINE = register("machine_pa_beamline", () -> new MachinePABeamlineBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PA_RFC = register("machine_pa_rfc", () -> new MachinePARFCBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PA_QUADRUPOLE = register("machine_pa_quadrupole", () -> new MachinePAQuadrupoleBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PA_DIPOLE = register("machine_pa_dipole", () -> new MachinePADipoleBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PA_DETECTOR = register("machine_pa_detector", () -> new MachinePADetectorBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Die Bestrahlungskammer: der Abnehmer der Teilchen und der einzige Weg zu den vier Endstoffen. */
    public static final DeferredBlock<Block> MACHINE_EXPOSURE_CHAMBER = register("machine_exposure_chamber", () -> new MachineExposureChamberBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Der Radiothermalgenerator: die letzte Maschine, die dem Port fehlte. */
    public static final DeferredBlock<Block> MACHINE_RAD_GEN = register("machine_rad_gen", () -> new MachineRadGenBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_GAS_CENT = register("machine_gas_cent", () -> new MachineGasCentBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_COMPRESSOR = register("machine_compressor", () -> new MachineCompressorBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_STEAM_ENGINE = register("machine_steam_engine", () -> new MachineSteamEngineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_STIRLING = register("machine_stirling", () -> new MachineStirlingBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_DIESEL = register("machine_diesel", () -> new MachineDieselBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_COMBUSTION_ENGINE = register("machine_combustion_engine", () -> new MachineCombustionEngineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_ELECTRIC_FURNACE = register("machine_electric_furnace", () -> new MachineElectricFurnaceBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(MachineElectricFurnaceBlock.LIT) ? 15 : 0)));
    public static final DeferredBlock<Block> MACHINE_RTG_FURNACE = register("machine_rtg_furnace", () -> new MachineRtgFurnaceBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(MachineRtgFurnaceBlock.LIT) ? 15 : 0)));
    public static final DeferredBlock<Block> MACHINE_DIFURNACE_RTG = register("machine_difurnace_rtg", () -> new MachineDiFurnaceRtgBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(MachineDiFurnaceRtgBlock.LIT) ? 15 : 0)));
    public static final DeferredBlock<Block> MACHINE_DIFURNACE = register("machine_difurnace", () -> new MachineDiFurnaceBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(MachineDiFurnaceBlock.LIT) ? 15 : 0)));
    public static final DeferredBlock<Block> MACHINE_DIFURNACE_EXTENSION = register("machine_difurnace_extension", () -> new MachineDiFurnaceExtensionBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_SHREDDER = register("machine_shredder", () -> new MachineShredderBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_SOLDERING_STATION = register("machine_soldering_station", () -> new MachineSolderingStationBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_ARC_WELDER = register("machine_arc_welder", () -> new MachineArcWelderBlock(BlockBehaviour.Properties.of().strength(0.6F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_WELL = registerNew("machine_well", () -> new MachineOilWellBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_PUMPJACK = registerNew("machine_pumpjack", () -> new MachinePumpjackBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_FRACKING_TOWER = registerNew("machine_fracking_tower", () -> new MachineFrackingTowerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_REFINERY = register("machine_refinery", () -> new MachineRefineryBlock(BlockBehaviour.Properties.of().strength(0.6F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_FRACTION_TOWER = register("machine_fraction_tower", () -> new MachineFractionTowerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> FRACTION_SPACER = register("fraction_spacer", () -> new FractionSpacerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_CATALYTIC_REFORMER = register("machine_catalytic_reformer", () -> new MachineCatalyticReformerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_HYDROTREATER = register("machine_hydrotreater", () -> new MachineHydrotreaterBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    /* Die Deuteriumkette. Werte aus ModBlocks.java:2245 und 2246 -- der Turm ist doppelt so
     * hart wie der Extraktor. */
    public static final DeferredBlock<Block> MACHINE_DEUTERIUM_EXTRACTOR = register("machine_deuterium_extractor", () -> new MachineDeuteriumExtractorBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_DEUTERIUM_TOWER = register("machine_deuterium_tower", () -> new DeuteriumTowerBlock(BlockBehaviour.Properties.of().strength(10.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    /* Die Zapfsaeule. Werte aus ModBlocks.java:2072. */
    public static final DeferredBlock<Block> MACHINE_REFUELER = register("machine_refueler", () -> new MachineRefuelerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_VACUUM_DISTILL = register("machine_vacuum_distill", () -> new MachineVacuumDistillBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PYRO_OVEN = register("machine_pyro_oven", () -> new MachinePyroOvenBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_CATALYTIC_CRACKER = register("machine_catalytic_cracker", () -> new MachineCatalyticCrackerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_COKER = register("machine_coker", () -> new MachineCokerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_GAS_FLARE = register("machine_gas_flare", () -> new MachineGasFlareBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_LIQUEFACTOR = register("machine_liquefactor", () -> new MachineLiquefactorBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_SOLIDIFIER = register("machine_solidifier", () -> new MachineSolidifierBlock(BlockBehaviour.Properties.of().strength(5.0F, 10F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> FURNACE_COMBINATION = register("furnace_combination", () -> new MachineFurnaceCombinationBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Runde 105: die drei Anschluesse an die Welt. */
    public static final DeferredBlock<Block> MACHINE_DRAIN = register("machine_drain", () -> new MachineDrainBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_INTAKE = register("machine_intake", () -> new MachineIntakeBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> PUMP_ELECTRIC = register("pump_electric", () -> new MachinePumpBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion(), false));
    public static final DeferredBlock<Block> PUMP_STEAM = register("pump_steam", () -> new MachinePumpBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion(), true));
    /* Runde 114: die Montagefabrik. */
    public static final DeferredBlock<Block> MACHINE_ASSEMBLY_FACTORY = register("machine_assembly_factory", () -> new MachineAssemblyFactoryBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Runde 113: die Chemiefabrik. */
    public static final DeferredBlock<Block> MACHINE_CHEMICAL_FACTORY = register("machine_chemical_factory", () -> new MachineChemicalFactoryBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Runde 112: die Schluesselschmiede und der Strommelder. */
    public static final DeferredBlock<Block> MACHINE_KEY_FORGE = register("machine_keyforge", () -> new MachineKeyForgeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MACHINE_DETECTOR = register("machine_detector", () -> new MachineDetectorBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Runde 111: der Teleporter. */
    public static final DeferredBlock<Block> MACHINE_TELEPORTER = register("machine_teleporter", () -> new MachineTeleporterBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Runde 110: der Hephaestus. */
    public static final DeferredBlock<Block> MACHINE_HEPHAESTUS = register("machine_hephaestus", () -> new MachineHephaestusBlock(BlockBehaviour.Properties.of().noOcclusion().strength(10.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Runde 109: der Strangguss. */
    public static final DeferredBlock<Block> MACHINE_STRAND_CASTER = register("machine_strand_caster", () -> new MachineStrandCasterBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops()));

    /* Runde 108: die Industrieturbine. */
    public static final DeferredBlock<Block> MACHINE_INDUSTRIAL_TURBINE = register("machine_industrial_turbine", () -> new MachineIndustrialTurbineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /* Runde 107: die elektrische Presse und der Trichter. */
    public static final DeferredBlock<Block> MACHINE_EPRESS = register("machine_epress", () -> new MachineEPressBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_FUNNEL = register("machine_funnel", () -> new MachineFunnelBlock(BlockBehaviour.Properties.of().strength(10.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Runde 106: der Selbstbauer. */
    public static final DeferredBlock<Block> MACHINE_AUTOCRAFTER = register("machine_autocrafter", () -> new MachineAutocrafterBlock(BlockBehaviour.Properties.of().strength(10.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> MACHINE_BLAST_FURNACE = register("machine_blast_furnace", () -> new MachineBlastFurnaceBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_WOOD_BURNER = register("machine_wood_burner", () -> new MachineWoodBurnerBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_CENTRIFUGE = register("machine_centrifuge", () -> new MachineCentrifugeBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_ROTARY_FURNACE = register("machine_rotary_furnace", () -> new MachineRotaryFurnaceBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_CRUCIBLE = register("machine_crucible", () -> new MachineCrucibleBlock(BlockBehaviour.Properties.of().strength(2.0F, 100.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> FOUNDRY_CHANNEL = register("foundry_channel", () -> new FoundryChannelBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> FOUNDRY_MOLD = register("foundry_mold", () -> new FoundryMoldBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> FOUNDRY_BASIN = register("foundry_basin", () -> new FoundryBasinBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    /* Der Ausguss: dieselben Werte wie die uebrige Giesserei, er ist ja aus derselben Rinne
     * gebaut. */
    public static final DeferredBlock<Block> FOUNDRY_OUTLET = register("foundry_outlet", () -> new FoundryOutletBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    /* Der HEV-Akku an der Wand. Er leuchtet schwach (10/15 im Original), ist mit einem Schlag
     * abzubauen und traegt seinen eigenen Blockgegenstand, der zugleich der tragbare Akku ist
     * -- siehe HEVBatteryItem. */
    public static final DeferredBlock<Block> HEV_BATTERY = register("hev_battery", () -> new HEVBatteryBlock(BlockBehaviour.Properties.of().strength(0.5F, 0.25F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().lightLevel(state -> 10)), HEVBatteryItem.class, new Properties().stacksTo(4));
    /* Der Schlackenabstich: derselbe Bau wie der Ausguss, nur laesst er die Schmelze fallen,
     * statt sie einem Abnehmer zu reichen. */
    public static final DeferredBlock<Block> FOUNDRY_SLAGTAP = register("foundry_slagtap", () -> new FoundrySlagtapBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    /* Die Schlackenpfuetze. Kein Kreativreiter und kein Gegenstand -- sie entsteht nur unter
     * dem Abstich und gibt beim Abbauen ihren Inhalt als Schrottklumpen zurueck, nicht sich
     * selbst. Darum noLootTable: die Beute steht in SlagBlock.getDrops. */
    public static final DeferredBlock<Block> SLAG = BLOCKS.register("slag", () -> new SlagBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).noOcclusion().noLootTable()));
    /* Der Lagerbehaelter. Mehrere nebeneinander laufen ineinander -- was die tausend
     * Blockzustaende erklaert, die er dafuer braucht. */
    public static final DeferredBlock<Block> FOUNDRY_TANK = register("foundry_tank", () -> new FoundryTankBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_ARC_FURNACE = register("machine_arc_furnace", () -> new MachineArcFurnaceLargeBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    /* RBMK: die Saeulen des Reaktors, dazu der Schutt, der von ihnen uebrig bleibt. */
    public static final DeferredBlock<Block> RBMK_BLANK = register("rbmk_blank", () -> new RBMKPassiveBlock(RBMKType.OTHER, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_MODERATOR = register("rbmk_moderator", () -> new RBMKPassiveBlock(RBMKType.MODERATOR, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_ABSORBER = register("rbmk_absorber", () -> new RBMKPassiveBlock(RBMKType.ABSORBER, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_REFLECTOR = register("rbmk_reflector", () -> new RBMKPassiveBlock(RBMKType.REFLECTOR, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_ROD = register("rbmk_rod", () -> new RBMKRodBlock(false, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_ROD_MOD = register("rbmk_rod_mod", () -> new RBMKRodBlock(true, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_CONTROL = register("rbmk_control", () -> new RBMKControlBlock(false, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_CONTROL_MOD = register("rbmk_control_mod", () -> new RBMKControlBlock(true, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_BOILER = register("rbmk_boiler", () -> new RBMKBoilerBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_COOLER = register("rbmk_cooler", () -> new RBMKCoolerBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_STORAGE = register("rbmk_storage", () -> new RBMKStorageBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_HEATER = register("rbmk_heater", () -> new RBMKHeaterBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_STEAM_INLET = register("rbmk_steam_inlet", () -> new RBMKPortBlock(true, BlockBehaviour.Properties.of().strength(50.0F, 60.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_STEAM_OUTLET = register("rbmk_steam_outlet", () -> new RBMKPortBlock(false, BlockBehaviour.Properties.of().strength(50.0F, 60.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /*
     * Runde 94: die Foerderbaender. Sie haben keine Blockentitaet -- der Gegenstand auf ihnen
     * bewegt sich selbst und fragt nur, wohin. Haerte und Widerstand wie im Original: 2.0 / 2.0.
     */
    public static final DeferredBlock<ConveyorBlock> CONVEYOR = register("conveyor", () -> new ConveyorBlock(conveyorProperties()));
    public static final DeferredBlock<ConveyorExpressBlock> CONVEYOR_EXPRESS = register("conveyor_express", () -> new ConveyorExpressBlock(conveyorProperties()));
    public static final DeferredBlock<ConveyorDoubleBlock> CONVEYOR_DOUBLE = register("conveyor_double", () -> new ConveyorDoubleBlock(conveyorProperties()));
    public static final DeferredBlock<ConveyorTripleBlock> CONVEYOR_TRIPLE = register("conveyor_triple", () -> new ConveyorTripleBlock(conveyorProperties()));
    /* Senkrechte Strecken. Sie sind voll hoch, also deckend -- anders als die flachen Baender. */
    public static final DeferredBlock<ConveyorLiftBlock> CONVEYOR_LIFT = register("conveyor_lift", () -> new ConveyorLiftBlock(BlockBehaviour.Properties.of().strength(2.0F, 2.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<ConveyorChuteBlock> CONVEYOR_CHUTE = register("conveyor_chute", () -> new ConveyorChuteBlock(BlockBehaviour.Properties.of().strength(2.0F, 2.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));

    /* Der Einleger: das Stueck, mit dem eine Bandstrecke in einer Maschine ankommt. */
    public static final DeferredBlock<CraneInserterBlock> CRANE_INSERTER = register("crane_inserter", () -> new CraneInserterBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /*
     * Runde 98: Tueren, Leitern, Zaun, Falltuer, Kette und Schmalspurgleis.
     *
     * Sie alle hat 1.21 als Blockarten schon fertig; das Original baut sie sich noch selbst
     * zusammen. Die Tueren lassen sich von Hand oeffnen -- dafuer steht der eigene Klangsatz in
     * NtmBlockSetTypes; die Falltuer nicht, sie nimmt den Eisensatz, so wie dort.
     */
    public static final DeferredBlock<DoorBlock> DOOR_METAL = register("door_metal", () -> new DoorBlock(NtmBlockSetTypes.METAL, BlockBehaviour.Properties.of().strength(5.0F, 5.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<DoorBlock> DOOR_OFFICE = register("door_office", () -> new DoorBlock(NtmBlockSetTypes.METAL, BlockBehaviour.Properties.of().strength(10.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<DoorBlock> DOOR_BUNKER = register("door_bunker", () -> new DoorBlock(NtmBlockSetTypes.METAL, BlockBehaviour.Properties.of().strength(10.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<TrapDoorBlock> TRAPDOOR_STEEL = register("trapdoor_steel", () -> new TrapDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of().strength(3.0F, 8.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<LadderBlock> LADDER_STEEL = register("ladder_steel", () -> new LadderBlock(BlockBehaviour.Properties.of().strength(0.25F, 2.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    /* Der Maschendrahtzaun gibt es als Feld und als Pfosten -- im Original zwei Metadaten-Werte. */
    public static final DeferredBlock<FenceBlock> FENCE_METAL = register("fence_metal", () -> new FenceBlock(BlockBehaviour.Properties.of().strength(15.0F, 0.25F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<FenceBlock> FENCE_METAL_POST = register("fence_metal_post", () -> new FenceBlock(BlockBehaviour.Properties.of().strength(15.0F, 0.25F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> DUNGEON_CHAIN = register("dungeon_chain", () -> new ClimbableChainBlock(BlockBehaviour.Properties.of().strength(0.25F, 2.0F).sound(SoundType.CHAIN).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<RailBlock> RAIL_NARROW = register("rail_narrow", () -> new RailBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noCollission()));

    /* Der Auszieher: das Gegenstueck, mit dem eine Strecke an einer Maschine beginnt. */
    public static final DeferredBlock<CraneExtractorBlock> CRANE_EXTRACTOR = register("crane_extractor", () -> new CraneExtractorBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CraneGrabberBlock> CRANE_GRABBER = register("crane_grabber", () -> new CraneGrabberBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CraneBoxerBlock> CRANE_BOXER = register("crane_boxer", () -> new CraneBoxerBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CraneUnboxerBlock> CRANE_UNBOXER = register("crane_unboxer", () -> new CraneUnboxerBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CraneRouterBlock> CRANE_ROUTER = register("crane_router", () -> new CraneRouterBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<CranePartitionerBlock> CRANE_PARTITIONER = register("crane_partitioner", () -> new CranePartitionerBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<CraneSplitterBlock> CRANE_SPLITTER = register("crane_splitter", () -> new CraneSplitterBlock(BlockBehaviour.Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));

    /* Der Rohranschluss unter der Saeule. Kein Kran -- siehe RBMKLoaderBlock. */
    public static final DeferredBlock<Block> RBMK_LOADER = register("rbmk_loader", () -> new RBMKLoaderBlock(BlockBehaviour.Properties.of().strength(50.0F, 60.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_OUTGASSER = register("rbmk_outgasser", () -> new RBMKOutgasserBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_ROD_REASIM = register("rbmk_rod_reasim", () -> new RBMKRodReaSimBlock(false, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_ROD_REASIM_MOD = register("rbmk_rod_reasim_mod", () -> new RBMKRodReaSimBlock(true, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_CONTROL_AUTO = register("rbmk_control_auto", () -> new RBMKControlAutoBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_CONSOLE = register("rbmk_console", () -> new RBMKConsoleBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_CRANE_CONSOLE = register("rbmk_crane_console", () -> new RBMKCraneConsoleBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_AUTOLOADER = register("rbmk_autoloader", () -> new RBMKAutoloaderBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_WASTE_DRUM = register("machine_waste_drum", () -> new WasteDrumBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_CONTROL_REASIM = register("rbmk_control_reasim", () -> new RBMKControlBlock(false, true, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_CONTROL_REASIM_AUTO = register("rbmk_control_reasim_auto", () -> new RBMKControlAutoBlock(true, BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_DEBRIS = register("rbmk_debris", () -> new RBMKDebrisBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_DEBRIS_BURNING = register("rbmk_debris_burning", () -> new RBMKDebrisBurningBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).lightLevel(state -> 7).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_DEBRIS_DIGAMMA = register("rbmk_debris_digamma", () -> new RBMKDebrisDigammaBlock(BlockBehaviour.Properties.of().strength(50.0F, 600.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).lightLevel(state -> 7).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RBMK_DEBRIS_RADIATING = register("rbmk_debris_radiating", () -> new RBMKDebrisRadiatingBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).lightLevel(state -> 7).requiresCorrectToolForDrops()));

    /* Runde 43: die RBMK-Anzeigetafeln. */
    public static final DeferredBlock<Block> RBMK_GAUGE = register("rbmk_gauge", () -> new RBMKGaugeBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_INDICATOR = register("rbmk_indicator", () -> new RBMKIndicatorBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_NUMITRON = register("rbmk_numitron", () -> new RBMKNumitronBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_LEVER = register("rbmk_lever", () -> new RBMKLeverBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_KEYPAD = register("rbmk_keypad", () -> new RBMKKeyPadBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_GRAPH = register("rbmk_graph", () -> new RBMKGraphBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_DISPLAY = register("rbmk_display", () -> new RBMKDisplayBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RBMK_TERMINAL = register("rbmk_terminal", () -> new RBMKTerminalBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    /** Die Blankotafel: dieselbe Form wie die anderen acht, aber ohne Innenleben. */
    public static final DeferredBlock<Block> RBMK_DISPLAY_BLANK = register("rbmk_display_blank", () -> new RBMKMiniPanelBlock(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_PUREX = register("machine_purex", () -> new MachinePUREXBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_RADIOLYSIS = register("machine_radiolysis", () -> new MachineRadiolysisBlock(BlockBehaviour.Properties.of().strength(5.0F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> MACHINE_CHEMICAL_PLANT = register("machine_chemical_plant", () -> new MachineChemicalPlantBlock(BlockBehaviour.Properties.of().strength(0.6F, 100.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> CRATE_IRON = registerNew("crate_iron", () -> new CrateBlock(BlockBehaviour.Properties.of().strength(0.6F, 6.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CrateBlock.Type.IRON));
    public static final DeferredBlock<Block> CRATE_TUNGSTEN = registerNew("crate_tungsten", () -> new CrateBlock(BlockBehaviour.Properties.of().strength(0.6F, 6.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CrateBlock.Type.TUNGSTEN));
    public static final DeferredBlock<Block> CRATE_STEEL = registerNew("crate_steel", () -> new CrateBlock(BlockBehaviour.Properties.of().strength(0.6F, 6.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CrateBlock.Type.STEEL));
    /*
     * Runde 256: der TRESOR. Im Original dieselbe Klasse wie die Vorratskisten, nur mit
     * fuenfzehn Faechern und einem Bild auf der Vorderseite statt auf dem Deckel. Seine
     * Sprengfestigkeit von 10000 ist die des Originals (ModBlocks.java:2176) -- er soll eine
     * Kernwaffe ueberstehen.
     */
    public static final DeferredBlock<Block> SAFE = registerNew("safe", () -> new CrateBlock(BlockBehaviour.Properties.of().strength(7.5F, 10000.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CrateBlock.Type.SAFE));
    /* Der Aktenschrank: im Original ein Block mit zwei Metadatensorten, im Port zwei
     * Anmeldungen derselben Klasse. Haerte und Sprengfestigkeit sind die des Originals
     * (setHardness(10.0F).setResistance(15.0F)). */
    public static final DeferredBlock<Block> FILING_CABINET = registerNew("filing_cabinet", () -> new FileCabinetBlock(BlockBehaviour.Properties.of().strength(10.0F, 15.0F).noOcclusion().sound(SoundType.METAL).mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> FILING_CABINET_STEEL = registerNew("filing_cabinet_steel", () -> new FileCabinetBlock(BlockBehaviour.Properties.of().strength(10.0F, 15.0F).noOcclusion().sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CRATE_DESH = registerNew("crate_desh", () -> new CrateBlock(BlockBehaviour.Properties.of().strength(0.6F, 6.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CrateBlock.Type.DESH));
    public static final DeferredBlock<Block> CRATE_TEMPLATE = registerNew("crate_template", () -> new CrateBlock(BlockBehaviour.Properties.of().strength(0.6F, 6.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CrateBlock.Type.TEMPLATE));
    /*
     * Runde 278: die SOJUS-LANDEKAPSEL. Haerte und Sprengfestigkeit sind die des Originals
     * (ModBlocks.java:2091). Sie hat kein Blockmodell -- ihr Aussehen kommt aus
     * soyuz_lander.obj, deshalb noOcclusion().
     */
    public static final DeferredBlock<Block> SOYUZ_CAPSULE = registerNew("soyuz_capsule", () -> new SoyuzCapsuleBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    /* Stufe 5: die Beutekisten. Sie fallen wie Kies und geben ihren Inhalt nur der
     * Brechstange her. Werte aus ModBlocks.java:2079 ff. des Originals. */
    public static final DeferredBlock<Block> CRATE_LEAD = register("crate_lead", () -> new LootCrateBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), LootCrateBlock.Art.BLEI));
    /* Die Munitionskiste. Sie faellt nicht wie die Beutekisten, hat aber denselben Beschlag:
     * nur die Brechstange oeffnet sie. */
    public static final DeferredBlock<Block> CRATE_AMMO = register("crate_ammo", () -> new AmmoCrateBlock(BlockBehaviour.Properties.of().strength(1.0F, 2.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Die Dosenkiste. Holz statt Metall, und ihr Umriss ist kein voller Wuerfel -- daher
     * noOcclusion. Werte aus ModBlocks.java:2169 des Originals. */
    /* Der Beutesockel. Hart wie nichts und ohne Widerstand, wie im Original
     * (ModBlocks.java:1446) -- er soll nicht im Weg stehen. */
    public static final DeferredBlock<Block> DECO_LOOT = register("deco_loot", () -> new LootDecoBlock(BlockBehaviour.Properties.of().strength(0.0F, 0.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().noLootTable()));
    /*
     * Runde 251: der BEUTESTAB. Er steht in den Bauwerksdateien an jeder Stelle, an der spaeter
     * Beute liegen soll, und ersetzt sich beim ersten Servertick durch das, was in ihm steht --
     * eine Truhe mit gezogenem Inhalt oder einen Beutesockel mit einem Beuterezept.
     *
     * KEIN KREATIVREITER, wie im Original (ModBlocks.java:2365 setzt keinen). Er ist Werkzeug
     * des Bauwerksbaus, nicht Ausstattung.
     */
    public static final DeferredBlock<Block> WAND_LOOT = register("wand_loot", () -> new WandLootBlock(BlockBehaviour.Properties.of().strength(1.0F, 1.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Der Logikstab, Runde 265 -- der zweite Zauberstab, den der Port als Block braucht. Er
     * ist die Falle in einem Bauwerk und steht wie der Beutestab in keinem Reiter. */
    public static final DeferredBlock<Block> WAND_LOGIC = register("wand_logic", () -> new WandLogicBlock(BlockBehaviour.Properties.of().strength(1.0F, 1.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Die Nachschubkiste am Fallschirm. Holzwerte wie im Original (ModBlocks.java:2092),
     * und sie teilt sich das Modell der Dosenkiste -- dort ebenfalls, ueber denselben
     * Zeichnertyp und dieselbe Textur. */
    public static final DeferredBlock<Block> CRATE_SUPPLY = register("crate_supply", () -> new SupplyCrateBlock(BlockBehaviour.Properties.of().strength(1.0F, 2.5F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> CRATE_CAN = register("crate_can", () -> new CanCrateBlock(BlockBehaviour.Properties.of().strength(1.0F, 2.5F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> CRATE_METAL = register("crate_metal", () -> new LootCrateBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), LootCrateBlock.Art.METALL));
    /* Nachschub- und Waffenkiste: Holz statt Metall, sonst dieselben Werte.
     * Aus ModBlocks.java:2164 f. des Originals. */
    public static final DeferredBlock<Block> CRATE = register("crate", () -> new LootCrateBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD), LootCrateBlock.Art.NACHSCHUB));
    public static final DeferredBlock<Block> CRATE_WEAPON = register("crate_weapon", () -> new LootCrateBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD), LootCrateBlock.Art.WAFFEN));
    /* Die rote Kiste. Metall wie Blei- und Metallkiste, Werte aus ModBlocks.java:2168.
     * Sie steht in keinem Kreativ-Reiter -- das Original gibt ihr setCreativeTab(null). */
    public static final DeferredBlock<Block> CRATE_RED = register("crate_red", () -> new LootCrateBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), LootCrateBlock.Art.ROT));
    /* Was vom Gueterwagen uebrig bleibt, wo er aufgeschlagen ist. Im Original ein DecoBlock,
     * Werte aus ModBlocks.java:2179. */
    public static final DeferredBlock<Block> BOXCAR = register("boxcar", () -> new Block(BlockBehaviour.Properties.of().strength(10.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Was vom Luftschiff uebrig bleibt. Im Original ebenfalls ein DecoBlock mit denselben
     * Werten, Textur "hbm:asphalt" -- ModBlocks.java:2180. */
    public static final DeferredBlock<Block> BOAT = register("boat", () -> new Block(BlockBehaviour.Properties.of().strength(10.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> BARREL_PLASTIC = registerNew("barrel_plastic", () -> new com.hbm.blocks.machine.BarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.3F, 2.5F).sound(SoundType.METAL), 12_000, false));
    public static final DeferredBlock<Block> EMP_BOMB = register("emp_bomb", () -> new Block(BlockBehaviour.Properties.of().strength(3.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BARREL_STEEL = registerNew("barrel_steel", () -> new com.hbm.blocks.machine.BarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.3F, 2.5F).sound(SoundType.METAL), 16_000, false));
    public static final DeferredBlock<Block> BARREL_CORRODED = registerNew("barrel_corroded", () -> new com.hbm.blocks.machine.BarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.3F, 2.5F).sound(SoundType.METAL), 6_000, true));
    public static final DeferredBlock<Block> BARREL_TCALLOY = registerNew("barrel_tcalloy", () -> new com.hbm.blocks.machine.BarrelBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.3F, 2.5F).sound(SoundType.METAL), 24_000, false));
    public static final DeferredBlock<Block> TRANSFORMER = register("transformer", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    public static final DeferredBlock<Block> REACTOR_ZIRNOX = register("machine_zirnox", () -> new ReactorZirnoxBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 100F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> ZIRNOX_DESTROYED = register("zirnox_destroyed", () -> new ZirnoxDestroyedBlock(BlockBehaviour.Properties.of().noOcclusion().strength(0.6F, 100F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /* Der Watz-Reaktor: Kern, Deckel, Zusammenbauklotz und die drei Wandbauteile. */
    public static final DeferredBlock<Block> WATZ = register("watz", () -> new WatzBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> WATZ_PUMP = register("watz_pump", () -> new WatzPumpBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> STRUCT_WATZ_CORE = register("struct_watz_core", () -> new WatzStructBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).lightLevel(state -> 15).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> WATZ_ELEMENT = register("watz_element", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> WATZ_COOLER = register("watz_cooler", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Zwei Baustufen: roh und verschraubt. Nur die verschraubte zaehlt beim Zusammenbau. */
    public static final DeferredBlock<Block> WATZ_END = register("watz_end", () -> new ToolConversionBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL), 2));

    /* Chicago Pile MK2: der Graphitziegel und der zusammengebaute Reaktor. */
    public static final DeferredBlock<Block> PILE_BRICK = register("pile_brick", () -> new PileBrickBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.COLOR_BLACK)));
    /* Ohne Beutetabelle -- wer ihn abbaut, bekommt den Graphitziegel zurueck, den der Block selbst setzt. */
    public static final DeferredBlock<Block> PILE_BLOCK = BLOCKS.register("pile_block", () -> new PileBlock(BlockBehaviour.Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> PILE_LOADER = register("pile_loader", () -> new PileLoaderBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> PILE_VENT = register("pile_vent", () -> new PileVentBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> PILE_CONTROL = register("pile_control", () -> new PileControlBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /* Traegheitsfusion: der Laser, die Brennkammer und ihre Bauteile. */
    public static final DeferredBlock<Block> ICF_LASER_COMPONENT = register("icf_laser_component", () -> new ICFLaserComponentBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> ICF_COMPONENT = register("icf_component", () -> new ICFComponentBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> ICF_CONTROLLER = register("icf_controller", () -> new ICFControllerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> STRUCT_ICF = register("struct_icf", () -> new ICFStructBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Ohne Beutetabelle -- beide geben beim Abbauen das zurueck, was sie ersetzt haben. */
    public static final DeferredBlock<Block> ICF_BLOCK = BLOCKS.register("icf_block", () -> new ICFWrapperBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_BREEDER = register("fusion_breeder", () -> new MachineFusionBreederBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_COLLECTOR = register("fusion_collector", () -> new MachineFusionCollectorBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_COUPLER = register("fusion_coupler", () -> new MachineFusionCouplerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_BOILER = register("fusion_boiler", () -> new MachineFusionBoilerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_MHDT = register("fusion_mhdt", () -> new MachineFusionMHDTBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_KLYSTRON = register("fusion_klystron", () -> new MachineFusionKlystronBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_TORUS = register("fusion_torus", () -> new MachineFusionTorusBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FUSION_PLASMA_FORGE = register("fusion_plasma_forge", () -> new MachineFusionPlasmaForgeBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Der Baustein der Fusionsanlage. Nur Zutat, aber platzierbar, weil eine Baustufe mit dem Schweissbrenner entsteht. */
    public static final DeferredBlock<Block> FUSION_COMPONENT = register("fusion_component", () -> new FusionComponentBlock(BlockBehaviour.Properties.of().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /* Der tote Zweig des Originals: Forschungsreaktor, Brutreaktor und Reaktorpult. */
    public static final DeferredBlock<Block> REACTOR_RESEARCH = register("reactor_research", () -> new ReactorResearchBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_REACTOR_BREEDING = register("machine_reactor_breeding", () -> new MachineReactorBreedingBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /* Die Geschuetztuerme. */
    public static final DeferredBlock<Block> TURRET_SENTRY = register("turret_sentry", () -> new TurretSentryBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> TURRET_SENTRY_DAMAGED = register("turret_sentry_damaged", () -> new TurretSentryDamagedBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> TURRET_JEREMY = register("turret_jeremy", () -> new TurretJeremyBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> TURRET_HOWARD = register("turret_howard", () -> new TurretHowardBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> TURRET_HOWARD_DAMAGED = register("turret_howard_damaged", () -> new TurretHowardDamagedBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> TURRET_CHEKHOV = register("turret_chekhov", () -> new TurretChekhovBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> TURRET_FRIENDLY = register("turret_friendly", () -> new TurretFriendlyBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> REACTOR_CONTROL = register("reactor_control", () -> new ReactorControlBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    /** Der Waffentisch, an dem Aufsaetze an- und abgebaut werden. */
    public static final DeferredBlock<Block> WEAPON_TABLE = register("weapon_table", () -> new WeaponTableBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> ARMOR_TABLE = register("armor_table", () -> new ArmorTableBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_ICF_PRESS = register("machine_icf_press", () -> new MachineICFPressBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ICF = register("icf", () -> new MachineICFBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    public static final DeferredBlock<Block> RED_CABLE = register("red_cable", () -> new CableBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> RED_WIRE_COATED = register("red_wire_coated", () -> new WireCoatedBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> CABLE_SWITCH = register("cable_switch", () -> new CableSwitchBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> CABLE_DIODE = register("cable_diode", () -> new CableDiodeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    public static final DeferredBlock<Block> CABLE_DETECTOR = register("cable_detector", () -> new CableDetectorBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> RED_CABLE_GAUGE = register("red_cable_gauge", () -> new CableGaugeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> RED_CONNECTOR = register("red_connector", () -> new ConnectorRedWireBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)), LoreBlockItem.class);
    public static final DeferredBlock<Block> RED_CONNECTOR_SUPER = register("red_connector_super", () -> new ConnectorRedWireSuperBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)), LoreBlockItem.class);

    public static final DeferredBlock<Block> RED_PYLON = register("red_pylon", () -> new PylonRedWireBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RED_PYLON_STEEL = register("red_pylon_steel", () -> new PylonRedWireBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RED_PYLON_MEDIUM_WOOD = register("red_pylon_medium_wood", () -> new PylonMediumBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RED_PYLON_MEDIUM_WOOD_TRANSFORMER = register("red_pylon_medium_wood_transformer", () -> new PylonMediumBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.WOOD).mapColor(MapColor.WOOD).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RED_PYLON_MEDIUM_STEEL = register("red_pylon_medium_steel", () -> new PylonMediumBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RED_PYLON_MEDIUM_STEEL_TRANSFORMER = register("red_pylon_medium_steel_transformer", () -> new PylonMediumBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> RED_PYLON_LARGE = register("red_pylon_large", () -> new PylonLargeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> SUBSTATION = register("substation", () -> new SubstationBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<Block> FLUID_DUCT_NEO = registerNew("fluid_duct_neo", () -> new FluidDuctStandardBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FLUID_DUCT_GAUGE = register("fluid_duct_gauge", () -> new FluidDuctGaugeBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    /* Die drei Haehne der Rohrfamilie. Werte aus ModBlocks.java:1885 bis 1887. */
    public static final DeferredBlock<Block> FLUID_VALVE = register("fluid_valve", () -> new FluidValveBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FLUID_SWITCH = register("fluid_switch", () -> new FluidSwitchBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> FLUID_COUNTER_VALVE = register("fluid_counter_valve", () -> new FluidCounterValveBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> RADIO_TORCH_SENDER = register("radio_torch_sender", () -> new RadioTorchSenderBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> RADIO_TORCH_RECEIVER  = register("radio_torch_receiver", () -> new RadioTorchReceiverBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    /* Der Zaehler: dieselbe Fackel, aber mit drei Musterfaechern und einem Menue.
     * Werte wie bei Sender und Empfaenger. */
    public static final DeferredBlock<Block> RADIO_TORCH_COUNTER = register("radio_torch_counter", () -> new RadioTorchCounterBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    /* Der Leser: dieselbe Fackel, aber sie fragt die Maschine hinter sich nach benannten
     * Werten. Haerte wie bei den drei anderen Funkfackeln des Ports -- das Original setzt fuer
     * alle vier 0.1F, der Port ist hier schon bei Sender und Empfaenger davon abgewichen. */
    public static final DeferredBlock<Block> RADIO_TORCH_READER = register("radio_torch_reader", () -> new RadioTorchReaderBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));
    /* Die Logikfackel: ein Empfaenger, dessen Redstone-Staerke aus sechzehn Bedingungen
     * hervorgeht statt aus der Nachricht selbst. */
    public static final DeferredBlock<Block> RADIO_TORCH_LOGIC = register("radio_torch_logic", () -> new RadioTorchLogicBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F, 10.0F).sound(ModSoundTypes.PIPE).mapColor(MapColor.METAL)));

    public static final DeferredBlock<Block> MACHINE_BATTERY_SOCKET = register("machine_battery_socket", () -> new MachineBatterySocketBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_BATTERY_REDD = register("machine_battery_redd", () -> new MachineBatteryREDDBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    // Fuenf Alt-Batteriestufen. Im Original stehen sie auf @Deprecated und setCreativeTab(null),
    // sie sind dort also nicht erreichbar und haben kein einziges Rezept -- sie existieren nur,
    // damit alte Welten nicht kaputtgehen. Der Port haelt es genauso.
    public static final DeferredBlock<Block> MACHINE_BATTERY_POTATO = registerNew("machine_battery_potato", () -> new MachineBatteryBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), 10_000L));
    public static final DeferredBlock<Block> MACHINE_BATTERY = registerNew("machine_battery", () -> new MachineBatteryBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), 1_000_000L));
    public static final DeferredBlock<Block> MACHINE_LITHIUM_BATTERY = registerNew("machine_lithium_battery", () -> new MachineBatteryBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), 50_000_000L));
    public static final DeferredBlock<Block> MACHINE_SCHRABIDIUM_BATTERY = registerNew("machine_schrabidium_battery", () -> new MachineBatteryBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), 25_000_000_000L));
    public static final DeferredBlock<Block> MACHINE_DINEUTRONIUM_BATTERY = registerNew("machine_dineutronium_battery", () -> new MachineBatteryBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops(), 1_000_000_000_000L));
    public static final DeferredBlock<Block> MACHINE_ASSEMBLY_MACHINE = register("machine_assembly_machine", () -> new MachineAssemblyMachineBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_PRECASS = register("machine_precass", () -> new MachinePrecAssBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_ORE_SLOPPER = register("machine_ore_slopper", () -> new MachineOreSlopperBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_EXCAVATOR = register("machine_excavator", () -> new MachineExcavatorBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 30.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_MISSILE_ASSEMBLY = register("machine_missile_assembly", () -> new MachineMissileAssemblyBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_FLUID_TANK = register("machine_fluid_tank", () -> new MachineFluidTankBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_BIGASSTANK = register("machine_bigasstank", () -> new MachineBigAssTankBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_CHUNGUS = register("machine_chungus", () -> new MachineChungusBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 20.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    public static final DeferredBlock<Block> MACHINE_SATLINKER = register("machine_satlinker", () -> new MachineSatLinkerBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    /* Nicht mit dem darueber verwechseln: der SATLINKER vergibt Frequenzen an Chips, der
     * SAT_LINK ist die Bodenstation, die auf einer solchen Frequenz einen Satelliten abhoert. */
    public static final DeferredBlock<Block> MACHINE_ANNIHILATOR = register("machine_annihilator", () -> new MachineAnnihilatorBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_SIREN = register("machine_siren", () -> new MachineSirenBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> RADAR_SCREEN = register("radar_screen", () -> new RadarScreenBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_AMMO_PRESS = register("machine_ammo_press", () -> new MachineAmmoPressBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_SUPER_COMPUTER = register("machine_supercomputer", () -> new MachineSuperComputerBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_TAPE_DRIVE = register("machine_tape_drive", () -> new MachineTapeDriveBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_SAT_LINK = register("machine_satlink", () -> new MachineSatLinkBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_SAT_DOCK = register("sat_dock", () -> new MachineSatDockBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    // Absorbers
    public static final DeferredBlock<Block> DECONTAMINATOR = register("decontaminator", () -> new DecontaminatorBlock(BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.TERRACOTTA_GREEN)));

    // PWR
    private static BlockBehaviour.Properties pwrPart() {
        return BlockBehaviour.Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<Block> PWR_CONTROLLER = register("pwr_controller", () -> new MachinePWRControllerBlock(pwrPart()));
    public static final DeferredBlock<Block> PWR_CASING = register("pwr_casing", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_REFLECTOR = register("pwr_reflector", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_PORT = register("pwr_port", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_HEATEX = register("pwr_heatex", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_HEATSINK = register("pwr_heatsink", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_NEUTRON_SOURCE = register("pwr_neutron_source", () -> new Block(pwrPart()));
    /* HIER STAND EIN ABSTURZ: Block und Gegenstand hiessen beide "pwr_fuel". Auf 1.7.10 ging
     * das, weil Bloecke und Gegenstaende getrennte Verzeichnisse hatten; auf 1.21 liegt das
     * BlockItem im selben Verzeichnis wie jeder andere Gegenstand, und NeoForge bricht mit
     * "Duplicate registration pwr_fuel" ab. Der Block heisst jetzt so, wie seine Anzeige ihn
     * ohnehin nennt -- "PWR Fuel Channel" --, passend zu pwr_control (Control Rod Channel) und
     * pwr_channel (Coolant Channel). Der Brennstab behaelt pwr_fuel. */
    public static final DeferredBlock<Block> PWR_FUEL_CHANNEL = register("pwr_fuel_channel", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_CONTROL = register("pwr_control", () -> new Block(pwrPart()));
    public static final DeferredBlock<Block> PWR_CHANNEL = register("pwr_channel", () -> new Block(pwrPart()));

    /**
     * Der Stellvertreter, durch den beim Einlesen jedes Bauteil ersetzt wird. Er steht in keinem
     * Kreativreiter und laesst sich nicht aufsammeln -- man bekommt ihn nur, indem man einen
     * Reaktor baut, und wieder los, indem man ihn abreisst.
     */
    public static final DeferredBlock<Block> PWR_BLOCK = register("pwr_block", () -> new PWRBlock(BlockBehaviour.Properties.of().strength(15.0F, 10.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).requiresCorrectToolForDrops().noLootTable()));
    
    // E
    public static final DeferredBlock<Block> BALEFIRE =     BLOCKS.register("balefire",     () -> new BalefireBlock(     BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).replaceable().noCollission().noOcclusion().strength(0F).lightLevel(state -> 15).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));
    /*
     * Die Digamma-Asche, Runde 276. Sie ist das, was der Speer hinterlaesst: ein fallender
     * Block wie Sand, mit derselben Haerte und Sprengfestigkeit wie im Original (0.5 und 150).
     *
     * IHRE TEXTUR LAG SCHON IM PORT -- ash_digamma.png, ohne dass ein Block sie benutzt haette.
     * Eine Textur ohne Block ist dasselbe wie ein Modell ohne Zeichner: sie liegt da und tut
     * nichts. Jetzt hat sie einen.
     *
     * NICHT DABEI: der Ascheschleier, den BlockAshes im Original ueber den Bildschirm legt --
     * der haengt an einem Client-Zaehler und an der Aschebrille, beides eigene Teilsysteme.
     */
    public static final DeferredBlock<Block> ASH_DIGAMMA = register("ash_digamma", () -> new SimpleFallingBlock(BlockBehaviour.Properties.of().strength(0.5F, 150.0F).sound(SoundType.SAND).mapColor(MapColor.COLOR_GRAY)));
    public static final DeferredBlock<Block> FIRE_DIGAMMA = BLOCKS.register("fire_digamma", () -> new DigammaFlameBlock( BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).replaceable().noCollission().noOcclusion().strength(0F, 150F).lightLevel(state -> 10).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> VOLCANO_CORE =     registerNew("volcano_core",     () -> new VolcanoBlock(BlockBehaviour.Properties.of().strength(-1.0F, 10000.0F).mapColor(MapColor.NETHER)));
    public static final DeferredBlock<Block> VOLCANO_RAD_CORE = registerNew("volcano_rad_core", () -> new VolcanoBlock(BlockBehaviour.Properties.of().strength(-1.0F, 10000.0F).mapColor(DyeColor.GREEN)));

    // Missile Blocks
    public static final DeferredBlock<Block> LAUNCH_PAD = register("launch_pad", () -> new LaunchPadBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).mapColor(MapColor.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> LAUNCH_PAD_LARGE = register("launch_pad_large", () -> new LaunchPadLargeBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).mapColor(MapColor.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> SOYUZ_LAUNCHER = register("soyuz_launcher", () -> new SoyuzLauncherBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).mapColor(MapColor.METAL).mapColor(MapColor.METAL)));
    public static final DeferredBlock<Block> MACHINE_RADAR = register("machine_radar", () -> new MachineRadarBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).mapColor(MapColor.METAL).mapColor(MapColor.METAL)));
    /* Den Block gab es schon; bis Runde 120 stand dahinter das GEWOEHNLICHE Radar, das grosse
     * war also nur dem Namen nach gross. Jetzt steht die eigene Klasse dahinter. */
    public static final DeferredBlock<Block> MACHINE_RADAR_LARGE = register("machine_radar_large", () -> new MachineRadarLargeBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F, 10.0F).mapColor(MapColor.METAL).mapColor(MapColor.METAL)));

    // Fluids
    public static final DeferredBlock<LiquidBlock> VOLCANIC_LAVA = BLOCKS.register("volcanic_lava", () -> new VolcanicLiquidBlock(NtmFluids.VOLCANIC_LAVA.get(), BlockBehaviour.Properties.of().randomTicks().noCollission().replaceable().strength(500F).lightLevel(state -> 15).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));
    public static final DeferredBlock<LiquidBlock> RAD_LAVA = BLOCKS.register("rad_lava", () -> new RadLiquidBlock(NtmFluids.RAD_LAVA.get(), BlockBehaviour.Properties.of().randomTicks().noCollission().replaceable().strength(500F).lightLevel(state -> 15).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));
    public static final DeferredBlock<LiquidBlock> CORIUM = BLOCKS.register("corium", () -> new CoriumLiquidBlock(NtmFluids.CORIUM.get(), BlockBehaviour.Properties.of().randomTicks().noCollission().replaceable().strength(500F).lightLevel(state -> 10).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));
    public static final DeferredBlock<LiquidBlock> MUD = BLOCKS.register("mud", () -> new MudLiquidBlock(NtmFluids.MUD.get(), BlockBehaviour.Properties.of().randomTicks().noCollission().replaceable().strength(500F).lightLevel(state -> 5).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));
    /* Stufe 5: die Giftbruehe aus dem gelben Fass. Leuchtkraft 15 wie im Original. */
    public static final DeferredBlock<LiquidBlock> TOXIC_BLOCK = BLOCKS.register("toxic_block", () -> new ToxicLiquidBlock(NtmFluids.TOXIC.get(), BlockBehaviour.Properties.of().randomTicks().noCollission().replaceable().strength(500F).lightLevel(state -> 15).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));
    /* Die Schwefelsaeure, Runde 273 -- der einzige Fluidblock des Originals mit
     * Schadensquelle, und damit der einzige Weg zum Erfolg "sulfuric". */
    public static final DeferredBlock<LiquidBlock> SULFURIC_ACID_BLOCK = BLOCKS.register("sulfuric_acid_block", () -> new SulfuricAcidLiquidBlock(NtmFluids.SULFURIC_ACID.get(), BlockBehaviour.Properties.of().randomTicks().noCollission().replaceable().strength(500F).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));

    // Other Technical Blocks
    public static final DeferredBlock<Block> GAS_RADON =       register("gas_radon",       () -> new GasRadonBlock(      BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_RADON_DENSE = register("gas_radon_dense", () -> new GasRadonDenseBlock( BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_RADON_TOMB =  register("gas_radon_tomb",  () -> new GasRadonTombBlock(  BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_MELTDOWN =    register("gas_meltdown",    () -> new GasMeltdownBlock(   BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_MONOXIDE =    register("gas_monoxide",    () -> new GasMonoxideBlock(   BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_ASBESTOS =    register("gas_asbestos",    () -> new GasAsbestosBlock(   BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_COAL =        register("gas_coal",        () -> new GasCoalBlock(       BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_FLAMMABLE =   register("gas_flammable",   () -> new GasFlammableBlock(  BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> GAS_EXPLOSIVE =   register("gas_explosive",   () -> new GasExplosiveBlock(  BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> OIL_PIPE =        register("oil_pipe",        () -> new Block(BlockBehaviour.Properties.of().strength(1.5F).noLootTable().sound(SoundType.METAL).mapColor(MapColor.METAL)));

    // ???
    public static final DeferredBlock<Block> TAINT = register("taint", () -> new TaintBlock(BlockBehaviour.Properties.of().randomTicks().strength(15.0F, 10.0F).noLootTable().mapColor(DyeColor.GRAY)));

    public static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) { return true; }
    public static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) { return false; }
    public static boolean noSpawn(BlockState var1, BlockGetter var2, BlockPos var3, EntityType<?> var4) { return false; }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> defBlock = BLOCKS.register(name, block);
        NtmItems.ITEMS.register(name, () -> new BlockItem(defBlock.get(), new Properties()));
        return defBlock;
    }

    /** Gemeinsame Eigenschaften der Foerderbaender: vier Pixel hoch, also nicht deckend. */
    private static BlockBehaviour.Properties conveyorProperties() {
        return BlockBehaviour.Properties.of().strength(2.0F, 2.0F).sound(SoundType.METAL).mapColor(MapColor.METAL)
                .noOcclusion().isSuffocating(NtmBlocks::never).isViewBlocking(NtmBlocks::never);
    }

    /** Ein Zierrohr: Saeule, nicht deckend, Haerte und Widerstand wie im Original. */
    private static DeferredBlock<RotatedPillarBlock> registerPipe(String name) {
        return register(name, () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .strength(2.0F, 5.0F).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion()));
    }

    private static <T extends Block> DeferredBlock<T> registerNew(String name, Supplier<T> block) {
        DeferredBlock<T> defBlock = BLOCKS.register(name, block);
        NtmItems.ITEMS.register(name, () -> new BlockItemBase(defBlock.get(), new Properties()));
        return defBlock;
    }

    private static <T extends Block> DeferredBlock<T> registerBlastInfoBlock(String name, Supplier<T> block) {
        DeferredBlock<T> defBlock = BLOCKS.register(name, block);
        NtmItems.ITEMS.register(name, () -> new BlastInfoBlockItem(defBlock.get(), new Properties()));
        return defBlock;
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block, Class<? extends BlockItem> clazz, Properties properties) {
        DeferredBlock<T> defBlock = BLOCKS.register(name, block);
        NtmItems.ITEMS.register(name, () -> {
            try {
                return clazz.getConstructor(Block.class, Properties.class).newInstance(defBlock.get(), properties);
            } catch(Exception ignored) {}
            return null;
        });
        return defBlock;
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block, Class<? extends BlockItem> clazz) { return register(name, block, clazz, new Properties()); }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    /** Die Eigenschaften aller fuenf toten Gewaechse: zerbrechlich, ohne Kollision, wie im Original. */
    private static BlockBehaviour.Properties deadPlant() {
        return BlockBehaviour.Properties.of()
                .instabreak()
                .noCollission()
                .noOcclusion()
                .sound(SoundType.GRASS)
                .mapColor(MapColor.PLANT)
                .pushReaction(PushReaction.DESTROY);
    }


    /** Die Eigenschaften aller drei Scheinwerfer; Haerte 0,5 wie im Original. */
    private static BlockBehaviour.Properties spotlight() {
        return BlockBehaviour.Properties.of()
                .strength(0.5F)
                .noCollission()
                .noOcclusion()
                .sound(SoundType.METAL)
                .mapColor(MapColor.NONE)
                .lightLevel(state -> state.getValue(SpotlightBlock.LIT) ? 15 : 0);
    }


    /** Die Eigenschaften der Deko aus den Bauwerken; Werte aus ModBlocks.java:1590 f. */
    private static BlockBehaviour.Properties deco() {
        return BlockBehaviour.Properties.of()
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .mapColor(MapColor.METAL)
                .noOcclusion();
    }

}
