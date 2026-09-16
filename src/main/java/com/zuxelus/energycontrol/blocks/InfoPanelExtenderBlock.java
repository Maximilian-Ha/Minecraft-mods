package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.blockentity.PanelScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.InfoPanelExtender.
 *
 * Vergroessert den Schirm einer Informationstafel. Der Block fuehrt selbst keine Daten und
 * braucht deshalb keine Block-Entitaet: welche Erweiterungen zu welcher Tafel gehoeren, rechnet
 * die Tafel aus (siehe {@link PanelScreens}).
 */
public class InfoPanelExtenderBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final MapCodec<InfoPanelExtenderBlock> CODEC = simpleCodec(InfoPanelExtenderBlock::new);

    public InfoPanelExtenderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<InfoPanelExtenderBlock> codec() {
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

    /**
     * Eine Erweiterung ist ein Stueck Schirm: der Klick darauf wirkt wie der Klick auf die
     * Tafel selbst -- erst die Beruehrung, sonst die Oberflaeche.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos panelPos = PanelScreens.findPanel(level, pos, state.getValue(FACING));
        if(panelPos == null) return InteractionResult.PASS;
        if(!(level.getBlockEntity(panelPos) instanceof InfoPanelBlockEntity panel)) return InteractionResult.PASS;

        if(!player.isShiftKeyDown() && panel.tryTouch(player)) return InteractionResult.CONSUME;

        player.openMenu(new SimpleMenuProvider(panel, panel.getDisplayName()), panelPos);
        return InteractionResult.CONSUME;
    }
}
