package com.hbm.blocks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorBendable.
 *
 * Ein waagerechtes Band, das auch um die Ecke gehen kann. Im Original steckt die Kurve in
 * denselben Metadaten wie die Richtung -- plus 4 heisst Linkskurve, plus 8 Rechtskurve; auf
 * 1.21 ist daraus eine eigene Blockstate-Eigenschaft geworden.
 */
public abstract class ConveyorBendableBlock extends ConveyorBaseBlock {

    public static final EnumProperty<ConveyorShape> SHAPE = EnumProperty.create("shape", ConveyorShape.class);

    public ConveyorBendableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(SHAPE, ConveyorShape.STRAIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, SHAPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(SHAPE, ConveyorShape.STRAIGHT);
    }

    @Override
    public Direction getOutputDirection(Level level, BlockPos pos) {

        BlockState state = level.getBlockState(pos);
        Direction primary = state.getValue(HORIZONTAL_FACING).getOpposite();

        return switch(state.getValue(SHAPE)) {
            case LEFT -> primary.getCounterClockWise();
            case RIGHT -> primary.getClockWise();
            case STRAIGHT -> primary;
        };
    }

    /**
     * In der Kurve haengt die Fahrtrichtung davon ab, wo im Block der Gegenstand gerade ist: im
     * ersten Stueck faehrt er noch geradeaus, hinter dem Scheitel quer. Der Scheitel liegt in
     * der Ecke, um die die Kurve fuehrt; von dort wird der Abstand gemessen.
     */
    @Override
    public Direction getTravelDirection(Level level, BlockPos pos, Vec3 itemPos) {

        BlockState state = level.getBlockState(pos);
        Direction primary = state.getValue(HORIZONTAL_FACING);
        ConveyorShape shape = state.getValue(SHAPE);

        if(shape == ConveyorShape.STRAIGHT) return primary;

        int bend = shape == ConveyorShape.LEFT ? 0 : 1;
        Direction secondary = primary.getClockWise();

        double cornerX = pos.getX() + 0.5 - (-primary.getStepX() * 0.5 + secondary.getStepX() * (0.5 - bend));
        double cornerZ = pos.getZ() + 0.5 - (-primary.getStepZ() * 0.5 + secondary.getStepZ() * (0.5 - bend));

        if(Math.abs(itemPos.x - cornerX) + Math.abs(itemPos.z - cornerZ) >= 1D) {
            return bend == 0 ? secondary.getOpposite() : secondary;
        }

        return primary;
    }

    /** Mit Schleichtaste: gerade, Linkskurve, Rechtskurve, wieder gerade. */
    @Override
    protected boolean onScrewSneaking(Level level, Player player, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(SHAPE, state.getValue(SHAPE).next()), 3);
        return true;
    }

    /** Gerade, Linkskurve, Rechtskurve -- im Original die Metadaten-Stufen 0, +4 und +8. */
    public enum ConveyorShape implements StringRepresentable {
        STRAIGHT("straight"),
        LEFT("left"),
        RIGHT("right");

        private final String name;

        ConveyorShape(String name) {
            this.name = name;
        }

        public ConveyorShape next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        @Override public String getSerializedName() { return this.name; }
    }
}
