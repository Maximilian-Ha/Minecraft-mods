package com.hbm.world.feature;

import com.hbm.blocks.NtmBlocks;
import com.hbm.util.LootGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.world.feature.GlyphidHive.
 *
 * Der kleine Bau. Elf mal elf Bloecke, fuenf Schichten hoch, und er steht als Zahlengitter
 * im Quelltext -- genau wie im Original, Zeile fuer Zeile uebernommen.
 *
 * VIER ZAHLEN, VIER BEDEUTUNGEN:
 *   0 -- nichts, hier bleibt die Welt, wie sie ist
 *   1 -- Baufleisch
 *   2 -- mit einem Drittel Wahrscheinlichkeit ein Gelege, sonst Baufleisch
 *   3 -- die Kammer: ein Schaedel, ein Knochenhaufen oder (wenn Beute erlaubt ist) ein
 *        Beutesockel; das Drittel entscheidet der Zufall
 *
 * DAS GITTER STEHT KOPF: der Bauweg liest schematicSmall[4 - j], zaehlt also von oben nach
 * unten. Das ist im Original so und hier uebernommen -- wer die Schichten umdreht, baut ein
 * anderes Nest.
 *
 * DER MITTELPUNKT liegt bei fuenf, fuenf und zwei: der Bau waechst um den Uebergabepunkt
 * herum und zwei Bloecke tief.
 *
 * WARUM LevelAccessor UND NICHT Level: beim Setzen durch den Weltgenerator laeuft das hier
 * auf dem worldgen-Faden, und der hat nur einen WorldGenLevel. Wer sich von dort den echten
 * ServerLevel holt und darauf schreibt, loest Chunk-Ladungen aus, die auf denselben Faden
 * warten -- der Server haengt dann in der Vorbereitung des Startgebiets und kommt nie
 * heraus. Genau das ist in CI 493 passiert: keine Ausnahme, nur Stillstand bei 34 Prozent.
 * LevelAccessor deckt beide Faelle ab, den Weltgenerator und den Spaeher zur Laufzeit.
 */
public class GlyphidHive {

    public static final int[][][] SCHEMA_KLEIN = new int[][][] {
        {
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
        },
        {
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,1,1,1,1,1,0,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,0,1,1,1,1,1,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
        },
        {
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,1,1,1,3,3,3,1,1,1,0},
            {0,1,1,1,3,3,3,1,1,1,0},
            {0,1,1,1,3,3,3,1,1,1,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
        },
        {
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,2,2,2,1,1,0,0},
            {0,1,1,2,2,2,2,2,1,1,0},
            {0,1,1,2,2,2,2,2,1,1,0},
            {0,1,1,2,2,2,2,2,1,1,0},
            {0,0,1,1,2,2,2,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
        },
        {
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,1,1,1,1,1,1,1,1,1,0},
            {0,1,1,1,1,1,1,1,1,1,0},
            {0,1,1,1,1,1,1,1,1,1,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,0,0,0,1,1,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
        }
    };

    /**
     * Setzt einen kleinen Bau um die uebergebene Stelle.
     *
     * @param verseucht waehlt die verseuchte Bauart statt der gewoehnlichen
     * @param beute erlaubt den Beutesockel in der Kammer; ist sie false, steht dort
     *              stattdessen Baufleisch
     */
    public static void generateSmall(LevelAccessor level, BlockPos mitte, RandomSource zufall, boolean verseucht, boolean beute) {

        Block fleisch = verseucht ? NtmBlocks.GLYPHID_BASE_INFESTED.get() : NtmBlocks.GLYPHID_BASE.get();
        Block gelege = verseucht ? NtmBlocks.GLYPHID_SPAWNER_INFESTED.get() : NtmBlocks.GLYPHID_SPAWNER.get();

        for(int i = 0; i < 11; i++) {
            for(int j = 0; j < 5; j++) {
                for(int k = 0; k < 11; k++) {

                    int art = SCHEMA_KLEIN[4 - j][i][k];
                    BlockPos stelle = mitte.offset(i - 5, j - 2, k - 5);

                    switch(art) {

                        case 1 -> level.setBlock(stelle, fleisch.defaultBlockState(), 2);

                        case 2 -> level.setBlock(stelle,
                                (zufall.nextInt(3) == 0 ? gelege : fleisch).defaultBlockState(), 2);

                        case 3 -> kammer(level, stelle, zufall, fleisch, beute);

                        default -> { }
                    }
                }
            }
        }
    }

    /** Ein Feld der Kammer: Schaedel, Knochen oder Beute -- je ein Drittel. */
    private static void kammer(LevelAccessor level, BlockPos stelle, RandomSource zufall, Block fleisch, boolean beute) {

        int wurf = zufall.nextInt(3);

        if(wurf == 0) {
            /* Das Original setzt Blocks.skull mit Metawert 1 -- den stehenden Schaedel --
             * und dreht ihn auf eine von sechzehn Richtungen. */
            BlockState schaedel = Blocks.SKELETON_SKULL.defaultBlockState()
                    .setValue(SkullBlock.ROTATION, zufall.nextInt(16));
            /* Flagge 2 statt der 3 des Originals: die 3 benachrichtigt die Nachbarn, und
             * das ist waehrend der Weltgenerierung derselbe Fallstrick wie oben -- es kann
             * weitere Chunks nachladen. Der Blockinhalt entsteht auch mit der 2. */
            level.setBlock(stelle, schaedel, 2);
            return;
        }

        if(wurf == 1) {
            level.setBlock(stelle, NtmBlocks.DECO_LOOT.get().defaultBlockState(), 2);
            LootGenerator.applyLoot(level, stelle, LootGenerator.LOOT_BONES);
            return;
        }

        if(!beute) {
            level.setBlock(stelle, fleisch.defaultBlockState(), 2);
            return;
        }

        level.setBlock(stelle, NtmBlocks.DECO_LOOT.get().defaultBlockState(), 2);
        LootGenerator.applyLoot(level, stelle, LootGenerator.LOOT_GLYPHID_HIVE);
    }
}
