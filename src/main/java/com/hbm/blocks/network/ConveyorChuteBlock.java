package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorChute.
 *
 * Der Schacht ist das Gegenstueck zum Steigband: er nimmt oben an und laesst nach unten fallen.
 *
 * Solange es unter ihm weitergeht -- ein weiterer Schacht oder eine annehmende Maschine --,
 * faellt der Gegenstand einfach durch, und zwar schnell: das Original gibt ihm dann die
 * fuenffache Schrittweite. Im letzten Stueck des Schachts wird er abgebremst und waagerecht auf
 * die eingestellte Richtung abgesetzt; solange er dabei noch ueber der Bandhoehe liegt, faellt
 * er mit dreifacher Schrittweite.
 */
public class ConveyorChuteBlock extends ConveyorBaseBlock {

    public static final MapCodec<ConveyorChuteBlock> CODEC = simpleCodec(ConveyorChuteBlock::new);

    private static final VoxelShape FULL = Block.box(0D, 0D, 0D, 16D, 16D, 16D);

    public ConveyorChuteBlock(Properties properties) {
        super(properties);
    }

    /** Geht es unter dem Schacht weiter? Dann faellt der Gegenstand einfach durch. */
    private static boolean passesThrough(BlockGetter level, BlockPos pos) {
        Block below = level.getBlockState(pos.below()).getBlock();
        return below instanceof IConveyorBelt || below instanceof IEnterableBlock;
    }

    /** Faellt der Gegenstand hier noch, statt schon abgesetzt zu werden? */
    private static boolean stillFalling(BlockGetter level, BlockPos pos, Vec3 itemPos) {
        return passesThrough(level, pos) || itemPos.y > pos.getY() + 0.25;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {

        if(passesThrough(level, pos)) speed *= 5D;
        else if(itemPos.y > pos.getY() + 0.25) speed *= 3D;

        return super.getTravelLocation(level, pos, itemPos, speed);
    }

    @Override public Direction getInputDirection(Level level, BlockPos pos) { return Direction.UP; }
    @Override public Direction getOutputDirection(Level level, BlockPos pos) { return Direction.DOWN; }

    @Override
    public Direction getTravelDirection(Level level, BlockPos pos, Vec3 itemPos) {
        /* Nach oben zeigen heisst hier: nach unten fallen. */
        return stillFalling(level, pos, itemPos) ? Direction.UP : level.getBlockState(pos).getValue(HORIZONTAL_FACING);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {

        if(!stillFalling(level, pos, itemPos)) return super.getClosestSnappingPosition(level, pos, itemPos);

        return new Vec3(pos.getX() + 0.5, itemPos.y, pos.getZ() + 0.5);
    }

    /** Mit Schleichtaste schliesst sich der Ring: aus dem Schacht wird wieder ein flaches Band. */
    @Override
    protected boolean onScrewSneaking(Level level, Player player, BlockPos pos, BlockState state) {
        level.setBlock(pos, NtmBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING)), 3);
        return true;
    }

    @Override protected MapCodec<? extends ConveyorBaseBlock> codec() { return CODEC; }
}
