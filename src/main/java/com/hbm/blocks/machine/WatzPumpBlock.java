package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.WatzPumpBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.WatzPump.
 *
 * Der Deckel des Watz-Reaktors. Er tut selbst nichts: er steht oben drauf, sieht so aus, wie er
 * aussieht, und ist zusammen mit einem Redstonesignal die Bedingung dafuer, dass die Saeule
 * darunter laeuft.
 *
 * ABWEICHUNG: das Original macht nur seinen Dummy oben begehbar (isSideSolid bei Metadatenwert
 * 1). Auf 1.21 entscheidet darueber die Kollisionsform des Blocks, die hier durchgehend voll
 * ist -- der ganze Deckel traegt also, nicht nur eine Stelle.
 */
public class WatzPumpBlock extends DummyableBlock {

    public WatzPumpBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCore(state) ? new WatzPumpBlockEntity(pos, state) : null;
    }

    @Override public int[] getDimensions() { return new int[] {1, 0, 0, 0, 0, 0}; }
    @Override public int getOffset() { return 0; }

    public static final MapCodec<WatzPumpBlock> CODEC = simpleCodec(WatzPumpBlock::new);
    @Override public MapCodec<WatzPumpBlock> codec() { return CODEC; }
}
