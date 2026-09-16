package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.PanelScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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

    /** Der Klick auf eine Erweiterung oeffnet die Oberflaeche ihrer Tafel. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos panel = PanelScreens.findPanel(level, pos, state.getValue(FACING));
        if(panel == null) return InteractionResult.PASS;

        if(level.getBlockEntity(panel) instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), panel);
        }
        return InteractionResult.CONSUME;
    }
}
