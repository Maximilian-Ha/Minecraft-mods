package com.hbm.blocks.machine.pile;

import api.hbm.block.IToolable;
import com.hbm.blockentity.machine.pile.PileBaseBlockEntity;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity.PileOrientation;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.FlammableBlock;
import com.hbm.blocks.states.PileBlockType;
import com.hbm.particle.helper.MarkerCreator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.pile.BlockPileBrick.
 *
 * Der Graphitziegel. Man mauert einen Quader daraus und setzt die Handbohrmaschine an die Seite,
 * die spaeter die Vorderseite sein soll -- der angebohrte Ziegel wird dann zum Kern, und von ihm
 * aus wird der ganze Quader vermessen und in einen Reaktor verwandelt.
 *
 * Der Quader muss in jeder Richtung mindestens fuenf und hoechstens fuenfzehn Bloecke messen,
 * und der Kern darf nicht an einer Kante sitzen -- sonst waere die Vorderseite nicht eindeutig.
 * Passt etwas nicht, wird die schuldige Stelle rot eingerahmt.
 */
public class PileBrickBlock extends FlammableBlock implements IToolable {

    public static final int MIN_V_SIZE = 5;
    public static final int MIN_H_SIZE = 5;
    public static final int MAX_V_SIZE = 15;
    public static final int MAX_H_SIZE = 15;

    public PileBrickBlock(Properties properties) {
        super(properties, 30, 5);
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.HAND_DRILL) return false;
        /* Von oben oder unten laesst sich keine Vorderseite bestimmen. */
        if(direction.getAxis() == Direction.Axis.Y) return false;
        if(level.isClientSide) return true;

        Direction dir = direction.getOpposite();
        Direction dirLeft = dir.getCounterClockWise();

        int posHeight = 0;
        int negHeight = 0;
        int left = 0;
        int right = 0;
        int depth = 0;

        /* Erst abmessen, wie weit der Quader in jede Richtung reicht. */
        for(int i = 1; i <= MAX_V_SIZE - 1; i++)              { if(!this.isBrick(level, pos.above(i))) break; posHeight = i; }
        for(int i = 1; i <= MAX_V_SIZE - posHeight - 1; i++)  { if(!this.isBrick(level, pos.below(i))) break; negHeight = i; }
        for(int i = 1; i <= MAX_H_SIZE - 1; i++)              { if(!this.isBrick(level, pos.relative(dirLeft, i))) break; left = i; }
        for(int i = 1; i <= MAX_H_SIZE - left - 1; i++)       { if(!this.isBrick(level, pos.relative(dirLeft.getOpposite(), i))) break; right = i; }
        for(int i = 1; i <= MAX_H_SIZE; i++)                  { if(!this.isBrick(level, pos.relative(dir, i))) break; depth = i; }

        /* Dann pruefen, ob das reicht. */
        if(posHeight + negHeight + 1 < MIN_V_SIZE) {
            this.error(player, pos.above(posHeight), "Height too low (<" + MIN_V_SIZE + ")");
            this.error(player, pos.below(negHeight), "Height too low (<" + MIN_V_SIZE + ")");
            return true;
        }

        if(left + right + 1 < MIN_H_SIZE) {
            this.error(player, pos.relative(dirLeft, left), "Width too low (<" + MIN_H_SIZE + ")");
            this.error(player, pos.relative(dirLeft.getOpposite(), right), "Width too low (<" + MIN_H_SIZE + ")");
            return true;
        }

        if(depth + 1 < MIN_H_SIZE) {
            this.error(player, pos.relative(dir, depth), "Depth too low (<" + MIN_H_SIZE + ")");
            return true;
        }

        if(posHeight == 0 || negHeight == 0 || left == 0 || right == 0) {
            this.error(player, pos, "Core cannot be on an edge");
            return true;
        }

        /* Und ob der Quader auch wirklich voll ist. */
        for(int h = -negHeight; h <= posHeight; h++) {
            for(int v = -left; v <= right; v++) {
                for(int d = 0; d <= depth; d++) {
                    BlockPos iPos = this.offset(pos, dir, dirLeft, h, v, d);
                    if(!this.isBrick(level, iPos)) {
                        this.error(player, iPos, "Graphite block missing");
                        return true;
                    }
                }
            }
        }

        /* Alles gut -- bauen. */
        for(int h = -negHeight; h <= posHeight; h++) {
            for(int v = -left; v <= right; v++) {
                for(int d = 0; d <= depth; d++) {

                    BlockPos iPos = this.offset(pos, dir, dirLeft, h, v, d);

                    if(iPos.equals(pos)) {

                        level.setBlock(iPos, NtmBlocks.PILE_BLOCK.get().defaultBlockState().setValue(PileBlock.TYPE, PileBlockType.CORE), 3);

                        if(level.getBlockEntity(iPos) instanceof PileCoreBlockEntity core) {
                            core.orientation = PileOrientation.getOrientation(dir);
                            core.setupSize(posHeight, negHeight, left, right, depth + 1);
                            core.setChanged();
                        }

                    } else {

                        /*
                         * Kante ist, was in mindestens zwei Achsen aussen liegt. Nur dort darf
                         * nicht gebohrt werden -- eine einzelne Aussenflaeche ist in Ordnung,
                         * sonst gaebe es keine Ein- und Auslaesse.
                         */
                        int edgeCount = 0;
                        if(h == -negHeight || h == posHeight) edgeCount++;
                        if(v == -left || v == right) edgeCount++;
                        if(d == 0 || d == depth) edgeCount++;

                        PileBlockType type = edgeCount > 1 ? PileBlockType.EDGE : PileBlockType.DUMMY;
                        level.setBlock(iPos, NtmBlocks.PILE_BLOCK.get().defaultBlockState().setValue(PileBlock.TYPE, type), 3);

                        if(level.getBlockEntity(iPos) instanceof PileBaseBlockEntity pile) pile.setCore(pos);
                    }
                }
            }
        }

        return true;
    }

    private BlockPos offset(BlockPos pos, Direction dir, Direction dirLeft, int h, int v, int d) {
        return pos.offset(
                -dirLeft.getStepX() * v + dir.getStepX() * d,
                h,
                -dirLeft.getStepZ() * v + dir.getStepZ() * d);
    }

    private boolean isBrick(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(this);
    }

    private void error(Player player, BlockPos pos, String message) {
        if(player instanceof ServerPlayer serverPlayer) MarkerCreator.sendError(serverPlayer, pos, Component.literal(message));
    }
}
