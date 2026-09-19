package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.world.feature.HugeMush.
 *
 * Kein Weltgenerator im Sinne von 1.21, sondern genau das, was das Original war: eine
 * Handvoll geschachtelter Schleifen, die den Riesenpilz Lage fuer Lage hinsetzen. Das
 * Original ruft sie ebenfalls unmittelbar auf, nicht ueber die Weltgenerierung.
 */
public class HugeMush {

    private HugeMush() { }

    public static void wachse(Level level, BlockPos wurzel) {

        BlockState hut = NtmBlocks.MUSH_BLOCK.get().defaultBlockState();
        BlockState stiel = NtmBlocks.MUSH_BLOCK_STEM.get().defaultBlockState();

        lage(level, wurzel, hut, 1, 0);
        lage(level, wurzel, hut, 1, 3);
        lage(level, wurzel, hut, 2, 5);
        lage(level, wurzel, hut, 4, 6);
        lage(level, wurzel, hut, 4, 7);
        lage(level, wurzel, hut, 4, 8);
        lage(level, wurzel, hut, 3, 9);
        lage(level, wurzel, hut, 1, 10);

        for(int i = 0; i < 8; i++) {
            level.setBlock(wurzel.above(i), stiel, 3);
        }
    }

    /** Eine quadratische Lage mit der Kantenlaenge 2*rand+1 in der Hoehe hoch. */
    private static void lage(Level level, BlockPos wurzel, BlockState block, int rand, int hoch) {
        for(int x = -rand; x <= rand; x++) {
            for(int z = -rand; z <= rand; z++) {
                level.setBlock(wurzel.offset(x, hoch, z), block, 3);
            }
        }
    }
}
