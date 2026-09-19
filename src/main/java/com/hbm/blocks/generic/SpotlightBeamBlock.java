package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.SpotlightBeam.
 *
 * Der Lichtkegel eines Scheinwerfers, Block fuer Block. Er ist unsichtbar, laesst sich nicht
 * anfassen und bringt nur eines mit: volles Licht.
 *
 * Ein Platz kann von mehreren Scheinwerfern zugleich beleuchtet werden. Das Original merkt
 * sich die Herkunftsrichtungen als sechs Bits in einer Blockentitaet; hier stehen dafuer sechs
 * Ja-Nein-Eigenschaften im Blockzustand -- dieselbe Auskunft ohne Blockentitaet. Erst wenn die
 * letzte Richtung wegfaellt, verschwindet der Strahl.
 */
public class SpotlightBeamBlock extends Block {

    public static final MapCodec<SpotlightBeamBlock> CODEC = simpleCodec(SpotlightBeamBlock::new);

    public static final BooleanProperty FROM_DOWN = BooleanProperty.create("from_down");
    public static final BooleanProperty FROM_UP = BooleanProperty.create("from_up");
    public static final BooleanProperty FROM_NORTH = BooleanProperty.create("from_north");
    public static final BooleanProperty FROM_SOUTH = BooleanProperty.create("from_south");
    public static final BooleanProperty FROM_WEST = BooleanProperty.create("from_west");
    public static final BooleanProperty FROM_EAST = BooleanProperty.create("from_east");

    private static final Map<Direction, BooleanProperty> BY_DIRECTION = new EnumMap<>(Direction.class);

    static {
        BY_DIRECTION.put(Direction.DOWN, FROM_DOWN);
        BY_DIRECTION.put(Direction.UP, FROM_UP);
        BY_DIRECTION.put(Direction.NORTH, FROM_NORTH);
        BY_DIRECTION.put(Direction.SOUTH, FROM_SOUTH);
        BY_DIRECTION.put(Direction.WEST, FROM_WEST);
        BY_DIRECTION.put(Direction.EAST, FROM_EAST);
    }

    public static BooleanProperty propertyFor(Direction dir) {
        return BY_DIRECTION.get(dir);
    }

    public SpotlightBeamBlock(Properties properties) {
        super(properties);

        BlockState state = this.stateDefinition.any();
        for(BooleanProperty property : BY_DIRECTION.values()) state = state.setValue(property, false);
        this.registerDefaultState(state);
    }

    @Override public MapCodec<SpotlightBeamBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FROM_DOWN, FROM_UP, FROM_NORTH, FROM_SOUTH, FROM_WEST, FROM_EAST);
    }

    /** Ob dieser Strahl ueberhaupt noch von irgendwoher beleuchtet wird. */
    public static boolean isLit(BlockState state) {
        for(BooleanProperty property : BY_DIRECTION.values()) {
            if(state.getValue(property)) return true;
        }
        return false;
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }

    /** Der Strahl weicht allem: wer hier baut, schneidet ihn ab. */
    @Override public boolean canBeReplaced(BlockState state, BlockPlaceContext context) { return true; }

    /**
     * Faellt in der Bahn ein Hindernis weg, sucht der Strahl von hier aus zurueck zur Quelle
     * und laeuft von dort neu los -- backPropagate im Original.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {

        if(level.isClientSide) return;
        if(neighborBlock instanceof SpotlightBeamBlock) return;

        for(Direction dir : Direction.values()) {
            if(state.getValue(propertyFor(dir))) SpotlightBlock.backPropagate(level, pos, dir);
        }
    }

    /**
     * Wird der Strahl zerstoert -- etwa weil jemand hineinbaut --, erlischt alles dahinter.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!level.isClientSide && !newState.is(this)) {
            for(Direction dir : Direction.values()) {
                if(state.getValue(propertyFor(dir))) SpotlightBlock.unpropagateBeam(level, pos, dir);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
