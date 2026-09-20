package com.hbm.world.gen;

import com.hbm.blocks.NtmBlocks;
import com.hbm.main.NuclearTechMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Blockwaehler des Meteoritenverlieses.
 *
 * DAS ORIGINAL VERWUERFELT BEIM SETZEN. Jedes Verliesstueck bekommt eine Tabelle
 * (JigsawPiece.blockTable), die bestimmte Bloecke durch einen Waehler ersetzt: wo im Bauwerk
 * ein glatter Meteoritenziegel steht, entsteht in der Welt zu vier Zehnteln ein glatter, zu
 * drei ein bemooster und zu drei ein rissiger. Die Bauwerksdatei kennt diese Streuung nicht --
 * sie steht in Component.MeteorBricks und den drei Geschwisterklassen.
 *
 * IN 1.21 HEISST DAS PROZESSORLISTE. Eine Regel prueft einen Eingangsblock mit einer
 * Wahrscheinlichkeit und tauscht ihn; die ERSTE ZUTREFFENDE REGEL GEWINNT, danach hoert die
 * Liste auf. Deshalb sind die Zahlen unten BEDINGTE Wahrscheinlichkeiten und nicht die des
 * Originals: drei Zehntel fuer den bemoosten, dann drei Siebtel des Rests fuer den rissigen --
 * das sind wieder drei Zehntel -- und was uebrig bleibt, die restlichen vier Zehntel, ist der
 * glatte.
 */
public class NtmProcessorLists {

    /** Nur die Ziegelstreuung. Im Original die Tabelle "bricks". */
    public static final ResourceKey<StructureProcessorList> METEOR_BRICKS = registerKey("meteor_bricks");
    /** Ziegel, Kisten und Krabbennester. Im Original "crates". */
    public static final ResourceKey<StructureProcessorList> METEOR_CRATES = registerKey("meteor_crates");
    /** Ziegel und der gruene Schleim. Im Original "ooze". */
    public static final ResourceKey<StructureProcessorList> METEOR_OOZE = registerKey("meteor_ooze");
    /** Die Spitze folgt dem Gelaende, Spalte fuer Spalte. */
    public static final ResourceKey<StructureProcessorList> METEOR_SPIKE = registerKey("meteor_spike");

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {

        context.register(METEOR_BRICKS, new StructureProcessorList(List.of(
                new RuleProcessor(ziegelregeln()))));

        /* Component.SupplyCrates und Component.CrabSpawners, hinter der Ziegelstreuung. */
        List<ProcessorRule> kisten = ziegelregeln();
        kisten.add(regel(NtmBlocks.CRATE.get(), 0.6F, Blocks.AIR));
        kisten.add(regel(NtmBlocks.CRATE.get(), 0.5F, NtmBlocks.CRATE_AMMO.get()));
        kisten.add(regel(NtmBlocks.CRATE.get(), 0.5F, NtmBlocks.CRATE_CAN.get()));
        kisten.add(regel(NtmBlocks.METEOR_SPAWNER.get(), 0.8F, NtmBlocks.METEOR_BRICK.get()));
        context.register(METEOR_CRATES, new StructureProcessorList(List.of(new RuleProcessor(kisten))));

        /* Component.GreenOoze. In der Bauwerksdatei steht an diesen Stellen gruener Beton;
         * er ueberlebt die Streuung nie -- vier Fuenftel werden Giftblock, ein Fuenftel
         * Meteoritenbau. */
        List<ProcessorRule> schleim = ziegelregeln();
        schleim.add(regel(NtmBlocks.CONCRETE_LIME.get(), 0.8F, NtmBlocks.TOXIC_BLOCK.get()));
        schleim.add(regel(NtmBlocks.CONCRETE_LIME.get(), 1.0F, NtmBlocks.METEOR_POLISHED.get()));
        context.register(METEOR_OOZE, new StructureProcessorList(List.of(new RuleProcessor(schleim))));

        /*
         * DIE SPITZE. Im Original traegt sie conformToTerrain und heightOffset = -3: jede
         * einzelne Spalte wird auf die Gelaendehoehe gesetzt und dann drei Bloecke abgesenkt
         * (NBTStructure.java:639). Der Schwerkraftprozessor von 1.21 tut genau das --
         * Spalte fuer Spalte auf die Hoehenkarte, mit Versatz.
         */
        context.register(METEOR_SPIKE, new StructureProcessorList(List.of(
                new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -3))));
    }

    /**
     * Component.MeteorBricks: vier Zehntel glatt, drei bemoost, drei rissig -- hier als
     * bedingte Kette. Drei Siebtel von den verbliebenen sieben Zehnteln sind wieder drei
     * Zehntel.
     *
     * Die Liste ist veraenderlich, weil zwei der drei Tabellen noch etwas anhaengen.
     */
    private static List<ProcessorRule> ziegelregeln() {
        List<ProcessorRule> regeln = new ArrayList<>();
        regeln.add(regel(NtmBlocks.METEOR_BRICK.get(), 0.3F, NtmBlocks.METEOR_BRICK_MOSSY.get()));
        regeln.add(regel(NtmBlocks.METEOR_BRICK.get(), 3F / 7F, NtmBlocks.METEOR_BRICK_CRACKED.get()));
        return regeln;
    }

    private static ProcessorRule regel(Block eingang, float wahrscheinlichkeit, Block ausgang) {
        return new ProcessorRule(new RandomBlockMatchTest(eingang, wahrscheinlichkeit),
                AlwaysTrueTest.INSTANCE, ausgang.defaultBlockState());
    }

    private static ResourceKey<StructureProcessorList> registerKey(String path) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, NuclearTechMod.withDefaultNamespace(path));
    }
}
