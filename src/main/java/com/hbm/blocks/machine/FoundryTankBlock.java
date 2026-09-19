package com.hbm.blocks.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FoundryTankBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ScrapsItem;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundryTank.
 *
 * Der Lagerbehaelter der Giesserei. Mehrere nebeneinander laufen ineinander: die Wand
 * dazwischen faellt weg, und die Schmelze steht in allen gleich hoch.
 *
 * ABWEICHUNG: das Original liest die Nachbarschaft beim Zeichnen und setzt daraus seine
 * Flaechen zusammen. Auf 1.21 steht sie im Blockzustand -- sechs Anschluesse und dazu vier
 * Merker, ob seitlich ein Ausguss haengt, der in diesen Tank zeigt. Zusammen tausend
 * Zustaende; die Zustandsdatei dazu erzeugt tools/gen-foundry-tank-models.py.
 */
public class FoundryTankBlock extends BaseEntityBlock implements ICrucibleAcceptor {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public static final BooleanProperty OUTLET_NORTH = BooleanProperty.create("outlet_north");
    public static final BooleanProperty OUTLET_SOUTH = BooleanProperty.create("outlet_south");
    public static final BooleanProperty OUTLET_EAST = BooleanProperty.create("outlet_east");
    public static final BooleanProperty OUTLET_WEST = BooleanProperty.create("outlet_west");

    private static final Map<Direction, BooleanProperty> ANSCHLUSS = Map.of(
            Direction.NORTH, NORTH, Direction.SOUTH, SOUTH,
            Direction.EAST, EAST, Direction.WEST, WEST,
            Direction.UP, UP, Direction.DOWN, DOWN);

    private static final Map<Direction, BooleanProperty> AUSGUSS = Map.of(
            Direction.NORTH, OUTLET_NORTH, Direction.SOUTH, OUTLET_SOUTH,
            Direction.EAST, OUTLET_EAST, Direction.WEST, OUTLET_WEST);

    public static final MapCodec<FoundryTankBlock> CODEC = simpleCodec(FoundryTankBlock::new);

    public FoundryTankBlock(Properties properties) {
        super(properties);

        BlockState state = this.stateDefinition.any();
        for(BooleanProperty p : ANSCHLUSS.values()) state = state.setValue(p, Boolean.FALSE);
        for(BooleanProperty p : AUSGUSS.values()) state = state.setValue(p, Boolean.FALSE);
        this.registerDefaultState(state);
    }

    @Override public MapCodec<FoundryTankBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, OUTLET_NORTH, OUTLET_SOUTH, OUTLET_EAST, OUTLET_WEST);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryTankBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /** Ein Ausguss zaehlt nur, wenn sein Trog in diesem Tank haengt -- er zeigt dann zu uns. */
    private static boolean ausgussZeigtHierher(LevelReader level, BlockPos pos, Direction dir) {
        BlockState nachbar = level.getBlockState(pos.relative(dir));
        return nachbar.getBlock() instanceof FoundryOutletBlock
                && nachbar.getValue(FoundryOutletBlock.FACING) == dir;
    }

    private BlockState mitNachbarschaft(BlockState state, LevelReader level, BlockPos pos) {

        for(Map.Entry<Direction, BooleanProperty> eintrag : ANSCHLUSS.entrySet()) {
            boolean tank = level.getBlockState(pos.relative(eintrag.getKey())).is(NtmBlocks.FOUNDRY_TANK.get());
            state = state.setValue(eintrag.getValue(), tank);
        }

        for(Map.Entry<Direction, BooleanProperty> eintrag : AUSGUSS.entrySet()) {
            state = state.setValue(eintrag.getValue(), ausgussZeigtHierher(level, pos, eintrag.getKey()));
        }

        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.mitNachbarschaft(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return this.mitNachbarschaft(state, level, pos);
    }

    private static @Nullable ICrucibleAcceptor acceptor(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acc ? acc : null;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc != null && acc.canAcceptPartialPour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc == null ? stack : acc.pour(level, pos, dX, dY, dZ, side, stack);
    }

    /*
     * Seitlich nimmt der Tank NICHTS an. Das ist keine Nachlaessigkeit des Originals, sondern
     * Absicht: unter Tanks laeuft der Austausch ueber die Blockentitaeten, und ein Kanal soll
     * nicht in einen Tank laufen koennen.
     */
    @Override public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return stack; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(!stack.is(ItemTags.SHOVELS)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof FoundryTankBlockEntity tank)) return ItemInteractionResult.FAIL;

        if(tank.amount > 0 && tank.type != null) {

            ItemStack schrott = ScrapsItem.create(new MaterialStack(tank.type, tank.amount));

            if(player.getInventory().add(schrott)) {
                player.inventoryMenu.broadcastChanges();
            } else {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, schrott);
            }

            tank.amount = 0;
            tank.type = null;
            tank.setChanged();
        }

        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FoundryTankBlockEntity tank) {

            if(tank.amount > 0 && tank.type != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        ScrapsItem.create(new MaterialStack(tank.type, tank.amount)));
                tank.amount = 0;
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
