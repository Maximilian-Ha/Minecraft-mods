package api.hbm.block;

import com.hbm.inventory.material.Mats.MaterialStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: api.hbm.block.ICrucibleAcceptor.
 *
 * Bloecke, die fluessiges Metall annehmen koennen. Zwei Wege fuehren hinein:
 *
 * Giessen -- das Metall verlaesst Rinne oder Tiegel und faellt in aller Regel nach unten. Die
 * zusaetzlichen Koordinaten geben den Auftreffpunkt genauer an, was etwa fuer grosse Tiegel
 * zaehlt, die von oben befuellt werden.
 *
 * Fliessen -- die "sichere" Weitergabe ueber eine Rinne, meist von Block zu Block und meist
 * waagerecht, aber nicht zwingend.
 */
public interface ICrucibleAcceptor {

    boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack);
    MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack);

    boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack);
    MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack);
}
