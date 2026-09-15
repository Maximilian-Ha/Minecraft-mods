package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorLift.
 *
 * Das Steigband traegt Gegenstaende senkrecht nach oben. Es steht immer im Stapel: unten nimmt
 * es an, oben gibt es ab.
 *
 * Es weiss selbst, wo im Stapel es steht, und leitet das aus seinen beiden Nachbarn ab. Steht
 * ueber ihm kein weiteres Band und auch keine annehmende Maschine, ist es das oberste Stueck --
 * dort wird der Gegenstand waagerecht auf die eingestellte Richtung abgesetzt und die obere
 * Haelfte des Blocks bleibt frei. Ueberall sonst faehrt er einfach weiter senkrecht.
 *
 * Genau darum haengt die Umrissform am Nachbarn: das oberste Stueck ist einen halben Block hoch,
 * jedes andere ein ganzer.
 *
 * ABWEICHUNG: das Original liest die Nachbarn bei jeder Abfrage neu. Auf 1.21 steht das Ergebnis
 * in der Blockstate-Eigenschaft TOP und wird nachgefuehrt, wenn sich oben oder unten etwas
 * aendert. Anders ginge es nicht: Modell und Umriss werden nach dem Blockstate gewaehlt, und ein
 * halb hohes Stueck, das wie ein ganzes aussieht, waere sichtbar falsch.
 */
public class ConveyorLiftBlock extends ConveyorBaseBlock {

    public static final MapCodec<ConveyorLiftBlock> CODEC = simpleCodec(ConveyorLiftBlock::new);

    /** Ist dies das oberste Stueck des Stapels? */
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    private static final VoxelShape FULL = Block.box(0D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape HALF = Block.box(0D, 0D, 0D, 16D, 8D, 16D);

    public ConveyorLiftBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, TOP);
    }

    /**
     * Das oberste Stueck des Stapels. Wer ganz unten steht, ist es nicht -- ein einzelnes
     * Steigband ohne Nachbarn ist der Anfang einer Strecke, nicht ihr Ende.
     */
    private static boolean isTopOfStack(BlockGetter level, BlockPos pos) {

        if(!(level.getBlockState(pos.below()).getBlock() instanceof IConveyorBelt)) return false;

        Block above = level.getBlockState(pos.above()).getBlock();
        return !(above instanceof IConveyorBelt) && !(above instanceof IEnterableBlock);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(TOP, isTopOfStack(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {

        if(direction.getAxis() != Direction.Axis.Y) return state;

        return state.setValue(TOP, isTopOfStack(level, pos));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TOP) ? HALF : FULL;
    }

    @Override public Direction getInputDirection(Level level, BlockPos pos) { return Direction.DOWN; }
    @Override public Direction getOutputDirection(Level level, BlockPos pos) { return Direction.UP; }

    @Override
    public Direction getTravelDirection(Level level, BlockPos pos, Vec3 itemPos) {
        /* Nach unten zeigen heisst hier: nach oben fahren -- gefahren wird der Richtung entgegen. */
        BlockState state = level.getBlockState(pos);
        return state.getValue(TOP) ? state.getValue(HORIZONTAL_FACING) : Direction.DOWN;
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {

        if(level.getBlockState(pos).getValue(TOP)) return super.getClosestSnappingPosition(level, pos, itemPos);

        /* Mitten im Stapel bleibt der Gegenstand auf der Achse und behaelt seine Hoehe. */
        return new Vec3(pos.getX() + 0.5, itemPos.y, pos.getZ() + 0.5);
    }

    /** Mit Schleichtaste wird aus dem Steigband der Schacht. */
    @Override
    protected boolean onScrewSneaking(Level level, Player player, BlockPos pos, BlockState state) {
        level.setBlock(pos, NtmBlocks.CONVEYOR_CHUTE.get().defaultBlockState()
                .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING)), 3);
        return true;
    }

    @Override protected MapCodec<? extends ConveyorBaseBlock> codec() { return CODEC; }
}
