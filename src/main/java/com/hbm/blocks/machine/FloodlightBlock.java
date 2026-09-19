package com.hbm.blocks.machine;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FloodlightBlockEntity;
import com.hbm.blocks.INBTBlockTransformable;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.Floodlight.
 *
 * Ein Flutlicht haengt an der Flaeche, auf die man es setzt, und laesst sich mit dem
 * Schraubendreher in der Hoehe verstellen -- stufenlos in Schritten von fuenf Grad.
 *
 * Das Original legt die Aufstellseite in den Metadaten ab und verwendet die Werte 6 und 7
 * fuer einen Sonderfall: haengt das Flutlicht an Decke oder Boden, gibt es zwei Lagen, je
 * nachdem, ob man beim Setzen nach Norden/Sueden oder nach Osten/Westen blickt. Hier ist
 * das die Eigenschaft FLIPPED neben der Richtung.
 */
public class FloodlightBlock extends BaseEntityBlock implements IToolable, ITooltipProvider, INBTBlockTransformable {

    public static final MapCodec<FloodlightBlock> CODEC = simpleCodec(FloodlightBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");

    public FloodlightBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(FLIPPED, false));
    }

    @Override public MapCodec<FloodlightBlock> codec() { return CODEC; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FLIPPED);
    }

    /** Die angeklickte Flaeche traegt das Flutlicht -- wie das Metadatenfeld des Originals. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if(placer != null) stelleEin(level, pos, placer, true);
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {
        if(tool != ToolType.SCREWDRIVER) return false;
        stelleEin(level, pos, player, false);
        return true;
    }

    /**
     * Richtet das Flutlicht nach dem Blick des Spielers aus: die Neigung auf fuenf Grad
     * gerundet, und bei Decken- oder Bodenmontage zusaetzlich die Lage quer dazu.
     */
    private static void stelleEin(Level level, BlockPos pos, LivingEntity spieler, boolean auchLage) {

        if(!(level.getBlockEntity(pos) instanceof FloodlightBlockEntity flutlicht)) return;

        BlockState zustand = level.getBlockState(pos);
        Direction seite = zustand.getValue(FACING);

        // Dieselbe Viertelkreis-Einteilung wie im Original: 0 = Sued, 1 = West, 2 = Nord, 3 = Ost.
        int viertel = Math.floorMod(Math.round(spieler.getYRot() * 4.0F / 360.0F), 4);
        float neigung = spieler.getXRot();

        if(seite == Direction.DOWN || seite == Direction.UP) {
            if(auchLage && (viertel == 0 || viertel == 2)) {
                level.setBlock(pos, zustand.setValue(FLIPPED, true), 3);
            }
            if(seite == Direction.UP && (viertel == 0 || viertel == 1)) neigung = 180F - neigung;
            if(seite == Direction.DOWN && (viertel == 0 || viertel == 3)) neigung = 180F - neigung;
        }

        flutlicht.setzeNeigung(-Math.round(neigung / 5F) * 5F);
    }

    @Override
    public BlockState transformState(BlockState state, Rotation rotation) {
        return INBTBlockTransformable.transformFacingState(state, rotation);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FloodlightBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
