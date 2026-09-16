package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.blocks.InfoPanelBlock;
import com.zuxelus.energycontrol.blocks.InfoPanelExtenderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wie aus einer Tafel und ihren Erweiterungen ein Schirm wird.
 *
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.ScreenManager (rund 430 Zeilen).
 * Das Original verwaltete die Schirme in einer Liste je Welt und musste sie beim Setzen,
 * Abbauen und Laden von Hand nachfuehren -- der haeufigste Fehlerort des ganzen Mods.
 *
 * Hier wird nichts verwaltet. Die Tafel rechnet ihre Flaeche in ihrem eigenen Takt neu aus;
 * eine Suche ueber hoechstens vierundsechzig Bloecke einmal je Sekunde kostet nichts, und
 * dafuer gibt es keinen Zustand, der veralten kann.
 *
 * Regeln, die sich aus dem Original ergeben:
 *
 * - Zum Schirm gehoeren nur Bloecke in derselben Ebene mit derselben Blickrichtung.
 * - Der Schirm muss ein volles Rechteck sein. Eine Erweiterung, die ueber die Kante
 *   hinausragt, macht aus der Tafel wieder einen Einzelblock.
 * - Zu einem Schirm gehoert genau **eine** Tafel. Reichen zwei Tafeln ueber dieselben
 *   Erweiterungen, gibt jede ihren Anspruch auf -- sonst schrieben beide uebereinander.
 */
public final class PanelScreens {

    /** Acht mal acht. Darueber lohnt weder die Suche noch die Schrift. */
    public static final int MAX_BLOCKS = 64;

    private PanelScreens() { }

    /** Die Flaeche eines Schirms, beide Ecken einschliesslich. */
    public record Screen(BlockPos min, BlockPos max) {

        public static Screen single(BlockPos pos) {
            return new Screen(pos, pos);
        }

        public boolean isSingle() {
            return min.equals(max);
        }
    }

    /** Die vier Richtungen, in denen ein Schirm mit dieser Blickrichtung waechst. */
    public static Direction[] plane(Direction facing) {
        List<Direction> result = new ArrayList<>(4);
        for(Direction dir : Direction.values()) {
            if(dir.getAxis() != facing.getAxis()) result.add(dir);
        }
        return result.toArray(new Direction[0]);
    }

    private static boolean isExtender(BlockState state, Direction facing) {
        return state.getBlock() instanceof InfoPanelExtenderBlock
                && state.getValue(InfoPanelExtenderBlock.FACING) == facing;
    }

    private static boolean isPanel(BlockState state, Direction facing) {
        return state.getBlock() instanceof InfoPanelBlock
                && state.getValue(InfoPanelBlock.FACING) == facing;
    }

    /**
     * Die Flaeche, die diese Tafel bespielt. Findet sich keine gueltige, bleibt es beim
     * einzelnen Block -- nie bei einem halben Rechteck.
     */
    public static Screen around(Level level, BlockPos panel, Direction facing) {
        if(level == null) return Screen.single(panel);

        Set<BlockPos> region = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Direction[] plane = plane(facing);

        region.add(panel);
        queue.add(panel);
        boolean sharedWithOtherPanel = false;

        while(!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for(Direction dir : plane) {
                BlockPos next = current.relative(dir);
                if(region.contains(next)) continue;
                if(!level.isLoaded(next)) continue;

                BlockState state = level.getBlockState(next);
                if(isPanel(state, facing)) {
                    sharedWithOtherPanel = true;
                    continue;
                }
                if(!isExtender(state, facing)) continue;

                region.add(next);
                if(region.size() > MAX_BLOCKS) return Screen.single(panel);
                queue.add(next);
            }
        }

        if(sharedWithOtherPanel || region.size() == 1) return Screen.single(panel);

        BlockPos min = panel;
        BlockPos max = panel;
        for(BlockPos pos : region) {
            min = new BlockPos(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
            max = new BlockPos(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
        }

        int area = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        if(area != region.size()) return Screen.single(panel);

        return new Screen(min, max);
    }

    /**
     * Die Tafel, zu der eine Erweiterung gehoert -- fuer den Klick auf eine Erweiterung.
     * Beruehren mehrere Tafeln dieselbe Flaeche, gewinnt die mit der kleinsten Position;
     * dieselbe Flaeche gibt dann zwar keinen gemeinsamen Schirm, aber der Klick soll trotzdem
     * eine Oberflaeche oeffnen.
     */
    public static BlockPos findPanel(BlockGetter level, BlockPos extender, Direction facing) {
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Direction[] plane = plane(facing);

        seen.add(extender);
        queue.add(extender);
        BlockPos best = null;

        while(!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for(Direction dir : plane) {
                BlockPos next = current.relative(dir);
                if(!seen.add(next)) continue;

                BlockState state = level.getBlockState(next);
                if(isPanel(state, facing)) {
                    if(best == null || next.asLong() < best.asLong()) best = next;
                    continue;
                }
                if(!isExtender(state, facing)) continue;

                if(seen.size() > MAX_BLOCKS) return best;
                queue.add(next);
            }
        }

        return best;
    }
}
