package api.hbm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: api.hbm.block.IBlowable.
 *
 * Ein Block, den ein Geblaese anblasen kann. Der Chicago Pile kuehlt sich so.
 */
public interface IBlowable {

    /** Wird serverseitig jeden Tick aufgerufen, solange ein Geblaese in Reichweite blaest. */
    void applyFan(Level level, BlockPos pos, Direction dir, int dist);
}
