package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.Spotlight und SpotlightModular.
 *
 * Ein Scheinwerfer haengt an einer Wand, einer Decke oder einem Boden und wirft einen
 * Lichtkegel aus Strahlbloecken vor sich her -- je nach Bauart zwei, acht oder zweiunddreissig
 * Bloecke weit.
 *
 * Zwei Eigenheiten des Originals sind uebernommen:
 *  - Das Rotsteinsignal schaltet ihn AUS, nicht ein. Im Original sind das zwei Bloecke
 *    (spotlight_x und spotlight_x_off); im Port ist es die Eigenschaft LIT, wie beim
 *    elektrischen Ofen.
 *  - Eine zerschossene Lampe (BROKEN) bleibt dunkel, bis jemand sie mit einem Rechtsklick
 *    ersetzt. Das Original repariert dabei gleich alle anliegenden Lampen mit.
 */
public class SpotlightBlock extends Block {

    public static final MapCodec<SpotlightBlock> CODEC = simpleCodec(properties -> new SpotlightBlock(properties, 2, Bauart.GLUEHBIRNE));

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    /** Die drei Bauarten des Originals, mit ihren halben Abmessungen. */
    public enum Bauart {
        GLUEHBIRNE(0.25F, 0.2F, 0.15F),
        LEUCHTSTOFF(0.5F, 0.5F, 0.1F),
        HALOGEN(0.35F, 0.25F, 0.2F);

        final float x, y, z;

        Bauart(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final int beamLength;
    private final Bauart bauart;

    public SpotlightBlock(Properties properties, int beamLength, Bauart bauart) {
        super(properties);
        this.beamLength = beamLength;
        this.bauart = bauart;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, true)
                .setValue(BROKEN, false));
    }

    @Override public MapCodec<SpotlightBlock> codec() { return CODEC; }

    public int getBeamLength() { return this.beamLength; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, BROKEN);
    }

    /**
     * Die Abmessungen des Originals werden je nach Richtung durchgetauscht (swizzleBounds) und
     * dann an die Wand geschoben, an der die Lampe haengt.
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        Direction dir = state.getValue(FACING);

        float hx = this.bauart.x, hy = this.bauart.y, hz = this.bauart.z;

        float[] halb = switch(dir.getAxis()) {
            case X -> new float[] { hz, hy, hx };
            case Y -> new float[] { hy, hz, hx };
            case Z -> new float[] { hx, hy, hz };
        };

        double mx = 0.5D - dir.getStepX() * (0.5D - halb[0]);
        double my = 0.5D - dir.getStepY() * (0.5D - halb[1]);
        double mz = 0.5D - dir.getStepZ() * (0.5D - halb[2]);

        return Block.box((mx - halb[0]) * 16, (my - halb[1]) * 16, (mz - halb[2]) * 16,
                         (mx + halb[0]) * 16, (my + halb[1]) * 16, (mz + halb[2]) * 16);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    /** Die Lampe braucht eine feste Flaeche im Ruecken. */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction dir = state.getValue(FACING);
        BlockPos support = pos.relative(dir.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, dir);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if(!level.isClientSide) this.pruefeStrom(state, level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {

        if(level.isClientSide) return;
        if(neighborBlock instanceof SpotlightBeamBlock) return;

        if(!this.canSurvive(state, level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
            return;
        }

        this.pruefeStrom(state, level, pos);
    }

    /**
     * Rotstein schaltet die Lampe AUS. Das Original wartet dafuer vier Ticks, damit ein kurzer
     * Impuls sie nicht sofort ausknipst; das ist hier genauso.
     */
    private void pruefeStrom(BlockState state, Level level, BlockPos pos) {

        if(state.getValue(BROKEN)) return;

        boolean strom = level.hasNeighborSignal(pos);

        if(state.getValue(LIT) && strom) {
            level.scheduleTick(pos, this, 4);
            return;
        }

        if(!state.getValue(LIT) && !strom) {
            level.setBlock(pos, state.setValue(LIT, true), 2);
            this.strahlSetzen(level, pos, state.getValue(FACING));
            return;
        }

        if(state.getValue(LIT)) this.strahlSetzen(level, pos, state.getValue(FACING));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if(state.getValue(LIT) && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(LIT, false), 2);
            unpropagateBeam(level, pos, state.getValue(FACING));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!level.isClientSide && !newState.is(this)) {
            unpropagateBeam(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Eine zerschossene Lampe laesst sich mit der Hand wieder in Gang setzen. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {

        if(!state.getValue(BROKEN)) return InteractionResult.PASS;
        if(level.isClientSide) return InteractionResult.SUCCESS;

        this.reparieren(level, pos);
        return InteractionResult.CONSUME;
    }

    /** Wie im Original: der Rechtsklick nimmt alle anliegenden Lampen gleich mit. */
    private void reparieren(Level level, BlockPos pos) {

        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof SpotlightBlock) || !state.getValue(BROKEN)) return;

        level.setBlock(pos, state.setValue(BROKEN, false).setValue(LIT, true), 2);
        this.pruefeStrom(level.getBlockState(pos), level, pos);

        for(Direction dir : Direction.values()) this.reparieren(level, pos.relative(dir));
    }

    private void strahlSetzen(Level level, BlockPos pos, Direction dir) {
        if(!level.getBlockState(pos).getValue(LIT)) return;
        propagateBeam(level, pos, dir, this.beamLength);
    }

    /**
     * Setzt den Lichtkegel Block fuer Block, bis die Reichweite aufgebraucht ist oder etwas im
     * Weg steht. Trifft er auf einen fremden Strahl, vermerkt er dort nur seine Richtung.
     */
    public static void propagateBeam(Level level, BlockPos pos, Direction dir, int distance) {

        distance--;
        if(distance <= 0) return;

        BlockPos next = pos.relative(dir);
        BlockState state = level.getBlockState(next);

        if(state.getBlock() instanceof SpotlightBeamBlock) {
            level.setBlock(next, state.setValue(SpotlightBeamBlock.propertyFor(dir), true), 2);
        } else if(state.isAir()) {
            level.setBlock(next, NtmBlocks.SPOTLIGHT_BEAM.get().defaultBlockState()
                    .setValue(SpotlightBeamBlock.propertyFor(dir), true), 2);
        } else {
            return;
        }

        propagateBeam(level, next, dir, distance);
    }

    /**
     * Nimmt den Lichtkegel zurueck. Ein Strahl verschwindet erst, wenn ihn keine Richtung mehr
     * beleuchtet -- er kann von mehreren Lampen zugleich getroffen werden.
     */
    public static void unpropagateBeam(Level level, BlockPos pos, Direction dir) {

        BlockPos next = pos.relative(dir);
        BlockState state = level.getBlockState(next);

        if(!(state.getBlock() instanceof SpotlightBeamBlock)) return;

        BlockState ohne = state.setValue(SpotlightBeamBlock.propertyFor(dir), false);

        if(SpotlightBeamBlock.isLit(ohne)) {
            level.setBlock(next, ohne, 2);
        } else {
            level.setBlock(next, Blocks.AIR.defaultBlockState(), 2);
        }

        unpropagateBeam(level, next, dir);
    }

    /**
     * Geht den Strahl rueckwaerts bis zur Lampe und laesst sie neu leuchten -- gebraucht,
     * wenn ein Hindernis aus der Bahn verschwindet.
     */
    public static void backPropagate(Level level, BlockPos pos, Direction dir) {

        BlockPos previous = pos.relative(dir.getOpposite());
        BlockState state = level.getBlockState(previous);

        if(state.getBlock() instanceof SpotlightBlock spotlight) {
            if(state.getValue(LIT)) propagateBeam(level, previous, dir, spotlight.getBeamLength());
            return;
        }

        if(!(state.getBlock() instanceof SpotlightBeamBlock)) return;

        backPropagate(level, previous, dir);
    }
}
