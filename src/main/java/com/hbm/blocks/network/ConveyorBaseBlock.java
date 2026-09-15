package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockConveyorBase.
 *
 * Das Foerderband. Es hat keine Blockentitaet und keinen Takt -- es sagt nur, wohin ein
 * Gegenstand, der auf ihm liegt, als naechstes soll. Bewegt wird der Gegenstand von sich selbst:
 * er ist eine eigene Entitaet (MovingItem), fragt bei jedem Takt den Block unter sich und faehrt
 * dorthin, wohin der ihn schickt. Faellt er von der Strecke, wird wieder ein gewoehnlicher
 * Gegenstand daraus.
 *
 * Im Original stecken Richtung und Kurve in einer einzigen Metadaten-Zahl: 2 bis 5 sind die vier
 * Himmelsrichtungen, plus 4 heisst Linkskurve, plus 8 Rechtskurve. Auf 1.21 ist die Richtung eine
 * Blockstate-Eigenschaft; die Kurve kommt in ConveyorBendableBlock dazu, so wie das Original sie
 * auch in einer eigenen Klasse fuehrt.
 *
 * Die Richtung zeigt dorthin, WOHER die Gegenstaende kommen: gefahren wird ihr entgegen. So
 * steht es im Original, und die Umsetzungstabelle der Bauwerke rechnet spaeter damit.
 */
public abstract class ConveyorBaseBlock extends Block implements IConveyorBelt, IToolable {

    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Vier Pixel hoch, wie im Original. */
    private static final VoxelShape COLLISION = Block.box(0D, 0D, 0D, 16D, 4D, 16D);

    /** Ab wann ein neu abgelegter Gegenstand aufgenommen wird -- im Original zehn Ticks. */
    private static final int PICKUP_DELAY = 10;

    public ConveyorBaseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        /* Die Richtung zeigt zum Setzenden zurueck, gefahren wird von ihm weg. */
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    // ------------------------------------------------------------------------------------
    // Wohin der Gegenstand soll
    // ------------------------------------------------------------------------------------

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return true;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {

        Direction dir = this.getTravelDirection(level, pos, itemPos);
        Vec3 snap = this.getClosestSnappingPosition(level, pos, itemPos);
        /* Der Rastpunkt, um eine Schrittweite entgegen der Bandrichtung verschoben. */
        Vec3 dest = new Vec3(snap.x - dir.getStepX() * speed, snap.y - dir.getStepY() * speed, snap.z - dir.getStepZ() * speed);
        Vec3 motion = new Vec3(dest.x - itemPos.x, dest.y - itemPos.y, dest.z - itemPos.z);
        double len = motion.length();

        if(len == 0D) return itemPos;

        return new Vec3(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
    }

    public Direction getInputDirection(Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(HORIZONTAL_FACING);
    }

    public Direction getOutputDirection(Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(HORIZONTAL_FACING).getOpposite();
    }

    public Direction getTravelDirection(Level level, BlockPos pos, Vec3 itemPos) {
        return level.getBlockState(pos).getValue(HORIZONTAL_FACING);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {

        Direction dir = this.getTravelDirection(level, pos, itemPos);

        double clampedX = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
        double clampedZ = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;

        if(dir.getStepX() != 0) x = clampedX;
        if(dir.getStepZ() != 0) z = clampedZ;

        return new Vec3(x, pos.getY() + 0.25, z);
    }

    // ------------------------------------------------------------------------------------
    // Aufnehmen und Drehen
    // ------------------------------------------------------------------------------------

    /**
     * Ein gewoehnlicher Gegenstand, der auf dem Band landet, wird gegen eine fahrende Entitaet
     * getauscht. Die kurze Wartezeit ist die des Originals: sie verhindert, dass ein gerade
     * ausgeworfener Gegenstand sofort wieder eingesammelt wird.
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        if(level.isClientSide) return;
        if(!(entity instanceof ItemEntity item)) return;
        if(entity.tickCount <= PICKUP_DELAY || !entity.isAlive()) return;

        MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), level);
        moving.setItemStack(item.getItem().copy());

        Vec3 snap = this.getClosestSnappingPosition(level, pos, entity.position());
        moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);

        level.addFreshEntity(moving);
        entity.discard();
    }

    /**
     * Der Schraubenzieher dreht das Band eine Vierteldrehung weiter. Was die Schleichtaste
     * zusaetzlich tut, entscheidet jede Bauform fuer sich: die waagerechten Baender schalten die
     * Kurve weiter, Steigband und Schacht wechseln die Bauform.
     */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, @Nullable Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;

        BlockState state = level.getBlockState(pos);

        if(player.isShiftKeyDown()) return this.onScrewSneaking(level, player, pos, state);

        level.setBlock(pos, state.setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING).getClockWise()), 3);
        return true;
    }

    /** Was die Schleichtaste tut. Ohne eigene Antwort bleibt das Band, wie es ist. */
    protected boolean onScrewSneaking(Level level, Player player, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected abstract MapCodec<? extends ConveyorBaseBlock> codec();
}
