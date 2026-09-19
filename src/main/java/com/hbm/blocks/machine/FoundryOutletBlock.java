package com.hbm.blocks.machine;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FoundryOutletBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ScrapsItem;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundryOutlet.
 *
 * Der Ausguss. Er sitzt am Rand des Blocks, und zwar auf der Seite, von der er beliefert wird
 * -- FACING zeigt von ihm weg, genau wie das Metadatum des Originals.
 *
 * ABWEICHUNG: Filter und Riegel stehen im Blockzustand. Das Original liest sie beim Zeichnen
 * aus der Blockentitaet, was auf 1.21 einen eigenen Renderer braeuchte; als Zustand kennt sie
 * das Modell unmittelbar, so wie die Rinne ihre vier Anschluesse kennt.
 */
public class FoundryOutletBlock extends BaseEntityBlock implements ICrucibleAcceptor, ILookOverlay, IToolable {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Ob ein Materialfilter gesetzt ist -- nur fuers Modell. */
    public static final BooleanProperty FILTERED = BooleanProperty.create("filtered");

    /** Ob der Riegel vorliegt -- nur fuers Modell. */
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    public static final MapCodec<FoundryOutletBlock> CODEC = simpleCodec(FoundryOutletBlock::new);

    /** Der Trog liegt auf der Seite, die FACING entgegengesetzt ist. Masse aus dem Original. */
    private static final Map<Direction, VoxelShape> SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.NORTH, Block.box(5, 0, 10, 11, 8, 16));
        map.put(Direction.SOUTH, Block.box(5, 0, 0, 11, 8, 6));
        map.put(Direction.WEST, Block.box(10, 0, 5, 16, 8, 11));
        map.put(Direction.EAST, Block.box(0, 0, 5, 6, 8, 11));
    });

    public FoundryOutletBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FILTERED, Boolean.FALSE)
                .setValue(CLOSED, Boolean.FALSE));
    }

    /* Platzhalter, weil der Schlackenabstich von dieser Klasse erbt: MapCodec ist invariant,
     * eine Ableitung koennte den festen Typ nicht verengen. */
    @Override public MapCodec<? extends FoundryOutletBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FILTERED, CLOSED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryOutletBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** Die Vorderseite zeigt zum Spieler, also weg vom Trog -- wie im Original. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * Haelt FILTERED und CLOSED an dem, was die Blockentitaet sagt. Aufgerufen wird das von
     * jeder Stelle, die eines von beiden aendern kann: Rechtsklick, Werkzeug, Nachbarwechsel
     * und das Einfuegen mit dem Aufsatzwerkzeug.
     */
    public static void uebernimmZustand(Level level, BlockPos pos, FoundryOutletBlockEntity ausguss) {

        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof FoundryOutletBlock)) return;

        BlockState neu = state
                .setValue(FILTERED, ausguss.filter != null)
                .setValue(CLOSED, ausguss.isClosed());

        if(neu != state) level.setBlock(pos, neu, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if(!level.isClientSide && level.getBlockEntity(pos) instanceof FoundryOutletBlockEntity ausguss) {
            uebernimmZustand(level, pos, ausguss);
        }
    }

    /**
     * Ein Schrottstueck in der Hand setzt den Filter auf dessen Material, alles andere kehrt
     * das Redstoneverhalten um. Beides aus dem Original.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;
        if(!(level.getBlockEntity(pos) instanceof FoundryOutletBlockEntity ausguss)) return InteractionResult.FAIL;

        ItemStack inHand = player.getMainHandItem();

        if(inHand.getItem() instanceof ScrapsItem) {
            MaterialStack mat = ScrapsItem.getMats(inHand);
            if(mat != null) ausguss.filter = mat.material;
        } else {
            ausguss.invertRedstone = !ausguss.invertRedstone;
        }

        ausguss.setChanged();
        uebernimmZustand(level, pos, ausguss);

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER && tool != ToolType.HAND_DRILL) return false;
        if(level.isClientSide) return true;
        if(!(level.getBlockEntity(pos) instanceof FoundryOutletBlockEntity ausguss)) return false;

        if(tool == ToolType.SCREWDRIVER) {
            ausguss.filter = null;
            ausguss.invertFilter = false;
        } else {
            ausguss.invertFilter = !ausguss.invertFilter;
        }

        ausguss.setChanged();
        uebernimmZustand(level, pos, ausguss);

        return true;
    }

    private static @Nullable ICrucibleAcceptor acceptor(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acc ? acc : null;
    }

    /* Gegossen wird in den Ausguss nicht -- er nimmt nur seitlichen Zulauf. */
    @Override public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) { return stack; }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc != null && acc.canAcceptPartialFlow(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc == null ? stack : acc.flow(level, pos, side, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof FoundryOutletBlockEntity ausguss)) return;

        List<Component> text = new ArrayList<>();

        if(ausguss.filter != null) {
            text.add(Component.translatable("foundry.filter", ausguss.filter.getName()).withStyle(ChatFormatting.YELLOW));
        }
        if(ausguss.invertFilter) {
            text.add(Component.translatable("foundry.invertFilter").withStyle(ChatFormatting.YELLOW));
        }
        if(ausguss.invertRedstone) {
            text.add(Component.translatable("foundry.inverted").withStyle(ChatFormatting.DARK_RED));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xFF4000, 0x401000, text);
    }
}
