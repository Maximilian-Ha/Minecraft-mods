package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.InfoPanel.
 *
 * Die Informationstafel laesst sich an jede der sechs Flaechen setzen; die Vorderseite
 * zeigt immer vom Block weg, auf den sie gesetzt wurde.
 */
public class InfoPanelBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<InfoPanelBlock> CODEC = simpleCodec(InfoPanelBlock::new);

    public InfoPanelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * Der Rueckgabetyp ist bewusst offen gehalten: die fortgeschrittene Tafel erbt von
     * dieser Klasse und bringt ihren eigenen Codec mit -- mit {@code MapCodec<InfoPanelBlock>}
     * liesse sich die Methode nicht ueberschreiben. Offener als {@code BaseEntityBlock} geht
     * nicht: dort steht genau diese Schranke.
     */
    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfoPanelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if(be instanceof InfoPanelBlockEntity panel) panel.tick();
        };
    }

    /**
     * Ein Rechtsklick mit leerer Hand faehrt zuerst die Beruehrung: traegt die Tafel die
     * Beruehrungsaufwertung und kann die Karte etwas damit anfangen, wirkt der Klick auf das
     * Ziel der Karte. Sonst -- und im Schleichen immer -- oeffnet er die Oberflaeche.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof InfoPanelBlockEntity panel)) return InteractionResult.PASS;

        if(!player.isShiftKeyDown() && panel.tryTouch(player)) return InteractionResult.CONSUME;

        player.openMenu(new SimpleMenuProvider(panel, panel.getDisplayName()), pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof InfoPanelBlockEntity panel) Containers.dropContents(level, pos, panel);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
