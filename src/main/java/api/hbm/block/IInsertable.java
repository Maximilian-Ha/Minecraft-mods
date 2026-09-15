package api.hbm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: api.hbm.block.IInsertable.
 *
 * Ein Block, in den sich von aussen ein Gegenstand schieben laesst, ohne dass er ein Inventar
 * nach aussen zeigt. Der Chicago Pile nimmt so seine Brennstoffkassetten auf.
 */
public interface IInsertable {

    /**
     * @param dir Richtung, aus der eingeschoben wird
     * @return TRUE, wenn der Stapel angenommen wurde
     */
    boolean insertItem(Level level, BlockPos pos, Direction dir, ItemStack stack);
}
