package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockGrate.
 *
 * Eine zwei Pixel hohe Platte, die sich auf jeder der acht Achtelhoehen im Block
 * absetzen laesst -- wohin genau, entscheidet der Trefferpunkt beim Setzen. Im
 * Original steckt diese Hoehe in den Blockmetadaten; auf 1.21 ist daraus die
 * Blockstate-Eigenschaft "layer" geworden.
 *
 * Die beiden Sonderwerte des Originals sind mit uebernommen: mit gedrueckter
 * Schleichtaste rutscht das Gitter in den Block darunter (9, Hoehe -0.125) oder in den
 * Block darueber (8, Hoehe 1.0) hinein -- aber nur, wenn dieser Nachbar dort ueberhaupt
 * Platz laesst. Wird er spaeter zu einem vollen Block, bricht das Gitter ab.
 *
 * Nicht portiert ist das breite Gitter (steel_grate_wide), durch das Gegenstaende und
 * Erfahrungskugeln fallen -- es steht im Port noch nicht zur Verfuegung und wird hier
 * auch von nichts gebraucht.
 *
 * Ebenfalls weggelassen: das Original meldet den Block als ITooltipProvider an und blendet
 * damit einen Beschreibungstext ein -- fuer steel_grate gibt es in en_US.lang aber gar keinen,
 * dort stuende der unaufgeloeste Schluessel. Der Block bleibt deshalb ohne Tooltip.
 */
public class GrateBlock extends Block {

    /** 0-7 wie im Original, 8 = ragt in den Block darueber, 9 = ragt in den Block darunter. */
    public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, 9);

    public static final MapCodec<GrateBlock> CODEC = simpleCodec(GrateBlock::new);

    public GrateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYER, 0));
    }

    @Override public MapCodec<GrateBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYER);
    }

    /** Entspricht BlockGrate.getY(meta). */
    public static float getY(int layer) {
        if(layer == 9) return -0.125F;
        return layer * 0.125F;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        float fy = getY(state.getValue(LAYER));
        return Shapes.box(0D, fy, 0D, 1D, fy + 0.125D, 1D);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        int layer;

        if(face == Direction.DOWN) {
            layer = 7;
        } else if(face == Direction.UP) {
            layer = 0;
        } else {
            double hitY = context.getClickLocation().y - context.getClickedPos().getY();
            layer = Math.min(7, Math.max(0, (int) Math.floor(hitY * 8D)));
        }

        // Original: onBlockPlacedBy prueft zusaetzlich die Schleichtaste und schiebt das
        // Gitter dann in den Nachbarblock hinein.
        if(context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            // Beim Setzen verlangt das Original zusaetzlich, dass der Nachbar ueberhaupt da ist --
            // in Luft haengt nichts. Beim spaeteren Nachbarwechsel prueft es nur noch den Platz.
            if(layer == 0 && !level.getBlockState(pos.below()).isAir() && hasClearance(level, pos.below(), true)) layer = 9;
            else if(layer == 7 && !level.getBlockState(pos.above()).isAir() && hasClearance(level, pos.above(), false)) layer = 8;
        }

        return this.defaultBlockState().setValue(LAYER, layer);
    }

    /**
     * Der Nachbarblock darf mit seiner Kollisionsbox nicht bis an die gemeinsame Flaeche
     * reichen, sonst steckte das Gitter in ihm drin. Hat er gar keine (Luft, Gras, Fackel),
     * ist Platz -- so rechnet das Original, dort ist die Box dann null.
     */
    private static boolean hasClearance(LevelReader level, BlockPos neighbour, boolean below) {
        VoxelShape shape = level.getBlockState(neighbour).getCollisionShape(level, neighbour);
        if(shape.isEmpty()) return true;

        return below ? shape.max(Direction.Axis.Y) < 0.95D : shape.min(Direction.Axis.Y) > 0.05D;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int layer = state.getValue(LAYER);
        if(layer == 9) return hasClearance(level, pos.below(), true);
        if(layer == 8) return hasClearance(level, pos.above(), false);
        return true;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override protected boolean useShapeForLightOcclusion(BlockState state) { return true; }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }
}
