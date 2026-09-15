package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CranePartitionerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CranePartitioner.
 *
 * Der Portionierer ist SELBST EIN BAND -- er hat eine Richtung, Gegenstaende fahren auf ihm, und
 * am Ende setzt er sie in der abgemessenen Menge wieder darauf ab. Er sitzt deshalb mitten in
 * einer Strecke, nicht daneben.
 *
 * ZWOELF PIXEL HOCH statt der vier eines Bandes: er hat ein Gehaeuse, in dem die Warteschlange
 * steht.
 *
 * ABWEICHUNG: das Original zeichnet ihn mit einem eigenen Renderer, der das laufende Band im
 * Inneren zeigt. Hier steht ein schlichter Kasten -- die Form stimmt, das Bild ist einfacher.
 */
public class CranePartitionerBlock extends BaseEntityBlock implements IConveyorBelt, IEnterableBlock {

    public static final MapCodec<CranePartitionerBlock> CODEC = simpleCodec(CranePartitionerBlock::new);

    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Zwoelf Pixel hoch, wie im Original. */
    private static final VoxelShape SHAPE = Block.box(0D, 0D, 0D, 16D, 12D, 16D);

    public CranePartitionerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Wie beim Band zeigt die Richtung dorthin, WOHER die Gegenstaende kommen. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CranePartitionerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_PARTITIONER.get(), (l, p, s, be) -> be.updateEntity());
    }

    // ------------------------------------------------------------------------------------
    // Als Band
    // ------------------------------------------------------------------------------------

    /**
     * Dieselbe Rechnung wie beim geraden Band -- der Portionierer ist ein gerades Stueck
     * Strecke. Sie steht hier ein zweites Mal, weil er von einer anderen Basisklasse kommt: er
     * braucht eine Blockentitaet, das Band nicht.
     */
    @Override public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) { return true; }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {

        Direction dir = level.getBlockState(pos).getValue(HORIZONTAL_FACING);
        Vec3 snap = this.getClosestSnappingPosition(level, pos, itemPos);
        Vec3 dest = new Vec3(snap.x - dir.getStepX() * speed, snap.y - dir.getStepY() * speed, snap.z - dir.getStepZ() * speed);
        Vec3 motion = new Vec3(dest.x - itemPos.x, dest.y - itemPos.y, dest.z - itemPos.z);
        double len = motion.length();

        if(len == 0D) return itemPos;

        return new Vec3(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {

        Direction dir = level.getBlockState(pos).getValue(HORIZONTAL_FACING);

        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;

        if(dir.getStepX() != 0) x = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
        if(dir.getStepZ() != 0) z = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

        return new Vec3(x, pos.getY() + 0.25, z);
    }

    // ------------------------------------------------------------------------------------
    // Als annehmende Maschine
    // ------------------------------------------------------------------------------------

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {
        return level.getBlockState(pos).getValue(HORIZONTAL_FACING) == dir;
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {

        if(entity == null || entity.getItemStack().isEmpty()) return;
        if(!(level.getBlockEntity(pos) instanceof CranePartitionerBlockEntity be)) return;

        ItemStack rest = be.store(entity.getItemStack().copy());
        if(!rest.isEmpty()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rest);
    }

    /* Ein Paket laesst sich nicht portionieren -- es faehrt vorbei. */
    @Override public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { return false; }
    @Override public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CranePartitionerBlockEntity be) {
            Containers.dropContents(level, pos, be);
        }

        super.onRemove(state, level, pos, newState, moved);
    }
}
